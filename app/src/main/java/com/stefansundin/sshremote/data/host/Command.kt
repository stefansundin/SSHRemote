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

package com.stefansundin.sshremote.data.host

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Keep
@Parcelize
data class Command(
    val command: String,
    val longPressCommand: String? = null,
    val name: String? = null,
    val showOutput: Boolean = false,
    val renderOutputAsMarkdown: Boolean = false,
    val repeat: Boolean = false,
    val runInBackground: Boolean = false,
    val id: String = UUID.randomUUID().toString(),
) : Parcelable {
    fun formatCommand(text: String): String {
        // Escape single quotes in the text to avoid breaking the command.
        // This may not be foolproof for all shell injection cases, so you should still be careful about what you're feeding this app.
        val escapedText = text.replace("'", "'\\''")
        return command.format(escapedText)
    }
}
