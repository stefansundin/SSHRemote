## Um SSH Remote

Þessi íslenska þýðing var unnin með aðstoð GitHub Copilot (GPT-5.3-Codex) og gæti innihaldið þýðingarvillur.

SSH Remote er ókeypis og opið forrit sem gerir þér kleift að stjórna tölvum úr fjarlægð með SSH.

Þú getur sérsniðið skipanirnar sem eru keyrðar að fullu og það eru til forstillingar fyrir algengar uppsetningar.

Ég nota þetta forrit til að stjórna HTPC-uppsetningunni minni, sem keyrir Raspberry Pi OS. Að stjórna HTPC er grunnsviðsmyndin sem forritið er best aðlagað fyrir.

Þetta forrit er ekki skjáhermir fyrir skel, en það gerir þér kleift að keyra `apt-get install` í neyð.

## Að byrja

Ef þú vilt nota SSH-lykil til að tengjast skaltu fyrst opna stillingar forritsins og flytja inn eða mynda lykil.

Bættu við nýrri vél með því að pikka á `+` hnappinn neðst til hægri. Sláðu inn tengiupplýsingarnar og vistaðu.

Í fyrsta skipti sem þú tengist verður þú beðin eða beðinn um að velja forstillingu. Þetta val stillir hnappana á fjarstýringunni þannig að þeir virki vel á ýmsum tegundum tölva. Sjáðu hér að neðan lýsingu á tiltækum forstillingum. Ef þú vilt geturðu byrjað án stillinga með því að velja `Engin forstilling`.

Ef þú veist ekki hvort Linux-tölvan þín keyrir X11 eða Wayland skaltu keyra þetta í skel:

```shell
echo $XDG_SESSION_TYPE
```

Þetta ætti að skila `x11` eða `wayland`. Þú verður að keyra þetta innan skjáborðsumhverfisins.

## Fjarstýring

Þegar tenging hefur tekist geturðu notað fjarstýringarviðmótið til að senda skipanir. Skiptu um flipa til að fá aðgang að mismunandi innsláttaraðferðum.

Hver hnappasmellur keyrir skipun á vélinni. Þetta er töluverð yfirbygging fyrir eitthvað jafn einfalt og takkasmell og þú gætir fundið fyrir talsverðri töf miðað við venjulegt lyklaborð. Ég vona að bæta þetta í framtíðarútgáfum.

Notaðu valmyndina til að fara í breytingaham. Eins og er er ekki hægt að breyta útliti eða táknum hnappa. Ég vona að þetta verði mögulegt í framtíðarútgáfu.

## Forstillingar

Þú þarft að setja upp tólið sem hentar skjáborðsumhverfinu þínu.

Ég mæli með `ydotool` því í prófunum mínum hefur það skilað bestum afköstum og það virkar bæði á X11 og Wayland.

### ydotool

`ydotool` ætti að virka með hvaða gluggastjóra sem er, en þú þarft bakgrunnsþjónustu í keyrslu. Ef dreifingin þín býður upp á systemd notendaþjónustu skaltu ræsa hana með því að keyra:

```shell
systemctl start --user ydotool
```

Láttu þjónustuna ræsast sjálfkrafa við innskráningu með því að keyra:

```shell
systemctl enable --user ydotool
```

Vinsamlegast gakktu úr skugga um að þú sért að setja upp nægilega nýja útgáfu af `ydotool`. Ubuntu útgáfur fyrir 26.04 bjóða upp á of gamlar útgáfur. Sjáðu umræðuþráðinn fyrir hjáleið.

Vinsamlegast ræddu `ydotool` í þessum umræðuþræði: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` er fyrir tölvur sem keyra X11. X11 er það sem flestar Linux-tölvur hafa notað sögulega séð, þó Wayland verði sífellt vinsælla.

Einn galli við X11 er að þú gætir þurft að leyfa aðgang að X-þjóninum. Þetta á við ef þú færð villur eins og `Authorization required`. Þú hefur nokkra möguleika til að laga þetta. Hér eru tveir valkostir sem hafa virkað fyrir mig:

Ef `xauth list` sýnir engar færslur skaltu prófa að búa til `.Xauthority` skrá:

```shell
xauth generate :0 . trusted
```

Ef það virkaði ekki skaltu prófa að veita aðgang með `xhost`:

```shell
xhost +local:$USER
```

Þú þarft að keyra `xhost` skipunina eftir hverja ræsingu. Þú getur sjálfvirknivætt þetta með því að búa til bash-skriftu og stilla hana til að ræsa sjálfkrafa við innskráningu.

Vinsamlegast ræddu `xdotool` í þessum umræðuþræði: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` er eins og `xdotool`, en fyrir Wayland.

Venjulega styður það ekki músarstýringu, en ég hef búið til breytta útgáfu sem bætir við músarstuðningi. Vinsamlegast settu hana upp ef þú þarft músarstuðning: <https://github.com/stefansundin/wtype>

Ef þú færð villuna `Compositor does not support the virtual keyboard protocol` legg ég til að þú prófir annað tól, eins og `ydotool`.

