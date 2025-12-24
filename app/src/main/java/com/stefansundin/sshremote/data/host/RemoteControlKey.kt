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

package com.stefansundin.sshremote.data.host

enum class RemoteControlKey(val title: String) {
    // Dpad
    UP("Up"),
    RIGHT("Right"),
    DOWN("Down"),
    LEFT("Left"),
    SELECT("Select"),

    // Action buttons
    VOLUME_DOWN("Volume Down"),
    MUTE("Mute"),
    VOLUME_UP("Volume Up"),
    BACK("Back"),
    HOME("Home"),
    MENU("Menu"),
    PREVIOUS("Previous"),
    PLAY_PAUSE("Play/Pause"),
    NEXT("Next"),

    // Mouse
    MOUSE_MOVE("Mouse Move"),
    MOUSE_LEFT_CLICK("Left Click"),
    MOUSE_RIGHT_CLICK("Right Click"),
    MOUSE_PAN_UP("Two-Finger Pan Up"),
    MOUSE_PAN_DOWN("Two-Finger Pan Down"),
    MOUSE_PAN_LEFT("Two-Finger Pan Left"),
    MOUSE_PAN_RIGHT("Two-Finger Pan Right"),

    // Keyboard
    KEYBOARD_TYPE_INPUT("Keyboard Type"),
    KEYBOARD_KEY_INPUT("Keyboard Key"),
}

/**
 * wtype is a Linux command line utility that can send input to Wayland displays.
 * https://github.com/atx/wtype
 */
val wtypePreset = mapOf(
    RemoteControlKey.UP to Command("wtype -k Up"),
    RemoteControlKey.RIGHT to Command("wtype -k Right"),
    RemoteControlKey.DOWN to Command("wtype -k Down"),
    RemoteControlKey.LEFT to Command("wtype -k Left"),
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
    RemoteControlKey.MOUSE_PAN_UP to Command("wtype -A axis_v 1"),
    RemoteControlKey.MOUSE_PAN_DOWN to Command("wtype -A axis_v -1"),
    RemoteControlKey.MOUSE_PAN_LEFT to Command("wtype -A axis_h 1"),
    RemoteControlKey.MOUSE_PAN_RIGHT to Command("wtype -A axis_h -1"),
    RemoteControlKey.KEYBOARD_TYPE_INPUT to Command("wtype '%s'"),
    RemoteControlKey.KEYBOARD_KEY_INPUT to Command("wtype -k %s"),
)

/**
 * xdotool is a Linux command line utility that can send input to X11 displays.
 * https://github.com/jordansissel/xdotool
 */
val xdotoolPreset = mapOf(
    RemoteControlKey.UP to Command("DISPLAY=:0 xdotool key Up"),
    RemoteControlKey.RIGHT to Command("DISPLAY=:0 xdotool key Right"),
    RemoteControlKey.DOWN to Command("DISPLAY=:0 xdotool key Down"),
    RemoteControlKey.LEFT to Command("DISPLAY=:0 xdotool key Left"),
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
    RemoteControlKey.MOUSE_PAN_UP to Command("DISPLAY=:0 xdotool click 5"),
    RemoteControlKey.MOUSE_PAN_DOWN to Command("DISPLAY=:0 xdotool click 4"),
    RemoteControlKey.MOUSE_PAN_LEFT to Command("DISPLAY=:0 xdotool click 7"),
    RemoteControlKey.MOUSE_PAN_RIGHT to Command("DISPLAY=:0 xdotool click 6"),
    RemoteControlKey.KEYBOARD_TYPE_INPUT to Command("DISPLAY=:0 xdotool type '%s'"),
    RemoteControlKey.KEYBOARD_KEY_INPUT to Command("DISPLAY=:0 xdotool key %s"),
)

/**
 * TODO
 */
val cecClientPreset = mapOf(
    RemoteControlKey.UP to Command("echo 'up' | cec-client -s"),
    RemoteControlKey.RIGHT to Command("echo 'right' | cec-client -s"),
    RemoteControlKey.DOWN to Command("echo 'down' | cec-client -s"),
    RemoteControlKey.LEFT to Command("echo 'left' | cec-client -s"),
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
    RemoteControlKey.UP to Command("osascript -e 'tell application \"VLC\" to step forward 3'"),
    RemoteControlKey.RIGHT to Command("osascript -e 'tell application \"VLC\" to step forward'"),
    RemoteControlKey.DOWN to Command("osascript -e 'tell application \"VLC\" to step backward 3'"),
    RemoteControlKey.LEFT to Command("osascript -e 'tell application \"VLC\" to step backward'"),
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
    RemoteControlKey.UP to Command("input keyevent KEYCODE_DPAD_UP"),
    RemoteControlKey.RIGHT to Command("input keyevent KEYCODE_DPAD_RIGHT"),
    RemoteControlKey.DOWN to Command("input keyevent KEYCODE_DPAD_DOWN"),
    RemoteControlKey.LEFT to Command("input keyevent KEYCODE_DPAD_LEFT"),
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
    "wtype" to wtypePreset,
    "xdotool" to xdotoolPreset,
    "cec-client" to cecClientPreset,
    "macOS VLC" to macosVlcPreset,
    "Android" to androidPreset,
)
