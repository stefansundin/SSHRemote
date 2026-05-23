## O aplikaci SSH Remote

Tento český překlad byl vytvořen pomocí AI nástroje GitHub Copilot (model v tomto prostředí nebyl specifikován), takže může obsahovat chyby v překladu.

SSH Remote je svobodná aplikace s otevřeným zdrojovým kódem, která vám umožňuje na dálku ovládat počítače pomocí SSH.

Můžete si plně přizpůsobit spouštěné příkazy a k dispozici jsou předvolby pro běžná nastavení.

Tuto aplikaci používám k ovládání svého HTPC, které běží na Raspberry Pi OS. Ovládání HTPC je základní scénář, pro který je aplikace optimalizována.

Tato aplikace není terminálový emulátor, ale v nouzi vám umožní spustit `apt-get install`.

## Začínáme

Pokud se chcete připojovat pomocí SSH klíče, nejprve otevřete nastavení aplikace a klíč importujte nebo vygenerujte.

Přidejte nového hostitele klepnutím na tlačítko `+` vpravo dole. Zadejte údaje pro připojení a uložte je.

Při prvním připojení budete vyzváni k výběru předvolby. Tato volba nakonfiguruje tlačítka dálkového ovládání tak, aby dobře fungovala na různých typech počítačů. Popis dostupných předvoleb najdete níže. Pokud chcete, můžete začít bez konfigurace výběrem `Žádná předvolba`.

Pokud nevíte, zda váš linuxový počítač používá X11 nebo Wayland, spusťte v terminálu toto:

```shell
echo $XDG_SESSION_TYPE
```

Měl by se vypsat `x11` nebo `wayland`. Tento příkaz musíte spustit uvnitř desktopového prostředí.

## Dálkové ovládání

Po připojení můžete pomocí rozhraní dálkového ovládání odesílat příkazy. Přepínáním karet získáte přístup k různým způsobům vstupu.

Každé stisknutí tlačítka spustí na hostiteli příkaz. Na něco tak jednoduchého, jako je stisk klávesy, je to poměrně velká režie, takže ve srovnání s běžnou klávesnicí můžete zaznamenat poměrně vysokou latenci. Doufám, že to v budoucích verzích zlepším.

Pomocí nabídky přejděte do režimu úprav. Rozložení ani ikony tlačítek zatím není možné upravovat. Doufám, že to bude možné v některé z budoucích verzí.

## Předvolby

Budete muset nainstalovat nástroj požadovaný vaším desktopovým prostředím.

Doporučuji `ydotool`, protože podle mých testů má nejlepší výkon a funguje na X11 i Waylandu.

### ydotool

`ydotool` by měl fungovat s jakýmkoli správcem oken, ale potřebujete spuštěnou službu na pozadí. Pokud vaše distribuce poskytuje uživatelskou službu systemd, spusťte ji příkazem:

```shell
systemctl start --user ydotool
```

Automatické spuštění služby při přihlášení nastavíte příkazem:

```shell
systemctl enable --user ydotool
```

Ujistěte se prosím, že instalujete dostatečně novou verzi `ydotool`. Verze Ubuntu před 26.04 poskytují příliš staré verze. Řešení najdete v příslušném diskusním vlákně.

Diskuzi o `ydotool` prosím veďte v tomto vlákně: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` je určen pro počítače s X11. X11 historicky používala většina linuxových počítačů, i když Wayland je stále populárnější.

Jedna zvláštnost X11 je, že možná budete muset povolit přístup k X serveru. To je pravděpodobný problém, pokud se zobrazují chyby „Authorization required“. Zde jsou dvě možnosti, které se mi osvědčily:

Pokud `xauth list` neukazuje žádné položky, zkuste vygenerovat soubor `.Xauthority`:

```shell
xauth generate :0 . trusted
```

Pokud to nepomohlo, zkuste udělit přístup pomocí `xhost`:

```shell
xhost +local:$USER
```

Příkaz `xhost` budete muset spustit po každém restartu. Můžete to zautomatizovat vytvořením skriptu pro bash a jeho nastavením ke spuštění při přihlášení.

Diskuzi o `xdotool` prosím veďte v tomto vlákně: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` je něco jako `xdotool`, ale pro Wayland.

Normálně nepodporuje ovládání myši, ale vytvořil jsem upravenou verzi, která podporu myši přidává. Pokud ovládání myši potřebujete, nainstalujte si ji: <https://github.com/stefansundin/wtype>

Pokud se zobrazí chyba `Compositor does not support the virtual keyboard protocol`, doporučuji zkusit jiný nástroj, například `ydotool`.

