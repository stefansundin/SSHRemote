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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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

private const val TAG = "MarkdownText"

/** Blocks that take longer than this to render will emit a warning log. */
private const val SLOW_RENDER_THRESHOLD_MS = 16L
private const val SLOW_MARKDOWN_TOTAL_THRESHOLD_MS = 32L

// Lightweight parser — no table/HTML extensions to keep it fast
private val markdownParser: Parser = Parser.builder()
    .extensions(
        listOf(
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
    object MdThematicBreak : MdBlock
}

private data class MdListItem(
    val blocks: List<MdBlock>,
    val taskChecked: Boolean? = null,
)

private sealed interface MdInline {
    data class MdText(val text: String) : MdInline
    data class MdBold(val children: List<MdInline>) : MdInline
    data class MdItalic(val children: List<MdInline>) : MdInline
    data class MdStrikethrough(val children: List<MdInline>) : MdInline
    data class MdInlineCode(val code: String) : MdInline
    data class MdLink(val url: String, val children: List<MdInline>) : MdInline
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
            is SoftLineBreak -> result.add(MdInline.MdSoftBreak)
            is HardLineBreak -> result.add(MdInline.MdHardBreak)
            else -> result.addAll(parseInlines(child)) // fallback: recurse into unknown inline
        }
        child = child.next
    }
    return result
}

private fun parseListItem(item: ListItem, listDepth: Int): MdListItem {
    val marker = item.firstChild as? TaskListItemMarker
    val blocks = parseBlocks(item, listDepth + 1)

    return MdListItem(blocks = blocks, taskChecked = marker?.isChecked)
}

private fun parseBlocks(node: Node, listDepth: Int = 0): List<MdBlock> {
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
            is BlockQuote -> result.add(MdBlock.MdBlockQuote(parseBlocks(child, listDepth)))
            is BulletList -> {
                val items = mutableListOf<MdListItem>()
                var item = child.firstChild
                while (item != null) {
                    if (item is ListItem) items.add(parseListItem(item, listDepth))
                    item = item.next
                }
                result.add(MdBlock.MdBulletList(items, listDepth))
            }

            is OrderedList -> {
                val items = mutableListOf<MdListItem>()
                var item = child.firstChild
                while (item != null) {
                    if (item is ListItem) items.add(parseListItem(item, listDepth))
                    item = item.next
                }
                result.add(MdBlock.MdOrderedList(items, child.markerStartNumber, listDepth))
            }
            // Ignore HTML blocks, tables etc. for now
            else -> Unit
        }
        child = child.next
    }
    return result
}

