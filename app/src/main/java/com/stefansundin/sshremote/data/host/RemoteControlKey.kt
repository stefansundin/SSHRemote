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

package com.stefansundin.sshremote.data.host

import androidx.annotation.StringRes
import com.stefansundin.sshremote.R

enum class RemoteControlKey(@StringRes val titleRes: Int) {
    // Dpad
    UP(R.string.key_up),
    RIGHT(R.string.key_right),
    DOWN(R.string.key_down),
    LEFT(R.string.key_left),
    SELECT(R.string.key_select),

    // Action buttons
    VOLUME_DOWN(R.string.key_volume_down),
    MUTE(R.string.key_mute),
    VOLUME_UP(R.string.key_volume_up),
    BACK(R.string.key_back),
    HOME(R.string.key_home),
    MENU(R.string.key_menu),
    PREVIOUS(R.string.key_previous),
    PLAY_PAUSE(R.string.key_play_pause),
    NEXT(R.string.key_next),

    // Mouse
    MOUSE_MOVE(R.string.key_mouse_move),
    MOUSE_LEFT_CLICK(R.string.left_click),
    MOUSE_RIGHT_CLICK(R.string.right_click),
    MOUSE_LEFT_DOWN(R.string.key_mouse_left_down),
    MOUSE_LEFT_UP(R.string.key_mouse_left_up),
    MOUSE_RIGHT_DOWN(R.string.key_mouse_right_down),
    MOUSE_RIGHT_UP(R.string.key_mouse_right_up),
    MOUSE_PAN_UP(R.string.key_mouse_pan_up),
    MOUSE_PAN_DOWN(R.string.key_mouse_pan_down),
    MOUSE_PAN_LEFT(R.string.key_mouse_pan_left),
    MOUSE_PAN_RIGHT(R.string.key_mouse_pan_right),

    // Keyboard
    KEYBOARD_TYPE_INPUT(R.string.key_keyboard_type_text),
    KEYBOARD_KEY_INPUT(R.string.key_keyboard_key_press),
    KEYBOARD_KEY_DOWN(R.string.key_keyboard_key_down),
    KEYBOARD_KEY_UP(R.string.key_keyboard_key_up),
}

/**
 * wtype is a Linux command line utility that can send input to Wayland displays.
 * https://github.com/atx/wtype
 */
val wtypePreset = mapOf(
    RemoteControlKey.UP to Command("wtype -k Up", repeat = true),
    RemoteControlKey.RIGHT to Command("wtype -k Right", repeat = true),
    RemoteControlKey.DOWN to Command("wtype -k Down", repeat = true),
    RemoteControlKey.LEFT to Command("wtype -k Left", repeat = true),
    RemoteControlKey.SELECT to Command("wtype -k return"),
    RemoteControlKey.VOLUME_DOWN to Command("wtype -k XF86AudioLowerVolume"),
    RemoteControlKey.MUTE to Command("wtype -k XF86AudioMute"),
    RemoteControlKey.VOLUME_UP to Command("wtype -k XF86AudioRaiseVolume"),
    RemoteControlKey.BACK to Command("wtype -k XF86Back"),
    RemoteControlKey.HOME to Command("wtype -k Home"),
    RemoteControlKey.MENU to Command("wtype -k Menu"),
    RemoteControlKey.PREVIOUS to Command("wtype -k XF86AudioPrev"),
    RemoteControlKey.PLAY_PAUSE to Command("wtype -k XF86AudioPlay"),
    RemoteControlKey.NEXT to Command("wtype -k XF86AudioNext"),
    RemoteControlKey.MOUSE_MOVE to Command("wtype -mm %dx %dy"),
    RemoteControlKey.MOUSE_LEFT_CLICK to Command("wtype -c left"),
    RemoteControlKey.MOUSE_RIGHT_CLICK to Command("wtype -c right"),
    RemoteControlKey.MOUSE_LEFT_DOWN to Command("wtype -P left"),
    RemoteControlKey.MOUSE_LEFT_UP to Command("wtype -p left"),
    RemoteControlKey.MOUSE_RIGHT_DOWN to Command("wtype -P right"),
    RemoteControlKey.MOUSE_RIGHT_UP to Command("wtype -p right"),
    RemoteControlKey.MOUSE_PAN_UP to Command("wtype -A axis_v 1"),
    RemoteControlKey.MOUSE_PAN_DOWN to Command("wtype -A axis_v -1"),
    RemoteControlKey.MOUSE_PAN_LEFT to Command("wtype -A axis_h 1"),
    RemoteControlKey.MOUSE_PAN_RIGHT to Command("wtype -A axis_h -1"),
    RemoteControlKey.KEYBOARD_TYPE_INPUT to Command("wtype '%s'"),
    RemoteControlKey.KEYBOARD_KEY_INPUT to Command("wtype -k %s"),
    RemoteControlKey.KEYBOARD_KEY_DOWN to Command("wtype -P %s"),
    RemoteControlKey.KEYBOARD_KEY_UP to Command("wtype -p %s"),
)

