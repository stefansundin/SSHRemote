## O SSH Remote aplikaciji

Ovaj prevod je napravljen uz pomoć AI alata i može sadržavati greške u prevodu.

SSH Remote je besplatna aplikacija otvorenog koda koja vam omogućava da daljinski upravljate računarima koristeći SSH.

Možete u potpunosti prilagoditi komande koje se izvršavaju, a dostupna su i unaprijed postavljena podešavanja za uobičajene konfiguracije.

Ja koristim ovu aplikaciju za upravljanje svojim HTPC sistemom koji koristi Raspberry Pi OS. Upravljanje HTPC-om je osnovni scenarij za koji je aplikacija optimizirana.

Ova aplikacija nije emulator terminala, ali će vam omogućiti da u hitnom slučaju pokrenete `apt-get install`.

## Početak rada

Ako želite koristiti SSH ključ za povezivanje, prvo otvorite postavke aplikacije i uvezite ili generirajte ključ.

Dodajte novi host dodirom na dugme `+` u donjem desnom uglu. Unesite detalje povezivanja i sačuvajte.

Prvi put kada se povežete, bit će vam ponuđeno da odaberete unaprijed postavljeno podešavanje. Ovaj izbor konfigurira dugmad daljinskog upravljača tako da dobro rade na različitim vrstama računara. U nastavku pogledajte opis dostupnih unaprijed postavljenih podešavanja. Ako želite, možete početi bez konfiguracije tako što ćete odabrati `Bez unaprijed postavljenog`.

Ako ne znate da li vaš Linux računar koristi X11 ili Wayland, pokrenite ovo u terminalu:

```shell
echo $XDG_SESSION_TYPE
```

Ovo bi trebalo ispisati `x11` ili `wayland`. Ovu komandu morate pokrenuti unutar desktop okruženja.

## Daljinski upravljač

Kada se povežete, možete koristiti interfejs daljinskog upravljača za slanje komandi. Prebacujte kartice za pristup različitim načinima unosa.

Svaki pritisak na dugme izvršit će komandu na hostu. To je dosta opterećenja za nešto tako jednostavno kao što je pritisak na tipku, pa možete primijetiti prilično veliku latenciju u odnosu na običnu tastaturu. Nadam se da ću to poboljšati u budućim verzijama.

Koristite meni da uđete u način uređivanja. Trenutno nije moguće uređivati raspored ili ikonice dugmadi. Nadam se da ću to omogućiti u nekoj budućoj verziji.

## Unaprijed postavljena podešavanja

Morat ćete instalirati alat potreban za vaše desktop okruženje.

Preporučujem `ydotool` jer je u mojim testovima imao najbolje performanse i radi i na X11 i na Waylandu.

### ydotool

`ydotool` bi trebao raditi s bilo kojim upraviteljem prozora, ali morate imati pokrenut servis u pozadini. Ako vaša distribucija nudi systemd korisnički servis, pokrenite ga ovako:

```shell
systemctl start --user ydotool
```

Da se servis automatski pokrene pri prijavi, pokrenite:

```shell
systemctl enable --user ydotool
```

Molimo provjerite da instalirate dovoljno novu verziju `ydotool` alata. Verzije Ubuntua prije 26.04 nude verzije koje su prestare. Pogledajte diskusionu temu za zaobilazno rješenje.

Molimo razgovarajte o `ydotool` alatu u ovoj diskusionoj temi: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` je za računare koji koriste X11. X11 je ono što je većina Linux računara historijski koristila, iako Wayland postaje sve popularniji.

Jedna specifičnost X11 sistema je da ćete možda morati dozvoliti pristup X serveru. To je vjerovatno problem ako dobijate greške tipa "Authorization required". Imate nekoliko opcija da riješite ovaj problem; evo dvije koje su meni funkcionisale:

Ako `xauth list` ne prikazuje nikakve unose, pokušajte generirati `.Xauthority` datoteku:

```shell
xauth generate :0 . trusted
```

Ako to nije pomoglo, pokušajte odobriti pristup pomoću `xhost`:

```shell
xhost +local:$USER
```

Morat ćete pokrenuti `xhost` komandu nakon svakog pokretanja sistema. Ovo možete automatizirati tako što ćete napraviti bash skriptu i podesiti da se automatski pokreće pri prijavi.

Molimo razgovarajte o `xdotool` alatu u ovoj diskusionoj temi: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` je poput `xdotool` alata, ali za Wayland.

Normalno ne podržava upravljanje mišem, ali sam napravio izmijenjenu verziju koja dodaje podršku za miš. Instalirajte je ako vam treba podrška za miš: <https://github.com/stefansundin/wtype>

Ako dobijete grešku `Compositor does not support the virtual keyboard protocol`, predlažem da probate drugi alat, poput `ydotool`.

