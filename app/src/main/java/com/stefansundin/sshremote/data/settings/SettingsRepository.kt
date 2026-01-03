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

package com.stefansundin.sshremote.data.settings

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.stefansundin.sshremote.HapticFeedback
import com.stefansundin.sshremote.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")
        val BACKGROUND_COLOR = intPreferencesKey("background_color")
        val PRIMARY_COLOR = intPreferencesKey("primary_color")
        val ON_PRIMARY_COLOR = intPreferencesKey("on_primary_color")
        val HAPTIC_FEEDBACK_DURATION = longPreferencesKey("haptic_feedback_duration")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val STRICT_HOST_KEY_CHECKING = booleanPreferencesKey("strict_host_key_checking")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
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

    val useDynamicColors: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_DYNAMIC_COLORS] ?: true
        }

    suspend fun setUseDynamicColors(useDynamicColors: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_DYNAMIC_COLORS] = useDynamicColors
        }
    }

    val backgroundColor: Flow<Color?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BACKGROUND_COLOR]?.let { Color(it) }
        }

    suspend fun setBackgroundColor(color: Color?) {
        dataStore.edit { preferences ->
            if (color == null) {
                preferences.remove(PreferencesKeys.BACKGROUND_COLOR)
            } else {
                preferences[PreferencesKeys.BACKGROUND_COLOR] = color.toArgb()
            }
        }
    }

    val primaryColor: Flow<Color?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PRIMARY_COLOR]?.let { Color(it) }
        }

    suspend fun setPrimaryColor(color: Color?) {
        dataStore.edit { preferences ->
            if (color == null) {
                preferences.remove(PreferencesKeys.PRIMARY_COLOR)
            } else {
                preferences[PreferencesKeys.PRIMARY_COLOR] = color.toArgb()
            }
        }
    }

    val onPrimaryColor: Flow<Color?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ON_PRIMARY_COLOR]?.let { Color(it) }
        }

    suspend fun setOnPrimaryColor(color: Color?) {
        dataStore.edit { preferences ->
            if (color == null) {
                preferences.remove(PreferencesKeys.ON_PRIMARY_COLOR)
            } else {
                preferences[PreferencesKeys.ON_PRIMARY_COLOR] = color.toArgb()
            }
        }
    }

    val hapticFeedback: Flow<HapticFeedback> = dataStore.data
        .map { preferences ->
            val duration = preferences[PreferencesKeys.HAPTIC_FEEDBACK_DURATION]
                ?: HapticFeedback.Medium.duration
            HapticFeedback.fromDuration(duration)
        }

    suspend fun setHapticFeedback(hapticFeedback: HapticFeedback) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK_DURATION] = hapticFeedback.duration
        }
    }

    val keepScreenOn: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.KEEP_SCREEN_ON] ?: true
        }

    suspend fun setKeepScreenOn(keepScreenOn: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEEP_SCREEN_ON] = keepScreenOn
        }
    }

    val notificationsEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: false
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
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
