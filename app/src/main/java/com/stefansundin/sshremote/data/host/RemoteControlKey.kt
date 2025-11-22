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
}

/**
 * wtype is a Linux command line utility that can send input to Wayland displays.
 * https://github.com/atx/wtype
 */
val wtypePreset = mapOf(
    RemoteControlKey.UP to Command("wtype -k Up", "Up"),
    RemoteControlKey.RIGHT to Command("wtype -k Right", "Right"),
    RemoteControlKey.DOWN to Command("wtype -k Down", "Down"),
    RemoteControlKey.LEFT to Command("wtype -k Left", "Left"),
    RemoteControlKey.SELECT to Command("wtype -k return", "Select"),
    RemoteControlKey.VOLUME_DOWN to Command("wtype -k XF86AudioLowerVolume", "Volume Down"),
    RemoteControlKey.MUTE to Command("wtype -k XF86AudioMute", "Mute"),
    RemoteControlKey.VOLUME_UP to Command("wtype -k XF86AudioRaiseVolume", "Volume Up"),
    RemoteControlKey.BACK to Command("wtype -k XF86Back", "Back"),
    RemoteControlKey.HOME to Command("wtype -k Home", "Home"),
    RemoteControlKey.MENU to Command("wtype -k Menu", "Menu"),
    RemoteControlKey.PREVIOUS to Command("wtype -k XF86AudioPrev", "Previous"),
    RemoteControlKey.PLAY_PAUSE to Command("wtype -k XF86AudioPlay", "Play/Pause"),
    RemoteControlKey.NEXT to Command("wtype -k XF86AudioNext", "Next"),
    RemoteControlKey.MOUSE_MOVE to Command("wtype -mm %dx %dy", "Mouse Move"),
    RemoteControlKey.MOUSE_LEFT_CLICK to Command("wtype -c left", "Left Click"),
    RemoteControlKey.MOUSE_RIGHT_CLICK to Command("wtype -c right", "Right Click"),
    RemoteControlKey.MOUSE_PAN_UP to Command("wtype -A axis_v 1", "Two-Finger Pan Up"),
    RemoteControlKey.MOUSE_PAN_DOWN to Command("wtype -A axis_v -1", "Two-Finger Pan Down"),
    RemoteControlKey.MOUSE_PAN_LEFT to Command("wtype -A axis_h 1", "Two-Finger Pan Left"),
    RemoteControlKey.MOUSE_PAN_RIGHT to Command("wtype -A axis_h -1", "Two-Finger Pan Right"),
)

/**
 * xdotool is a Linux command line utility that can send input to X11 displays.
 * https://github.com/jordansissel/xdotool
 */
val xdotoolPreset = mapOf(
    RemoteControlKey.UP to Command("DISPLAY=:0 xdotool key Up", "Up"),
    RemoteControlKey.RIGHT to Command("DISPLAY=:0 xdotool key Right", "Right"),
    RemoteControlKey.DOWN to Command("DISPLAY=:0 xdotool key Down", "Down"),
    RemoteControlKey.LEFT to Command("DISPLAY=:0 xdotool key Left", "Left"),
    RemoteControlKey.SELECT to Command("DISPLAY=:0 xdotool key return", "Select"),
    RemoteControlKey.VOLUME_DOWN to Command("DISPLAY=:0 xdotool key XF86AudioLowerVolume", "Volume Down"),
    RemoteControlKey.MUTE to Command("DISPLAY=:0 xdotool key XF86AudioMute", "Mute"),
    RemoteControlKey.VOLUME_UP to Command("DISPLAY=:0 xdotool key XF86AudioRaiseVolume", "Volume Up"),
    RemoteControlKey.BACK to Command("DISPLAY=:0 xdotool key XF86Back", "Back"),
    RemoteControlKey.HOME to Command("DISPLAY=:0 xdotool key Home", "Home"),
    RemoteControlKey.MENU to Command("DISPLAY=:0 xdotool key Menu", "Menu"),
    RemoteControlKey.PREVIOUS to Command("DISPLAY=:0 xdotool key XF86AudioPrev", "Previous"),
    RemoteControlKey.PLAY_PAUSE to Command("DISPLAY=:0 xdotool key XF86AudioPlay", "Play/Pause"),
    RemoteControlKey.NEXT to Command("DISPLAY=:0 xdotool key XF86AudioNext", "Next"),
    RemoteControlKey.MOUSE_MOVE to Command("DISPLAY=:0 xdotool mousemove_relative -- %dx %dy", "Mouse Move"),
    RemoteControlKey.MOUSE_LEFT_CLICK to Command("DISPLAY=:0 xdotool click 1", "Left Click"),
    RemoteControlKey.MOUSE_RIGHT_CLICK to Command("DISPLAY=:0 xdotool click 3", "Right Click"),
    RemoteControlKey.MOUSE_PAN_UP to Command("DISPLAY=:0 xdotool click 5", "Two-Finger Pan Up"),
    RemoteControlKey.MOUSE_PAN_DOWN to Command("DISPLAY=:0 xdotool click 4", "Two-Finger Pan Down"),
    RemoteControlKey.MOUSE_PAN_LEFT to Command("DISPLAY=:0 xdotool click 7", "Two-Finger Pan Left"),
    RemoteControlKey.MOUSE_PAN_RIGHT to Command("DISPLAY=:0 xdotool click 6", "Two-Finger Pan Right"),
)