Molimo razgovarajte o `wtype` alatu u ovoj diskusionoj temi: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` bi trebao raditi s bilo kojim upraviteljem prozora, slično kao `ydotool`. U mom ograničenom testiranju bio je mnogo sporiji od `ydotool` alata.

Molimo razgovarajte o `dotool` alatu u ovoj diskusionoj temi: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Ovo unaprijed postavljeno podešavanje je eksperimentalno jer ga nisam uspio provjeriti na vlastitom hardveru. Povratne informacije su dobrodošle.

Molimo razgovarajte o `cec-client` alatu u ovoj diskusionoj temi: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Postoji unaprijed postavljeno podešavanje za upravljanje VLC-om na macOS-u, koristeći AppleScript komande. Nisam upoznat s alatom koji podržava slanje događaja tastature ili miša.

Molimo razgovarajte o macOS-u u ovoj diskusionoj temi: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Ako vaš Android uređaj ima SSH server, možda ćete se moći povezati na njega i slati događaje unosa. To je vjerovatnije ako vaš uređaj koristi prilagođeni ROM, kao što je KonstaKANG na Raspberry Pi-u.

Nisam uspio otkriti kako da podrška za miš radi.

Molimo razgovarajte o Androidu u ovoj diskusionoj temi: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Pametna kontrola glasnoće

Prilikom uređivanja daljinskog upravljača u meniju možete pronaći "pametne" postavke glasnoće. To može prikazati trenutnu glasnoću računara u aplikaciji i omogućiti vam da brzo postavite glasnoću pomoću klizača. Također možete koristiti hardverska dugmad svog uređaja da brzo pošaljete komande za pojačavanje/smanjivanje glasnoće.

Čitanje trenutne glasnoće i postavljanje nove glasnoće pomoću klizača trenutno je hardkodirano da koristi `pactl`.

Paket koji sadrži `pactl` obično se zove `pulseaudio-utils` ili `libpulse`.

## SSH ključevi

Možete uvesti ili generirati SSH ključeve u postavkama aplikacije. Povezivanje pomoću SSH ključa sigurnije je od korištenja lozinki.

Najlakši način da uvezete postojeći SSH ključ s računara jeste da skenirate QR kod. Možete koristiti program `qrencode` za generiranje slike QR koda. Pokrenite komandu poput sljedeće da generirate sliku QR koda:

```shell
# Idite u direktorij sa svojim SSH ključevima:
cd ~/.ssh

# Prikažite QR kod u terminalu:
qrencode -r id_ed25519 -t ansiutf8

# Alternativno, napravite datoteku slike:
qrencode -r id_ed25519 -o qr.png

# RSA ključevi od 4096 bita su preveliki za QR kod. Možete koristiti gzip da jedan jedva stane:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Možete poslati javne SSH ključeve na server koristeći opciju `Pošalji javni ključ` u meniju. Ovo će dodati odabrani SSH ključ u datoteku `~/.ssh/authorized_keys`. To vam omogućava da se lako prebacite s prijave lozinkom na prijavu pomoću SSH ključa.

Možete uvoziti i koristiti šifrirane SSH ključeve, ali ih trenutno ne možete generirati unutar aplikacije.

## Sigurnost

Nije moguće izvesti ili izdvojiti privatni dio SSH ključeva niti sačuvane lozinke iz aplikacije. Ti podaci su šifrirani korištenjem 256-bitnog AES-a, a ključ za šifriranje pohranjen je u Android Keystore-u. Šifrirani podaci su isključeni iz Android sigurnosnih kopija.

U ovoj aplikaciji nema softvera za prijavu rušenja. Nema telemetrije. Nema reklama. Nema mrežnih zahtjeva osim SSH veze.

Sigurnost ove aplikacije nije revidirana. Ako imate iskustva s Android sigurnošću ili SSH sigurnošću, pogledajte izvorni kod i prijavite svoja zapažanja u ovom GitHub problemu:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Zahtjevi za funkcije

Slobodno pošaljite zahtjeve za nove funkcije i prijave grešaka u GitHub repozitorij. Molimo koristite engleski jezik. Molimo ostanite pristojni u komentarima. Nepristojni komentari bit će uklonjeni, a korisnici mogu biti blokirani iz repozitorija.

Pogledajte postojeće probleme i diskusione teme da provjerite da li je vaše pitanje već postavljeno ili odgovoreno.

Molimo budite puni poštovanja. Ovu aplikaciju sam napravio u svoje slobodno vrijeme i dajem je besplatno. Prije svega, pravim ovu aplikaciju za vlastite potrebe.

Molimo nemojte mi slati pitanja e-poštom. Pokušajte razgovore zadržati na GitHubu, jer to pomaže i drugim ljudima! Pitanja možete postaviti u sekciji za diskusije na GitHubu.

Uvijek ste dobrodošli da forkate aplikaciju kako biste implementirali vlastite funkcije. To je sjajan način za učenje. Razmotrite doprinos korisnim funkcijama.

Izvorni kod je licenciran pod GNU GPLv3. Ako distribuirate izmijenjene verzije ove aplikacije, tada morate učiniti dostupnim i izvorni kod.

<https://github.com/stefansundin/SSHRemote>

## Donacije

Ako želite pokazati svoju zahvalnost i podršku, donacije su prihvaćene.

<https://stefansundin.github.io/donate/>

Ako ste donirali, potrudit ću se da odgovorim na svako vaše pitanje. Molimo pišite upite na engleskom jeziku.

Hvala vam na podršci!
