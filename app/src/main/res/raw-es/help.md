## Acerca de SSH Remote

Esta traducción al español fue realizada con IA y puede contener errores.

SSH Remote es una aplicación gratuita y de código abierto que te permite controlar ordenadores de forma remota usando SSH.

Puedes personalizar por completo los comandos que se ejecutan, y hay preajustes para configuraciones habituales.

Yo uso esta aplicación para controlar mi configuración HTPC, que ejecuta Raspberry Pi OS. Controlar un HTPC es el escenario básico para el que la aplicación está optimizada.

Esta aplicación no es un emulador de terminal, pero te permitirá ejecutar `apt-get install` en una emergencia.

## Primeros pasos

Si quieres usar una clave SSH para conectarte, primero abre la configuración de la aplicación e importa o genera una clave.

Añade un nuevo host tocando el botón `+` en la esquina inferior derecha. Introduce los datos de conexión y guarda.

La primera vez que te conectes, se te pedirá que selecciones un preajuste. Esta selección configura los botones del control remoto para que funcionen bien en varios tipos de ordenadores. Más abajo encontrarás una descripción de los preajustes disponibles. Si lo prefieres, puedes empezar sin configuración seleccionando `Sin preajuste`.

Si no sabes si tu ordenador Linux está ejecutando X11 o Wayland, ejecuta esto en una terminal:

```shell
echo $XDG_SESSION_TYPE
```

Esto debería mostrar `x11` o `wayland`. Debes ejecutar esto dentro del entorno de escritorio.

## Control remoto

Una vez conectado, puedes usar la interfaz del control remoto para enviar comandos. Cambia de pestaña para acceder a varios métodos de entrada.

Cada pulsación de botón ejecutará un comando en el host. Esto supone mucha sobrecarga para algo tan simple como una pulsación de tecla, y es posible que experimentes una latencia bastante alta en comparación con un teclado normal. Espero mejorar esto en futuras versiones.

Usa el menú para entrar en el modo de edición. Actualmente no es posible editar el diseño ni los iconos de los botones. Espero poder hacerlo en una futura versión.

## Preajustes

Tendrás que instalar la herramienta necesaria para tu entorno de escritorio.

Recomiendo `ydotool` porque, en mis pruebas, ofrece el mejor rendimiento y funciona tanto con X11 como con Wayland.

### ydotool

`ydotool` debería funcionar con cualquier gestor de ventanas, pero necesitas que haya un servicio en segundo plano en ejecución. Si tu distribución proporciona un servicio de usuario de systemd, inícialo ejecutando:

```shell
systemctl start --user ydotool
```

Para iniciar automáticamente el servicio al iniciar sesión, ejecuta:

```shell
systemctl enable --user ydotool
```

Asegúrate de instalar una versión suficientemente reciente de `ydotool`. Las versiones de Ubuntu anteriores a la 26.04 incluyen versiones demasiado antiguas. Consulta el hilo de discusión para ver una solución alternativa.

Comenta sobre `ydotool` en este hilo de discusión: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` es para ordenadores que ejecutan X11. X11 es lo que históricamente han usado la mayoría de los ordenadores Linux, aunque Wayland se está volviendo más popular.

Una peculiaridad de X11 es que quizá necesites permitir el acceso al servidor X. Este es el problema si recibes errores de "Authorization required". Tienes varias opciones para solucionar este problema; aquí tienes dos opciones que me han funcionado:

Si `xauth list` no muestra ninguna entrada, intenta generar un archivo `.Xauthority`:

```shell
xauth generate :0 . trusted
```

Si eso no funcionó, intenta conceder acceso usando `xhost`:

```shell
xhost +local:$USER
```

Tendrás que ejecutar el comando `xhost` después de cada arranque. Puedes automatizarlo creando un script de bash y configurándolo para que se inicie automáticamente al iniciar sesión.

Comenta sobre `xdotool` en este hilo de discusión: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` es como `xdotool`, pero para Wayland.

Normalmente no admite control del ratón, pero he creado una versión modificada que añade soporte para ratón. Instálala si necesitas soporte para ratón: <https://github.com/stefansundin/wtype>

Si recibes el error `Compositor does not support the virtual keyboard protocol`, te sugiero probar otra herramienta, como `ydotool`.

