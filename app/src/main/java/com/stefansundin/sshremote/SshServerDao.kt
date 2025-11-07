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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SshServerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(server: SshServer)

    @Delete
    suspend fun delete(server: SshServer)

    @Query("SELECT * FROM ssh_servers ORDER BY name ASC")
    fun getAllServers(): Flow<List<SshServer>>

    @Query("SELECT * FROM ssh_servers WHERE id = :id")
    fun getServerById(id: Int): Flow<SshServer?>
}
