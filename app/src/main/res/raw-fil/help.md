## Tungkol sa SSH Remote

Ang saling ito ay ginawa ng AI gamit ang GitHub Copilot (GPT-5.3-Codex). Maaaring may mga pagkakamali o di-ganap na salin.

Ang SSH Remote ay isang libre at open source na app na nagbibigay-daan sa iyo na kontrolin ang mga computer nang malayuan gamit ang SSH.

Maaari mong ganap na i-customize ang mga utos na pinapatakbo, at may mga preset para sa mga karaniwang setup.

Ginagamit ko ang app na ito para kontrolin ang aking HTPC setup, na nagpapatakbo ng Raspberry Pi OS. Ang pagkontrol sa HTPC ang pangunahing sitwasyon na in-optimize para sa app na ito.

Ang app na ito ay hindi terminal emulator, pero papayagan ka nitong magpatakbo ng `apt-get install` kapag kailangan.

## Pagsisimula

Kung gusto mong gumamit ng SSH key para kumonekta, buksan muna ang settings ng app at mag-import o gumawa ng key.

Magdagdag ng bagong host sa pamamagitan ng pag-tap sa `+` button sa kanang ibaba. Ilagay ang mga detalye ng koneksyon at i-save.

Sa unang beses na kumonekta ka, hihingan ka na pumili ng preset. Kino-configure ng pagpiling ito ang mga button ng remote control para gumana nang maayos sa iba’t ibang uri ng computer. Tingnan sa ibaba ang paglalarawan ng mga available na preset. Kung gusto mo, maaari kang magsimula nang walang configuration sa pamamagitan ng pagpili sa `Walang preset`.

Kung hindi mo alam kung X11 o Wayland ang pinapatakbo ng iyong Linux computer, patakbuhin ito sa isang terminal:

```shell
echo $XDG_SESSION_TYPE
```

Dapat itong maglabas ng `x11` o `wayland`. Kailangan mo itong patakbuhin sa loob ng desktop environment.

## Remote Control

Kapag nakakonekta na, maaari mong gamitin ang interface ng remote control para magpadala ng mga utos. Lumipat ng mga tab para ma-access ang iba’t ibang paraan ng pag-input.

Bawat pagpindot ng button ay magpapatakbo ng isang utos sa host. Malaki ang overhead nito para sa isang bagay na kasing simple ng pagpindot ng key, at maaari kang makaranas ng medyo mataas na latency kumpara sa karaniwang keyboard. Umaasa akong mapabuti ito sa mga susunod na bersyon.

Gamitin ang menu para pumasok sa edit mode. Sa kasalukuyan, hindi pa posible na i-edit ang layout o mga icon ng button. Umaasa akong magagawa ito sa hinaharap na bersyon.

## Mga Preset

Kailangan mong i-install ang tool na kailangan para sa iyong desktop environment.

Inirerekomenda ko ang `ydotool` dahil sa aking pagsusuri ito ang may pinakamahusay na performance, at gumagana ito sa parehong X11 at Wayland.

### ydotool

Dapat gumana ang `ydotool` sa anumang window manager, pero kailangan mong magpatakbo ng background service. Kung may systemd user service ang iyong distribution, simulan ito sa pamamagitan ng pagpapatakbo ng:

```shell
systemctl start --user ydotool
```

Para awtomatikong simulan ang service sa pag-login, patakbuhin ang:

```shell
systemctl enable --user ydotool
```

Pakisigurong sapat na bago ang bersyon ng `ydotool` na ini-install mo. Ang mga bersyon ng Ubuntu bago ang 26.04 ay may mga bersyong masyadong luma. Tingnan ang discussion thread para sa workaround.

Mangyaring talakayin ang `ydotool` sa discussion thread na ito: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

Ang `xdotool` ay para sa mga computer na nagpapatakbo ng X11. Ang X11 ang makasaysayang ginagamit ng karamihan sa mga Linux computer, bagaman mas nagiging popular ang Wayland.

Isang kakaibang bagay sa X11 ay maaaring kailanganin mong payagan ang access sa X server. Ito ang problema kung nakakakuha ka ng mga error na "Authorization required". May ilang paraan para ayusin ito; narito ang dalawang opsyon na gumana para sa akin:

Kung walang ipinapakitang entry ang `xauth list`, subukang gumawa ng `.Xauthority` file:

```shell
xauth generate :0 . trusted
```

Kung hindi iyon gumana, subukang magbigay ng access gamit ang `xhost`:

```shell
xhost +local:$USER
```

Kailangan mong patakbuhin ang utos na `xhost` pagkatapos ng bawat boot. Maaari mo itong i-automate sa pamamagitan ng paggawa ng bash script at pag-configure nito para awtomatikong magsimula sa pag-login.

Mangyaring talakayin ang `xdotool` sa discussion thread na ito: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

Ang `wtype` ay parang `xdotool`, pero para sa Wayland.

Karaniwan, hindi nito sinusuportahan ang mouse control, pero gumawa ako ng binagong bersyon na nagdaragdag ng mouse support. Pakii-install ito kung kailangan mo ng mouse support: <https://github.com/stefansundin/wtype>

Kung makuha mo ang error na `Compositor does not support the virtual keyboard protocol`, iminumungkahi kong sumubok ka ng ibang tool, gaya ng `ydotool`.

Mangyaring talakayin ang `wtype` sa discussion thread na ito: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

Dapat gumana ang `dotool` sa anumang window manager, katulad ng `ydotool`. Sa limitado kong pagsusuri, mas mabagal ito kaysa sa `ydotool`.

