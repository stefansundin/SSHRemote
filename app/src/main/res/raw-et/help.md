## SSH Remote'ist

See eestikeelne tõlge on loodud tehisintellekti abil GitHub Copiloti kaudu ja võib sisaldada tõlkevigu.

SSH Remote on tasuta ja avatud lähtekoodiga rakendus, mis võimaldab sul SSH kaudu arvuteid kaugelt juhtida.

Sa saad täielikult kohandada käivitatavaid käske ning olemas on eelseadistused levinud seadistuste jaoks.

Kasutan seda rakendust oma HTPC seadistuse juhtimiseks, mis töötab Raspberry Pi OS-iga. HTPC juhtimine on peamine kasutusjuht, mille jaoks rakendus on optimeeritud.

See rakendus ei ole terminaliemulaator, kuid hädaolukorras võimaldab see sul käivitada `apt-get install`.

## Alustamine

Kui soovid ühenduse loomiseks kasutada SSH võtit, ava esmalt rakenduse seaded ning impordi või genereeri võti.

Lisa uus host, puudutades all paremas nurgas nuppu `+`. Sisesta ühenduse andmed ja salvesta.

Esimesel ühendamisel palutakse sul valida eelseadistus. See valik seadistab puldi nupud nii, et need töötaksid hästi eri tüüpi arvutitega. Allpool on kirjeldatud saadaolevaid eelseadistusi. Soovi korral võid alustada ka ilma seadistuseta, valides `Eelseadistus puudub`.

Kui sa ei tea, kas sinu Linuxi arvuti kasutab X11 või Waylandi, käivita terminalis järgmine käsk:

```shell
echo $XDG_SESSION_TYPE
```

Väljund peaks olema `x11` või `wayland`. See käsk tuleb käivitada töölauakeskkonna sees.

## Pult

Pärast ühenduse loomist saad käskude saatmiseks kasutada puldi kasutajaliidest. Eri sisestusviiside kasutamiseks vaheta vahekaarte.

Iga nupuvajutus käivitab hostis käsu. Nii lihtsa toimingu nagu klahvivajutuse jaoks on see üsna suur lisakoormus ning latentsus võib olla märksa suurem kui tavalise klaviatuuri puhul. Loodan seda tulevastes versioonides parandada.

Kasuta menüüd muutmisrežiimi sisenemiseks. Praegu ei ole võimalik paigutust ega nuppude ikoone muuta. Loodan selle võimaluse lisada tulevastes versioonides.

## Eelseadistused

Sul tuleb paigaldada sinu töölauakeskkonna jaoks vajalik tööriist.

Soovitan `ydotool`-i, sest minu testides pakub see parimat jõudlust ja töötab nii X11 kui ka Waylandi puhul.

### ydotool

`ydotool` peaks töötama iga aknahalduriga, kuid selleks peab taustal töötama teenus. Kui sinu distributsioon pakub systemd kasutajateenust, käivita see nii:

```shell
systemctl start --user ydotool
```

Teenuse automaatseks käivitamiseks sisselogimisel kasuta:

```shell
systemctl enable --user ydotool
```

Veendu, et paigaldad piisavalt uue `ydotool`-i versiooni. Ubuntu versioonid enne 26.04 sisaldavad liiga vana versiooni. Lahenduse leidmiseks vaata arutelulõime.

Palun arutle `ydotool`-i teemal selles arutelulõimes: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` on mõeldud X11 kasutavatele arvutitele. X11 on see, mida enamik Linuxi arvuteid on ajalooliselt kasutanud, kuigi Wayland muutub üha populaarsemaks.

Üks X11 eripära on see, et sul võib olla vaja lubada ligipääs X-serverile. See on probleem siis, kui näed vigu „Authorization required“. Probleemi lahendamiseks on mitu võimalust; siin on kaks varianti, mis on minu jaoks töötanud:

Kui `xauth list` ei näita ühtegi kirjet, proovi genereerida `.Xauthority` fail:

```shell
xauth generate :0 . trusted
```

Kui see ei aidanud, proovi anda ligipääs `xhost`-iga:

```shell
xhost +local:$USER
```

Käsk `xhost` tuleb käivitada pärast iga algkäivitust. Sa saad selle automatiseerida, luues bash-skripti ja seadistades selle sisselogimisel automaatselt käivituma.

Palun arutle `xdotool`-i teemal selles arutelulõimes: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` on nagu `xdotool`, kuid Waylandi jaoks.

Tavaliselt ei toeta see hiire juhtimist, kuid olen loonud muudetud versiooni, mis lisab hiiretoe. Palun paigalda see, kui vajad hiiretuge: <https://github.com/stefansundin/wtype>

Kui saad vea `Compositor does not support the virtual keyboard protocol`, soovitan proovida mõnda teist tööriista, näiteks `ydotool`-i.

