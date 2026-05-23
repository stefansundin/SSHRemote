## O aplikaciji SSH Remote

Ovaj prijevod je generiran uz pomoć umjetne inteligencije putem GitHub Copilota (GPT-5.3-Codex) i može sadržavati pogreške u prijevodu.

SSH Remote je besplatna i otvorenog koda aplikacija koja vam omogućuje daljinsko upravljanje računalima putem SSH-a.

Možete potpuno prilagoditi naredbe koje se izvršavaju, a postoje i unaprijed postavljene konfiguracije za uobičajene postavke.

Ovu aplikaciju koristim za upravljanje svojim HTPC sustavom koji radi na Raspberry Pi OS-u. Upravljanje HTPC-jem je osnovni scenarij za koji je aplikacija optimizirana.

Ova aplikacija nije terminalski emulator, ali će vam omogućiti da u hitnim slučajevima pokrenete `apt-get install`.

## Početak rada

Ako želite koristiti SSH ključ za povezivanje, prvo otvorite postavke aplikacije i uvezite ili generirajte ključ.

Dodajte novi host pritiskom na gumb `+` u donjem desnom kutu. Unesite podatke za povezivanje i spremite.

Prvi put kada se povežete, bit ćete zamoljeni da odaberete unaprijed postavljeno. Taj odabir konfigurira gumbe daljinskog upravljanja tako da dobro rade na različitim vrstama računala. U nastavku je opis dostupnih unaprijed postavljenih postavki. Ako želite, možete započeti bez konfiguracije odabirom opcije `Bez unaprijed postavljenog`.

Ako ne znate koristi li vaše Linux računalo X11 ili Wayland, pokrenite ovo u terminalu:

```shell
echo $XDG_SESSION_TYPE
```

Ovo bi trebalo ispisati `x11` ili `wayland`. To morate pokrenuti unutar radnog okruženja.

## Daljinsko upravljanje

Nakon povezivanja možete koristiti sučelje daljinskog upravljanja za slanje naredbi. Prebacujte se između kartica za pristup različitim načinima unosa.

Svaki pritisak gumba izvršit će naredbu na hostu. To stvara poprilično veliko opterećenje za nešto tako jednostavno kao što je pritisak tipke, pa možete osjetiti prilično veliku latenciju u usporedbi s običnom tipkovnicom. Nadam se da ću to poboljšati u budućim verzijama.

Koristite izbornik za ulazak u način uređivanja. Trenutno nije moguće uređivati raspored niti ikone gumba. Nadam se da ću to omogućiti u budućoj verziji.

## Unaprijed postavljeno

Morat ćete instalirati alat potreban za vaše radno okruženje.

Preporučujem `ydotool` jer je, prema mojim testovima, najbrži, a radi i na X11 i na Waylandu.

### ydotool

`ydotool` bi trebao raditi sa svim upraviteljima prozora, ali treba vam pozadinska usluga. Ako vaša distribucija nudi systemd korisničku uslugu, pokrenite je ovako:

```shell
systemctl start --user ydotool
```

Automatsko pokretanje usluge pri prijavi omogućite ovako:

```shell
systemctl enable --user ydotool
```

Pazite da instalirate dovoljno novu verziju `ydotoola`. Ubuntu verzije prije 26.04 nude verzije koje su prestarjele. Pogledajte temu rasprave za zaobilazno rješenje.

Molimo raspravljajte o `ydotoolu` u ovoj temi rasprave: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` je za računala koja koriste X11. X11 je povijesno ono što je većina Linux računala koristila, iako Wayland postaje sve popularniji.

Jedna posebnost X11-a je da ćete možda morati dopustiti pristup X poslužitelju. To je problem ako dobijete pogrešku "Authorization required". Imate nekoliko opcija za rješavanje tog problema, a ovdje su dvije koje su meni radile:

Ako `xauth list` ne prikazuje nijedan unos, pokušajte generirati `.Xauthority` datoteku:

```shell
xauth generate :0 . trusted
```

Ako to nije pomoglo, pokušajte dopustiti pristup pomoću `xhost`:

```shell
xhost +local:$USER
```

Naredbu `xhost` morat ćete pokrenuti nakon svakog pokretanja sustava. To možete automatizirati stvaranjem bash skripte i konfiguriranjem njezina automatskog pokretanja pri prijavi.

Molimo raspravljajte o `xdotoolu` u ovoj temi rasprave: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` je poput `xdotoola`, ali za Wayland.

Uobičajeno ne podržava upravljanje mišem, ali sam izradio izmijenjenu verziju koja dodaje podršku za miš. Instalirajte je ako vam je potrebna podrška za miš: <https://github.com/stefansundin/wtype>

Ako dobijete pogrešku `Compositor does not support the virtual keyboard protocol`, preporučujem da isprobate neki drugi alat, poput `ydotoola`.

