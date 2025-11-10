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

package com.stefansundin.sshremote.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.stefansundin.sshremote.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val STRICT_HOST_KEY_CHECKING = booleanPreferencesKey("strict_host_key_checking")
    }

    val theme: Flow<Theme> = dataStore.data
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.THEME] ?: Theme.SYSTEM.name
            try {
                Theme.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                Theme.SYSTEM
            }
        }

    suspend fun setTheme(theme: Theme) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    val strictHostKeyChecking: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.STRICT_HOST_KEY_CHECKING] ?: true
        }

    suspend fun setStrictHostKeyChecking(strictHostKeyChecking: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.STRICT_HOST_KEY_CHECKING] = strictHostKeyChecking
        }
    }

}
