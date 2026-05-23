## O SSH Remote

To polskie tłumaczenie zostało przygotowane przez AI przy użyciu GitHub Copilot (GPT-5.3-Codex) i może zawierać błędy tłumaczenia.

SSH Remote to darmowa aplikacja open source, która pozwala zdalnie sterować komputerami przez SSH.

Możesz w pełni dostosować wykonywane polecenia, a dla typowych konfiguracji dostępne są presety.

Używam tej aplikacji do sterowania moim zestawem HTPC, działającym na Raspberry Pi OS. Sterowanie HTPC to podstawowy scenariusz, pod który aplikacja jest zoptymalizowana.

Ta aplikacja nie jest emulatorem terminala, ale w awaryjnej sytuacji pozwoli Ci uruchomić `apt-get install`.

## Pierwsze kroki

Jeśli chcesz używać klucza SSH do łączenia, najpierw otwórz ustawienia aplikacji i zaimportuj lub wygeneruj klucz.

Dodaj nowy host, stukając przycisk `+` w prawym dolnym rogu. Wprowadź dane połączenia i zapisz.

Przy pierwszym połączeniu zostaniesz poproszony o wybranie presetu. Ten wybór konfiguruje przyciski pilota tak, aby dobrze działały na różnych typach komputerów. Poniżej znajdziesz opis dostępnych presetów. Jeśli wolisz, możesz zacząć bez konfiguracji, wybierając `Brak presetu`.

Jeśli nie wiesz, czy Twój komputer z Linuksem działa pod X11 czy Waylandem, uruchom w terminalu:

```shell
echo $XDG_SESSION_TYPE
```

Powinno to zwrócić `x11` albo `wayland`. Musisz uruchomić to wewnątrz środowiska graficznego.

## Pilot

Po połączeniu możesz używać interfejsu pilota do wysyłania poleceń. Przełączaj karty, aby uzyskać dostęp do różnych metod wprowadzania.

Każde naciśnięcie przycisku wykonuje polecenie na hoście. To spory narzut jak na coś tak prostego jak naciśnięcie klawisza, więc możesz zauważyć dość duże opóźnienie w porównaniu ze zwykłą klawiaturą. Mam nadzieję poprawić to w przyszłych wersjach.

Użyj menu, aby przejść do trybu edycji. Obecnie nie da się edytować układu ani ikon przycisków. Mam nadzieję, że w przyszłości będzie to możliwe.

## Presety

Musisz zainstalować narzędzie wymagane przez Twoje środowisko graficzne.

Polecam `ydotool`, ponieważ w moich testach działa najlepiej i obsługuje zarówno X11, jak i Wayland.

### ydotool

`ydotool` powinien działać z każdym menedżerem okien, ale wymaga działającej usługi w tle. Jeśli Twoja dystrybucja udostępnia usługę systemd dla użytkownika, uruchom ją poleceniem:

```shell
systemctl start --user ydotool
```

Aby uruchamiać tę usługę automatycznie przy logowaniu, wykonaj:

```shell
systemctl enable --user ydotool
```

Upewnij się, że instalujesz wystarczająco nową wersję `ydotool`. Wersje Ubuntu sprzed 26.04 dostarczają wersje, które są zbyt stare. Zobacz wątek dyskusji, aby poznać obejście problemu.

Dyskutuj o `ydotool` w tym wątku: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` jest przeznaczony dla komputerów działających pod X11. X11 był historycznie używany przez większość komputerów z Linuksem, choć Wayland staje się coraz popularniejszy.

Jedną z osobliwości X11 jest to, że może być konieczne zezwolenie na dostęp do serwera X. To właśnie jest problemem, jeśli pojawiają się błędy „Authorization required”. Masz kilka możliwości rozwiązania tego problemu; oto dwie opcje, które u mnie zadziałały:

Jeśli `xauth list` nie pokazuje żadnych wpisów, spróbuj wygenerować plik `.Xauthority`:

```shell
xauth generate :0 . trusted
```

Jeśli to nie pomogło, spróbuj przyznać dostęp za pomocą `xhost`:

```shell
xhost +local:$USER
```

Polecenie `xhost` trzeba będzie uruchamiać po każdym uruchomieniu komputera. Możesz to zautomatyzować, tworząc skrypt bash i konfigurując jego automatyczne uruchamianie przy logowaniu.

Dyskutuj o `xdotool` w tym wątku: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` jest podobny do `xdotool`, ale dla Waylanda.

Normalnie nie obsługuje sterowania myszą, ale stworzyłem zmodyfikowaną wersję, która dodaje obsługę myszy. Zainstaluj ją, jeśli potrzebujesz obsługi myszy: <https://github.com/stefansundin/wtype>

Jeśli pojawi się błąd `Compositor does not support the virtual keyboard protocol`, sugeruję wypróbowanie innego narzędzia, na przykład `ydotool`.

