## SSH Remote Hakkında

Bu çeviri GitHub Copilot (GPT-5.3-Codex) tarafından hazırlanmıştır ve çeviri hataları içerebilir.

SSH Remote, SSH kullanarak bilgisayarları uzaktan kontrol etmenizi sağlayan ücretsiz ve açık kaynaklı bir uygulamadır.

Çalıştırılan komutları tamamen özelleştirebilirsiniz ve yaygın kurulumlar için ön ayarlar vardır.

Ben bu uygulamayı Raspberry Pi OS çalıştıran HTPC kurulumumu kontrol etmek için kullanıyorum. Bir HTPC kontrol etmek, uygulamanın optimize edildiği temel senaryodur.

Bu uygulama bir terminal öykünücüsü değildir, ancak acil bir durumda `apt-get install` çalıştırmanıza izin verir.

## Başlarken

Bağlanmak için bir SSH anahtarı kullanmak istiyorsanız önce uygulama ayarlarını açın ve bir anahtar içe aktarın veya oluşturun.

Sağ alttaki `+` düğmesine dokunarak yeni bir ana bilgisayar ekleyin. Bağlantı ayrıntılarını girin ve kaydedin.

İlk kez bağlandığınızda bir ön ayar seçmeniz istenir. Bu seçim, uzaktan kumanda düğmelerini çeşitli bilgisayar türlerinde iyi çalışacak şekilde yapılandırır. Kullanılabilir ön ayarların açıklaması için aşağıya bakın. İsterseniz `Ön ayar yok` seçeneğini seçerek hiçbir yapılandırma olmadan da başlayabilirsiniz.

Linux bilgisayarınızın X11 mi yoksa Wayland mı çalıştırdığını bilmiyorsanız terminalde şunu çalıştırın:

```shell
echo $XDG_SESSION_TYPE
```

Çıktı olarak `x11` veya `wayland` görmelisiniz. Bu komutu masaüstü ortamının içinden çalıştırmanız gerekir.

## Uzaktan Kumanda

Bağlandıktan sonra komut göndermek için uzaktan kumanda arayüzünü kullanabilirsiniz. Çeşitli giriş yöntemlerine erişmek için sekmeler arasında geçiş yapın.

Her düğme basışı ana bilgisayarda bir komut çalıştırır. Bu, basit bir tuş basışı için bile oldukça fazla ek yük demektir ve normal bir klavyeye kıyasla oldukça yüksek gecikme yaşayabilirsiniz. Bunu gelecekteki sürümlerde geliştirmeyi umuyorum.

Düzenleme moduna girmek için menüyü kullanın. Şu anda düzeni veya düğme simgelerini düzenlemek mümkün değildir. Bunu gelecekteki bir sürümde mümkün hale getirmeyi umuyorum.

## Ön Ayarlar

Masaüstü ortamınız için gerekli aracı kurmanız gerekir.

Ben `ydotool` öneriyorum çünkü testlerimde en iyi performansı verdi ve hem X11 hem de Wayland ile çalışıyor.

### ydotool

`ydotool` herhangi bir pencere yöneticisiyle çalışmalıdır, ancak arka planda çalışan bir hizmet gerekir. Dağıtımınız bir systemd kullanıcı hizmeti sağlıyorsa bunu şu komutla başlatın:

```shell
systemctl start --user ydotool
```

Hizmeti oturum açıldığında otomatik başlatmak için şunu çalıştırın:

```shell
systemctl enable --user ydotool
```

Lütfen yeterince yeni bir `ydotool` sürümü kurduğunuzdan emin olun. 26.04 sürümünden önceki Ubuntu sürümleri çok eski sürümler sağlar. Çözüm için tartışma başlığına bakın.

Lütfen `ydotool` hakkında şu tartışma başlığında konuşun: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool`, X11 çalıştıran bilgisayarlar içindir. X11 tarihsel olarak çoğu Linux bilgisayarın kullandığı sistemdi, ancak Wayland giderek daha popüler hale geliyor.

X11 ile ilgili bir tuhaflık, X sunucusuna erişime izin vermeniz gerekebilmesidir. `Authorization required` hataları alıyorsanız sorun budur. Bu sorunu düzeltmek için birkaç seçeneğiniz var; benim için işe yarayan iki seçenek şunlar:

`xauth list` hiçbir girdi göstermiyorsa bir `.Xauthority` dosyası oluşturmayı deneyin:

```shell
xauth generate :0 . trusted
```

Bu işe yaramadıysa `xhost` kullanarak erişim vermeyi deneyin:

```shell
xhost +local:$USER
```

`xhost` komutunu her yeniden başlatmadan sonra çalıştırmanız gerekir. Bunu otomatikleştirmek için bir bash betiği oluşturabilir ve oturum açıldığında otomatik başlayacak şekilde ayarlayabilirsiniz.

Lütfen `xdotool` hakkında şu tartışma başlığında konuşun: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype`, Wayland için `xdotool` gibidir.

Normalde fare kontrolünü desteklemez, ancak fare desteği ekleyen değiştirilmiş bir sürüm oluşturdum. Fare desteğine ihtiyacınız varsa lütfen bunu kurun: <https://github.com/stefansundin/wtype>

`Compositor does not support the virtual keyboard protocol` hatasını alırsanız `ydotool` gibi başka bir araç denemenizi öneririm.

