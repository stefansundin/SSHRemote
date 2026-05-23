## SSH Remote-ri buruz

Itzulpen hau AI bidez egin da, GitHub Copilot (GPT-5-Codex) erabiliz. Itzulpen-akatsak egon daitezke.

SSH Remote doako eta kode irekiko aplikazio bat da, SSH erabiliz ordenagailuak urrunetik kontrolatzeko.

Exekutatzen diren komandoak guztiz pertsonaliza ditzakezu, eta ohiko konfigurazioetarako aurrezarpenak daude.

Nik aplikazio hau nire HTPC konfigurazioa kontrolatzeko erabiltzen dut; Raspberry Pi OS darabil. HTPC bat kontrolatzea da aplikazio hau optimizatuta dagoen oinarrizko agertokia.

Aplikazio hau ez da terminal-emulatzaile bat, baina larrialdi batean `apt-get install` exekutatzen utziko dizu.

## Hasteko

SSH gako bat erabili nahi baduzu konektatzeko, lehenik ireki aplikazioaren ezarpenak eta inportatu edo sortu gako bat.

Gehitu ostalari berri bat beheko eskuin aldeko `+` botoia sakatuz. Sartu konexioaren xehetasunak eta gorde.

Lehen aldiz konektatzen zarenean, aurrezarpen bat hautatzeko eskatuko zaizu. Hautapen horrek urruneko kontrolaren botoiak hainbat ordenagailu motatan ondo funtzionatzeko konfiguratzen ditu. Behean aurkituko duzu aurrezarpen erabilgarrien azalpena. Nahiago baduzu, konfiguraziorik gabe hasi zaitezke `Aurrezarpenik ez` hautatuta.

Ez badakizu zure Linux ordenagailua X11 ala Wayland exekutatzen ari den, exekutatu hau terminal batean:

```shell
echo $XDG_SESSION_TYPE
```

Honek `x11` edo `wayland` eman behar du. Hau mahaigaineko ingurune barruan exekutatu behar duzu.

## Urruneko kontrola

Konektatu ondoren, urruneko kontrolaren interfazea erabil dezakezu komandoak bidaltzeko. Aldatu fitxaz sarrera-metodo desberdinak atzitzeko.

Botoi-sakatze bakoitzak komando bat exekutatuko du ostalarian. Horrek gainkarga handia du tekla-sakatzea bezain sinplea den zerbaitentzat, eta teklatu arrunt batekin alderatuta latentzia nahiko handia nabaritu dezakezu. Espero dut etorkizuneko bertsioetan hau hobetzea.

Erabili menua edizio moduan sartzeko. Une honetan ezin da diseinua edo botoien ikonoak editatu. Etorkizuneko bertsio batean hau posible egitea espero dut.

## Aurrezarpenak

Zure mahaigaineko ingurunerako behar den tresna instalatu beharko duzu.

Nik `ydotool` gomendatzen dut, nire probetan errendimendurik onena duelako eta X11 zein Wayland-en funtzionatzen duelako.

### ydotool

`ydotool`-ek edozein leiho-kudeatzailerekin funtzionatu beharko luke, baina atzeko planoko zerbitzu bat behar duzu martxan. Zure banaketak systemd erabiltzaile-zerbitzua badu, abiatu honela:

```shell
systemctl start --user ydotool
```

Saioa hastean zerbitzua automatikoki abiatzeko:

```shell
systemctl enable --user ydotool
```

Mesedez, ziurtatu `ydotool`-en bertsio nahiko berria instalatzen ari zarela. Ubuntu 26.04 baino lehenagoko bertsioek zaharregiak diren bertsioak ematen dituzte. Ikusi eztabaida-haria konponbide baterako.

Mesedez, eztabaidatu `ydotool` honako harian: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` X11 exekutatzen duten ordenagailuetarako da. X11 da historikoki Linux ordenagailu gehienek erabili dutena, nahiz eta Wayland gero eta ezagunagoa den.

X11ren berezitasun bat da balitekeela X zerbitzarirako sarbidea baimendu behar izatea. Hau da arazoa "Authorization required" erroreak jasotzen badituzu. Arazo hau konpontzeko hainbat aukera dituzu; hona hemen niri funtzionatu didaten bi aukera:

`xauth list`-ek ez badu sarrerarik erakusten, saiatu `.Xauthority` fitxategi bat sortzen:

```shell
xauth generate :0 . trusted
```

Honek ez badu funtzionatu, saiatu sarbidea ematen `xhost` erabiliz:

```shell
xhost +local:$USER
```

`xhost` komandoa abio bakoitzaren ondoren exekutatu beharko duzu. Hau automatiza dezakezu bash script bat sortuz eta saioa hastean automatikoki exekutatzeko konfiguratuz.

Mesedez, eztabaidatu `xdotool` honako harian: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` `xdotool` bezalakoa da, baina Wayland-erako.

Normalean, ez du sagu-kontrola onartzen, baina nik bertsio aldatu bat sortu dut saguaren euskarria gehitzen duena. Instalatu mesedez saguaren euskarria behar baduzu: <https://github.com/stefansundin/wtype>

`Compositor does not support the virtual keyboard protocol` errorea jasotzen baduzu, beste tresna bat probatzea gomendatzen dizut, adibidez `ydotool`.

