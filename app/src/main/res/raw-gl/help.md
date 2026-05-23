## Acerca de SSH Remote

Esta tradución foi realizada por GitHub Copilot (GPT-5.3-Codex) e pode conter erros de tradución.

SSH Remote é unha aplicación libre e de código aberto que lle permite controlar computadores de maneira remota usando SSH.

Pode personalizar completamente os comandos que se executan, e hai predefinicións para configuracións comúns.

Eu uso esta aplicación para controlar a miña configuración HTPC, que está executando Raspberry Pi OS. Controlar un HTPC é o escenario básico para o que está optimizada a aplicación.

Esta aplicación non é un emulador de terminal, pero permitiralle executar `apt-get install` en caso de emerxencia.

## Primeiros pasos

Se quere usar unha clave SSH para conectarse, primeiro abra os axustes da aplicación e importe ou xere unha clave.

Engada un novo host tocando o botón `+` na esquina inferior dereita. Introduza os detalles da conexión e garde.

A primeira vez que se conecte pediráselle que seleccione unha predefinición. Esta selección configura os botóns do control remoto para que funcionen ben en varios tipos de computadores. Vexa máis abaixo unha descrición das predefinicións dispoñibles. Se o prefire, pode comezar sen ningunha configuración seleccionando `Sen predefinición`.

Se non sabe se o seu computador Linux está executando X11 ou Wayland, execute isto nun terminal:

```shell
echo $XDG_SESSION_TYPE
```

Isto debería devolver `x11` ou `wayland`. Debe executar isto dentro do contorno de escritorio.

## Control remoto

Unha vez conectado, pode usar a interface de control remoto para enviar comandos. Cambie entre lapelas para acceder a varios métodos de entrada.

Cada pulsación dun botón executará un comando no host. Isto supón moita sobrecarga para algo tan simple como premer unha tecla, e pode experimentar unha latencia bastante alta en comparación cun teclado normal. Espero melloralo en versións futuras.

Use o menú para entrar no modo de edición. Actualmente non é posible editar o deseño nin as iconas dos botóns. Espero facer isto posible nunha versión futura.

## Predefinicións

Terá que instalar a ferramenta necesaria para o seu contorno de escritorio.

Recomendo `ydotool` porque nas miñas probas ofreceu o mellor rendemento, e funciona tanto con X11 como con Wayland.

### ydotool

`ydotool` debería funcionar con calquera xestor de xanelas, pero precisa dun servizo en execución en segundo plano. Se a súa distribución ofrece un servizo de usuario de systemd, inícieo executando:

```shell
systemctl start --user ydotool
```

Para iniciar o servizo automaticamente ao iniciar sesión, execute:

```shell
systemctl enable --user ydotool
```

Asegúrese de estar instalando unha versión de `ydotool` suficientemente recente. As versións de Ubuntu anteriores á 26.04 proporcionan versións demasiado antigas. Consulte o fío de discusión para unha solución alternativa.

Comente sobre `ydotool` neste fío de discusión: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` é para computadores que executan X11. X11 é o que a maioría dos computadores Linux usaron historicamente, aínda que Wayland se está a facer máis popular.

Unha peculiaridade de X11 é que pode que teña que permitir o acceso ao servidor X. Ese é o problema se recibe erros de "Authorization required". Ten algunhas opcións para solucionar este problema; aquí van dúas que me funcionaron:

Se `xauth list` non mostra ningunha entrada, tente xerar un ficheiro `.Xauthority`:

```shell
xauth generate :0 . trusted
```

Se iso non funcionou, tente conceder acceso usando `xhost`:

```shell
xhost +local:$USER
```

Terá que executar o comando `xhost` despois de cada reinicio. Pode automatizalo creando un script de bash e configurándoo para que se inicie automaticamente ao iniciar sesión.

Comente sobre `xdotool` neste fío de discusión: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` é como `xdotool`, pero para Wayland.

Normalmente non admite control do rato, pero eu creei unha versión modificada que engade soporte para o rato. Instálea se precisa soporte para o rato: <https://github.com/stefansundin/wtype>

Se recibe o erro `Compositor does not support the virtual keyboard protocol`, suxírolle que probe outra ferramenta, como `ydotool`.

