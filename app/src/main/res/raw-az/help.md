## SSH Remote haqqında

Bu tərcümə GitHub Copilot (GPT-5-Codex) vasitəsilə hazırlanıb, buna görə tərcümə xətaları ola bilər.

SSH Remote SSH vasitəsilə kompüterləri uzaqdan idarə etməyə imkan verən pulsuz və açıq mənbəli tətbiqdir.

İcra edilən əmrləri tam fərdiləşdirə bilərsiniz və yayğın qurulumlar üçün hazır ayarlar mövcuddur.

Mən bu tətbiqdən Raspberry Pi OS ilə işləyən HTPC quruluşumu idarə etmək üçün istifadə edirəm. HTPC idarəsi tətbiqin optimallaşdırıldığı əsas ssenaridir.

Bu tətbiq terminal emulyatoru deyil, amma fövqəladə hallarda `apt-get install` işlətməyə imkan verir.

## Başlanğıc

SSH açarı ilə qoşulmaq istəyirsinizsə, əvvəlcə tətbiq ayarlarını açın və açarı idxal edin və ya yaradın.

Aşağı sağ küncdəki `+` düyməsinə toxunaraq yeni host əlavə edin. Bağlantı məlumatlarını daxil edin və yadda saxlayın.

İlk dəfə qoşulanda sizdən hazır ayar seçmək istənəcək. Bu seçim pult düymələrini müxtəlif tipli kompüterlərdə yaxşı işləməsi üçün konfiqurasiya edir. Mövcud hazır ayarların təsviri üçün aşağıya baxın. İstəsəniz, `Hazır ayar yoxdur` seçərək heç bir konfiqurasiya olmadan başlaya bilərsiniz.

Linux kompüterinizin X11, yoxsa Wayland ilə işlədiyini bilmirsinizsə, terminalda bunu işlədin:

```shell
echo $XDG_SESSION_TYPE
```

Nəticə `x11` və ya `wayland` olmalıdır. Bu əmri masaüstü mühiti daxilində işlətməlisiniz.

## Pult

Qoşulduqdan sonra əmrlər göndərmək üçün pult interfeysindən istifadə edə bilərsiniz. Müxtəlif giriş üsullarına keçmək üçün bölmələri dəyişin.

Hər düymə basışı hostda bir əmri icra edəcək. Bu, düymə basışı kimi sadə bir əməliyyat üçün xeyli əlavə yük gətirir və adi klaviatura ilə müqayisədə daha yüksək gecikmə hiss edə bilərsiniz. Ümid edirəm bunu gələcək versiyalarda yaxşılaşdırım.

Redaktə rejiminə keçmək üçün menyudan istifadə edin. Hazırda düzülüşü və ya düymə ikonlarını redaktə etmək mümkün deyil. Gələcək versiyada bunu da mümkün etməyi planlayıram.

## Hazır ayarlar

Masaüstü mühitiniz üçün tələb olunan aləti quraşdırmalısınız.

Mən `ydotool` tövsiyə edirəm, çünki testlərimdə ən yaxşı performansı göstərib və həm X11, həm də Wayland ilə işləyir.

### ydotool

`ydotool` istənilən pəncərə menecerində işləməlidir, amma fonda çalışan xidmət lazımdır. Əgər distributiviniz systemd user service təqdim edirsə, bunu işlədin:

```shell
systemctl start --user ydotool
```

Xidmətin giriş zamanı avtomatik başlaması üçün bunu işlədin:

```shell
systemctl enable --user ydotool
```

`ydotool`-un kifayət qədər yeni versiyasını quraşdırdığınıza əmin olun. Ubuntu 26.04-dən əvvəlki versiyalar çox köhnə versiyalar təqdim edir. Həll yolu üçün müzakirə mövzusuna baxın.

