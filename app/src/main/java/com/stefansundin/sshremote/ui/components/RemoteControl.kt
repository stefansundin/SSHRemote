/*
 * SSH Remote
 * Copyright (C) 2025  Stefan Sundin
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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.ui.screens.MouseEvent
import com.stefansundin.sshremote.ui.screens.MousePadScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RemoteControl(
    onKeyClicked: (RemoteControlKey) -> Unit,
    onMouseEvent: (MouseEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState { 2 }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text("Remote") },
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text("Mouse") },
            )
        }

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val isLandscape = maxWidth > maxHeight
                        RemoteControlLayout(isLandscape, onKeyClicked)
                    }
                }

                1 -> {
                    MousePadScreen(onMouseEvent)
                }
            }
        }
    }
}

@Composable
private fun RemoteControlLayout(isLandscape: Boolean, onKeyClicked: (RemoteControlKey) -> Unit) {
    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Dpad(onKeyClicked)
            ActionButtons(onKeyClicked, modifier = Modifier.fillMaxHeight())
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        ) {
            Dpad(onKeyClicked)
            ActionButtons(onKeyClicked)
        }
    }
}

@Composable
private fun ActionButtons(onKeyClicked: (RemoteControlKey) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val buttonModifier = Modifier
            .weight(1f)
            .height(56.dp)
        val rowModifier = Modifier.fillMaxWidth()

        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = { onKeyClicked(RemoteControlKey.VOLUME_DOWN) },
                modifier = buttonModifier,
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = "Volume Down")
            }
            Button(
                onClick = { onKeyClicked(RemoteControlKey.MUTE) },
                modifier = buttonModifier,
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeOff, contentDescription = "Mute")
            }
            Button(
                onClick = { onKeyClicked(RemoteControlKey.VOLUME_UP) },
                modifier = buttonModifier,
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Volume Up")
            }
        }
        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = { onKeyClicked(RemoteControlKey.BACK) },
                modifier = buttonModifier,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Button(
                onClick = { onKeyClicked(RemoteControlKey.HOME) },
                modifier = buttonModifier,
            ) {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }
            Button(
                onClick = { onKeyClicked(RemoteControlKey.MENU) },
                modifier = buttonModifier,
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        }
        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = { onKeyClicked(RemoteControlKey.PREVIOUS) },
                modifier = buttonModifier,
            ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
            }
            Button(
                onClick = { onKeyClicked(RemoteControlKey.PLAY_PAUSE) },
                modifier = buttonModifier,
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play/Pause")
                Icon(Icons.Default.Pause, contentDescription = "Play/Pause")
            }
            Button(
                onClick = { onKeyClicked(RemoteControlKey.NEXT) },
                modifier = buttonModifier,
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next")
            }
        }
    }
}

@Composable
private fun Dpad(onKeyClicked: (RemoteControlKey) -> Unit) {
    val dpadSize = 280.dp
    val iconOffset = dpadSize / 3f
    val iconSize = dpadSize / 3f

    Box(
        modifier = Modifier.size(dpadSize),
        contentAlignment = Alignment.Center,
    ) {
        val directionalButtonModifier = Modifier.fillMaxSize()

        // UP
        Button(
            onClick = { onKeyClicked(RemoteControlKey.UP) },
            shape = ArcShape(225f, 90f),
            modifier = directionalButtonModifier,
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Up",
                modifier = Modifier
                    .size(iconSize)
                    .offset(y = -iconOffset),
            )
        }

        // RIGHT
        Button(
            onClick = { onKeyClicked(RemoteControlKey.RIGHT) },
            shape = ArcShape(315f, 90f),
            modifier = directionalButtonModifier,
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Right",
                modifier = Modifier
                    .size(iconSize)
                    .offset(x = iconOffset),
            )
        }

        // DOWN
        Button(
            onClick = { onKeyClicked(RemoteControlKey.DOWN) },
            shape = ArcShape(45f, 90f),
            modifier = directionalButtonModifier,
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Down",
                modifier = Modifier
                    .size(iconSize)
                    .offset(y = iconOffset),
            )
        }

        // LEFT
        Button(
            onClick = { onKeyClicked(RemoteControlKey.LEFT) },
            shape = ArcShape(135f, 90f),
            modifier = directionalButtonModifier,
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Left",
                modifier = Modifier
                    .size(iconSize)
                    .offset(x = -iconOffset),
            )
        }

        // CENTER
        Button(
            onClick = { onKeyClicked(RemoteControlKey.SELECT) },
            modifier = Modifier.size(dpadSize / 2.8f),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(
                Icons.Default.RadioButtonUnchecked,
                contentDescription = "Select",
                modifier = Modifier.fillMaxSize(0.8f),
            )
        }
    }
}

private class ArcShape(private val startAngle: Float, private val sweepAngle: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path().apply {
            moveTo(size.width / 2f, size.height / 2f)
            arcTo(
                rect = Rect(0f, 0f, size.width, size.height),
                startAngleDegrees = startAngle,
                sweepAngleDegrees = sweepAngle,
                forceMoveTo = false,
            )
            close()
        }
        return Outline.Generic(path)
    }
}
