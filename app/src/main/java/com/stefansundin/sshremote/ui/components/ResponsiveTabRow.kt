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

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme

@Composable
fun ResponsiveTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    edgePadding: Dp = 0.dp,
    tabs: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val tabMeasurables = subcompose("tabs") {
            Row { tabs() }
        }[0]
        val totalTabsWidth = tabMeasurables.measure(Constraints()).width
        val availableWidth = constraints.maxWidth
        val useScrollable = totalTabsWidth > availableWidth

        val tabRowMeasurable = subcompose(if (useScrollable) "scrollable" else "fixed") {
            if (useScrollable) {
                SecondaryScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = edgePadding,
                ) {
                    tabs()
                }
            } else {
                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                ) {
                    tabs()
                }
            }
        }[0]

        val tabRowPlaceable = tabRowMeasurable.measure(constraints)
        layout(tabRowPlaceable.width, tabRowPlaceable.height) {
            tabRowPlaceable.placeRelative(0, 0)
        }
    }
}

@Preview(showBackground = true, widthDp = 500)
@Preview(showBackground = true, widthDp = 250, name = "Scrolling")
@Preview(
    showBackground = true,
    widthDp = 250,
    name = "Dark and large font",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun ResponsiveTabRowPreview() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Remote", "Mouse", "Keyboard", "Commands")

    SSHRemoteTheme {
        ResponsiveTabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = title) },
                )
            }
        }
    }
}
