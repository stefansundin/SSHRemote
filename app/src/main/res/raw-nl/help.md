## Over SSH Remote

Deze Nederlandse vertaling is gemaakt met GitHub Copilot (het gebruikte model is niet weergegeven in deze omgeving), dus er kunnen vertaalfouten in staan.

SSH Remote is een gratis opensource-app waarmee je computers op afstand kunt bedienen via SSH.

Je kunt de commando's die worden uitgevoerd volledig aanpassen, en er zijn voorinstellingen voor veelvoorkomende configuraties.

Ik gebruik deze app om mijn HTPC-opstelling te bedienen, die draait op Raspberry Pi OS. Het bedienen van een HTPC is het basisscenario waarvoor de app is geoptimaliseerd.

Deze app is geen terminalemulator, maar in noodgevallen kun je er wel `apt-get install` mee uitvoeren.

## Aan de slag

Als je een SSH-sleutel wilt gebruiken om verbinding te maken, open dan eerst de app-instellingen en importeer of genereer een sleutel.

Voeg een nieuwe host toe door op de knop `+` rechtsonder te tikken. Vul de verbindingsgegevens in en sla op.

De eerste keer dat je verbinding maakt, wordt je gevraagd een voorinstelling te selecteren. Deze keuze configureert de knoppen van de afstandsbediening zodat ze goed werken op verschillende soorten computers. Zie hieronder voor een beschrijving van de beschikbare voorinstellingen. Als je dat liever hebt, kun je zonder configuratie beginnen door `Geen voorinstelling` te selecteren.

Als je niet weet of je Linux-computer X11 of Wayland gebruikt, voer dan dit uit in een terminal:

```shell
echo $XDG_SESSION_TYPE
```

Dit zou `x11` of `wayland` moeten tonen. Je moet dit uitvoeren binnen de desktopomgeving.

## Afstandsbediening

Zodra je verbonden bent, kun je de interface van de afstandsbediening gebruiken om commando's te versturen. Wissel van tabblad om verschillende invoermethoden te gebruiken.

Bij elke druk op een knop wordt een commando op de host uitgevoerd. Dat is behoorlijk veel overhead voor iets simpels als een toetsaanslag, en je kunt daardoor relatief veel vertraging ervaren vergeleken met een normaal toetsenbord. Ik hoop dit in toekomstige versies te verbeteren.

Gebruik het menu om de bewerkmodus te openen. Het is momenteel niet mogelijk om de lay-out of de pictogrammen van knoppen te bewerken. Ik hoop dit in een toekomstige versie mogelijk te maken.

## Voorinstellingen

Je moet het hulpprogramma installeren dat vereist is voor jouw desktopomgeving.

Ik raad `ydotool` aan, omdat het in mijn tests de beste prestaties heeft en werkt op zowel X11 als Wayland.

### ydotool

`ydotool` zou met elke windowmanager moeten werken, maar je hebt wel een achtergrondservice nodig. Als je distributie een systemd-gebruikersservice levert, start die dan met:

```shell
systemctl start --user ydotool
```

Laat de service automatisch starten bij het inloggen met:

```shell
systemctl enable --user ydotool
```

Zorg er alsjeblieft voor dat je een recente versie van `ydotool` installeert. Ubuntu-versies vóór 26.04 leveren versies die te oud zijn. Zie de discussiethread voor een tijdelijke oplossing.

Bespreek `ydotool` in deze discussiethread: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` is voor computers die X11 gebruiken. X11 is wat de meeste Linux-computers historisch hebben gebruikt, al wordt Wayland steeds populairder.

Een eigenaardigheid van X11 is dat je mogelijk toegang tot de X-server moet toestaan. Dat is het probleem als je fouten krijgt zoals `Authorization required`. Je hebt een paar opties om dit op te lossen; hier zijn twee opties die voor mij hebben gewerkt:

Als `xauth list` geen vermeldingen toont, probeer dan een `.Xauthority`-bestand te genereren:

```shell
xauth generate :0 . trusted
```

Als dat niet werkte, probeer dan toegang te verlenen met `xhost`:

```shell
xhost +local:$USER
```

Je moet het commando `xhost` na elke herstart uitvoeren. Je kunt dit automatiseren door een bash-script te maken en het zo te configureren dat het automatisch start bij het inloggen.

Bespreek `xdotool` in deze discussiethread: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` is zoals `xdotool`, maar dan voor Wayland.

Normaal ondersteunt het geen muisbediening, maar ik heb een aangepaste versie gemaakt die muisondersteuning toevoegt. Installeer die alsjeblieft als je muisondersteuning nodig hebt: <https://github.com/stefansundin/wtype>

Als je de fout `Compositor does not support the virtual keyboard protocol` krijgt, raad ik aan een ander hulpprogramma te proberen, zoals `ydotool`.

