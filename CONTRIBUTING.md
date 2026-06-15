# Contributing to SSH Remote

First of all, thank you for contributing a feature or bug fix.

Even though not all contributions may be accepted, I encourage you to submit the PR anyway since it helps other people to discover your fork. Even rejected features may eventually work their way into the official app in one shape or another.

If you haven't already, please read through the Help screen in the app. There's a lot of good information there. Not all features need to be described in that text, but if yours does then please add the appropriate text. If you are unsure, just ask me in the PR description.

Translations have been done with the help of AI. If you don't have readily access to a competent AI model then feel free to omit the translation for your new strings. I will translate any new strings before releasing a new version.


## Quality bar

Let's aim to make this app the best it can be. I appreciate a high level of polish.

It is OK to use AI to build new features or fix bugs, as long as you review the code thoroughly and understand what it does.

Please test new UI with haptic feedback and touch sounds enabled. These must be enabled in the Android settings to be heard. Ideally the UI framework would handle it for us, but unfortunately Compose does not do this for us yet (legacy XML View applications automatically emit these sounds properly without the developer having to do anything). You will need to add `view.playSoundEffect(SoundEffectConstants.CLICK)` for all taps and `view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)` for long presses.

If it's not too much work, include a `@Preview` for your new UI. Test new UI on the "Small Phone" form factor in the emulator, with the font size set to the largest option.

Take care when adding new dependencies. Make sure it's not adding an unreasonable amount to the APK file size.

Test your feature on Android 6 in the emulator. If it doesn't work then disable or hide the feature when the device does not support it.

Lastly, I want to note that this is my first proper Android app. Please teach me the better way if you think that I am not following Android best practices.


## Commit messages

Please do NOT use conventional commit messages. Make the commit message human-readable, this also goes for your issue or pull request title. Avoid use of emoji.

You can, if you wish, prefix your commit message with the file name or class that you are improving. Here's an example:

```
EditHostScreen: Fix bug not being able to open the QR code scanner more than once.
```

Please limit the first line of the commit message to a reasonable length. If you want to include lots of information then please put it in the commit message body. The message summary is the first line, and the body is after two newlines.


## Please give me plenty of time to respond

I might not get back to you very quickly. It depends on how much free time I have and how eager I am to work on the app at the time that you submit the PR.

If I haven't replied in a reasonable time period, and you are getting impatient, then please feel free to give me a gentle @-mention. I apologize in advance. 🫠
