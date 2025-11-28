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

package com.stefansundin.sshremote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class HapticFeedback(val duration: kotlin.Long) : Parcelable {
    data object Off : HapticFeedback(0)
    data object Short : HapticFeedback(20)
    data object Medium : HapticFeedback(40)
    data object Long : HapticFeedback(80)
    data class Custom(val customDuration: kotlin.Long) : HapticFeedback(customDuration)

    val label: String
        get() = when (this) {
            is Off -> "Off"
            is Short -> "Short ($duration ms)"
            is Medium -> "Medium ($duration ms)"
            is Long -> "Long ($duration ms)"
            is Custom -> "Custom ($duration ms)"
        }

    companion object {
        val presets by lazy { listOf(Off, Short, Medium, Long) }

        fun fromDuration(duration: kotlin.Long): HapticFeedback {
            return when (duration) {
                Off.duration -> Off
                Short.duration -> Short
                Medium.duration -> Medium
                Long.duration -> Long
                else -> Custom(duration)
            }
        }
    }
}
