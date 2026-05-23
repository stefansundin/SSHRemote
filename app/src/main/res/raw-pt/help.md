## Sobre o SSH Remote

Esta tradução foi gerada com ajuda de IA e pode conter erros de tradução.

SSH Remote é um aplicativo livre e de código aberto que permite controlar computadores remotamente usando SSH.

Você pode personalizar totalmente os comandos que são executados, e há predefinições para configurações comuns.

Eu uso este aplicativo para controlar minha configuração de HTPC, que está rodando Raspberry Pi OS. Controlar um HTPC é o cenário básico para o qual o aplicativo foi otimizado.

Este aplicativo não é um emulador de terminal, mas ele permitirá que você execute `apt-get install` em caso de emergência.

## Primeiros passos

Se quiser usar uma chave SSH para se conectar, primeiro abra as configurações do aplicativo e importe ou gere uma chave.

Adicione um novo host tocando no botão `+` no canto inferior direito. Digite os detalhes da conexão e salve.

Na primeira vez que você se conectar, será solicitado que selecione uma predefinição. Essa seleção configura os botões do controle remoto para funcionar bem em vários tipos de computadores. Veja abaixo uma descrição das predefinições disponíveis. Se preferir, você pode começar sem configuração selecionando `No preset`.

Se você não souber se o seu computador Linux está executando X11 ou Wayland, rode isto em um terminal:

```shell
echo $XDG_SESSION_TYPE
```

Isso deve mostrar `x11` ou `wayland`. Você precisa executar isso dentro do ambiente de desktop.

## Controle remoto

Depois de conectado, você pode usar a interface de controle remoto para enviar comandos. Alterne entre as abas para acessar vários métodos de entrada.

Cada pressionamento de botão executará um comando no host. Isso gera bastante sobrecarga para algo tão simples quanto o pressionamento de uma tecla, e você pode perceber uma latência relativamente alta em comparação com um teclado comum. Espero melhorar isso em versões futuras.

Use o menu para entrar no modo de edição. No momento, não é possível editar o layout nem os ícones dos botões. Espero tornar isso possível em uma versão futura.

## Predefinições

Você precisará instalar a ferramenta exigida pelo seu ambiente de desktop.

Eu recomendo `ydotool` porque, nos meus testes, ele tem o melhor desempenho e funciona tanto no X11 quanto no Wayland.

### ydotool

`ydotool` deve funcionar com qualquer gerenciador de janelas, mas você precisa de um serviço em segundo plano em execução. Se a sua distribuição fornecer um serviço de usuário do systemd, inicie-o executando:

```shell
systemctl start --user ydotool
```

Para iniciar o serviço automaticamente ao entrar, execute:

```shell
systemctl enable --user ydotool
```

Certifique-se de estar instalando uma versão suficientemente recente do `ydotool`. Versões do Ubuntu anteriores à 26.04 fornecem versões antigas demais. Consulte o tópico de discussão para ver uma solução alternativa.

Discuta `ydotool` neste tópico de discussão: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` é para computadores executando X11. O X11 é o que a maioria dos computadores Linux usou historicamente, embora o Wayland esteja se tornando mais popular.

Uma particularidade do X11 é que talvez você precise permitir acesso ao servidor X. Esse é o problema se você receber erros de "Authorization required". Você tem algumas opções para corrigir isso; aqui estão duas opções que funcionaram para mim:

Se `xauth list` não mostrar nenhuma entrada, tente gerar um arquivo `.Xauthority`:

```shell
xauth generate :0 . trusted
```

Se isso não funcionar, tente conceder acesso usando `xhost`:

```shell
xhost +local:$USER
```

Você precisará executar o comando `xhost` após cada inicialização. Você pode automatizar isso criando um script bash e configurando-o para iniciar automaticamente ao entrar.

Discuta `xdotool` neste tópico de discussão: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` é como `xdotool`, mas para Wayland.

Normalmente, ele não oferece suporte a controle do mouse, mas eu criei uma versão modificada que adiciona suporte ao mouse. Instale-a se você precisar de suporte ao mouse: <https://github.com/stefansundin/wtype>

Se você receber o erro `Compositor does not support the virtual keyboard protocol`, sugiro que experimente outra ferramenta, como `ydotool`.

