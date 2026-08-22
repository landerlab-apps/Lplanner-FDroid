package com.landerlab.lplanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.landerlab.lplanner.PlannerModel

/**
 * LogSheet.kt — Lplanner Android v1.0.0
 *
 * Read-only view of plans recorded this session. Entries are written by
 * Calculate, not by opening this sheet — opening it used to append a duplicate
 * of the current plan every time.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSheet(m: PlannerModel, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
            Text("Log", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (m.log.isNotEmpty()) {
                Text(
                    "  ${m.log.size} plan${if (m.log.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Box(Modifier.weight(1f))
            if (m.log.isNotEmpty()) TextButton(onClick = { m.clearLog() }) { Text("Clear") }
            TextButton(onClick = onDismiss) { Text("Done") }
        }
        HorizontalDivider()

        if (m.log.isEmpty()) {
            Text(
                "No plans logged this session.\n" +
                    "Every successful Calculate is recorded here automatically.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth().padding(32.dp),
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 640.dp).padding(16.dp),
            ) {
                items(m.log, key = { it.id }) { e ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                e.stamp,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Box(Modifier.weight(1f))
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Delete this entry",
                                modifier = Modifier.size(18.dp).clickable { m.removeLog(e) },
                            )
                        }
                        // Which dive and settings produced this plan.
                        Text(
                            e.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        // Fixed-width report: scroll rather than wrap, or the
                        // columns stop lining up with their headers.
                        SelectionContainer {
                            Text(
                                e.text,
                                style = MonoTextSmall,
                                softWrap = false,
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}
