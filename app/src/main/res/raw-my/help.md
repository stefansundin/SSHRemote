## SSH Remote အကြောင်း

ဤဘာသာပြန်ကို GitHub Copilot (ဤပတ်ဝန်းကျင်တွင် model အမည်ကို မပြနိုင်ပါ) ၏ အကူအညီဖြင့် ပြုလုပ်ထားပြီး ဘာသာပြန်အမှားများ ရှိနိုင်ပါသည်။

SSH Remote သည် SSH ကို အသုံးပြုပြီး ကွန်ပျူတာများကို အဝေးမှ ထိန်းချုပ်နိုင်စေသည့် အခမဲ့နှင့် open source အက်ပ်တစ်ခု ဖြစ်သည်။

လုပ်ဆောင်မည့် အမိန့်များကို သင်အပြည့်အဝ စိတ်ကြိုက်ပြင်ဆင်နိုင်ပြီး၊ အသုံးများသော setup များအတွက် preset များလည်း ပါရှိသည်။

ကျွန်ုပ်သည် Raspberry Pi OS ဖြင့် လည်ပတ်နေသော ကျွန်ုပ်၏ HTPC setup ကို ထိန်းချုပ်ရန် ဤအက်ပ်ကို အသုံးပြုပါသည်။ HTPC ကို ထိန်းချုပ်ခြင်းသည် ဤအက်ပ်က အဓိက optimize လုပ်ထားသော အခြေခံအသုံးပြုမှု ဖြစ်သည်။

ဤအက်ပ်သည် terminal emulator မဟုတ်သော်လည်း အရေးပေါ်အခြေအနေတွင် `apt-get install` ကို run နိုင်စေပါသည်။

## စတင်အသုံးပြုခြင်း

ချိတ်ဆက်ရန် SSH ကီးကို အသုံးပြုလိုပါက အရင်ဆုံး အက်ပ် settings ကို ဖွင့်ပြီး ကီးတစ်ခုကို import လုပ်ပါ သို့မဟုတ် generate လုပ်ပါ။

ညာဘက်အောက်ထောင့်ရှိ `+` ခလုတ်ကို နှိပ်ပြီး host အသစ်တစ်ခု ထည့်ပါ။ ချိတ်ဆက်ရန် အချက်အလက်များကို ဖြည့်ပြီး သိမ်းပါ။

ပထမဆုံး ချိတ်ဆက်သည့်အခါ preset တစ်ခုကို ရွေးရန် မေးပါမည်။ ဤရွေးချယ်မှုက အမျိုးမျိုးသော ကွန်ပျူတာအမျိုးအစားများတွင် အဆင်ပြေစွာ အလုပ်လုပ်စေရန် remote control ခလုတ်များကို စီစဉ်ပေးသည်။ ရရှိနိုင်သော preset များအကြောင်းကို အောက်တွင် ကြည့်ပါ။ ဆန္ဒရှိပါက `No preset` ကို ရွေးပြီး configuration မရှိဘဲလည်း စတင်နိုင်သည်။

သင့် Linux ကွန်ပျူတာသည် X11 သို့မဟုတ် Wayland ကို အသုံးပြုနေသလား မသိပါက terminal တွင် အောက်ပါအမိန့်ကို run ပါ-

```shell
echo $XDG_SESSION_TYPE
```

ဤအမိန့်က `x11` သို့မဟုတ် `wayland` ဟု ပြသသင့်ပါသည်။ ၎င်းကို desktop environment အတွင်း run ရပါမည်။

## Remote Control

ချိတ်ဆက်ပြီးသည့်နောက် command များ ပို့ရန် remote control interface ကို အသုံးပြုနိုင်ပါသည်။ မတူညီသော input method များကို အသုံးပြုရန် tab များကို ပြောင်းပါ။

ခလုတ်တစ်ချက်နှိပ်တိုင်း host ပေါ်တွင် command တစ်ခုကို run လုပ်ပါမည်။ ကီးတစ်ချက်နှိပ်သည့်အရာလောက် ရိုးရှင်းသည့် လုပ်ဆောင်ချက်အတွက်တောင် overhead များစွာ ရှိနေသောကြောင့် ပုံမှန် keyboard နှင့် နှိုင်းယှဉ်လျှင် latency အတော်မြင့်နိုင်သည်။ နောင်လာမည့် version များတွင် ၎င်းကို ပိုကောင်းအောင် ပြုလုပ်ရန် မျှော်လင့်ထားပါသည်။

တည်းဖြတ်မုဒ်သို့ ဝင်ရန် menu ကို အသုံးပြုပါ။ လောလောဆယ် layout သို့မဟုတ် button icon များကို တည်းဖြတ်၍ မရသေးပါ။ နောင် version များတွင် ဤစွမ်းရည်ကို ထည့်သွင်းနိုင်ရန် မျှော်လင့်ထားပါသည်။

