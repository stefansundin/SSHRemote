## O aplikaciji SSH Remote

_Ovaj prevod je napravljen uz pomoć GitHub Copilot AI asistenta i može sadržati greške u prevodu._

SSH Remote je besplatna aplikacija otvorenog koda koja vam omogućava da daljinski upravljate računarima putem SSH-a.

Možete u potpunosti da prilagodite komande koje se izvršavaju, a dostupni su i preseti za uobičajene konfiguracije.

Koristim ovu aplikaciju za upravljanje svojim HTPC podešavanjem, koje radi na Raspberry Pi OS-u. Upravljanje HTPC-om je osnovni scenario za koji je aplikacija optimizovana.

Ova aplikacija nije terminal emulator, ali će vam omogućiti da u hitnom slučaju pokrenete `apt-get install`.

## Početak

Ako želite da za povezivanje koristite SSH ključ, prvo otvorite podešavanja aplikacije i uvezite ili generišite ključ.

Dodajte novi host tako što ćete dodirnuti dugme `+` u donjem desnom uglu. Unesite detalje veze i sačuvajte ih.

Kada se prvi put povežete, bićete upitani da izaberete preset. Ovaj izbor podešava dugmad daljinskog upravljača tako da dobro rade na različitim tipovima računara. U nastavku pogledajte opis dostupnih preseta. Ako želite, možete da počnete i bez konfiguracije tako što ćete izabrati `Bez preseta`.

Ako ne znate da li vaš Linux računar koristi X11 ili Wayland, pokrenite ovo u terminalu:

```shell
echo $XDG_SESSION_TYPE
```

Ovo bi trebalo da ispiše `x11` ili `wayland`. Ovu komandu morate da pokrenete unutar desktop okruženja.

## Daljinski upravljač

Kada se povežete, možete koristiti interfejs daljinskog upravljača za slanje komandi. Menjajte kartice da biste pristupili različitim metodama unosa.

Svaki pritisak na dugme izvršava komandu na hostu. To donosi dosta režijskog troška za nešto jednostavno kao što je pritisak na taster, pa možete primetiti prilično veliko kašnjenje u poređenju sa običnom tastaturom. Nadam se da ću to poboljšati u budućim verzijama.

Koristite meni da biste ušli u režim uređivanja. Trenutno nije moguće uređivati raspored ili ikonice dugmadi. Nadam se da ću to omogućiti u nekoj budućoj verziji.

## Preseti

Morate da instalirate alat potreban za vaše desktop okruženje.

Preporučujem `ydotool` zato što je u mom testiranju imao najbolje performanse i radi i na X11 i na Wayland-u.

### ydotool

`ydotool` bi trebalo da radi sa bilo kojim upravljačem prozora, ali vam je potrebna pozadinska usluga koja radi. Ako vaša distribucija nudi systemd korisničku uslugu, pokrenite je ovom komandom:

```shell
systemctl start --user ydotool
```

Da biste automatski pokrenuli uslugu pri prijavljivanju, pokrenite:

```shell
systemctl enable --user ydotool
```

Vodite računa da instalirate dovoljno novu verziju `ydotool` alata. Verzije na Ubuntu-u pre 26.04 su previše stare. Pogledajte temu za diskusiju radi zaobilaznog rešenja.

Molimo da o `ydotool` alatu diskutujete u ovoj temi za diskusiju: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` je za računare koji koriste X11. X11 je ono što je većina Linux računara istorijski koristila, iako Wayland postaje sve popularniji.

Jedna specifičnost X11-a je da možda morate da dozvolite pristup X serveru. To je verovatno problem ako dobijate greške `Authorization required`. Imate nekoliko načina da rešite ovaj problem; evo dve opcije koje su meni funkcionisale:

Ako `xauth list` ne prikazuje nijedan unos, pokušajte da generišete `.Xauthority` datoteku:

```shell
xauth generate :0 . trusted
```

Ako to nije pomoglo, pokušajte da odobrite pristup pomoću `xhost`:

```shell
xhost +local:$USER
```

Moraćete da pokrenete komandu `xhost` posle svakog podizanja sistema. Ovo možete automatizovati tako što ćete napraviti bash skriptu i podesiti da se automatski pokreće pri prijavljivanju.

Molimo da o `xdotool` alatu diskutujete u ovoj temi za diskusiju: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` je kao `xdotool`, ali za Wayland.

Obično ne podržava upravljanje mišem, ali sam napravio izmenjenu verziju koja dodaje podršku za miš. Instalirajte je ako vam je potrebna podrška za miš: <https://github.com/stefansundin/wtype>

Ako dobijete grešku `Compositor does not support the virtual keyboard protocol`, predlažem da probate neki drugi alat, kao što je `ydotool`.

