## Kuhusu SSH Remote

Tafsiri hii imetengenezwa kwa msaada wa AI na huenda ikawa na makosa ya tafsiri.

SSH Remote ni programu ya bure na ya wazi inayokuruhusu kudhibiti kompyuta ukiwa mbali kwa kutumia SSH.

Unaweza kubinafsisha kikamilifu amri zinazotekelezwa, na kuna mipangilio ya awali kwa usanidi wa kawaida.

Ninatumia programu hii kudhibiti mfumo wangu wa HTPC, unaotumia Raspberry Pi OS. Kudhibiti HTPC ndiyo hali kuu ambayo programu hii imeboreshwa kwa ajili yake.

Programu hii si emulator ya terminal, lakini itakuwezesha kuendesha `apt-get install` wakati wa dharura.

## Kuanza

Ikiwa unataka kutumia ufunguo wa SSH kuunganisha, kwanza fungua mipangilio ya programu na uingize au utengeneze ufunguo.

Ongeza hosti mpya kwa kugusa kitufe cha `+` kilicho chini kulia. Weka maelezo ya muunganisho na uhifadhi.

Mara ya kwanza unapounganisha, utaombwa uchague mpangilio wa awali. Uteuzi huu husanidi vitufe vya kidhibiti cha mbali ili vifanye kazi vizuri kwenye aina mbalimbali za kompyuta. Tazama hapa chini kwa maelezo ya mipangilio ya awali inayopatikana. Ukipenda, unaweza kuanza bila usanidi wowote kwa kuchagua `Hakuna mpangilio wa awali`.

Ikiwa hujui kama kompyuta yako ya Linux inatumia X11 au Wayland, endesha hili kwenye terminal:

```shell
echo $XDG_SESSION_TYPE
```

Hii inapaswa kutoa `x11` au `wayland`. Lazima uendeshe hili ndani ya mazingira ya eneo-kazi.

## Kidhibiti cha Mbali

Baada ya kuunganisha, unaweza kutumia kiolesura cha kidhibiti cha mbali kutuma amri. Badili vichupo ili kufikia mbinu mbalimbali za kuingiza.

Kila kubonyeza kitufe kutatekeleza amri kwenye hosti. Hii ni mzigo mkubwa kwa jambo rahisi kama kubonyeza kitufe, na unaweza kuona ucheleweshaji mkubwa ukilinganishwa na kibodi ya kawaida. Natumaini kuboresha hili katika matoleo yajayo.

Tumia menyu kuingia hali ya kuhariri. Kwa sasa haiwezekani kuhariri mpangilio au aikoni za vitufe. Natumaini kufanya hili liwezekane katika toleo lijalo.

## Mipangilio ya awali

Utahitaji kusakinisha zana inayohitajika kwa mazingira yako ya eneo-kazi.

Ninapendekeza `ydotool` kwa sababu katika majaribio yangu ina utendaji bora zaidi, na inafanya kazi kwenye X11 na Wayland.

### ydotool

`ydotool` inapaswa kufanya kazi na meneja wowote wa madirisha, lakini unahitaji huduma ya chinichini iendelee kufanya kazi. Ikiwa usambazaji wako unatoa huduma ya mtumiaji ya systemd basi ianze kwa kuendesha:

```shell
systemctl start --user ydotool
```

Anzisha huduma kiotomatiki unapoingia kwa kuendesha:

```shell
systemctl enable --user ydotool
```

Tafadhali hakikisha unasakinisha toleo jipya la kutosha la `ydotool`. Matoleo ya Ubuntu kabla ya 26.04 hutoa matoleo ya zamani mno. Tazama mjadala kwa suluhisho la muda.