Diskuzi o `wtype` prosím veďte v tomto vlákně: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` by měl fungovat s jakýmkoli správcem oken, podobně jako `ydotool`. V mých omezených testech byl však mnohem pomalejší než `ydotool`.

Diskuzi o `dotool` prosím veďte v tomto vlákně: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Tato předvolba je experimentální, protože jsem ji nemohl ověřit na vlastním hardwaru. Zpětná vazba je vítána.

Diskuzi o `cec-client` prosím veďte v tomto vlákně: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Existuje předvolba pro ovládání VLC na macOS pomocí příkazů AppleScript. Nejsem si vědom žádného nástroje, který by podporoval odesílání událostí klávesnice nebo myši.

Diskuzi o macOS prosím veďte v tomto vlákně: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Pokud vaše zařízení s Androidem obsahuje SSH server, možná se k němu budete moci připojit a odesílat vstupní události. Je to pravděpodobnější, pokud zařízení používá vlastní ROM, například KonstaKANG na Raspberry Pi.

Zatím se mi nepodařilo zjistit, jak zprovoznit podporu myši.

Diskuzi o Androidu prosím veďte v tomto vlákně: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Chytré ovládání hlasitosti

Při úpravě dálkového ovládání najdete v nabídce „chytré“ nastavení hlasitosti. To může v aplikaci zobrazit aktuální hlasitost počítače a umožnit vám ji rychle nastavit pomocí posuvníku. Pomocí hardwarových tlačítek zařízení můžete také rychle odesílat příkazy pro zvýšení nebo snížení hlasitosti.

Čtení aktuální hlasitosti a nastavování nové hodnoty pomocí posuvníku je momentálně napevno nastaveno na použití `pactl`.

Balíček obsahující `pactl` se obvykle jmenuje `pulseaudio-utils` nebo `libpulse`.

## SSH klíče

V nastavení aplikace můžete SSH klíče importovat nebo generovat. Připojování pomocí SSH klíče je bezpečnější než používání hesel.

Nejjednodušší způsob, jak importovat existující SSH klíč z počítače, je naskenovat QR kód. K vygenerování obrázku QR kódu můžete použít program `qrencode`. Následující příkaz ukazuje jeden z možných postupů:

```shell
# Přejděte do adresáře se svými SSH klíči:
cd ~/.ssh

# Zobrazte QR kód v terminálu:
qrencode -r id_ed25519 -t ansiutf8

# Případně vytvořte soubor s obrázkem:
qrencode -r id_ed25519 -o qr.png

# 4096bitové RSA klíče jsou pro QR kód příliš velké. Pomocí gzipu se jeden vejde jen tak tak:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Veřejné SSH klíče můžete nahrát na server pomocí funkce `Nahrát veřejný klíč` v nabídce. Tím se vybraný SSH klíč připojí do souboru `~/.ssh/authorized_keys`. Díky tomu můžete snadno přejít od přihlašování heslem k přihlašování pomocí SSH klíče.

Můžete importovat a používat šifrované SSH klíče, ale aplikace je zatím neumí generovat.

## Zabezpečení

Není možné z aplikace exportovat ani získat soukromou část SSH klíčů nebo uložená hesla. Tato data jsou šifrována pomocí 256bitového AES a šifrovací klíč je uložen v Android Keystore. Šifrovaná data jsou z androidích záloh vyloučena.

V této aplikaci není žádný software pro hlášení pádů. Není zde žádná telemetrie. Nejsou zde žádné reklamy. Neprobíhají žádné síťové požadavky kromě SSH připojení.

Zabezpečení této aplikace nebylo auditováno. Pokud máte zkušenosti s bezpečností Androidu nebo SSH, podívejte se prosím do zdrojového kódu a své poznatky nahlaste v tomto issue na GitHubu:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Požadavky na funkce

Neváhejte posílat návrhy funkcí a hlášení chyb v repozitáři na GitHubu. Používejte prosím angličtinu. Zachovejte prosím slušnost. Neuctivé komentáře budou odstraněny a uživatelé mohou být z repozitáře zablokováni.

Podívejte se prosím na existující issue a diskusní vlákna, zda už váš dotaz nebyl položen nebo zodpovězen.

Buďte prosím ohleduplní. Tuto aplikaci jsem vytvořil ve svém volném čase a dávám ji zdarma k dispozici. Tuto aplikaci vyvíjím především pro vlastní potřebu.

Prosím, neposílejte mi dotazy e-mailem. Snažte se konverzace udržet na GitHubu, protože to pomáhá i ostatním! Otázky můžete klást v sekci Discussions na GitHubu.

Aplikaci si samozřejmě můžete forknout a implementovat si vlastní funkce. Je to skvělý způsob, jak se učit. Zvažte prosím přispění užitečnými funkcemi.

Zdrojový kód je licencován pod GNU GPLv3. Pokud distribuujete upravené verze této aplikace, musíte také zpřístupnit zdrojový kód.

<https://github.com/stefansundin/SSHRemote>

## Dary

Pokud chcete vyjádřit svou vděčnost a uznání, můžete přispět.

<https://stefansundin.github.io/donate/>

Pokud jste přispěli, udělám vše pro to, abych odpověděl na jakýkoli váš dotaz. Pokud mi budete psát s dotazem, napište prosím anglicky.

Děkuji za vaši podporu!
