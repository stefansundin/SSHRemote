## Tentang SSH Remote

Terjemahan bahasa Indonesia ini dibuat dengan bantuan AI dan mungkin masih mengandung kesalahan terjemahan.

SSH Remote adalah aplikasi gratis dan sumber terbuka yang memungkinkan Anda mengendalikan komputer dari jarak jauh menggunakan SSH.

Anda dapat sepenuhnya menyesuaikan perintah yang dijalankan, dan tersedia preset untuk pengaturan yang umum.

Saya menggunakan aplikasi ini untuk mengendalikan pengaturan HTPC saya, yang menjalankan Raspberry Pi OS. Mengendalikan HTPC adalah skenario dasar yang menjadi fokus optimasi aplikasi ini.

Aplikasi ini bukan emulator terminal, tetapi tetap memungkinkan Anda menjalankan `apt-get install` dalam keadaan darurat.

## Memulai

Jika Anda ingin menggunakan kunci SSH untuk terhubung, pertama buka pengaturan aplikasi lalu impor atau buat sebuah kunci.

Tambahkan host baru dengan mengetuk tombol `+` di kanan bawah. Masukkan detail koneksi lalu simpan.

Saat pertama kali terhubung, Anda akan diminta memilih preset. Pilihan ini mengonfigurasi tombol kendali jarak jauh agar bekerja dengan baik pada berbagai jenis komputer. Lihat di bawah untuk deskripsi preset yang tersedia. Jika mau, Anda juga bisa memulai tanpa konfigurasi dengan memilih `No preset`.

Jika Anda tidak tahu apakah komputer Linux Anda menjalankan X11 atau Wayland, jalankan ini di terminal:

```shell
echo $XDG_SESSION_TYPE
```

Perintah ini akan menampilkan `x11` atau `wayland`. Anda harus menjalankannya dari dalam lingkungan desktop.

## Kendali Jarak Jauh

Setelah terhubung, Anda dapat menggunakan antarmuka kendali jarak jauh untuk mengirim perintah. Berpindahlah antar tab untuk mengakses berbagai metode input.

Setiap penekanan tombol akan menjalankan sebuah perintah pada host. Ini menimbulkan overhead yang cukup besar untuk sesuatu yang sesederhana penekanan tombol, dan Anda mungkin akan merasakan latensi yang cukup tinggi dibandingkan keyboard biasa. Saya berharap dapat memperbaikinya di versi mendatang.

Gunakan menu untuk masuk ke mode edit. Saat ini belum memungkinkan untuk mengedit tata letak atau ikon tombol. Saya berharap dapat menambahkan kemampuan ini di versi mendatang.

## Preset

Anda perlu memasang alat yang diperlukan untuk lingkungan desktop Anda.

Saya merekomendasikan `ydotool` karena dalam pengujian saya performanya paling baik, dan alat ini bekerja pada X11 maupun Wayland.

### ydotool

`ydotool` seharusnya bekerja dengan window manager apa pun, tetapi Anda memerlukan layanan latar belakang yang berjalan. Jika distribusi Anda menyediakan layanan pengguna systemd, jalankan dengan perintah berikut:

```shell
systemctl start --user ydotool
```

Agar layanan dimulai otomatis saat login, jalankan:

```shell
systemctl enable --user ydotool
```

Pastikan Anda memasang versi `ydotool` yang cukup baru. Versi Ubuntu sebelum 26.04 menyediakan versi yang terlalu lama. Lihat utas diskusi untuk solusi alternatif.

Silakan diskusikan `ydotool` di utas diskusi ini: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` ditujukan untuk komputer yang menjalankan X11. X11 adalah sistem yang secara historis digunakan oleh sebagian besar komputer Linux, meskipun Wayland kini semakin populer.

Salah satu keanehan pada X11 adalah Anda mungkin perlu mengizinkan akses ke server X. Inilah masalahnya jika Anda mendapatkan kesalahan "Authorization required". Anda memiliki beberapa pilihan untuk memperbaikinya; berikut dua opsi yang berhasil bagi saya:

Jika `xauth list` tidak menampilkan entri apa pun, cobalah membuat file `.Xauthority`:

```shell
xauth generate :0 . trusted
```

Jika itu tidak berhasil, cobalah memberi akses menggunakan `xhost`:

```shell
xhost +local:$USER
```

Anda perlu menjalankan perintah `xhost` setiap kali selesai boot. Anda dapat mengotomatiskannya dengan membuat skrip bash dan mengatur agar dijalankan otomatis saat login.

Silakan diskusikan `xdotool` di utas diskusi ini: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` mirip seperti `xdotool`, tetapi untuk Wayland.

Secara normal, alat ini tidak mendukung kontrol mouse, tetapi saya telah membuat versi modifikasi yang menambahkan dukungan mouse. Silakan pasang jika Anda membutuhkan dukungan mouse: <https://github.com/stefansundin/wtype>

Jika Anda mendapatkan kesalahan `Compositor does not support the virtual keyboard protocol`, saya menyarankan Anda mencoba alat lain, seperti `ydotool`.

