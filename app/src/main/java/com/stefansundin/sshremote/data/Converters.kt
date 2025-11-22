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

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.RemoteControlKey
import java.time.OffsetDateTime

class Converters {
    @TypeConverter
    fun fromOffsetDateTime(value: String?): OffsetDateTime? {
        return value?.let { OffsetDateTime.parse(it) }
    }

    @TypeConverter
    fun offsetDateTimeToString(offsetDateTime: OffsetDateTime?): String? {
        return offsetDateTime?.toString()
    }

    @TypeConverter
    fun fromCommandList(value: List<Command>?): String? {
        if (value == null) {
            return null
        }
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCommandList(value: String?): List<Command>? {
        if (value == null) {
            return null
        }
        val type = object : TypeToken<List<Command>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        if (value == null) {
            return null
        }
        return gson.toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        if (value == null) {
            return null
        }
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        if (value == null) {
            return null
        }
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) {
            return null
        }
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromRemoteControlKeyMap(value: Map<RemoteControlKey, Command>?): String? {
        if (value == null) {
            return null
        }
        return gson.toJson(value)
    }

    @TypeConverter
    fun toRemoteControlKeyMap(value: String?): Map<RemoteControlKey, Command>? {
        if (value == null) {
            return null
        }
        val type = object : TypeToken<Map<RemoteControlKey, Command>>() {}.type
        return gson.fromJson(value, type)
    }
}
