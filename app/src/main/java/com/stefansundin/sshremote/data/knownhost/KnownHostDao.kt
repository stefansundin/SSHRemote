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

package com.stefansundin.sshremote.data.knownhost

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface KnownHostDao {
    @Query("SELECT * FROM known_hosts ORDER BY createdAt DESC")
    fun getAll(): Flow<List<KnownHost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(knownHost: KnownHost)

    @Delete
    suspend fun delete(knownHost: KnownHost)

    @Query("DELETE FROM known_hosts")
    suspend fun deleteAll()
}