/**
 * xdotool is a Linux command line utility that can send input to X11 displays.
 * https://github.com/jordansissel/xdotool
 */
val xdotoolPreset = mapOf(
    RemoteControlKey.UP to Command("DISPLAY=:0 xdotool key Up", repeat = true),
    RemoteControlKey.RIGHT to Command("DISPLAY=:0 xdotool key Right", repeat = true),
    RemoteControlKey.DOWN to Command("DISPLAY=:0 xdotool key Down", repeat = true),
    RemoteControlKey.LEFT to Command("DISPLAY=:0 xdotool key Left", repeat = true),
    RemoteControlKey.SELECT to Command("DISPLAY=:0 xdotool key return"),
    RemoteControlKey.VOLUME_DOWN to Command("DISPLAY=:0 xdotool key XF86AudioLowerVolume"),
    RemoteControlKey.MUTE to Command("DISPLAY=:0 xdotool key XF86AudioMute"),
    RemoteControlKey.VOLUME_UP to Command("DISPLAY=:0 xdotool key XF86AudioRaiseVolume"),
    RemoteControlKey.BACK to Command("DISPLAY=:0 xdotool key XF86Back"),
    RemoteControlKey.HOME to Command("DISPLAY=:0 xdotool key Home"),
    RemoteControlKey.MENU to Command("DISPLAY=:0 xdotool key Menu"),
    RemoteControlKey.PREVIOUS to Command("DISPLAY=:0 xdotool key XF86AudioPrev"),
    RemoteControlKey.PLAY_PAUSE to Command("DISPLAY=:0 xdotool key XF86AudioPlay"),
    RemoteControlKey.NEXT to Command("DISPLAY=:0 xdotool key XF86AudioNext"),
    RemoteControlKey.MOUSE_MOVE to Command("DISPLAY=:0 xdotool mousemove_relative -- %dx %dy"),
    RemoteControlKey.MOUSE_LEFT_CLICK to Command("DISPLAY=:0 xdotool click 1"),
    RemoteControlKey.MOUSE_RIGHT_CLICK to Command("DISPLAY=:0 xdotool click 3"),
    RemoteControlKey.MOUSE_LEFT_DOWN to Command("DISPLAY=:0 xdotool mousedown 1"),
    RemoteControlKey.MOUSE_LEFT_UP to Command("DISPLAY=:0 xdotool mouseup 1"),
    RemoteControlKey.MOUSE_RIGHT_DOWN to Command("DISPLAY=:0 xdotool mousedown 3"),
    RemoteControlKey.MOUSE_RIGHT_UP to Command("DISPLAY=:0 xdotool mouseup 3"),
    RemoteControlKey.MOUSE_PAN_UP to Command("DISPLAY=:0 xdotool click 5"),
    RemoteControlKey.MOUSE_PAN_DOWN to Command("DISPLAY=:0 xdotool click 4"),
    RemoteControlKey.MOUSE_PAN_LEFT to Command("DISPLAY=:0 xdotool click 7"),
    RemoteControlKey.MOUSE_PAN_RIGHT to Command("DISPLAY=:0 xdotool click 6"),
    RemoteControlKey.KEYBOARD_TYPE_INPUT to Command("DISPLAY=:0 xdotool type '%s'"),
    RemoteControlKey.KEYBOARD_KEY_INPUT to Command("DISPLAY=:0 xdotool key %s"),
    RemoteControlKey.KEYBOARD_KEY_DOWN to Command("DISPLAY=:0 xdotool keydown %s"),
    RemoteControlKey.KEYBOARD_KEY_UP to Command("DISPLAY=:0 xdotool keyup %s"),
)

