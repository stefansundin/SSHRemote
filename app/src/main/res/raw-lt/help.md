## Apie SSH Remote

Šis vertimas buvo sukurtas naudojant GitHub Copilot (modelio informacija šioje aplinkoje nepasiekiama), todėl jame gali būti vertimo netikslumų.

SSH Remote yra nemokama atvirojo kodo programa, leidžianti nuotoliniu būdu valdyti kompiuterius naudojant SSH.

Galite visiškai pritaikyti vykdomas komandas, o dažniausiai pasitaikantiems scenarijams yra paruošti išankstiniai nustatymai.

Aš naudoju šią programą savo HTPC sistemai valdyti, kurioje veikia Raspberry Pi OS. HTPC valdymas yra pagrindinis scenarijus, kuriam ši programa yra optimizuota.

Ši programa nėra terminalo emuliatorius, tačiau kritiniu atveju leis paleisti `apt-get install`.

## Darbo pradžia

Jei jungdamiesi norite naudoti SSH raktą, pirmiausia atidarykite programos nustatymus ir importuokite arba sugeneruokite raktą.

Pridėkite naują kompiuterį bakstelėdami `+` mygtuką apatiniame dešiniajame kampe. Įveskite prisijungimo duomenis ir išsaugokite.

Prisijungdami pirmą kartą būsite paprašyti pasirinkti išankstinį nustatymą. Šis pasirinkimas sukonfigūruoja nuotolinio valdymo pulto mygtukus, kad jie gerai veiktų įvairių tipų kompiuteriuose. Toliau rasite galimų išankstinių nustatymų aprašymą. Jei norite, galite pradėti ir be jokios konfigūracijos, pasirinkę `Be išankstinio nustatymo`.

Jei nežinote, ar jūsų Linux kompiuteryje naudojama X11, ar Wayland, paleiskite tai terminale:

```shell
echo $XDG_SESSION_TYPE
```

Turėtų būti parodyta `x11` arba `wayland`. Šią komandą būtina vykdyti jau paleistoje grafinėje aplinkoje.

## Nuotolinio valdymo pultas

Prisijungę galite naudoti nuotolinio valdymo pulto sąsają komandoms siųsti. Perjunkite skirtukus, kad pasiektumėte įvairius įvesties būdus.

Kiekvienas mygtuko paspaudimas vykdo komandą kompiuteryje. Tai gana didelė apkrova tokiam paprastam veiksmui kaip klavišo paspaudimas, todėl galite jausti nemažą delsą, palyginti su įprasta klaviatūra. Tikiuosi tai patobulinti būsimose versijose.

Naudokite meniu, kad įjungtumėte redagavimo režimą. Šiuo metu išdėstymo ar mygtukų piktogramų redaguoti negalima. Tikiuosi ateityje ir tai padaryti įmanoma.

## Išankstiniai nustatymai

Turėsite įdiegti įrankį, reikalingą jūsų darbalaukio aplinkai.

Rekomenduoju `ydotool`, nes mano bandymuose jis veikė geriausiai ir tinka tiek X11, tiek Wayland.

### ydotool

`ydotool` turėtų veikti su bet kuriuo langų tvarkytuvu, tačiau reikia veikiančios foninės tarnybos. Jei jūsų distribucija pateikia systemd naudotojo tarnybą, paleiskite ją taip:

```shell
systemctl start --user ydotool
```

Kad tarnyba būtų automatiškai paleidžiama prisijungus, vykdykite:

```shell
systemctl enable --user ydotool
```

Įsitikinkite, kad diegiate pakankamai naują `ydotool` versiją. Ubuntu versijose iki 26.04 pateikiamos per senos versijos. Sprendimo būdą rasite diskusijų temoje.

Apie `ydotool` prašome diskutuoti šioje diskusijų temoje: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` skirtas kompiuteriams, kuriuose naudojama X11. Istoriškai dauguma Linux kompiuterių naudojo X11, nors Wayland tampa vis populiaresnė.

Viena X11 ypatybė yra ta, kad gali reikėti suteikti prieigą prie X serverio. Jei gaunate klaidas „Authorization required“, greičiausiai problema būtent tame. Štai du sprendimo būdai, kurie man pasiteisino:

Jei `xauth list` nerodo jokių įrašų, pabandykite sugeneruoti `.Xauthority` failą:

```shell
xauth generate :0 . trusted
```

Jei tai nepadėjo, pabandykite suteikti prieigą naudodami `xhost`:

```shell
xhost +local:$USER
```

`xhost` komandą turėsite paleisti po kiekvieno kompiuterio paleidimo. Tai galite automatizuoti sukūrę bash scenarijų ir nustatę, kad jis būtų paleidžiamas automatiškai prisijungus.

Apie `xdotool` prašome diskutuoti šioje diskusijų temoje: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` yra panašus į `xdotool`, tik skirtas Wayland.

Paprastai jis nepalaiko pelės valdymo, tačiau aš sukūriau modifikuotą versiją, kuri prideda pelės palaikymą. Jei jums reikia pelės valdymo, įdiekite ją: <https://github.com/stefansundin/wtype>

Jei gaunate klaidą `Compositor does not support the virtual keyboard protocol`, siūlau išbandyti kitą įrankį, pavyzdžiui, `ydotool`.