## Presets

သင့် desktop environment အတွက် လိုအပ်သော tool ကို install လုပ်ရပါမည်။

ကျွန်ုပ်၏ စမ်းသပ်မှုများအရ `ydotool` သည် စွမ်းဆောင်ရည်အကောင်းဆုံးဖြစ်ပြီး X11 နှင့် Wayland နှစ်ခုလုံးတွင် အလုပ်လုပ်သောကြောင့် ၎င်းကို အကြံပြုပါသည်။

### ydotool

`ydotool` သည် မည်သည့် window manager နှင့်မဆို အလုပ်လုပ်သင့်သော်လည်း နောက်ခံ service တစ်ခု လည်ပတ်နေဖို့ လိုအပ်သည်။ သင့် distribution က systemd user service တစ်ခု ပေးထားပါက အောက်ပါအမိန့်ဖြင့် စတင်ပါ-

```shell
systemctl start --user ydotool
```

login ဝင်ချိန်တွင် service ကို အလိုအလျောက် စတင်ရန် အောက်ပါအမိန့်ကို run ပါ-

```shell
systemctl enable --user ydotool
```

`ydotool` ၏ version သည် လုံလောက်စွာ အသစ်ဖြစ်ကြောင်း သေချာပါစေ။ Ubuntu 26.04 မတိုင်မီ version များတွင် အလွန်ဟောင်းသည့် version များကိုသာ ပေးထားသည်။ ဖြေရှင်းနည်းတစ်ခုအတွက် discussion thread ကို ကြည့်ပါ။

`ydotool` အကြောင်း ဆွေးနွေးရန် discussion thread: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` သည် X11 ဖြင့် လည်ပတ်သော ကွန်ပျူတာများအတွက် ဖြစ်သည်။ X11 သည် သမိုင်းအရ Linux ကွန်ပျူတာများအများစု အသုံးပြုခဲ့သော စနစ်ဖြစ်ပြီး Wayland က ပိုမိုလူကြိုက်များလာနေသည်။

X11 တွင် ထူးခြားချက်တစ်ခုမှာ X server သို့ ဝင်ခွင့် ပေးရန် လိုလာနိုင်ခြင်း ဖြစ်သည်။ `Authorization required` အမှားကို တွေ့ရပါက ဤပြဿနာဖြစ်နိုင်သည်။ ပြဿနာကို ဖြေရှင်းရန် နည်းလမ်းအချို့ ရှိပြီး၊ အောက်တွင် ကျွန်ုပ်အတွက် အလုပ်လုပ်ခဲ့သော နည်းလမ်းနှစ်ခုကို ဖော်ပြထားပါသည်-

`xauth list` တွင် entries မပေါ်ပါက `.Xauthority` file တစ်ခု ဖန်တီးကြည့်ပါ-

```shell
xauth generate :0 . trusted
```

အဲဒါ မအောင်မြင်ပါက `xhost` ဖြင့် ဝင်ခွင့်ပေးကြည့်ပါ-

```shell
xhost +local:$USER
```

စက် boot တိုင်း `xhost` command ကို run ရပါမည်။ ၎င်းကို အလိုအလျောက် လုပ်စေရန် bash script တစ်ခု ဖန်တီးပြီး login ဝင်ချိန် autostart အဖြစ် သတ်မှတ်နိုင်သည်။

`xdotool` အကြောင်း ဆွေးနွေးရန် discussion thread: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` သည် Wayland အတွက် `xdotool` ကဲ့သို့သော tool ဖြစ်သည်။

ပုံမှန်အားဖြင့် mouse control ကို မထောက်ပံ့သော်လည်း ကျွန်ုပ်က mouse support ထပ်ထည့်ထားသော version ပြင်ထားတစ်ခုကို ဖန်တီးထားပါသည်။ Mouse support လိုပါက ၎င်းကို install လုပ်ပါ- <https://github.com/stefansundin/wtype>

`Compositor does not support the virtual keyboard protocol` အမှားပေါ်လာပါက `ydotool` ကဲ့သို့သော အခြား tool တစ်ခုကို စမ်းကြည့်ရန် အကြံပြုပါသည်။

