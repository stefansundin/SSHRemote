## Tietoja SSH Remotesta

Tämä suomennos on tehty tekoälyn avulla GitHub Copilotilla (GPT-5.3-Codex). Käännöksessä voi olla virheitä.

SSH Remote on ilmainen ja avoimen lähdekoodin sovellus, jolla voit ohjata tietokoneita etänä SSH:n avulla.

Voit mukauttaa täysin suoritettavat komennot, ja yleisiä käyttötapoja varten on valmiita esiasetuksia.

Käytän tätä sovellusta HTPC-järjestelmäni ohjaamiseen, jossa on käytössä Raspberry Pi OS. HTPC:n ohjaaminen on perusskenaario, jota varten sovellus on ensisijaisesti optimoitu.

Tämä sovellus ei ole pääte-emulaattori, mutta hätätilanteessa sillä voi suorittaa komennon `apt-get install`.

## Aloittaminen

Jos haluat käyttää SSH-avainta yhteyden muodostamiseen, avaa ensin sovelluksen asetukset ja tuo tai luo avain.

Lisää uusi isäntä napauttamalla oikeassa alakulmassa olevaa `+`-painiketta. Syötä yhteystiedot ja tallenna.

Kun yhdistät ensimmäisen kerran, sinua pyydetään valitsemaan esiasetus. Tämä valinta määrittää kaukosäätimen painikkeet toimimaan hyvin erilaisten tietokoneiden kanssa. Katso alta kuvaus saatavilla olevista esiasetuksista. Halutessasi voit aloittaa ilman määrityksiä valitsemalla `Ei esiasetusta`.

Jos et tiedä, käyttääkö Linux-tietokoneesi X11:tä vai Waylandia, suorita päätteessä seuraava komento:

```shell
echo $XDG_SESSION_TYPE
```

Tuloksen pitäisi olla `x11` tai `wayland`. Komento täytyy suorittaa työpöytäympäristön sisällä.

## Kaukosäädin

Kun yhteys on muodostettu, voit lähettää komentoja kaukosäätimen käyttöliittymästä. Vaihda välilehteä käyttääksesi eri syöttötapoja.

Jokainen painallus suorittaa komennon isännällä. Tämä aiheuttaa paljon ylimääräistä kuormaa niinkin yksinkertaiselle asialle kuin näppäimen painallus, joten viive voi olla melko suuri verrattuna tavalliseen näppäimistöön. Toivon voivani parantaa tätä tulevissa versioissa.

Siirry valikosta muokkaustilaan. Tällä hetkellä asettelua tai painikkeiden kuvakkeita ei voi muokata. Toivon voivani mahdollistaa tämän tulevassa versiossa.

## Esiasetukset

Sinun on asennettava työpöytäympäristöllesi sopiva työkalu.

Suosittelen `ydotool`ia, koska testieni perusteella sillä on paras suorituskyky ja se toimii sekä X11:ssä että Waylandissa.

### ydotool

`ydotool`in pitäisi toimia millä tahansa ikkunointijärjestelmällä, mutta se vaatii taustapalvelun. Jos jakelusi tarjoaa systemd:n käyttäjäpalvelun, käynnistä se komennolla:

```shell
systemctl start --user ydotool
```

Voit käynnistää palvelun automaattisesti sisäänkirjautumisen yhteydessä komennolla:

```shell
systemctl enable --user ydotool
```

Varmista, että asennat riittävän uuden version `ydotool`ista. Ubuntu-versiot ennen versiota 26.04 tarjoavat liian vanhoja versioita. Katso kiertotapa keskusteluketjusta.

Keskustele `ydotool`ista tässä keskusteluketjussa: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` on tarkoitettu X11:tä käyttäville tietokoneille. X11 on ollut historiallisesti useimpien Linux-tietokoneiden käytössä, vaikka Wayland onkin yleistymässä.

Yksi X11:n erikoisuus on, että sinun voi olla tarpeen sallia pääsy X-palvelimeen. Tämä on ongelma, jos saat virheitä kuten "Authorization required". Ongelman korjaamiseen on useita vaihtoehtoja. Tässä on kaksi tapaa, jotka ovat toimineet minulla:

Jos `xauth list` ei näytä yhtään merkintää, kokeile luoda `.Xauthority`-tiedosto:

```shell
xauth generate :0 . trusted
```

Jos tämä ei auttanut, kokeile myöntää pääsy `xhost`illa:

```shell
xhost +local:$USER
```

`xhost`-komento täytyy suorittaa jokaisen käynnistyksen jälkeen. Voit automatisoida tämän luomalla bash-skriptin ja määrittämällä sen käynnistymään automaattisesti sisäänkirjautumisen yhteydessä.

Keskustele `xdotool`ista tässä keskusteluketjussa: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` on kuin `xdotool`, mutta Waylandille.

Normaalisti se ei tue hiiren ohjausta, mutta olen tehnyt muokatun version, joka lisää hiirituen. Asenna se, jos tarvitset hiiritukea: <https://github.com/stefansundin/wtype>

Jos saat virheen `Compositor does not support the virtual keyboard protocol`, suosittelen kokeilemaan toista työkalua, kuten `ydotool`ia.