Apie `wtype` prašome diskutuoti šioje diskusijų temoje: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` turėtų veikti su bet kuriuo langų tvarkytuvu, panašiai kaip `ydotool`. Mano ribotuose bandymuose jis buvo gerokai lėtesnis už `ydotool`.

Apie `dotool` prašome diskutuoti šioje diskusijų temoje: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Šis išankstinis nustatymas yra eksperimentinis, nes neturėjau galimybės jo patikrinti savo įrangoje. Atsiliepimai laukiami.

Apie `cec-client` prašome diskutuoti šioje diskusijų temoje: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Yra išankstinis nustatymas VLC valdymui macOS sistemoje, naudojant AppleScript komandas. Nežinau įrankio, kuris leistų siųsti klaviatūros ar pelės įvykius.

Apie macOS prašome diskutuoti šioje diskusijų temoje: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Jei jūsų Android įrenginyje yra SSH serveris, galbūt galėsite prie jo prisijungti ir siųsti įvesties įvykius. Tai labiau tikėtina, jei įrenginyje naudojama pasirinktinė ROM, pavyzdžiui, KonstaKANG Raspberry Pi įrenginiuose.

Kol kas nesu išsiaiškinęs, kaip priversti veikti pelės palaikymą.

Apie Android prašome diskutuoti šioje diskusijų temoje: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Išmanusis garsumo valdymas

Redaguodami nuotolinio valdymo pultą meniu rasite „išmaniuosius“ garsumo valdymo nustatymus. Jie gali rodyti dabartinį kompiuterio garsumo lygį programoje ir leisti greitai nustatyti garsumą slankikliu. Taip pat galite naudoti savo įrenginio aparatinius mygtukus, kad greitai išsiųstumėte garsinimo / tildymo komandas.

Dabartinio garsumo nuskaitymas ir naujo garsumo nustatymas slankikliu šiuo metu yra standžiai susieti su `pactl`.

Paketas, kuriame yra `pactl`, dažniausiai vadinamas `pulseaudio-utils` arba `libpulse`.

## SSH raktai

Programos nustatymuose galite importuoti arba sugeneruoti SSH raktus. Prisijungti naudojant SSH raktą yra saugiau nei naudojant slaptažodžius.

Lengviausias būdas importuoti esamą SSH raktą iš kompiuterio – nuskaityti QR kodą. QR kodo vaizdui sugeneruoti galite naudoti programą `qrencode`. Paleiskite tokią komandą:

```shell
# Pereikite į savo SSH raktų katalogą:
cd ~/.ssh

# Parodykite QR kodą terminale:
qrencode -r id_ed25519 -t ansiutf8

# Arba sukurkite paveikslėlio failą:
qrencode -r id_ed25519 -o qr.png

# 4096 bitų RSA raktai yra per dideli QR kodui. Naudodami gzip galite vos sutalpinti tokį raktą:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Naudodami meniu funkciją `Nusiųsti viešąjį raktą`, galite nusiųsti viešuosius SSH raktus į serverį. Taip pasirinktas SSH raktas bus pridėtas prie `~/.ssh/authorized_keys` failo. Tai leidžia lengvai pereiti nuo prisijungimo slaptažodžiu prie prisijungimo SSH raktu.

Galite importuoti ir naudoti užšifruotus SSH raktus, tačiau šiuo metu programoje jų sugeneruoti negalima.

## Saugumas

Iš programos neįmanoma eksportuoti ar kitaip išgauti privačiosios SSH raktų dalies arba išsaugotų slaptažodžių. Šie duomenys šifruojami naudojant 256 bitų AES, o šifravimo raktas saugomas Android Keystore. Užšifruoti duomenys neįtraukiami į Android atsargines kopijas.

Šioje programoje nėra jokios strigčių ataskaitų siuntimo programinės įrangos. Nėra telemetrijos. Nėra reklamų. Nėra tinklo užklausų, išskyrus SSH ryšį.

Šios programos saugumas nebuvo audituotas. Jei turite Android ar SSH saugumo patirties, pažvelkite į šaltinio kodą ir praneškite savo išvadas šiame GitHub issue:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Funkcijų užklausos

Drąsiai siųskite funkcijų užklausas ir pranešimus apie klaidas GitHub saugykloje. Prašome naudoti anglų kalbą. Prašome išlikti mandagiems. Nepagarbūs komentarai bus pašalinti, o naudotojai gali būti užblokuoti saugykloje.

Prašome peržiūrėti esamas issues ir diskusijų temas, kad pamatytumėte, ar į jūsų klausimą jau nebuvo paklausta arba atsakyta.

Prašome būti pagarbūs. Šią programą sukūriau savo laisvalaikiu ir atiduodu ją nemokamai. Šią programą pirmiausia kuriu savo reikmėms.

Prašome nesiųsti man klausimų el. paštu. Stenkitės pokalbius palikti GitHub, nes tai padeda ir kitiems žmonėms! Klausimus galite užduoti GitHub diskusijų skiltyje.

Visada galite išsišakoti programą ir įgyvendinti savo funkcijas. Tai puikus būdas mokytis. Apsvarstykite galimybę prisidėti naudingomis funkcijomis.

Šaltinio kodas licencijuojamas pagal GNU GPLv3. Jei platinate modifikuotas šios programos versijas, privalote taip pat pateikti ir šaltinio kodą.

<https://github.com/stefansundin/SSHRemote>

## Aukos

Jei norite parodyti savo dėkingumą ir palaikymą, aukos yra priimamos.

<https://stefansundin.github.io/donate/>

Jei paaukojote, aš pasistengsiu kiek įmanoma geriau atsakyti į bet kokį jūsų klausimą. Prašome visus paklausimus rašyti anglų kalba.

Ačiū už jūsų palaikymą!
