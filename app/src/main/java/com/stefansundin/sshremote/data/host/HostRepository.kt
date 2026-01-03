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

package com.stefansundin.sshremote.data.host

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, and retrieve of [Host] from a given
 * data source.
 */
class HostRepository(private val hostDao: HostDao) {

    /**
     * Retrieves a flow of all hosts from the database, ordered by name.
     */
    fun getAll(): Flow<List<Host>> = hostDao.getAll()

    /**
     * Retrieves a flow of a single host by its ID.
     */
    fun get(id: String): Flow<Host?> = hostDao.get(id)

    /**
     * Retrieves a single host by its ID, once.
     */
    suspend fun getOnce(id: String): Host? = hostDao.getOnce(id)

    /**
     * Inserts a host in the database.
     */
    suspend fun insert(host: Host) {
        hostDao.insert(host)
    }

    /**
     * Inserts or updates a host in the database.
     */
    suspend fun upsert(host: Host) {
        hostDao.upsert(host)
    }

    /**
     * Deletes a host from the database.
     */
    suspend fun delete(host: Host) {
        hostDao.delete(host)
    }

    /**
     * Deletes all hosts from the database.
     */
    suspend fun deleteAll() {
        hostDao.deleteAll()
    }

    /**
     * Returns the number of hosts in the database.
     */
    suspend fun count(): Int = hostDao.count()
}
