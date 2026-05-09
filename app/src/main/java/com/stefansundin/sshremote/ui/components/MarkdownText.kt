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
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.text.style.LineBackgroundSpan
import android.text.style.LineHeightSpan
import android.text.style.MetricAffectingSpan
import android.view.SoundEffectConstants
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.stefansundin.sshremote.R
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.RenderProps
import io.noties.markwon.SpanFactory
import io.noties.markwon.core.CoreProps
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.syntax.SyntaxHighlight
import kotlinx.coroutines.launch
import org.commonmark.node.Code
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Node

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    selectableText: Boolean = true,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val clipboard = LocalClipboard.current
    val resources = LocalResources.current
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val scope = rememberCoroutineScope()
    var selectedLink by rememberSaveable { mutableStateOf<String?>(null) }
    val maxDialogHeight = with(density) { windowInfo.containerSize.height.toDp() * 0.9f }
    val codeBlockMarginPx = remember(density) { with(density) { 6.dp.roundToPx() } }
    val codeBlockVerticalPaddingPx = remember(density) { with(density) { 6.dp.roundToPx() } }
    val codeBlockCornerRadiusPx = remember(density) { with(density) { 6.dp.toPx() } }
    val commentColorArgb = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.95f).toArgb()

    val markwon = remember(
        context,
        codeBlockMarginPx,
        codeBlockVerticalPaddingPx,
        codeBlockCornerRadiusPx,
        commentColorArgb,
    ) {
        Markwon.builder(context)
            .usePlugin(
                object : AbstractMarkwonPlugin() {
                    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                        builder.linkResolver { _, link -> selectedLink = link }
                        builder.syntaxHighlight(HashCommentSyntaxHighlight(commentColorArgb))
                    }

                    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                        builder.on(Code::class.java) { visitor, code ->
                            val length = visitor.length()
                            // CorePlugin wraps inline code with non-breaking spaces for extra padding
                            // Render the literal directly to remove that injected side spacing
                            visitor.builder().append(code.literal)
                            visitor.setSpansForNodeOptional(code, length)
                        }

                        builder.on(FencedCodeBlock::class.java) { visitor, codeBlock ->
                            visitCodeBlockWithoutExtraBlankLines(
                                visitor = visitor,
                                info = codeBlock.info,
                                code = codeBlock.literal,
                                node = codeBlock,
                            )
                        }

                        builder.on(IndentedCodeBlock::class.java) { visitor, codeBlock ->
                            visitCodeBlockWithoutExtraBlankLines(
                                visitor = visitor,
                                info = null,
                                code = codeBlock.literal,
                                node = codeBlock,
                            )
                        }
                    }

                    override fun configureTheme(builder: MarkwonTheme.Builder) {
                        // Remove heading underlines and reduce code block side padding
                        builder
                            .headingBreakHeight(0)
                            .codeBlockMargin(codeBlockMarginPx)
                    }

                    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                        val roundedFactory = RoundedCodeBlockSpanFactory(
                            cornerRadiusPx = codeBlockCornerRadiusPx,
                            verticalPaddingPx = codeBlockVerticalPaddingPx,
                        )
                        builder
                            .setFactory(FencedCodeBlock::class.java, roundedFactory)
                            .setFactory(IndentedCodeBlock::class.java, roundedFactory)
                    }
                },
            )
            .usePlugin(TablePlugin.create(context))
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(StrikethroughPlugin.create())
            .build()
    }

    val textColor = LocalContentColor.current
    val textSizeSp = LocalTextStyle.current.fontSize
    val linkColor = MaterialTheme.colorScheme.primary

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            TextView(viewContext).apply {
                setTextIsSelectable(selectableText)
                linksClickable = true
                isClickable = true
                movementMethod = LinkMovementMethod.getInstance()
            }
        },
        update = { textView ->
            textView.setTextColor(textColor.toArgb())
            textView.setLinkTextColor(linkColor.toArgb())
            textView.movementMethod = LinkMovementMethod.getInstance()
            if (textSizeSp.isSp) {
                textView.textSize = textSizeSp.value
            }
            markwon.setMarkdown(textView, markdown)
        },
    )

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
                if (openLinkInBrowser(context, url)) {
                    selectedLink = null
                }
            },
        )
    }
}

private fun visitCodeBlockWithoutExtraBlankLines(
    visitor: MarkwonVisitor,
    info: String?,
    code: String,
    node: Node,
) {
    visitor.blockStart(node)

    val length = visitor.length()
    visitor.builder().append(visitor.configuration().syntaxHighlight().highlight(info, code))
    visitor.ensureNewLine()

    CoreProps.CODE_BLOCK_INFO.set(visitor.renderProps(), info)
    visitor.setSpansForNodeOptional(node, length)

    visitor.blockEnd(node)
}

