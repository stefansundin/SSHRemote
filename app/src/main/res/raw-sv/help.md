## Om SSH Remote

SSH Remote är en fri app med öppen källkod som låter dig styra datorer på distans via SSH.

Du kan helt anpassa vilka kommandon som körs, och det finns förinställningar för vanliga uppsättningar.

Jag använder den här appen för att styra min HTPC-installation, som kör Raspberry Pi OS. Att styra en HTPC är det grundscenario som appen främst är optimerad för.

Den här appen är inte en terminalemulator, men den låter dig köra `apt-get install` i nödfall.

## Komma igång

Om du vill använda en SSH-nyckel för att ansluta, öppna först appens inställningar och importera eller generera en nyckel.

Lägg till en ny värd genom att trycka på `+`-knappen längst ned till höger. Ange anslutningsuppgifterna och spara.

Första gången du ansluter blir du ombedd att välja en förinställning. Det valet konfigurerar fjärrkontrollens knappar så att de fungerar bra för olika typer av datorer. Se nedan för en beskrivning av de tillgängliga förinställningarna. Om du vill kan du börja utan någon konfiguration genom att välja `No preset`.

Om du inte vet om din Linux-dator kör X11 eller Wayland kan du köra detta i en terminal:

```shell
echo $XDG_SESSION_TYPE
```

Detta bör skriva ut `x11` eller `wayland`. Du måste köra det här inne i skrivbordsmiljön.

## Fjärrkontroll

När du är ansluten kan du använda fjärrkontrollgränssnittet för att skicka kommandon. Byt flik för att komma åt olika inmatningsmetoder.

Varje knapptryckning kör ett kommando på värden. Det innebär ganska mycket overhead för något så enkelt som en tangenttryckning, och du kan uppleva ganska hög latens jämfört med ett vanligt tangentbord. Jag hoppas kunna förbättra detta i framtida versioner.

Använd menyn för att gå in i redigeringsläget. Det är för närvarande inte möjligt att redigera layouten eller knappikonerna. Jag hoppas kunna göra detta möjligt i en framtida version.

## Förinställningar

Du behöver installera verktyget som krävs för din skrivbordsmiljö.

Jag rekommenderar `ydotool` eftersom det i mina tester har bäst prestanda och fungerar på både X11 och Wayland.

### ydotool

`ydotool` bör fungera med vilken fönsterhanterare som helst, men du behöver en bakgrundstjänst som körs. Om din distribution tillhandahåller en systemd-tjänst för användare kan du starta den med:

```shell
systemctl start --user ydotool
```

För att starta tjänsten automatiskt vid inloggning kör du:

```shell
systemctl enable --user ydotool
```

Se till att du installerar en tillräckligt ny version av `ydotool`. Ubuntu-versioner före 26.04 innehåller versioner som är för gamla. Se diskussionstråden för en lösning.

Diskutera gärna `ydotool` i denna diskussionstråd: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` är för datorer som kör X11. X11 är vad de flesta Linux-datorer historiskt har använt, även om Wayland blir allt vanligare.

En egenhet med X11 är att du kan behöva tillåta åtkomst till X-servern. Det här är problemet om du får felmeddelanden som "Authorization required". Du har några alternativ för att lösa detta, här är två som har fungerat för mig:

Om `xauth list` inte visar några poster kan du försöka generera en `.Xauthority`-fil:

```shell
xauth generate :0 . trusted
```

Om det inte fungerade kan du i stället försöka ge åtkomst med `xhost`:

```shell
xhost +local:$USER
```

Du behöver köra kommandot `xhost` efter varje omstart. Du kan automatisera detta genom att skapa ett bash-skript och konfigurera det att starta automatiskt vid inloggning.

Diskutera gärna `xdotool` i denna diskussionstråd: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` är som `xdotool`, men för Wayland.

Normalt stöder det inte musstyrning, men jag har skapat en modifierad version som lägger till musstöd. Installera den gärna om du behöver musstöd: <https://github.com/stefansundin/wtype>

Om du får felet `Compositor does not support the virtual keyboard protocol` föreslår jag att du provar ett annat verktyg, till exempel `ydotool`.

