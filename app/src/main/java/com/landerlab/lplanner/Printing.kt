package com.landerlab.lplanner

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * Printing.kt — Lplanner Android v1.6.0
 *
 * The plan is a monospace table whose alignment carries meaning: the depth,
 * stop, run, PO2 and EAD columns only line up because every glyph is the same
 * width. Android has no plain-text print path, so the schedule is wrapped in a
 * <pre> block and handed to the platform's WebView print adapter, which is the
 * documented way to produce a PDF or a paper page from an app.
 *
 * The WebView is deliberately held in a field for the life of the print job.
 * A local would be eligible for collection the moment print() returns, and the
 * adapter renders asynchronously afterwards — the job then fails silently with
 * nothing on screen to explain it.
 */
object Printing {

    private var printer: WebView? = null

    /** Escape the four characters that would otherwise be read as markup. */
    private fun escape(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    /**
     * Send [plan] to the system print dialogue, from where it can go to a
     * printer or be saved as a PDF.
     *
     * @param jobName appears in the print queue and as the default PDF filename
     */
    fun printPlan(context: Context, plan: String, jobName: String = "Lplanner dive plan") {
        if (plan.isBlank()) return

        // 9 pt is the largest size that keeps the 54-column report inside a
        // portrait A4/Letter margin without the WebView reflowing it. Anything
        // wider wraps, and a wrapped column is a misread column.
        val html = """
            <html><head><meta charset="utf-8">
            <style>
              @page { margin: 12mm; }
              body { margin: 0; }
              pre  { font-family: monospace; font-size: 9pt; line-height: 1.25;
                     white-space: pre; }
            </style></head>
            <body><pre>${escape(plan)}</pre></body></html>
        """.trimIndent()

        val web = WebView(context)
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val pm = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
                pm.print(
                    jobName,
                    view.createPrintDocumentAdapter(jobName),
                    PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.UNKNOWN_PORTRAIT)
                        .build(),
                )
                // The adapter holds its own reference from here on.
                printer = null
            }
        }
        printer = web
        web.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)
    }
}
