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

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.RemoteControlKey

val gson: Gson = GsonBuilder()
    .registerTypeAdapterFactory(
        // Creates a TypeAdapter for Map<RemoteControlKey, Command> that is passed the gson instance
        object : com.google.gson.TypeAdapterFactory {
            override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
                if (type.type == object : TypeToken<Map<RemoteControlKey, Command>>() {}.type) {
                    @Suppress("UNCHECKED_CAST")
                    return LenientRemoteControlKeyMapTypeAdapter(gson) as TypeAdapter<T>
                }
                return null
            }
        },
    )
    .create()
