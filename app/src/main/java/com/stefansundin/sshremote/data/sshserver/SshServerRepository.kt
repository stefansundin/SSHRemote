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

package com.stefansundin.sshremote.data.sshserver

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, and retrieve of [SshServer] from a given
 * data source.
 */
class SshServerRepository(private val sshServerDao: SshServerDao) {

    /**
     * Retrieves a flow of all SSH servers from the database, ordered by name.
     */
    fun getAllServers(): Flow<List<SshServer>> = sshServerDao.getAllServers()

    /**
     * Retrieves a flow of a single SSH server by its ID.
     */
    fun getServerById(id: Int): Flow<SshServer?> = sshServerDao.getServerById(id)

    /**
     * Retrieves a single SSH server by its ID, once.
     */
    suspend fun getServerByIdOnce(id: Int): SshServer? = sshServerDao.getServerByIdOnce(id)

    /**
     * Inserts or updates an SSH server in the database.
     */
    suspend fun upsert(server: SshServer) {
        sshServerDao.upsert(server)
    }

    /**
     * Deletes an SSH server from the database.
     */
    suspend fun delete(server: SshServer) {
        sshServerDao.delete(server)
    }
}
