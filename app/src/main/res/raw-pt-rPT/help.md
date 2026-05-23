## Acerca do SSH Remote

Esta tradução foi gerada com ajuda de IA e pode conter erros de tradução.

SSH Remote é uma aplicação livre e de código aberto que permite controlar computadores remotamente usando SSH.

Pode personalizar totalmente os comandos que são executados, e existem predefinições para configurações comuns.

Eu uso esta aplicação para controlar a minha configuração de HTPC, que está a executar Raspberry Pi OS. Controlar um HTPC é o cenário básico para o qual a aplicação foi otimizada.

Esta aplicação não é um emulador de terminal, mas permite-lhe executar `apt-get install` numa emergência.

## Primeiros passos

Se quiser usar uma chave SSH para se ligar, primeiro abra as definições da aplicação e importe ou gere uma chave.

Adicione um novo host tocando no botão `+` no canto inferior direito. Introduza os detalhes da ligação e guarde.

Da primeira vez que se ligar, ser-lhe-á pedido que selecione uma predefinição. Esta seleção configura os botões do controlo remoto para funcionarem bem em vários tipos de computadores. Veja abaixo uma descrição das predefinições disponíveis. Se preferir, pode começar sem configuração selecionando `No preset`.

Se não souber se o seu computador Linux está a executar X11 ou Wayland, execute isto num terminal:

```shell
echo $XDG_SESSION_TYPE
```

Isto deverá mostrar `x11` ou `wayland`. Tem de executar isto dentro do ambiente de desktop.

## Controlo remoto

Depois de estar ligado, pode usar a interface de controlo remoto para enviar comandos. Alterne entre os separadores para aceder a vários métodos de introdução.

Cada pressão de botão executará um comando no host. Isto cria bastante sobrecarga para algo tão simples como premir uma tecla, e poderá notar uma latência relativamente elevada em comparação com um teclado normal. Espero melhorar isto em versões futuras.

Use o menu para entrar no modo de edição. Atualmente, não é possível editar a disposição nem os ícones dos botões. Espero tornar isto possível numa versão futura.

## Predefinições

Terá de instalar a ferramenta exigida pelo seu ambiente de desktop.

Recomendo `ydotool` porque, nos meus testes, tem o melhor desempenho e funciona tanto em X11 como em Wayland.

### ydotool

`ydotool` deverá funcionar com qualquer gestor de janelas, mas precisa de um serviço em segundo plano a correr. Se a sua distribuição disponibilizar um serviço de utilizador do systemd, inicie-o executando:

```shell
systemctl start --user ydotool
```

Para iniciar o serviço automaticamente ao iniciar sessão, execute:

```shell
systemctl enable --user ydotool
```

Certifique-se de que está a instalar uma versão suficientemente recente do `ydotool`. Versões do Ubuntu anteriores à 26.04 fornecem versões demasiado antigas. Consulte o tópico de discussão para ver uma solução alternativa.

Discuta `ydotool` neste tópico de discussão: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` é para computadores que executam X11. O X11 é o que a maioria dos computadores Linux usou historicamente, embora o Wayland esteja a tornar-se mais popular.

Uma particularidade do X11 é que poderá precisar de permitir acesso ao servidor X. Esse é o problema se receber erros de "Authorization required". Tem algumas opções para corrigir isto; aqui estão duas opções que funcionaram para mim:

Se `xauth list` não mostrar nenhuma entrada, tente gerar um ficheiro `.Xauthority`:

```shell
xauth generate :0 . trusted
```

Se isso não funcionar, tente conceder acesso usando `xhost`:

```shell
xhost +local:$USER
```

Terá de executar o comando `xhost` após cada arranque. Pode automatizar isto criando um script bash e configurando-o para arrancar automaticamente ao iniciar sessão.

Discuta `xdotool` neste tópico de discussão: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` é como `xdotool`, mas para Wayland.

Normalmente, não suporta controlo do rato, mas eu criei uma versão modificada que adiciona suporte ao rato. Instale-a se precisar de suporte ao rato: <https://github.com/stefansundin/wtype>

Se receber o erro `Compositor does not support the virtual keyboard protocol`, sugiro que experimente outra ferramenta, como `ydotool`.

