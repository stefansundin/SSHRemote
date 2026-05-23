## O aplikaciji SSH Remote

Ta prevod je ustvarila umetna inteligenca GitHub Copilot (GPT-5.3-Codex) in lahko vsebuje prevajalske napake.

SSH Remote je brezplačna odprtokodna aplikacija, ki vam omogoča oddaljeno upravljanje računalnikov prek SSH.

Ukaze, ki se izvajajo, lahko povsem prilagodite, na voljo pa so tudi prednastavitve za pogoste postavitve.

Sam to aplikacijo uporabljam za upravljanje svoje postavitve HTPC, ki poganja Raspberry Pi OS. Upravljanje HTPC-ja je osnovni scenarij, za katerega je aplikacija optimizirana.

Ta aplikacija ni terminalski emulator, vendar vam bo v sili omogočila zagnati `apt-get install`.

## Začetek uporabe

Če želite za povezavo uporabiti ključ SSH, najprej odprite nastavitve aplikacije ter uvozite ali ustvarite ključ.

Dodajte novega gostitelja tako, da tapnete gumb `+` spodaj desno. Vnesite podatke povezave in jih shranite.

Ob prvi povezavi boste pozvani, da izberete prednastavitev. Ta izbira nastavi gumbe daljinca tako, da dobro delujejo z različnimi vrstami računalnikov. Opis razpoložljivih prednastavitev je spodaj. Če želite, lahko začnete brez nastavitev tako, da izberete `Brez prednastavitve`.

Če ne veste, ali vaš računalnik z Linuxom uporablja X11 ali Wayland, v terminalu zaženite:

```shell
echo $XDG_SESSION_TYPE
```

Izpis bi moral biti `x11` ali `wayland`. Ta ukaz morate zagnati znotraj namiznega okolja.

## Daljinec

Ko ste povezani, lahko za pošiljanje ukazov uporabljate vmesnik daljinca. Preklapljajte med zavihki za dostop do različnih načinov vnosa.

Vsak pritisk gumba izvede ukaz na gostitelju. To je precejšen režijski strošek za nekaj tako preprostega, kot je pritisk tipke, zato lahko pride do precej velike zakasnitve v primerjavi z običajno tipkovnico. Upam, da bom to izboljšal v prihodnjih različicah.

Za vstop v način urejanja uporabite meni. Trenutno ni mogoče urejati postavitve ali ikon gumbov. Upam, da bo to mogoče v prihodnji različici.

## Prednastavitve

Namestiti boste morali orodje, ki je potrebno za vaše namizno okolje.

Priporočam `ydotool`, ker se je po mojih preizkusih izkazal za najzmogljivejšega in deluje tako na X11 kot na Waylandu.

### ydotool

`ydotool` bi moral delovati s katerimkoli upravljalnikom oken, vendar potrebujete zagnano storitev v ozadju. Če vaša distribucija ponuja uporabniško storitev systemd, jo zaženite z naslednjim ukazom:

```shell
systemctl start --user ydotool
```

Če želite, da se storitev samodejno zažene ob prijavi, zaženite:

```shell
systemctl enable --user ydotool
```

Prepričajte se, da nameščate dovolj novo različico `ydotool`. Različice Ubuntu pred 26.04 ponujajo prestare različice. Za rešitev si oglejte temo razprave.

O `ydotool` prosim razpravljajte v tej temi: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` je za računalnike, ki poganjajo X11. X11 je tisto, kar je večina računalnikov z Linuxom uporabljala zgodovinsko, čeprav Wayland postaja vse bolj priljubljen.

Ena posebnost X11 je, da boste morda morali dovoliti dostop do strežnika X. To je težava, če prejemate napake »Authorization required«. To lahko odpravite na več načinov; tukaj sta dve možnosti, ki sta pri meni delovali:

Če `xauth list` ne prikaže nobenega vnosa, poskusite ustvariti datoteko `.Xauthority`:

```shell
xauth generate :0 . trusted
```

Če to ni pomagalo, poskusite dodeliti dostop z `xhost`:

```shell
xhost +local:$USER
```

Ukaz `xhost` boste morali zagnati po vsakem zagonu sistema. To lahko avtomatizirate tako, da ustvarite skript bash in ga nastavite za samodejni zagon ob prijavi.

O `xdotool` prosim razpravljajte v tej temi: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` je podoben `xdotool`, vendar za Wayland.

Običajno ne podpira upravljanja miške, vendar sem ustvaril prilagojeno različico, ki dodaja podporo za miško. Namestite jo, če potrebujete podporo za miško: <https://github.com/stefansundin/wtype>

Če prejmete napako `Compositor does not support the virtual keyboard protocol`, predlagam, da poskusite drugo orodje, na primer `ydotool`.