Tafadhali jadili `ydotool` katika mjadala huu: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` ni kwa kompyuta zinazotumia X11. X11 ndiyo ambayo kompyuta nyingi za Linux zimetumia kihistoria, ingawa Wayland inazidi kuwa maarufu.

Jambo moja la kipekee katika X11 ni kwamba huenda ukahitaji kuruhusu ufikiaji wa seva ya X. Hili ndilo tatizo ikiwa unapata hitilafu za "Authorization required". Una chaguo kadhaa za kurekebisha tatizo hili; hapa kuna chaguo mbili ambazo zimenifanyia kazi:

Ikiwa `xauth list` haionyeshi maingizo yoyote basi jaribu kutengeneza faili ya `.Xauthority`:

```shell
xauth generate :0 . trusted
```

Ikiwa hilo halikufanya kazi basi jaribu kutoa ruhusa ya ufikiaji kwa kutumia `xhost`:

```shell
xhost +local:$USER
```

Utahitaji kuendesha amri ya `xhost` kila baada ya kuwasha upya kompyuta. Unaweza kuweka hili kiotomatiki kwa kuunda hati ya bash na kuiweka ianze yenyewe unapoingia.

Tafadhali jadili `xdotool` katika mjadala huu: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` ni kama `xdotool`, lakini kwa Wayland.

Kwa kawaida, haitoi msaada wa kudhibiti kipanya, lakini nimetengeneza toleo lililorekebishwa linaloongeza msaada wa kipanya. Tafadhali lisakinishe ikiwa unahitaji msaada wa kipanya: <https://github.com/stefansundin/wtype>

Ukiona hitilafu `Compositor does not support the virtual keyboard protocol` basi napendekeza ujaribu zana nyingine, kama `ydotool`.

Tafadhali jadili `wtype` katika mjadala huu: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` inapaswa kufanya kazi na meneja wowote wa madirisha, sawa na `ydotool`. Katika majaribio yangu machache ilikuwa polepole zaidi kuliko `ydotool`.

Tafadhali jadili `dotool` katika mjadala huu: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Mpangilio huu wa awali ni wa majaribio kwa sababu sijafaulu kuuthibitisha kwenye vifaa vyangu mwenyewe. Maoni yanakaribishwa.

Tafadhali jadili `cec-client` katika mjadala huu: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Kuna mpangilio wa awali wa kudhibiti VLC kwenye macOS, kwa kutumia amri za AppleScript. Sina ufahamu wa zana inayoweza kutuma matukio ya kibodi au kipanya.

Tafadhali jadili macOS katika mjadala huu: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Ikiwa kifaa chako cha Android kinakuja na seva ya SSH, basi huenda ukaweza kuunganisha nacho na kutuma matukio ya ingizo. Hili lina uwezekano mkubwa zaidi ikiwa kifaa chako kinatumia ROM maalum, kama KonstaKANG kwenye Raspberry Pi.

Bado sijagundua jinsi ya kufanya msaada wa kipanya ufanye kazi.

Tafadhali jadili Android katika mjadala huu: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Udhibiti mahiri wa sauti

Unapohariri kidhibiti cha mbali, unaweza kupata mipangilio ya udhibiti "mahiri" wa sauti katika menyu. Hii inaweza kuonyesha sauti ya sasa ya kompyuta ndani ya programu na kukuwezesha kuweka sauti haraka kwa kutumia kitelezi. Pia unaweza kutumia vitufe vya kifaa chako kuongeza au kupunguza sauti kwa haraka.

Kusoma sauti ya sasa na kuweka sauti mpya kwa kutumia kitelezi kwa sasa kumewekewa moja kwa moja kutumia `pactl`.

Kifurushi chenye `pactl` kwa kawaida huitwa `pulseaudio-utils` au `libpulse`.

## Funguo za SSH

Unaweza kuingiza au kutengeneza funguo za SSH katika mipangilio ya programu. Kuunganisha kwa kutumia ufunguo wa SSH ni salama zaidi kuliko kutumia manenosiri.

Njia rahisi zaidi ya kuingiza ufunguo uliopo kutoka kwa kompyuta ni kuskani msimbo wa QR. Unaweza kutumia programu ya `qrencode` kutengeneza picha ya msimbo wa QR. Endesha amri kama ifuatayo ili kutengeneza msimbo wa QR:

```shell
# Nenda kwenye funguo zako za SSH:
cd ~/.ssh