Lütfen `wtype` hakkında şu tartışma başlığında konuşun: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool`, `ydotool` benzeri şekilde herhangi bir pencere yöneticisiyle çalışmalıdır. Sınırlı testlerimde `ydotool` uygulamasından çok daha yavaştı.

Lütfen `dotool` hakkında şu tartışma başlığında konuşun: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Kendi donanımımda doğrulayamamış olduğum için bu ön ayar deneyseldir. Geri bildirim memnuniyetle karşılanır.

Lütfen `cec-client` hakkında şu tartışma başlığında konuşun: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

macOS üzerinde AppleScript komutlarını kullanarak VLC kontrolü için bir ön ayar vardır. Klavye veya fare olayları göndermeyi destekleyen bir araç bildiğim yok.

Lütfen macOS hakkında şu tartışma başlığında konuşun: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Android cihazınızda bir SSH sunucusu varsa ona bağlanıp giriş olayları gönderebilirsiniz. Bu, cihazınız Raspberry Pi üzerinde KonstaKANG gibi özel bir ROM çalıştırıyorsa daha olasıdır.

Fare desteğini nasıl çalıştıracağımı henüz çözemedim.

Lütfen Android hakkında şu tartışma başlığında konuşun: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Akıllı ses kontrolü

Uzaktan kumandayı düzenlerken menüde "akıllı" ses kontrolü ayarlarını bulabilirsiniz. Bu özellik bilgisayarın geçerli ses düzeyini uygulamada gösterebilir ve bir kaydırıcı kullanarak sesi hızlıca ayarlamanıza izin verir. Ayrıca cihazınızın donanım düğmelerini kullanarak ses açma/kısma komutlarını hızlıca gönderebilirsiniz.

Geçerli ses düzeyini okuma ve kaydırıcıyla yeni bir ses düzeyi ayarlama işlemi şu anda sabit olarak `pactl` kullanacak şekilde kodlanmıştır.

`pactl` içeren paket genellikle `pulseaudio-utils` veya `libpulse` olarak adlandırılır.

## SSH anahtarları

Uygulama ayarlarında SSH anahtarlarını içe aktarabilir veya oluşturabilirsiniz. SSH anahtarıyla bağlanmak, parola kullanmaktan daha güvenlidir.

Bir bilgisayardaki mevcut bir SSH anahtarını içe aktarmanın en kolay yolu bir QR kod taramaktır. QR kod görselini oluşturmak için `qrencode` programını kullanabilirsiniz. QR kodu oluşturmak için aşağıdakine benzer bir komut çalıştırın:

```shell
# SSH anahtarlarınızın bulunduğu dizine gidin:
cd ~/.ssh

# QR kodu terminalde gösterin:
qrencode -r id_ed25519 -t ansiutf8

# Alternatif olarak, bir görsel dosyası oluşturun:
qrencode -r id_ed25519 -o qr.png

# 4096 bit RSA anahtarları bir QR kod için çok büyüktür. Bir tanesini zar zor sığdırmak için gzip kullanabilirsiniz:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Menüdeki `Açık anahtarı yükle` özelliğini kullanarak açık SSH anahtarlarını bir sunucuya yükleyebilirsiniz. Bu işlem seçilen SSH anahtarını `~/.ssh/authorized_keys` dosyasına ekler. Bu, parolayla oturum açmaktan SSH anahtarıyla oturum açmaya kolayca geçmenizi sağlar.

Şifrelenmiş SSH anahtarlarını içe aktarabilir ve kullanabilirsiniz, ancak şu anda uygulama içinde bunları oluşturamazsınız.

## Güvenlik

Uygulamadan SSH anahtarlarının özel kısmını veya saklanan parolaları dışa aktarmak ya da çıkarmak mümkün değildir. Bu veriler 256 bit AES kullanılarak şifrelenir ve şifreleme anahtarı Android Keystore içinde saklanır. Şifrelenmiş veriler Android yedeklemelerine dahil edilmez.

Bu uygulamada çökme raporlama yazılımı yoktur. Telemetri yoktur. Reklam yoktur. SSH bağlantısı dışında ağ isteği yoktur.

Bu uygulamanın güvenliği denetlenmemiştir. Android güvenliği veya SSH güvenliği konusunda deneyimliyseniz lütfen kaynak koduna bakın ve bulgularınızı şu GitHub issue içinde bildirin:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Özellik istekleri

GitHub deposunda özellik istekleri ve hata bildirimleri göndermekten çekinmeyin. Lütfen İngilizce kullanın. Lütfen yorumlarınızı saygılı tutun. Saygısız yorumlar kaldırılacak ve kullanıcılar depodan engellenebilecektir.

Sorunuzun daha önce sorulup sorulmadığını veya yanıtlanıp yanıtlanmadığını görmek için lütfen mevcut issue ve tartışma başlıklarına göz atın.

Lütfen saygılı olun. Bu uygulamayı boş zamanlarımda geliştirdim ve ücretsiz olarak veriyorum. Bu uygulamayı her şeyden önce kendi kullanımım için geliştiriyorum.

Lütfen bana e-postayla soru göndermeyin. Lütfen konuşmaları GitHub üzerinde tutmaya çalışın; çünkü bu başkalarına da yardımcı olur. Sorularınızı GitHub üzerindeki tartışmalar bölümünde sorabilirsiniz.

Kendi özelliklerinizi uygulamak için uygulamayı çatallamanız her zaman mümkündür. Bu öğrenmek için harika bir yoldur. Lütfen faydalı özelliklere katkıda bulunmayı düşünün.

Kaynak kodu GNU GPLv3 lisansı altındadır. Bu uygulamanın değiştirilmiş sürümlerini dağıtırsanız kaynak kodunu da erişilebilir kılmanız gerekir.

<https://github.com/stefansundin/SSHRemote>

## Bağışlar

Minnettarlığınızı ve takdirinizi göstermek isterseniz bağış kabul edilmektedir.

<https://stefansundin.github.io/donate/>

Bağış yaptıysanız soracağınız her soruyu elimden geldiğince yanıtlamaya çalışırım. Lütfen sorularınızı İngilizce yazın.

Desteğiniz için teşekkür ederim!