`wtype` အကြောင်း ဆွေးနွေးရန် discussion thread: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` သည် `ydotool` ကဲ့သို့ပင် မည်သည့် window manager နှင့်မဆို အလုပ်လုပ်သင့်ပါသည်။ ကျွန်ုပ်၏ ကန့်သတ်ထားသော စမ်းသပ်မှုများအရ ၎င်းသည် `ydotool` ထက် များစွာ နှေးကွေးပါသည်။

`dotool` အကြောင်း ဆွေးနွေးရန် discussion thread: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

ဤ preset သည် စမ်းသပ်ဆဲအဆင့်ဖြစ်ပြီး ကျွန်ုပ်ကိုယ်တိုင် hardware ပေါ်တွင် အတည်မပြုနိုင်သေးပါ။ အကြံပြုချက်များကို ကြိုဆိုပါသည်။

`cec-client` အကြောင်း ဆွေးနွေးရန် discussion thread: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

macOS တွင် AppleScript command များကို အသုံးပြုပြီး VLC ကို ထိန်းချုပ်ရန် preset တစ်ခု ပါရှိသည်။ Keyboard သို့မဟုတ် mouse event များ ပို့ပေးနိုင်သော tool ကို ကျွန်ုပ် မသိပါ။

macOS အကြောင်း ဆွေးနွေးရန် discussion thread: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

သင့် Android device တွင် SSH server ပါလာပါက ၎င်းသို့ ချိတ်ဆက်ပြီး input event များ ပို့နိုင်နိုင်ပါသည်။ Raspberry Pi ပေါ်ရှိ KonstaKANG ကဲ့သို့ custom ROM ကို အသုံးပြုနေပါက ထိုသို့ဖြစ်နိုင်ချေ ပိုများသည်။

Mouse support ကို မည်သို့ အလုပ်လုပ်စေရမည်ဆိုတာကို ကျွန်ုပ် မရှာဖွေနိုင်သေးပါ။

Android အကြောင်း ဆွေးနွေးရန် discussion thread: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Smart volume control

remote control ကို တည်းဖြတ်နေစဉ် menu တွင် “smart” volume control setting များကို တွေ့နိုင်ပါသည်။ ၎င်းတို့က app အတွင်း ကွန်ပျူတာ၏ လက်ရှိ volume ကို ပြသနိုင်ပြီး slider ဖြင့် volume ကို လွယ်ကူစွာ သတ်မှတ်နိုင်စေသည်။ ထို့အပြင် device ၏ hardware button များကို အသုံးပြုပြီး volume up/down command များကို လျင်မြန်စွာ ပို့နိုင်သည်။

လက်ရှိ volume ကို ဖတ်ခြင်းနှင့် slider ဖြင့် volume အသစ် သတ်မှတ်ခြင်းတို့ကို လောလောဆယ် `pactl` ကို အသုံးပြုရန် hard-code လုပ်ထားပါသည်။

`pactl` ပါဝင်သော package ကို များသောအားဖြင့် `pulseaudio-utils` သို့မဟုတ် `libpulse` ဟု ခေါ်သည်။

## SSH keys

အက်ပ် settings ထဲတွင် SSH key များကို import လုပ်နိုင်သလို generate လုပ်နိုင်ပါသည်။ SSH key ဖြင့် ချိတ်ဆက်ခြင်းသည် password အသုံးပြုခြင်းထက် ပိုလုံခြုံပါသည်။

ကွန်ပျူတာတစ်လုံးမှ ရှိပြီးသား SSH key ကို import လုပ်ရန် အလွယ်ဆုံးနည်းမှာ QR code တစ်ခုကို scan လုပ်ခြင်းဖြစ်သည်။ QR code image ဖန်တီးရန် `qrencode` program ကို အသုံးပြုနိုင်ပါသည်။ အောက်ပါအတိုင်း command မျိုးကို run ၍ QR code ဖန်တီးနိုင်ပါသည်-

```shell
# သင့်ရဲ့ SSH key တွေဆီ သွားပါ-
cd ~/.ssh

# terminal မှာ QR code ကိုပြပါ-
qrencode -r id_ed25519 -t ansiutf8

# တနည်းအားဖြင့် ရုပ်ပုံဖိုင်တစ်ခု ဖန်တီးပါ-
qrencode -r id_ed25519 -o qr.png

# 4096-bit RSA key များသည် QR ကုဒ်အတွက် ကြီးလွန်းပါသည်။ gzip ကို အသုံးပြု၍ တစ်ခုကို အနည်းငယ်သာ ထည့်နိုင်သည်-
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

menu ထဲရှိ `အများသုံးကီး ပို့မည်` feature ကို အသုံးပြုပြီး public SSH key များကို server တစ်ခုသို့ ပို့နိုင်ပါသည်။ ၎င်းက ရွေးထားသော SSH key ကို `~/.ssh/authorized_keys` file တွင် append လုပ်ပေးသည်။ ထို့ကြောင့် password ဖြင့် login ဝင်ခြင်းမှ SSH key ဖြင့် login ဝင်ခြင်းသို့ အလွယ်တကူ ပြောင်းနိုင်သည်။

encrypted SSH key များကို import လုပ်ပြီး အသုံးပြုနိုင်သော်လည်း လောလောဆယ် အက်ပ်အတွင်း ၎င်းတို့ကို generate လုပ်၍ မရသေးပါ။

