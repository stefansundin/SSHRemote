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

package com.stefansundin.sshremote.data.identity

import kotlinx.coroutines.flow.Flow

class IdentityRepository(private val identityDao: IdentityDao) {

    fun getAll(): Flow<List<Identity>> {
        return identityDao.getAll()
    }

    fun get(id: String): Flow<Identity?> = identityDao.get(id)

    suspend fun insert(identity: Identity) {
        identityDao.insert(identity)
    }

    suspend fun update(identity: Identity) {
        identityDao.update(identity)
    }

    suspend fun delete(identity: Identity) {
        identityDao.delete(identity)
    }
}
