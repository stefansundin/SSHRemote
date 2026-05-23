## 关于 SSH Remote

此翻译由 AI 使用 GitHub Copilot（GPT-5.3-Codex）生成，内容中可能存在一些翻译错误。

SSH Remote 是一款免费且开源的应用，允许你通过 SSH 远程控制电脑。

你可以完全自定义要执行的命令，并且应用内提供了适用于常见配置的预设。

我使用这款应用来控制我的 HTPC 设置，它运行的是 Raspberry Pi OS。控制 HTPC 是这款应用主要优化的基本使用场景。

这款应用不是终端模拟器，但在紧急情况下，它仍然能让你运行 `apt-get install`。

## 开始使用

如果你想使用 SSH 密钥进行连接，请先打开应用设置并导入或生成一个密钥。

点击右下角的 `+` 按钮来添加新主机。输入连接详情后保存。

首次连接时，系统会要求你选择一个预设。这个选择会配置遥控器按钮，使其更适合在不同类型的电脑上使用。下面会介绍可用预设。如果你愿意，也可以选择 `No preset`，在没有任何配置的情况下开始使用。

如果你不知道你的 Linux 电脑运行的是 X11 还是 Wayland，请在终端中运行：

```shell
echo $XDG_SESSION_TYPE
```

它应该输出 `x11` 或 `wayland`。你必须在桌面环境内部运行此命令。

## 遥控器

连接成功后，你可以使用遥控器界面发送命令。切换标签页即可使用不同的输入方式。

每次按下按钮都会在主机上执行一个命令。对于像按键这样简单的操作来说，这样的开销相当大，因此与普通键盘相比，你可能会感觉到较高的延迟。我希望在未来版本中改善这一点。

使用菜单进入编辑模式。目前还不能编辑布局或按钮图标。我希望未来版本能支持这些功能。

## 预设

你需要安装适用于你的桌面环境的工具。

我推荐 `ydotool`，因为根据我的测试，它的性能最好，而且同时适用于 X11 和 Wayland。

### ydotool

`ydotool` 应该适用于任何窗口管理器，但你需要有一个后台服务正在运行。如果你的发行版提供 systemd 用户服务，那么请运行以下命令来启动它：

```shell
systemctl start --user ydotool
```

要让该服务在登录时自动启动，请运行：

```shell
systemctl enable --user ydotool
```

请确保你安装的是足够新的 `ydotool` 版本。Ubuntu 26.04 之前的版本提供的 `ydotool` 太旧。请查看讨论串以获取替代解决方案。

请在这个讨论串中讨论 `ydotool`：<https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` 适用于运行 X11 的电脑。历史上，大多数 Linux 电脑都使用 X11，不过 Wayland 正变得越来越流行。

X11 的一个特点是，你可能需要允许访问 X 服务器。如果你遇到 “Authorization required” 错误，问题通常就在这里。你有几种方法可以解决这个问题，下面是两个对我有效的做法：

如果 `xauth list` 没有显示任何条目，请尝试生成一个 `.Xauthority` 文件：

```shell
xauth generate :0 . trusted
```

如果这还不行，请尝试使用 `xhost` 授予访问权限：

```shell
xhost +local:$USER
```

你需要在每次开机后都运行一次 `xhost` 命令。你可以通过创建一个 bash 脚本并将其配置为登录时自动启动来实现自动化。

请在这个讨论串中讨论 `xdotool`：<https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` 类似于 `xdotool`，但用于 Wayland。

通常情况下，它不支持鼠标控制，但我创建了一个增加鼠标支持的修改版本。如果你需要鼠标支持，请安装它：<https://github.com/stefansundin/wtype>

如果你遇到 `Compositor does not support the virtual keyboard protocol` 错误，那么我建议你尝试其他工具，例如 `ydotool`。

