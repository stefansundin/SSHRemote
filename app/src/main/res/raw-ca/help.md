## Quant a SSH Remote

Aquesta traducció ha estat generada per IA amb GitHub Copilot i pot contenir errors de traducció.

SSH Remote és una aplicació gratuïta i de codi obert que us permet controlar ordinadors remotament mitjançant SSH.

Podeu personalitzar completament les ordres que s'executen, i hi ha preajustos per a configuracions habituals.

Jo faig servir aquesta aplicació per controlar la meva configuració HTPC, que funciona amb Raspberry Pi OS. Controlar un HTPC és l'escenari bàsic per al qual l'aplicació està optimitzada.

Aquesta aplicació no és un emulador de terminal, però us permetrà executar `apt-get install` en cas d'emergència.

## Com començar

Si voleu fer servir una clau SSH per connectar-vos, primer obriu la configuració de l'aplicació i importeu o genereu una clau.

Afegiu un host nou tocant el botó `+` a la cantonada inferior dreta. Introduïu les dades de connexió i deseu-les.

La primera vegada que us connecteu, se us demanarà que seleccioneu un preajust. Aquesta selecció configura els botons del control remot perquè funcionin bé en diferents tipus d'ordinadors. Vegeu més avall una descripció dels preajustos disponibles. Si voleu, podeu començar sense configuració seleccionant `Sense preajust`.

Si no sabeu si el vostre ordinador Linux fa servir X11 o Wayland, executeu això en un terminal:

```shell
echo $XDG_SESSION_TYPE
```

Això hauria de mostrar `x11` o `wayland`. Ho heu d'executar dins de l'entorn d'escriptori.

## Control remot

Un cop connectat, podeu fer servir la interfície de control remot per enviar ordres. Canvieu de pestanya per accedir a diferents mètodes d'entrada.

Cada pulsació de botó executarà una ordre al host. Això afegeix molta càrrega per a una cosa tan simple com una pulsació de tecla, i és possible que noteu una latència bastant elevada en comparació amb un teclat normal. Espero millorar-ho en versions futures.

Feu servir el menú per entrar al mode d'edició. Actualment no és possible editar la disposició ni les icones dels botons. Espero fer-ho possible en una versió futura.

## Preajustos

Haureu d'instal·lar l'eina necessària per al vostre entorn d'escriptori.

Recomano `ydotool` perquè, segons les meves proves, té el millor rendiment i funciona tant a X11 com a Wayland.

### ydotool

`ydotool` hauria de funcionar amb qualsevol gestor de finestres, però cal que hi hagi un servei en segon pla en execució. Si la vostra distribució proporciona un servei d'usuari de systemd, inicieu-lo amb:

```shell
systemctl start --user ydotool
```

Per iniciar automàticament el servei en iniciar sessió, executeu:

```shell
systemctl enable --user ydotool
```

Assegureu-vos que instal·leu una versió prou recent de `ydotool`. Les versions d'Ubuntu anteriors a 26.04 proporcionen versions massa antigues. Consulteu el fil de la discussió per trobar una solució.

Si us plau, parleu de `ydotool` en aquest fil de discussió: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` és per a ordinadors que executen X11. X11 és el que històricament han fet servir la majoria d'ordinadors Linux, tot i que Wayland és cada vegada més popular.

Una particularitat de X11 és que potser haureu de permetre l'accés al servidor X. Aquest és el problema si rebeu errors de "Authorization required". Teniu algunes opcions per solucionar aquest problema; aquestes són dues que m'han funcionat:

Si `xauth list` no mostra cap entrada, proveu de generar un fitxer `.Xauthority`:

```shell
xauth generate :0 . trusted
```

Si això no funciona, proveu de concedir accés amb `xhost`:

```shell
xhost +local:$USER
```

Haureu d'executar l'ordre `xhost` després de cada arrencada. Podeu automatitzar-ho creant un script de bash i configurant-lo perquè s'iniciï automàticament en iniciar sessió.

Si us plau, parleu de `xdotool` en aquest fil de discussió: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` és com `xdotool`, però per a Wayland.

Normalment no admet el control del ratolí, però he creat una versió modificada que hi afegeix suport. Si necessiteu suport per al ratolí, instal·leu-la: <https://github.com/stefansundin/wtype>

Si obteniu l'error `Compositor does not support the virtual keyboard protocol`, us suggereixo que proveu una altra eina, com ara `ydotool`.