`ydotool` barədə müzakirə üçün bu mövzuya yazın: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` X11 ilə işləyən kompüterlər üçündür. Tarixən Linux kompüterlərinin çoxu X11 istifadə edib, baxmayaraq ki, Wayland getdikcə populyarlaşır.

X11 ilə bağlı bir xüsusiyyət odur ki, X serverinə girişə icazə vermək lazım gələ bilər. "Authorization required" xətası alırsınızsa, problem budur. Bu problemi həll etmək üçün bir neçə yol var, məndə işləyən iki yolu aşağıda verilir:

Əgər `xauth list` heç bir qeyd göstərmirsə, `.Xauthority` faylı yaratmağa çalışın:

```shell
xauth generate :0 . trusted
```

Bu işləməsə, `xhost` ilə giriş icazəsi verməyə çalışın:

```shell
xhost +local:$USER
```

`xhost` əmrini hər açılışdan sonra işlətməlisiniz. Bunu avtomatlaşdırmaq üçün bash skripti yaradıb girişdə avtomatik başladılacaq şəkildə qura bilərsiniz.

`xdotool` barədə müzakirə üçün bu mövzuya yazın: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype`, Wayland üçün `xdotool` kimidir.

Normalda siçan idarəsini dəstəkləmir, amma mən siçan dəstəyini əlavə edən dəyişdirilmiş versiya yaratmışam. Siçan dəstəyi lazımdırsa, bunu quraşdırın: <https://github.com/stefansundin/wtype>

`Compositor does not support the virtual keyboard protocol` xətası alsanız, `ydotool` kimi başqa aləti sınamağınızı tövsiyə edirəm.

`wtype` barədə müzakirə üçün bu mövzuya yazın: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool`, `ydotool` kimi, istənilən pəncərə menecerində işləməlidir. Məhdud testlərimdə `ydotool`-dan xeyli yavaş idi.

`dotool` barədə müzakirə üçün bu mövzuya yazın: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Bu hazır ayar eksperimentaldır, çünki onu öz avadanlığımda yoxlaya bilməmişəm. Rəy bildirməyiniz xoş qarşılanır.

`cec-client` barədə müzakirə üçün bu mövzuya yazın: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

macOS üçün AppleScript əmrləri ilə VLC idarəsi edən hazır ayar var. Klaviatura və ya siçan hadisələri göndərməni dəstəkləyən alət bildiyim qədər yoxdur.

macOS barədə müzakirə üçün bu mövzuya yazın: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Android cihazınızda SSH server varsa, ona qoşulub giriş hadisələri göndərə bilərsiniz. Bu, cihaz custom ROM (məsələn Raspberry Pi üzərində KonstaKANG) ilə işləyirsə daha ehtimallıdır.

Siçan dəstəyini necə işə salmağı hələ tapa bilməmişəm.

Android barədə müzakirə üçün bu mövzuya yazın: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Ağıllı səs idarəsi

Pultu redaktə edərkən menyuda "ağıllı" səs idarəsi ayarlarını tapa bilərsiniz. Bu funksiya tətbiqdə kompüterin cari səsini göstərə və sürüşdürgəclə səsi tez dəyişməyə imkan verə bilər. Cihazınızın fiziki düymələri ilə də səs artır/azalt əmrlərini tez göndərə bilərsiniz.

Cari səsi oxuma və sürüşdürgəclə yeni səs dəyəri təyin etmə hazırda birbaşa `pactl` istifadəsinə bağlıdır.

`pactl` paketinin adı adətən `pulseaudio-utils` və ya `libpulse` olur.

## SSH açarları

Tətbiq ayarlarında SSH açarlarını idxal edə və ya yarada bilərsiniz. SSH açarı ilə qoşulmaq paroldan daha təhlükəsizdir.

Kompüterdən mövcud SSH açarını idxal etməyin ən asan yolu QR kod skan etməkdir. QR kod şəkli yaratmaq üçün `qrencode` proqramından istifadə edə bilərsiniz. Aşağıdakı kimi bir əmr işlədin:

```shell
# SSH açarları qovluğunuza keçin:
cd ~/.ssh

