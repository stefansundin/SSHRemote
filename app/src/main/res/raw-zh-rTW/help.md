## 關於 SSH Remote

此翻譯由 AI 協助完成，可能包含翻譯錯誤或不自然之處，請同時參考英文原文。

SSH Remote 是一款免費且開放原始碼的 app，讓你可以透過 SSH 遠端控制電腦。

你可以完全自訂要執行的指令，並且內建適用於常見環境的預設集。

我使用這個 app 來控制我的 HTPC 設定，它執行的是 Raspberry Pi OS。控制 HTPC 是這個 app 主要最佳化的使用情境。

這個 app 不是終端模擬器，但在緊急情況下，它仍然能讓你執行 `apt-get install`。

## 開始使用

如果你想使用 SSH 金鑰連線，請先開啟 app 設定並匯入或產生金鑰。

點一下右下角的 `+` 按鈕來新增主機。輸入連線詳細資料後儲存。

首次連線時，系統會要求你選擇一個預設集。這個選擇會設定遙控器按鈕，使其能在各種類型的電腦上良好運作。下方有各個預設集的說明。如果你願意，也可以選擇 `無預設集` 來不套用任何設定開始。

如果你不知道 Linux 電腦正在使用 X11 還是 Wayland，請在終端機中執行以下指令：

```shell
echo $XDG_SESSION_TYPE
```

這應該會輸出 `x11` 或 `wayland`。你必須在桌面環境內執行這個指令。

## 遙控器

連線後，你可以使用遙控器介面來傳送指令。切換分頁即可使用各種輸入方式。

每次按下按鈕都會在主機上執行一個指令。對於像按下一個按鍵這樣簡單的操作來說，這會帶來不少額外負擔，因此你可能會感受到相較於一般鍵盤更高的延遲。我希望未來版本能改善這一點。

使用選單進入編輯模式。目前還無法編輯版面配置或按鈕圖示。我希望未來版本能做到這件事。

## 預設集

你需要安裝適用於你的桌面環境的工具。

我推薦 `ydotool`，因為根據我的測試，它的效能最好，而且同時支援 X11 與 Wayland。

### ydotool

`ydotool` 應該可以搭配任何視窗管理器使用，但你需要讓背景服務保持執行。如果你的發行版有提供 systemd 使用者服務，請執行以下指令啟動：

```shell
systemctl start --user ydotool
```

如果要在登入時自動啟動服務，請執行：

```shell
systemctl enable --user ydotool
```

請確認你安裝的是夠新的 `ydotool` 版本。Ubuntu 26.04 之前提供的版本太舊。可參考討論串中的替代作法。

請在這個討論串討論 `ydotool`：<https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` 適用於執行 X11 的電腦。X11 是大多數 Linux 電腦過去一直使用的系統，不過 Wayland 正變得越來越普及。

X11 有一個特性是你可能需要允許存取 X server。如果你遇到 `Authorization required` 錯誤，通常就是這個問題。你有幾種方式可以修正，以下是兩種對我有效的方法：

如果 `xauth list` 沒有顯示任何項目，請試著產生 `.Xauthority` 檔案：

```shell
xauth generate :0 . trusted
```

如果那樣還是無效，請試著使用 `xhost` 授予存取權：

```shell
xhost +local:$USER
```

你需要在每次開機後重新執行 `xhost` 指令。你可以建立一個 bash 腳本來自動化，並將它設定為登入時自動啟動。

請在這個討論串討論 `xdotool`：<https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` 類似 `xdotool`，但適用於 Wayland。

正常情況下，它不支援滑鼠控制，但我做了一個加入滑鼠支援的修改版。如果你需要滑鼠支援，請安裝它：<https://github.com/stefansundin/wtype>

如果你看到錯誤 `Compositor does not support the virtual keyboard protocol`，我建議你改試其他工具，例如 `ydotool`。

