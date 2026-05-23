## Informazioni su SSH Remote

Questa traduzione italiana è stata realizzata con GitHub Copilot (modello AI) e potrebbe contenere errori di traduzione.

SSH Remote è un'app gratuita e open source che ti permette di controllare computer da remoto tramite SSH.

Puoi personalizzare completamente i comandi eseguiti e sono disponibili preset per le configurazioni più comuni.

Io uso quest'app per controllare la mia configurazione HTPC, che esegue Raspberry Pi OS. Il controllo di un HTPC è lo scenario principale per cui l'app è stata ottimizzata.

Questa app non è un emulatore di terminale, ma in caso di emergenza ti permette di eseguire `apt-get install`.

## Per iniziare

Se vuoi usare una chiave SSH per connetterti, apri prima le impostazioni dell'app e importa o genera una chiave.

Aggiungi un nuovo host toccando il pulsante `+` in basso a destra. Inserisci i dettagli di connessione e salva.

La prima volta che ti connetti, ti verrà chiesto di selezionare un preset. Questa scelta configura i pulsanti del telecomando affinché funzionino bene su vari tipi di computer. Vedi sotto per una descrizione dei preset disponibili. Se preferisci, puoi iniziare senza configurazione selezionando `Nessun preset`.

Se non sai se il tuo computer Linux usa X11 o Wayland, esegui questo comando in un terminale:

```shell
echo $XDG_SESSION_TYPE
```

Dovrebbe restituire `x11` o `wayland`. Devi eseguire questo comando all'interno dell'ambiente desktop.

## Telecomando

Una volta connesso, puoi usare l'interfaccia del telecomando per inviare comandi. Cambia scheda per accedere ai vari metodi di input.

Ogni pressione di un pulsante esegue un comando sull'host. È un notevole sovraccarico per qualcosa di semplice come la pressione di un tasto, quindi potresti notare una latenza piuttosto elevata rispetto a una tastiera normale. Spero di migliorare questo aspetto nelle versioni future.

Usa il menu per entrare in modalità modifica. Attualmente non è possibile modificare il layout o le icone dei pulsanti. Spero di rendere possibile anche questo in una versione futura.

## Preset

Dovrai installare lo strumento richiesto per il tuo ambiente desktop.

Consiglio `ydotool` perché nei miei test ha offerto le prestazioni migliori e funziona sia su X11 sia su Wayland.

### ydotool

`ydotool` dovrebbe funzionare con qualsiasi window manager, ma serve un servizio in background in esecuzione. Se la tua distribuzione fornisce un servizio utente systemd, avvialo con:

```shell
systemctl start --user ydotool
```

Per avviare automaticamente il servizio all'accesso, esegui:

```shell
systemctl enable --user ydotool
```

Assicurati di installare una versione sufficientemente recente di `ydotool`. Le versioni di Ubuntu precedenti alla 26.04 forniscono versioni troppo vecchie. Consulta il thread di discussione per una soluzione alternativa.

Parla di `ydotool` in questo thread di discussione: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` è pensato per i computer che eseguono X11. X11 è ciò che storicamente è stato usato dalla maggior parte dei computer Linux, anche se Wayland sta diventando sempre più popolare.

Una particolarità di X11 è che potresti dover consentire l'accesso al server X. È questo il problema se ricevi errori del tipo `Authorization required`. Hai diverse opzioni per risolvere il problema; ecco due soluzioni che hanno funzionato per me:

Se `xauth list` non mostra alcuna voce, prova a generare un file `.Xauthority`:

```shell
xauth generate :0 . trusted
```

Se non funziona, prova a concedere l'accesso usando `xhost`:

```shell
xhost +local:$USER
```

Dovrai eseguire il comando `xhost` dopo ogni avvio. Puoi automatizzarlo creando uno script bash e configurandolo per l'avvio automatico all'accesso.

Parla di `xdotool` in questo thread di discussione: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` è come `xdotool`, ma per Wayland.

Normalmente non supporta il controllo del mouse, ma io ho creato una versione modificata che aggiunge il supporto del mouse. Installala se ti serve il supporto del mouse: <https://github.com/stefansundin/wtype>

Se ricevi l'errore `Compositor does not support the virtual keyboard protocol`, ti suggerisco di provare un altro strumento, come `ydotool`.