Dyskutuj o `wtype` w tym wątku: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` powinien działać z każdym menedżerem okien, podobnie jak `ydotool`. W moich ograniczonych testach był jednak dużo wolniejszy od `ydotool`.

Dyskutuj o `dotool` w tym wątku: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Ten preset jest eksperymentalny, ponieważ nie udało mi się go zweryfikować na własnym sprzęcie. Opinie są mile widziane.

Dyskutuj o `cec-client` w tym wątku: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Jest dostępny preset do sterowania VLC na macOS przy użyciu poleceń AppleScript. Nie znam narzędzia, które umożliwiałoby wysyłanie zdarzeń klawiatury lub myszy.

Dyskutuj o macOS w tym wątku: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Jeśli Twoje urządzenie z Androidem ma serwer SSH, możesz być w stanie połączyć się z nim i wysyłać zdarzenia wejściowe. Jest to bardziej prawdopodobne, jeśli urządzenie działa na niestandardowym ROM-ie, takim jak KonstaKANG na Raspberry Pi.

Nie udało mi się ustalić, jak uruchomić obsługę myszy.

Dyskutuj o Androidzie w tym wątku: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Inteligentne sterowanie głośnością

Podczas edycji pilota w menu znajdziesz „inteligentne” ustawienia sterowania głośnością. Mogą one wyświetlać w aplikacji bieżący poziom głośności komputera i pozwolić szybko ustawić głośność suwakiem. Możesz też używać sprzętowych przycisków urządzenia, aby szybko wysyłać polecenia zwiększania lub zmniejszania głośności.

Odczyt bieżącej głośności i ustawianie nowej głośności suwakiem są obecnie na sztywno powiązane z użyciem `pactl`.

Pakiet zawierający `pactl` zwykle nazywa się `pulseaudio-utils` albo `libpulse`.

## Klucze SSH

Możesz importować lub generować klucze SSH w ustawieniach aplikacji. Łączenie się za pomocą klucza SSH jest bezpieczniejsze niż używanie haseł.

Najłatwiejszym sposobem zaimportowania istniejącego klucza SSH z komputera jest zeskanowanie kodu QR. Możesz użyć programu `qrencode`, aby wygenerować obraz kodu QR. Uruchom polecenie podobne do poniższego, aby wygenerować kod QR:

```shell
# Przejdź do katalogu z kluczami SSH:
cd ~/.ssh

# Wyświetl kod QR w terminalu:
qrencode -r id_ed25519 -t ansiutf8

# Alternatywnie utwórz plik obrazu:
qrencode -r id_ed25519 -o qr.png

# Klucze RSA 4096-bit są zbyt duże dla kodu QR. Możesz użyć gzip, aby ledwo się zmieściły:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Możesz przesyłać publiczne klucze SSH na serwer za pomocą funkcji `Prześlij klucz publiczny` w menu. Spowoduje to dopisanie wybranego klucza SSH do pliku `~/.ssh/authorized_keys`. Dzięki temu łatwo przejdziesz z logowania hasłem na logowanie kluczem SSH.

Możesz importować i używać zaszyfrowanych kluczy SSH, ale obecnie nie możesz ich generować w aplikacji.

## Bezpieczeństwo

Nie da się wyeksportować ani wyodrębnić z aplikacji prywatnej części kluczy SSH ani zapisanych haseł. Dane te są szyfrowane przy użyciu 256-bitowego AES, a klucz szyfrowania jest przechowywany w Android Keystore. Zaszyfrowane dane są wykluczone z kopii zapasowych Androida.

W tej aplikacji nie ma oprogramowania do raportowania awarii. Nie ma telemetrii. Nie ma reklam. Nie ma żadnych żądań sieciowych poza połączeniem SSH.

Bezpieczeństwo tej aplikacji nie zostało poddane audytowi. Jeśli masz doświadczenie w zakresie bezpieczeństwa Androida lub SSH, zajrzyj do kodu źródłowego i zgłoś swoje ustalenia w tym zgłoszeniu na GitHubie:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Prośby o funkcje

Możesz zgłaszać prośby o nowe funkcje i raporty błędów w repozytorium GitHub. Proszę używać języka angielskiego. Zachowuj kulturę wypowiedzi. Niegrzeczne komentarze będą usuwane, a użytkownicy mogą zostać zablokowani w repozytorium.

Sprawdź istniejące zgłoszenia i wątki dyskusji, aby zobaczyć, czy Twoje pytanie nie zostało już zadane lub czy nie ma już na nie odpowiedzi.

Proszę o szacunek. Tworzę tę aplikację w wolnym czasie i udostępniam ją za darmo. Buduję tę aplikację przede wszystkim na własny użytek.

Proszę nie pisać do mnie maili z pytaniami. Staraj się prowadzić rozmowy na GitHubie, bo to pomaga również innym! Pytania możesz zadawać w sekcji dyskusji na GitHubie.

Zawsze możesz sforkować aplikację, aby wdrożyć własne funkcje. To świetny sposób na naukę. Rozważ wniesienie przydatnych ulepszeń.

Kod źródłowy jest licencjonowany na zasadach GNU GPLv3. Jeśli rozpowszechniasz zmodyfikowane wersje tej aplikacji, musisz także udostępnić kod źródłowy.

<https://github.com/stefansundin/SSHRemote>

## Darowizny

Jeśli chcesz okazać wdzięczność i uznanie, możesz przekazać darowiznę.

<https://stefansundin.github.io/donate/>

Jeśli przekażesz darowiznę, postaram się jak najlepiej odpowiedzieć na każde Twoje pytanie. Jeśli masz jakieś pytania, napisz proszę po angielsku.

Dziękuję za wsparcie!