Comenta sobre `wtype` en este hilo de discusión: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` debería funcionar con cualquier gestor de ventanas, de forma similar a `ydotool`. En mis pruebas limitadas fue mucho más lento que `ydotool`.

Comenta sobre `dotool` en este hilo de discusión: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Este preajuste es experimental, ya que no he podido verificarlo en mi propio hardware. Se agradecen los comentarios.

Comenta sobre `cec-client` en este hilo de discusión: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Hay un preajuste para controlar VLC en macOS, usando comandos de AppleScript. No conozco ninguna herramienta que permita enviar eventos de teclado o ratón.

Comenta sobre macOS en este hilo de discusión: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Si tu dispositivo Android incluye un servidor SSH, quizá puedas conectarte a él y enviar eventos de entrada. Esto es más probable si tu dispositivo ejecuta una ROM personalizada, como KonstaKANG en Raspberry Pi.

No he conseguido averiguar cómo hacer que funcione el soporte para ratón.

Comenta sobre Android en este hilo de discusión: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Control inteligente de volumen

Al editar el control remoto, puedes encontrar la configuración de control de volumen "inteligente" en el menú. Esto puede mostrar el volumen actual del ordenador en la aplicación y permitirte ajustar rápidamente el volumen con un deslizador. También puedes usar los botones físicos de tu dispositivo para enviar rápidamente comandos de subir o bajar volumen.

La lectura del volumen actual y el ajuste de un nuevo volumen con el deslizador están actualmente programados de forma fija para usar `pactl`.

El paquete que contiene `pactl` suele llamarse `pulseaudio-utils` o `libpulse`.

## Claves SSH

Puedes importar o generar claves SSH en la configuración de la aplicación. Conectarse con una clave SSH es más seguro que usar contraseñas.

La forma más sencilla de importar una clave SSH existente desde un ordenador es escanear un código QR. Puedes usar el programa `qrencode` para generar la imagen del código QR. Ejecuta un comando como el siguiente para generar el código QR:

```shell
# Navega a tus claves SSH:
cd ~/.ssh

# Muestra el código QR en la terminal:
qrencode -r id_ed25519 -t ansiutf8

# Como alternativa, crea un archivo de imagen:
qrencode -r id_ed25519 -o qr.png

# Las claves RSA de 4096 bits son demasiado grandes para un código QR. Puedes usar gzip para que apenas quepan:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Puedes enviar claves SSH públicas a un servidor usando la función `Enviar clave pública` del menú. Esto añadirá la clave SSH seleccionada al archivo `~/.ssh/authorized_keys`. Esto te permite migrar fácilmente de iniciar sesión con contraseña a iniciar sesión con una clave SSH.

Puedes importar y usar claves SSH cifradas, pero actualmente no puedes generarlas en la aplicación.

## Seguridad

No es posible exportar ni extraer la parte privada de las claves SSH, ni las contraseñas almacenadas, desde la aplicación. Estos datos se cifran usando AES de 256 bits, y la clave de cifrado se almacena en Android Keystore. Los datos cifrados se excluyen de las copias de seguridad de Android.

No hay software de informes de fallos en esta aplicación. No hay telemetría. No hay anuncios. No hay solicitudes de red, salvo la conexión SSH.

La seguridad de esta aplicación no ha sido auditada. Si tienes experiencia en seguridad de Android o seguridad SSH, echa un vistazo al código fuente y comunica tus hallazgos en esta incidencia de GitHub:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Solicitudes de funciones

No dudes en enviar solicitudes de funciones e informes de errores en el repositorio de GitHub. Por favor, usa inglés. Mantén tus comentarios dentro de un tono respetuoso. Los comentarios irrespetuosos serán eliminados y los usuarios podrán ser bloqueados del repositorio.

Revisa las incidencias y los hilos de discusión existentes para ver si tu pregunta ya se ha planteado o respondido.

Por favor, sé respetuoso. Construí esta aplicación en mi tiempo libre y la ofrezco gratis. Estoy creando esta aplicación para mi propio uso, ante todo.

Por favor, no me envíes preguntas por correo electrónico. Intenta mantener las conversaciones en GitHub, ¡ya que eso también ayuda a otras personas! Puedes hacer preguntas en la sección de discusiones de GitHub.

Siempre eres bienvenido a bifurcar la aplicación para implementar tus propias funciones. Es una forma estupenda de aprender. Considera contribuir con funciones útiles.

El código fuente está licenciado bajo GNU GPLv3. Si distribuyes versiones modificadas de esta aplicación, también debes poner el código fuente a disposición.

<https://github.com/stefansundin/SSHRemote>

## Donaciones

Si deseas mostrar tu gratitud y aprecio, se aceptan donaciones.

<https://stefansundin.github.io/donate/>

Si has donado, haré todo lo posible por responder cualquier pregunta que puedas tener. Si tienes alguna consulta, escríbela en inglés.

¡Gracias por tu apoyo!
