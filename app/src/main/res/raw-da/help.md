## Om SSH Remote

Denne oversættelse er lavet med GitHub Copilot (model ikke oplyst i denne kontekst), og der kan forekomme oversættelsesfejl.

SSH Remote er en gratis app med åben kildekode, som giver dig mulighed for at styre computere eksternt ved hjælp af SSH.

Du kan tilpasse de kommandoer, der udføres, fuldstændigt, og der findes forudindstillinger til almindelige opsætninger.

Jeg bruger denne app til at styre mit HTPC-setup, som kører Raspberry Pi OS. Styring af en HTPC er det grundlæggende scenarie, som appen er optimeret til.

Denne app er ikke en terminalemulator, men den lader dig køre `apt-get install` i en nødsituation.

## Kom godt i gang

Hvis du vil bruge en SSH-nøgle til at oprette forbindelse, skal du først åbne appens indstillinger og importere eller generere en nøgle.

Tilføj en ny vært ved at trykke på `+`-knappen nederst til højre. Indtast forbindelsesoplysningerne, og gem.

Første gang du opretter forbindelse, bliver du bedt om at vælge en forudindstilling. Dette valg konfigurerer fjernbetjeningens knapper, så de fungerer godt på forskellige typer computere. Se nedenfor for en beskrivelse af de tilgængelige forudindstillinger. Hvis du foretrækker det, kan du starte uden nogen konfiguration ved at vælge `Ingen forudindstilling`.

Hvis du ikke ved, om din Linux-computer kører X11 eller Wayland, kan du køre dette i en terminal:

```shell
echo $XDG_SESSION_TYPE
```

Dette bør udskrive `x11` eller `wayland`. Du skal køre dette inde i skrivebordsmiljøet.

## Fjernbetjening

Når du er forbundet, kan du bruge fjernbetjeningsgrænsefladen til at sende kommandoer. Skift faner for at få adgang til forskellige inputmetoder.

Hvert tryk på en knap udfører en kommando på værten. Det er en del overhead for noget så simpelt som et tastetryk, og du kan opleve en ret høj grad af forsinkelse sammenlignet med et almindeligt tastatur. Jeg håber at forbedre dette i fremtidige versioner.

Brug menuen til at gå i redigeringstilstand. Det er i øjeblikket ikke muligt at redigere layoutet eller knapikonerne. Jeg håber at gøre dette muligt i en fremtidig version.

## Forudindstillinger

Du skal installere det værktøj, der kræves til dit skrivebordsmiljø.

Jeg anbefaler `ydotool`, fordi det i mine test har den bedste ydeevne, og det fungerer på både X11 og Wayland.

### ydotool

`ydotool` bør fungere med enhver vindueshåndtering, men du skal have en baggrundstjeneste kørende. Hvis din distribution leverer en systemd-brugertjeneste, kan du starte den ved at køre:

```shell
systemctl start --user ydotool
```

Start tjenesten automatisk ved login ved at køre:

```shell
systemctl enable --user ydotool
```

Sørg for at installere en tilstrækkelig ny version af `ydotool`. Ubuntu-versioner før 26.04 leverer versioner, der er for gamle. Se diskussionstråden for en løsning.

Diskutér gerne `ydotool` i denne diskussionstråd: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` er til computere, der kører X11. X11 er det, de fleste Linux-computere historisk har brugt, selv om Wayland bliver mere populært.

En særhed ved X11 er, at du muligvis skal give adgang til X-serveren. Det er problemet, hvis du får fejl som "Authorization required". Du har et par muligheder for at løse dette; her er to muligheder, som har virket for mig:

Hvis `xauth list` ikke viser nogen poster, så prøv at generere en `.Xauthority`-fil:

```shell
xauth generate :0 . trusted
```

Hvis det ikke virkede, så prøv at give adgang med `xhost`:

```shell
xhost +local:$USER
```

Du skal køre `xhost`-kommandoen efter hver opstart. Du kan automatisere dette ved at oprette et bash-script og konfigurere det til at starte automatisk ved login.

Diskutér gerne `xdotool` i denne diskussionstråd: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` er som `xdotool`, men til Wayland.

Normalt understøtter det ikke musestyring, men jeg har lavet en modificeret version, der tilføjer musesupport. Installér den gerne, hvis du har brug for musesupport: <https://github.com/stefansundin/wtype>

Hvis du får fejlen `Compositor does not support the virtual keyboard protocol`, foreslår jeg, at du prøver et andet værktøj, såsom `ydotool`.

