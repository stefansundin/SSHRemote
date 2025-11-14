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

package com.stefansundin.sshremote.data.identity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

/**
 * An identity is a private/public key pair that can be used to authenticate to an SSH server.
 *
 * An identity is also known as an SSH key, which is the term used in the app UI.
 */
@Entity(tableName = "identities")
data class Identity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val name: String,
    val encryptedPrivateKey: ByteArray,
)
