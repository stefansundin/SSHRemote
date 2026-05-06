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

import kotlinx.coroutines.flow.Flow

class KnownHostRepository(private val knownHostDao: KnownHostDao) {
    fun getAll(): Flow<List<KnownHost>> {
        return knownHostDao.getAll()
    }

    suspend fun insert(knownHost: KnownHost) {
        knownHostDao.insert(knownHost)
    }

    suspend fun delete(knownHost: KnownHost) {
        knownHostDao.delete(knownHost)
    }

    suspend fun deleteAll() {
        knownHostDao.deleteAll()
    }
}