Bespreek `wtype` in deze discussiethread: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` zou met elke windowmanager moeten werken, net als `ydotool`. In mijn beperkte tests was het veel trager dan `ydotool`.

Bespreek `dotool` in deze discussiethread: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Deze voorinstelling is experimenteel, omdat ik die niet op mijn eigen hardware heb kunnen verifiëren. Feedback is welkom.

Bespreek `cec-client` in deze discussiethread: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Er is een voorinstelling voor het bedienen van VLC op macOS, met AppleScript-commando's. Ik ken geen hulpprogramma dat het versturen van toetsenbord- of muisgebeurtenissen ondersteunt.

Bespreek macOS in deze discussiethread: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Als je Android-apparaat een SSH-server heeft, kun je er mogelijk verbinding mee maken en invoergebeurtenissen versturen. Dit is waarschijnlijker als je apparaat een aangepaste ROM gebruikt, zoals KonstaKANG op de Raspberry Pi.

Ik heb nog niet uitgezocht hoe ik muisondersteuning werkend krijg.

Bespreek Android in deze discussiethread: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Slimme volumeregeling

Bij het bewerken van de afstandsbediening kun je in het menu instellingen voor "slimme" volumeregeling vinden. Hiermee kun je het huidige volume van de computer in de app weergeven en het volume snel instellen met een schuifregelaar. Je kunt ook de hardwareknoppen van je apparaat gebruiken om snel commando's voor volume omhoog/omlaag te sturen.

Het uitlezen van het huidige volume en het instellen van een nieuw volume met de schuifregelaar zijn momenteel hardcoded om `pactl` te gebruiken.

Het pakket met `pactl` heet meestal `pulseaudio-utils` of `libpulse`.

## SSH-sleutels

Je kunt SSH-sleutels importeren of genereren in de app-instellingen. Verbinden met een SSH-sleutel is veiliger dan wachtwoorden gebruiken.

De makkelijkste manier om een bestaande SSH-sleutel van een computer te importeren is door een QR-code te scannen. Je kunt het programma `qrencode` gebruiken om de QR-codeafbeelding te genereren. Voer een commando uit zoals het volgende om de QR-code te genereren:

```shell
# Navigeer naar je SSH-sleutels:
cd ~/.ssh

# Toon de QR-code in de terminal:
qrencode -r id_ed25519 -t ansiutf8

# Je kunt ook een afbeeldingsbestand maken:
qrencode -r id_ed25519 -o qr.png

# 4096-bit RSA-sleutels zijn te groot voor een QR-code. Met gzip past er net één:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Je kunt publieke SSH-sleutels naar een server uploaden met de functie `Publieke sleutel uploaden` in het menu. Daarmee wordt de geselecteerde SSH-sleutel toegevoegd aan het bestand `~/.ssh/authorized_keys`. Zo kun je eenvoudig overstappen van inloggen met een wachtwoord naar inloggen met een SSH-sleutel.

Je kunt versleutelde SSH-sleutels importeren en gebruiken, maar je kunt die momenteel niet in de app genereren.

## Beveiliging

Het is niet mogelijk om het privégedeelte van SSH-sleutels, of opgeslagen wachtwoorden, uit de app te exporteren of te extraheren. Deze gegevens worden versleuteld met 256-bit AES, en de versleutelingssleutel wordt opgeslagen in de Android Keystore. Versleutelde gegevens worden uitgesloten van Android-back-ups.

Er zit geen crashrapportagesoftware in deze app. Er is geen telemetrie. Er zijn geen advertenties. Er zijn geen netwerkverzoeken behalve de SSH-verbinding.

De beveiliging van deze app is niet geaudit. Als je ervaring hebt met Android-beveiliging of SSH-beveiliging, kijk dan alsjeblieft naar de broncode en meld je bevindingen in deze GitHub-issue:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Functieverzoeken

Voel je vrij om functieverzoeken en bugmeldingen in te dienen in de GitHub-repository. Gebruik alsjeblieft Engels. Houd je opmerkingen beleefd. Respectloze opmerkingen worden verwijderd en gebruikers kunnen worden geblokkeerd in de repository.

Kijk alsjeblieft eerst door bestaande issues en discussiethreads om te zien of je vraag al is gesteld of beantwoord.

Wees alsjeblieft respectvol. Ik heb deze app in mijn vrije tijd gebouwd en ik geef hem gratis weg. Ik bouw deze app in de eerste plaats voor mijn eigen gebruik.

Stuur me alsjeblieft geen vragen per e-mail. Probeer gesprekken op GitHub te houden, want daar hebben andere mensen ook iets aan! Je kunt vragen stellen in de discussiesectie op GitHub.

Je bent altijd welkom om de app te forken om je eigen functies te implementeren. Dat is een geweldige manier om te leren. Overweeg alsjeblieft om nuttige functies bij te dragen.

De broncode is gelicentieerd onder GNU GPLv3. Als je aangepaste versies van deze app distribueert, moet je ook de broncode beschikbaar maken.

<https://github.com/stefansundin/SSHRemote>

## Donaties

Als je je dankbaarheid en waardering wilt tonen, zijn donaties welkom.

<https://stefansundin.github.io/donate/>

Als je hebt gedoneerd, zal ik mijn best doen om elke vraag die je hebt te beantwoorden, maar schrijf je vragen alsjeblieft in het Engels.

Bedankt voor je steun!
