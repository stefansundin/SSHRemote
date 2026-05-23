## Rreth SSH Remote

Ky përkthim është bërë me GitHub Copilot (GPT-5) dhe mund të përmbajë gabime përkthimi.

SSH Remote është një aplikacion falas dhe me burim të hapur që ju lejon të kontrolloni kompjuterët nga distanca duke përdorur SSH.

Mund t'i personalizoni plotësisht komandat që ekzekutohen, dhe ka paracaktime për konfigurime të zakonshme.

Unë e përdor këtë aplikacion për të kontrolluar HTPC-në time, e cila përdor Raspberry Pi OS. Kontrollimi i një HTPC-je është skenari bazë për të cilin aplikacioni është optimizuar.

Ky aplikacion nuk është një emulator terminali, por do t'ju lejojë të ekzekutoni `apt-get install` në raste emergjente.

## Fillimi

Nëse dëshironi të përdorni një çelës SSH për t'u lidhur, hapni fillimisht cilësimet e aplikacionit dhe importoni ose gjeneroni një çelës.

Shtoni një host të ri duke prekur butonin `+` në këndin e poshtëm djathtas. Futni të dhënat e lidhjes dhe ruajini.

Herën e parë që lidheni, do t'ju kërkohet të zgjidhni një paracaktim. Kjo zgjedhje i konfiguron butonat e telekomandës që të punojnë mirë në lloje të ndryshme kompjuterësh. Shikoni më poshtë për një përshkrim të paracaktimeve të disponueshme. Nëse preferoni, mund të filloni pa konfigurim duke zgjedhur `Pa paracaktim`.

Nëse nuk e dini nëse kompjuteri juaj Linux përdor X11 apo Wayland, ekzekutoni këtë në terminal:

```shell
echo $XDG_SESSION_TYPE
```

Kjo duhet të shfaqë `x11` ose `wayland`. Duhet ta ekzekutoni këtë brenda mjedisit të desktopit.

## Telekomanda

Pasi të lidheni, mund të përdorni ndërfaqen e telekomandës për të dërguar komanda. Ndërroni skedat për të hyrë në mënyra të ndryshme hyrjeje.

Çdo shtypje butoni do të ekzekutojë një komandë në host. Kjo sjell një mbingarkesë të konsiderueshme për diçka aq të thjeshtë sa një shtypje tasti, dhe mund të përjetoni një vonesë relativisht të lartë krahasuar me një tastierë të zakonshme. Shpresoj ta përmirësoj këtë në versione të ardhshme.

Përdorni menynë për të hyrë në mënyrën e redaktimit. Aktualisht nuk është e mundur të redaktoni paraqitjen ose ikonat e butonave. Shpresoj ta bëj të mundur këtë në një version të ardhshëm.

## Paracaktime

Do t'ju duhet të instaloni mjetin e kërkuar për mjedisin tuaj të desktopit.

Unë rekomandoj `ydotool` sepse, sipas testimeve të mia, ka performancën më të mirë dhe funksionon si në X11 ashtu edhe në Wayland.

### ydotool

`ydotool` duhet të funksionojë me çdo menaxher dritaresh, por ju nevojitet një shërbim në sfond. Nëse shpërndarja juaj ofron një shërbim përdoruesi systemd, niseni atë duke ekzekutuar:

```shell
systemctl start --user ydotool
```

Niseni automatikisht shërbimin gjatë hyrjes duke ekzekutuar:

```shell
systemctl enable --user ydotool
```

Ju lutemi sigurohuni që po instaloni një version mjaft të ri të `ydotool`. Versionet e Ubuntu para 26.04 ofrojnë versione që janë shumë të vjetra. Shihni temën e diskutimit për një zgjidhje.

