## SSH Remote haqida

Ushbu oʻzbekcha tarjima GitHub Copilot yordamida tayyorlangan va unda tarjima xatolari boʻlishi mumkin.

SSH Remote — bu SSH orqali kompyuterlarni masofadan boshqarish imkonini beruvchi bepul va ochiq manbali ilova.

Siz bajariladigan buyruqlarni toʻliq moslashtirishingiz mumkin, shuningdek keng tarqalgan sozlamalar uchun oldindan tayyorlangan variantlar mavjud.

Men bu ilovadan Raspberry Pi OS ishlayotgan HTPC tizimimni boshqarish uchun foydalanaman. HTPC ni boshqarish ushbu ilova eng koʻp moslashtirilgan asosiy ssenariydir.

Bu ilova terminal emulyatori emas, ammo zarurat tugʻilganda `apt-get install` ni ishga tushirish imkonini beradi.

## Boshlash

Agar ulanish uchun SSH kalitidan foydalanmoqchi boʻlsangiz, avval ilova sozlamalarini ochib, kalitni import qiling yoki yarating.

Pastki oʻng burchakdagi `+` tugmasini bosib yangi host qoʻshing. Ulanish maʼlumotlarini kiriting va saqlang.

Birinchi marta ulanganingizda, sizdan oldindan sozlamani tanlash soʻraladi. Bu tanlov pult tugmalarini turli xil kompyuterlarda qulay ishlashi uchun sozlaydi. Mavjud oldindan sozlamalar tavsifi bilan quyida tanishishingiz mumkin. Istasangiz, `Oldindan sozlama yoʻq` ni tanlab hech qanday sozlamasiz boshlashingiz ham mumkin.

Agar Linux kompyuteringiz X11 yoki Wayland da ishlayotganini bilmasangiz, terminalda quyidagini ishga tushiring:

```shell
echo $XDG_SESSION_TYPE
```

Natijada `x11` yoki `wayland` chiqishi kerak. Bu buyruqni ish stoli muhiti ichida ishga tushirishingiz kerak.

## Pult

Ulangandan soʻng buyruqlar yuborish uchun pult interfeysidan foydalanishingiz mumkin. Turli kiritish usullariga oʻtish uchun ichki yorliqlarni almashtiring.

Har bir tugma bosilishi hostda buyruq bajaradi. Oddiy tugma bosish uchun bu anchagina ortiqcha yuk bo‘ladi va oddiy klaviaturaga nisbatan ancha sezilarli kechikish kuzatilishi mumkin. Kelgusi versiyalarda buni yaxshilashga umid qilaman.

Menyu orqali tahrirlash rejimiga kiring. Hozircha joylashuvni yoki tugma ikonalarini tahrirlash imkoni yoʻq. Kelgusida buni ham qoʻshishga umid qilaman.

## Oldindan sozlamalar

Ish stoli muhitingiz uchun kerakli vositani oʻrnatishingiz kerak bo‘ladi.

Men `ydotool` ni tavsiya qilaman, chunki sinovlarimda u eng yaxshi unumdorlikni ko‘rsatdi va X11 hamda Wayland da ishlaydi.

### ydotool

`ydotool` istalgan oynalar boshqaruvchisi bilan ishlashi kerak, ammo fon xizmati ishlab turishi lozim. Agar distributivingiz systemd user xizmatini taqdim etsa, uni quyidagi buyruq bilan ishga tushiring:

```shell
systemctl start --user ydotool
```

Tizimga kirishda xizmatni avtomatik ishga tushirish uchun quyidagini bajaring:

```shell
systemctl enable --user ydotool
```

Iltimos, `ydotool` ning yetarlicha yangi versiyasini oʻrnatayotganingizga ishonch hosil qiling. Ubuntu ning 26.04 dan oldingi versiyalarida taqdim etiladigan versiyalar juda eski. Aylanma yechim uchun muhokama mavzusiga qarang.