請在這個討論串討論 `wtype`：<https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` 應該和 `ydotool` 一樣可搭配任何視窗管理器使用。在我有限的測試中，它比 `ydotool` 慢得多。

請在這個討論串討論 `dotool`：<https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

這個預設集屬於實驗性功能，因為我無法在自己的硬體上驗證。歡迎提供意見回饋。

請在這個討論串討論 `cec-client`：<https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

有一個可用來控制 macOS 上 VLC 的預設集，使用的是 AppleScript 指令。我目前不知道有哪個工具支援傳送鍵盤或滑鼠事件。

請在這個討論串討論 macOS：<https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

如果你的 Android 裝置內建 SSH 伺服器，你或許可以連線到它並傳送輸入事件。如果你的裝置使用自訂 ROM，例如 Raspberry Pi 上的 KonstaKANG，這種可能性會更高。

我還沒找到讓滑鼠支援正常運作的方法。

請在這個討論串討論 Android：<https://github.com/stefansundin/SSHRemote/discussions/9>

## 智慧音量控制

編輯遙控器時，你可以在選單中找到「智慧」音量控制設定。這可以在 app 中顯示電腦目前的音量，並讓你使用滑桿快速調整音量。你也可以使用裝置的硬體按鈕快速傳送提高／降低音量指令。

讀取目前音量與使用滑桿設定新音量，目前都寫死使用 `pactl`。

包含 `pactl` 的套件通常叫做 `pulseaudio-utils` 或 `libpulse`。

## SSH 金鑰

你可以在 app 設定中匯入或產生 SSH 金鑰。使用 SSH 金鑰連線比使用密碼更安全。

從電腦匯入現有 SSH 金鑰最簡單的方法是掃描 QR Code。你可以使用 `qrencode` 程式產生 QR Code 圖像。執行下列指令即可產生 QR Code：

```shell
# 切換到你的 SSH 金鑰目錄：
cd ~/.ssh

# 在終端機中顯示 QR Code：
qrencode -r id_ed25519 -t ansiutf8

# 或者建立圖片檔：
qrencode -r id_ed25519 -o qr.png

# 4096 位元的 RSA 金鑰對 QR Code 來說太大。你可以用 gzip 勉強塞進去：
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

你可以使用選單中的 `推送公開金鑰` 功能將公開 SSH 金鑰推送到伺服器。這會把選取的 SSH 金鑰附加到 `~/.ssh/authorized_keys` 檔案。這能讓你輕鬆從使用密碼登入改成使用 SSH 金鑰登入。

你可以匯入並使用加密的 SSH 金鑰，但目前無法在 app 內產生這種金鑰。

## 安全性

無法從 app 匯出或擷取 SSH 金鑰的私密部分，也無法擷取已儲存的密碼。這些資料使用 256 位元 AES 加密，而加密金鑰儲存在 Android Keystore 中。加密資料不會包含在 Android 備份內。

這個 app 沒有任何當機回報軟體。沒有遙測。沒有廣告。除了 SSH 連線之外，也不會有任何網路請求。

這個 app 的安全性尚未經過稽核。如果你熟悉 Android 安全性或 SSH 安全性，請查看原始碼，並在這個 GitHub issue 回報你的發現：

<https://github.com/stefansundin/SSHRemote/issues/1>

## 功能請求

歡迎在 GitHub 儲存庫提交功能請求與錯誤回報。請使用英文。也請保持留言理性友善。不尊重他人的留言會被移除，使用者也可能被封鎖而無法再使用該儲存庫。

請先查看既有的 issue 與討論串，確認你的問題是否已經有人提出或回答。

請保持尊重。我是在自己的空閒時間開發這個 app，並免費提供給大家使用。首先，我是為了自己的需求而開發這個 app。

請不要透過電子郵件向我提問。請盡量把討論留在 GitHub，因為這也能幫助其他人！你可以在 GitHub 的討論區提問。

你隨時都可以 fork 這個 app 來實作自己的功能。這是很棒的學習方式。也請考慮貢獻實用的功能。

原始碼依 GNU GPLv3 授權。如果你散布這個 app 的修改版本，你也必須一併提供原始碼。

<https://github.com/stefansundin/SSHRemote>

## 捐款

如果你想表達感謝與支持，歡迎捐款。

<https://stefansundin.github.io/donate/>

如果你已捐款，我會盡力回答你的任何問題，但請以英文提出詢問。

感謝你的支持！