请在这个讨论串中讨论 `wtype`：<https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` 应该和 `ydotool` 一样，适用于任何窗口管理器。根据我有限的测试，它比 `ydotool` 慢很多。

请在这个讨论串中讨论 `dotool`：<https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

这个预设仍属实验性质，因为我还无法在自己的硬件上验证它。欢迎反馈。

请在这个讨论串中讨论 `cec-client`：<https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

有一个预设可以使用 AppleScript 命令控制 macOS 上的 VLC。至于能发送键盘或鼠标事件的工具，我目前还不知道有哪一个可以做到。

请在这个讨论串中讨论 macOS：<https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

如果你的 Android 设备自带 SSH 服务器，那么你也许可以连接到它并发送输入事件。如果你的设备运行的是自定义 ROM，例如 Raspberry Pi 上的 KonstaKANG，那么这种可能性会更高。

我还没有弄清楚如何让鼠标支持正常工作。

请在这个讨论串中讨论 Android：<https://github.com/stefansundin/SSHRemote/discussions/9>

## 智能音量控制

编辑遥控器时，你可以在菜单中找到“智能”音量控制设置。它可以在应用中显示电脑当前的音量，并让你通过滑块快速设置音量。你还可以使用设备的硬件按钮，快速发送音量增大/减小命令。

读取当前音量以及通过滑块设置新音量，目前是硬编码为使用 `pactl` 的。

包含 `pactl` 的软件包通常叫做 `pulseaudio-utils` 或 `libpulse`。

## SSH 密钥

你可以在应用设置中导入或生成 SSH 密钥。使用 SSH 密钥连接比使用密码更安全。

从电脑导入现有 SSH 密钥最简单的方法，是扫描一个二维码。你可以使用 `qrencode` 程序来生成二维码图像。运行如下命令即可生成二维码：

```shell
# 进入你的 SSH 密钥目录：
cd ~/.ssh

# 在终端中显示二维码：
qrencode -r id_ed25519 -t ansiutf8

# 或者，创建一个图像文件：
qrencode -r id_ed25519 -o qr.png

# 4096 位 RSA 密钥对于二维码来说太大了。你可以使用 gzip 勉强塞进一个：
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

你可以通过菜单中的 `推送公钥` 功能，将 SSH 公钥推送到服务器。这会把选中的 SSH 密钥追加到 `~/.ssh/authorized_keys` 文件中。这样你就可以轻松地从使用密码登录迁移到使用 SSH 密钥登录。

你可以导入和使用已加密的 SSH 密钥，但目前无法在应用中生成这类密钥。

## 安全

无法从应用中导出或提取 SSH 密钥的私钥部分，也无法导出或提取已保存的密码。这些数据使用 256 位 AES 加密，而加密密钥存储在 Android Keystore 中。加密数据不会包含在 Android 备份中。

这款应用没有崩溃报告软件，没有遥测，没有广告。除 SSH 连接外，也没有任何网络请求。

这款应用的安全性尚未经过审计。如果你对 Android 安全或 SSH 安全有经验，请查看源代码，并在这个 GitHub issue 中报告你的发现：

<https://github.com/stefansundin/SSHRemote/issues/1>

## 功能请求

欢迎你在 GitHub 仓库中提交功能请求和错误报告。请使用英语。请保持评论文明。无礼的评论会被删除，相关用户也可能会被仓库屏蔽。

请先查看现有的 issue 和讨论串，看看你的问题是否已经有人问过或回答过。

请保持尊重。我是在自己的空闲时间开发这款应用的，而且我将它免费提供给大家。首先，这款应用是为我自己的使用场景而开发的。

请不要通过电子邮件向我提问。请尽量把讨论留在 GitHub 上，因为这样也能帮助到其他人！你可以在 GitHub 的讨论区提问。

你始终可以 fork 这款应用来实现你自己的功能。这是一个很好的学习方式。也请考虑贡献有用的功能。

源代码采用 GNU GPLv3 许可证。如果你分发此应用的修改版本，那么你也必须提供相应的源代码。

<https://github.com/stefansundin/SSHRemote>

## 捐赠

如果你想表达你的感激与支持，欢迎捐赠。

<https://stefansundin.github.io/donate/>

如果你已经捐赠，我会尽力回答你可能提出的任何问题。请以英语书写所有咨询内容。

感谢你的支持！
