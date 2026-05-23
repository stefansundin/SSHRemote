## Az SSH Remote névjegye

*Megjegyzés: Ez a magyar fordítás mesterséges intelligencia segítségével készült a GitHub Copilot használatával, ezért előfordulhatnak fordítási hibák.*

Az SSH Remote egy ingyenes és nyílt forráskódú alkalmazás, amellyel SSH-n keresztül távolról vezérelhet számítógépeket.

Teljes mértékben testreszabhatja a végrehajtott parancsokat, és gyakori felállásokhoz előbeállítások is rendelkezésre állnak.

Én ezt az alkalmazást a Raspberry Pi OS-t futtató HTPC rendszerem vezérlésére használom. A HTPC vezérlése az az alaphelyzet, amelyre az alkalmazás elsősorban optimalizálva van.

Ez az alkalmazás nem terminálemulátor, de vészhelyzetben lehetővé teszi például az `apt-get install` futtatását.

## Első lépések

Ha SSH kulccsal szeretne csatlakozni, először nyissa meg az alkalmazás beállításait, és importáljon vagy generáljon egy kulcsot.

Új hoszt hozzáadásához érintse meg a jobb alsó sarokban található `+` gombot. Adja meg a kapcsolódási adatokat, majd mentse el.

Az első csatlakozáskor ki kell választania egy előbeállítást. Ez a választás úgy konfigurálja a távirányító gombjait, hogy azok jól működjenek különböző típusú számítógépeken. Az elérhető előbeállítások leírását lásd alább. Ha szeretné, mindenféle kezdeti konfiguráció nélkül is indulhat a `Nincs előbeállítás` kiválasztásával.

Ha nem tudja, hogy a Linuxos számítógépe X11-et vagy Waylandet futtat, futtassa ezt egy terminálban:

```shell
echo $XDG_SESSION_TYPE
```

Ennek `x11` vagy `wayland` értéket kell kiírnia. Ezt az asztali környezeten belül kell futtatnia.

## Távirányító

A csatlakozás után a távirányító felületet használhatja parancsok küldésére. A különböző beviteli módok eléréséhez váltson a lapok között.

Minden gombnyomás egy parancsot hajt végre a hoszton. Ez elég nagy többletterhelést jelent valami olyan egyszerűhöz, mint egy billentyűleütés, ezért a hagyományos billentyűzethez képest viszonylag nagy késleltetést tapasztalhat. Remélem, ezt a jövőbeli verziókban javítani tudom.

A menü segítségével léphet szerkesztési módba. Jelenleg nem lehetséges az elrendezés vagy a gombikonok szerkesztése. Remélem, ezt a jövőben lehetővé tudom tenni.

## Előbeállítások

Telepítenie kell az asztali környezetéhez szükséges eszközt.

Én a `ydotool` használatát javaslom, mert a tesztjeim alapján ennek a legjobb a teljesítménye, és X11-en és Waylanden is működik.

### ydotool

A `ydotool` bármilyen ablakkezelővel működhet, de ehhez futnia kell egy háttérszolgáltatásnak. Ha a disztribúciója biztosít systemd felhasználói szolgáltatást, akkor indítsa el ezzel:

```shell
systemctl start --user ydotool
```

A szolgáltatás automatikus indításához bejelentkezéskor futtassa ezt:

```shell
systemctl enable --user ydotool
```

Kérjük, győződjön meg róla, hogy kellően friss `ydotool` verziót telepít. A 26.04 előtti Ubuntu-verziók túl régi kiadásokat tartalmaznak. Kerülőmegoldásért nézze meg a kapcsolódó beszélgetésszálat.

A `ydotool` kapcsán ebben a beszélgetésszálban írhat: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

Az `xdotool` X11-et futtató számítógépekhez való. A legtöbb Linuxos számítógép korábban X11-et használt, bár a Wayland egyre népszerűbb.

Az X11 egyik sajátossága, hogy lehet, engedélyeznie kell a hozzáférést az X szerverhez. Erre utal, ha „Authorization required” hibákat kap. Több lehetősége is van a probléma megoldására; itt van két módszer, amely nálam működött:

Ha az `xauth list` nem mutat semmilyen bejegyzést, akkor próbáljon meg létrehozni egy `.Xauthority` fájlt:

```shell
xauth generate :0 . trusted
```

Ha ez nem működött, próbáljon hozzáférést adni az `xhost` használatával:

```shell
xhost +local:$USER
```

Az `xhost` parancsot minden rendszerindítás után újra futtatnia kell. Ezt automatizálhatja egy bash szkript létrehozásával, majd annak beállításával, hogy bejelentkezéskor automatikusan elinduljon.

Az `xdotool` kapcsán ebben a beszélgetésszálban írhat: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

A `wtype` olyan, mint az `xdotool`, csak Waylandhez.

Alapból nem támogatja az egérvezérlést, de készítettem egy módosított verziót, amely egértámogatást is ad. Ha szüksége van egértámogatásra, kérjük, ezt telepítse: <https://github.com/stefansundin/wtype>

Ha a `Compositor does not support the virtual keyboard protocol` hibát kapja, akkor azt javaslom, próbáljon ki egy másik eszközt, például a `ydotool`-t.

A `wtype` kapcsán ebben a beszélgetésszálban írhat: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

A `dotool` a `ydotool`-hoz hasonlóan bármilyen ablakkezelővel működhet. A korlátozott tesztjeimben azonban sokkal lassabb volt, mint a `ydotool`.