Mesedez, eztabaidatu `wtype` honako harian: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool`-ek edozein leiho-kudeatzailerekin funtzionatu beharko luke, `ydotool`-en antzera. Nik egin ditudan proba mugatuetan `ydotool` baino askoz motelagoa izan da.

Mesedez, eztabaidatu `dotool` honako harian: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Aurrezarpen hau esperimentala da, ezin izan baitut nire hardwarean egiaztatu. Iritziak ongi etorriak dira.

Mesedez, eztabaidatu `cec-client` honako harian: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

macOS-en VLC kontrolatzeko aurrezarpen bat dago, AppleScript komandoak erabiliz. Ez dut ezagutzen teklatu edo sagu gertaerak bidaltzea onartzen duen tresnarik.

Mesedez, eztabaidatu macOS honako harian: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Zure Android gailuak SSH zerbitzari bat badakar, baliteke bertara konektatu eta sarrerako gertaerak bidali ahal izatea. Hau litekeena da zure gailuak ROM pertsonalizatu bat badarabil, adibidez Raspberry Pi-rako KonstaKANG.

Ez dut oraindik asmatu nola lortu saguarentzako euskarria.

Mesedez, eztabaidatu Android honako harian: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Bolumen adimendunaren kontrola

Urruneko kontrola editatzean, menuan "smart" bolumen-kontrolaren ezarpenak aurki ditzakezu. Honek ordenagailuaren uneko bolumena aplikazioan erakutsi dezake eta graduatzaile batekin azkar ezartzen utzi. Gailuaren hardware botoiak ere erabil ditzakezu bolumena igo/jaitsi komandoak azkar bidaltzeko.

Uneko bolumena irakurtzea eta graduatzailearekin bolumen berria ezartzea gaur egun `pactl` erabiltzera lotuta dago.

`pactl` daukan paketea normalean `pulseaudio-utils` edo `libpulse` deitzen da.

## SSH gakoak

Aplikazioaren ezarpenetan SSH gakoak inportatu edo sor ditzakezu. SSH gakoarekin konektatzea pasahitzak erabiltzea baino seguruagoa da.

Lehendik dagoen SSH gako bat ordenagailu batetik inportatzeko modurik errazena QR kode bat eskaneatzea da. `qrencode` programa erabil dezakezu QR kodearen irudia sortzeko. Exekutatu honelako komando bat QR kodea sortzeko:

```shell
# Joan zure SSH gakoetara:
cd ~/.ssh

# Erakutsi QR kodea terminalean:
qrencode -r id_ed25519 -t ansiutf8

# Bestela, sortu irudi-fitxategi bat:
qrencode -r id_ed25519 -o qr.png

# 4096 biteko RSA gakoak handiegiak dira QR kode baterako. gzip erabil dezakezu estu-estu sartzeko:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Gako publikoak zerbitzari batera bidal ditzakezu menuko `Bidali gako publikoa` funtzioarekin. Honek hautatutako SSH gakoa `~/.ssh/authorized_keys` fitxategiari amaieran erantsiko dio. Horri esker erraz migra dezakezu pasahitz bidezko saio-hasieratik SSH gako bidezkora.

Zifratutako SSH gakoak inportatu eta erabil ditzakezu, baina une honetan ezin dituzu aplikazioan sortu.

## Segurtasuna

Ezin da SSH gakoen zati pribatua edo gordetako pasahitzak esportatu edo atera aplikaziotik. Datu hau 256 biteko AES erabiliz zifratzen da, eta zifratze-gakoa Android Keystore-an gordetzen da. Datu zifratua Androiden babeskopietatik kanpo uzten da.

Aplikazio honetan ez dago kraskatze-txostenetarako softwarerik. Ez dago telemetriarik. Ez dago iragarkirik. Ez dago sare-eskaririk SSH konexioa izan ezik.

Aplikazio honen segurtasuna ez da auditatu. Android segurtasunean edo SSH segurtasunean esperientzia baduzu, begiratu mesedez iturburu-kodea eta jakinarazi zure aurkikuntzak GitHub issue honetan:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Funtzio-eskaerak

Lasai bidali funtzio-eskaerak eta errore-txostenak GitHub biltegian. Mesedez, erabili ingelesa. Mesedez, mantendu zure iruzkinak adeitsu. Errespeturik gabeko iruzkinak kendu egingo dira eta erabiltzaileak biltegitik blokeatuak izan daitezke.

Begiratu lehendik dauden issue eta eztabaida-hariak zure galdera dagoeneko eginda edo erantzunda dagoen ikusteko.

Izan errespetuzkoa, mesedez. Aplikazio hau nire denbora librean eraiki dut eta doan ematen ari naiz. Aplikazio hau nire erabilera pertsonalerako eraikitzen ari naiz, lehenik eta behin.

Mesedez, ez bidali galderarik posta elektronikoz. Saiatu elkarrizketak GitHub-en mantentzen, horrek beste pertsonei ere laguntzen dielako. Galderak egin ditzakezu GitHub-eko eztabaiden atalean.

Beti duzu aplikazioa fork egiteko aukera, zure eginbide propioak inplementatzeko. Ikasteko modu bikaina da. Mesedez, kontuan hartu funtzio erabilgarriak ekarpen gisa bidaltzea.

Iturburu-kodea GNU GPLv3 lizentziapean dago. Aplikazio honen bertsio aldatuak banatzen badituzu, iturburu-kodea ere eskuragarri jarri behar duzu.

<https://github.com/stefansundin/SSHRemote>

## Dohaintzak

Eskerrona eta estimua adierazi nahi baduzu, dohaintzak onartzen dira.

<https://stefansundin.github.io/donate/>

Dohaintza egin baduzu, ahal dudan onena egingo dut izan dezakezun edozein galderari erantzuteko. Mesedez, bidali kontsultak ingelesez.

Eskerrik asko zure babesagatik!
