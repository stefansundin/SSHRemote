## À propos de SSH Remote

Cette traduction française a été réalisée par une IA via GitHub Copilot (le nom exact du modèle n'est pas exposé dans cette session) et peut contenir des erreurs de traduction.

SSH Remote est une application gratuite et open source qui vous permet de contrôler des ordinateurs à distance via SSH.

Vous pouvez personnaliser entièrement les commandes exécutées, et des préréglages sont disponibles pour les configurations courantes.

J'utilise cette application pour contrôler mon installation HTPC, qui fonctionne sous Raspberry Pi OS. Le contrôle d'un HTPC est le scénario principal pour lequel l'application est optimisée.

Cette application n'est pas un émulateur de terminal, mais elle vous permettra d'exécuter `apt-get install` en cas d'urgence.

## Premiers pas

Si vous souhaitez utiliser une clé SSH pour vous connecter, ouvrez d'abord les paramètres de l'application et importez ou générez une clé.

Ajoutez un nouvel hôte en appuyant sur le bouton `+` en bas à droite. Saisissez les détails de connexion et enregistrez.

La première fois que vous vous connecterez, il vous sera demandé de sélectionner un préréglage. Cette sélection configure les boutons de la télécommande pour bien fonctionner sur différents types d'ordinateurs. Voir ci-dessous pour une description des préréglages disponibles. Si vous préférez, vous pouvez commencer sans configuration en sélectionnant `Aucun préréglage`.

Si vous ne savez pas si votre ordinateur Linux utilise X11 ou Wayland, exécutez ceci dans un terminal :

```shell
echo $XDG_SESSION_TYPE
```

Cela doit afficher `x11` ou `wayland`. Vous devez exécuter cette commande à l'intérieur de l'environnement de bureau.

## Télécommande

Une fois connecté, vous pouvez utiliser l'interface de télécommande pour envoyer des commandes. Changez d'onglet pour accéder à différentes méthodes de saisie.

Chaque pression sur un bouton exécutera une commande sur l'hôte. Cela représente beaucoup de surcharge pour quelque chose d'aussi simple qu'un appui sur une touche, et vous pouvez constater une latence assez élevée par rapport à un clavier classique. J'espère améliorer cela dans les futures versions.

Utilisez le menu pour passer en mode édition. Il n'est actuellement pas possible de modifier la disposition ou les icônes des boutons. J'espère rendre cela possible dans une future version.

## Préréglages

Vous devrez installer l'outil requis pour votre environnement de bureau.

Je recommande `ydotool` car, d'après mes tests, il offre les meilleures performances et fonctionne à la fois avec X11 et Wayland.

### ydotool

`ydotool` devrait fonctionner avec n'importe quel gestionnaire de fenêtres, mais vous devez avoir un service en arrière-plan en cours d'exécution. Si votre distribution fournit un service utilisateur systemd, démarrez-le en exécutant :

```shell
systemctl start --user ydotool
```

Pour démarrer automatiquement le service à la connexion, exécutez :

```shell
systemctl enable --user ydotool
```

Assurez-vous d'installer une version suffisamment récente de `ydotool`. Les versions d'Ubuntu antérieures à 26.04 fournissent des versions trop anciennes. Consultez le fil de discussion pour une solution de contournement.

Veuillez discuter de `ydotool` dans ce fil de discussion : <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` est destiné aux ordinateurs utilisant X11. X11 est ce que la plupart des ordinateurs Linux ont utilisé historiquement, bien que Wayland devienne plus populaire.

Une particularité de X11 est que vous devrez peut-être autoriser l'accès au serveur X. C'est probablement le problème si vous obtenez des erreurs « Authorization required ». Vous avez plusieurs options pour corriger ce problème ; voici deux solutions qui ont fonctionné pour moi :

Si `xauth list` n'affiche aucune entrée, essayez de générer un fichier `.Xauthority` :

```shell
xauth generate :0 . trusted
```

Si cela n'a pas fonctionné, essayez alors d'accorder l'accès à l'aide de `xhost` :

```shell
xhost +local:$USER
```

Vous devrez exécuter la commande `xhost` après chaque démarrage. Vous pouvez automatiser cela en créant un script bash et en le configurant pour qu'il se lance automatiquement à la connexion.

Veuillez discuter de `xdotool` dans ce fil de discussion : <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` ressemble à `xdotool`, mais pour Wayland.

Normalement, il ne prend pas en charge le contrôle de la souris, mais j'ai créé une version modifiée qui ajoute cette prise en charge. Veuillez l'installer si vous avez besoin du contrôle de la souris : <https://github.com/stefansundin/wtype>

Si vous obtenez l'erreur `Compositor does not support the virtual keyboard protocol`, je vous suggère d'essayer un autre outil, comme `ydotool`.

Veuillez discuter de `wtype` dans ce fil de discussion : <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` devrait fonctionner avec n'importe quel gestionnaire de fenêtres, comme `ydotool`. Lors de mes tests limités, il était bien plus lent que `ydotool`.

Veuillez discuter de `dotool` dans ce fil de discussion : <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Ce préréglage est expérimental, car je n'ai pas pu le vérifier sur mon propre matériel. Les retours sont les bienvenus.

Veuillez discuter de `cec-client` dans ce fil de discussion : <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Il existe un préréglage pour contrôler VLC sur macOS à l'aide de commandes AppleScript. Je ne connais pas d'outil permettant d'envoyer des événements clavier ou souris.

Veuillez discuter de macOS dans ce fil de discussion : <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Si votre appareil Android est livré avec un serveur SSH, vous pourrez peut-être vous y connecter et envoyer des événements d'entrée. C'est plus probable si votre appareil exécute une ROM personnalisée, comme KonstaKANG sur le Raspberry Pi.

Je n'ai pas encore trouvé comment faire fonctionner la prise en charge de la souris.

Veuillez discuter d'Android dans ce fil de discussion : <https://github.com/stefansundin/SSHRemote/discussions/9>

## Contrôle intelligent du volume

Lors de la modification de la télécommande, vous pouvez trouver des paramètres de contrôle du volume « intelligent » dans le menu. Cela permet d'afficher le volume actuel de l'ordinateur dans l'application et de régler rapidement le volume à l'aide d'un curseur. Vous pouvez également utiliser les boutons matériels de votre appareil pour envoyer rapidement des commandes d'augmentation/de diminution du volume.

La lecture du volume actuel et le réglage d'un nouveau volume à l'aide du curseur sont actuellement codés en dur pour utiliser `pactl`.

Le paquet contenant `pactl` s'appelle généralement `pulseaudio-utils` ou `libpulse`.

## Clés SSH

Vous pouvez importer ou générer des clés SSH dans les paramètres de l'application. Se connecter avec une clé SSH est plus sûr que d'utiliser des mots de passe.

Le moyen le plus simple d'importer une clé SSH existante depuis un ordinateur est de scanner un code QR. Vous pouvez utiliser le programme `qrencode` pour générer l'image du code QR. Exécutez une commande comme celle-ci pour générer le code QR :

```shell
# Accédez à vos clés SSH :
cd ~/.ssh

# Affichez le code QR dans le terminal :
qrencode -r id_ed25519 -t ansiutf8

# Sinon, créez un fichier image :
qrencode -r id_ed25519 -o qr.png

# Les clés RSA 4096 bits sont trop volumineuses pour un code QR. Vous pouvez utiliser gzip pour en faire tenir une de justesse :
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Vous pouvez envoyer des clés SSH publiques vers un serveur à l'aide de la fonctionnalité `Envoyer la clé publique` du menu. Cela ajoutera la clé SSH sélectionnée au fichier `~/.ssh/authorized_keys`. Cela vous permet de migrer facilement d'une connexion par mot de passe à une connexion par clé SSH.

Vous pouvez importer et utiliser des clés SSH chiffrées, mais vous ne pouvez pas encore en générer dans l'application.

## Sécurité

Il n'est pas possible d'exporter ou d'extraire la partie privée des clés SSH, ni les mots de passe stockés, depuis l'application. Ces données sont chiffrées à l'aide d'AES 256 bits, et la clé de chiffrement est stockée dans l'Android Keystore. Les données chiffrées sont exclues des sauvegardes Android.

Il n'y a aucun logiciel de rapport de plantage dans cette application. Il n'y a aucune télémétrie. Il n'y a aucune publicité. Il n'y a aucune requête réseau en dehors de la connexion SSH.

La sécurité de cette application n'a pas été auditée. Si vous avez de l'expérience en sécurité Android ou en sécurité SSH, veuillez jeter un œil au code source et signaler vos conclusions dans cette issue GitHub :

<https://github.com/stefansundin/SSHRemote/issues/1>

## Demandes de fonctionnalités

N'hésitez pas à soumettre des demandes de fonctionnalités et des rapports de bugs dans le dépôt GitHub. Veuillez utiliser l'anglais. Veuillez garder des commentaires courtois. Les commentaires irrespectueux seront supprimés et les utilisateurs pourront être bloqués du dépôt.

Veuillez parcourir les issues existantes et les fils de discussion pour vérifier si votre question a déjà été posée ou a déjà reçu une réponse.

Veuillez rester respectueux. J'ai créé cette application sur mon temps libre et je la distribue gratuitement. Je développe d'abord cette application pour mon propre usage.

Merci de ne pas m'envoyer de questions par e-mail. Essayez plutôt de garder les conversations sur GitHub, car cela aide aussi les autres ! Vous pouvez poser des questions dans la section Discussions sur GitHub.

Vous êtes toujours libre de forker l'application pour implémenter vos propres fonctionnalités. C'est un excellent moyen d'apprendre. N'hésitez pas à contribuer des fonctionnalités utiles.

Le code source est distribué sous licence GNU GPLv3. Si vous distribuez des versions modifiées de cette application, vous devez également rendre le code source disponible.

<https://github.com/stefansundin/SSHRemote>

## Dons

Si vous souhaitez montrer votre gratitude et votre appréciation, les dons sont acceptés.

<https://stefansundin.github.io/donate/>

Si vous avez fait un don, je ferai de mon mieux pour répondre à toutes vos questions. Veuillez rédiger toute demande en anglais.

Merci pour votre soutien !