Silakan diskusikan `wtype` di utas diskusi ini: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` seharusnya bekerja dengan window manager apa pun, mirip dengan `ydotool`. Dalam pengujian terbatas saya, alat ini jauh lebih lambat daripada `ydotool`.

Silakan diskusikan `dotool` di utas diskusi ini: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Preset ini bersifat eksperimental karena saya belum bisa memverifikasinya pada perangkat keras saya sendiri. Masukan sangat diterima.

Silakan diskusikan `cec-client` di utas diskusi ini: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Tersedia preset untuk mengendalikan VLC di macOS, menggunakan perintah AppleScript. Saya tidak mengetahui alat yang mendukung pengiriman kejadian keyboard atau mouse.

Silakan diskusikan macOS di utas diskusi ini: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Jika perangkat Android Anda dilengkapi server SSH, Anda mungkin dapat terhubung ke perangkat tersebut dan mengirim kejadian input. Hal ini lebih mungkin terjadi jika perangkat Anda menjalankan ROM kustom, seperti KonstaKANG pada Raspberry Pi.

Saya belum menemukan cara agar dukungan mouse berfungsi.

Silakan diskusikan Android di utas diskusi ini: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Kontrol volume pintar

Saat mengedit kendali jarak jauh, Anda dapat menemukan pengaturan kontrol volume "pintar" di menu. Fitur ini dapat menampilkan volume komputer saat ini di aplikasi dan memungkinkan Anda dengan cepat mengatur volume menggunakan slider. Anda juga dapat menggunakan tombol perangkat keras pada perangkat Anda untuk segera mengirim perintah volume naik/turun.

Membaca volume saat ini dan menetapkan volume baru menggunakan slider saat ini dikodekan secara tetap untuk menggunakan `pactl`.

Paket yang berisi `pactl` biasanya bernama `pulseaudio-utils` atau `libpulse`.

## Kunci SSH

Anda dapat mengimpor atau membuat kunci SSH di pengaturan aplikasi. Terhubung dengan kunci SSH lebih aman daripada menggunakan kata sandi.

Cara termudah untuk mengimpor kunci SSH yang sudah ada dari komputer adalah dengan memindai kode QR. Anda dapat menggunakan program `qrencode` untuk membuat gambar kode QR. Jalankan perintah seperti berikut untuk membuat gambar kode QR:

```shell
# Pindah ke direktori kunci SSH Anda:
cd ~/.ssh

# Tampilkan kode QR di terminal:
qrencode -r id_ed25519 -t ansiutf8

# Atau, buat file gambar:
qrencode -r id_ed25519 -o qr.png

# Kunci RSA 4096-bit terlalu besar untuk kode QR. Anda dapat menggunakan gzip agar pas:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Anda dapat mengirim kunci SSH publik ke server menggunakan fitur `Kirim kunci publik` di menu. Fitur ini akan menambahkan kunci SSH yang dipilih ke file `~/.ssh/authorized_keys`. Ini memudahkan Anda bermigrasi dari login dengan kata sandi ke login dengan kunci SSH.

Anda dapat mengimpor dan menggunakan kunci SSH terenkripsi, tetapi saat ini Anda belum dapat membuatnya di aplikasi.

## Keamanan

Tidak mungkin mengekspor atau mengekstrak bagian privat dari kunci SSH, atau kata sandi yang disimpan, dari aplikasi. Data ini dienkripsi menggunakan AES 256-bit, dan kunci enkripsinya disimpan di Android Keystore. Data terenkripsi dikecualikan dari pencadangan Android.

Tidak ada perangkat lunak pelaporan crash di aplikasi ini. Tidak ada telemetri. Tidak ada iklan. Tidak ada permintaan jaringan selain koneksi SSH.

Keamanan aplikasi ini belum diaudit. Jika Anda berpengalaman dalam keamanan Android atau SSH, silakan lihat kode sumber dan laporkan temuan Anda di issue GitHub ini:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Permintaan fitur

Jangan ragu untuk mengirim permintaan fitur dan laporan bug di repositori GitHub. Harap gunakan bahasa Inggris. Harap jaga komentar Anda tetap sopan. Komentar yang tidak menghormati akan dihapus dan pengguna dapat diblokir dari repositori.

Silakan periksa issue dan utas diskusi yang sudah ada untuk melihat apakah pertanyaan Anda sudah pernah diajukan atau dijawab.

Harap saling menghormati. Saya membangun aplikasi ini di waktu luang saya dan saya memberikannya secara gratis. Saya membangun aplikasi ini pertama dan terutama untuk penggunaan saya sendiri.

Mohon jangan mengirim pertanyaan melalui email. Sebisa mungkin, jaga percakapan tetap di GitHub, karena itu juga membantu orang lain! Anda dapat mengajukan pertanyaan di bagian diskusi GitHub.

Anda selalu dipersilakan untuk melakukan fork pada aplikasi ini guna menerapkan fitur Anda sendiri. Itu adalah cara yang bagus untuk belajar. Mohon pertimbangkan untuk berkontribusi dengan fitur yang berguna.

Kode sumber dilisensikan di bawah GNU GPLv3. Jika Anda mendistribusikan versi aplikasi ini yang telah dimodifikasi, maka Anda juga harus menyediakan kode sumbernya.

<https://github.com/stefansundin/SSHRemote>

## Donasi

Jika Anda ingin menunjukkan rasa terima kasih dan apresiasi, donasi diterima.

<https://stefansundin.github.io/donate/>

Jika Anda telah berdonasi maka saya akan berusaha sebaik mungkin untuk menjawab pertanyaan apa pun yang mungkin Anda miliki. Jika Anda memiliki pertanyaan, mohon tulis dalam bahasa Inggris.

Terima kasih atas dukungan Anda!
