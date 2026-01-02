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

package com.stefansundin.sshremote

import android.util.Log
import com.jcraft.jsch.Logger

/**
 * A custom JSch logger that redirects output to Android's Logcat.
 */
class JschLogger : Logger {
    companion object {
        private const val TAG = "JSch"
    }

    override fun log(level: Int, message: String) {
        when (level) {
            Logger.DEBUG -> Log.d(TAG, message)
            Logger.INFO -> Log.i(TAG, message)
            Logger.WARN -> Log.w(TAG, message)
            Logger.ERROR -> Log.e(TAG, message)
            Logger.FATAL -> Log.e(TAG, message) // No FATAL in Android Log, use ERROR
            else -> Log.v(TAG, message)
        }
    }

    // JSch asks the logger if a certain level is enabled.
    // We'll enable all of them to get maximum debug output.
    override fun isEnabled(level: Int): Boolean {
        return true
    }
}
