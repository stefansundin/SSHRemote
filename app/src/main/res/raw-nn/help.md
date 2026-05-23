## Om SSH Remote

Denne omsetjinga er laga med AI via GitHub Copilot og kan innehalde omsetjingsfeil.

SSH Remote er ein gratis app med open kjeldekode som lèt deg styre datamaskiner eksternt med SSH.

Du kan tilpasse kommandoane som blir køyrde fullt ut, og det finst førehandsinnstillingar for vanlege oppsett.

Eg bruker denne appen til å styre HTPC-oppsettet mitt, som køyrer Raspberry Pi OS. Å styre ein HTPC er det grunnleggjande scenariet appen er optimalisert for.

Denne appen er ikkje ein terminalemulator, men han lèt deg køyre `apt-get install` i ein naudssituasjon.

## Kom i gang

Dersom du vil bruke ein SSH-nøkkel for å kople til, opnar du først appinnstillingane og importerer eller genererer ein nøkkel.

Legg til ein ny vert ved å trykkje på `+`-knappen nede til høgre. Skriv inn tilkoplingsdetaljane og lagre.

Første gongen du koplar til, blir du beden om å velje ei førehandsinnstilling. Dette valet konfigurerer knappane på fjernkontrollen slik at dei fungerer godt på ulike typar datamaskiner. Sjå nedanfor for ei skildring av dei tilgjengelege førehandsinnstillingane. Dersom du føretrekkjer det, kan du starte utan konfigurasjon ved å velje `Ingen førehandsinnstilling`.

Dersom du ikkje veit om Linux-datamaskina di køyrer X11 eller Wayland, køyrer du dette i ein terminal:

```shell
echo $XDG_SESSION_TYPE
```

Dette skal skrive ut `x11` eller `wayland`. Du må køyre dette inne i skrivebordsmiljøet.

## Fjernkontroll

Når du er kopla til, kan du bruke fjernkontrollgrensesnittet til å sende kommandoar. Byt faner for å få tilgang til ulike inndatametodar.

Kvart knappetrykk køyrer ein kommando på verten. Dette gir mykje overhead for noko så enkelt som eit tastetrykk, og du kan oppleve ganske høg forseinking samanlikna med eit vanleg tastatur. Eg håpar å forbetre dette i framtidige versjonar.

Bruk menyen for å gå inn i redigeringsmodus. Det er førebels ikkje mogleg å redigere oppsettet eller knappikona. Eg håpar å gjere dette mogleg i ein framtidig versjon.

## Førehandsinnstillingar

Du må installere verktøyet som trengst for skrivebordsmiljøet ditt.

Eg tilrår `ydotool` fordi det i mine testar har best yting, og det fungerer både på X11 og Wayland.

### ydotool

`ydotool` skal fungere med alle vindaugshandsamarar, men du treng ei bakgrunnsteneste som køyrer. Dersom distribusjonen di tilbyr ei systemd-brukarteneste, startar du henne med:

```shell
systemctl start --user ydotool
```

Start tenesta automatisk ved innlogging med:

```shell
systemctl enable --user ydotool
```

Pass på at du installerer ein ny nok versjon av `ydotool`. Ubuntu-versjonar før 26.04 leverer versjonar som er for gamle. Sjå diskusjonstråden for ei løysing.

Diskuter gjerne `ydotool` i denne diskusjonstråden: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` er for datamaskiner som køyrer X11. X11 er det dei fleste Linux-datamaskiner historisk har brukt, sjølv om Wayland blir meir populært.

Eit særtrekk ved X11 er at du kanskje må gje tilgang til X-serveren. Dette er truleg problemet dersom du får feilen "Authorization required". Du har fleire moglegheiter til å løyse dette; her er to alternativ som har fungert for meg:

Dersom `xauth list` ikkje viser nokre oppføringar, kan du prøve å generere ei `.Xauthority`-fil:

```shell
xauth generate :0 . trusted
```

Dersom det ikkje fungerte, kan du prøve å gje tilgang med `xhost`:

```shell
xhost +local:$USER
```

Du må køyre `xhost`-kommandoen etter kvar oppstart. Du kan automatisere dette ved å lage eit bash-skript og konfigurere det til å starte automatisk ved innlogging.

Diskuter gjerne `xdotool` i denne diskusjonstråden: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` er som `xdotool`, men for Wayland.

Vanlegvis støttar det ikkje musestyring, men eg har laga ein modifisert versjon som legg til musestøtte. Installer han dersom du treng musestøtte: <https://github.com/stefansundin/wtype>

Dersom du får feilen `Compositor does not support the virtual keyboard protocol`, tilrår eg at du prøver eit anna verktøy, til dømes `ydotool`.

