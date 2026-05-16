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

import android.app.Application
import com.stefansundin.sshremote.data.AppDatabase
import com.stefansundin.sshremote.data.EncryptedAppDatabase
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandRepository
import com.stefansundin.sshremote.data.host.HostRepository
import com.stefansundin.sshremote.data.identity.IdentityRepository
import com.stefansundin.sshremote.data.knownhost.KnownHostRepository
import com.stefansundin.sshremote.data.settings.SettingsRepository
import com.stefansundin.sshremote.notification.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.i2p.crypto.eddsa.EdDSASecurityProvider
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class SshRemoteApplication : Application() {
    private val database by lazy { AppDatabase.getInstance(this) }
    private val encryptedDatabase by lazy { EncryptedAppDatabase.getInstance(this) }

    val hostRepository by lazy { HostRepository(database.hostDao()) }
    val identityRepository by lazy { IdentityRepository(encryptedDatabase.identityDao()) }
    val knownHostRepository by lazy { KnownHostRepository(database.knownHostDao()) }
    val passwordDao by lazy { encryptedDatabase.passwordDao() }
    val adHocCommandRepository by lazy { AdHocCommandRepository(database.adHocCommandDao()) }
    val settingsRepository by lazy { SettingsRepository(this) }
    val sshRepository by lazy { SshRepository(settingsRepository) }
    val activeConnectionTracker by lazy { ActiveConnectionTracker() }
    val applicationScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Main) }

    var isRestoredFromBackup: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()

        // The encrypted database is not included in backups, so inform the user if it is missing
        isRestoredFromBackup = run {
            val databaseExists = getDatabasePath("database").exists()
            val encryptedDatabaseExists = getDatabasePath("encrypted_database").exists()
            databaseExists && !encryptedDatabaseExists
        }
        // Initialize both databases to ensure we don't accidentally trigger isRestoredFromBackup
        CoroutineScope(Dispatchers.IO).launch {
            encryptedDatabase.openHelper.writableDatabase
            database.openHelper.writableDatabase
        }

        // Replace system providers with bundled versions to ensure full algorithm support
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.addProvider(BouncyCastleProvider())
        Security.removeProvider(EdDSASecurityProvider.PROVIDER_NAME)
        Security.addProvider(EdDSASecurityProvider())

        NotificationService.createNotificationChannel(this)
    }
}