Vinsamlegast ræddu `wtype` í þessum umræðuþræði: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` ætti að virka með hvaða gluggastjóra sem er, svipað og `ydotool`. Í takmörkuðum prófunum mínum var það mun hægara en `ydotool`.

Vinsamlegast ræddu `dotool` í þessum umræðuþræði: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Þessi forstilling er tilraunakennd þar sem ég hef ekki getað sannreynt hana á eigin vélbúnaði. Endurgjöf er vel þegin.

Vinsamlegast ræddu `cec-client` í þessum umræðuþræði: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Það er til forstilling til að stjórna VLC á macOS með AppleScript skipunum. Ég veit ekki um tól sem styður að senda lyklaborðs- eða músaratvik.

Vinsamlegast ræddu macOS í þessum umræðuþræði: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Ef Android-tækið þitt kemur með SSH-þjóni gætirðu getað tengst því og sent inntaksatvik til þess. Þetta er líklegra ef tækið þitt keyrir sérsniðna ROM, eins og KonstaKANG á Raspberry Pi.

Mér hefur ekki tekist að finna leið til að fá músarstuðning til að virka.

Vinsamlegast ræddu Android í þessum umræðuþræði: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Snjöll hljóðstyrksstýring

Þegar þú breytir fjarstýringunni geturðu fundið „snjallar“ hljóðstyrksstillingar í valmyndinni. Þær geta sýnt núverandi hljóðstyrk tölvunnar í forritinu og leyft þér að stilla hljóðstyrkinn hratt með sleða. Þú getur líka notað vélbúnaðarhnappa tækisins til að senda fljótt skipanir um að hækka eða lækka hljóðstyrk.

Lestur á núverandi hljóðstyrk og stilling á nýjum hljóðstyrk með sleðanum er eins og er harðkóðuð til að nota `pactl`.

Pakkinn sem inniheldur `pactl` heitir venjulega `pulseaudio-utils` eða `libpulse`.

## SSH-lyklar

Þú getur flutt inn eða myndað SSH-lykla í stillingum forritsins. Að tengjast með SSH-lykli er öruggara en að nota lykilorð.

Auðveldasta leiðin til að flytja inn SSH-lykil sem þegar er til af tölvu er að skanna QR-kóða. Þú getur notað `qrencode` forritið til að búa til QR-kóðamyndina. Keyrðu skipun eins og þessa til að búa til QR-kóðann:

```shell
# Farðu í SSH-lyklamöppuna:
cd ~/.ssh

# Sýndu QR-kóðann í skelinni:
qrencode -r id_ed25519 -t ansiutf8

# Einnig er hægt að búa til myndskrá:
qrencode -r id_ed25519 -o qr.png

# 4096 bita RSA-lyklar eru of stórir fyrir QR-kóða. Þú getur notað gzip til að koma einum rétt svo fyrir:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Þú getur sent opinbera SSH-lykla á þjón með því að nota eiginleikann `Senda opinberan lykil` í valmyndinni. Þetta bætir völdum SSH-lykli við skrána `~/.ssh/authorized_keys`. Þetta gerir þér kleift að flytja þig auðveldlega frá innskráningu með lykilorði yfir í innskráningu með SSH-lykli.

Þú getur flutt inn og notað dulkóðaða SSH-lykla, en þú getur ekki myndað þá í forritinu eins og er.

## Öryggi

Það er ekki hægt að flytja út eða draga út einkahluta SSH-lykla eða vistuð lykilorð úr forritinu. Þessi gögn eru dulkóðuð með 256 bita AES og dulkóðunarlykillinn er geymdur í Android Keystore. Dulkóðuð gögn eru útilokuð úr Android öryggisafritum.

Það er enginn hugbúnaður fyrir hrunskýrslur í þessu forriti. Það er engin fjarvöktun. Það eru engar auglýsingar. Það eru engar netbeiðnir nema SSH-tengingin.

Öryggi þessa forrits hefur ekki verið öryggisúttekið. Ef þú hefur reynslu af Android- eða SSH-öryggi skaltu endilega skoða frumkóðann og tilkynna niðurstöður þínar í þessu GitHub-máli:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Eiginleikabeiðnir

Ekki hika við að senda inn eiginleikabeiðnir og villuskýrslur í GitHub-geymslunni. Vinsamlegast notaðu ensku. Vinsamlegast hafðu athugasemdir kurteislegar. Óvirðulegar athugasemdir verða fjarlægðar og notendur gætu verið útilokaðir frá geymslunni.

Vinsamlegast skoðaðu fyrirliggjandi mál og umræðuþræði til að sjá hvort spurningin þín hafi þegar verið spurð eða svarað.

Vinsamlegast sýndu virðingu. Ég byggði þetta forrit í frítíma mínum og gef það frá mér ókeypis. Ég byggi þetta forrit fyrst og fremst fyrir eigin notkun.

Vinsamlegast ekki senda mér tölvupóst með spurningum. Reyndu frekar að halda samtölum á GitHub þar sem það hjálpar öðrum líka. Þú getur spurt spurninga í umræðuhlutanum á GitHub.

Þér er alltaf velkomið að forka forritið til að innleiða eigin eiginleika. Það er frábær leið til að læra. Vinsamlegast íhugaðu að leggja til nytsamlega eiginleika.

Frumkóðinn er gefinn út undir GNU GPLv3. Ef þú dreifir breyttum útgáfum af þessu forriti verður þú líka að gera frumkóðann aðgengilegan.

<https://github.com/stefansundin/SSHRemote>

## Styrkir

Ef þú vilt sýna þakklæti og velvild eru styrkir vel þegnir.

<https://stefansundin.github.io/donate/>

Ef þú hefur styrkt mig mun ég reyna mitt besta til að svara öllum spurningum sem þú kannt að hafa. Vinsamlegast skrifaðu fyrirspurnir á ensku.

Takk fyrir stuðninginn!
