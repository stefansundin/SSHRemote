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
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val SHOW_WHEN_LOCKED = booleanPreferencesKey("show_when_locked")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val STRICT_HOST_KEY_CHECKING = booleanPreferencesKey("strict_host_key_checking")
        val ALLOW_PASSWORD_PROMPTING = booleanPreferencesKey("allow_password_prompting")
        val SHARE_TARGET_ENABLED = booleanPreferencesKey("share_target_enabled")
    }

    private fun Preferences.readTheme(): Theme {
        val themeName = this[PreferencesKeys.THEME] ?: Theme.SYSTEM.name
        return try {
            Theme.valueOf(themeName)
        } catch (_: IllegalArgumentException) {
            Theme.SYSTEM
        }
    }

    private fun Preferences.toAppearanceSettings(): AppearanceSettings {
        return AppearanceSettings(
            theme = readTheme(),
            useDynamicColors = this[PreferencesKeys.USE_DYNAMIC_COLORS] ?: true,
            backgroundColor = this[PreferencesKeys.BACKGROUND_COLOR]?.let { Color(it) },
            primaryColor = this[PreferencesKeys.PRIMARY_COLOR]?.let { Color(it) },
            onPrimaryColor = this[PreferencesKeys.ON_PRIMARY_COLOR]?.let { Color(it) },
        )
    }

    val appearance: Flow<AppearanceSettings> = dataStore.data
        .map { preferences ->
            preferences.toAppearanceSettings()
        }

    val theme: Flow<Theme> = appearance
        .map { it.theme }

    suspend fun setTheme(theme: Theme) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    val useDynamicColors: Flow<Boolean> = appearance
        .map { it.useDynamicColors }

    suspend fun setUseDynamicColors(useDynamicColors: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_DYNAMIC_COLORS] = useDynamicColors
        }
    }

    val backgroundColor: Flow<Color?> = appearance
        .map { it.backgroundColor }

    suspend fun setBackgroundColor(color: Color?) {
        dataStore.edit { preferences ->
            if (color == null) {
                preferences.remove(PreferencesKeys.BACKGROUND_COLOR)
            } else {
                preferences[PreferencesKeys.BACKGROUND_COLOR] = color.toArgb()
            }
        }
    }

    val primaryColor: Flow<Color?> = appearance
        .map { it.primaryColor }

    suspend fun setPrimaryColor(color: Color?) {
        dataStore.edit { preferences ->
            if (color == null) {
                preferences.remove(PreferencesKeys.PRIMARY_COLOR)
            } else {
                preferences[PreferencesKeys.PRIMARY_COLOR] = color.toArgb()
            }
        }
    }

    val onPrimaryColor: Flow<Color?> = appearance
        .map { it.onPrimaryColor }

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

    val showWhenLocked: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SHOW_WHEN_LOCKED] ?: false
        }

    suspend fun setShowWhenLocked(showWhenLocked: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_WHEN_LOCKED] = showWhenLocked
        }
    }

    val notificationsEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: false
        }

    suspend fun setNotificationsEnabled(notificationsEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = notificationsEnabled
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

    val allowPasswordPrompting: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ALLOW_PASSWORD_PROMPTING] ?: true
        }

    suspend fun setAllowPasswordPrompting(allowPasswordPrompting: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALLOW_PASSWORD_PROMPTING] = allowPasswordPrompting
        }
    }

    val shareTargetEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SHARE_TARGET_ENABLED] ?: false
        }

    suspend fun setShareTargetEnabled(shareTargetEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHARE_TARGET_ENABLED] = shareTargetEnabled
        }
    }
}