Ju lutemi diskutoni `ydotool` në këtë temë diskutimi: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` është për kompjuterët që përdorin X11. X11 është sistemi që shumica e kompjuterëve Linux kanë përdorur historikisht, megjithëse Wayland po bëhet gjithnjë e më popullor.

Një veçanti e X11 është se mund t'ju duhet të lejoni qasje në serverin X. Ky është problemi nëse merrni gabime "Authorization required". Keni disa mundësi për ta rregulluar këtë problem; ja dy opsione që kanë funksionuar për mua:

Nëse `xauth list` nuk tregon asnjë hyrje, atëherë provoni të krijoni një skedar `.Xauthority`:

```shell
xauth generate :0 . trusted
```

Nëse kjo nuk funksionoi, atëherë provoni të lejoni qasjen duke përdorur `xhost`:

```shell
xhost +local:$USER
```

Do t'ju duhet ta ekzekutoni komandën `xhost` pas çdo ndezjeje. Këtë mund ta automatizoni duke krijuar një skript bash dhe duke e konfiguruar që të niset automatikisht gjatë hyrjes.

Ju lutemi diskutoni `xdotool` në këtë temë diskutimi: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` është si `xdotool`, por për Wayland.

Normalisht, ai nuk mbështet kontrollin e mausit, por unë kam krijuar një version të modifikuar që shton mbështetje për mausin. Ju lutemi instalojeni nëse keni nevojë për mbështetje të mausit: <https://github.com/stefansundin/wtype>

Nëse merrni gabimin `Compositor does not support the virtual keyboard protocol`, atëherë ju sugjeroj të provoni një mjet tjetër, si `ydotool`.

Ju lutemi diskutoni `wtype` në këtë temë diskutimi: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` duhet të funksionojë me çdo menaxher dritaresh, ngjashëm me `ydotool`. Në testimet e mia të kufizuara ishte shumë më i ngadaltë se `ydotool`.

Ju lutemi diskutoni `dotool` në këtë temë diskutimi: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Ky paracaktim është eksperimental, pasi nuk kam arritur ta verifikoj në pajisjen time. Përshtypjet janë të mirëpritura.

Ju lutemi diskutoni `cec-client` në këtë temë diskutimi: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Ka një paracaktim për kontrollimin e VLC-së në macOS, duke përdorur komanda AppleScript. Nuk jam në dijeni për ndonjë mjet që mbështet dërgimin e ngjarjeve të tastierës ose mausit.

Ju lutemi diskutoni macOS në këtë temë diskutimi: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Nëse pajisja juaj Android vjen me një server SSH, atëherë mund të jeni në gjendje të lidheni me të dhe të dërgoni ngjarje hyrëse. Kjo është më e mundshme nëse pajisja juaj përdor një ROM të personalizuar, si KonstaKANG për Raspberry Pi.

Nuk kam arritur të kuptoj se si ta bëj të funksionojë mbështetja e mausit.

Ju lutemi diskutoni Android në këtë temë diskutimi: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Kontrolli i zgjuar i volumit

Kur redaktoni telekomandën, në meny mund të gjeni cilësimet e kontrollit "të zgjuar" të volumit. Kjo mund të shfaqë volumin aktual të kompjuterit në aplikacion dhe t'ju lejojë ta vendosni shpejt volumin duke përdorur një rrëshqitës. Gjithashtu mund të përdorni butonat fizikë të pajisjes për të dërguar shpejt komanda për uljen ose ngritjen e volumit.

Leximi i volumit aktualisht është i programuar në mënyrë fikse për të përdorur `pactl`.

Paketa që përmban `pactl` zakonisht quhet `pulseaudio-utils` ose `libpulse`.

## Çelësat SSH

Mund të importoni ose të gjeneroni çelësa SSH në cilësimet e aplikacionit. Lidhja me një çelës SSH është më e sigurt se përdorimi i fjalëkalimeve.

Mënyra më e lehtë për të importuar një çelës ekzistues SSH nga një kompjuter është të skanoni një kod QR. Mund të përdorni programin `qrencode` për të gjeneruar imazhin e kodit QR. Ekzekutoni një komandë si më poshtë për të gjeneruar kodin QR:

```shell
# Shkoni te çelësat tuaj SSH:
cd ~/.ssh

