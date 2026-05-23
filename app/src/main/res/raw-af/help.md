## Oor SSH Remote

Hierdie vertaling is deur AI gegenereer met GitHub Copilot (GPT-5 Codex) en kan vertaalfoute bevat.

SSH Remote is ’n gratis en oopbron-toepassing wat jou toelaat om rekenaars op afstand via SSH te beheer.

Jy kan die opdragte wat uitgevoer word heeltemal aanpas, en daar is voorafinstellings vir algemene opstellings.

Ek gebruik hierdie toepassing om my HTPC-opstelling te beheer, wat Raspberry Pi OS gebruik. Om ’n HTPC te beheer is die basiese scenario waarvoor die toepassing geoptimaliseer is.

Hierdie toepassing is nie ’n terminaalemulator nie, maar dit laat jou wel toe om `apt-get install` in ’n noodgeval uit te voer.

## Aan die gang

As jy ’n SSH-sleutel wil gebruik om te koppel, maak eers die toepassing se instellings oop en voer ’n sleutel in of genereer een.

Voeg ’n nuwe gasheer by deur die `+`-knoppie in die onderste regterhoek te tik. Voer die verbindingsbesonderhede in en stoor.

Die eerste keer wat jy koppel, sal jy gevra word om ’n voorafinstelling te kies. Hierdie keuse stel die afstandbeheerknoppies so op dat hulle goed werk op verskillende tipes rekenaars. Sien hieronder vir ’n beskrywing van die beskikbare voorafinstellings. As jy verkies, kan jy sonder konfigurasie begin deur `Geen voorafinstelling` te kies.

As jy nie weet of jou Linux-rekenaar X11 of Wayland gebruik nie, voer dit in ’n terminaal uit:

```shell
echo $XDG_SESSION_TYPE
```

Dit behoort `x11` of `wayland` terug te gee. Jy moet dit binne die lessenaaromgewing uitvoer.

## Afstandbeheer

Sodra jy gekoppel is, kan jy die afstandbeheer-koppelvlak gebruik om opdragte te stuur. Skakel tussen oortjies om toegang tot verskillende invoermetodes te kry.

Elke knoppiedruk sal ’n opdrag op die gasheer uitvoer. Dit voeg heelwat oorhoofse koste by vir iets so eenvoudig soos ’n sleuteldruk, en jy mag ’n redelik hoë mate van latensie ervaar in vergelyking met ’n gewone sleutelbord. Ek hoop om dit in toekomstige weergawes te verbeter.

Gebruik die kieslys om redigeermodus te betree. Dit is tans nie moontlik om die uitleg of knoppie-ikone te wysig nie. Ek hoop om dit in ’n toekomstige weergawe moontlik te maak.

## Voorafinstellings

Jy sal die hulpmiddel moet installeer wat vir jou lessenaaromgewing benodig word.

Ek beveel `ydotool` aan omdat dit volgens my toetse die beste werkverrigting het, en dit werk op beide X11 en Wayland.

### ydotool

`ydotool` behoort met enige vensterbestuurder te werk, maar jy moet ’n agtergronddiens laat loop. As jou verspreiding ’n systemd-gebruikersdiens voorsien, begin dit deur dit uit te voer:

```shell
systemctl start --user ydotool
```

Begin die diens outomaties by aanmelding deur dit uit te voer:

```shell
systemctl enable --user ydotool
```

Maak asseblief seker dat jy ’n voldoende nuwe weergawe van `ydotool` installeer. Ubuntu-weergawes voor 26.04 voorsien weergawes wat te oud is. Sien die besprekingsdraad vir ’n oplossing.

Bespreek asseblief `ydotool` in hierdie besprekingsdraad: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` is vir rekenaars wat X11 gebruik. X11 is wat die meeste Linux-rekenaars histories gebruik het, al raak Wayland al hoe gewilder.

Een eienaardigheid van X11 is dat jy dalk toegang tot die X-bediener moet toestaan. Dit is die probleem as jy "Authorization required"-foute kry. Jy het ’n paar opsies om hierdie probleem op te los; hier is twee opsies wat vir my gewerk het:

As `xauth list` geen inskrywings wys nie, probeer om ’n `.Xauthority`-lêer te genereer:

```shell
xauth generate :0 . trusted
```

As dit nie gewerk het nie, probeer om toegang met `xhost` toe te staan:

```shell
xhost +local:$USER
```

Jy sal die `xhost`-opdrag na elke herbegin moet uitvoer. Jy kan dit outomatiseer deur ’n bash-skryfstuk te skep en dit op aanmelding te laat begin.

Bespreek asseblief `xdotool` in hierdie besprekingsdraad: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` is soos `xdotool`, maar vir Wayland.

Normaalweg ondersteun dit nie muisbeheer nie, maar ek het ’n gewysigde weergawe geskep wat muisondersteuning byvoeg. Installeer dit as jy muisondersteuning nodig het: <https://github.com/stefansundin/wtype>

As jy die fout `Compositor does not support the virtual keyboard protocol` kry, stel ek voor dat jy ’n ander hulpmiddel probeer, soos `ydotool`.

