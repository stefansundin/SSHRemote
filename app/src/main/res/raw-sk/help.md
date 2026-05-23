## O aplikácii SSH Remote

Tento slovenský preklad bol vytvorený pomocou AI od GitHub Copilot (GPT-5.3-Codex) a môže obsahovať chyby v preklade.

SSH Remote je bezplatná open source aplikácia, ktorá vám umožňuje ovládať počítače na diaľku pomocou SSH.

Môžete si úplne prispôsobiť vykonávané príkazy a k dispozícii sú aj predvoľby pre bežné nastavenia.

Túto aplikáciu používam na ovládanie svojho HTPC, na ktorom beží Raspberry Pi OS. Ovládanie HTPC je základný scenár, na ktorý je aplikácia optimalizovaná.

Táto aplikácia nie je terminálový emulátor, ale v núdzi vám umožní spustiť `apt-get install`.

## Začíname

Ak sa chcete pripájať pomocou SSH kľúča, najprv otvorte nastavenia aplikácie a kľúč importujte alebo vygenerujte.

Pridajte nového hostiteľa klepnutím na tlačidlo `+` vpravo dole. Zadajte údaje o pripojení a uložte ich.

Pri prvom pripojení budete vyzvaní na výber predvoľby. Tento výber nakonfiguruje tlačidlá diaľkového ovládania tak, aby dobre fungovali na rôznych typoch počítačov. Popis dostupných predvolieb nájdete nižšie. Ak chcete, môžete začať aj bez konfigurácie výberom možnosti `Bez predvoľby`.

Ak neviete, či váš linuxový počítač používa X11 alebo Wayland, spustite v termináli toto:

```shell
echo $XDG_SESSION_TYPE
```

Výstup by mal byť `x11` alebo `wayland`. Tento príkaz musíte spustiť v rámci desktopového prostredia.

## Diaľkové ovládanie

Po pripojení môžete na odosielanie príkazov používať rozhranie diaľkového ovládania. Prepínaním kariet získate prístup k rôznym spôsobom vstupu.

Každé stlačenie tlačidla vykoná na hostiteľovi príkaz. Pri niečom tak jednoduchom, ako je stlačenie klávesu, je to pomerne veľká réžia a v porovnaní s bežnou klávesnicou môžete zaznamenať pomerne vysokú latenciu. Dúfam, že to v budúcich verziách zlepším.

Pomocou menu môžete prejsť do režimu úprav. Rozloženie ani ikony tlačidiel momentálne nie je možné upravovať. Dúfam, že to bude možné v budúcej verzii.

## Predvoľby

Budete musieť nainštalovať nástroj potrebný pre vaše desktopové prostredie.

Odporúčam `ydotool`, pretože v mojom testovaní mal najlepší výkon a funguje na X11 aj Waylande.

### ydotool

`ydotool` by mal fungovať s akýmkoľvek správcom okien, ale musíte mať spustenú službu na pozadí. Ak vaša distribúcia poskytuje systemd používateľskú službu, spustite ju týmto príkazom:

```shell
systemctl start --user ydotool
```

Ak chcete službu spúšťať automaticky po prihlásení, spustite:

```shell
systemctl enable --user ydotool
```

Uistite sa, že inštalujete dostatočne novú verziu `ydotool`. Verzie Ubuntu pred 26.04 poskytujú príliš staré verzie. Riešenie nájdete v diskusnom vlákne.

Prosím, diskutujte o `ydotool` v tomto diskusnom vlákne: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` je určený pre počítače s X11. X11 historicky používala väčšina linuxových počítačov, hoci Wayland je čoraz populárnejší.

Jednou zvláštnosťou X11 je, že možno budete musieť povoliť prístup k X serveru. To je problém, ak dostávate chyby „Authorization required“. Na vyriešenie tohto problému máte niekoľko možností; tu sú dve, ktoré u mňa fungovali:

Ak `xauth list` nezobrazuje žiadne záznamy, skúste vygenerovať súbor `.Xauthority`:

```shell
xauth generate :0 . trusted
```

Ak to nepomohlo, skúste udeliť prístup pomocou `xhost`:

```shell
xhost +local:$USER
```

Príkaz `xhost` budete musieť spustiť po každom štarte. Môžete to zautomatizovať vytvorením bash skriptu a jeho nastavením na automatické spustenie po prihlásení.

Prosím, diskutujte o `xdotool` v tomto diskusnom vlákne: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` je podobný `xdotool`, ale pre Wayland.

Za normálnych okolností nepodporuje ovládanie myšou, ale vytvoril som upravenú verziu, ktorá podporu myši pridáva. Ak potrebujete podporu myši, nainštalujte si ju: <https://github.com/stefansundin/wtype>

Ak sa zobrazí chyba `Compositor does not support the virtual keyboard protocol`, odporúčam vyskúšať iný nástroj, napríklad `ydotool`.

