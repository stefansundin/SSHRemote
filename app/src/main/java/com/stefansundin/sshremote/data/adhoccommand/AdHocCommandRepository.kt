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

package com.stefansundin.sshremote.data.adhoccommand

import kotlinx.coroutines.flow.Flow

class AdHocCommandRepository(private val adHocCommandDao: AdHocCommandDao) {
    fun getAdHocCommands(): Flow<List<AdHocCommand>> {
        return adHocCommandDao.getAdHocCommands()
    }

    suspend fun insert(adHocCommand: AdHocCommand) {
        adHocCommandDao.insert(adHocCommand)
    }

    suspend fun delete(adHocCommand: AdHocCommand) {
        adHocCommandDao.delete(adHocCommand)
    }

    suspend fun clear() {
        adHocCommandDao.clear()
    }
}