/**
 * ydotool is a generic Linux command-line automation tool.
 * https://github.com/ReimuNotMoe/ydotool
 */
val ydotoolPreset = mapOf(
    RemoteControlKey.UP to Command("ydotool key 103:1 103:0", repeat = true),
    RemoteControlKey.RIGHT to Command("ydotool key 106:1 106:0", repeat = true),
    RemoteControlKey.DOWN to Command("ydotool key 108:1 108:0", repeat = true),
    RemoteControlKey.LEFT to Command("ydotool key 105:1 105:0", repeat = true),
    RemoteControlKey.SELECT to Command("ydotool key 28:1 28:0"),
    RemoteControlKey.VOLUME_DOWN to Command("ydotool key 114:1 114:0"),
    RemoteControlKey.MUTE to Command("ydotool key 113:1 113:0"),
    RemoteControlKey.VOLUME_UP to Command("ydotool key 115:1 115:0"),
    RemoteControlKey.BACK to Command("ydotool key 158:1 158:0"),
    RemoteControlKey.HOME to Command("ydotool key 172:1 172:0"),
    RemoteControlKey.MENU to Command("ydotool key 139:1 139:0"),
    RemoteControlKey.PREVIOUS to Command("ydotool key 165:1 165:0"),
    RemoteControlKey.PLAY_PAUSE to Command("ydotool key 164:1 164:0"),
    RemoteControlKey.NEXT to Command("ydotool key 163:1 163:0"),
    RemoteControlKey.MOUSE_MOVE to Command("ydotool mousemove -- %dx %dy"),
    RemoteControlKey.MOUSE_LEFT_CLICK to Command("ydotool click 0xC0"),
    RemoteControlKey.MOUSE_RIGHT_CLICK to Command("ydotool click 0xC1"),
    RemoteControlKey.MOUSE_LEFT_DOWN to Command("ydotool click 0x40"),
    RemoteControlKey.MOUSE_LEFT_UP to Command("ydotool click 0x80"),
    RemoteControlKey.MOUSE_RIGHT_DOWN to Command("ydotool click 0x41"),
    RemoteControlKey.MOUSE_RIGHT_UP to Command("ydotool click 0x81"),
    RemoteControlKey.MOUSE_PAN_UP to Command("ydotool mousemove --wheel -- 0 -1"),
    RemoteControlKey.MOUSE_PAN_DOWN to Command("ydotool mousemove --wheel -- 0 1"),
    RemoteControlKey.MOUSE_PAN_LEFT to Command("ydotool mousemove --wheel -- 1 0"),
    RemoteControlKey.MOUSE_PAN_RIGHT to Command("ydotool mousemove --wheel -- -1 0"),
    RemoteControlKey.KEYBOARD_TYPE_INPUT to Command("ydotool type '%s'"),
    RemoteControlKey.KEYBOARD_KEY_INPUT to Command("ydotool key %d:1 %d:0"),
    RemoteControlKey.KEYBOARD_KEY_DOWN to Command("ydotool key %d:1"),
    RemoteControlKey.KEYBOARD_KEY_UP to Command("ydotool key %d:0"),
)

/**
 * dotool is a command-line tool to simulate input on Linux.
 * https://git.sr.ht/~geb/dotool
 */
