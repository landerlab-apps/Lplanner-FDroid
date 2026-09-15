/*
 * zplan_jni.c — Lplanner Android v1.0.0
 *
 * JNI bridge to the ZPlanKit C engine (czplan.c), mirroring the Swift API in
 * ZPlanKit/Sources/ZPlanKit/ZPlanKit.swift exactly:
 *
 *   profile.dat text (+ optional tissue.dat text)
 *     ->  report, warnings, tissue, refused
 *
 * The whole engine is driven through the profile text, so nothing of zp_config
 * has to be marshalled field by field. Keeping the boundary this narrow is what
 * lets iOS and Android share one engine with no divergence.
 *
 * ============================ WARNING ============================
 * THIS SOFTWARE PLANS DECOMPRESSION DIVES AND CAN KILL YOU.
 * Never dive a schedule from this program without independently
 * verifying it against trusted tables/software.
 * =================================================================
 */

#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "czplan.h"

#define REPORT_BUF   32768
#define TISSUE_BUF   2048
#define ERR_BUF      256

/* Same constant the Swift layer uses to convert bar <-> ATM in tissue files. */
static const double kAtm = 1.01325;

/* ------------------------------------------------------------------ *
 *  tissue.dat parsing — byte-compatible with the original ZPlan files
 *  and with TissueState.tissueFileText on the Swift side.
 * ------------------------------------------------------------------ */
static void load_tissues(const char *text, zp_config *cfg)
{
    double n2[ZP_COMPARTMENTS], he[ZP_COMPARTMENTS], cns = 0.0;
    int n_n2 = 0, n_he = 0;
    const char *p = text;

    while (*p) {
        const char *eol = strchr(p, '\n');
        size_t len = eol ? (size_t)(eol - p) : strlen(p);
        char line[128];
        if (len >= sizeof(line)) len = sizeof(line) - 1;
        memcpy(line, p, len);
        line[len] = '\0';

        /* trim */
        char *s = line;
        while (*s && isspace((unsigned char)*s)) s++;
        char *e = s + strlen(s);
        while (e > s && isspace((unsigned char)e[-1])) *--e = '\0';

        if (strncmp(s, "CNS:", 4) == 0) {
            cns = atof(s + 4);
        } else if (*s) {
            char *end = NULL;
            double v = strtod(s, &end);
            if (end && end != s) {
                if (n_n2 < ZP_COMPARTMENTS)      n2[n_n2++] = v * kAtm;
                else if (n_he < ZP_COMPARTMENTS) he[n_he++] = v * kAtm;
            }
        }
        if (!eol) break;
        p = eol + 1;
    }

    if (n_n2 != ZP_COMPARTMENTS || n_he != ZP_COMPARTMENTS) return;  /* ignore malformed */

    memcpy(cfg->init_pn2, n2, sizeof(n2));
    memcpy(cfg->init_phe, he, sizeof(he));
    cfg->init_cns_pct = cns;
    cfg->have_initial_tissues = true;
}

/* Serialise end-of-dive tissue state for the next repetitive dive. */
static void write_tissues(const zp_result *r, char *buf, size_t buflen)
{
    int n = snprintf(buf, buflen, "CNS:%f\n", r->end_cns_pct);
    for (int i = 0; i < ZP_COMPARTMENTS && n > 0 && (size_t)n < buflen; i++)
        n += snprintf(buf + n, buflen - n, "%f\n", r->end_pn2[i] / kAtm);
    for (int i = 0; i < ZP_COMPARTMENTS && n > 0 && (size_t)n < buflen; i++)
        n += snprintf(buf + n, buflen - n, "%f\n", r->end_phe[i] / kAtm);
}

/* The fifth element is the refusal flag. Without it the Kotlin side cannot
 * tell "here is a plan, with advisories" from "there is no plan, and this is
 * why", and it strips the warnings in both cases - which on a refusal leaves
 * the diver looking at NO PLAN COMPUTED and no reason. */
static jobjectArray make_result(JNIEnv *env, const char *err, const char *report,
                                const char *warnings, const char *tissue,
                                int refused)
{
    jclass cls = (*env)->FindClass(env, "java/lang/String");
    jobjectArray out = (*env)->NewObjectArray(env, 5, cls, NULL);
    const char *vals[5] = { err, report, warnings, tissue,
                            refused ? "1" : "0" };
    for (int i = 0; i < 5; i++) {
        if (!vals[i]) continue;
        jstring s = (*env)->NewStringUTF(env, vals[i]);
        (*env)->SetObjectArrayElement(env, out, i, s);
        (*env)->DeleteLocalRef(env, s);
    }
    return out;
}

/*
 * Returns String[5]: { error-or-null, report, warnings, tissueFile,
 * "1" or "0" for refused }.
 * When element 0 is non-null the run failed and the rest are null.
 */
JNIEXPORT jobjectArray JNICALL
Java_com_landerlab_lplanner_ZPlan_nativePlan(JNIEnv *env, jobject thiz,
                                             jstring jProfile, jstring jTissue)
{
    (void)thiz;   /* ZPlan is a Kotlin object: natives bind as instance methods */
    jobjectArray ret = NULL;

    zp_config *cfg = calloc(1, sizeof(zp_config));
    zp_result *res = calloc(1, sizeof(zp_result));
    char *report   = calloc(1, REPORT_BUF);
    char *tissue   = calloc(1, TISSUE_BUF);
    char err[ERR_BUF];
    err[0] = '\0';

    if (!cfg || !res || !report || !tissue) {
        ret = make_result(env, "Out of memory", NULL, NULL, NULL, 0);
        goto done;
    }

    const char *profile = (*env)->GetStringUTFChars(env, jProfile, NULL);
    if (!profile) {
        ret = make_result(env, "Could not read profile text", NULL, NULL, NULL, 0);
        goto done;
    }

    zp_config_init(cfg);
    int rc = zp_parse_profile(profile, cfg, err, sizeof(err));
    (*env)->ReleaseStringUTFChars(env, jProfile, profile);

    if (rc != 0) {
        char msg[ERR_BUF + 32];
        snprintf(msg, sizeof(msg), "Profile parse error: %s", err);
        ret = make_result(env, msg, NULL, NULL, NULL, 0);
        goto done;
    }

    if (jTissue) {
        const char *t = (*env)->GetStringUTFChars(env, jTissue, NULL);
        if (t) {
            load_tissues(t, cfg);
            (*env)->ReleaseStringUTFChars(env, jTissue, t);
        }
    }

    if (zp_plan(cfg, res) != 0) {
        ret = make_result(env, "Decompression planning failed", NULL, NULL,
                          NULL, 0);
        goto done;
    }

    zp_report(cfg, res, report, REPORT_BUF);
    write_tissues(res, tissue, TISSUE_BUF);

    ret = make_result(env, NULL, report, res->warnings, tissue,
                      res->refused ? 1 : 0);

done:
    free(cfg); free(res); free(report); free(tissue);
    return ret;
}

JNIEXPORT jstring JNICALL
Java_com_landerlab_lplanner_ZPlan_nativeVersion(JNIEnv *env, jobject thiz)
{
    (void)thiz;
    return (*env)->NewStringUTF(env, zp_version());
}
