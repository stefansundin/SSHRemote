## Davart SSH Remote

Questa translaziun è vegnida creada cun agid da GitHub Copilot (GPT-5) e po cuntegnair sbagls da translaziun.

SSH Remote è ina app gratuita e open source che ta permetta da controllar computers a distanza cun SSH.

Ti pos persunalisar cumplettamain ils cumonds che vegnan exequids, ed i dat presets per configuraziuns usitadas.

Jau dovr questa app per controllar mia configuraziun HTPC, che funcziuna cun Raspberry Pi OS. Il control d'in HTPC è il scenari principal per il qual l'app è optimada.

Questa app n'è betg in emulatur da terminal, ma en in cas urgent ta lascha ella exequir `apt-get install`.

## Cumenzar

Sche ti vuls duvrar ina clav SSH per connectar, avra l'emprim ils parameters da l'app ed importescha u generescha ina clav.

Agiunta in nov host cun tocar il buttun `+` en la chantunada inferiura a dretga. Endatescha las novitads da connexiun e memorisescha.

L'emprima giada che ti ta connectas, vegns ti dumandà da tscherner in preset. Questa selecziun configurescha ils buttuns da la telecomonda uschia ch'els funcziunan bain cun differents tips da computers. Guarda sutvart per ina descripziun dals presets disponibels. Sche ti prefereschas, pos ti cumenzar senza configuraziun cun tscherner `Nagin preset`.

Sche ti na sas betg sche tes computer Linux utilisescha X11 u Wayland, exequescha quai en in terminal:

```shell
echo $XDG_SESSION_TYPE
```

Quai duai returnar `x11` u `wayland`. Ti stos exequir quai entaifer l'ambient da desktop.

## Telecomonda

Ina giada connectà, pos ti duvrar l'interfatscha da la telecomonda per trametter cumonds. Mida tabs per acceder a differents metods d'endataziun.

Mintga pressiun d'in buttun exequescha in cumond sin il host. Quai è bler overhead per insatge uschè simpel sco ina pressiun da tasta, e ti pudessas experimentar ina latenza plitost auta cumpareglià cun ina tastatura normala. Jau sper da meglierar quai en futuras versiuns.

Dovra il menu per entrar en il modus da modifitgar. Actualmain n'èsi betg pussaivel da modifitgar il layout u las iconas dals buttuns. Jau sper da render quai pussaivel en ina futura versiun.

## Presets

Ti stos installar l'utensil necessari per tes ambient da desktop.

Jau recumond `ydotool` perquai che quel ha gì la meglra prestaziun en mes tests e funcziuna tant cun X11 sco cun Wayland.

### ydotool

`ydotool` duai funcziunar cun mintga manager da fanestra, ma ti dovras in servetsch da fundal activ. Sche tia distribuziun porscha in servetsch systemd per utilisaders, al avra cun exequir:

```shell
systemctl start --user ydotool
```

Per avviar automaticamain il servetsch a la connexiun, exequescha:

```shell
systemctl enable --user ydotool
```

Per plaschair controllescha che ti installeschias ina versiun suffizientamain nova da `ydotool`. Versiuns d'Ubuntu avant 26.04 porschan versiuns memia veglias. Guarda il fil da discussiun per ina schliaziun alternativa.

Discutescha `ydotool` en quest fil da discussiun: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` è per computers che funcziunan cun X11. X11 è quai che la gronda part dals computers Linux ha duvrà istoricamain, er sche Wayland daventa pli popular.

Ina particularitad da X11 è che ti stos eventualmain lubir l'access al server X. Quai è probablamain il problem sche ti survegns errors `Authorization required`. Ti has pliras pussaivladads per schliar quest problem; qua èn duas variantas che han funcziunà tar mai:

Sche `xauth list` na mussa naginas endataziuns, emprova da generar in datotec `.Xauthority`:

```shell
xauth generate :0 . trusted
```

Sche quai n'ha betg funcziunà, emprova da conceder access cun `xhost`:

```shell
xhost +local:$USER
```

Ti stos exequir il cumond `xhost` suenter mintga reinizialisaziun. Ti pos automatizar quai cun crear in script bash e configurar el per vegnir avvià automaticamain a la connexiun.

Discutescha `xdotool` en quest fil da discussiun: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` è sco `xdotool`, dentant per Wayland.

Normalmain na sustegna el betg il control da la mieur, ma jau hai creà ina versiun modifitgada che agiunta sustegn per la mieur. Per plaschair installescha quella, sche ti dovras sustegn per la mieur: <https://github.com/stefansundin/wtype>

Sche ti survegns l'errur `Compositor does not support the virtual keyboard protocol`, ta recumond jau d'empruvar in auter utensil, sco `ydotool`.

