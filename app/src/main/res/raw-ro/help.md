## Despre SSH Remote

Această traducere a fost realizată cu ajutorul AI-ului GitHub Copilot, folosind un model AI al cărui nume exact nu este specificat în acest context, și poate conține erori de traducere.

SSH Remote este o aplicație gratuită și open source care îți permite să controlezi calculatoare de la distanță folosind SSH.

Poți personaliza complet comenzile care sunt executate și există presetări pentru configurații comune.

Eu folosesc această aplicație pentru a-mi controla configurația HTPC, care rulează Raspberry Pi OS. Controlarea unui HTPC este scenariul de bază pentru care aplicația este optimizată.

Această aplicație nu este un emulator de terminal, dar îți va permite să rulezi `apt-get install` în caz de urgență.

## Primii pași

Dacă vrei să folosești o cheie SSH pentru conectare, deschide mai întâi setările aplicației și importă sau generează o cheie.

Adaugă o gazdă nouă atingând butonul `+` din colțul din dreapta jos. Introdu detaliile conexiunii și salvează.

Prima dată când te conectezi, ți se va cere să selectezi o presetare. Această selecție configurează butoanele telecomenzii astfel încât să funcționeze bine pe diferite tipuri de calculatoare. Vezi mai jos o descriere a presetărilor disponibile. Dacă preferi, poți începe fără nicio configurație selectând `Fără presetare`.

Dacă nu știi dacă calculatorul tău Linux rulează X11 sau Wayland, rulează asta într-un terminal:

```shell
echo $XDG_SESSION_TYPE
```

Ar trebui să afișeze `x11` sau `wayland`. Trebuie să rulezi această comandă din interiorul mediului desktop.

## Telecomandă

După conectare, poți folosi interfața de telecomandă pentru a trimite comenzi. Schimbă filele pentru a accesa diferite metode de introducere.

Fiecare apăsare de buton va executa o comandă pe gazdă. Este un cost destul de mare pentru ceva atât de simplu precum apăsarea unei taste și este posibil să observi o latență destul de mare comparativ cu o tastatură obișnuită. Sper să îmbunătățesc acest lucru în versiunile viitoare.

Folosește meniul pentru a intra în modul de editare. În prezent nu este posibilă editarea aspectului sau a pictogramelor butoanelor. Sper să fac acest lucru posibil într-o versiune viitoare.

## Presetări

Va trebui să instalezi instrumentul necesar pentru mediul tău desktop.

Recomand `ydotool` deoarece, în testele mele, are cele mai bune performanțe și funcționează atât pe X11, cât și pe Wayland.

### ydotool

`ydotool` ar trebui să funcționeze cu orice manager de ferestre, dar ai nevoie de un serviciu de fundal pornit. Dacă distribuția ta oferă un serviciu systemd pentru utilizator, pornește-l rulând:

```shell
systemctl start --user ydotool
```

Pornește automat serviciul la autentificare rulând:

```shell
systemctl enable --user ydotool
```

Te rugăm să te asiguri că instalezi o versiune suficient de nouă de `ydotool`. Versiunile Ubuntu de dinainte de 26.04 oferă versiuni prea vechi. Vezi firul de discuție pentru o soluție alternativă.

Te rugăm să discuți despre `ydotool` în acest fir de discuție: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` este pentru calculatoare care rulează X11. X11 este ceea ce au folosit istoric majoritatea calculatoarelor Linux, deși Wayland devine din ce în ce mai popular.

O particularitate a X11 este că s-ar putea să fie nevoie să permiți accesul la serverul X. Aceasta este problema dacă primești erori de tipul „Authorization required”. Ai mai multe opțiuni pentru a rezolva această problemă; iată două opțiuni care au funcționat pentru mine:

Dacă `xauth list` nu afișează nicio intrare, încearcă să generezi un fișier `.Xauthority`:

```shell
xauth generate :0 . trusted
```

Dacă asta nu a funcționat, încearcă să permiți accesul folosind `xhost`:

```shell
xhost +local:$USER
```

Va trebui să rulezi comanda `xhost` după fiecare pornire. Poți automatiza acest lucru creând un script bash și configurându-l să pornească automat la autentificare.

Te rugăm să discuți despre `xdotool` în acest fir de discuție: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` este asemănător cu `xdotool`, dar pentru Wayland.

În mod normal, nu acceptă controlul mouse-ului, dar am creat o versiune modificată care adaugă suport pentru mouse. Te rugăm să o instalezi dacă ai nevoie de suport pentru mouse: <https://github.com/stefansundin/wtype>

Dacă primești eroarea `Compositor does not support the virtual keyboard protocol`, îți sugerez să încerci alt instrument, precum `ydotool`.