Prosím, diskutujte o `wtype` v tomto diskusnom vlákne: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` by mal fungovať s akýmkoľvek správcom okien, podobne ako `ydotool`. V mojom obmedzenom testovaní bol však výrazne pomalší než `ydotool`.

Prosím, diskutujte o `dotool` v tomto diskusnom vlákne: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Táto predvoľba je experimentálna, pretože som ju nemohol overiť na vlastnom hardvéri. Spätná väzba je vítaná.

Prosím, diskutujte o `cec-client` v tomto diskusnom vlákne: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

K dispozícii je predvoľba na ovládanie VLC v systéme macOS pomocou príkazov AppleScript. Nie som si vedomý nástroja, ktorý by podporoval odosielanie udalostí klávesnice alebo myši.

Prosím, diskutujte o macOS v tomto diskusnom vlákne: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Ak vaše zariadenie s Androidom obsahuje SSH server, možno sa k nemu budete môcť pripojiť a odosielať udalosti vstupu. Je to pravdepodobnejšie, ak vaše zariadenie používa vlastnú ROM, napríklad KonstaKANG na Raspberry Pi.

Zatiaľ som neprišiel na to, ako rozchodiť podporu myši.

Prosím, diskutujte o Androide v tomto diskusnom vlákne: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Inteligentné ovládanie hlasitosti

Pri úprave diaľkového ovládania nájdete v menu „inteligentné“ nastavenia hlasitosti. Môžu v aplikácii zobraziť aktuálnu hlasitosť počítača a umožniť vám rýchlo nastaviť hlasitosť pomocou posuvníka. Pomocou hardvérových tlačidiel zariadenia môžete tiež rýchlo odosielať príkazy na zvýšenie alebo zníženie hlasitosti.

Čítanie aktuálnej hlasitosti a nastavenie novej hlasitosti pomocou posuvníka je momentálne napevno naviazané na `pactl`.

Balík obsahujúci `pactl` sa zvyčajne nazýva `pulseaudio-utils` alebo `libpulse`.

## SSH kľúče

V nastaveniach aplikácie môžete SSH kľúče importovať alebo generovať. Pripájanie pomocou SSH kľúča je bezpečnejšie ako používanie hesiel.

Najjednoduchší spôsob, ako importovať existujúci SSH kľúč z počítača, je naskenovať QR kód. Na vygenerovanie obrázka QR kódu môžete použiť program `qrencode`. Spustite napríklad príkaz podobný tomuto:

```shell
# Prejdite do priečinka so svojimi SSH kľúčmi:
cd ~/.ssh

# Zobrazte QR kód v termináli:
qrencode -r id_ed25519 -t ansiutf8

# Prípadne vytvorte obrázkový súbor:
qrencode -r id_ed25519 -o qr.png

# 4096-bitové RSA kľúče sú na QR kód príliš veľké. Pomocou gzip ich tam môžete len tak-tak zmestiť:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Pomocou funkcie `Nahrať verejný kľúč` v menu môžete verejné SSH kľúče nahrať na server. Tým sa vybraný SSH kľúč pridá do súboru `~/.ssh/authorized_keys`. Takto môžete jednoducho prejsť z prihlasovania heslom na prihlasovanie pomocou SSH kľúča.

Môžete importovať a používať šifrované SSH kľúče, ale momentálne ich v aplikácii nemožno generovať.

## Bezpečnosť

Zo aplikácie nie je možné exportovať ani extrahovať súkromnú časť SSH kľúčov ani uložené heslá. Tieto údaje sú šifrované pomocou 256-bitového AES a šifrovací kľúč je uložený v Android Keystore. Šifrované údaje sú vylúčené z androidových záloh.

V tejto aplikácii nie je žiadny softvér na hlásenie pádov. Nie je tu žiadna telemetria. Nie sú tu reklamy. Nevykonávajú sa žiadne sieťové požiadavky okrem SSH pripojenia.

Bezpečnosť tejto aplikácie nebola auditovaná. Ak máte skúsenosti so zabezpečením Androidu alebo SSH, pozrite sa prosím na zdrojový kód a nahláste svoje zistenia v tomto issue na GitHube:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Požiadavky na funkcie

Neváhajte posielať požiadavky na nové funkcie a hlásenia chýb v GitHub repozitári. Prosím, používajte angličtinu. Zachovajte slušnosť. Neúctivé komentáre budú odstránené a používatelia môžu byť z repozitára zablokovaní.

Prezrite si existujúce issues a diskusné vlákna, aby ste zistili, či vaša otázka už nebola položená alebo zodpovedaná.

Prosím, buďte ohľaduplní. Túto aplikáciu som vytvoril vo svojom voľnom čase a dávam ju zadarmo. Túto aplikáciu budujem predovšetkým pre vlastné použitie.

Prosím, neposielajte mi otázky e-mailom. Skúste viesť konverzácie na GitHube, pretože to pomáha aj ostatným! Otázky môžete klásť v sekcii diskusií na GitHube.

Aplikáciu si môžete kedykoľvek forkovať a implementovať si vlastné funkcie. Je to skvelý spôsob, ako sa učiť. Zvážte prosím prispievanie užitočnými funkciami.

Zdrojový kód je licencovaný pod GNU GPLv3. Ak distribuujete upravené verzie tejto aplikácie, musíte sprístupniť aj zdrojový kód.

<https://github.com/stefansundin/SSHRemote>

## Dary

Ak chcete vyjadriť svoju vďačnosť a uznanie, môžete prispieť darom.

<https://stefansundin.github.io/donate/>

Ak ste prispeli, pokúsim sa čo najlepšie odpovedať na akúkoľvek otázku, ktorú môžete mať. Prosím, všetky otázky píšte po anglicky.

Ďakujem za vašu podporu!