Discutescha `wtype` en quest fil da discussiun: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` duai funcziunar cun mintga manager da fanestra, sumegliant a `ydotool`. En mes tests limitads era el dentant bler pli plaun che `ydotool`.

Discutescha `dotool` en quest fil da discussiun: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Quest preset è experimental, perquai che jau n'hai betg pudì verifitgar el cun mes agen hardware. Feedback è bainvegni.

Discutescha `cec-client` en quest fil da discussiun: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

I dat in preset per controllar VLC sin macOS, cun duvrar cumonds AppleScript. Jau n'enconusch nagin utensil che sustegnia da trametter novitads da tastatura u da mieur.

Discutescha macOS en quest fil da discussiun: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Sche tes apparat Android vegn cun in server SSH, lura pudessas ti esser abel da ta connectar ad el e trametter novitads d'input. Quai è pli probabel sche tes apparat funcziuna cun ina custom ROM, sco KonstaKANG sin il Raspberry Pi.

Jau n'hai betg anc chattà co far funcziunar il sustegn per la mieur.

Discutescha Android en quest fil da discussiun: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Control da volumen intelligent

Cura che ti modifitgeschas la telecomonda, pos ti chattar novitads per in control da volumen "intelligent" en il menu. Quai po mussar en l'app il volumen actual dal computer e ta permetter da definir svelt il volumen cun in regulader. Ti pos er duvrar ils buttuns da hardware da tes apparat per trametter svelt cumonds da volumen si/giu.

La lectura dal volumen actual e la definiziun d'in nov volumen cun il regulader èn actualmain fixadas per duvrar `pactl`.

Il pachet che cuntegna `pactl` sa numna savens `pulseaudio-utils` u `libpulse`.

## Clavs SSH

Ti pos importar u generar clavs SSH en ils parameters da l'app. La connexiun cun ina clav SSH è pli segira che cun pleds-clav.

Il pli simpel per importar ina clav SSH existenta d'in computer è da scannar in code QR. Ti pos duvrar il program `qrencode` per generar l'imagina dal code QR. Exequescha in cumond sco il suandant per generar il code QR:

```shell
# Vai a tes clavs SSH:
cd ~/.ssh

# Mussa il code QR en il terminal:
qrencode -r id_ed25519 -t ansiutf8

# Alternativamain, crea in datotec da maletg:
qrencode -r id_ed25519 -o qr.png

# Clavs RSA da 4096 bits èn memia grondas per in code QR. Cun gzip pos ti gist far ch'ina entri:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Ti pos transferir clavs SSH publicas ad in server cun la funcziun `Transferir la clav publica` en il menu. Quai agiunta la clav SSH tschernida al datotec `~/.ssh/authorized_keys`. Uschia pos ti midar facilmente da l'annunzia cun pled-clav a l'annunzia cun clav SSH.

Ti pos importar e duvrar clavs SSH criptadas, ma actualmain na pos ti betg generar quellas en l'app.

## Segirezza

Igl è betg pussaivel d'exportar u d'extrair la part privata da clavs SSH u da pleds-clav memorisads da l'app. Questas datas vegnan criptadas cun AES da 256 bits, e la clav da criptaziun vegn memorisada en il Android Keystore. Datas criptadas vegnan exclusas da copias da segirezza Android.

En questa app na datti nagin software per rapportar crashes. I na dat nagina telemetria. I na dat naginas novitads publicitaras. I na dat naginas novitads da rait autras che la connexiun SSH.

La segirezza da questa app n'è betg vegnida auditada. Sche ti has novitads d'Android security u SSH security, fa per plaschair in sguard sin il code funtauna e rapporta tias novitads en quest issue da GitHub:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Giavischs da novitads

Ta senta liber da trametter giavischs per novitads e rapports da bug en il repository GitHub. Per plaschair utilisescha l'englais. Per plaschair tegna tes commentaris civils. Commentaris manchants vegnan allontanads, ed utilisaders pon vegnir bloccads dal repository.

Per plaschair guarda tras issues e fils da discussiun existents per vesair sche tia dumonda è gia vegnida fatga u respundida.

Per plaschair sa cumporta cun respect. Jau hai construì questa app en mes temp liber e jau la met a disposiziun gratuitamain. Jau sviluppesch questa app surtut per mes agen diever.

Per plaschair na ma trametta betg dumondas per e-mail. Emprova da tegnair las novitads sin GitHub, perquai che quai gida er autras persunas. Ti pos far dumondas en la secziun da discussiun sin GitHub.

Ti es adina bainvegni da forkir l'app per implementar tias atgnas novitads. Quai è ina fitg buna via per emprender. Per plaschair resguarda da contribuir novitads utilas.

Il code funtauna è sut licenza GNU GPLv3. Sche ti distribueschas versiuns modifitgadas da questa app, stos ti era metter a disposiziun il code funtauna.

<https://github.com/stefansundin/SSHRemote>

## Donaziuns

Sche ti vuls mussar tia gratitud e stima, vegnan donaziuns acceptadas.

<https://stefansundin.github.io/donate/>

Sche ti has fatg ina donaziun, empruvarai jau da respunder uschè bain sco pussaivel a mintga dumonda che ti has. Per plaschair scriva dumondas en englais.

Grazia fitg per tes sustegn!