# Shfaqni kodin QR në terminal:
qrencode -r id_ed25519 -t ansiutf8

# Ose, krijoni një skedar imazhi:
qrencode -r id_ed25519 -o qr.png

# Çelësat RSA 4096-bit janë shumë të mëdhenj për një kod QR. Mund të përdorni gzip për ta futur me zor një të tillë:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Mund të dërgoni çelësa publikë SSH në një server duke përdorur veçorinë `Dërgo çelësin publik` në meny. Kjo do të shtojë çelësin e zgjedhur SSH te skedari `~/.ssh/authorized_keys`. Kjo ju lejon të kaloni lehtësisht nga hyrja me fjalëkalim në hyrje me çelës SSH.

Mund të importoni dhe të përdorni çelësa SSH të enkriptuar, por aktualisht nuk mund t'i gjeneroni në aplikacion.

## Siguria

Nuk është e mundur të eksportohet ose të nxirret nga aplikacioni pjesa private e çelësave SSH, ose fjalëkalimet e ruajtura. Këto të dhëna janë të enkriptuara duke përdorur AES 256-bit dhe çelësi i enkriptimit ruhet në Android Keystore. Të dhënat e enkriptuara përjashtohen nga kopjet rezervë të Android.

Ky aplikacion nuk përmban softuer për raportimin e dështimeve. Nuk ka telemetri. Nuk ka reklama. Nuk ka kërkesa rrjeti përveç lidhjes SSH.

Siguria e këtij aplikacioni nuk është audituar. Nëse keni përvojë me sigurinë e Android ose sigurinë SSH, ju lutemi hidhni një sy në kodin burimor dhe raportoni gjetjet tuaja në këtë çështje GitHub:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Kërkesa për veçori

Mos ngurroni të dërgoni kërkesa për veçori dhe raporte gabimesh në depozitën e GitHub. Ju lutemi përdorni anglishten. Ju lutemi mbajini komentet tuaja të sjellshme. Komentet fyese do të hiqen dhe përdoruesit mund të bllokohen nga depozita.

Ju lutemi shikoni çështjet dhe temat e diskutimit ekzistuese për të parë nëse pyetja juaj është bërë ose është përgjigjur tashmë.

Ju lutemi tregoni respekt. Unë e ndërtova këtë aplikacion në kohën time të lirë dhe po e jap falas. Unë po e ndërtoj këtë aplikacion kryesisht për përdorimin tim.

Ju lutemi mos më dërgoni pyetje me email. Ju lutemi përpiquni t'i mbani bisedat në GitHub, sepse kjo i ndihmon edhe njerëzit e tjerë! Mund të bëni pyetje në seksionin e diskutimeve në GitHub.

Jeni gjithmonë të mirëpritur ta forkoni aplikacionin për të zbatuar veçoritë tuaja. Kjo është një mënyrë e shkëlqyer për të mësuar. Ju lutemi merrni në konsideratë kontribuimin e veçorive të dobishme.

Kodi burimor është i licencuar sipas GNU GPLv3. Nëse shpërndani versione të modifikuara të këtij aplikacioni, atëherë duhet gjithashtu ta bëni të disponueshëm kodin burimor.

<https://github.com/stefansundin/SSHRemote>

## Dhurime

Nëse dëshironi të tregoni mirënjohjen dhe vlerësimin tuaj, pranohen dhurime.

<https://stefansundin.github.io/donate/>

Nëse keni dhuruar, atëherë do të përpiqem t'ju përgjigjem sa më mirë çdo pyetjeje që mund të keni. Ju lutemi shkruani çdo pyetje në anglisht.

Faleminderit për mbështetjen tuaj!