Diskutér gerne `wtype` i denne diskussionstråd: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` bør fungere med enhver vindueshåndtering, ligesom `ydotool`. I min begrænsede test var det meget langsommere end `ydotool`.

Diskutér gerne `dotool` i denne diskussionstråd: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Denne forudindstilling er eksperimentel, da jeg ikke har kunnet verificere den på min egen hardware. Feedback er velkommen.

Diskutér gerne `cec-client` i denne diskussionstråd: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Der findes en forudindstilling til at styre VLC på macOS ved hjælp af AppleScript-kommandoer. Jeg kender ikke til noget værktøj, der understøtter afsendelse af tastatur- eller musehændelser.

Diskutér gerne macOS i denne diskussionstråd: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Hvis din Android-enhed leveres med en SSH-server, kan du muligvis oprette forbindelse til den og sende inputhændelser. Dette er mere sandsynligt, hvis din enhed kører en brugerdefineret ROM, som for eksempel KonstaKANG på Raspberry Pi.

Jeg har ikke fundet ud af, hvordan jeg får musesupport til at virke.

Diskutér gerne Android i denne diskussionstråd: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Smart lydstyrkestyring

Når du redigerer fjernbetjeningen, kan du finde "smarte" lydstyrkeindstillinger i menuen. Dette kan vise computerens aktuelle lydstyrke i appen og lade dig hurtigt indstille lydstyrken med en skyder. Du kan også bruge enhedens hardwareknapper til hurtigt at sende kommandoer til at skrue op eller ned for lyden.

Læsning af den aktuelle lydstyrke og indstilling af en ny lydstyrke med skyderen er i øjeblikket hårdkodet til at bruge `pactl`.

Pakken, der indeholder `pactl`, hedder som regel `pulseaudio-utils` eller `libpulse`.

## SSH-nøgler

Du kan importere eller generere SSH-nøgler i appens indstillinger. Det er mere sikkert at oprette forbindelse med en SSH-nøgle end at bruge adgangskoder.

Den nemmeste måde at importere en eksisterende SSH-nøgle fra en computer på er at scanne en QR-kode. Du kan bruge programmet `qrencode` til at generere QR-kodebilledet. Kør en kommando som følgende for at generere QR-koden:

```shell
# Gå til dine SSH-nøgler:
cd ~/.ssh

# Vis QR-koden i terminalen:
qrencode -r id_ed25519 -t ansiutf8

# Alternativt kan du oprette en billedfil:
qrencode -r id_ed25519 -o qr.png

# 4096-bit RSA-nøgler er for store til en QR-kode. Du kan bruge gzip for lige akkurat at få en til at passe:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Du kan overføre offentlige SSH-nøgler til en server med funktionen `Overfør offentlig nøgle` i menuen. Dette tilføjer den valgte SSH-nøgle til filen `~/.ssh/authorized_keys`. Det gør det nemt at migrere fra login med adgangskode til login med en SSH-nøgle.

Du kan importere og bruge krypterede SSH-nøgler, men du kan ikke generere dem i appen endnu.

## Sikkerhed

Det er ikke muligt at eksportere eller udtrække den private del af SSH-nøgler eller gemte adgangskoder fra appen. Disse data er krypteret med 256-bit AES, og krypteringsnøglen gemmes i Android Keystore. Krypterede data udelukkes fra Android-sikkerhedskopier.

Der er ingen software til nedbrudsrapportering i denne app. Der er ingen telemetri. Der er ingen annoncer. Der er ingen netværksanmodninger bortset fra SSH-forbindelsen.

Sikkerheden i denne app er ikke blevet revideret. Hvis du har erfaring med Android-sikkerhed eller SSH-sikkerhed, så tag gerne et kig på kildekoden og rapportér dine fund i denne GitHub-issue:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Funktionsønsker

Du er velkommen til at indsende funktionsønsker og fejlrapporter i GitHub-repositoriet. Brug venligst engelsk. Hold venligst dine kommentarer saglige. Respektløse kommentarer vil blive fjernet, og brugere kan blive blokeret fra repositoriet.

Se venligst eksisterende issues og diskussionstråde igennem for at se, om dit spørgsmål allerede er blevet stillet eller besvaret.

Vær venligst respektfuld. Jeg har bygget denne app i min fritid, og jeg giver den væk gratis. Jeg bygger først og fremmest denne app til mit eget brug.

Send mig venligst ikke spørgsmål via e-mail. Prøv at holde samtaler på GitHub, da det også hjælper andre! Du kan stille spørgsmål i diskussionssektionen på GitHub.

Du er altid velkommen til at forke appen for at implementere dine egne funktioner. Det er en god måde at lære på. Overvej gerne at bidrage med nyttige funktioner.

Kildekoden er licenseret under GNU GPLv3. Hvis du distribuerer modificerede versioner af denne app, skal du også gøre kildekoden tilgængelig.

<https://github.com/stefansundin/SSHRemote>

## Donationer

Hvis du ønsker at vise din taknemmelighed og værdsættelse, modtages donationer gerne.

<https://stefansundin.github.io/donate/>

Hvis du har doneret, vil jeg gøre mit bedste for at besvare ethvert spørgsmål, du måtte have. Skriv venligst eventuelle henvendelser på engelsk.

Tak for din støtte!