private fun parseMarkdown(markdown: String): List<MdBlock> {
    val document = markdownParser.parse(markdown) as Document
    return parseBlocks(document)
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
// AnnotatedString builder (runs on IO thread)
// ---------------------------------------------------------------------------

private fun buildInlineAnnotatedString(
    inlines: List<MdInline>,
    onLinkClick: (String) -> Unit,
    linkColor: androidx.compose.ui.graphics.Color,
    codeBackground: androidx.compose.ui.graphics.Color,
): AnnotatedString = buildAnnotatedString {
    fun appendInlines(list: List<MdInline>) {
        for (inline in list) {
            when (inline) {
                is MdInline.MdText -> append(inline.text)
                is MdInline.MdBold -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    appendInlines(inline.children)
                }

                is MdInline.MdItalic -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    appendInlines(inline.children)
                }

                is MdInline.MdStrikethrough -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                    appendInlines(inline.children)
                }

                is MdInline.MdInlineCode -> withStyle(
                    SpanStyle(fontFamily = FontFamily.Monospace, background = codeBackground),
                ) {
                    append(inline.code)
                }

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
    data class RenderedCode(val code: String, val language: String?) : RenderedBlock
    data class RenderedBulletList(val items: List<RenderedListItem>, val depth: Int) : RenderedBlock
    data class RenderedOrderedList(val items: List<RenderedListItem>, val startNumber: Int, val depth: Int) :
        RenderedBlock

    data class RenderedBlockQuote(val children: List<RenderedBlock>) : RenderedBlock
    object RenderedThematicBreak : RenderedBlock
}

private data class RenderedListItem(
    val blocks: List<RenderedBlock>,
    val taskChecked: Boolean? = null,
)

private fun renderBlocks(
    blocks: List<MdBlock>,
    onLinkClick: (String) -> Unit,
    linkColor: androidx.compose.ui.graphics.Color,
    codeBackground: androidx.compose.ui.graphics.Color,
): List<RenderedBlock> = blocks.map { block ->
    when (block) {
        is MdBlock.MdHeading -> RenderedBlock.RenderedHeading(
            block.level,
            buildInlineAnnotatedString(block.inline, onLinkClick, linkColor, codeBackground),
        )

        is MdBlock.MdParagraph -> RenderedBlock.RenderedParagraph(
            buildInlineAnnotatedString(block.inline, onLinkClick, linkColor, codeBackground),
        )

        is MdBlock.MdCode -> RenderedBlock.RenderedCode(block.code, block.language)
        is MdBlock.MdThematicBreak -> RenderedBlock.RenderedThematicBreak
        is MdBlock.MdBlockQuote -> RenderedBlock.RenderedBlockQuote(
            renderBlocks(block.children, onLinkClick, linkColor, codeBackground),
        )

        is MdBlock.MdBulletList -> RenderedBlock.RenderedBulletList(
            block.items.map { item ->
                RenderedListItem(
                    blocks = renderBlocks(item.blocks, onLinkClick, linkColor, codeBackground),
                    taskChecked = item.taskChecked,
                )
            },
            block.depth,
        )

        is MdBlock.MdOrderedList -> RenderedBlock.RenderedOrderedList(
            block.items.map { item ->
                RenderedListItem(
                    blocks = renderBlocks(item.blocks, onLinkClick, linkColor, codeBackground),
                    taskChecked = item.taskChecked,
                )
            },
            block.startNumber,
            block.depth,
        )
    }
}

// ---------------------------------------------------------------------------
// Public composable
// ---------------------------------------------------------------------------

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
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
    var selectedLink by rememberSaveable { mutableStateOf<String?>(null) }
    val onLinkClick = remember<(String) -> Unit> { { url -> selectedLink = url } }

    var renderedBlocks by remember(markdown) { mutableStateOf<List<RenderedBlock>?>(null) }

    LaunchedEffect(markdown, linkColor, codeBackground, onLinkClick) {
        renderedBlocks = null

        val totalStartNs = System.nanoTime()
        val parseStartNs = totalStartNs
        val parsedBlocks = withContext(Dispatchers.Default) { parseMarkdown(markdown) }
        val parseElapsedMs = (System.nanoTime() - parseStartNs) / 1_000_000L

        val renderStartNs = System.nanoTime()
        val rendered = withContext(Dispatchers.Default) {
            renderBlocks(parsedBlocks, onLinkClick, linkColor, codeBackground)
        }

        val renderElapsedMs = (System.nanoTime() - renderStartNs) / 1_000_000L
        val totalElapsedMs = (System.nanoTime() - totalStartNs) / 1_000_000L
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
        SelectionContainer {
            Column(modifier = modifier.verticalScroll(rememberScrollState())) {
                for (block in blocks) {
                    RenderedBlockComposable(block)
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
private fun RenderedBlockComposable(block: RenderedBlock) {
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
                modifier = Modifier.padding(bottom = 8.dp),
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

        is RenderedBlock.RenderedBlockQuote -> {
            val quoteBarColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .background(quoteBarColor)
                        .padding(horizontal = 2.dp)
                        .fillMaxWidth(0f), // zero-width spacer painted by the background
                )
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .background(quoteBarColor, RoundedCornerShape(2.dp))
                        .padding(start = 4.dp, end = 0.dp, top = 0.dp, bottom = 0.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
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
                    Row(modifier = Modifier.padding(bottom = 8.dp)) {
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
                                RenderedBlockComposable(child)
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
                    Row(modifier = Modifier.padding(bottom = 8.dp)) {
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
                                RenderedBlockComposable(child)
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
