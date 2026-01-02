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

package com.stefansundin.sshremote.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.stefansundin.sshremote.data.identity.Identity
import com.stefansundin.sshremote.data.identity.IdentityDao
import com.stefansundin.sshremote.data.password.Password
import com.stefansundin.sshremote.data.password.PasswordDao

@Database(
    entities = [Identity::class, Password::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class EncryptedAppDatabase : RoomDatabase() {

    abstract fun identityDao(): IdentityDao
    abstract fun passwordDao(): PasswordDao

    companion object {
        @Volatile
        private var INSTANCE: EncryptedAppDatabase? = null

        fun getInstance(context: Context): EncryptedAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EncryptedAppDatabase::class.java,
                    "encrypted_database",
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
