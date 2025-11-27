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

import android.app.Application
import com.stefansundin.sshremote.data.AppDatabase
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandRepository
import com.stefansundin.sshremote.data.host.HostRepository
import com.stefansundin.sshremote.data.identity.IdentityRepository
import com.stefansundin.sshremote.data.settings.SettingsRepository
import com.stefansundin.sshremote.notification.NotificationService
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class SshRemoteApplication : Application() {
    private val database by lazy { AppDatabase.getInstance(this) }
    val hostRepository by lazy { HostRepository(database.hostDao()) }
    val identityRepository by lazy { IdentityRepository(database.identityDao()) }
    val adHocCommandRepository by lazy { AdHocCommandRepository(database.adHocCommandDao()) }
    val settingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        Security.addProvider(BouncyCastleProvider())
        NotificationService.createNotificationChannel(this)
    }
}