Diskuter gjerne `wtype` i denne diskusjonstråden: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` skal fungere med alle vindaugshandsamarar, på same måte som `ydotool`. I den avgrensa testinga mi var det mykje tregare enn `ydotool`.

Diskuter gjerne `dotool` i denne diskusjonstråden: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Denne førehandsinnstillinga er eksperimentell, sidan eg ikkje har kunna verifisere henne på mi eiga maskinvare. Tilbakemeldingar er velkomne.

Diskuter gjerne `cec-client` i denne diskusjonstråden: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Det finst ei førehandsinnstilling for å styre VLC på macOS ved hjelp av AppleScript-kommandoar. Eg kjenner ikkje til noko verktøy som støttar sending av tastatur- eller musehendingar.

Diskuter gjerne macOS i denne diskusjonstråden: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Dersom Android-eininga di kjem med ein SSH-server, kan det hende du kan kople til henne og sende inndatahendingar. Dette er meir sannsynleg dersom eininga køyrer ein tilpassa ROM, til dømes KonstaKANG på Raspberry Pi.

Eg har ikkje funne ut korleis musestøtte kan fungere.

Diskuter gjerne Android i denne diskusjonstråden: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Smart volumkontroll

Når du redigerer fjernkontrollen, finn du innstillingar for "smart" volumkontroll i menyen. Dette kan vise det noverande volumet på datamaskina i appen og la deg raskt setje volumet med ein skyveknapp. Du kan også bruke maskinvareknappane på eininga til raskt å sende kommandoar for volum opp og ned.

Lesing av gjeldande volum og setjing av nytt volum med skyveknappen er førebels hardkoda til å bruke `pactl`.

Pakken som inneheld `pactl` heiter vanlegvis `pulseaudio-utils` eller `libpulse`.

## SSH-nøklar

Du kan importere eller generere SSH-nøklar i appinnstillingane. Å kople til med ein SSH-nøkkel er tryggare enn å bruke passord.

Den enklaste måten å importere ein eksisterande SSH-nøkkel frå ein datamaskin på, er å skanne ein QR-kode. Du kan bruke programmet `qrencode` til å generere QR-kodebiletet. Køyr ein kommando som denne for å generere QR-koden:

```shell
# Gå til SSH-nøklane dine:
cd ~/.ssh

# Vis QR-koden i terminalen:
qrencode -r id_ed25519 -t ansiutf8

# Alternativt kan du lage ei biletfil:
qrencode -r id_ed25519 -o qr.png

# 4096-bit RSA-nøklar er for store for ein QR-kode. Du kan bruke gzip for så vidt å få plass til ein:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Du kan sende offentlege SSH-nøklar til ein server med funksjonen `Send offentleg nøkkel` i menyen. Dette legg den valde SSH-nøkkelen til i fila `~/.ssh/authorized_keys`. Dette gjer det enkelt å gå frå innlogging med passord til innlogging med SSH-nøkkel.

Du kan importere og bruke krypterte SSH-nøklar, men du kan ikkje generere slike i appen enno.

## Tryggleik

Det er ikkje mogleg å eksportere eller hente ut den private delen av SSH-nøklar eller lagra passord frå appen. Desse dataa er krypterte med 256-bit AES, og krypteringsnøkkelen blir lagra i Android Keystore. Krypterte data er utelatne frå Android-sikkerheitskopiar.

Det finst ingen programvare for krasjrapportering i denne appen. Det finst ingen telemetri. Det finst ingen annonsar. Det finst ingen nettverksførespurnader bortsett frå SSH-tilkoplinga.

Tryggleiken i denne appen er ikkje revidert. Dersom du har erfaring med Android-tryggleik eller SSH-tryggleik, kan du gjerne sjå på kjeldekoden og rapportere funna dine i denne GitHub-saka:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Funksjonsønske

Du er velkomen til å sende inn funksjonsønske og feilrapportar i GitHub-repositoriet. Bruk gjerne engelsk. Hald kommentarane saklege og respektfulle. Urespektfulle kommentarar blir fjerna, og brukarar kan bli blokkerte frå repositoriet.

Sjå gjennom eksisterande saker og diskusjonstrådar for å sjå om spørsmålet ditt allereie er stilt eller svart på.

Ver respektfull. Eg bygde denne appen på fritida mi, og eg gir han bort gratis. Eg byggjer først og fremst denne appen for min eigen bruk.

Ikkje send meg spørsmål på e-post. Prøv å halde samtalar på GitHub, sidan det også hjelper andre. Du kan stille spørsmål i diskusjonsseksjonen på GitHub.

Du er alltid velkomen til å lage ein fork av appen for å implementere dine eigne funksjonar. Det er ein flott måte å lære på. Vurder gjerne å bidra med nyttige funksjonar.

Kjeldekoden er lisensiert under GNU GPLv3. Dersom du distribuerer modifiserte versjonar av denne appen, må du også gjere kjeldekoden tilgjengeleg.

<https://github.com/stefansundin/SSHRemote>

## Donasjonar

Dersom du vil vise takksemd og støtte, blir donasjonar tekne imot.

<https://stefansundin.github.io/donate/>

Dersom du har donert, skal eg gjere mitt beste for å svare på spørsmål du måtte ha. Skriv gjerne førespurnader på engelsk.

Takk for støtta!
