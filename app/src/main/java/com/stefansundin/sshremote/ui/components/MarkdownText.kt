/*
 * SSH Remote
 * Copyright (C) 2026  Stefan Sundin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.stefansundin.sshremote.ui.components

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.stefansundin.sshremote.R
import kotlinx.coroutines.launch
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.task.list.items.TaskListItemMarker
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Document
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser
import org.commonmark.node.Text as CmText

enum class MarkdownHorizontalOverflow {
    Wrap,
    Scroll,
}

private val markdownParser: Parser by lazy {
    Parser.builder()
        .extensions(
            listOf(
                TablesExtension.create(),
                StrikethroughExtension.create(),
                TaskListItemsExtension.create(),
            ),
        )
        .build()
}

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    selectableText: Boolean = true,
    codeBlockOverflow: MarkdownHorizontalOverflow = MarkdownHorizontalOverflow.Wrap,
    tableOverflow: MarkdownHorizontalOverflow = MarkdownHorizontalOverflow.Wrap,
) {
    val view = LocalView.current
    val clipboard = LocalClipboard.current
    val resources = LocalResources.current
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val scope = rememberCoroutineScope()
    var selectedLink by rememberSaveable { mutableStateOf<String?>(null) }
    val maxDialogHeight = with(density) { windowInfo.containerSize.height.toDp() * 0.9f }
    val document = remember(markdown) { markdownParser.parse(markdown) as Document }

    OptionalSelectionContainer(selectableText = selectableText) {
        Column(modifier = modifier) {
            MarkdownDocument(
                document = document,
                onLinkClick = { url -> selectedLink = url },
                codeBlockOverflow = codeBlockOverflow,
                tableOverflow = tableOverflow,
            )
        }
    }

    selectedLink?.let { url ->
        MarkdownLinkDialog(
            url = url,
            maxDialogHeight = maxDialogHeight,
            onDismiss = { selectedLink = null },
            onCopy = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                val clipData = ClipData.newPlainText(resources.getString(R.string.link_destination), url)
                scope.launch { clipboard.setClipEntry(clipData.toClipEntry()) }
            },
            onOpenInBrowser = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
            },
        )
    }
}

@Composable
private fun MarkdownDocument(
    document: Document,
    onLinkClick: (String) -> Unit,
    codeBlockOverflow: MarkdownHorizontalOverflow,
    tableOverflow: MarkdownHorizontalOverflow,
) {
    var child = document.firstChild
    while (child != null) {
        MarkdownBlock(
            node = child,
            onLinkClick = onLinkClick,
            codeBlockOverflow = codeBlockOverflow,
            tableOverflow = tableOverflow,
        )
        child = child.next
    }
}

@Composable
private fun MarkdownBlock(
    node: Node,
    onLinkClick: (String) -> Unit,
    codeBlockOverflow: MarkdownHorizontalOverflow,
    tableOverflow: MarkdownHorizontalOverflow,
    listDepth: Int = 0,
) {
    when (node) {
        is Heading -> {
            val style = when (node.level) {
                1 -> MaterialTheme.typography.headlineLarge
                2 -> MaterialTheme.typography.headlineMedium
                3 -> MaterialTheme.typography.headlineSmall
                4 -> MaterialTheme.typography.titleLarge
                5 -> MaterialTheme.typography.titleMedium
                else -> MaterialTheme.typography.titleSmall
            }
            Text(
                text = buildInlineAnnotatedString(node, onLinkClick),
                style = style,
                modifier = Modifier.padding(bottom = 4.dp, top = if (node.level <= 2) 8.dp else 4.dp),
            )
        }

        is Paragraph -> {
            Text(
                text = buildInlineAnnotatedString(node, onLinkClick),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        is FencedCodeBlock -> {
            CodeBlock(
                code = node.literal.trimEnd(),
                language = node.info?.takeIf { it.isNotBlank() },
                overflow = codeBlockOverflow,
            )
        }

        is IndentedCodeBlock -> {
            CodeBlock(code = node.literal.trimEnd(), language = null, overflow = codeBlockOverflow)
        }

        is BulletList -> {
            Column(modifier = Modifier.padding(bottom = if (listDepth == 0) 8.dp else 0.dp)) {
                var item = node.firstChild
                while (item != null) {
                    if (item is ListItem) {
                        val marker = findTaskListItemMarker(item)
                        Row {
                            Text(
                                text = if (marker == null) "• " else "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = (listDepth * 12).dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                MarkdownListItemChildren(
                                    item,
                                    onLinkClick,
                                    codeBlockOverflow,
                                    tableOverflow,
                                    marker,
                                    listDepth,
                                )
                            }
                        }
                    }
                    item = item.next
                }
            }
        }

        is OrderedList -> {
            Column(modifier = Modifier.padding(bottom = if (listDepth == 0) 8.dp else 0.dp)) {
                var item = node.firstChild
                var index = node.markerStartNumber
                while (item != null) {
                    if (item is ListItem) {
                        val marker = findTaskListItemMarker(item)
                        Row {
                            Text(
                                text = if (marker == null) "$index. " else "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = (listDepth * 12).dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                MarkdownListItemChildren(
                                    item,
                                    onLinkClick,
                                    codeBlockOverflow,
                                    tableOverflow,
                                    marker,
                                    listDepth,
                                )
                            }
                        }
                        index++
                    }
                    item = item.next
                }
            }
        }

        is BlockQuote -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(bottom = 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(3.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
                Column(modifier = Modifier.padding(start = 10.dp, top = 2.dp, bottom = 2.dp)) {
                    var child = node.firstChild
                    while (child != null) {
                        MarkdownBlock(
                            node = child,
                            onLinkClick = onLinkClick,
                            codeBlockOverflow = codeBlockOverflow,
                            tableOverflow = tableOverflow,
                            listDepth = listDepth,
                        )
                        child = child.next
                    }
                }
            }
        }

        is ThematicBreak -> {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        is TableBlock -> {
            MarkdownTable(
                table = node,
                onLinkClick = onLinkClick,
                overflow = tableOverflow,
            )
        }

        is HtmlBlock -> {
            val htmlWithoutComments = stripHtmlComments(node.literal)
            if (htmlWithoutComments.isNotBlank()) {
                CodeBlock(code = htmlWithoutComments.trimEnd(), language = "html", overflow = codeBlockOverflow)
            }
        }
    }
}

private val htmlCommentRegex = Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL)

private fun stripHtmlComments(html: String): String {
    return html.replace(htmlCommentRegex, "")
}

@Composable
private fun MarkdownListItemChildren(
    item: ListItem,
    onLinkClick: (String) -> Unit,
    codeBlockOverflow: MarkdownHorizontalOverflow,
    tableOverflow: MarkdownHorizontalOverflow,
    taskMarker: TaskListItemMarker?,
    listDepth: Int,
) {
    var child = item.firstChild
    var consumedTaskMarker = false
    while (child != null) {
        when (child) {
            is Paragraph -> {
                if (!consumedTaskMarker && taskMarker != null) {
                    RenderParagraphWithTaskMarker(child, taskMarker, onLinkClick)
                    consumedTaskMarker = true
                } else {
                    Text(
                        text = buildInlineAnnotatedString(child, onLinkClick),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            is BulletList, is OrderedList -> {
                MarkdownBlock(child, onLinkClick, codeBlockOverflow, tableOverflow, listDepth + 1)
            }

            else -> MarkdownBlock(child, onLinkClick, codeBlockOverflow, tableOverflow, listDepth)
        }
        child = child.next
    }
}

@Composable
private fun CodeBlock(
    code: String,
    language: String?,
    overflow: MarkdownHorizontalOverflow,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Column {
            if (!language.isNullOrBlank()) {
                Text(
                    text = language,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
            if (overflow == MarkdownHorizontalOverflow.Wrap) {
                Text(
                    text = code,
                    style = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            } else {
                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    Text(
                        text = code,
                        style = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MarkdownTable(
    table: TableBlock,
    onLinkClick: (String) -> Unit,
    overflow: MarkdownHorizontalOverflow,
) {
    val tableRows = remember(table) { collectTableRows(table) }
    if (tableRows.isEmpty()) return

    val tableStateKey = remember(tableRows) {
        tableRows.joinToString(separator = "\n") { row ->
            row.cells.joinToString(separator = "|") { cell -> buildInlinePlainText(cell) }
        }
    }
    var currentOverflow by rememberSaveable(tableStateKey) { mutableStateOf(overflow) }

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val bodyCellStyle = MaterialTheme.typography.bodySmall
    val headerCellStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
    val measuredColumnWidths = remember(
        tableRows,
        currentOverflow,
        textMeasurer,
        bodyCellStyle,
        headerCellStyle,
        density.density,
    ) {
        calculatePreferredColumnWidths(
            rows = tableRows,
            overflow = currentOverflow,
            textMeasurer = textMeasurer,
            bodyStyle = bodyCellStyle,
            headerStyle = headerCellStyle,
            densityScale = density.density,
        )
    }

    Surface(
        color = Color.Transparent,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val rowPadding = 8.dp
            val verticalDividerThickness = 1.dp
            val outerBorderThickness = 1.dp
            val dividerTotalWidth = verticalDividerThickness *
                    (measuredColumnWidths.size - 1).coerceAtLeast(0).toFloat()
            val availableColumnsWidth = (
                    maxWidth -
                            rowPadding -
                            dividerTotalWidth -
                            (outerBorderThickness * 2)
                    ).coerceAtLeast(0.dp)
            val columnWidths = fitColumnWidthsToAvailable(
                preferred = measuredColumnWidths,
                available = availableColumnsWidth,
                allowShrink = currentOverflow == MarkdownHorizontalOverflow.Wrap,
            )
            val columnsTotalWidth = columnWidths.sumDp()
            val columnDividersWidth = verticalDividerThickness *
                    (columnWidths.size - 1).coerceAtLeast(0).toFloat()
            val measuredColumnsTotalWidth = measuredColumnWidths.sumDp()
            val measuredTableWidth = measuredColumnsTotalWidth + columnDividersWidth
            val shouldShowToggle = measuredTableWidth > availableColumnsWidth
            val tableContentWidth = when (currentOverflow) {
                MarkdownHorizontalOverflow.Scroll -> {
                    columnsTotalWidth + columnDividersWidth + rowPadding + (outerBorderThickness * 2)
                }

                MarkdownHorizontalOverflow.Wrap -> maxWidth
            }

            val borderColor = MaterialTheme.colorScheme.outlineVariant
            val zebraColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            val headerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            val tableShape = MaterialTheme.shapes.small

            val tableContainerModifier = Modifier
                .width(tableContentWidth)
                .clip(tableShape)
                .border(width = outerBorderThickness, color = borderColor, shape = tableShape)

            val tableGrid: @Composable () -> Unit = {
                Column(modifier = tableContainerModifier) {
                    tableRows.forEachIndexed { index, rowData ->
                        RenderTableRow(
                            rowData = rowData,
                            columnWidths = columnWidths,
                            onLinkClick = onLinkClick,
                            borderColor = borderColor,
                            rowBackground = when {
                                rowData.isHeader -> headerColor
                                rowData.bodyIndex % 2 == 0 -> Color.Transparent
                                else -> zebraColor
                            },
                            softWrap = currentOverflow == MarkdownHorizontalOverflow.Wrap,
                            verticalDividerThickness = verticalDividerThickness,
                        )

                        val nextRow = tableRows.getOrNull(index + 1)
                        if (nextRow != null) {
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                thickness = 1.dp,
                                color = borderColor,
                            )
                        }
                    }
                }
            }

            Column {
                if (shouldShowToggle) {
                    DisableSelection {
                        TextButton(
                            onClick = {
                                currentOverflow = when (currentOverflow) {
                                    MarkdownHorizontalOverflow.Wrap -> MarkdownHorizontalOverflow.Scroll
                                    MarkdownHorizontalOverflow.Scroll -> MarkdownHorizontalOverflow.Wrap
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp)
                                .height(24.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 4.dp,
                                vertical = 0.dp,
                            ),
                        ) {
                            Text(
                                text = stringResource(R.string.toggle_word_wrap),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }

                if (currentOverflow == MarkdownHorizontalOverflow.Scroll) {
                    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        tableGrid()
                    }
                } else {
                    tableGrid()
                }
            }
        }
    }
}

private data class TableRowData(
    val isHeader: Boolean,
    val cells: List<TableCell>,
    val bodyIndex: Int,
)

private fun collectTableRows(table: TableBlock): List<TableRowData> {
    val rows = mutableListOf<TableRowData>()
    var bodyRowIndex = 0

    var section = table.firstChild
    while (section != null) {
        when (section) {
            is TableHead -> {
                var row = section.firstChild
                while (row != null) {
                    if (row is TableRow) {
                        rows.add(
                            TableRowData(
                                isHeader = true,
                                cells = collectTableCells(row),
                                bodyIndex = -1,
                            ),
                        )
                    }
                    row = row.next
                }
            }

            is TableBody -> {
                var row = section.firstChild
                while (row != null) {
                    if (row is TableRow) {
                        rows.add(
                            TableRowData(
                                isHeader = false,
                                cells = collectTableCells(row),
                                bodyIndex = bodyRowIndex++,
                            ),
                        )
                    }
                    row = row.next
                }
            }
        }
        section = section.next
    }

    return rows
}

private fun collectTableCells(row: TableRow): List<TableCell> {
    val cells = mutableListOf<TableCell>()
    var cell = row.firstChild
    while (cell != null) {
        if (cell is TableCell) {
            cells.add(cell)
        }
        cell = cell.next
    }
    return cells
}

private fun calculatePreferredColumnWidths(
    rows: List<TableRowData>,
    overflow: MarkdownHorizontalOverflow,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    bodyStyle: TextStyle,
    headerStyle: TextStyle,
    densityScale: Float,
): List<Dp> {
    val columnCount = rows.maxOfOrNull { it.cells.size } ?: return emptyList()
    val maxWidthsPxByColumn = FloatArray(columnCount)

    rows.forEach { row ->
        for (index in 0 until columnCount) {
            val text = row.cells.getOrNull(index)
                ?.let { buildInlineAnnotatedString(it) { } }
                ?: AnnotatedString("")

            val measuredWidth = textMeasurer.measure(
                text = text,
                style = if (row.isHeader) headerStyle else bodyStyle,
                softWrap = false,
            ).size.width.toFloat()

            maxWidthsPxByColumn[index] = maxOf(maxWidthsPxByColumn[index], measuredWidth)
        }
    }

    val minColumnWidthPx = 56.dp.value * densityScale
    val cellHorizontalPaddingPx = 16.dp.value * densityScale
    val glyphOverhangBufferPx = 2.dp.value * densityScale
    val maxWrapColumnContentWidthPx = 520.dp.value * densityScale

    return List(columnCount) { index ->
        val measuredContentWidth = maxWidthsPxByColumn[index].coerceAtLeast(0f)
        val contentWidth = when (overflow) {
            MarkdownHorizontalOverflow.Wrap -> measuredContentWidth.coerceAtMost(maxWrapColumnContentWidthPx)
            MarkdownHorizontalOverflow.Scroll -> measuredContentWidth
        }
        val widthPx = (contentWidth + cellHorizontalPaddingPx + glyphOverhangBufferPx)
            .coerceAtLeast(minColumnWidthPx)
        (widthPx / densityScale).dp
    }
}

private fun fitColumnWidthsToAvailable(
    preferred: List<Dp>,
    available: Dp,
    allowShrink: Boolean,
): List<Dp> {
    if (preferred.isEmpty()) return preferred

    val totalPreferred = preferred.sumDp()
    if (available <= 0.dp) return preferred

    val scale = available.value / totalPreferred.value.coerceAtLeast(1f)
    if (scale >= 1f) {
        return preferred.map { width -> width * scale }
    }

    if (!allowShrink) return preferred

    val minColumnWidth = 48.dp
    return preferred.map { width ->
        (width * scale).coerceAtLeast(minColumnWidth)
    }
}

private fun List<Dp>.sumDp(): Dp = fold(0.dp) { acc, width -> acc + width }

@Composable
private fun RenderTableRow(
    rowData: TableRowData,
    columnWidths: List<Dp>,
    onLinkClick: (String) -> Unit,
    borderColor: Color,
    rowBackground: Color,
    softWrap: Boolean,
    verticalDividerThickness: Dp,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(rowBackground),
    ) {
        columnWidths.forEachIndexed { index, columnWidth ->
            val cell = rowData.cells.getOrNull(index)
            val cellText = cell?.let { buildInlineAnnotatedString(it, onLinkClick) } ?: AnnotatedString("")

            Text(
                text = cellText,
                style = if (rowData.isHeader) {
                    MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                } else {
                    MaterialTheme.typography.bodySmall
                },
                softWrap = softWrap,
                modifier = Modifier
                    .width(columnWidth)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )

            if (index < columnWidths.lastIndex) {
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    thickness = verticalDividerThickness,
                    color = borderColor,
                )
            }
        }
    }
}

private fun findTaskListItemMarker(item: ListItem): TaskListItemMarker? {
    return item.firstChild as? TaskListItemMarker
}

@Composable
private fun RenderParagraphWithTaskMarker(
    paragraph: Paragraph,
    marker: TaskListItemMarker,
    onLinkClick: (String) -> Unit,
) {
    val checked = marker.isChecked
    val content = buildInlineAnnotatedString(paragraph, onLinkClick)

    Row {
        Text(
            text = if (checked) "☑" else "☐",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun buildInlinePlainText(node: Node): String = buildString {
    appendInlinePlainText(node)
}.trim()

private fun StringBuilder.appendInlinePlainText(node: Node) {
    var child = node.firstChild
    while (child != null) {
        when (child) {
            is CmText -> append(child.literal)
            is SoftLineBreak -> append(' ')
            is HardLineBreak -> append(' ')
            is Code -> append(child.literal)
            is HtmlInline -> {}
            else -> appendInlinePlainText(child)
        }
        child = child.next
    }
}

private fun buildInlineAnnotatedString(node: Node, onLinkClick: (String) -> Unit): AnnotatedString {
    return buildAnnotatedString {
        appendInlineChildren(node, onLinkClick)
    }
}

private fun AnnotatedString.Builder.appendInlineChildren(node: Node, onLinkClick: (String) -> Unit) {
    var child = node.firstChild
    while (child != null) {
        appendInlineNode(child, onLinkClick)
        child = child.next
    }
}

private fun AnnotatedString.Builder.appendInlineNode(node: Node, onLinkClick: (String) -> Unit) {
    when (node) {
        is CmText -> append(node.literal)
        is SoftLineBreak -> append(" ")
        is HardLineBreak -> append("\n")
        is Emphasis -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
            appendInlineChildren(node, onLinkClick)
        }

        is StrongEmphasis -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            appendInlineChildren(node, onLinkClick)
        }

        is Strikethrough -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
            appendInlineChildren(node, onLinkClick)
        }

        is Code -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
            append(node.literal)
        }

        is Link -> {
            val url = node.destination
            withLink(
                LinkAnnotation.Clickable(
                    tag = url,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = Color.Unspecified,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ),
                    linkInteractionListener = { onLinkClick(url) },
                ),
            ) {
                appendInlineChildren(node, onLinkClick)
            }
        }

        is Image -> {
            val alt = buildAnnotatedString { appendInlineChildren(node, onLinkClick) }.text
            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                append(if (alt.isNotBlank()) "[$alt]" else "[image]")
            }
        }

        is HtmlInline -> {
            val inlineHtmlWithoutComments = stripHtmlComments(node.literal)
            if (inlineHtmlWithoutComments.isNotEmpty()) {
                append(inlineHtmlWithoutComments)
            }
        }
    }
}

@Composable
private fun OptionalSelectionContainer(
    selectableText: Boolean,
    content: @Composable () -> Unit,
) {
    if (selectableText) {
        SelectionContainer(content = content)
    } else {
        content()
    }
}

@Composable
private fun MarkdownLinkDialog(
    url: String,
    maxDialogHeight: Dp,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onOpenInBrowser: () -> Unit,
) {
    val context = LocalContext.current
    val canOpenInBrowser = remember(url) { canOpenInBrowser(url) }

    AlertDialog(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .heightIn(max = maxDialogHeight),
        title = { Text(stringResource(R.string.link_destination)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                SelectionContainer {
                    Text(
                        text = url,
                        style = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                    )
                }
            }
        },
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        confirmButton = {
            if (canOpenInBrowser) {
                Button(
                    onClick = {
                        onOpenInBrowser()
                        if (openLinkInBrowser(context, url)) {
                            onDismiss()
                        }
                    },
                ) {
                    Text(stringResource(R.string.open_in_browser))
                }
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onCopy) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(R.string.copy),
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.copy))
                }
                Spacer(Modifier.size(8.dp))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.close))
                }
            }
        },
    )
}

private fun canOpenInBrowser(url: String): Boolean {
    val scheme = url.toUri().scheme?.lowercase() ?: return false
    return scheme == "http" || scheme == "https"
}

private fun openLinkInBrowser(context: Context, url: String): Boolean {
    if (!canOpenInBrowser(url)) return false
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        .addCategory(Intent.CATEGORY_BROWSABLE)
    return runCatching {
        context.startActivity(intent)
        true
    }.getOrDefault(false)
}
