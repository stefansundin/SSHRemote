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

package com.stefansundin.sshremote.data

import android.util.Log
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.stefansundin.sshremote.data.host.RemoteControlKey

class LenientRemoteControlKeyMapTypeAdapter : TypeAdapter<Map<RemoteControlKey, String>>() {
    override fun write(writer: JsonWriter, value: Map<RemoteControlKey, String>?) {
        if (value == null) {
            writer.nullValue()
            return
        }
        writer.beginObject()
        for ((key, v) in value) {
            writer.name(key.name)
            writer.value(v)
        }
        writer.endObject()
    }

    override fun read(reader: JsonReader): Map<RemoteControlKey, String>? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        val map = mutableMapOf<RemoteControlKey, String>()
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            try {
                val key = RemoteControlKey.valueOf(name)
                val value = reader.nextString()
                map[key] = value
            } catch (_: IllegalArgumentException) {
                // Ignore unknown keys
                Log.d("LenientRemoteControlKeyMapTypeAdapter", "Ignoring unknown $name")
                reader.skipValue()
            }
        }
        reader.endObject()
        return map
    }
}