Bespreek asseblief `wtype` in hierdie besprekingsdraad: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` behoort met enige vensterbestuurder te werk, soortgelyk aan `ydotool`. In my beperkte toetse was dit baie stadiger as `ydotool`.

Bespreek asseblief `dotool` in hierdie besprekingsdraad: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Hierdie voorafinstelling is eksperimenteel aangesien ek dit nog nie op my eie hardeware kon verifieer nie. Terugvoer is welkom.

Bespreek asseblief `cec-client` in hierdie besprekingsdraad: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Daar is ’n voorafinstelling om VLC op macOS te beheer, met behulp van AppleScript-opdragte. Ek weet nie van ’n hulpmiddel wat die stuur van sleutelbord- of muisgebeurtenisse ondersteun nie.

Bespreek asseblief macOS in hierdie besprekingsdraad: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

As jou Android-toestel met ’n SSH-bediener kom, kan jy dalk daaraan koppel en invoergebeurtenisse stuur. Dit is meer waarskynlik as jou toestel ’n pasgemaakte ROM gebruik, soos KonstaKANG op die Raspberry Pi.

Ek het nog nie uitgewerk hoe om muisondersteuning te laat werk nie.

Bespreek asseblief Android in hierdie besprekingsdraad: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Slim volumebeheer

Wanneer jy die afstandbeheer wysig, kan jy "slim" volumebeheerinstellings in die kieslys vind. Dit kan die rekenaar se huidige volume in die toepassing vertoon, en jou toelaat om vinnig die volume met behulp van 'n skuifregelaar in te stel. Jy kan ook jou toestel se hardewareknoppies gebruik om vinnig volume-af/-op-opdragte te stuur.

Die lees van die huidige volume en die instelling van 'n nuwe volume met behulp van die skuifregelaar is tans hardgekodeer om `pactl` te gebruik.

Die pakket wat `pactl` bevat, word gewoonlik `pulseaudio-utils` of `libpulse` genoem.

## SSH-sleutels

Jy kan SSH-sleutels in die toepassing se instellings invoer of genereer. Om met ’n SSH-sleutel te koppel is veiliger as om wagwoorde te gebruik.

Die maklikste manier om ’n bestaande SSH-sleutel vanaf ’n rekenaar in te voer, is om ’n QR-kode te skandeer. Jy kan die `qrencode`-program gebruik om die QR-kodebeeld te genereer. Voer ’n opdrag soos die volgende uit om die QR-kode te genereer:

```shell
# Gaan na jou SSH-sleutels:
cd ~/.ssh

# Wys die QR-kode in die terminaal:
qrencode -r id_ed25519 -t ansiutf8

# Of, skep ’n beeldlêer:
qrencode -r id_ed25519 -o qr.png

# 4096-bis RSA-sleutels is te groot vir ’n QR-kode. Jy kan gzip gebruik om een net-net te laat pas:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Jy kan publieke SSH-sleutels na ’n bediener stuur met die `Stuur publieke sleutel`-funksie in die kieslys. Dit sal die geselekteerde SSH-sleutel by die `~/.ssh/authorized_keys`-lêer voeg. Dit laat jou maklik van aanmelding met ’n wagwoord na aanmelding met ’n SSH-sleutel oorskakel.

Jy kan geënkripteerde SSH-sleutels invoer en gebruik, maar jy kan hulle tans nie in die toepassing genereer nie.

## Sekuriteit

Dit is nie moontlik om die private deel van SSH-sleutels of gestoorde wagwoorde uit die toepassing uit te voer of te onttrek nie. Hierdie data word met 256-bis AES geënkripteer, en die enkripsiesleutel word in die Android Keystore gestoor. Geënkripteerde data word van Android-rugsteunkopieë uitgesluit.

Daar is geen foutverslagdoeningsagteware in hierdie toepassing nie. Daar is geen telemetrie nie. Daar is geen advertensies nie. Daar is geen netwerkversoeke behalwe die SSH-verbinding nie.

Die sekuriteit van hierdie toepassing is nie geoudit nie. As jy ondervinding het met Android-sekuriteit of SSH-sekuriteit, kyk asseblief na die bronkode en rapporteer jou bevindings in hierdie GitHub-kwessie:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Funksieversoeke

Stuur gerus funksieversoeke en foutverslae in die GitHub-bewaarplek. Gebruik asseblief Engels. Hou asseblief jou opmerkings beleefd. Respeklose opmerkings sal verwyder word en gebruikers kan uit die bewaarplek geblokkeer word.

Lees asseblief bestaande kwessies en besprekingsdrade deur om te sien of jou vraag reeds gevra of beantwoord is.

Wees asseblief respekvol. Ek het hierdie toepassing in my vrye tyd gebou en ek gee dit gratis weg. Ek bou hierdie toepassing eers en veral vir my eie gebruik.

Moet asseblief nie vir my vrae e-pos nie. Probeer asseblief om gesprekke op GitHub te hou, aangesien dit ander mense ook help! Jy kan vrae in die besprekingsafdeling op GitHub vra.

Jy is altyd welkom om die toepassing te vurk om jou eie funksies te implementeer. Dit is ’n uitstekende manier om te leer. Oorweeg gerus om nuttige funksies by te dra.

Die bronkode is gelisensieer onder GNU GPLv3. As jy gewysigde weergawes van hierdie toepassing versprei, moet jy ook die bronkode beskikbaar stel.

<https://github.com/stefansundin/SSHRemote>

## Donasies

As jy jou dankbaarheid en waardering wil toon, word donasies aanvaar.

<https://stefansundin.github.io/donate/>

As jy gedoneer het, sal ek my bes probeer om enige vrae wat jy mag hê te beantwoord. Skryf asseblief enige navrae in Engels.

Baie dankie vir jou ondersteuning!