`ydotool` bo‘yicha muhokama shu yerda: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` X11 da ishlaydigan kompyuterlar uchun. Tarixan aksariyat Linux kompyuterlari X11 dan foydalangan, biroq Wayland tobora ommalashmoqda.

X11 ning bir o‘ziga xos jihati shundaki, sizga X serverga kirishga ruxsat berish kerak boʻlishi mumkin. Agar `Authorization required` xatolarini koʻrsangiz, muammo shunda boʻlishi mumkin. Bu muammoni hal qilishning bir nechta yo‘li bor, quyida menda ishlagan ikkita usul keltirilgan:

Agar `xauth list` hech qanday yozuv ko‘rsatmasa, `.Xauthority` faylini yaratib ko‘ring:

```shell
xauth generate :0 . trusted
```

Agar bu yordam bermasa, `xhost` yordamida ruxsat berib ko‘ring:

```shell
xhost +local:$USER
```

`xhost` buyrugʻini har safar tizim yuklangandan keyin qayta ishga tushirishingiz kerak boʻladi. Buni avtomatlashtirish uchun bash skript yaratib, uni tizimga kirishda avtomatik ishga tushadigan qilib sozlashingiz mumkin.

`xdotool` bo‘yicha muhokama shu yerda: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` — bu Wayland uchun `xdotool` ga oʻxshash vosita.

Odatda u sichqoncha boshqaruvini qoʻllab-quvvatlamaydi, lekin men sichqoncha qoʻllab-quvvatlovini qoʻshadigan oʻzgartirilgan versiyasini yaratganman. Agar sizga sichqoncha qoʻllab-quvvatlovi kerak boʻlsa, uni oʻrnating: <https://github.com/stefansundin/wtype>

Agar `Compositor does not support the virtual keyboard protocol` xatosini ko‘rsangiz, boshqa vositani, masalan `ydotool` ni sinab ko‘rishni tavsiya qilaman.

`wtype` bo‘yicha muhokama shu yerda: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` ham `ydotool` kabi istalgan oynalar boshqaruvchisi bilan ishlashi kerak. Cheklangan sinovlarimda u `ydotool` ga qaraganda ancha sekinroq bo‘ldi.

`dotool` bo‘yicha muhokama shu yerda: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Bu oldindan sozlama tajriba holatida, chunki men uni o‘z qurilmamda tekshira olmadim. Fikr-mulohazalar mamnuniyat bilan qabul qilinadi.

`cec-client` bo‘yicha muhokama shu yerda: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

macOS da AppleScript buyruqlari yordamida VLC ni boshqarish uchun oldindan sozlama mavjud. Klaviatura yoki sichqoncha hodisalarini yuborishni qoʻllab-quvvatlaydigan vositani men bilmayman.

macOS bo‘yicha muhokama shu yerda: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Agar Android qurilmangizda SSH server boʻlsa, unga ulanib kiritish hodisalarini yuborishingiz mumkin boʻlishi ehtimol. Bu, ayniqsa, qurilmangiz Raspberry Pi dagi KonstaKANG kabi maxsus ROM da ishlayotgan bo‘lsa ko‘proq ehtimolga ega.

Men hozircha sichqoncha qoʻllab-quvvatlovini qanday ishlatishni aniqlay olmadim.

Android bo‘yicha muhokama shu yerda: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Aqlli ovoz balandligi boshqaruvi

Pultni tahrirlash paytida menyudan “aqlli” ovoz balandligi sozlamalarini topishingiz mumkin. Bu ilovada kompyuterning joriy ovoz balandligini ko‘rsatishi va slayder yordamida ovoz balandligini tez sozlash imkonini berishi mumkin. Shuningdek, qurilmangizning apparat tugmalari yordamida ovoz balandligini oshirish yoki pasaytirish buyruqlarini tez yuborishingiz mumkin.

Joriy ovoz balandligini oʻqish va slayder yordamida yangi qiymat oʻrnatish hozircha `pactl` ga qatʼiy bogʻlangan.

`pactl` ni oʻz ichiga olgan paket odatda `pulseaudio-utils` yoki `libpulse` deb nomlanadi.

## SSH kalitlari

Ilova sozlamalarida SSH kalitlarini import qilishingiz yoki yaratishingiz mumkin. SSH kaliti bilan ulanish paroldan foydalanishga qaraganda xavfsizroqdir.

Kompyuterdan mavjud SSH kalitini import qilishning eng oson yoʻli — QR kodni skanerlash. QR kod rasmini yaratish uchun `qrencode` dasturidan foydalanishingiz mumkin. QR kodni yaratish uchun quyidagiga o‘xshash buyruqni ishga tushiring:

```shell
# SSH kalitlaringiz joylashgan katalogga oʻting:
cd ~/.ssh