Discuta `wtype` neste tópico de discussão: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` deverá funcionar com qualquer gestor de janelas, de forma semelhante ao `ydotool`. Nos meus testes limitados, foi bastante mais lento do que `ydotool`.

Discuta `dotool` neste tópico de discussão: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Esta predefinição é experimental, pois não consegui verificá-la no meu próprio hardware. O seu feedback é bem-vindo.

Discuta `cec-client` neste tópico de discussão: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Existe uma predefinição para controlar o VLC no macOS, usando comandos AppleScript. Não conheço nenhuma ferramenta que suporte o envio de eventos de teclado ou rato.

Discuta macOS neste tópico de discussão: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Se o seu dispositivo Android vier com um servidor SSH, poderá conseguir ligar-se a ele e enviar eventos de introdução. Isto é mais provável se o seu dispositivo estiver a executar uma ROM personalizada, como KonstaKANG no Raspberry Pi.

Ainda não descobri como fazer o suporte ao rato funcionar.

Discuta Android neste tópico de discussão: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Controlo inteligente de volume

Ao editar o controlo remoto, pode encontrar definições de controlo de volume "inteligente" no menu. Isto pode mostrar o volume atual do computador na aplicação e permitir-lhe ajustar rapidamente o volume usando um controlo deslizante. Também pode usar os botões físicos do seu dispositivo para enviar rapidamente comandos de aumentar/diminuir volume.

A leitura do volume atual e a definição de um novo volume com o controlo deslizante usam atualmente `pactl` de forma fixa.

O pacote que contém `pactl` costuma chamar-se `pulseaudio-utils` ou `libpulse`.

## Chaves SSH

Pode importar ou gerar chaves SSH nas definições da aplicação. Ligar-se com uma chave SSH é mais seguro do que usar palavras-passe.

A forma mais fácil de importar uma chave SSH existente de um computador é digitalizar um código QR. Pode usar o programa `qrencode` para gerar a imagem do código QR. Execute um comando como o seguinte para gerar a imagem do código QR:

```shell
# Vá para as suas chaves SSH:
cd ~/.ssh

# Mostre o código QR no terminal:
qrencode -r id_ed25519 -t ansiutf8

# Em alternativa, crie um ficheiro de imagem:
qrencode -r id_ed25519 -o qr.png

# Chaves RSA de 4096 bits são demasiado grandes para um código QR.
# Pode usar gzip para as fazer caber por pouco:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Pode enviar chaves SSH públicas para um servidor usando a funcionalidade `Enviar chave pública` no menu. Isto anexará a chave SSH selecionada ao ficheiro `~/.ssh/authorized_keys`. Isto permite migrar facilmente do início de sessão com palavra-passe para o início de sessão com chave SSH.

Pode importar e usar chaves SSH encriptadas, mas atualmente não as pode gerar na aplicação.

## Segurança

Não é possível exportar nem extrair da aplicação a parte privada das chaves SSH nem as palavras-passe armazenadas. Estes dados são encriptados usando AES de 256 bits, e a chave de encriptação é armazenada no Android Keystore. Os dados encriptados são excluídos das cópias de segurança do Android.

Não existe software de relatório de falhas nesta aplicação. Não existe telemetria. Não existem anúncios. Não existem pedidos de rede além da ligação SSH.

A segurança desta aplicação não foi auditada. Se tiver experiência com segurança no Android ou segurança em SSH, dê uma vista de olhos ao código-fonte e relate as suas conclusões nesta issue do GitHub:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Pedidos de funcionalidades

Esteja à vontade para enviar pedidos de funcionalidades e relatórios de erros no repositório do GitHub. Use inglês. Mantenha os seus comentários respeitosos. Comentários desrespeitosos serão removidos e os utilizadores poderão ser bloqueados do repositório.

Veja as issues e os tópicos de discussão existentes para verificar se a sua pergunta já foi feita ou respondida.

Seja respeitoso. Eu construí esta aplicação no meu tempo livre e estou a disponibilizá-la gratuitamente. Estou a criar esta aplicação principalmente para o meu próprio uso.

Por favor, não me envie perguntas por e-mail. Tente manter as conversas no GitHub, pois isso também ajuda outras pessoas! Pode fazer perguntas na secção de discussões do GitHub.

Pode sempre fazer um fork da aplicação para implementar as suas próprias funcionalidades. Essa é uma ótima forma de aprender. Considere contribuir com funcionalidades úteis.

O código-fonte está licenciado sob a GNU GPLv3. Se distribuir versões modificadas desta aplicação, também terá de disponibilizar o código-fonte.

<https://github.com/stefansundin/SSHRemote>

## Doações

Se quiser demonstrar a sua gratidão e apreço, doações são aceites.

<https://stefansundin.github.io/donate/>

Se tiver feito uma doação, farei o meu melhor para responder a qualquer pergunta que tenha. Se entrar em contacto, escreva em inglês.

Obrigado pelo seu apoio!
