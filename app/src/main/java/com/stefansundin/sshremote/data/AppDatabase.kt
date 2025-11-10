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

package com.stefansundin.sshremote.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.stefansundin.sshremote.data.sshkey.SshKey
import com.stefansundin.sshremote.data.sshkey.SshKeyDao
import com.stefansundin.sshremote.data.sshserver.SshServer
import com.stefansundin.sshremote.data.sshserver.SshServerDao

@Database(entities = [SshServer::class, SshKey::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class, SshKeyIdsConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sshServerDao(): SshServerDao
    abstract fun sshKeyDao(): SshKeyDao

    companion object {
        // The @Volatile annotation ensures that writes to this field are immediately
        // made visible to other threads.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ssh_remote_database",
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
