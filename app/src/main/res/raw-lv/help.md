## Par SSH Remote

Šo tulkojumu izveidoja AI, izmantojot GitHub Copilot (GPT-5.3-Codex). Iespējamas tulkojuma kļūdas.

SSH Remote ir bezmaksas atvērtā koda lietotne, kas ļauj attālināti vadīt datorus, izmantojot SSH.

Jūs varat pilnībā pielāgot izpildāmās komandas, un biežāk sastopamiem scenārijiem ir pieejami priekšiestatījumi.

Es izmantoju šo lietotni, lai vadītu savu HTPC sistēmu, kas darbojas ar Raspberry Pi OS. HTPC vadība ir galvenais scenārijs, kam šī lietotne ir optimizēta.

Šī lietotne nav termināļa emulators, taču ārkārtas gadījumā tā ļaus palaist `apt-get install`.

## Darba sākšana

Ja vēlaties savienoties, izmantojot SSH atslēgu, vispirms atveriet lietotnes iestatījumus un importējiet vai ģenerējiet atslēgu.

Pievienojiet jaunu hostu, pieskaroties `+` pogai apakšējā labajā stūrī. Ievadiet savienojuma informāciju un saglabājiet.

Pirmajā savienojuma reizē jums tiks lūgts izvēlēties priekšiestatījumu. Šī izvēle konfigurē tālvadības pogas, lai tās labi darbotos ar dažādu tipu datoriem. Tālāk ir aprakstīti pieejamie priekšiestatījumi. Ja vēlaties, varat sākt arī bez konfigurācijas, izvēloties `Bez priekšiestatījuma`.

Ja nezināt, vai jūsu Linux dators izmanto X11 vai Wayland, izpildiet terminālī šo komandu:

```shell
echo $XDG_SESSION_TYPE
```

Tai vajadzētu izvadīt `x11` vai `wayland`. Šī komanda ir jāizpilda no darbvirsmas vides sesijas.

## Tālvadība

Kad savienojums ir izveidots, varat izmantot tālvadības saskarni komandu nosūtīšanai. Pārslēdziet cilnes, lai piekļūtu dažādām ievades metodēm.

Katra pogas nospiešana izpildīs komandu uz hosta. Tas rada diezgan lielu pieskaitāmo slodzi tik vienkāršai darbībai kā taustiņa nospiešana, tāpēc salīdzinājumā ar parastu tastatūru var būt jūtama diezgan liela aizture. Es ceru to uzlabot nākamajās versijās.

Izmantojiet izvēlni, lai ieslēgtu rediģēšanas režīmu. Pašlaik nav iespējams rediģēt izkārtojumu vai pogu ikonas. Es ceru to padarīt iespējamu nākotnē.

## Priekšiestatījumi

Jums būs jāinstalē rīks, kas nepieciešams jūsu darbvirsmas videi.

Es iesaku `ydotool`, jo manos testos tam bija vislabākā veiktspēja, un tas darbojas gan ar X11, gan Wayland.

### ydotool

`ydotool` vajadzētu darboties ar jebkuru logu pārvaldnieku, taču tam ir nepieciešams fona serviss. Ja jūsu distribūcija nodrošina systemd lietotāja servisu, palaidiet to ar šādu komandu:

```shell
systemctl start --user ydotool
```

Lai serviss tiktu automātiski palaists pēc pieteikšanās, izpildiet:

```shell
systemctl enable --user ydotool
```

Lūdzu, pārliecinieties, ka instalējat pietiekami jaunu `ydotool` versiju. Ubuntu versijās pirms 26.04 pieejamās versijas ir pārāk vecas. Risinājumu skatiet diskusijas pavedienā.

Lūdzu, apspriediet `ydotool` šajā diskusijas pavedienā: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` ir paredzēts datoriem, kas izmanto X11. X11 ir sistēma, ko vēsturiski izmantoja lielākā daļa Linux datoru, lai gan Wayland kļūst arvien populārāks.

Viena X11 īpatnība ir tāda, ka jums, iespējams, būs jāatļauj piekļuve X serverim. Tā ir problēma, ja saņemat kļūdas ziņojumu `Authorization required`. Ir vairāki veidi, kā to novērst; šeit ir divi varianti, kas man ir darbojušies:

Ja `xauth list` nerāda nevienu ierakstu, mēģiniet ģenerēt `.Xauthority` failu:

```shell
xauth generate :0 . trusted
```

Ja tas nepalīdzēja, tad mēģiniet piešķirt piekļuvi, izmantojot `xhost`:

```shell
xhost +local:$USER
```

Komanda `xhost` būs jāpalaiž pēc katras sāknēšanas. To var automatizēt, izveidojot bash skriptu un iestatot tā automātisku palaišanu pēc pieteikšanās.

Lūdzu, apspriediet `xdotool` šajā diskusijas pavedienā: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` ir līdzīgs `xdotool`, bet paredzēts Wayland.

Parasti tas neatbalsta peles vadību, bet esmu izveidojis modificētu versiju, kas pievieno peles atbalstu. Lūdzu, instalējiet to, ja jums ir vajadzīgs peles atbalsts: <https://github.com/stefansundin/wtype>

Ja saņemat kļūdu `Compositor does not support the virtual keyboard protocol`, tad iesaku izmēģināt citu rīku, piemēram, `ydotool`.

