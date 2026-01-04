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

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
