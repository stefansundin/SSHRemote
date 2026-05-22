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
import android.util.Log
import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
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
import androidx.compose.ui.text.TextMeasurer
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
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.stefansundin.sshremote.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import kotlin.math.roundToInt
import org.commonmark.node.Text as CmText

private const val TAG = "MarkdownText"

/** Blocks that take longer than this to render will emit a warning log. */
private const val SLOW_RENDER_THRESHOLD_MS = 16L
private const val SLOW_MARKDOWN_TOTAL_THRESHOLD_MS = 32L

private enum class MarkdownTableOverflowMode {
    Wrap,
    Scroll,
}

private val markdownParser: Parser = Parser.builder()
    .extensions(
        listOf(
            TablesExtension.create(),
            StrikethroughExtension.create(),
            TaskListItemsExtension.create(),
        ),
    )
    .build()

// ---------------------------------------------------------------------------
// Sealed IR (intermediate representation) built off the UI thread
// ---------------------------------------------------------------------------

private sealed interface MdBlock {
    data class MdHeading(val level: Int, val inline: List<MdInline>) : MdBlock
    data class MdParagraph(val inline: List<MdInline>) : MdBlock
    data class MdCode(val code: String, val language: String?) : MdBlock
    data class MdBulletList(val items: List<MdListItem>, val depth: Int) : MdBlock
    data class MdOrderedList(val items: List<MdListItem>, val startNumber: Int, val depth: Int) : MdBlock
    data class MdBlockQuote(val children: List<MdBlock>) : MdBlock
    data class MdTable(val id: Int, val rows: List<MdTableRow>) : MdBlock
    object MdThematicBreak : MdBlock
}

private data class MdTableRow(
    val isHeader: Boolean,
    val cells: List<List<MdInline>>,
    val bodyIndex: Int,
)

private data class MdListItem(
    val blocks: List<MdBlock>,
    val taskChecked: Boolean? = null,
)

private data class ParseState(
    var nextTableId: Int = 0,
)

private sealed interface MdInline {
    data class MdText(val text: String) : MdInline
    data class MdBold(val children: List<MdInline>) : MdInline
    data class MdItalic(val children: List<MdInline>) : MdInline
    data class MdStrikethrough(val children: List<MdInline>) : MdInline
    data class MdInlineCode(val code: String) : MdInline
    data class MdLink(val url: String, val children: List<MdInline>) : MdInline
    data class MdImage(val alt: List<MdInline>) : MdInline
    data class MdHtmlTag(val literal: String) : MdInline
    object MdSoftBreak : MdInline
    object MdHardBreak : MdInline
}

// ---------------------------------------------------------------------------
// Parsing helpers (runs on IO thread)
// ---------------------------------------------------------------------------

private fun parseInlines(node: Node): List<MdInline> {
    val result = mutableListOf<MdInline>()
    var child = node.firstChild
    while (child != null) {
        when (child) {
            is CmText -> result.add(MdInline.MdText(child.literal))
            is StrongEmphasis -> result.add(MdInline.MdBold(parseInlines(child)))
            is Emphasis -> result.add(MdInline.MdItalic(parseInlines(child)))
            is Strikethrough -> result.add(MdInline.MdStrikethrough(parseInlines(child)))
            is Code -> result.add(MdInline.MdInlineCode(child.literal))
            is Link -> result.add(MdInline.MdLink(child.destination, parseInlines(child)))
            is Image -> result.add(MdInline.MdImage(parseInlines(child)))
            is HtmlInline -> result.add(MdInline.MdHtmlTag(child.literal))
            is SoftLineBreak -> result.add(MdInline.MdSoftBreak)
            is HardLineBreak -> result.add(MdInline.MdHardBreak)
            else -> result.addAll(parseInlines(child)) // fallback: recurse into unknown inline
        }
        child = child.next
    }
    return result
}

private fun parseListItem(item: ListItem, listDepth: Int, parseState: ParseState): MdListItem {
    val marker = item.firstChild as? TaskListItemMarker
    val blocks = parseBlocks(item, listDepth + 1, parseState)

    return MdListItem(blocks = blocks, taskChecked = marker?.isChecked)
}

private fun parseTableCells(row: TableRow): List<List<MdInline>> {
    val cells = mutableListOf<List<MdInline>>()
    var cell = row.firstChild
    while (cell != null) {
        if (cell is TableCell) {
            cells.add(parseInlines(cell))
        }
        cell = cell.next
    }
    return cells
}