Si us plau, parleu de `wtype` en aquest fil de discussió: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` hauria de funcionar amb qualsevol gestor de finestres, de manera similar a `ydotool`. En les meves proves limitades era molt més lent que `ydotool`.

Si us plau, parleu de `dotool` en aquest fil de discussió: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Aquest preajust és experimental, ja que no he pogut verificar-lo amb el meu propi maquinari. Qualsevol comentari serà benvingut.

Si us plau, parleu de `cec-client` en aquest fil de discussió: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Hi ha un preajust per controlar VLC a macOS, fent servir ordres d'AppleScript. No conec cap eina que admeti l'enviament d'esdeveniments de teclat o ratolí.

Si us plau, parleu de macOS en aquest fil de discussió: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Si el vostre dispositiu Android incorpora un servidor SSH, potser podreu connectar-vos-hi i enviar-hi esdeveniments d'entrada. Això és més probable si el dispositiu executa una ROM personalitzada, com ara KonstaKANG al Raspberry Pi.

No he aconseguit fer funcionar el suport del ratolí.

Si us plau, parleu d'Android en aquest fil de discussió: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Control intel·ligent del volum

Quan editeu el control remot, podeu trobar al menú la configuració de control de volum "intel·ligent". Això pot mostrar el volum actual de l'ordinador a l'aplicació i permetre-us ajustar-lo ràpidament amb un control lliscant. També podeu utilitzar els botons físics del dispositiu per enviar ràpidament ordres de pujar/baixar volum.

La lectura del volum actual i l'establiment d'un volum nou mitjançant el control lliscant estan actualment codificats per fer servir `pactl`.

El paquet que conté `pactl` normalment s'anomena `pulseaudio-utils` o `libpulse`.

## Claus SSH

Podeu importar o generar claus SSH a la configuració de l'aplicació. Connectar-vos amb una clau SSH és més segur que fer servir contrasenyes.

La manera més fàcil d'importar una clau SSH existent des d'un ordinador és escanejant un codi QR. Podeu utilitzar el programa `qrencode` per generar la imatge del codi QR. Executeu una ordre com la següent per generar el codi QR:

```shell
# Aneu a les vostres claus SSH:
cd ~/.ssh

# Mostreu el codi QR al terminal:
qrencode -r id_ed25519 -t ansiutf8

# Com a alternativa, creeu un fitxer d'imatge:
qrencode -r id_ed25519 -o qr.png

# Les claus RSA de 4096 bits són massa grans per a un codi QR. Podeu fer servir gzip per fer-la encabir just:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Podeu enviar claus SSH públiques a un servidor fent servir la funció `Envia la clau pública` del menú. Això afegirà la clau SSH seleccionada al fitxer `~/.ssh/authorized_keys`. D'aquesta manera podeu passar fàcilment d'iniciar sessió amb una contrasenya a fer-ho amb una clau SSH.

Podeu importar i fer servir claus SSH xifrades, però actualment no podeu generar-ne dins de l'aplicació.

## Seguretat

No és possible exportar ni extreure la part privada de les claus SSH, ni les contrasenyes desades, de l'aplicació. Aquestes dades s'encripten amb AES de 256 bits, i la clau d'encriptació es desa al Android Keystore. Les dades encriptades queden excloses de les còpies de seguretat d'Android.

No hi ha programari d'informes de fallades en aquesta aplicació. No hi ha telemetria. No hi ha anuncis. No hi ha peticions de xarxa excepte la connexió SSH.

La seguretat d'aquesta aplicació no ha estat auditada. Si teniu experiència amb la seguretat d'Android o amb la seguretat SSH, feu una ullada al codi font i comuniqueu els vostres resultats en aquest problema de GitHub:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Sol·licituds de funcions

No dubteu a enviar sol·licituds de funcions i informes d'errors al repositori de GitHub. Si us plau, feu servir l'anglès. Si us plau, manteniu els vostres comentaris amb civisme. Els comentaris irrespectuosos s'eliminaran i els usuaris poden ser bloquejats del repositori.

Si us plau, mireu els problemes i fils de discussió existents per veure si la vostra pregunta ja s'ha fet o s'ha respost.

Si us plau, segueu respectuosos. He treballat en aquesta aplicació en el meu temps lliure i l'ofereixo de franc. La faig, abans que res, per al meu propi ús.

Si us plau, no m'envieu preguntes per correu electrònic. Si us plau, intenteu mantenir les converses a GitHub, ja que així ajudeu també altres persones. Podeu fer preguntes a la secció de discussions de GitHub.

Sou sempre benvinguts a bifurcar l'aplicació per implementar les vostres pròpies funcions. És una manera fantàstica d'aprendre. Si us plau, considereu contribuir amb funcions útils.

El codi font està llicenciat sota la GNU GPLv3. Si distribuïu versions modificades d'aquesta aplicació, també heu de fer disponible el codi font.

<https://github.com/stefansundin/SSHRemote>

## Donacions

Si voleu mostrar el vostre agraïment i apreciació, s'accepten donacions.

<https://stefansundin.github.io/donate/>

Si heu fet una donació, faré tot el possible per respondre qualsevol pregunta que pugueu tenir. Si us plau, escriviu qualsevol consulta en anglès.

Gràcies pel vostre suport!
