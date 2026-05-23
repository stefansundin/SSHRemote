## Perihal SSH Remote

Terjemahan ini dibuat oleh AI dan mungkin mengandungi kesilapan terjemahan.

SSH Remote ialah aplikasi percuma dan sumber terbuka yang membolehkan anda mengawal komputer dari jauh menggunakan SSH.

Anda boleh menyesuaikan sepenuhnya arahan yang dilaksanakan, dan terdapat pratetap untuk persediaan biasa.

Saya menggunakan aplikasi ini untuk mengawal persediaan HTPC saya, yang menjalankan Raspberry Pi OS. Mengawal HTPC ialah senario asas yang dioptimumkan oleh aplikasi ini.

Aplikasi ini bukan emulator terminal, tetapi ia membolehkan anda menjalankan `apt-get install` ketika kecemasan.

## Bermula

Jika anda mahu menggunakan kunci SSH untuk menyambung, mula-mula buka tetapan aplikasi dan import atau jana kunci.

Tambah hos baharu dengan mengetik butang `+` di bahagian bawah kanan. Masukkan butiran sambungan dan simpan.

Kali pertama anda menyambung, anda akan diminta memilih pratetap. Pilihan ini mengkonfigurasikan butang kawalan jauh supaya berfungsi dengan baik pada pelbagai jenis komputer. Lihat di bawah untuk penerangan pratetap yang tersedia. Jika anda mahu, anda boleh bermula tanpa konfigurasi dengan memilih `Tiada pratetap`.

Jika anda tidak tahu sama ada komputer Linux anda menjalankan X11 atau Wayland, jalankan ini dalam terminal:

```shell
echo $XDG_SESSION_TYPE
```

Ini sepatutnya mengeluarkan `x11` atau `wayland`. Anda mesti menjalankannya di dalam persekitaran desktop.

## Kawalan Jauh

Setelah disambungkan, anda boleh menggunakan antara muka kawalan jauh untuk menghantar arahan. Tukar tab untuk mengakses pelbagai kaedah input.

Setiap tekanan butang akan melaksanakan arahan pada hos. Ini melibatkan banyak overhed untuk sesuatu yang semudah tekanan kekunci, dan anda mungkin mengalami kependaman yang agak tinggi berbanding papan kekunci biasa. Saya berharap dapat menambah baik perkara ini dalam versi akan datang.

Gunakan menu untuk memasuki mod edit. Buat masa ini, susun atur atau ikon butang tidak boleh diedit. Saya berharap dapat menjadikannya mungkin dalam versi akan datang.

## Pratetap

Anda perlu memasang alat yang diperlukan untuk persekitaran desktop anda.

Saya mengesyorkan `ydotool` kerana dalam ujian saya ia memberikan prestasi terbaik, dan ia berfungsi pada kedua-dua X11 dan Wayland.

### ydotool

`ydotool` sepatutnya berfungsi dengan mana-mana pengurus tetingkap, tetapi anda memerlukan perkhidmatan latar belakang yang sedang berjalan. Jika distribusi anda menyediakan perkhidmatan pengguna systemd maka mulakannya dengan menjalankan:

```shell
systemctl start --user ydotool
```

Mulakan perkhidmatan secara automatik semasa log masuk dengan menjalankan:

```shell
systemctl enable --user ydotool
```

Sila pastikan anda memasang versi `ydotool` yang cukup baharu. Versi Ubuntu sebelum 26.04 menyediakan versi yang terlalu lama. Lihat thread perbincangan untuk penyelesaiannya.

Sila bincangkan `ydotool` dalam thread perbincangan ini: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` adalah untuk komputer yang menjalankan X11. X11 ialah apa yang kebanyakan komputer Linux gunakan secara sejarah, walaupun Wayland semakin popular.

Satu keanehan pada X11 ialah anda mungkin perlu membenarkan akses kepada pelayan X. Inilah masalahnya jika anda mendapat ralat "Authorization required". Anda mempunyai beberapa pilihan untuk membetulkan isu ini, berikut ialah dua pilihan yang berkesan bagi saya:

Jika `xauth list` tidak menunjukkan sebarang entri maka cuba jana fail `.Xauthority`:

```shell
xauth generate :0 . trusted
```

Jika itu tidak berjaya maka cuba berikan akses menggunakan `xhost`:

```shell
xhost +local:$USER
```

Anda perlu menjalankan arahan `xhost` selepas setiap but semula. Anda boleh mengautomasikannya dengan mencipta skrip bash dan mengkonfigurasikannya untuk dimulakan secara automatik semasa log masuk.

Sila bincangkan `xdotool` dalam thread perbincangan ini: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` adalah seperti `xdotool`, tetapi untuk Wayland.

Biasanya, ia tidak menyokong kawalan tetikus, tetapi saya telah mencipta versi diubah suai yang menambah sokongan tetikus. Sila pasangkannya jika anda memerlukan sokongan tetikus: <https://github.com/stefansundin/wtype>

Jika anda mendapat ralat `Compositor does not support the virtual keyboard protocol` maka saya cadangkan anda mencuba alat lain, seperti `ydotool`.

