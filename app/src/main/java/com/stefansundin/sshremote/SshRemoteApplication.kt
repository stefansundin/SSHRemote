/*
SSH Remote
Copyright (C) 2025  Stefan Sundin

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.stefansundin.sshremote

import android.app.Application
import com.stefansundin.sshremote.data.AppDatabase
import com.stefansundin.sshremote.data.sshkey.SshKeyRepository
import com.stefansundin.sshremote.data.settings.SettingsRepository
import com.stefansundin.sshremote.data.sshserver.SshServerRepository

class SshRemoteApplication : Application() {
    private val database by lazy { AppDatabase.getInstance(this) }
    val sshServerRepository by lazy { SshServerRepository(database.sshServerDao()) }
    val sshKeyRepository by lazy { SshKeyRepository(database.sshKeyDao()) }
    val settingsRepository by lazy { SettingsRepository(this) }
}