Comente sobre `wtype` neste fío de discusión: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` debería funcionar con calquera xestor de xanelas, de maneira semellante a `ydotool`. Nas miñas probas limitadas foi moito máis lento ca `ydotool`.

Comente sobre `dotool` neste fío de discusión: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Esta predefinición é experimental, xa que non puiden verificala no meu propio hardware. Agradecerei comentarios.

Comente sobre `cec-client` neste fío de discusión: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Hai unha predefinición para controlar VLC en macOS usando comandos AppleScript. Non coñezo ningunha ferramenta que admita enviar eventos de teclado ou rato.

Comente sobre macOS neste fío de discusión: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Se o seu dispositivo Android vén cun servidor SSH, quizais poida conectarse a el e enviar eventos de entrada. Isto é máis probable se o dispositivo executa unha ROM personalizada, como KonstaKANG no Raspberry Pi.

Aínda non descubrín como facer que funcione o soporte para o rato.

Comente sobre Android neste fío de discusión: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Control intelixente do volume

Ao editar o control remoto, pode atopar no menú os axustes de control de volume "intelixente". Isto pode mostrar o volume actual do computador na aplicación e permitirlle axustalo rapidamente usando un control deslizante. Tamén pode usar os botóns físicos do seu dispositivo para enviar rapidamente comandos de subir/baixar o volume.

A lectura do volume actual e o establecemento dun novo volume usando o control deslizante están actualmente codificados de maneira fixa para usar `pactl`.

O paquete que contén `pactl` adoita chamarse `pulseaudio-utils` ou `libpulse`.

## Claves SSH

Pode importar ou xerar claves SSH nos axustes da aplicación. Conectarse cunha clave SSH é máis seguro que usar contrasinais.

A forma máis sinxela de importar unha clave SSH existente desde un computador é escanear un código QR. Pode usar o programa `qrencode` para xerar a imaxe do código QR. Execute un comando como o seguinte para xerar o código QR:

```shell
# Vaia ao directorio das súas claves SSH:
cd ~/.ssh

# Mostre o código QR no terminal:
qrencode -r id_ed25519 -t ansiutf8

# Como alternativa, cree un ficheiro de imaxe:
qrencode -r id_ed25519 -o qr.png

# As claves RSA de 4096 bits son demasiado grandes para un código QR. Pode usar gzip para facer que unha entre por pouco:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Pode enviar claves SSH públicas a un servidor usando a función `Enviar clave pública` no menú. Isto engadirá a clave SSH seleccionada ao ficheiro `~/.ssh/authorized_keys`. Isto permítelle migrar facilmente de iniciar sesión cun contrasinal a iniciar sesión cunha clave SSH.

Pode importar e usar claves SSH cifradas, pero actualmente non pode xeralas na aplicación.

## Seguridade

Non é posible exportar nin extraer da aplicación a parte privada das claves SSH nin os contrasinais gardados. Estes datos cífranse usando AES de 256 bits, e a clave de cifrado almacénase no Android Keystore. Os datos cifrados exclúense das copias de seguridade de Android.

Nesta aplicación non hai software de informe de fallos. Non hai telemetría. Non hai anuncios. Non hai solicitudes de rede agás a conexión SSH.

A seguridade desta aplicación non foi auditada. Se ten experiencia en seguridade de Android ou seguridade SSH, bote unha ollada ao código fonte e informe dos seus achados nesta issue de GitHub:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Solicitudes de funcións

Non dubide en enviar solicitudes de funcións e informes de erros no repositorio de GitHub. Use o inglés. Manteña os seus comentarios respectuosos. Os comentarios irrespectuosos eliminaranse e os usuarios poderán ser bloqueados do repositorio.

Revise as issues e os fíos de discusión existentes para ver se a súa pregunta xa foi formulada ou respondida.

Sexa respectuoso. Construín esta aplicación no meu tempo libre e estou regalándoa. Estou desenvolvendo esta aplicación principalmente para o meu propio uso.

Non me envíe preguntas por correo electrónico. Tente manter as conversas en GitHub, xa que iso tamén axuda a outras persoas. Pode facer preguntas na sección de discusións de GitHub.

Sempre pode facer un fork da aplicación para implementar as súas propias funcións. É unha boa maneira de aprender. Considere contribuír con funcións útiles.

O código fonte está licenciado baixo GNU GPLv3. Se distribúe versións modificadas desta aplicación, tamén deberá poñer o código fonte á disposición dos demais.

<https://github.com/stefansundin/SSHRemote>

## Doazóns

Se quere mostrar a súa gratitude e agradecemento, acéptanse doazóns.

<https://stefansundin.github.io/donate/>

Se fixo unha doazón, intentarei responder o mellor posible a calquera pregunta que poida ter. Por favor, escriba as consultas en inglés.

Grazas polo seu apoio!