## လုံခြုံရေး

အက်ပ်ထဲမှ SSH key များ၏ private အပိုင်း သို့မဟုတ် သိမ်းထားသော password များကို export လုပ်ခြင်း သို့မဟုတ် extract လုပ်ခြင်း မပြုနိုင်ပါ။ ဤဒေတာကို 256-bit AES ဖြင့် စာဝှက်ထားပြီး encryption key ကို Android Keystore တွင် သိမ်းထားပါသည်။ စာဝှက်ထားသော ဒေတာများကို Android backup များမှ ချန်လှပ်ထားပါသည်။

ဤအက်ပ်တွင် crash reporting software မရှိပါ။ telemetry မရှိပါ။ ကြော်ငြာမရှိပါ။ SSH connection မှလွဲ၍ network request များ မရှိပါ။

ဤအက်ပ်၏ လုံခြုံရေးကို audit မလုပ်ရသေးပါ။ သင်သည် Android security သို့မဟုတ် SSH security တွင် အတွေ့အကြုံရှိပါက source code ကို ကြည့်ရှုပြီး သင်တွေ့ရှိသမျှကို အောက်ပါ GitHub issue တွင် တင်ပြပေးပါ-

<https://github.com/stefansundin/SSHRemote/issues/1>

## Feature requests

GitHub repository တွင် feature request များနှင့် bug report များကို လွတ်လပ်စွာ တင်နိုင်ပါသည်။ ကျေးဇူးပြု၍ English ဖြင့် ရေးပါ။ မှတ်ချက်များကို ယဉ်ကျေးစွာ ထိန်းသိမ်းပါ။ မလေးစားသော မှတ်ချက်များကို ဖျက်ပစ်မည်ဖြစ်ပြီး repository မှ block လုပ်ခံရနိုင်ပါသည်။

သင့်မေးခွန်းကို ယခင်က မေးထားပြီး သို့မဟုတ် ဖြေထားပြီးဖြစ်မဖြစ် သိရန် ရှိပြီးသား issue များနှင့် discussion thread များကို အရင်ကြည့်ပါ။

ကျေးဇူးပြု၍ လေးစားမှုရှိပါ။ ဤအက်ပ်ကို ကျွန်ုပ်၏ အားလပ်ချိန်တွင် တည်ဆောက်ခဲ့ပြီး အခမဲ့ ပေးအပ်နေခြင်းဖြစ်ပါသည်။ ဤအက်ပ်ကို ကျွန်ုပ်ကိုယ်တိုင် အသုံးပြုရန် အဓိက ရည်ရွယ်ပြီး တည်ဆောက်နေခြင်းဖြစ်ပါသည်။

ကျွန်ုပ်ထံ အီးမေးလ်ဖြင့် မေးခွန်းများ မပို့ပါနှင့်။ GitHub ပေါ်တွင် စကားဝိုင်းများကို ဆက်လက်ထားရှိရန် ကြိုးစားပေးပါ၊ အဘယ်ကြောင့်ဆိုသော် ၎င်းက အခြားသူများကိုလည်း အထောက်အကူဖြစ်စေပါသည်။ GitHub ရှိ discussion section တွင် မေးခွန်းများ မေးနိုင်ပါသည်။

သင့်ကိုယ်ပိုင် feature များ ထည့်သွင်းရန် app ကို fork လုပ်ရန် အမြဲကြိုဆိုပါသည်။ ၎င်းသည် လေ့လာရန် အလွန်ကောင်းသော နည်းလမ်းတစ်ခုဖြစ်သည်။ အသုံးဝင်သော feature များကို ပြန်လည်ကူညီပံ့ပိုးရန် စဉ်းစားပေးပါ။

source code ကို GNU GPLv3 လိုင်စင်ဖြင့် လိုင်စင်ပေးထားပါသည်။ ဤအက်ပ်၏ ပြင်ဆင်ထားသော version များကို သင်ဖြန့်ချိပါက source code ကိုလည်း ရရှိနိုင်အောင် ပြုလုပ်ရပါမည်။

<https://github.com/stefansundin/SSHRemote>

## Donations

ကျေးဇူးတင်ကြောင်းနှင့် လေးစားတန်ဖိုးထားကြောင်း ပြသလိုပါက လှူဒါန်းမှုများကို လက်ခံပါသည်။

<https://stefansundin.github.io/donate/>

လှူဒါန်းထားပြီးပါက သင့်မေးခွန်းများကို ကျွန်ုပ်တတ်နိုင်သမျှ ဖြေပေးရန် ကြိုးစားပါမည်။ မေးမြန်းမှုများကို English ဖြင့် ရေးပေးပါ။

သင့်အားပေးမှုအတွက် ကျေးဇူးတင်ပါသည်!