val dotoolPreset = mapOf(
    RemoteControlKey.UP to Command("echo key up | dotool", repeat = true),
    RemoteControlKey.RIGHT to Command("echo key right | dotool", repeat = true),
    RemoteControlKey.DOWN to Command("echo key down | dotool", repeat = true),
    RemoteControlKey.LEFT to Command("echo key left | dotool", repeat = true),
    RemoteControlKey.SELECT to Command("echo key enter | dotool"),
    RemoteControlKey.VOLUME_DOWN to Command("echo key volumedown | dotool"),
    RemoteControlKey.MUTE to Command("echo key mute | dotool"),
    RemoteControlKey.VOLUME_UP to Command("echo key volumeup | dotool"),
    RemoteControlKey.BACK to Command("echo key back | dotool"),
    RemoteControlKey.HOME to Command("echo key homepage | dotool"),
    RemoteControlKey.MENU to Command("echo key menu | dotool"),
    RemoteControlKey.PREVIOUS to Command("echo key previoussong | dotool"),
    RemoteControlKey.PLAY_PAUSE to Command("echo key playpause | dotool"),
    RemoteControlKey.NEXT to Command("echo key nextsong | dotool"),
    RemoteControlKey.MOUSE_MOVE to Command("echo move %dx %dy | dotool"),
    RemoteControlKey.MOUSE_LEFT_CLICK to Command("echo click left | dotool"),
    RemoteControlKey.MOUSE_RIGHT_CLICK to Command("echo click right | dotool"),
    RemoteControlKey.MOUSE_LEFT_DOWN to Command("echo mousedown left | dotool"),
    RemoteControlKey.MOUSE_LEFT_UP to Command("echo mouseup left | dotool"),
    RemoteControlKey.MOUSE_RIGHT_DOWN to Command("echo mousedown right | dotool"),
    RemoteControlKey.MOUSE_RIGHT_UP to Command("echo mouseup right | dotool"),
    RemoteControlKey.MOUSE_PAN_UP to Command("echo wheel -1 | dotool"),
    RemoteControlKey.MOUSE_PAN_DOWN to Command("echo wheel 1 | dotool"),
    RemoteControlKey.MOUSE_PAN_LEFT to Command("echo hwheel -1 | dotool"),
    RemoteControlKey.MOUSE_PAN_RIGHT to Command("echo hwheel 1 | dotool"),
    RemoteControlKey.KEYBOARD_TYPE_INPUT to Command("echo type '%s' | dotool"),
    RemoteControlKey.KEYBOARD_KEY_INPUT to Command("echo key %s | dotool"),
    RemoteControlKey.KEYBOARD_KEY_DOWN to Command("echo keydown %s | dotool"),
    RemoteControlKey.KEYBOARD_KEY_UP to Command("echo keyup %s | dotool"),
)

/**
 * TODO
 */
val cecClientPreset = mapOf(
    RemoteControlKey.UP to Command("echo 'up' | cec-client -s", repeat = true),
    RemoteControlKey.RIGHT to Command("echo 'right' | cec-client -s", repeat = true),
    RemoteControlKey.DOWN to Command("echo 'down' | cec-client -s", repeat = true),
    RemoteControlKey.LEFT to Command("echo 'left' | cec-client -s", repeat = true),
    RemoteControlKey.SELECT to Command("echo 'select' | cec-client -s"),
    RemoteControlKey.VOLUME_DOWN to Command("echo 'vol-down' | cec-client -s"),
    RemoteControlKey.MUTE to Command("echo 'mute' | cec-client -s"),
    RemoteControlKey.VOLUME_UP to Command("echo 'vol-up' | cec-client -s"),
    RemoteControlKey.BACK to Command("echo 'exit' | cec-client -s"),
    RemoteControlKey.HOME to Command("echo 'home' | cec-client -s"),
    RemoteControlKey.MENU to Command("echo 'menu' | cec-client -s"),
    RemoteControlKey.PREVIOUS to Command("echo 'rew' | cec-client -s"),
    RemoteControlKey.PLAY_PAUSE to Command("echo 'play' | cec-client -s"),
    RemoteControlKey.NEXT to Command("echo 'ff' | cec-client -s"),
)

