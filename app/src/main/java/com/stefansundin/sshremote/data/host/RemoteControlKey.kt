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
    RemoteControlKey.UP to "wtype -k Up",
    RemoteControlKey.RIGHT to "wtype -k Right",
    RemoteControlKey.DOWN to "wtype -k Down",
    RemoteControlKey.LEFT to "wtype -k Left",
    RemoteControlKey.SELECT to "wtype -k return",
    RemoteControlKey.VOLUME_DOWN to "wtype -k XF86AudioLowerVolume",
    RemoteControlKey.MUTE to "wtype -k XF86AudioMute",
    RemoteControlKey.VOLUME_UP to "wtype -k XF86AudioRaiseVolume",
    RemoteControlKey.BACK to "wtype -k XF86Back",
    RemoteControlKey.HOME to "wtype -k Home",
    RemoteControlKey.MENU to "wtype -k Menu",
    RemoteControlKey.PREVIOUS to "wtype -k XF86AudioPrev",
    RemoteControlKey.PLAY_PAUSE to "wtype -k XF86AudioPlay",
    RemoteControlKey.NEXT to "wtype -k XF86AudioNext",
    RemoteControlKey.MOUSE_MOVE to "wtype -mm %dx %dy",
    RemoteControlKey.MOUSE_LEFT_CLICK to "wtype -c left",
    RemoteControlKey.MOUSE_RIGHT_CLICK to "wtype -c right",
    RemoteControlKey.MOUSE_PAN_UP to "wtype -A axis_v 1",
    RemoteControlKey.MOUSE_PAN_DOWN to "wtype -A axis_v -1",
    RemoteControlKey.MOUSE_PAN_LEFT to "wtype -A axis_h 1",
    RemoteControlKey.MOUSE_PAN_RIGHT to "wtype -A axis_h -1",
)

/**
 * xdotool is a Linux command line utility that can send input to X11 displays.
 * https://github.com/jordansissel/xdotool
 */
val xdotoolPreset = mapOf(
    RemoteControlKey.UP to "DISPLAY=:0 xdotool key Up",
    RemoteControlKey.RIGHT to "DISPLAY=:0 xdotool key Right",
    RemoteControlKey.DOWN to "DISPLAY=:0 xdotool key Down",
    RemoteControlKey.LEFT to "DISPLAY=:0 xdotool key Left",
    RemoteControlKey.SELECT to "DISPLAY=:0 xdotool key return",
    RemoteControlKey.VOLUME_DOWN to "DISPLAY=:0 xdotool key XF86AudioLowerVolume",
    RemoteControlKey.MUTE to "DISPLAY=:0 xdotool key XF86AudioMute",
    RemoteControlKey.VOLUME_UP to "DISPLAY=:0 xdotool key XF86AudioRaiseVolume",
    RemoteControlKey.BACK to "DISPLAY=:0 xdotool key XF86Back",
    RemoteControlKey.HOME to "DISPLAY=:0 xdotool key Home",
    RemoteControlKey.MENU to "DISPLAY=:0 xdotool key Menu",
    RemoteControlKey.PREVIOUS to "DISPLAY=:0 xdotool key XF86AudioPrev",
    RemoteControlKey.PLAY_PAUSE to "DISPLAY=:0 xdotool key XF86AudioPlay",
    RemoteControlKey.NEXT to "DISPLAY=:0 xdotool key XF86AudioNext",
    RemoteControlKey.MOUSE_MOVE to "DISPLAY=:0 xdotool mousemove_relative -- %dx %dy",
    RemoteControlKey.MOUSE_LEFT_CLICK to "DISPLAY=:0 xdotool click 1",
    RemoteControlKey.MOUSE_RIGHT_CLICK to "DISPLAY=:0 xdotool click 3",
    RemoteControlKey.MOUSE_PAN_UP to "DISPLAY=:0 xdotool click 5",
    RemoteControlKey.MOUSE_PAN_DOWN to "DISPLAY=:0 xdotool click 4",
    RemoteControlKey.MOUSE_PAN_LEFT to "DISPLAY=:0 xdotool click 7",
    RemoteControlKey.MOUSE_PAN_RIGHT to "DISPLAY=:0 xdotool click 6",
)

/**
 * TODO
 */
val cecClientPreset = mapOf(
    RemoteControlKey.UP to "echo 'up' | cec-client -s",
    RemoteControlKey.RIGHT to "echo 'right' | cec-client -s",
    RemoteControlKey.DOWN to "echo 'down' | cec-client -s",
    RemoteControlKey.LEFT to "echo 'left' | cec-client -s",
    RemoteControlKey.SELECT to "echo 'select' | cec-client -s",
    RemoteControlKey.VOLUME_DOWN to "echo 'vol-down' | cec-client -s",
    RemoteControlKey.MUTE to "echo 'mute' | cec-client -s",
    RemoteControlKey.VOLUME_UP to "echo 'vol-up' | cec-client -s",
    RemoteControlKey.BACK to "echo 'exit' | cec-client -s",
    RemoteControlKey.HOME to "echo 'home' | cec-client -s",
    RemoteControlKey.MENU to "echo 'menu' | cec-client -s",
    RemoteControlKey.PREVIOUS to "echo 'rew' | cec-client -s",
    RemoteControlKey.PLAY_PAUSE to "echo 'play' | cec-client -s",
    RemoteControlKey.NEXT to "echo 'ff' | cec-client -s",
)

/**
 * Send AppleScript events to VLC on macOS.
 * To see the dictionary of supported commands, open Script Editor, then in the menu go to File -> Open Dictionary -> VLC.app.
 */
val macosVlcPreset = mapOf(
    RemoteControlKey.UP to "osascript -e 'tell application \"VLC\" to step forward 3'",
    RemoteControlKey.RIGHT to "osascript -e 'tell application \"VLC\" to step forward'",
    RemoteControlKey.DOWN to "osascript -e 'tell application \"VLC\" to step backward 3'",
    RemoteControlKey.LEFT to "osascript -e 'tell application \"VLC\" to step backward'",
    RemoteControlKey.SELECT to "osascript -e 'tell application \"VLC\" to fullscreen'",
    RemoteControlKey.VOLUME_DOWN to "osascript -e 'tell application \"VLC\" to volumeDown'",
    RemoteControlKey.MUTE to "osascript -e 'tell application \"VLC\" to mute'",
    RemoteControlKey.VOLUME_UP to "osascript -e 'tell application \"VLC\" to volumeUp'",
    RemoteControlKey.BACK to "osascript -e 'tell application \"VLC\" to stop'",
    RemoteControlKey.HOME to "osascript -e 'tell application \"VLC\" to stop'",
    RemoteControlKey.MENU to "osascript -e 'tell application \"VLC\" to stop'",
    RemoteControlKey.PREVIOUS to "osascript -e 'tell application \"VLC\" to previous'",
    RemoteControlKey.PLAY_PAUSE to "osascript -e 'tell application \"VLC\" to play'",
    RemoteControlKey.NEXT to "osascript -e 'tell application \"VLC\" to next'",
)