Molimo da o `wtype` alatu diskutujete u ovoj temi za diskusiju: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` bi trebalo da radi sa bilo kojim upravljačem prozora, slično kao `ydotool`. U mom ograničenom testiranju bio je znatno sporiji od `ydotool` alata.

Molimo da o `dotool` alatu diskutujete u ovoj temi za diskusiju: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Ovaj preset je eksperimentalan jer nisam uspeo da ga proverim na sopstvenom hardveru. Povratne informacije su dobrodošle.

Molimo da o `cec-client` alatu diskutujete u ovoj temi za diskusiju: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Postoji preset za upravljanje VLC-om na macOS-u, koji koristi AppleScript komande. Nisam upoznat sa alatom koji podržava slanje događaja sa tastature ili miša.

Molimo da o macOS-u diskutujete u ovoj temi za diskusiju: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Ako vaš Android uređaj dolazi sa SSH serverom, možda ćete moći da se povežete sa njim i šaljete događaje unosa. To je verovatnije ako vaš uređaj koristi prilagođeni ROM, kao što je KonstaKANG na Raspberry Pi-ju.

Nisam uspeo da pronađem kako da osposobim podršku za miš.

Molimo da o Android-u diskutujete u ovoj temi za diskusiju: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Pametna kontrola jačine zvuka

Kada uređujete daljinski upravljač, u meniju možete pronaći „pametna“ podešavanja jačine zvuka. To može da prikaže trenutnu jačinu zvuka računara u aplikaciji i da vam omogući da je brzo podesite pomoću klizača. Takođe možete koristiti hardversku dugmad uređaja da brzo pošaljete komande za pojačavanje i smanjivanje zvuka.

Čitanje trenutne jačine zvuka i postavljanje nove vrednosti pomoću klizača trenutno je fiksno podešeno da koristi `pactl`.

Paket koji sadrži `pactl` obično se zove `pulseaudio-utils` ili `libpulse`.

## SSH ključevi

Možete uvesti ili generisati SSH ključeve u podešavanjima aplikacije. Povezivanje pomoću SSH ključa je bezbednije nego korišćenje lozinki.

Najlakši način da uvezete postojeći SSH ključ sa računara jeste da skenirate QR kod. Možete koristiti program `qrencode` da generišete sliku QR koda. Pokrenite komandu poput sledeće da biste generisali QR kod:

```shell
# Pređite u direktorijum sa SSH ključevima:
cd ~/.ssh

# Prikažite QR kod u terminalu:
qrencode -r id_ed25519 -t ansiutf8

# Alternativno, napravite datoteku slike:
qrencode -r id_ed25519 -o qr.png

# 4096-bitni RSA ključevi su preveliki za QR kod. Možete koristiti gzip da jedan jedva stane:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Možete da pošaljete javne SSH ključeve na server pomoću funkcije `Pošalji javni ključ` u meniju. Time će izabrani SSH ključ biti dodat u datoteku `~/.ssh/authorized_keys`. To vam omogućava da lako pređete sa prijavljivanja lozinkom na prijavljivanje SSH ključem.

Možete da uvezete i koristite šifrovane SSH ključeve, ali ih trenutno ne možete generisati u aplikaciji.

## Bezbednost

Nije moguće izvesti niti izvući privatni deo SSH ključeva ili sačuvanih lozinki iz aplikacije. Ovi podaci su šifrovani pomoću 256-bitnog AES-a, a ključ za šifrovanje čuva se u Android Keystore-u. Šifrovani podaci se isključuju iz Android rezervnih kopija.

U ovoj aplikaciji ne postoji softver za prijavljivanje rušenja. Nema telemetrije. Nema oglasa. Nema mrežnih zahteva osim SSH veze.

Bezbednost ove aplikacije nije auditovana. Ako imate iskustva sa Android bezbednošću ili SSH bezbednošću, pogledajte izvorni kod i prijavite svoje nalaze u ovom GitHub issue-u:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Zahtevi za funkcije

Slobodno pošaljite zahteve za funkcije i prijave grešaka u GitHub repozitorijumu. Molimo koristite engleski jezik. Molimo vas da komentari budu pristojni. Komentari bez poštovanja biće uklonjeni, a korisnicima može biti zabranjen pristup repozitorijumu.

Pogledajte postojeće issue-e i teme za diskusiju da biste videli da li je vaše pitanje već postavljeno ili na njega već postoji odgovor.

Budite obzirni. Napravio sam ovu aplikaciju u svoje slobodno vreme i dajem je besplatno. Pravim ovu aplikaciju prvenstveno za sopstvenu upotrebu.

Molim vas da mi ne šaljete pitanja imejlom. Pokušajte da razgovore zadržite na GitHub-u, jer to pomaže i drugim ljudima! Pitanja možete postavljati u odeljku za diskusije na GitHub-u.

Uvek ste dobrodošli da fork-ujete aplikaciju kako biste implementirali sopstvene funkcije. To je sjajan način za učenje. Razmislite o tome da doprinesete korisnim funkcijama.

Izvorni kod je licenciran pod GNU GPLv3. Ako distribuirate izmenjene verzije ove aplikacije, morate takođe da učinite izvorni kod dostupnim.

<https://github.com/stefansundin/SSHRemote>

## Donacije

Ako želite da pokažete zahvalnost i uvažavanje, donacije su dobrodošle.

<https://stefansundin.github.io/donate/>

Ako ste donirali, potrudiću se da odgovorim na svako pitanje koje imate. Molim vas da sva pitanja pišete na engleskom jeziku.

Hvala vam na podršci!
