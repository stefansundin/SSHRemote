## Om SSH Remote

Denne oversettelsen er laget med AI via GitHub Copilot og kan inneholde oversettelsesfeil.

SSH Remote er en gratis app med åpen kildekode som lar deg styre datamaskiner eksternt med SSH.

Du kan tilpasse kommandoene som kjøres fullt ut, og det finnes forhåndsinnstillinger for vanlige oppsett.

Jeg bruker denne appen til å styre HTPC-oppsettet mitt, som kjører Raspberry Pi OS. Å styre en HTPC er det grunnleggende scenariet appen er optimalisert for.

Denne appen er ikke en terminalemulator, men den lar deg kjøre `apt-get install` i en nødsituasjon.

## Kom i gang

Hvis du vil bruke en SSH-nøkkel for å koble til, åpner du først appinnstillingene og importerer eller genererer en nøkkel.

Legg til en ny vert ved å trykke på `+`-knappen nederst til høyre. Skriv inn tilkoblingsdetaljene og lagre.

Første gang du kobler til, blir du bedt om å velge en forhåndsinnstilling. Dette valget konfigurerer knappene på fjernkontrollen slik at de fungerer godt på ulike typer datamaskiner. Se nedenfor for en beskrivelse av de tilgjengelige forhåndsinnstillingene. Hvis du foretrekker det, kan du starte uten konfigurasjon ved å velge `Ingen forhåndsinnstilling`.

Hvis du ikke vet om Linux-datamaskinen din kjører X11 eller Wayland, kjører du dette i en terminal:

```shell
echo $XDG_SESSION_TYPE
```

Dette skal skrive ut `x11` eller `wayland`. Du må kjøre dette inne i skrivebordsmiljøet.

## Fjernkontroll

Når du er koblet til, kan du bruke fjernkontrollgrensesnittet til å sende kommandoer. Bytt faner for å få tilgang til ulike inndatametoder.

Hvert knappetrykk kjører en kommando på verten. Dette gir mye overhead for noe så enkelt som et tastetrykk, og du kan oppleve ganske høy forsinkelse sammenlignet med et vanlig tastatur. Jeg håper å forbedre dette i fremtidige versjoner.

Bruk menyen for å gå inn i redigeringsmodus. Det er foreløpig ikke mulig å redigere oppsettet eller knappikonene. Jeg håper å gjøre dette mulig i en fremtidig versjon.

## Forhåndsinnstillinger

Du må installere verktøyet som kreves for skrivebordsmiljøet ditt.

Jeg anbefaler `ydotool` fordi det i mine tester har best ytelse, og det fungerer både på X11 og Wayland.

### ydotool

`ydotool` skal fungere med alle vindusbehandlere, men du trenger en bakgrunnstjeneste som kjører. Hvis distribusjonen din tilbyr en systemd-brukertjeneste, starter du den med:

```shell
systemctl start --user ydotool
```

Start tjenesten automatisk ved innlogging med:

```shell
systemctl enable --user ydotool
```

Pass på at du installerer en ny nok versjon av `ydotool`. Ubuntu-versjoner før 26.04 leverer versjoner som er for gamle. Se diskusjonstråden for en løsning.

Diskuter gjerne `ydotool` i denne diskusjonstråden: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` er for datamaskiner som kjører X11. X11 er det de fleste Linux-datamaskiner historisk har brukt, selv om Wayland blir mer populært.

En egenhet med X11 er at du kanskje må gi tilgang til X-serveren. Dette er sannsynligvis problemet hvis du får feilen "Authorization required". Du har flere muligheter for å løse dette; her er to alternativer som har fungert for meg:

Hvis `xauth list` ikke viser noen oppføringer, kan du prøve å generere en `.Xauthority`-fil:

```shell
xauth generate :0 . trusted
```

Hvis det ikke fungerte, kan du prøve å gi tilgang med `xhost`:

```shell
xhost +local:$USER
```

Du må kjøre `xhost`-kommandoen etter hver oppstart. Du kan automatisere dette ved å lage et bash-skript og konfigurere det til å starte automatisk ved innlogging.

Diskuter gjerne `xdotool` i denne diskusjonstråden: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` er som `xdotool`, men for Wayland.

Normalt støtter det ikke musestyring, men jeg har laget en modifisert versjon som legger til musestøtte. Installer den hvis du trenger musestøtte: <https://github.com/stefansundin/wtype>

Hvis du får feilen `Compositor does not support the virtual keyboard protocol`, foreslår jeg at du prøver et annet verktøy, for eksempel `ydotool`.