# QR kodu terminalda göstərin:
qrencode -r id_ed25519 -t ansiutf8

# Alternativ olaraq, şəkil faylı yaradın:
qrencode -r id_ed25519 -o qr.png

# 4096-bit RSA açarları QR kod üçün çox böyükdür. Birini güclə sığdırmaq üçün gzip istifadə edə bilərsiniz:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Menyudakı `İctimai açarı göndər` funksiyası ilə ictimai SSH açarlarını serverə göndərə bilərsiniz. Bu, seçilmiş SSH açarını `~/.ssh/authorized_keys` faylının sonuna əlavə edəcək. Bu yolla parolla girişdən SSH açarı ilə girişə asan keçid edə bilərsiniz.

Şifrələnmiş SSH açarlarını idxal edib istifadə edə bilərsiniz, amma tətbiqdə hazırda belə açar yaratmaq olmur.

## Təhlükəsizlik

Tətbiqdən SSH açarlarının şəxsi hissəsini və ya yadda saxlanmış parolları ixrac etmək və ya çıxarmaq mümkün deyil. Bu məlumatlar 256-bit AES ilə şifrələnir və şifrələmə açarı Android Keystore-da saxlanılır. Şifrələnmiş məlumatlar Android ehtiyat nüsxələrinə daxil edilmir.

Bu tətbiqdə crash report proqramı yoxdur. Telemetriya yoxdur. Reklam yoxdur. SSH bağlantısı istisna olmaqla şəbəkə sorğusu yoxdur.

Bu tətbiqin təhlükəsizliyi audit edilməyib. Android təhlükəsizliyi və ya SSH təhlükəsizliyi üzrə təcrübəniz varsa, zəhmət olmasa mənbə koduna baxın və tapdıqlarınızı bu GitHub issue-da paylaşın:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Funksiya təklifləri

GitHub deposunda funksiya təklifləri və xəta hesabatları paylaşa bilərsiniz. Zəhmət olmasa ingilis dilindən istifadə edin. Şərhlərinizdə nəzakətli olun. Hörmətsiz şərhlər silinəcək və istifadəçilər depodan bloklana bilər.

Sualınızın daha əvvəl verilib-verilmədiyini və cavablandırılıb-cavablandırılmadığını görmək üçün mövcud issue və müzakirə mövzularına baxın.

Xahiş edirəm hörmətli olun. Mən bu tətbiqi boş vaxtımda hazırlamışam və pulsuz təqdim edirəm. Bu tətbiqi ilk növbədə öz istifadəm üçün hazırlayıram.

Xahiş edirəm sualları e-poçtla göndərməyin. Müzakirələri GitHub-da saxlamağa çalışın, çünki bu başqalarına da kömək edir. Suallarınızı GitHub-un discussions bölməsində verə bilərsiniz.

Tətbiqi fork edib öz funksiyalarınızı əlavə etməkdə tam sərbəstsiniz. Bu, öyrənmək üçün əla üsuldur. Zəhmət olmasa faydalı funksiyalarla töhfə verməyi düşünün.

Mənbə kodu GNU GPLv3 lisenziyası ilə lisenziyalaşdırılıb. Bu tətbiqin dəyişdirilmiş versiyalarını paylayırsınızsa, mənbə kodunu da əlçatan etməlisiniz.

<https://github.com/stefansundin/SSHRemote>

## Dəstək

Minnətdarlığınızı göstərmək istəyirsinizsə, ianələr qəbul olunur.

<https://stefansundin.github.io/donate/>

Əgər ianə etmisinizsə, suallarınızı mümkün qədər yaxşı cavablandırmağa çalışacağam. Xahiş olunur, sorğularınızı ingilis dilində yazın.

Dəstəyiniz üçün təşəkkür edirəm!
