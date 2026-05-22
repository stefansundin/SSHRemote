## About SSH Remote

SSH Remote is a free and open source app that allows you to control computers remotely using SSH.

You can fully customize the commands that are executed, and there are presets for common setups.

I use this app to control my HTPC setup, which is running Raspberry Pi OS. Controlling an HTPC is the basic scenario that the app is optimized for.

This app is not a terminal emulator, but it will let you run `apt-get install` in an emergency.

## Getting Started

If you want to use an SSH key to connect, first open the app settings and import or generate a key.

Add a new host by tapping the `+` button in the bottom right corner. Enter the connection details and save.

The first time you connect, you will be asked to select a preset. This selection configures the remote control buttons to work well on various types of computers. See below for a description of the available presets. If you prefer, you can start with no configuration by selecting `No preset`.

If you don't know whether your Linux computer is running X11 or Wayland, run this in a terminal:

```shell
echo $XDG_SESSION_TYPE
```

This should output `x11` or `wayland`. You must run this inside the desktop environment.

## Remote Control

Once connected, you can use the remote control interface to send commands. Switch tabs to access various input methods.

Each button press will execute a command on the host. This is a lot of overhead for something as simple as a key press, and you may experience a fairly high amount of latency compared to a regular keyboard. I hope to improve this in future versions.

Use the menu to enter edit mode. It is currently not possible to edit the layout or button icons. I hope to make this possible in a future version.

## Presets

You will need to install the tool required for your desktop environment.

I recommend `ydotool` because in my testing it has the best performance, and it works on both X11 and Wayland.

### ydotool

`ydotool` should work with any window manager, but you need a background service running. If your distribution provides a systemd user service then start it by running:

```shell
systemctl start --user ydotool
```

Automatically start the service on login by running:

```shell
systemctl enable --user ydotool
```

Please make sure you are installing a recent enough version of `ydotool`. Ubuntu versions before 26.04 provide versions that are too old. See the discussion thread for a workaround.

Please discuss `ydotool` in this discussion thread: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` is for computers running X11. X11 is what most Linux computers have used historically, though Wayland is becoming more popular.

One quirk with X11 is that you may need to allow access to the X server. This is the problem if you get "Authorization required" errors. You have a few options to fix this issue, here are two options that have worked for me:

If `xauth list` doesn't show any entries then try to generate an `.Xauthority` file:

```shell
xauth generate :0 . trusted
```

If that didn't work then try to grant access using `xhost`:

```shell
xhost +local:$USER
```

You will need to run the `xhost` command after every boot. You can automate this by creating a bash script and configure it to autostart on login.

Please discuss `xdotool` in this discussion thread: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` is like `xdotool`, but for Wayland.

Normally, it does not support mouse control, but I have created a modified version that adds mouse support. Please install it if you need mouse support: <https://github.com/stefansundin/wtype>

If you get the error `Compositor does not support the virtual keyboard protocol` then I suggest that you try another tool, like `ydotool`.

Please discuss `wtype` in this discussion thread: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` should work with any window manager, similar to `ydotool`. In my limited testing it was much slower than `ydotool`.

Please discuss `dotool` in this discussion thread: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

This preset is experimental as I have not been able to verify it on my own hardware. Feedback is welcome.

Please discuss `cec-client` in this discussion thread: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

There is a preset for controlling VLC on macOS, using AppleScript commands. I am not aware of a tool that supports sending keyboard or mouse events.

Please discuss macOS in this discussion thread: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

If your Android device comes with an SSH server, then you may be able to connect to it and send input events. This is more likely if your device is running a custom ROM, like KonstaKANG on the Raspberry Pi.

I have not figured out how to get mouse support working.

Please discuss Android in this discussion thread: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Smart volume control

When editing the remote control, you can find "smart" volume control settings in the menu. This can display the computer's current volume in the app and let you quickly set the volume using a slider. You can also use your device's hardware buttons to quickly send volume up/down commands.

Reading the current volume and setting a new volume using the slider is currently hard-coded to use `pactl`.

The package containing `pactl` is usually called `pulseaudio-utils` or `libpulse`.

## SSH keys

You can import or generate SSH keys in the app settings. Connecting with an SSH key is more secure than using passwords.

The easiest way to import an existing SSH key from a computer is to scan a QR code. You can use the `qrencode` program to generate the QR code image. Run a command like the following to generate the QR code:

```shell
# Navigate to your SSH keys:
cd ~/.ssh

# Display the QR code in the terminal:
qrencode -r id_ed25519 -t ansiutf8

# Alternatively, create an image file:
qrencode -r id_ed25519 -o qr.png

# 4096-bit RSA keys are too big for a QR code. You can use gzip to just barely fit one:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

You can push public SSH keys to a server using the `Push public key` feature in the menu. This will append the selected SSH key to the `~/.ssh/authorized_keys` file. This lets you easily migrate from logging in with a password to logging in with an SSH key.

You can import and use encrypted SSH keys, but you can't generate these in the app currently.

## Security

It is not possible to export or extract the private part of SSH keys, or stored passwords, from the app. This data is encrypted using 256-bit AES, and the encryption key is stored in the Android Keystore. Encrypted data is excluded from Android backups.

There is no crash reporting software in this app. There is no telemetry. There are no ads. There are no network requests except for the SSH connection.

The security of this app has not been audited. If you are experienced with Android security or SSH security, please take a look at the source code and report your findings in this GitHub issue:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Feature requests

Feel free to submit feature requests and bug reports in the GitHub repository. Please use English. Please keep your comments civil. Disrespectful comments will be removed and users may be blocked from the repository.

Please look through existing issues and discussion threads to see if your question has already been asked or answered.

Please be respectful. I built this app in my free time and I am giving it away for free. I am building this app for my own use, first and foremost.

Please do not email me questions. Please try to keep conversations on GitHub, since that helps other people too! You can ask questions in the discussion section on GitHub.

You are always welcome to fork the app to implement your own features. That's a great way to learn. Please consider contributing useful features.

The source code is licensed under GNU GPLv3. If you distribute modified versions of this app then you must also make the source code available.

<https://github.com/stefansundin/SSHRemote>

## Donations

If you wish to show your gratitude and appreciation, donations are accepted.

<https://stefansundin.github.io/donate/>

If you have donated then I will try my best to answer any question you may have.

Thank you for your support!