/**
 * Send AppleScript events to VLC on macOS.
 * To see the dictionary of supported commands, open Script Editor, then in the menu go to File -> Open Dictionary -> VLC.app.
 */
val macosVlcPreset = mapOf(
    RemoteControlKey.UP to Command("osascript -e 'tell application \"VLC\" to step forward 3'", repeat = true),
    RemoteControlKey.RIGHT to Command("osascript -e 'tell application \"VLC\" to step forward'", repeat = true),
    RemoteControlKey.DOWN to Command("osascript -e 'tell application \"VLC\" to step backward 3'", repeat = true),
    RemoteControlKey.LEFT to Command("osascript -e 'tell application \"VLC\" to step backward'", repeat = true),
    RemoteControlKey.SELECT to Command("osascript -e 'tell application \"VLC\" to fullscreen'"),
    RemoteControlKey.VOLUME_DOWN to Command("osascript -e 'tell application \"VLC\" to volumeDown'"),
    RemoteControlKey.MUTE to Command("osascript -e 'tell application \"VLC\" to mute'"),
    RemoteControlKey.VOLUME_UP to Command("osascript -e 'tell application \"VLC\" to volumeUp'"),
    RemoteControlKey.BACK to Command("osascript -e 'tell application \"VLC\" to stop'"),
    RemoteControlKey.PREVIOUS to Command("osascript -e 'tell application \"VLC\" to previous'"),
    RemoteControlKey.PLAY_PAUSE to Command("osascript -e 'tell application \"VLC\" to play'"),
    RemoteControlKey.NEXT to Command("osascript -e 'tell application \"VLC\" to next'"),
)

/**
 * Android "input" command.
 * https://developer.android.com/reference/android/view/KeyEvent
 */
val androidPreset = mapOf(
    RemoteControlKey.UP to Command("input keyevent KEYCODE_DPAD_UP", repeat = true),
    RemoteControlKey.RIGHT to Command("input keyevent KEYCODE_DPAD_RIGHT", repeat = true),
    RemoteControlKey.DOWN to Command("input keyevent KEYCODE_DPAD_DOWN", repeat = true),
    RemoteControlKey.LEFT to Command("input keyevent KEYCODE_DPAD_LEFT", repeat = true),
    RemoteControlKey.SELECT to Command(
        "input keyevent KEYCODE_DPAD_CENTER",
        "input keyevent --longpress KEYCODE_DPAD_CENTER",
    ),
    RemoteControlKey.VOLUME_DOWN to Command("input keyevent KEYCODE_VOLUME_DOWN"),
    RemoteControlKey.MUTE to Command("input keyevent KEYCODE_VOLUME_MUTE"),
    RemoteControlKey.VOLUME_UP to Command("input keyevent KEYCODE_VOLUME_UP"),
    RemoteControlKey.BACK to Command("input keyevent KEYCODE_BACK"),
    RemoteControlKey.HOME to Command("input keyevent KEYCODE_HOME"),
    RemoteControlKey.MENU to Command("input keyevent KEYCODE_MENU"),
    RemoteControlKey.PREVIOUS to Command("input keyevent KEYCODE_MEDIA_PREVIOUS"),
    RemoteControlKey.PLAY_PAUSE to Command("input keyevent KEYCODE_MEDIA_PLAY_PAUSE"),
    RemoteControlKey.NEXT to Command("input keyevent KEYCODE_MEDIA_NEXT"),
    RemoteControlKey.KEYBOARD_TYPE_INPUT to Command("input text '%s'"),
    RemoteControlKey.KEYBOARD_KEY_INPUT to Command("input keyevent %s"),
)

val presets = mapOf(
    "ydotool" to ydotoolPreset,
    "xdotool" to xdotoolPreset,
    "wtype" to wtypePreset,
    "dotool" to dotoolPreset,
    "cec-client" to cecClientPreset,
    "macOS VLC" to macosVlcPreset,
    "Android" to androidPreset,
)