private class HashCommentSyntaxHighlight(
    private val commentColor: Int,
) : SyntaxHighlight {
    override fun highlight(info: String?, code: String): CharSequence {
        if (code.indexOf('#') == -1) return code

        val out = SpannableString(code)
        var lineStart = 0
        while (lineStart < code.length) {
            val lineEnd = code.indexOf('\n', lineStart).let { if (it == -1) code.length else it }
            val hashIndex = code.indexOf('#', lineStart)
            if (hashIndex in lineStart until lineEnd) {
                out.setSpan(
                    ForegroundColorSpan(commentColor),
                    hashIndex,
                    lineEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
            lineStart = lineEnd + 1
        }
        return out
    }
}

private class RoundedCodeBlockSpanFactory(
    private val cornerRadiusPx: Float,
    private val verticalPaddingPx: Int,
) : SpanFactory {
    override fun getSpans(
        configuration: MarkwonConfiguration,
        props: RenderProps,
    ): Any = RoundedCodeBlockSpan(
        theme = configuration.theme(),
        cornerRadiusPx = cornerRadiusPx,
        verticalPaddingPx = verticalPaddingPx,
    )
}

private class RoundedCodeBlockSpan(
    private val theme: MarkwonTheme,
    private val cornerRadiusPx: Float,
    private val verticalPaddingPx: Int,
) : MetricAffectingSpan(), LeadingMarginSpan, LineBackgroundSpan, LineHeightSpan.WithDensity {

    private val rect = RectF()
    private val path = Path()
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun updateMeasureState(p: TextPaint) {
        theme.applyCodeBlockTextStyle(p)
    }

    override fun updateDrawState(ds: TextPaint) {
        theme.applyCodeBlockTextStyle(ds)
    }

    override fun getLeadingMargin(first: Boolean): Int = theme.getCodeBlockMargin()

    override fun drawLeadingMargin(
        c: Canvas,
        p: Paint,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout,
    ) {
        // Background is drawn in drawBackground, so no-op here.
    }

    override fun drawBackground(
        c: Canvas,
        p: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lnum: Int,
    ) {
        val spanned = text as? Spanned ?: return
        val spanStart = spanned.getSpanStart(this)
        val spanEnd = spanned.getSpanEnd(this)
        val isFirstLine = start <= spanStart
        val isLastLine = end >= spanEnd

        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.color = theme.getCodeBlockBackgroundColor(p)

        val l = minOf(left, right).toFloat()
        val r = maxOf(left, right).toFloat()
        val t = top.toFloat()
        val b = bottom.toFloat()

        rect.set(l, t, r, b)

        if (isFirstLine && isLastLine) {
            c.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, backgroundPaint)
            return
        }

        if (isFirstLine || isLastLine) {
            path.reset()
            val radii = if (isFirstLine) {
                floatArrayOf(cornerRadiusPx, cornerRadiusPx, cornerRadiusPx, cornerRadiusPx, 0f, 0f, 0f, 0f)
            } else {
                floatArrayOf(0f, 0f, 0f, 0f, cornerRadiusPx, cornerRadiusPx, cornerRadiusPx, cornerRadiusPx)
            }
            path.addRoundRect(rect, radii, Path.Direction.CW)
            c.drawPath(path, backgroundPaint)
            return
        }

        c.drawRect(rect, backgroundPaint)
    }

    override fun chooseHeight(
        text: CharSequence,
        start: Int,
        end: Int,
        spanstartv: Int,
        v: Int,
        fm: Paint.FontMetricsInt,
    ) {
        applyVerticalPadding(text, start, end, fm)
    }

    override fun chooseHeight(
        text: CharSequence,
        start: Int,
        end: Int,
        spanstartv: Int,
        v: Int,
        fm: Paint.FontMetricsInt,
        paint: TextPaint?,
    ) {
        applyVerticalPadding(text, start, end, fm)
    }

    private fun applyVerticalPadding(
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt,
    ) {
        if (verticalPaddingPx <= 0) return
        val spanned = text as? Spanned ?: return
        val spanStart = spanned.getSpanStart(this)
        val spanEnd = spanned.getSpanEnd(this)
        if (start <= spanStart) {
            fm.top -= verticalPaddingPx
            fm.ascent -= verticalPaddingPx
        }
        if (end >= spanEnd) {
            fm.bottom += verticalPaddingPx
            fm.descent += verticalPaddingPx
        }
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
                Button(onClick = onOpenInBrowser) {
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