# QR kodni terminalda ko‘rsating:
qrencode -r id_ed25519 -t ansiutf8

# Yoki rasm fayli yarating:
qrencode -r id_ed25519 -o qr.png

# 4096-bit RSA kalitlari QR kod uchun juda katta. Sigʻdirish uchun gzip dan foydalanishingiz mumkin:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Menyudagi `Ommaviy kalitni yuborish` funksiyasi yordamida ommaviy SSH kalitlarini serverga yuborishingiz mumkin. Bu tanlangan SSH kalitini `~/.ssh/authorized_keys` fayliga qoʻshadi. Shu tariqa parol bilan kirishdan SSH kaliti bilan kirishga oson oʻtishingiz mumkin.

Siz shifrlangan SSH kalitlarini import qilib foydalanishingiz mumkin, ammo hozircha ularni ilovaning oʻzida yaratib boʻlmaydi.

## Xavfsizlik

Ilovadan SSH kalitlarining maxfiy qismini yoki saqlangan parollarni eksport qilish yoki ajratib olish imkoni yoʻq. Bu maʼlumotlar 256-bit AES yordamida shifrlanadi, shifrlash kaliti esa Android Keystore da saqlanadi. Shifrlangan maʼlumotlar Android zaxira nusxalariga kiritilmaydi.

Ushbu ilovada nosozliklar haqida hisobot yuboruvchi dastur yoʻq. Telemetriya yoʻq. Reklama yoʻq. SSH ulanishidan tashqari hech qanday tarmoq soʻrovlari amalga oshirilmaydi.

Ushbu ilovaning xavfsizligi auditdan oʻtmagan. Agar siz Android xavfsizligi yoki SSH xavfsizligi boʻyicha tajribali boʻlsangiz, iltimos, manba kodini koʻrib chiqing va topganlaringizni ushbu GitHub issue da bildiring:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Funksiya soʻrovlari

GitHub repozitoriyasida yangi funksiyalar boʻyicha takliflar va xatoliklar haqida xabarlarni bemalol yuboring. Iltimos, ingliz tilidan foydalaning. Iltimos, izohlaringiz hurmat doirasida boʻlsin. Hurmatsiz izohlar oʻchiriladi va foydalanuvchilar repozitoriydan bloklanishi mumkin.

Savolingiz avvaldan berilgan yoki javoblangan bo‘lishi mumkin, shu sababli mavjud issue lar va muhokama mavzularini ko‘rib chiqing.

Iltimos, hurmat bilan muomala qiling. Men bu ilovani bo‘sh vaqtimda yaratganman va uni bepul tarqatmoqdaman. Men bu ilovani eng avvalo o‘zim foydalanishim uchun yaratmoqdaman.

Iltimos, savollarni menga email orqali yubormang. Suhbatlarni imkon qadar GitHub da olib borishga harakat qiling, chunki bu boshqalarga ham foyda beradi. Savollarni GitHub dagi discussions bo‘limida berishingiz mumkin.

Siz har doim ilovani fork qilib, oʻz funksiyalaringizni amalga oshirishingiz mumkin. Bu oʻrganishning ajoyib usuli. Iltimos, foydali funksiyalar bilan hissa qoʻshishni oʻylab koʻring.

Manba kodi GNU GPLv3 asosida litsenziyalangan. Agar siz ushbu ilovaning oʻzgartirilgan versiyalarini tarqatsangiz, manba kodini ham ochiq taqdim etishingiz kerak.

<https://github.com/stefansundin/SSHRemote>

## Xayriyalar

Agar minnatdorchiligingizni va eʼtirozingizni bildirmoqchi boʻlsangiz, xayriyalar qabul qilinadi.

<https://stefansundin.github.io/donate/>

Agar xayriya qilgan boʻlsangiz, men sizdagi har qanday savolga imkon qadar javob berishga harakat qilaman. Iltimos, murojaatlaringizni ingliz tilida yozing.

Qoʻllab-quvvatlaganingiz uchun rahmat!