Parla di `wtype` in questo thread di discussione: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` dovrebbe funzionare con qualsiasi window manager, in modo simile a `ydotool`. Nei miei test limitati è risultato molto più lento di `ydotool`.

Parla di `dotool` in questo thread di discussione: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Questo preset è sperimentale, perché non ho potuto verificarlo sul mio hardware. I feedback sono benvenuti.

Parla di `cec-client` in questo thread di discussione: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Esiste un preset per controllare VLC su macOS, usando comandi AppleScript. Non conosco uno strumento che supporti l'invio di eventi da tastiera o mouse.

Parla di macOS in questo thread di discussione: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Se il tuo dispositivo Android include un server SSH, potresti riuscire a connetterti e a inviare eventi di input. Questo è più probabile se il dispositivo usa una ROM personalizzata, come KonstaKANG sul Raspberry Pi.

Non sono ancora riuscito a capire come far funzionare il supporto del mouse.

Parla di Android in questo thread di discussione: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Controllo intelligente del volume

Quando modifichi il telecomando, puoi trovare nel menu le impostazioni di controllo del volume "intelligente". Questo permette di visualizzare il volume corrente del computer nell'app e di impostarlo rapidamente con un cursore. Puoi anche usare i pulsanti hardware del tuo dispositivo per inviare rapidamente i comandi volume su/giù.

La lettura del volume corrente e l'impostazione di un nuovo volume tramite il cursore sono attualmente codificate per usare `pactl`.

Il pacchetto che contiene `pactl` di solito si chiama `pulseaudio-utils` o `libpulse`.

## Chiavi SSH

Puoi importare o generare chiavi SSH nelle impostazioni dell'app. Connettersi con una chiave SSH è più sicuro che usare password.

Il modo più semplice per importare una chiave SSH esistente da un computer è scansionare un codice QR. Puoi usare il programma `qrencode` per generare l'immagine del codice QR. Esegui un comando come il seguente per generare il codice QR:

```shell
# Vai alla cartella delle tue chiavi SSH:
cd ~/.ssh

# Mostra il codice QR nel terminale:
qrencode -r id_ed25519 -t ansiutf8

# In alternativa, crea un file immagine:
qrencode -r id_ed25519 -o qr.png

# Le chiavi RSA a 4096 bit sono troppo grandi per un codice QR. Puoi usare gzip per farcene stare una per un soffio:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Puoi inviare chiavi SSH pubbliche a un server usando la funzione `Invia chiave pubblica` nel menu. Questo aggiungerà la chiave SSH selezionata al file `~/.ssh/authorized_keys`. In questo modo puoi passare facilmente dall'accesso con password all'accesso con chiave SSH.

Puoi importare e usare chiavi SSH cifrate, ma al momento non puoi generarle nell'app.

## Sicurezza

Non è possibile esportare o estrarre dall'app la parte privata delle chiavi SSH o le password memorizzate. Questi dati sono cifrati con AES a 256 bit e la chiave di cifratura è archiviata nell'Android Keystore. I dati cifrati sono esclusi dai backup Android.

In questa app non è presente alcun software di crash reporting. Non c'è telemetria. Non ci sono annunci pubblicitari. Non vengono effettuate richieste di rete, a eccezione della connessione SSH.

La sicurezza di questa app non è stata sottoposta ad audit. Se hai esperienza in materia di sicurezza Android o SSH, dai un'occhiata al codice sorgente e segnala i tuoi risultati in questa issue GitHub:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Richieste di funzionalità

Sentiti libero di inviare richieste di funzionalità e segnalazioni di bug nel repository GitHub. Usa l'inglese. Mantieni i commenti civili. I commenti irrispettosi verranno rimossi e gli utenti potranno essere bloccati dal repository.

Controlla le issue esistenti e i thread di discussione per vedere se la tua domanda è già stata posta o ha già ricevuto risposta.

Ti prego di essere rispettoso. Ho creato questa app nel mio tempo libero e la distribuisco gratuitamente. Sto sviluppando questa app prima di tutto per il mio uso personale.

Per favore non inviarmi domande via email. Cerca di mantenere le conversazioni su GitHub, perché così possono aiutare anche altre persone. Puoi fare domande nella sezione discussioni su GitHub.

Sei sempre il benvenuto se vuoi fare un fork dell'app per implementare le tue funzionalità. È un ottimo modo per imparare. Valuta di contribuire con funzionalità utili.

Il codice sorgente è distribuito con licenza GNU GPLv3. Se distribuisci versioni modificate di questa app, devi rendere disponibile anche il codice sorgente.

<https://github.com/stefansundin/SSHRemote>

## Donazioni

Se vuoi mostrare la tua gratitudine e il tuo apprezzamento, le donazioni sono ben accette.

<https://stefansundin.github.io/donate/>

Se hai effettuato una donazione, farò del mio meglio per rispondere a qualsiasi domanda tu possa avere. Per favore, scrivi eventuali richieste in inglese.

Grazie per il tuo supporto!