Te rugăm să discuți despre `wtype` în acest fir de discuție: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` ar trebui să funcționeze cu orice manager de ferestre, similar cu `ydotool`. În testele mele limitate, a fost mult mai lent decât `ydotool`.

Te rugăm să discuți despre `dotool` în acest fir de discuție: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Această presetare este experimentală, deoarece nu am reușit să o verific pe propriul meu hardware. Feedbackul este binevenit.

Te rugăm să discuți despre `cec-client` în acest fir de discuție: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Există o presetare pentru controlul VLC pe macOS, folosind comenzi AppleScript. Nu cunosc vreun instrument care să permită trimiterea de evenimente de tastatură sau mouse.

Te rugăm să discuți despre macOS în acest fir de discuție: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Dacă dispozitivul tău Android vine cu un server SSH, atunci s-ar putea să te poți conecta la el și să trimiți evenimente de intrare. Acest lucru este mai probabil dacă dispozitivul rulează un ROM personalizat, precum KonstaKANG pe Raspberry Pi.

Nu am reușit să aflu cum să fac suportul pentru mouse să funcționeze.

Te rugăm să discuți despre Android în acest fir de discuție: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Control inteligent al volumului

Când editezi telecomanda, poți găsi setările „inteligente” pentru controlul volumului în meniu. Acestea pot afișa volumul curent al calculatorului în aplicație și îți permit să setezi rapid volumul folosind un glisor. De asemenea, poți folosi butoanele hardware ale dispozitivului pentru a trimite rapid comenzi de creștere sau scădere a volumului.

Citirea volumului curent și setarea unui volum nou folosind glisorul sunt momentan configurate direct să folosească `pactl`.

Pachetul care conține `pactl` se numește de obicei `pulseaudio-utils` sau `libpulse`.

## Chei SSH

Poți importa sau genera chei SSH în setările aplicației. Conectarea cu o cheie SSH este mai sigură decât folosirea parolelor.

Cel mai simplu mod de a importa o cheie SSH existentă de pe un calculator este să scanezi un cod QR. Poți folosi programul `qrencode` pentru a genera imaginea codului QR. Rulează o comandă ca aceasta pentru a genera codul QR:

```shell
# Mergi în directorul cheilor tale SSH:
cd ~/.ssh

# Afișează codul QR în terminal:
qrencode -r id_ed25519 -t ansiutf8

# Alternativ, creează un fișier imagine:
qrencode -r id_ed25519 -o qr.png

# Cheile RSA de 4096 de biți sunt prea mari pentru un cod QR. Poți folosi gzip ca să încapă la limită:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Poți trimite chei SSH publice pe un server folosind funcția `Trimite cheia publică` din meniu. Aceasta va adăuga cheia SSH selectată la fișierul `~/.ssh/authorized_keys` al gazdei. Astfel poți trece ușor de la autentificarea cu parolă la autentificarea cu cheie SSH.

Poți importa și folosi chei SSH criptate, dar momentan nu le poți genera în aplicație.

## Securitate

Nu este posibil să exporți sau să extragi din aplicație partea privată a cheilor SSH sau parolele stocate. Aceste date sunt criptate folosind AES pe 256 de biți, iar cheia de criptare este stocată în Android Keystore. Datele criptate sunt excluse din copiile de siguranță Android.

În această aplicație nu există software de raportare a erorilor. Nu există telemetrie. Nu există reclame. Nu există cereri de rețea, cu excepția conexiunii SSH.

Securitatea acestei aplicații nu a fost auditată. Dacă ai experiență în securitatea Android sau SSH, te rugăm să arunci o privire peste codul sursă și să raportezi concluziile tale în această problemă GitHub:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Cereri de funcționalități

Te rugăm să trimiți cereri de funcționalități și rapoarte de erori în depozitul GitHub. Te rugăm să folosești limba engleză. Te rugăm să păstrezi comentariile civilizate. Comentariile lipsite de respect vor fi eliminate, iar utilizatorii pot fi blocați din depozit.

Te rugăm să parcurgi problemele și firele de discuție existente pentru a vedea dacă întrebarea ta a fost deja pusă sau a primit deja răspuns.

Te rugăm să fii respectuos. Am construit această aplicație în timpul meu liber și o ofer gratuit. Construiesc această aplicație pentru uzul meu personal, înainte de toate.

Te rugăm să nu îmi trimiți întrebări prin email. Încearcă să păstrezi conversațiile pe GitHub, deoarece asta îi ajută și pe alți oameni! Poți pune întrebări în secțiunea de discuții de pe GitHub.

Ești mereu binevenit să fork-uiești aplicația pentru a implementa propriile funcționalități. Este o modalitate excelentă de a învăța. Te rugăm să iei în considerare contribuirea cu funcționalități utile.

Codul sursă este licențiat sub GNU GPLv3. Dacă distribui versiuni modificate ale acestei aplicații, atunci trebuie să pui la dispoziție și codul sursă.

<https://github.com/stefansundin/SSHRemote>

## Donații

Dacă vrei să îți arăți gratitudinea și aprecierea, donațiile sunt acceptate.

<https://stefansundin.github.io/donate/>

Dacă ai făcut o donație, voi încerca să răspund cât pot de bine la orice întrebare ai putea avea. Te rugăm ca orice întrebare să fie scrisă în limba engleză.

Mulțumesc pentru sprijin!