Lūdzu, apspriediet `wtype` šajā diskusijas pavedienā: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` vajadzētu darboties ar jebkuru logu pārvaldnieku, līdzīgi kā `ydotool`. Manos ierobežotajos testos tas bija ievērojami lēnāks par `ydotool`.

Lūdzu, apspriediet `dotool` šajā diskusijas pavedienā: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Šis priekšiestatījums ir eksperimentāls, jo man nav bijis iespējas to pārbaudīt uz savas aparatūras. Atsauksmes ir gaidītas.

Lūdzu, apspriediet `cec-client` šajā diskusijas pavedienā: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Ir pieejams priekšiestatījums VLC vadībai uz macOS, izmantojot AppleScript komandas. Man nav zināms rīks, kas atbalstītu tastatūras vai peles notikumu nosūtīšanu.

Lūdzu, apspriediet macOS šajā diskusijas pavedienā: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Ja jūsu Android ierīcē ir SSH serveris, iespējams, varēsiet tai pieslēgties un nosūtīt ievades notikumus. Tas ir ticamāk, ja ierīcē darbojas pielāgota ROM versija, piemēram, KonstaKANG uz Raspberry Pi.

Man vēl nav izdevies panākt, lai peles atbalsts darbotos.

Lūdzu, apspriediet Android šajā diskusijas pavedienā: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Viedā skaļuma vadība

Rediģējot tālvadību, izvēlnē varat atrast “viedos” skaļuma vadības iestatījumus. Tie ļauj lietotnē parādīt datora pašreizējo skaļumu un ātri iestatīt skaļumu ar slīdni. Varat arī izmantot ierīces aparatūras pogas, lai ātri nosūtītu skaļuma palielināšanas vai samazināšanas komandas.

Pašreizējā skaļuma nolasīšana un jauna skaļuma iestatīšana ar slīdni šobrīd ir stingri piesaistīta `pactl` izmantošanai.

Pakotni, kurā ir `pactl`, parasti sauc `pulseaudio-utils` vai `libpulse`.

## SSH atslēgas

Lietotnes iestatījumos varat importēt vai ģenerēt SSH atslēgas. Savienoties ar SSH atslēgu ir drošāk nekā izmantot paroles.

Vienkāršākais veids, kā no datora importēt esošu SSH atslēgu, ir noskenēt QR kodu. Lai ģenerētu QR koda attēlu, varat izmantot programmu `qrencode`. Izpildiet šādu komandu, lai ģenerētu QR kodu:

```shell
# Pārejiet uz savu SSH atslēgu mapi:
cd ~/.ssh

# Parādiet QR kodu terminālī:
qrencode -r id_ed25519 -t ansiutf8

# Vai arī izveidojiet attēla failu:
qrencode -r id_ed25519 -o qr.png

# 4096 bitu RSA atslēgas ir pārāk lielas QR kodam. Izmantojot gzip, to var tik tikko ietilpināt:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Varat pārsūtīt publiskās SSH atslēgas uz serveri, izmantojot izvēlnes funkciju `Pārsūtīt publisko atslēgu`. Tādējādi izvēlētā SSH atslēga tiks pievienota `~/.ssh/authorized_keys` failam. Tas ļauj ērti pāriet no pieteikšanās ar paroli uz pieteikšanos ar SSH atslēgu.

Varat importēt un izmantot šifrētas SSH atslēgas, taču pašlaik lietotnē tās ģenerēt nevar.

## Drošība

Nav iespējams eksportēt vai iegūt no lietotnes SSH atslēgu privāto daļu vai saglabātās paroles. Šie dati tiek šifrēti ar 256 bitu AES, un šifrēšanas atslēga tiek glabāta Android Keystore. Šifrētie dati netiek iekļauti Android dublējumos.

Šajā lietotnē nav avāriju ziņošanas programmatūras. Nav telemetrijas. Nav reklāmu. Nav tīkla pieprasījumu, izņemot SSH savienojumu.

Šīs lietotnes drošība nav auditēta. Ja jums ir pieredze Android drošības vai SSH drošības jomā, lūdzu, apskatiet pirmkodu un ziņojiet par saviem secinājumiem šajā GitHub jautājumā:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Funkciju pieprasījumi

Droši iesniedziet funkciju pieprasījumus un kļūdu ziņojumus GitHub repozitorijā. Lūdzu, izmantojiet angļu valodu. Lūdzu, saglabājiet cieņpilnu saziņu. Necieņpilni komentāri tiks dzēsti, un lietotājiem var tikt liegta piekļuve repozitorijam.

Lūdzu, pārskatiet esošās problēmas un diskusiju pavedienus, lai redzētu, vai jūsu jautājums jau nav uzdots vai atbildēts.

Lūdzu, esiet cieņpilni. Es izveidoju šo lietotni savā brīvajā laikā un atdodu to bez maksas. Es šo lietotni būvēju vispirms savām vajadzībām.

Lūdzu, nesūtiet man jautājumus e-pastā. Mēģiniet sarunas paturēt GitHub, jo tas palīdz arī citiem cilvēkiem! Jautājumus varat uzdot GitHub diskusiju sadaļā.

Jūs vienmēr varat izveidot lietotnes atzaru, lai ieviestu savas funkcijas. Tas ir lielisks veids, kā mācīties. Lūdzu, apsveriet iespēju ieguldīt noderīgas funkcijas.

Pirmkods ir licencēts saskaņā ar GNU GPLv3. Ja izplatāt šīs lietotnes modificētas versijas, jums ir arī jāpadara pirmkods pieejams.

<https://github.com/stefansundin/SSHRemote>

## Ziedojumi

Ja vēlaties izrādīt savu pateicību un novērtējumu, ziedojumi tiek pieņemti.

<https://stefansundin.github.io/donate/>

Ja esat ziedojis, es centīšos pēc iespējas labāk atbildēt uz jebkuru jūsu jautājumu. Lūdzu, rakstiet jautājumus angļu valodā.

Paldies par jūsu atbalstu!