Diskutera gärna `wtype` i denna diskussionstråd: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` bör fungera med vilken fönsterhanterare som helst, ungefär som `ydotool`. I mina begränsade tester var det mycket långsammare än `ydotool`.

Diskutera gärna `dotool` i denna diskussionstråd: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Denna förinställning är experimentell eftersom jag inte har kunnat verifiera den på min egen hårdvara. Feedback välkomnas.

Diskutera gärna `cec-client` i denna diskussionstråd: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Det finns en förinställning för att styra VLC på macOS med AppleScript-kommandon. Jag känner inte till något verktyg som stöder att skicka tangentbords- eller mushändelser.

Diskutera gärna macOS i denna diskussionstråd: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Om din Android-enhet har en SSH-server kan du kanske ansluta till den och skicka inmatningshändelser. Det är mer sannolikt om din enhet kör ett anpassat ROM, som KonstaKANG på Raspberry Pi.

Jag har inte lyckats få musstöd att fungera.

Diskutera gärna Android i denna diskussionstråd: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Smart volymkontroll

När du redigerar fjärrkontrollen hittar du "smarta" inställningar för volymkontroll i menyn. Dessa kan visa datorns aktuella volym i appen och låta dig snabbt ställa in volymen med ett reglage. Du kan också använda enhetens hårdvaruknappar för att snabbt skicka kommandon för volym upp/ned.

Att läsa aktuell volym och ställa in en ny volym med reglaget är för närvarande hårdkodat att använda `pactl`.

Paketet som innehåller `pactl` heter vanligtvis `pulseaudio-utils` eller `libpulse`.

## SSH-nycklar

Du kan importera eller generera SSH-nycklar i appens inställningar. Att ansluta med en SSH-nyckel är säkrare än att använda lösenord.

Det enklaste sättet att importera en befintlig SSH-nyckel från en dator är att skanna en QR-kod. Du kan använda programmet `qrencode` för att generera QR-kodsbilden. Kör ett kommando som detta för att generera QR-koden:

```shell
# Gå till dina SSH-nycklar:
cd ~/.ssh

# Visa QR-koden i terminalen:
qrencode -r id_ed25519 -t ansiutf8

# Alternativt kan du skapa en bildfil:
qrencode -r id_ed25519 -o qr.png

# 4096-bitars RSA-nycklar är för stora för en QR-kod. Du kan använda gzip för att precis få plats med en:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Du kan skicka publika SSH-nycklar till en server med funktionen `Skicka publik nyckel` i menyn. Då läggs den valda SSH-nyckeln till i filen `~/.ssh/authorized_keys`. Det gör att du enkelt kan gå från inloggning med lösenord till inloggning med SSH-nyckel.

Du kan importera och använda krypterade SSH-nycklar, men du kan för närvarande inte generera dem i appen.

## Säkerhet

Det går inte att exportera eller extrahera den privata delen av SSH-nycklar, eller sparade lösenord, från appen. Denna data krypteras med 256-bitars AES, och krypteringsnyckeln lagras i Android Keystore. Krypterad data undantas från Android-säkerhetskopior.

Det finns ingen kraschrapportering i appen. Det finns ingen telemetri. Det finns ingen reklam. Det görs inga nätverksförfrågningar förutom SSH-anslutningen.

Säkerheten i den här appen har inte granskats. Om du har erfarenhet av Android-säkerhet eller SSH-säkerhet får du gärna titta på källkoden och rapportera dina resultat i detta GitHub-ärende:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Funktionsönskemål

Du är välkommen att skicka in funktionsönskemål och felrapporter i GitHub-repositoriet. Använd gärna engelska. Håll en god ton. Respektlösa kommentarer tas bort och användare kan blockeras från repositoriet.

Titta gärna igenom befintliga issues och diskussionstrådar för att se om din fråga redan har ställts eller besvarats.

Var respektfull. Jag byggde den här appen på min fritid och ger bort den gratis. Jag bygger den här appen främst för mitt eget bruk.

Skicka helst inte frågor via e-post. Försök att hålla konversationer på GitHub, eftersom det hjälper andra också! Du kan ställa frågor i diskussionssektionen på GitHub.

Du är alltid välkommen att forka appen för att implementera egna funktioner. Det är ett bra sätt att lära sig. Överväg gärna att bidra med användbara funktioner.

Källkoden är licensierad under GNU GPLv3. Om du distribuerar modifierade versioner av den här appen måste du också göra källkoden tillgänglig.

<https://github.com/stefansundin/SSHRemote>

## Donationer

Om du vill visa din tacksamhet och uppskattning tas donationer gärna emot.

<https://stefansundin.github.io/donate/>

Om du har donerat ska jag göra mitt bästa för att svara på frågor du kan ha.

Tack för ditt stöd!
