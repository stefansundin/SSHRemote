## Über SSH Remote

Diese Übersetzung wurde mit GitHub Copilot erstellt und kann Übersetzungsfehler enthalten.

SSH Remote ist eine freie Open-Source-App, mit der du Computer über SSH fernsteuern kannst.

Du kannst die ausgeführten Befehle vollständig anpassen, und es gibt Voreinstellungen für häufige Setups.

Ich nutze diese App, um mein HTPC-Setup zu steuern, auf dem Raspberry Pi OS läuft. Die Steuerung eines HTPC ist das grundlegende Szenario, für das die App optimiert ist.

Diese App ist kein Terminal-Emulator, aber sie lässt dich im Notfall `apt-get install` ausführen.

## Erste Schritte

Wenn du für die Verbindung einen SSH-Schlüssel verwenden möchtest, öffne zuerst die App-Einstellungen und importiere oder erstelle einen Schlüssel.

Füge einen neuen Host hinzu, indem du auf die `+`-Schaltfläche unten rechts tippst. Gib die Verbindungsdaten ein und speichere sie.

Wenn du dich zum ersten Mal verbindest, wirst du aufgefordert, eine Voreinstellung auszuwählen. Diese Auswahl konfiguriert die Fernbedienungsschaltflächen so, dass sie auf verschiedenen Computertypen gut funktionieren. Weiter unten findest du eine Beschreibung der verfügbaren Voreinstellungen. Wenn du möchtest, kannst du auch ohne Konfiguration beginnen, indem du `Keine Voreinstellung` auswählst.

Wenn du nicht weißt, ob dein Linux-Computer X11 oder Wayland verwendet, führe dies in einem Terminal aus:

```shell
echo $XDG_SESSION_TYPE
```

Als Ausgabe sollte `x11` oder `wayland` erscheinen. Du musst dies innerhalb der Desktop-Umgebung ausführen.

## Fernbedienung

Sobald die Verbindung hergestellt ist, kannst du die Fernbedienungsoberfläche verwenden, um Befehle zu senden. Wechsle zwischen den Tabs, um auf verschiedene Eingabemethoden zuzugreifen.

Jeder Tastendruck führt einen Befehl auf dem Host aus. Das ist ein erheblicher Overhead für etwas so Einfaches wie einen Tastendruck, und du kannst im Vergleich zu einer normalen Tastatur eine recht hohe Latenz bemerken. Ich hoffe, das in zukünftigen Versionen zu verbessern.

Verwende das Menü, um den Bearbeitungsmodus zu aktivieren. Derzeit ist es nicht möglich, das Layout oder die Schaltflächensymbole zu bearbeiten. Ich hoffe, dies in einer zukünftigen Version zu ermöglichen.

## Voreinstellungen

Du musst das für deine Desktop-Umgebung erforderliche Werkzeug installieren.

Ich empfehle `ydotool`, weil es in meinen Tests die beste Leistung hat und sowohl unter X11 als auch unter Wayland funktioniert.

### ydotool

`ydotool` sollte mit jedem Fenstermanager funktionieren, aber dafür muss ein Hintergrunddienst laufen. Wenn deine Distribution einen systemd-Benutzerdienst bereitstellt, starte ihn mit:

```shell
systemctl start --user ydotool
```

Damit der Dienst bei der Anmeldung automatisch startet, führe Folgendes aus:

```shell
systemctl enable --user ydotool
```

Bitte stelle sicher, dass du eine ausreichend neue Version von `ydotool` installierst. Ubuntu-Versionen vor 26.04 liefern Versionen, die zu alt sind. Im Diskussions-Thread findest du einen Workaround.

Bitte diskutiere über `ydotool` in diesem Diskussions-Thread: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` ist für Computer gedacht, auf denen X11 läuft. X11 wurde historisch von den meisten Linux-Computern verwendet, obwohl Wayland zunehmend beliebter wird.

Eine Besonderheit von X11 ist, dass du möglicherweise den Zugriff auf den X-Server erlauben musst. Das ist das Problem, wenn du Fehler wie „Authorization required“ erhältst. Du hast mehrere Möglichkeiten, dieses Problem zu beheben; hier sind zwei Optionen, die bei mir funktioniert haben:

Wenn `xauth list` keine Einträge anzeigt, versuche, eine `.Xauthority`-Datei zu erzeugen:

```shell
xauth generate :0 . trusted
```

Wenn das nicht funktioniert hat, versuche, mit `xhost` Zugriff zu gewähren:

```shell
xhost +local:$USER
```

Du musst den `xhost`-Befehl nach jedem Neustart erneut ausführen. Du kannst das automatisieren, indem du ein Bash-Skript erstellst und so konfigurierst, dass es bei der Anmeldung automatisch gestartet wird.

Bitte diskutiere über `xdotool` in diesem Diskussions-Thread: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` ist wie `xdotool`, aber für Wayland.

Normalerweise unterstützt es keine Maussteuerung, aber ich habe eine angepasste Version erstellt, die Mausunterstützung hinzufügt. Bitte installiere sie, wenn du Mausunterstützung benötigst: <https://github.com/stefansundin/wtype>

Wenn du den Fehler `Compositor does not support the virtual keyboard protocol` erhältst, schlage ich vor, ein anderes Werkzeug wie `ydotool` zu verwenden.