Keskustele `wtype`stä tässä keskusteluketjussa: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool`in pitäisi toimia millä tahansa ikkunointijärjestelmällä, samoin kuin `ydotool`. Rajallisten testieni perusteella se oli kuitenkin paljon hitaampi kuin `ydotool`.

Keskustele `dotool`ista tässä keskusteluketjussa: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Tämä esiasetus on kokeellinen, koska en ole pystynyt varmistamaan sen toimivuutta omalla laitteistollani. Palaute on tervetullutta.

Keskustele `cec-client`istä tässä keskusteluketjussa: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Saatavilla on esiasetus VLC:n ohjaamiseen macOS:ssa AppleScript-komennoilla. En tiedä työkalua, joka tukisi näppäimistö- tai hiiritapahtumien lähettämistä.

Keskustele macOS:stä tässä keskusteluketjussa: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Jos Android-laitteessasi on SSH-palvelin, saatat voida muodostaa siihen yhteyden ja lähettää syötetapahtumia. Tämä on todennäköisempää, jos laitteessasi on mukautettu ROM, kuten KonstaKANG Raspberry Pille.

En ole saanut selville, miten hiirituki saataisiin toimimaan.

Keskustele Androidista tässä keskusteluketjussa: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Älykäs äänenvoimakkuuden säätö

Kun muokkaat kaukosäädintä, löydät valikosta "älykkäät" äänenvoimakkuusasetukset. Niiden avulla sovellus voi näyttää tietokoneen nykyisen äänenvoimakkuuden ja antaa sinun asettaa äänenvoimakkuuden nopeasti liukusäätimellä. Voit myös käyttää laitteesi fyysisiä äänenvoimakkuuspainikkeita lähettääksesi nopeasti äänenvoimakkuuden lisäys-/vähennyskomentoja.

Nykyisen äänenvoimakkuuden lukeminen ja uuden äänenvoimakkuuden asettaminen liukusäätimellä on tällä hetkellä kovakoodattu käyttämään `pactl`ia.

`pactl`in sisältävä paketti on yleensä nimeltään `pulseaudio-utils` tai `libpulse`.

## SSH-avaimet

Voit tuoda tai luoda SSH-avaimia sovelluksen asetuksissa. SSH-avaimella yhdistäminen on turvallisempaa kuin salasanojen käyttäminen.

Helpoin tapa tuoda olemassa oleva SSH-avain tietokoneelta on skannata QR-koodi. Voit luoda QR-koodikuvan `qrencode`-ohjelmalla. Suorita esimerkiksi seuraavanlainen komento QR-koodin luomiseksi:

```shell
# Siirry SSH-avaintesi kansioon:
cd ~/.ssh

# Näytä QR-koodi päätteessä:
qrencode -r id_ed25519 -t ansiutf8

# Vaihtoehtoisesti luo kuvatiedosto:
qrencode -r id_ed25519 -o qr.png

# 4096-bittiset RSA-avaimet ovat liian suuria QR-koodiin. Gzipillä sellainen mahtuu juuri ja juuri:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Voit siirtää julkisia SSH-avaimia palvelimelle valikon `Siirrä julkinen avain` -toiminnolla. Tämä lisää valitun SSH-avaimen tiedoston `~/.ssh/authorized_keys` loppuun. Tämän avulla voit helposti siirtyä salasanalla kirjautumisesta SSH-avaimella kirjautumiseen.

Voit tuoda ja käyttää salattuja SSH-avaimia, mutta et voi tällä hetkellä luoda niitä sovelluksessa.

## Tietoturva

Sovelluksesta ei ole mahdollista viedä tai purkaa SSH-avainten yksityistä osaa tai tallennettuja salasanoja. Nämä tiedot salataan 256-bittisellä AES:llä, ja salausavain tallennetaan Android Keystoreen. Salatut tiedot jätetään pois Android-varmuuskopioista.

Sovelluksessa ei ole kaatumisraportointiohjelmistoa. Ei telemetriaa. Ei mainoksia. Ei verkkopyyntöjä SSH-yhteyttä lukuun ottamatta.

Tämän sovelluksen tietoturvaa ei ole auditoitu. Jos tunnet Androidin tai SSH:n tietoturvaa hyvin, tutustu lähdekoodiin ja raportoi havaintosi tähän GitHub-issueen:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Ominaisuuspyynnöt

Voit lähettää ominaisuuspyyntöjä ja virheraportteja GitHub-repositorioon. Käytä englantia. Pidä kommenttisi asiallisina. Epäkunnioittavat kommentit poistetaan ja käyttäjiä voidaan estää repositoriosta.

Katso olemassa olevat issuet ja keskusteluketjut läpi varmistaaksesi, ettei kysymystäsi ole jo esitetty tai siihen ole jo vastattu.

Ole kunnioittava. Rakensin tämän sovelluksen vapaa-ajallani ja annan sen ilmaiseksi. Rakennan tätä sovellusta ennen kaikkea omaan käyttööni.

Älä lähetä minulle kysymyksiä sähköpostitse. Pyri pitämään keskustelut GitHubissa, koska siitä on hyötyä myös muille. Voit esittää kysymyksiä GitHubin keskusteluosiossa.

Voit aina haarukoida sovelluksen ja toteuttaa omia ominaisuuksiasi. Se on hyvä tapa oppia. Harkitse hyödyllisten ominaisuuksien kontribuointia.

Tämän sovelluksen lähdekoodi on lisensoitu GNU GPLv3:lla. Jos levität tämän sovelluksen muokattuja versioita, sinun on myös asetettava lähdekoodi saataville.

<https://github.com/stefansundin/SSHRemote>

## Lahjoitukset

Jos haluat osoittaa kiitollisuuttasi ja arvostustasi, lahjoitukset ovat tervetulleita.

<https://stefansundin.github.io/donate/>

Jos olet lahjoittanut, yritän parhaani mukaan vastata kaikkiin kysymyksiisi. Kirjoita tiedustelut englanniksi.

Kiitos tuestasi!