# Onyesha msimbo wa QR kwenye terminal:
qrencode -r id_ed25519 -t ansiutf8

# Vinginevyo, tengeneza faili ya picha:
qrencode -r id_ed25519 -o qr.png

# Funguo za RSA za biti 4096 ni kubwa mno kwa msimbo wa QR. Unaweza kutumia gzip ili moja iweze kutoshea:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Unaweza kutuma funguo za umma za SSH kwa seva kwa kutumia kipengele cha `Tuma ufunguo wa umma` katika menyu. Hili litaongeza ufunguo wa SSH uliochaguliwa kwenye faili ya `~/.ssh/authorized_keys`. Hii hukuwezesha kuhama kwa urahisi kutoka kuingia kwa nenosiri hadi kuingia kwa ufunguo wa SSH.

Unaweza kuingiza na kutumia funguo za SSH zilizosimbwa, lakini kwa sasa huwezi kuzitengeneza ndani ya programu.

## Usalama

Haiwezekani kuhamisha au kutoa sehemu ya faragha ya funguo za SSH, au manenosiri yaliyohifadhiwa, kutoka ndani ya programu. Data hii imesimbwa kwa kutumia AES ya biti 256, na ufunguo wa usimbaji umehifadhiwa katika Android Keystore. Data iliyosimbwa huondolewa kwenye chelezo za Android.

Hakuna programu ya kuripoti hitilafu za kuanguka kwa programu ndani ya programu hii. Hakuna telemetry. Hakuna matangazo. Hakuna maombi ya mtandao isipokuwa muunganisho wa SSH.

Usalama wa programu hii haujakaguliwa rasmi. Ikiwa una uzoefu na usalama wa Android au usalama wa SSH, tafadhali angalia msimbo chanzo na uripoti ulichogundua katika issue hii ya GitHub:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Maombi ya vipengele

Jisikie huru kuwasilisha maombi ya vipengele na ripoti za hitilafu katika hazina ya GitHub. Tafadhali tumia Kiingereza. Tafadhali weka maoni yako yenye heshima. Maoni ya kukosa heshima yataondolewa na watumiaji wanaweza kuzuiwa kwenye hazina.

Tafadhali pitia issues zilizopo na mijadala iliyopo kuona kama swali lako tayari limeulizwa au kujibiwa.

Tafadhali kuwa na heshima. Nilitengeneza programu hii kwa wakati wangu wa bure na ninaitoa bila malipo. Ninatengeneza programu hii hasa kwa matumizi yangu mwenyewe.

Tafadhali usinitumie maswali kwa barua pepe. Tafadhali jaribu kuweka mazungumzo kwenye GitHub, kwa kuwa hilo huwasaidia watu wengine pia! Unaweza kuuliza maswali katika sehemu ya majadiliano kwenye GitHub.

Karibu kila wakati kuifork programu hii ili kutekeleza vipengele vyako mwenyewe. Hiyo ni njia nzuri ya kujifunza. Tafadhali fikiria kuchangia vipengele vyenye manufaa.

Msimbo chanzo umepewa leseni chini ya GNU GPLv3. Ikiwa unasambaza matoleo yaliyobadilishwa ya programu hii basi lazima pia ufanye msimbo chanzo upatikane.

<https://github.com/stefansundin/SSHRemote>

## Michango

Ikiwa ungependa kuonyesha shukrani na kuthamini, michango inakubaliwa.

<https://stefansundin.github.io/donate/>

Ikiwa umechangia basi nitajaribu kadiri ya uwezo wangu kujibu swali lolote ambalo unaweza kuwa nalo. Tafadhali andika maswali yoyote kwa Kiingereza.

Asante kwa msaada wako!