Bitte diskutiere über `wtype` in diesem Diskussions-Thread: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` sollte ähnlich wie `ydotool` mit jedem Fenstermanager funktionieren. In meinen begrenzten Tests war es jedoch deutlich langsamer als `ydotool`.

Bitte diskutiere über `dotool` in diesem Diskussions-Thread: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Diese Voreinstellung ist experimentell, da ich sie auf meiner eigenen Hardware nicht überprüfen konnte. Feedback ist willkommen.

Bitte diskutiere über `cec-client` in diesem Diskussions-Thread: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Es gibt eine Voreinstellung zur Steuerung von VLC unter macOS mit AppleScript-Befehlen. Mir ist kein Werkzeug bekannt, das das Senden von Tastatur- oder Mausereignissen unterstützt.

Bitte diskutiere über macOS in diesem Diskussions-Thread: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Wenn dein Android-Gerät mit einem SSH-Server ausgeliefert wird, kannst du dich möglicherweise damit verbinden und Eingabeereignisse senden. Das ist wahrscheinlicher, wenn auf deinem Gerät ein Custom-ROM läuft, etwa KonstaKANG auf dem Raspberry Pi.

Ich habe noch nicht herausgefunden, wie sich Mausunterstützung zum Laufen bringen lässt.

Bitte diskutiere über Android in diesem Diskussions-Thread: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Intelligente Lautstärkesteuerung

Beim Bearbeiten der Fernbedienung findest du im Menü Einstellungen für die „intelligente“ Lautstärkesteuerung. Damit kann die aktuelle Lautstärke des Computers in der App angezeigt werden, und du kannst die Lautstärke schnell mit einem Schieberegler einstellen. Außerdem kannst du mit den Hardwaretasten deines Geräts schnell Befehle zum Erhöhen oder Verringern der Lautstärke senden.

Das Auslesen der aktuellen Lautstärke und das Setzen einer neuen Lautstärke per Schieberegler ist derzeit fest auf `pactl` eingestellt.

Das Paket, das `pactl` enthält, heißt normalerweise `pulseaudio-utils` oder `libpulse`.

## SSH-Schlüssel

Du kannst SSH-Schlüssel in den App-Einstellungen importieren oder erstellen. Eine Verbindung mit einem SSH-Schlüssel ist sicherer als die Verwendung von Passwörtern.

Der einfachste Weg, einen vorhandenen SSH-Schlüssel von einem Computer zu importieren, ist das Scannen eines QR-Codes. Du kannst das Programm `qrencode` verwenden, um das QR-Code-Bild zu erzeugen. Führe einen Befehl wie den folgenden aus, um den QR-Code zu erzeugen:

```shell
# Zu deinen SSH-Schlüsseln wechseln:
cd ~/.ssh

# Den QR-Code im Terminal anzeigen:
qrencode -r id_ed25519 -t ansiutf8

# Alternativ eine Bilddatei erstellen:
qrencode -r id_ed25519 -o qr.png

# 4096-Bit-RSA-Schlüssel sind für einen QR-Code zu groß.
# Mit gzip passt einer gerade noch so hinein:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Du kannst öffentliche SSH-Schlüssel mit der Funktion `Öffentlichen Schlüssel übertragen` im Menü auf einen Server übertragen. Dabei wird der ausgewählte SSH-Schlüssel an die Datei `~/.ssh/authorized_keys` angehängt. So kannst du leicht von einer Anmeldung mit Passwort auf eine Anmeldung mit SSH-Schlüssel umsteigen.

Du kannst verschlüsselte SSH-Schlüssel importieren und verwenden, aber derzeit nicht in der App erstellen.

## Sicherheit

Es ist nicht möglich, den privaten Teil von SSH-Schlüsseln oder gespeicherte Passwörter aus der App zu exportieren oder zu extrahieren. Diese Daten werden mit 256-Bit-AES verschlüsselt, und der Verschlüsselungsschlüssel wird im Android-Keystore gespeichert. Verschlüsselte Daten sind von Android-Backups ausgeschlossen.

Es gibt in dieser App keine Crash-Reporting-Software. Es gibt keine Telemetrie. Es gibt keine Werbung. Es gibt keine Netzwerkanfragen außer der SSH-Verbindung.

Die Sicherheit dieser App wurde nicht geprüft. Wenn du Erfahrung mit Android-Sicherheit oder SSH-Sicherheit hast, schau dir bitte den Quellcode an und melde deine Erkenntnisse in diesem GitHub-Issue:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Funktionswünsche

Du kannst gerne Funktionswünsche und Fehlerberichte im GitHub-Repository einreichen. Bitte verwende Englisch. Bitte bleibe höflich. Respektlose Kommentare werden entfernt und Benutzer können aus dem Repository blockiert werden.

Bitte sieh dir bestehende Issues und Diskussions-Threads an, um zu prüfen, ob deine Frage bereits gestellt oder beantwortet wurde.

Bitte sei respektvoll. Ich habe diese App in meiner Freizeit entwickelt und stelle sie kostenlos zur Verfügung. Ich entwickle diese App in erster Linie für meinen eigenen Gebrauch.

Bitte schreibe mir keine E-Mails mit Fragen. Bitte versuche, Unterhaltungen auf GitHub zu führen, da das auch anderen hilft! Du kannst Fragen im Diskussionsbereich auf GitHub stellen.

Du kannst die App jederzeit forken, um eigene Funktionen zu implementieren. Das ist eine großartige Möglichkeit zu lernen. Bitte erwäge, nützliche Funktionen beizutragen.

Der Quellcode ist unter GNU GPLv3 lizenziert. Wenn du veränderte Versionen dieser App verbreitest, musst du auch den Quellcode verfügbar machen.

<https://github.com/stefansundin/SSHRemote>

## Spenden

Wenn du deine Dankbarkeit und Wertschätzung zeigen möchtest, sind Spenden willkommen.

<https://stefansundin.github.io/donate/>

Wenn du gespendet hast, werde ich mein Bestes tun, um jede Frage zu beantworten, die du haben könntest. Bitte schreibe Anfragen auf Englisch.

Vielen Dank für deine Unterstützung!