Diskuter gjerne `wtype` i denne diskusjonstråden: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` skal fungere med alle vindusbehandlere, på samme måte som `ydotool`. I min begrensede testing var det mye tregere enn `ydotool`.

Diskuter gjerne `dotool` i denne diskusjonstråden: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Denne forhåndsinnstillingen er eksperimentell, siden jeg ikke har kunnet verifisere den på min egen maskinvare. Tilbakemeldinger er velkomne.

Diskuter gjerne `cec-client` i denne diskusjonstråden: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Det finnes en forhåndsinnstilling for å styre VLC på macOS ved hjelp av AppleScript-kommandoer. Jeg kjenner ikke til noe verktøy som støtter sending av tastatur- eller musehendelser.

Diskuter gjerne macOS i denne diskusjonstråden: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Hvis Android-enheten din kommer med en SSH-server, kan det hende du kan koble til den og sende inndatahendelser. Dette er mer sannsynlig hvis enheten kjører en tilpasset ROM, for eksempel KonstaKANG på Raspberry Pi.

Jeg har ikke funnet ut hvordan musestøtte kan fungere.

Diskuter gjerne Android i denne diskusjonstråden: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Smart volumkontroll

Når du redigerer fjernkontrollen, finner du innstillinger for "smart" volumkontroll i menyen. Dette kan vise datamaskinens nåværende volum i appen og la deg raskt sette volumet med en skyveknapp. Du kan også bruke maskinvareknappene på enheten til raskt å sende kommandoer for volum opp og ned.

Lesing av gjeldende volum og setting av nytt volum med skyveknappen er foreløpig hardkodet til å bruke `pactl`.

Pakken som inneholder `pactl` heter vanligvis `pulseaudio-utils` eller `libpulse`.

## SSH-nøkler

Du kan importere eller generere SSH-nøkler i appinnstillingene. Å koble til med en SSH-nøkkel er sikrere enn å bruke passord.

Den enkleste måten å importere en eksisterende SSH-nøkkel fra en datamaskin på, er å skanne en QR-kode. Du kan bruke programmet `qrencode` til å generere QR-kodebildet. Kjør en kommando som denne for å generere QR-koden:

```shell
# Gå til SSH-nøklene dine:
cd ~/.ssh

# Vis QR-koden i terminalen:
qrencode -r id_ed25519 -t ansiutf8

# Alternativt kan du lage en bildefil:
qrencode -r id_ed25519 -o qr.png

# 4096-bit RSA-nøkler er for store for en QR-kode. Du kan bruke gzip for så vidt å få plass til en:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Du kan sende offentlige SSH-nøkler til en server med funksjonen `Send offentlig nøkkel` i menyen. Dette legger den valgte SSH-nøkkelen til i filen `~/.ssh/authorized_keys`. Dette gjør det enkelt å gå fra innlogging med passord til innlogging med SSH-nøkkel.

Du kan importere og bruke krypterte SSH-nøkler, men du kan ikke generere slike i appen ennå.

## Sikkerhet

Det er ikke mulig å eksportere eller hente ut den private delen av SSH-nøkler eller lagrede passord fra appen. Disse dataene er kryptert med 256-bit AES, og krypteringsnøkkelen lagres i Android Keystore. Krypterte data er utelatt fra Android-sikkerhetskopier.

Det finnes ingen programvare for krasjrapportering i denne appen. Det finnes ingen telemetri. Det finnes ingen annonser. Det finnes ingen nettverksforespørsler bortsett fra SSH-tilkoblingen.

Sikkerheten i denne appen er ikke revidert. Hvis du har erfaring med Android-sikkerhet eller SSH-sikkerhet, kan du gjerne se på kildekoden og rapportere funnene dine i denne GitHub-saken:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Funksjonsønsker

Du er velkommen til å sende inn funksjonsønsker og feilrapporter i GitHub-repositoriet. Bruk gjerne engelsk. Hold kommentarene saklige og respektfulle. Urespektfulle kommentarer blir fjernet, og brukere kan bli blokkert fra repositoriet.

Se gjennom eksisterende saker og diskusjonstråder for å se om spørsmålet ditt allerede er stilt eller besvart.

Vær respektfull. Jeg bygde denne appen på fritiden min, og jeg gir den bort gratis. Jeg bygger først og fremst denne appen for min egen bruk.

Vennligst ikke send meg spørsmål på e-post. Prøv å holde samtaler på GitHub, siden det også hjelper andre. Du kan stille spørsmål i diskusjonsseksjonen på GitHub.

Du er alltid velkommen til å lage en fork av appen for å implementere dine egne funksjoner. Det er en flott måte å lære på. Vurder gjerne å bidra med nyttige funksjoner.

Kildekoden er lisensiert under GNU GPLv3. Hvis du distribuerer modifiserte versjoner av denne appen, må du også gjøre kildekoden tilgjengelig.

<https://github.com/stefansundin/SSHRemote>

## Donasjoner

Hvis du vil vise takknemlighet og støtte, tas donasjoner imot.

<https://stefansundin.github.io/donate/>

Hvis du har donert, skal jeg gjøre mitt beste for å svare på spørsmål du måtte ha. Skriv gjerne henvendelser på engelsk.

Takk for støtten!