Molimo raspravljajte o `wtypeu` u ovoj temi rasprave: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` bi trebao raditi sa svim upraviteljima prozora, slično kao `ydotool`. U mojim ograničenim testovima bio je mnogo sporiji od `ydotoola`.

Molimo raspravljajte o `dotoolu` u ovoj temi rasprave: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Ova je unaprijed postavljena konfiguracija eksperimentalna jer je nisam uspio provjeriti na vlastitom hardveru. Povratne informacije su dobrodošle.

Molimo raspravljajte o `cec-clientu` u ovoj temi rasprave: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Postoji unaprijed postavljena konfiguracija za upravljanje VLC-om na macOS-u, uz korištenje AppleScript naredbi. Nije mi poznat alat koji podržava slanje tipkovnih ili mišićnih događaja.

Molimo raspravljajte o macOS-u u ovoj temi rasprave: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Ako vaš Android uređaj dolazi sa SSH poslužiteljem, možda ćete se moći povezati s njim i slati ulazne događaje. To je vjerojatnije ako uređaj koristi prilagođeni ROM, poput KonstaKANG-a na Raspberry Pi-ju.

Nisam uspio pronaći način da mi upravljanje mišem radi.

Molimo raspravljajte o Androidu u ovoj temi rasprave: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Pametno upravljanje glasnoćom

Prilikom uređivanja daljinskog upravljanja u izborniku možete pronaći postavke "pametnog" upravljanja glasnoćom. To može prikazati trenutačnu glasnoću računala u aplikaciji i omogućiti vam da koristite hardverske gumbe uređaja za brzo slanje naredbi za smanjenje/povećanje glasnoće. Čitanje glasnoće trenutačno je tvrdo zadano na `pactl`.

Paket koji sadrži `pactl` obično se zove `pulseaudio-utils` ili `libpulse`.

## SSH ključevi

SSH ključeve možete uvesti ili generirati u postavkama aplikacije. Povezivanje pomoću SSH ključa sigurnije je od korištenja lozinki.

Najlakši način za uvoz postojećeg SSH ključa s računala je skeniranje QR koda. Za generiranje slike QR koda možete koristiti program `qrencode`. Pokrenite naredbu poput sljedeće za generiranje QR koda:

```shell
# Idite do svojih SSH ključeva:
cd ~/.ssh

# Prikaži QR kod u terminalu:
qrencode -r id_ed25519 -t ansiutf8

# Ili stvorite slikovnu datoteku:
qrencode -r id_ed25519 -o qr.png

# RSA ključevi od 4096 bita preveliki su za QR kod. Možete koristiti gzip da ga jedva uklopite:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Javni SSH ključevi mogu se poslati na poslužitelj pomoću značajke `Pošalji javni ključ` u izborniku. Time će se odabrani SSH ključ dodati u datoteku `~/.ssh/authorized_keys`. Tako možete jednostavno prijeći s prijave lozinkom na prijavu SSH ključem.

Šifrirane SSH ključeve možete uvesti i koristiti, ali ih trenutačno ne možete generirati u aplikaciji.

## Sigurnost

Nije moguće izvesti ili izdvojiti privatni dio SSH ključeva ili spremljene lozinke iz aplikacije. Ti su podaci šifrirani 256-bitnim AES-om, a ključ za šifriranje pohranjen je u Android Keystoreu. Šifrirani podaci isključeni su iz Android sigurnosnih kopija.

U ovoj aplikaciji nema softvera za prijavu rušenja. Nema telemetrije. Nema oglasa. Nema mrežnih zahtjeva osim SSH veze.

Sigurnost ove aplikacije nije audirana. Ako imate iskustva s Android sigurnošću ili SSH sigurnošću, molimo pregledajte izvorni kod i prijavite svoja saznanja u ovom GitHub pitanju:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Zahtjevi za nove značajke

Slobodno pošaljite zahtjeve za nove značajke i prijave grešaka u GitHub repozitorij. Molimo koristite engleski. Molimo budite uljudni. Neuljudni komentari bit će uklonjeni, a korisnici mogu biti blokirani iz repozitorija.

Molimo pregledajte postojeća pitanja i teme rasprave kako biste vidjeli je li vaše pitanje već postavljeno ili odgovoreno.

Molimo budite obzirni. Ovu sam aplikaciju izradio u slobodno vrijeme i dijelim je besplatno. Prije svega je izrađujem za vlastitu upotrebu.

Molimo nemojte mi slati pitanja e-poštom. Pokušajte zadržati razgovore na GitHubu, jer to pomaže i drugim ljudima! Pitanja možete postaviti u odjeljku rasprava na GitHubu.

Uvijek ste dobrodošli forkati aplikaciju i implementirati vlastite značajke. To je odličan način za učenje. Slobodno doprinijesite korisnim značajkama.

Izvorni kod licenciran je pod GNU GPLv3. Ako distribuirate izmijenjene verzije ove aplikacije, također morate učiniti izvorni kod dostupnim.

<https://github.com/stefansundin/SSHRemote>

## Donacije

Ako želite pokazati svoju zahvalnost i podršku, donacije su dobrodošle.

<https://stefansundin.github.io/donate/>

Ako ste donirali, potrudit ću se odgovoriti na sva vaša pitanja. Molimo postavite sva dodatna pitanja na engleskom.

Hvala na podršci!