Sila bincangkan `wtype` dalam thread perbincangan ini: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` sepatutnya berfungsi dengan mana-mana pengurus tetingkap, sama seperti `ydotool`. Dalam ujian terhad saya, ia jauh lebih perlahan daripada `ydotool`.

Sila bincangkan `dotool` dalam thread perbincangan ini: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Pratetap ini bersifat eksperimen kerana saya belum dapat mengesahkannya pada perkakasan saya sendiri. Maklum balas amat dialu-alukan.

Sila bincangkan `cec-client` dalam thread perbincangan ini: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Terdapat pratetap untuk mengawal VLC pada macOS, menggunakan arahan AppleScript. Saya tidak mengetahui alat yang menyokong penghantaran peristiwa papan kekunci atau tetikus.

Sila bincangkan macOS dalam thread perbincangan ini: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Jika peranti Android anda disertakan dengan pelayan SSH, maka anda mungkin boleh menyambung kepadanya dan menghantar peristiwa input. Ini lebih berkemungkinan jika peranti anda menjalankan ROM tersuai, seperti KonstaKANG pada Raspberry Pi.

Saya belum dapat mengetahui cara untuk menjadikan sokongan tetikus berfungsi.

Sila bincangkan Android dalam thread perbincangan ini: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Kawalan kelantangan pintar

Semasa mengedit kawalan jauh, anda boleh menemui tetapan kawalan kelantangan "pintar" dalam menu. Ini boleh memaparkan kelantangan semasa komputer dalam aplikasi dan membolehkan anda menetapkan kelantangan dengan cepat menggunakan peluncur. Anda juga boleh menggunakan butang perkakasan peranti anda untuk menghantar arahan tambah/kurang kelantangan dengan cepat.

Membaca kelantangan semasa dan menetapkan kelantangan baharu menggunakan peluncur kini dikod keras untuk menggunakan `pactl`.

Pakej yang mengandungi `pactl` biasanya dipanggil `pulseaudio-utils` atau `libpulse`.

## Kunci SSH

Anda boleh mengimport atau menjana kunci SSH dalam tetapan aplikasi. Menyambung dengan kunci SSH adalah lebih selamat daripada menggunakan kata laluan.

Cara paling mudah untuk mengimport kunci SSH sedia ada daripada komputer ialah dengan mengimbas kod QR. Anda boleh menggunakan program `qrencode` untuk menjana imej kod QR. Jalankan arahan seperti berikut untuk menjana kod QR:

```shell
# Pergi ke kunci SSH anda:
cd ~/.ssh

# Paparkan kod QR dalam terminal:
qrencode -r id_ed25519 -t ansiutf8

# Sebagai alternatif, cipta fail imej:
qrencode -r id_ed25519 -o qr.png

# Kunci RSA 4096-bit terlalu besar untuk kod QR. Anda boleh menggunakan gzip supaya ia muat dengan cukup-cukup:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Anda boleh menghantar kunci SSH awam ke pelayan menggunakan ciri `Hantar kunci awam` dalam menu. Ini akan menambah kunci SSH yang dipilih ke fail `~/.ssh/authorized_keys`. Ini membolehkan anda berhijrah dengan mudah daripada log masuk menggunakan kata laluan kepada log masuk menggunakan kunci SSH.

Anda boleh mengimport dan menggunakan kunci SSH yang disulitkan, tetapi anda tidak boleh menjana kunci ini dalam aplikasi buat masa ini.

## Keselamatan

Tidak mungkin untuk mengeksport atau mengekstrak bahagian peribadi kunci SSH, atau kata laluan yang disimpan, daripada aplikasi. Data ini disulitkan menggunakan AES 256-bit, dan kunci penyulitan disimpan dalam Android Keystore. Data yang disulitkan dikecualikan daripada sandaran Android.

Tiada perisian pelaporan ranap dalam aplikasi ini. Tiada telemetri. Tiada iklan. Tiada permintaan rangkaian kecuali sambungan SSH.

Keselamatan aplikasi ini belum diaudit. Jika anda berpengalaman dengan keselamatan Android atau keselamatan SSH, sila lihat kod sumber dan laporkan penemuan anda dalam isu GitHub ini:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Permintaan ciri

Jangan segan untuk menghantar permintaan ciri dan laporan pepijat dalam repositori GitHub. Sila gunakan bahasa Inggeris. Sila kekalkan komen anda sopan. Komen yang tidak sopan akan dipadam dan pengguna mungkin disekat daripada repositori.

Sila lihat isu dan thread perbincangan sedia ada untuk melihat sama ada soalan anda sudah pernah ditanya atau dijawab.

Sila hormati. Saya membina aplikasi ini pada masa lapang saya dan saya memberikannya secara percuma. Saya membina aplikasi ini terutamanya untuk kegunaan saya sendiri.

Sila jangan e-mel saya dengan soalan. Cuba kekalkan perbualan di GitHub, kerana itu juga membantu orang lain! Anda boleh bertanya soalan dalam seksyen perbincangan di GitHub.

Anda sentiasa dialu-alukan untuk memfork aplikasi ini bagi melaksanakan ciri anda sendiri. Itu ialah cara yang bagus untuk belajar. Sila pertimbangkan untuk menyumbang ciri yang berguna.

Kod sumber dilesenkan di bawah GNU GPLv3. Jika anda mengedarkan versi yang diubah suai bagi aplikasi ini maka anda juga mesti menyediakan kod sumbernya.

<https://github.com/stefansundin/SSHRemote>

## Derma

Jika anda ingin menunjukkan rasa syukur dan penghargaan anda, derma diterima.

<https://stefansundin.github.io/donate/>

Jika anda telah menderma, saya akan cuba sedaya upaya untuk menjawab sebarang soalan anda. Sila tulis sebarang pertanyaan dalam bahasa Inggeris.

Terima kasih atas sokongan anda!