private fun parseTableRows(table: TableBlock): List<MdTableRow> {
    val rows = mutableListOf<MdTableRow>()
    var bodyRowIndex = 0

    var section = table.firstChild
    while (section != null) {
        when (section) {
            is TableHead -> {
                var row = section.firstChild
                while (row != null) {
                    if (row is TableRow) {
                        rows.add(MdTableRow(isHeader = true, cells = parseTableCells(row), bodyIndex = -1))
                    }
                    row = row.next
                }
            }

            is TableBody -> {
                var row = section.firstChild
                while (row != null) {
                    if (row is TableRow) {
                        rows.add(
                            MdTableRow(
                                isHeader = false,
                                cells = parseTableCells(row),
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

private fun parseBlocks(node: Node, listDepth: Int = 0, parseState: ParseState = ParseState()): List<MdBlock> {
    val result = mutableListOf<MdBlock>()
    var child = node.firstChild
    while (child != null) {
        when (child) {
            is Heading -> result.add(MdBlock.MdHeading(child.level, parseInlines(child)))
            is Paragraph -> result.add(MdBlock.MdParagraph(parseInlines(child)))
            is FencedCodeBlock -> result.add(
                MdBlock.MdCode(child.literal.trimEnd(), child.info?.takeIf { it.isNotBlank() }),
            )

            is IndentedCodeBlock -> result.add(MdBlock.MdCode(child.literal.trimEnd(), null))
            is ThematicBreak -> result.add(MdBlock.MdThematicBreak)
            is BlockQuote -> result.add(MdBlock.MdBlockQuote(parseBlocks(child, listDepth, parseState)))
            is BulletList -> {
                val items = mutableListOf<MdListItem>()
                var item = child.firstChild
                while (item != null) {
                    if (item is ListItem) items.add(parseListItem(item, listDepth, parseState))
                    item = item.next
                }
                result.add(MdBlock.MdBulletList(items, listDepth))
            }

            is OrderedList -> {
                val items = mutableListOf<MdListItem>()
                var item = child.firstChild
                while (item != null) {
                    if (item is ListItem) items.add(parseListItem(item, listDepth, parseState))
                    item = item.next
                }
                result.add(MdBlock.MdOrderedList(items, child.markerStartNumber, listDepth))
            }

            is TableBlock -> {
                val rows = parseTableRows(child)
                if (rows.isNotEmpty()) {
                    result.add(MdBlock.MdTable(id = parseState.nextTableId++, rows = rows))
                }
            }

            is HtmlBlock -> {
                val htmlWithoutComments = stripHtmlComments(child.literal).trimEnd()
                if (htmlWithoutComments.isNotBlank()) {
                    result.add(MdBlock.MdCode(code = htmlWithoutComments, language = "html"))
                }
            }

            // Ignore HTML blocks etc. for now
            else -> Unit
        }
        child = child.next
    }
    return result
}

private fun parseMarkdown(markdown: String): List<MdBlock> {
    val document = markdownParser.parse(markdown) as Document
    return parseBlocks(document, parseState = ParseState())
}

private fun logMarkdownTiming(parseMs: Long, renderMs: Long, totalMs: Long, totalBlocks: Int) {
    val message = "markdown parse=${parseMs}ms render=${renderMs}ms total=${totalMs}ms blocks=$totalBlocks"
    if (totalMs > SLOW_MARKDOWN_TOTAL_THRESHOLD_MS) {
        Log.w(TAG, "$message (threshold: ${SLOW_MARKDOWN_TOTAL_THRESHOLD_MS}ms)")
    } else {
        Log.d(TAG, message)
    }
}

// ---------------------------------------------------------------------------
// Inline HTML helpers (runs on IO thread)
// ---------------------------------------------------------------------------

/**
 * Minimal mutable state for inline HTML formatting tags.
 * Depth counters allow balanced open/close pairs to nest correctly.
 */
private data class MdInlineHtmlState(
    var boldDepth: Int = 0,
    var italicDepth: Int = 0,
    var strikethroughDepth: Int = 0,
)

/** Regex that matches simple HTML tags we support inline. */
private val supportedInlineTagRegex =
    Regex("""^<\s*(/?)\s*(br|strong|b|em|i|s|strike|del)\s*(/?)>$""", RegexOption.IGNORE_CASE)

/**
 * Applies a supported inline HTML tag to [state] and returns the action to take:
 * - `null` → tag consumed, nothing to append
 * - `"\n"` → line break should be appended
 */
private fun applyInlineHtmlTag(literal: String, state: MdInlineHtmlState): String? {
    val match = supportedInlineTagRegex.matchEntire(literal.trim()) ?: return null
    val isClosing = match.groupValues[1].isNotEmpty()
    val tagName = match.groupValues[2].lowercase()
    val isSelfClosing = match.groupValues[3].isNotEmpty()

    when (tagName) {
        "br" -> return "\n"
        "strong", "b" -> if (!isSelfClosing) {
            if (isClosing) state.boldDepth = (state.boldDepth - 1).coerceAtLeast(0)
            else state.boldDepth++
        }

        "em", "i" -> if (!isSelfClosing) {
            if (isClosing) state.italicDepth = (state.italicDepth - 1).coerceAtLeast(0)
            else state.italicDepth++
        }

        "s", "strike", "del" -> if (!isSelfClosing) {
            if (isClosing) state.strikethroughDepth = (state.strikethroughDepth - 1).coerceAtLeast(0)
            else state.strikethroughDepth++
        }
    }
    return null
}

// ---------------------------------------------------------------------------
// Search state
// ---------------------------------------------------------------------------

private data class SearchRenderState(
    val regex: Regex,
    val currentMatchIndex: Int,
    val normalHighlightBackground: Color,
    val currentHighlightBackground: Color,
    var nextMatchIndex: Int = 0,
)

// ---------------------------------------------------------------------------
// AnnotatedString builder (runs on IO thread)
// ---------------------------------------------------------------------------

private fun buildInlineAnnotatedString(
    inlines: List<MdInline>,
    onLinkClick: (String) -> Unit,
    linkColor: Color,
    codeBackground: Color,
    searchState: SearchRenderState?,
): AnnotatedString = buildAnnotatedString {
    val htmlState = MdInlineHtmlState()

    fun appendStyledText(text: String, style: SpanStyle?) {
        if (style != null) withStyle(style) { append(text) } else append(text)
    }

    fun appendSearchHighlightedText(text: String, baseStyle: SpanStyle?) {
        if (searchState == null || text.isEmpty()) {
            appendStyledText(text, baseStyle)
            return
        }

        var index = 0
        for (match in searchState.regex.findAll(text)) {
            if (match.range.first > index) {
                appendStyledText(text.substring(index, match.range.first), baseStyle)
            }

            val isCurrentMatch = searchState.nextMatchIndex == searchState.currentMatchIndex
            val highlightBackground = if (isCurrentMatch) {
                searchState.currentHighlightBackground
            } else {
                searchState.normalHighlightBackground
            }
            val highlightedStyle = (baseStyle ?: SpanStyle()).merge(SpanStyle(background = highlightBackground))
            appendStyledText(match.value, highlightedStyle)

            searchState.nextMatchIndex++
            index = match.range.last + 1
        }

        if (index < text.length) {
            appendStyledText(text.substring(index), baseStyle)
        }
    }

    fun appendWithHtmlStyles(text: String, codeStyle: SpanStyle? = null) {
        val boldStyle = if (htmlState.boldDepth > 0) FontWeight.Bold else null
        val italicStyle = if (htmlState.italicDepth > 0) FontStyle.Italic else null
        val strikeStyle = if (htmlState.strikethroughDepth > 0) TextDecoration.LineThrough else null

        val htmlSpan = if (boldStyle != null || italicStyle != null || strikeStyle != null) {
            SpanStyle(fontWeight = boldStyle, fontStyle = italicStyle, textDecoration = strikeStyle)
        } else null

        val combined = when {
            codeStyle != null && htmlSpan != null -> SpanStyle(
                fontFamily = codeStyle.fontFamily,
                background = codeStyle.background,
                fontWeight = htmlSpan.fontWeight,
                fontStyle = htmlSpan.fontStyle,
                textDecoration = htmlSpan.textDecoration,
            )

            codeStyle != null -> codeStyle
            htmlSpan != null -> htmlSpan
            else -> null
        }

        appendSearchHighlightedText(text, combined)
    }

    fun appendInlines(list: List<MdInline>) {
        for (inline in list) {
            when (inline) {
                is MdInline.MdText -> appendWithHtmlStyles(inline.text)
                is MdInline.MdBold -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    appendInlines(inline.children)
                }

                is MdInline.MdItalic -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    appendInlines(inline.children)
                }

                is MdInline.MdStrikethrough -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                    appendInlines(inline.children)
                }

                is MdInline.MdInlineCode -> appendWithHtmlStyles(
                    inline.code,
                    SpanStyle(fontFamily = FontFamily.Monospace, background = codeBackground),
                )

                is MdInline.MdLink -> withLink(
                    LinkAnnotation.Clickable(
                        tag = inline.url,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline,
                            ),
                        ),
                        linkInteractionListener = { onLinkClick(inline.url) },
                    ),
                ) {
                    appendInlines(inline.children)
                }

                is MdInline.MdImage -> {
                    val altText = buildInlineAnnotatedString(
                        inline.alt,
                        onLinkClick,
                        linkColor,
                        codeBackground,
                        searchState,
                    ).text
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(if (altText.isNotBlank()) "[$altText]" else "[image]")
                    }
                }

                is MdInline.MdHtmlTag -> {
                    val action = applyInlineHtmlTag(inline.literal, htmlState)
                    if (action != null) append(action)
                }

                MdInline.MdSoftBreak -> append(" ")
                MdInline.MdHardBreak -> append("\n")
            }
        }
    }
    appendInlines(inlines)
}

// ---------------------------------------------------------------------------
// Pre-rendered block data (produced off-UI thread, consumed on UI thread)
// ---------------------------------------------------------------------------

private sealed interface RenderedBlock {
    data class RenderedHeading(val level: Int, val text: AnnotatedString) : RenderedBlock
    data class RenderedParagraph(val text: AnnotatedString) : RenderedBlock
    data class RenderedCode(val code: AnnotatedString, val language: String?) : RenderedBlock
    data class RenderedBulletList(val items: List<RenderedListItem>, val depth: Int) : RenderedBlock
    data class RenderedOrderedList(val items: List<RenderedListItem>, val startNumber: Int, val depth: Int) :
        RenderedBlock

    data class RenderedBlockQuote(val children: List<RenderedBlock>) : RenderedBlock
    data class RenderedTable(val id: Int, val rows: List<RenderedTableRow>) : RenderedBlock
    object RenderedThematicBreak : RenderedBlock
}

private data class RenderedTableRow(
    val isHeader: Boolean,
    val cells: List<AnnotatedString>,
    val bodyIndex: Int,
)

private data class RenderedListItem(
    val blocks: List<RenderedBlock>,
    val taskChecked: Boolean? = null,
)

private fun renderBlocks(
    blocks: List<MdBlock>,
    onLinkClick: (String) -> Unit,
    linkColor: Color,
    codeBackground: Color,
    searchState: SearchRenderState?,
): List<RenderedBlock> = blocks.map { block ->
    when (block) {
        is MdBlock.MdHeading -> RenderedBlock.RenderedHeading(
            block.level,
            buildInlineAnnotatedString(
                block.inline,
                onLinkClick,
                linkColor,
                codeBackground,
                searchState,
            ),
        )

        is MdBlock.MdParagraph -> RenderedBlock.RenderedParagraph(
            buildInlineAnnotatedString(
                block.inline,
                onLinkClick,
                linkColor,
                codeBackground,
                searchState,
            ),
        )

        is MdBlock.MdCode -> RenderedBlock.RenderedCode(
            code = highlightSearchMatchesInText(
                text = block.code,
                searchState = searchState,
            ),
            language = block.language,
        )

        is MdBlock.MdThematicBreak -> RenderedBlock.RenderedThematicBreak
        is MdBlock.MdBlockQuote -> RenderedBlock.RenderedBlockQuote(
            renderBlocks(
                block.children,
                onLinkClick,
                linkColor,
                codeBackground,
                searchState,
            ),
        )

        is MdBlock.MdTable -> RenderedBlock.RenderedTable(
            id = block.id,
            rows = block.rows.map { row ->
                RenderedTableRow(
                    isHeader = row.isHeader,
                    cells = row.cells.map { cell ->
                        buildInlineAnnotatedString(
                            cell,
                            onLinkClick,
                            linkColor,
                            codeBackground,
                            searchState,
                        )
                    },
                    bodyIndex = row.bodyIndex,
                )
            },
        )

        is MdBlock.MdBulletList -> RenderedBlock.RenderedBulletList(
            block.items.map { item ->
                RenderedListItem(
                    blocks = renderBlocks(
                        item.blocks,
                        onLinkClick,
                        linkColor,
                        codeBackground,
                        searchState,
                    ),
                    taskChecked = item.taskChecked,
                )
            },
            block.depth,
        )

        is MdBlock.MdOrderedList -> RenderedBlock.RenderedOrderedList(
            block.items.map { item ->
                RenderedListItem(
                    blocks = renderBlocks(
                        item.blocks,
                        onLinkClick,
                        linkColor,
                        codeBackground,
                        searchState,
                    ),
                    taskChecked = item.taskChecked,
                )
            },
            block.startNumber,
            block.depth,
        )
    }
}

private fun highlightSearchMatchesInText(
    text: String,
    searchState: SearchRenderState?,
): AnnotatedString {
    if (searchState == null || text.isEmpty()) return AnnotatedString(text)

    return buildAnnotatedString {
        var index = 0
        for (match in searchState.regex.findAll(text)) {
            if (match.range.first > index) {
                append(text.substring(index, match.range.first))
            }

            val isCurrentMatch = searchState.nextMatchIndex == searchState.currentMatchIndex
            val highlightBackground = if (isCurrentMatch) {
                searchState.currentHighlightBackground
            } else {
                searchState.normalHighlightBackground
            }
            withStyle(SpanStyle(background = highlightBackground)) {
                append(match.value)
            }
            searchState.nextMatchIndex++
            index = match.range.last + 1
        }

        if (index < text.length) {
            append(text.substring(index))
        }
    }
}

// ---------------------------------------------------------------------------
// Public composable
// ---------------------------------------------------------------------------

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    searchNextRequest: Int = 0,
    onSearchPositionChanged: ((current: Int, total: Int) -> Unit)? = null,
) {
    val clipboard = LocalClipboard.current
    val density = LocalDensity.current
    val resources = LocalResources.current
    val view = LocalView.current
    val windowInfo = LocalWindowInfo.current
    val scope = rememberCoroutineScope()
    val maxDialogHeight = with(density) { windowInfo.containerSize.height.toDp() * 0.9f }

    val linkColor = MaterialTheme.colorScheme.primary
    val codeBackground = MaterialTheme.colorScheme.surfaceVariant
    val searchHighlightBackground = MaterialTheme.colorScheme.tertiaryContainer
    val currentSearchHighlightBackground = MaterialTheme.colorScheme.primaryContainer
    val trimmedSearchQuery = remember(searchQuery) { searchQuery.trim() }
    val searchRegex = remember(trimmedSearchQuery) {
        trimmedSearchQuery.takeIf { it.isNotEmpty() }?.let {
            Regex(Regex.escape(it), RegexOption.IGNORE_CASE)
        }
    }
    var currentSearchMatchIndex by rememberSaveable(trimmedSearchQuery) { mutableIntStateOf(0) }
    var handledSearchNextRequest by rememberSaveable(trimmedSearchQuery) { mutableIntStateOf(searchNextRequest) }
    var selectedLink by rememberSaveable { mutableStateOf<String?>(null) }
    val onLinkClick = remember<(String) -> Unit> { { url -> selectedLink = url } }

    var renderedBlocks by remember(markdown) { mutableStateOf<List<RenderedBlock>?>(null) }

    LaunchedEffect(
        markdown,
        linkColor,
        codeBackground,
        searchRegex,
        currentSearchMatchIndex,
        searchHighlightBackground,
        currentSearchHighlightBackground,
        onLinkClick,
    ) {
        val startNs = System.nanoTime()
        val parsedBlocks = withContext(Dispatchers.Default) { parseMarkdown(markdown) }
        val parseElapsedMs = (System.nanoTime() - startNs) / 1_000_000L

        val renderStartNs = System.nanoTime()
        val rendered = withContext(Dispatchers.Default) {
            val searchState = searchRegex?.let {
                SearchRenderState(
                    regex = it,
                    currentMatchIndex = currentSearchMatchIndex,
                    normalHighlightBackground = searchHighlightBackground,
                    currentHighlightBackground = currentSearchHighlightBackground,
                )
            }
            renderBlocks(
                parsedBlocks,
                onLinkClick,
                linkColor,
                codeBackground,
                searchState,
            )
        }

        val renderElapsedMs = (System.nanoTime() - renderStartNs) / 1_000_000L
        val totalElapsedMs = (System.nanoTime() - startNs) / 1_000_000L
        logMarkdownTiming(
            parseMs = parseElapsedMs,
            renderMs = renderElapsedMs,
            totalMs = totalElapsedMs,
            totalBlocks = parsedBlocks.size,
        )
        renderedBlocks = rendered
    }

    val blocks = renderedBlocks
    if (blocks == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.padding(16.dp),
        ) {
            CircularProgressIndicator()
        }
    } else {
        val totalSearchMatches = remember(blocks, searchRegex) {
            calculateTotalSearchMatches(blocks, searchRegex)
        }
        val verticalScrollState = rememberScrollState()
        val blockPositions = remember(blocks) { MutableList(blocks.size) { 0f } }
        val blockHeights = remember(blocks) { IntArray(blocks.size) }
        var layoutVersion by remember(blocks) { mutableIntStateOf(0) }
        var viewportHeightPx by remember { mutableIntStateOf(0) }
        val imeBottomPx = WindowInsets.ime.getBottom(density)

        fun scrollToCurrentMatch(topLevelBlockIndex: Int) {
            if (topLevelBlockIndex !in blockPositions.indices) return
            val blockTop = blockPositions[topLevelBlockIndex]
            val blockHeight = blockHeights[topLevelBlockIndex]
            val visibleViewportPx = (viewportHeightPx - imeBottomPx).coerceAtLeast(1)
            if (visibleViewportPx <= 0 || blockHeight <= 0) return

            val centeredTarget = (blockTop - ((visibleViewportPx - blockHeight) / 2f)).roundToInt()
                .coerceIn(0, verticalScrollState.maxValue)
            if (verticalScrollState.value != centeredTarget) {
                // Keep behavior close to browser find by centering the matched section when possible.
                scope.launch { verticalScrollState.animateScrollTo(centeredTarget) }
            }
        }

        LaunchedEffect(trimmedSearchQuery) {
            currentSearchMatchIndex = 0
            handledSearchNextRequest = searchNextRequest
        }

        LaunchedEffect(searchNextRequest, totalSearchMatches) {
            if (searchNextRequest <= 0 || totalSearchMatches <= 0) return@LaunchedEffect
            if (searchNextRequest == handledSearchNextRequest) return@LaunchedEffect
            handledSearchNextRequest = searchNextRequest
            currentSearchMatchIndex = (currentSearchMatchIndex + 1) % totalSearchMatches
        }

        LaunchedEffect(currentSearchMatchIndex, totalSearchMatches) {
            if (totalSearchMatches <= 0) {
                if (currentSearchMatchIndex != 0) currentSearchMatchIndex = 0
                onSearchPositionChanged?.invoke(0, 0)
                return@LaunchedEffect
            }

            if (currentSearchMatchIndex >= totalSearchMatches) {
                currentSearchMatchIndex = 0
                return@LaunchedEffect
            }

            onSearchPositionChanged?.invoke(currentSearchMatchIndex + 1, totalSearchMatches)
        }

        LaunchedEffect(
            currentSearchMatchIndex,
            totalSearchMatches,
            blocks,
            searchRegex,
            layoutVersion,
            viewportHeightPx,
        ) {
            if (totalSearchMatches <= 0 || searchRegex == null) return@LaunchedEffect
            val topLevelBlockIndex = findTopLevelBlockIndexForMatch(
                blocks = blocks,
                regex = searchRegex,
                globalMatchIndex = currentSearchMatchIndex,
            )
            scrollToCurrentMatch(topLevelBlockIndex)
        }

        SelectionContainer {
            Column(
                modifier = modifier
                    .onSizeChanged { viewportHeightPx = it.height }
                    .verticalScroll(verticalScrollState),
            ) {
                for ((index, block) in blocks.withIndex()) {
                    Box(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                val top = coordinates.positionInParent().y
                                val height = coordinates.size.height
                                if (index in blockPositions.indices) {
                                    if (blockPositions[index] != top || blockHeights[index] != height) {
                                        blockPositions[index] = top
                                        blockHeights[index] = height
                                        layoutVersion++
                                    }
                                }
                            },
                    ) {
                        RenderedBlockComposable(block)
                    }
                }
            }
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

private fun calculateTotalSearchMatches(blocks: List<RenderedBlock>, regex: Regex?): Int {
    if (regex == null) return 0
    return blocks.sumOf { countMatchesInRenderedBlock(it, regex) }
}

private fun countMatchesInRenderedBlock(block: RenderedBlock, regex: Regex): Int {
    return when (block) {
        is RenderedBlock.RenderedHeading -> regex.findAll(block.text.text).count()
        is RenderedBlock.RenderedParagraph -> regex.findAll(block.text.text).count()
        is RenderedBlock.RenderedCode -> regex.findAll(block.code.text).count()
        is RenderedBlock.RenderedThematicBreak -> 0
        is RenderedBlock.RenderedBlockQuote -> block.children.sumOf { countMatchesInRenderedBlock(it, regex) }
        is RenderedBlock.RenderedTable -> block.rows.sumOf { row ->
            row.cells.sumOf { cell -> regex.findAll(cell.text).count() }
        }

        is RenderedBlock.RenderedBulletList -> block.items.sumOf { item ->
            item.blocks.sumOf { countMatchesInRenderedBlock(it, regex) }
        }

        is RenderedBlock.RenderedOrderedList -> block.items.sumOf { item ->
            item.blocks.sumOf { countMatchesInRenderedBlock(it, regex) }
        }
    }
}

private fun findTopLevelBlockIndexForMatch(
    blocks: List<RenderedBlock>,
    regex: Regex,
    globalMatchIndex: Int,
): Int {
    var consumed = 0
    for ((index, block) in blocks.withIndex()) {
        val count = countMatchesInRenderedBlock(block, regex)
        if (globalMatchIndex < consumed + count) {
            return index
        }
        consumed += count
    }
    return 0
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
                        imageVector = Icons.Outlined.ContentCopy,
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

// ---------------------------------------------------------------------------
// Block composables
// ---------------------------------------------------------------------------

@Composable
private fun RenderedBlockComposable(block: RenderedBlock, isInsideList: Boolean = false) {
    val startNs = System.nanoTime()

    when (block) {
        is RenderedBlock.RenderedHeading -> {
            val style = when (block.level) {
                1 -> MaterialTheme.typography.headlineLarge
                2 -> MaterialTheme.typography.headlineMedium
                3 -> MaterialTheme.typography.headlineSmall
                4 -> MaterialTheme.typography.titleLarge
                5 -> MaterialTheme.typography.titleMedium
                else -> MaterialTheme.typography.titleSmall
            }
            Text(
                text = block.text,
                style = style,
                modifier = Modifier.padding(
                    top = if (block.level <= 2) 8.dp else 4.dp,
                    bottom = 4.dp,
                ),
            )
        }

        is RenderedBlock.RenderedParagraph -> {
            Text(
                text = block.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = if (isInsideList) 0.dp else 8.dp),
            )
        }

        is RenderedBlock.RenderedCode -> {
            val codeBackground = MaterialTheme.colorScheme.surfaceVariant
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(codeBackground)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = block.code,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                )
            }
        }

        is RenderedBlock.RenderedThematicBreak -> {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        is RenderedBlock.RenderedTable -> {
            RenderedTableComposable(table = block)
        }

        is RenderedBlock.RenderedBlockQuote -> {
            val quoteBarColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            Row(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .height(IntrinsicSize.Max),
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(quoteBarColor),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                ) {
                    for (child in block.children) {
                        RenderedBlockComposable(child)
                    }
                }
            }
        }

        is RenderedBlock.RenderedBulletList -> {
            Column(
                modifier = Modifier.padding(
                    bottom = if (block.depth == 0) 8.dp else 0.dp,
                ),
            ) {
                for (item in block.items) {
                    Row(modifier = Modifier.padding(bottom = 0.dp)) {
                        if (item.taskChecked != null) {
                            Icon(
                                imageVector = if (item.taskChecked) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                contentDescription = if (item.taskChecked) "Checked" else "Unchecked",
                                modifier = Modifier
                                    .padding(start = (block.depth * 12).dp, end = 8.dp)
                                    .size(20.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            Text(
                                text = "• ",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = (block.depth * 12).dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            for (child in item.blocks) {
                                RenderedBlockComposable(child, isInsideList = true)
                            }
                        }
                    }
                }
            }
        }

        is RenderedBlock.RenderedOrderedList -> {
            Column(
                modifier = Modifier.padding(
                    bottom = if (block.depth == 0) 8.dp else 0.dp,
                ),
            ) {
                block.items.forEachIndexed { index, item ->
                    Row(modifier = Modifier.padding(bottom = 0.dp)) {
                        if (item.taskChecked != null) {
                            Icon(
                                imageVector = if (item.taskChecked) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                contentDescription = if (item.taskChecked) "Checked" else "Unchecked",
                                modifier = Modifier
                                    .padding(start = (block.depth * 12).dp, end = 8.dp)
                                    .size(20.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            Text(
                                text = "${block.startNumber + index}. ",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = (block.depth * 12).dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            for (child in item.blocks) {
                                RenderedBlockComposable(child, isInsideList = true)
                            }
                        }
                    }
                }
            }
        }
    }

    val elapsedMs = (System.nanoTime() - startNs) / 1_000_000L
    if (elapsedMs > SLOW_RENDER_THRESHOLD_MS) {
        Log.w(
            TAG,
            "Slow block render (${block::class.simpleName}): ${elapsedMs}ms (threshold: ${SLOW_RENDER_THRESHOLD_MS}ms)",
        )
    }
}

private data class MarkdownTableLayoutMetrics(
    val availableColumnsWidth: Dp,
    val columnWidths: List<Dp>,
    val measuredTableWidth: Dp,
    val tableContentWidth: Dp,
) {
    val shouldShowToggle: Boolean get() = measuredTableWidth > availableColumnsWidth
}

private fun measureTableColumnContentWidths(
    rows: List<RenderedTableRow>,
    textMeasurer: TextMeasurer,
    bodyStyle: TextStyle,
    headerStyle: TextStyle,
): List<Float> {
    val columnCount = rows.maxOfOrNull { it.cells.size } ?: return emptyList()
    val maxWidthsPxByColumn = FloatArray(columnCount)
    rows.forEach { row ->
        for (index in 0 until columnCount) {
            val text = row.cells.getOrNull(index) ?: AnnotatedString("")
            val measuredWidth = textMeasurer.measure(
                text = text,
                style = if (row.isHeader) headerStyle else bodyStyle,
                softWrap = false,
            ).size.width.toFloat()
            maxWidthsPxByColumn[index] = maxOf(maxWidthsPxByColumn[index], measuredWidth)
        }
    }
    return maxWidthsPxByColumn.toList()
}

private fun calculatePreferredColumnWidthsFromPx(
    measuredColumnContentWidthsPx: List<Float>,
    overflow: MarkdownTableOverflowMode,
    densityScale: Float,
): List<Dp> {
    if (measuredColumnContentWidthsPx.isEmpty()) return emptyList()

    val minColumnWidthPx = 56.dp.value * densityScale
    val cellHorizontalPaddingPx = 16.dp.value * densityScale
    val glyphOverhangBufferPx = 2.dp.value * densityScale
    val maxWrapColumnContentWidthPx = 480.dp.value * densityScale

    return measuredColumnContentWidthsPx.map { measuredWidthPx ->
        val measuredContentWidth = measuredWidthPx.coerceAtLeast(0f)
        val contentWidth = when (overflow) {
            MarkdownTableOverflowMode.Wrap -> measuredContentWidth.coerceAtMost(maxWrapColumnContentWidthPx)
            MarkdownTableOverflowMode.Scroll -> measuredContentWidth
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
    if (available <= 0.dp) return preferred

    val totalPreferred = preferred.sumDp().value
    if (totalPreferred <= 0f) return preferred

    val scale = available.value / totalPreferred
    if (scale >= 1f) {
        return preferred.map { width -> width * scale }
    }

    if (!allowShrink) return preferred

    val minColumnWidth = 48.dp
    return preferred.map { width ->
        (width * scale).coerceAtLeast(minColumnWidth)
    }
}

private fun calculateTableLayoutMetrics(
    maxWidth: Dp,
    preferredColumnWidths: List<Dp>,
    overflow: MarkdownTableOverflowMode,
    rowPadding: Dp,
    verticalDividerThickness: Dp,
    outerBorderThickness: Dp,
): MarkdownTableLayoutMetrics {
    val preferredDividerWidth = verticalDividerThickness *
            (preferredColumnWidths.size - 1).coerceAtLeast(0).toFloat()
    val availableColumnsWidth = (
            maxWidth - rowPadding - preferredDividerWidth - (outerBorderThickness * 2)
            ).coerceAtLeast(0.dp)
    val columnWidths = fitColumnWidthsToAvailable(
        preferred = preferredColumnWidths,
        available = availableColumnsWidth,
        allowShrink = overflow == MarkdownTableOverflowMode.Wrap,
    )
    val columnDividerWidth = verticalDividerThickness * (columnWidths.size - 1).coerceAtLeast(0).toFloat()
    val measuredTableWidth = preferredColumnWidths.sumDp() + preferredDividerWidth
    val tableContentWidth = when (overflow) {
        MarkdownTableOverflowMode.Scroll ->
            columnWidths.sumDp() + columnDividerWidth + rowPadding + (outerBorderThickness * 2)

        MarkdownTableOverflowMode.Wrap -> maxWidth
    }
    return MarkdownTableLayoutMetrics(
        availableColumnsWidth = availableColumnsWidth,
        columnWidths = columnWidths,
        measuredTableWidth = measuredTableWidth,
        tableContentWidth = tableContentWidth,
    )
}

private fun List<Dp>.sumDp(): Dp = fold(0.dp) { acc, width -> acc + width }

@Composable
private fun RenderTableRow(
    rowData: RenderedTableRow,
    columnWidths: List<Dp>,
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
            val cellText = rowData.cells.getOrNull(index) ?: AnnotatedString("")

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

@Composable
private fun RenderedTableComposable(table: RenderedBlock.RenderedTable) {
    if (table.rows.isEmpty()) return

    val tableStateKey = "table-overflow-${table.id}"
    var overflow by rememberSaveable(tableStateKey) { mutableStateOf(MarkdownTableOverflowMode.Wrap) }

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val bodyCellStyle = MaterialTheme.typography.bodySmall
    val headerCellStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)

    // Cache column measurement per table/style to keep recompositions cheap.
    val measuredColumnContentWidthsPx = remember(
        table.rows,
        textMeasurer,
        bodyCellStyle,
        headerCellStyle,
    ) {
        measureTableColumnContentWidths(
            rows = table.rows,
            textMeasurer = textMeasurer,
            bodyStyle = bodyCellStyle,
            headerStyle = headerCellStyle,
        )
    }

    val preferredColumnWidths = remember(
        measuredColumnContentWidthsPx,
        overflow,
        density.density,
    ) {
        calculatePreferredColumnWidthsFromPx(
            measuredColumnContentWidthsPx = measuredColumnContentWidthsPx,
            overflow = overflow,
            densityScale = density.density,
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        val rowPadding = 8.dp
        val verticalDividerThickness = 1.dp
        val outerBorderThickness = 1.dp
        val tableLayout = calculateTableLayoutMetrics(
            maxWidth = maxWidth,
            preferredColumnWidths = preferredColumnWidths,
            overflow = overflow,
            rowPadding = rowPadding,
            verticalDividerThickness = verticalDividerThickness,
            outerBorderThickness = outerBorderThickness,
        )

        val borderColor = MaterialTheme.colorScheme.outlineVariant
        val zebraColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        val headerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        val tableShape = MaterialTheme.shapes.small

        val tableContainerModifier = Modifier
            .width(tableLayout.tableContentWidth)
            .clip(tableShape)
            .border(width = outerBorderThickness, color = borderColor, shape = tableShape)

        val tableGrid: @Composable () -> Unit = {
            Column(modifier = tableContainerModifier) {
                table.rows.forEachIndexed { index, rowData ->
                    RenderTableRow(
                        rowData = rowData,
                        columnWidths = tableLayout.columnWidths,
                        borderColor = borderColor,
                        rowBackground = when {
                            rowData.isHeader -> headerColor
                            rowData.bodyIndex % 2 == 0 -> Color.Transparent
                            else -> zebraColor
                        },
                        softWrap = overflow == MarkdownTableOverflowMode.Wrap,
                        verticalDividerThickness = verticalDividerThickness,
                    )

                    if (index < table.rows.lastIndex) {
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
            if (tableLayout.shouldShowToggle) {
                DisableSelection {
                    TextButton(
                        onClick = {
                            overflow = when (overflow) {
                                MarkdownTableOverflowMode.Wrap -> MarkdownTableOverflowMode.Scroll
                                MarkdownTableOverflowMode.Scroll -> MarkdownTableOverflowMode.Wrap
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .padding(horizontal = 6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.toggle_word_wrap),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            if (overflow == MarkdownTableOverflowMode.Scroll) {
                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    tableGrid()
                }
            } else {
                tableGrid()
            }
        }
    }
}

private val mdHtmlCommentRegex = Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL)

private fun stripHtmlComments(html: String): String {
    return html.replace(mdHtmlCommentRegex, "")
}
