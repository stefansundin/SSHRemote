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

package com.stefansundin.sshremote.data.host

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HostDao {

    @Insert
    suspend fun insert(host: Host): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(host: Host): Long

    @Delete
    suspend fun delete(host: Host)

    @Query("DELETE FROM hosts")
    suspend fun deleteAll()

    @Query("SELECT * FROM hosts ORDER BY name ASC")
    fun getAll(): Flow<List<Host>>

    @Query("SELECT * FROM hosts WHERE id = :id")
    fun get(id: Int): Flow<Host?>

    @Query("SELECT * FROM hosts WHERE id = :id")
    suspend fun getOnce(id: Int): Host?

    @Query("SELECT COUNT(*) FROM hosts")
    suspend fun count(): Int
}