O `wtype` prosim razpravljajte v tej temi: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` bi moral delovati s katerimkoli upravljalnikom oken, podobno kot `ydotool`. Po mojih omejenih preizkusih je bil precej počasnejši od `ydotool`.

O `dotool` prosim razpravljajte v tej temi: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Ta prednastavitev je poskusna, saj je na lastni strojni opremi nisem mogel preveriti. Povratne informacije so dobrodošle.

O `cec-client` prosim razpravljajte v tej temi: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Na voljo je prednastavitev za upravljanje VLC-ja v sistemu macOS z ukazi AppleScript. Ne poznam orodja, ki bi podpiralo pošiljanje dogodkov tipkovnice ali miške.

O macOS prosim razpravljajte v tej temi: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Če je na vaši napravi Android nameščen strežnik SSH, se boste morda lahko povezali z njo in pošiljali dogodke vnosa. To je bolj verjetno, če naprava uporablja prilagojen ROM, kot je KonstaKANG na Raspberry Piju.

Nisem še ugotovil, kako usposobiti podporo za miško.

O Androidu prosim razpravljajte v tej temi: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Pametno upravljanje glasnosti

Pri urejanju daljinca lahko v meniju najdete »pametne« nastavitve glasnosti. To lahko v aplikaciji prikaže trenutno glasnost računalnika in vam omogoči hitro nastavitev glasnosti z drsnikom. S fizičnimi gumbi svoje naprave lahko tudi hitro pošljete ukaze za povečanje ali zmanjšanje glasnosti.

Branje trenutne glasnosti in nastavljanje nove z drsnikom je trenutno trdo nastavljeno na uporabo `pactl`.

Paket, ki vsebuje `pactl`, se običajno imenuje `pulseaudio-utils` ali `libpulse`.

## Ključi SSH

Ključe SSH lahko uvozite ali ustvarite v nastavitvah aplikacije. Povezovanje s ključem SSH je varnejše kot uporaba gesel.

Najlažji način za uvoz obstoječega ključa SSH iz računalnika je skeniranje kode QR. Za ustvarjanje slike kode QR lahko uporabite program `qrencode`. Za ustvarjanje kode QR zaženite ukaz, kot je naslednji:

```shell
# Pomaknite se do svojih ključev SSH:
cd ~/.ssh

# Prikažite kodo QR v terminalu:
qrencode -r id_ed25519 -t ansiutf8

# Lahko pa ustvarite slikovno datoteko:
qrencode -r id_ed25519 -o qr.png

# 4096-bitni ključi RSA so preveliki za kodo QR. Z gzip jih lahko komaj še spravite vanjo:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Javne ključe SSH lahko pošljete na strežnik s funkcijo `Pošlji javni ključ` v meniju. To bo izbrani ključ SSH dodalo v datoteko `~/.ssh/authorized_keys`. Tako lahko preprosto preidete s prijave z geslom na prijavo s ključem SSH.

Uvozite in uporabljate lahko šifrirane ključe SSH, vendar jih trenutno v aplikaciji še ne morete ustvariti.

## Varnost

Iz aplikacije ni mogoče izvoziti ali izvleči zasebnega dela ključev SSH ali shranjenih gesel. Ti podatki so šifrirani z 256-bitnim AES, ključ za šifriranje pa je shranjen v shrambi Android Keystore. Šifrirani podatki so izključeni iz varnostnih kopij Androida.

V tej aplikaciji ni programske opreme za poročanje o zrušitvah. Ni telemetrije. Ni oglasov. Ni omrežnih zahtevkov, razen povezave SSH.

Varnost te aplikacije ni bila pregledana. Če imate izkušnje z varnostjo Androida ali SSH, si prosim oglejte izvorno kodo in svoje ugotovitve sporočite v tej težavi na GitHubu:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Predlogi funkcij

V repozitoriju na GitHubu lahko oddate predloge funkcij in poročila o napakah. Prosimo, uporabljajte angleščino. Prosimo, ohranite spoštljiv ton. Nespoštljivi komentarji bodo odstranjeni, uporabnikom pa bo morda blokiran dostop do repozitorija.

Preglejte obstoječe težave in razprave, da preverite, ali je bilo vaše vprašanje že zastavljeno ali odgovorjeno.

Prosimo, bodite spoštljivi. To aplikacijo sem izdelal v svojem prostem času in jo brezplačno dajem na voljo. To aplikacijo gradim najprej in predvsem za svojo lastno uporabo.

Prosim, ne pošiljajte mi vprašanj po e-pošti. Poskusite pogovore ohraniti na GitHubu, saj to pomaga tudi drugim! Vprašanja lahko zastavite v razdelku razprav na GitHubu.

Vedno ste dobrodošli, da aplikacijo razvejite in vanjo dodate svoje funkcije. To je odličen način za učenje. Prosimo, razmislite o prispevanju uporabnih funkcij.

Izvorna koda je licencirana pod GNU GPLv3. Če distribuirate spremenjene različice te aplikacije, morate prav tako dati na voljo izvorno kodo.

<https://github.com/stefansundin/SSHRemote>

## Donacije

Če želite pokazati svojo hvaležnost in podporo, so donacije dobrodošle.

<https://stefansundin.github.io/donate/>

Če ste donirali, se bom po najboljših močeh potrudil odgovoriti na vsako vaše vprašanje. Prosimo, da morebitna vprašanja napišete v angleščini.

Hvala za vašo podporo!