Discuta `wtype` neste tópico de discussão: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` deve funcionar com qualquer gerenciador de janelas, de forma semelhante ao `ydotool`. Nos meus testes limitados, ele foi bem mais lento que o `ydotool`.

Discuta `dotool` neste tópico de discussão: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Esta predefinição é experimental, pois eu não consegui verificá-la no meu próprio hardware. Comentários são bem-vindos.

Discuta `cec-client` neste tópico de discussão: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Há uma predefinição para controlar o VLC no macOS usando comandos AppleScript. Não conheço uma ferramenta que ofereça suporte ao envio de eventos de teclado ou mouse.

Discuta macOS neste tópico de discussão: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Se o seu dispositivo Android vier com um servidor SSH, talvez você consiga se conectar a ele e enviar eventos de entrada. Isso é mais provável se o seu dispositivo estiver executando uma ROM personalizada, como KonstaKANG no Raspberry Pi.

Ainda não descobri como fazer o suporte ao mouse funcionar.

Discuta Android neste tópico de discussão: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Controle inteligente de volume

Ao editar o controle remoto, você pode encontrar configurações de controle de volume "inteligente" no menu. Isso pode exibir o volume atual do computador no aplicativo e permitir que você ajuste rapidamente o volume usando um controle deslizante. Você também pode usar os botões físicos do seu dispositivo para enviar rapidamente comandos de aumentar/diminuir volume.

A leitura do volume atual e a definição de um novo volume com o controle deslizante atualmente usam `pactl` de forma fixa.

O pacote que contém `pactl` geralmente se chama `pulseaudio-utils` ou `libpulse`.

## Chaves SSH

Você pode importar ou gerar chaves SSH nas configurações do aplicativo. Conectar-se com uma chave SSH é mais seguro do que usar senhas.

A maneira mais fácil de importar uma chave SSH existente de um computador é escanear um código QR. Você pode usar o programa `qrencode` para gerar a imagem do código QR. Execute um comando como o seguinte para gerar a imagem do código QR:

```shell
# Vá para as suas chaves SSH:
cd ~/.ssh

# Exiba o código QR no terminal:
qrencode -r id_ed25519 -t ansiutf8

# Como alternativa, crie um ficheiro de imagem:
qrencode -r id_ed25519 -o qr.png

# Chaves RSA de 4096 bits são grandes demais para um código QR.
# Você pode usar gzip para fazê-las caber por pouco:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Você pode enviar chaves SSH públicas para um servidor usando o recurso `Enviar chave pública` no menu. Isso anexará a chave SSH selecionada ao arquivo `~/.ssh/authorized_keys`. Isso permite migrar facilmente do login com senha para o login com chave SSH.

Você pode importar e usar chaves SSH criptografadas, mas atualmente não pode gerá-las no aplicativo.

## Segurança

Não é possível exportar nem extrair do aplicativo a parte privada das chaves SSH nem as senhas armazenadas. Esses dados são criptografados usando AES de 256 bits, e a chave de criptografia é armazenada no Android Keystore. Dados criptografados são excluídos dos backups do Android.

Não há software de relatório de falhas neste aplicativo. Não há telemetria. Não há anúncios. Não há solicitações de rede além da conexão SSH.

A segurança deste aplicativo não foi auditada. Se você tem experiência com segurança no Android ou segurança em SSH, dê uma olhada no código-fonte e relate suas conclusões nesta issue do GitHub:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Solicitações de recursos

Sinta-se à vontade para enviar solicitações de recursos e relatórios de bugs no repositório do GitHub. Use inglês. Mantenha seus comentários respeitosos. Comentários desrespeitosos serão removidos e os usuários poderão ser bloqueados do repositório.

Veja as issues e os tópicos de discussão existentes para saber se a sua pergunta já foi feita ou respondida.

Seja respeitoso. Eu criei este aplicativo no meu tempo livre e estou disponibilizando-o gratuitamente. Estou criando este aplicativo principalmente para meu próprio uso.

Por favor, não me envie perguntas por e-mail. Tente manter as conversas no GitHub, pois isso também ajuda outras pessoas! Você pode fazer perguntas na seção de discussões no GitHub.

Você sempre pode fazer um fork do aplicativo para implementar seus próprios recursos. Essa é uma ótima forma de aprender. Considere contribuir com recursos úteis.

O código-fonte está licenciado sob a GNU GPLv3. Se você distribuir versões modificadas deste aplicativo, também deverá disponibilizar o código-fonte.

<https://github.com/stefansundin/SSHRemote>

## Doações

Se quiser demonstrar sua gratidão e apreço, doações são aceitas.

<https://stefansundin.github.io/donate/>

Se você tiver feito uma doação, farei o meu melhor para responder a qualquer pergunta que você tiver. Se entrar em contato, escreva em inglês.

Obrigado pelo seu apoio!