Mangyaring talakayin ang `dotool` sa discussion thread na ito: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Eksperimental ang preset na ito dahil hindi ko pa ito nabe-verify sa sarili kong hardware. Malugod na tinatanggap ang feedback.

Mangyaring talakayin ang `cec-client` sa discussion thread na ito: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

May preset para sa pagkontrol sa VLC sa macOS, gamit ang mga utos ng AppleScript. Wala akong alam na tool na sumusuporta sa pagpapadala ng mga keyboard o mouse event.

Mangyaring talakayin ang macOS sa discussion thread na ito: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Kung may SSH server ang iyong Android device, baka makakonekta ka rito at makakapagpadala ng mga input event. Mas malamang ito kung custom ROM ang pinapatakbo ng iyong device, tulad ng KonstaKANG sa Raspberry Pi.

Hindi ko pa nalalaman kung paano paganahin ang mouse support.

Mangyaring talakayin ang Android sa discussion thread na ito: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Smart volume control

Kapag ini-edit ang remote control, makikita mo ang mga setting ng "smart" volume control sa menu. Maaari nitong ipakita ang kasalukuyang volume ng computer sa app at hayaan kang mabilis na itakda ang volume gamit ang slider. Maaari mo ring gamitin ang mga hardware button ng iyong device para mabilis na magpadala ng mga utos na volume up/down.

Ang pagbabasa ng kasalukuyang volume at pagtatakda ng bagong volume gamit ang slider ay kasalukuyang naka-hard-code para gumamit ng `pactl`.

Ang package na naglalaman ng `pactl` ay karaniwang tinatawag na `pulseaudio-utils` o `libpulse`.

## SSH keys

Maaari kang mag-import o gumawa ng mga SSH key sa settings ng app. Mas ligtas ang pagkonekta gamit ang SSH key kaysa sa paggamit ng mga password.

Ang pinakamadaling paraan para mag-import ng umiiral nang SSH key mula sa computer ay ang pag-scan ng QR code. Maaari mong gamitin ang programang `qrencode` para buuin ang QR code image. Magpatakbo ng utos na tulad ng sumusunod para buuin ang QR code:

```shell
# Pumunta sa iyong mga SSH key:
cd ~/.ssh

# Ipakita ang QR code sa terminal:
qrencode -r id_ed25519 -t ansiutf8

# Bilang alternatibo, gumawa ng image file:
qrencode -r id_ed25519 -o qr.png

# Masyadong malaki ang 4096-bit RSA key para sa QR code. Maaari kang gumamit ng gzip para halos magkasya ito:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Maaari mong ipadala ang mga public SSH key sa server gamit ang feature na `Ipadala ang public key` sa menu. Idadagdag nito ang napiling SSH key sa `~/.ssh/authorized_keys` file. Dahil dito, madali kang makakalipat mula sa pag-log in gamit ang password papunta sa pag-log in gamit ang SSH key.

Maaari kang mag-import at gumamit ng mga naka-encrypt na SSH key, pero hindi ka pa makakagawa ng mga ito sa app sa kasalukuyan.

## Seguridad

Hindi posible na i-export o kunin ang pribadong bahagi ng mga SSH key, o mga naka-store na password, mula sa app. Ang data na ito ay naka-encrypt gamit ang 256-bit AES, at ang encryption key ay naka-store sa Android Keystore. Hindi isinasama ang naka-encrypt na data sa Android backups.

Walang crash reporting software sa app na ito. Walang telemetry. Walang ads. Walang network request maliban sa SSH connection.

Hindi pa naa-audit ang seguridad ng app na ito. Kung may karanasan ka sa Android security o SSH security, pakitingnan ang source code at iulat ang iyong mga natuklasan sa GitHub issue na ito:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Mga kahilingan sa feature

Malaya kang magsumite ng mga kahilingan sa feature at bug report sa GitHub repository. Mangyaring gumamit ng Ingles. Mangyaring panatilihing magalang ang iyong mga komento. Aalisin ang mga bastos na komento at maaaring i-block ang mga user mula sa repository.

Mangyaring tingnan muna ang mga umiiral nang issue at discussion thread para makita kung naitanong o nasagot na ang iyong tanong.

Mangyaring maging magalang. Ginawa ko ang app na ito sa aking libreng oras at ibinibigay ko ito nang libre. Binubuo ko ang app na ito para sa sarili kong gamit, una sa lahat.

Mangyaring huwag akong i-email ng mga tanong. Subukan nating panatilihin ang mga usapan sa GitHub, dahil nakakatulong din iyon sa ibang tao! Maaari kang magtanong sa seksyon ng discussion sa GitHub.

Palagi kang malugod na tinatanggap na i-fork ang app para ipatupad ang sarili mong mga feature. Magandang paraan iyon para matuto. Mangyaring isaalang-alang ang pag-ambag ng mga kapaki-pakinabang na feature.

Ang source code ay lisensyado sa ilalim ng GNU GPLv3. Kung magdi-distribute ka ng mga binagong bersyon ng app na ito, kailangan mo ring gawing available ang source code.

<https://github.com/stefansundin/SSHRemote>

## Mga Donasyon

Kung nais mong ipakita ang iyong pasasalamat at pagpapahalaga, tumatanggap ng mga donasyon.

<https://stefansundin.github.io/donate/>

Kung nakapag-donate ka na, gagawin ko ang aking makakaya para sagutin ang anumang tanong na maaaring mayroon ka. Mangyaring isulat sa Ingles ang anumang katanungan.

Salamat sa iyong suporta!