Palun arutle `wtype`-i teemal selles arutelulõimes: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` peaks sarnaselt `ydotool`-ile töötama iga aknahalduriga. Minu piiratud testides oli see palju aeglasem kui `ydotool`.

Palun arutle `dotool`-i teemal selles arutelulõimes: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

See eelseadistus on eksperimentaalne, sest ma ei ole saanud seda oma riistvaral kontrollida. Tagasiside on teretulnud.

Palun arutle `cec-client`-i teemal selles arutelulõimes: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

macOS-i jaoks on olemas eelseadistus VLC juhtimiseks AppleScripti käskudega. Ma ei tea tööriista, mis toetaks klaviatuuri- või hiiresündmuste saatmist.

Palun arutle macOS-i teemal selles arutelulõimes: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Kui sinu Android-seadmega tuleb kaasa SSH-server, võib sul olla võimalik sellega ühenduda ja sisendsündmusi saata. See on tõenäolisem siis, kui sinu seade kasutab kohandatud ROM-i, näiteks Raspberry Pi jaoks mõeldud KonstaKANG-i.

Ma ei ole veel välja selgitanud, kuidas hiiretoe toimima saada.

Palun arutle Androidi teemal selles arutelulõimes: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Nutikas helitugevuse juhtimine

Puldi muutmisel leiad menüüst „nutikad” helitugevuse seaded. Need võimaldavad rakenduses kuvada arvuti praegust helitugevust ning selle liuguri abil kiiresti määrata. Samuti saad seadme riistvaranuppe kasutada helitugevuse suurendamise ja vähendamise käskude kiireks saatmiseks.

Praeguse helitugevuse lugemine ja uue helitugevuse määramine liuguriga on praegu jäigalt seotud `pactl`-i kasutamisega.

Pakett, mis sisaldab `pactl`-i, kannab tavaliselt nime `pulseaudio-utils` või `libpulse`.

## SSH võtmed

Rakenduse seadetes saad SSH võtmeid importida või genereerida. SSH võtmega ühenduse loomine on turvalisem kui paroolide kasutamine.

Lihtsaim viis olemasoleva SSH võtme importimiseks arvutist on skannida QR-kood. QR-koodi pildi loomiseks saad kasutada programmi `qrencode`. QR-koodi genereerimiseks käivita näiteks järgmine käsk:

```shell
# Liigu oma SSH võtmete kataloogi:
cd ~/.ssh

# Kuva QR-kood terminalis:
qrencode -r id_ed25519 -t ansiutf8

# Teise võimalusena loo pildifail:
qrencode -r id_ed25519 -o qr.png

# 4096-bitised RSA võtmed on QR-koodi jaoks liiga suured. gzip aitab ühe napilt ära mahutada:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Saad avalikke SSH võtmeid serverisse edastada menüü funktsiooniga `Edasta avalik võti`. See lisab valitud SSH võtme faili `~/.ssh/authorized_keys`. Nii saad hõlpsasti üle minna parooliga sisselogimiselt SSH võtmega sisselogimisele.

Saad importida ja kasutada krüptitud SSH võtmeid, kuid praegu ei saa neid rakenduses genereerida.

## Turvalisus

Rakendusest ei ole võimalik eksportida ega välja võtta SSH võtmete privaatosa ega salvestatud paroole. Need andmed on krüptitud 256-bitise AES-iga ning krüptovõtit hoitakse Android Keystore'is. Krüptitud andmed jäetakse Androidi varukoopiatest välja.

Selles rakenduses ei ole krahhiraportite tarkvara. Ei ole telemeetriat. Ei ole reklaame. Võrgupäringuid ei tehta peale SSH ühenduse.

Selle rakenduse turvalisust ei ole auditeeritud. Kui sul on Androidi turvalisuse või SSH turvalisuse kogemust, palun vaata lähtekood üle ja anna oma tähelepanekutest teada selles GitHubi probleemikirjes:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Funktsioonisoovid

Esita julgelt funktsioonisoove ja veateateid GitHubi hoidlas. Palun kasuta inglise keelt. Palun jää viisakaks. Lugupidamatud kommentaarid eemaldatakse ja kasutajad võidakse hoidlast blokeerida.

Palun vaata läbi olemasolevad probleemikirjed ja arutelulõimed, et näha, kas sinu küsimust on juba küsitud või sellele vastatud.

Palun ole lugupidav. Ehitasin selle rakenduse oma vabast ajast ja annan selle tasuta ära. Eelkõige ehitan seda rakendust enda tarbeks.

Palun ära saada mulle küsimusi e-postiga. Püüa hoida arutelud GitHubis, sest see aitab ka teisi inimesi! Küsimusi saad esitada GitHubi arutelude jaotises.

Sul on alati võimalik rakendusest oma haru luua, et enda funktsioone lisada. See on suurepärane viis õppimiseks. Palun kaalu kasulike funktsioonide panustamist.

Lähtekood on litsentseeritud GNU GPLv3 all. Kui levitad selle rakenduse muudetud versioone, pead tegema kättesaadavaks ka lähtekoodi.

<https://github.com/stefansundin/SSHRemote>

## Annetused

Kui soovid väljendada oma tänu ja tunnustust, on annetused teretulnud.

<https://stefansundin.github.io/donate/>

Kui oled annetanud, püüan anda endast parima, et vastata sinu küsimustele. Palun kirjuta kõik päringud inglise keeles.

Aitäh toetuse eest!