A `dotool` kapcsán ebben a beszélgetésszálban írhat: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Ez az előbeállítás kísérleti, mivel a saját hardveremen nem tudtam ellenőrizni. A visszajelzéseket szívesen fogadom.

A `cec-client` kapcsán ebben a beszélgetésszálban írhat: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Van előbeállítás a VLC vezérlésére macOS-en AppleScript parancsokkal. Nem tudok olyan eszközről, amely támogatná a billentyűzet- vagy egéresemények küldését.

A macOS kapcsán ebben a beszélgetésszálban írhat: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Ha az Android-eszközén van SSH szerver, akkor lehet, hogy csatlakozni tud hozzá, és bemeneti eseményeket küldhet neki. Ez valószínűbb, ha az eszköz egy egyéni ROM-ot futtat, például a Raspberry Pi-hez készült KonstaKANG-ot.

Még nem sikerült rájönnöm, hogyan lehetne működésre bírni az egértámogatást.

Az Android kapcsán ebben a beszélgetésszálban írhat: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Intelligens hangerőszabályzás

A távirányító szerkesztésekor a menüben találhat intelligens hangerőszabályzási beállításokat. Ez képes megjeleníteni a számítógép aktuális hangerejét az alkalmazásban, és egy csúszkával gyorsan beállíthatja azt. Az eszköz hardvergombjaival gyorsan hangerő fel/le parancsokat is küldhet.

A jelenlegi hangerő beolvasása és új hangerő beállítása a csúszkával jelenleg fixen a `pactl` használatára van beégetve.

A `pactl` csomag neve általában `pulseaudio-utils` vagy `libpulse`.

## SSH kulcsok

Az alkalmazás beállításaiban importálhat vagy generálhat SSH kulcsokat. Az SSH kulccsal való csatlakozás biztonságosabb, mint a jelszavas bejelentkezés.

A legegyszerűbb módja annak, hogy egy meglévő SSH kulcsot importáljon egy számítógépről, egy QR-kód beolvasása. A `qrencode` programmal elkészítheti a QR-kód képét. Futtasson például egy ehhez hasonló parancsot a QR-kód létrehozásához:

```shell
# Lépjen az SSH-kulcsok könyvtárába:
cd ~/.ssh

# Jelenítse meg a QR-kódot a terminálban:
qrencode -r id_ed25519 -t ansiutf8

# Alternatívaként készítsen egy képfájlt:
qrencode -r id_ed25519 -o qr.png

# A 4096 bites RSA-kulcsok túl nagyok egy QR-kódhoz. A gzip segítségével éppen csak beleférhet egy:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

A nyilvános SSH kulcsokat a szerverre a menüben található `Nyilvános kulcs feltöltése` funkcióval töltheti fel. Ez hozzáfűzi a kiválasztott SSH kulcsot a `~/.ssh/authorized_keys` fájlhoz. Így könnyen áttérhet a jelszavas bejelentkezésről az SSH kulcsos bejelentkezésre.

Titkosított SSH kulcsokat importálhat és használhat is, de jelenleg ilyeneket nem lehet létrehozni az alkalmazásban.

## Biztonság

Az alkalmazásból nem lehetséges exportálni vagy kinyerni az SSH kulcsok privát részét, illetve a tárolt jelszavakat. Ezek az adatok 256 bites AES titkosítással vannak védve, a titkosítási kulcs pedig az Android Keystore-ban van tárolva. A titkosított adatok ki vannak zárva az Android biztonsági mentéseiből.

Ebben az alkalmazásban nincs összeomlásjelentő szoftver. Nincs telemetria. Nincsenek hirdetések. Az SSH kapcsolaton kívül nincs hálózati kérés.

Az alkalmazás biztonsága nem lett auditálva. Ha jártas az Android-biztonságban vagy az SSH-biztonságban, kérjük, nézze át a forráskódot, és ossza meg megállapításait ebben a GitHub issue-ban:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Funkciókérések

Nyugodtan küldjön be funkciókéréseket és hibajelentéseket a GitHub repóban. Kérjük, használjon angolt. Kérjük, maradjon kulturált. A tiszteletlen hozzászólásokat eltávolítjuk, és a felhasználókat akár ki is tilthatjuk a repóból.

Kérjük, nézze át a meglévő issue-kat és beszélgetésszálakat, hátha a kérdését már feltették vagy megválaszolták.

Kérem, legyen tiszteletteljes. Ezt az alkalmazást a szabadidőmben készítettem, és ingyen adom tovább. Ezt az alkalmazást elsősorban a saját használatomra fejlesztem.

Kérem, ne írjon nekem e-mailben kérdéseket. Próbáljuk a beszélgetéseket a GitHubon tartani, mert ez másoknak is segít. Kérdéseket a GitHub beszélgetés szekciójában tehet fel.

Mindig szívesen látom, ha valaki forkolja az alkalmazást, hogy saját funkciókat valósítson meg. Ez remek tanulási lehetőség. Kérjük, fontolja meg a hasznos funkciók visszaküldését is.

A forráskód GNU GPLv3 licenc alatt áll. Ha az alkalmazás módosított verzióját terjeszti, akkor a forráskódot is elérhetővé kell tennie.

<https://github.com/stefansundin/SSHRemote>

## Adományok

Ha szeretné kifejezni háláját és megbecsülését, adományokat örömmel fogadok.

<https://stefansundin.github.io/donate/>

Ha adományozott, minden tőlem telhetőt megteszek, hogy válaszoljak a kérdéseire. Kérem, a megkereséseit írja angolul.

Köszönöm a támogatását!