/**
 * TODO
 */
val cecClientPreset = mapOf(
    RemoteControlKey.UP to Command("echo 'up' | cec-client -s", "Up"),
    RemoteControlKey.RIGHT to Command("echo 'right' | cec-client -s", "Right"),
    RemoteControlKey.DOWN to Command("echo 'down' | cec-client -s", "Down"),
    RemoteControlKey.LEFT to Command("echo 'left' | cec-client -s", "Left"),
    RemoteControlKey.SELECT to Command("echo 'select' | cec-client -s", "Select"),
    RemoteControlKey.VOLUME_DOWN to Command("echo 'vol-down' | cec-client -s", "Volume Down"),
    RemoteControlKey.MUTE to Command("echo 'mute' | cec-client -s", "Mute"),
    RemoteControlKey.VOLUME_UP to Command("echo 'vol-up' | cec-client -s", "Volume Up"),
    RemoteControlKey.BACK to Command("echo 'exit' | cec-client -s", "Back"),
    RemoteControlKey.HOME to Command("echo 'home' | cec-client -s", "Home"),
    RemoteControlKey.MENU to Command("echo 'menu' | cec-client -s", "Menu"),
    RemoteControlKey.PREVIOUS to Command("echo 'rew' | cec-client -s", "Previous"),
    RemoteControlKey.PLAY_PAUSE to Command("echo 'play' | cec-client -s", "Play/Pause"),
    RemoteControlKey.NEXT to Command("echo 'ff' | cec-client -s", "Next"),
)

/**
 * Send AppleScript events to VLC on macOS.
 * To see the dictionary of supported commands, open Script Editor, then in the menu go to File -> Open Dictionary -> VLC.app.
 */
val macosVlcPreset = mapOf(
    RemoteControlKey.UP to Command("osascript -e 'tell application \"VLC\" to step forward 3'", "Up"),
    RemoteControlKey.RIGHT to Command("osascript -e 'tell application \"VLC\" to step forward'", "Right"),
    RemoteControlKey.DOWN to Command("osascript -e 'tell application \"VLC\" to step backward 3'", "Down"),
    RemoteControlKey.LEFT to Command("osascript -e 'tell application \"VLC\" to step backward'", "Left"),
    RemoteControlKey.SELECT to Command("osascript -e 'tell application \"VLC\" to fullscreen'", "Select"),
    RemoteControlKey.VOLUME_DOWN to Command("osascript -e 'tell application \"VLC\" to volumeDown'", "Volume Down"),
    RemoteControlKey.MUTE to Command("osascript -e 'tell application \"VLC\" to mute'", "Mute"),
    RemoteControlKey.VOLUME_UP to Command("osascript -e 'tell application \"VLC\" to volumeUp'", "Volume Up"),
    RemoteControlKey.BACK to Command("osascript -e 'tell application \"VLC\" to stop'", "Back"),
    RemoteControlKey.HOME to Command("osascript -e 'tell application \"VLC\" to stop'", "Home"),
    RemoteControlKey.MENU to Command("osascript -e 'tell application \"VLC\" to stop'", "Menu"),
    RemoteControlKey.PREVIOUS to Command("osascript -e 'tell application \"VLC\" to previous'", "Previous"),
    RemoteControlKey.PLAY_PAUSE to Command("osascript -e 'tell application \"VLC\" to play'", "Play/Pause"),
    RemoteControlKey.NEXT to Command("osascript -e 'tell application \"VLC\" to next'", "Next"),
)
