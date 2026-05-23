## SSH Remote 소개

이 번역은 AI를 사용해 작성되었으며 GitHub Copilot이 생성했습니다. 번역 오류나 어색한 표현이 있을 수 있으니 양해 부탁드립니다.

SSH Remote는 SSH를 사용하여 컴퓨터를 원격으로 제어할 수 있게 해 주는 무료 오픈 소스 앱입니다.

실행되는 명령을 완전히 사용자 지정할 수 있으며, 일반적인 환경을 위한 프리셋도 제공됩니다.

저는 이 앱을 Raspberry Pi OS를 실행하는 HTPC 환경을 제어하는 데 사용합니다. HTPC 제어는 이 앱이 기본적으로 최적화된 시나리오입니다.

이 앱은 터미널 에뮬레이터는 아니지만, 긴급한 경우 `apt-get install` 정도는 실행할 수 있습니다.

## 시작하기

SSH 키를 사용해 연결하려면 먼저 앱 설정을 열고 키를 가져오거나 생성하세요.

오른쪽 아래의 `+` 버튼을 탭해 새 호스트를 추가하세요. 연결 정보를 입력한 뒤 저장하면 됩니다.

처음 연결할 때는 프리셋을 선택하라는 안내가 표시됩니다. 이 선택은 다양한 종류의 컴퓨터에서 리모컨 버튼이 잘 동작하도록 설정해 줍니다. 사용 가능한 프리셋 설명은 아래를 참고하세요. 원한다면 `프리셋 없음`을 선택해 아무 설정 없이 시작할 수도 있습니다.

Linux 컴퓨터가 X11을 사용하는지 Wayland를 사용하는지 모르겠다면 터미널에서 다음 명령을 실행하세요.

```shell
echo $XDG_SESSION_TYPE
```

출력 결과는 `x11` 또는 `wayland`여야 합니다. 이 명령은 반드시 데스크톱 환경 안에서 실행해야 합니다.

## 리모컨

연결되면 리모컨 인터페이스를 사용해 명령을 전송할 수 있습니다. 탭을 전환해 다양한 입력 방식을 사용할 수 있습니다.

버튼을 한 번 누를 때마다 호스트에서 하나의 명령이 실행됩니다. 키 입력처럼 단순한 동작에도 오버헤드가 크기 때문에 일반 키보드에 비해 지연 시간이 꽤 크게 느껴질 수 있습니다. 앞으로 버전에서 개선하고 싶습니다.

메뉴를 사용해 편집 모드로 들어가세요. 현재는 레이아웃이나 버튼 아이콘을 편집할 수 없습니다. 앞으로 이런 기능도 추가할 수 있기를 바랍니다.

## 프리셋

데스크톱 환경에 맞는 도구를 설치해야 합니다.

제 테스트 기준으로는 `ydotool`이 가장 성능이 좋았고 X11과 Wayland 모두에서 동작하므로 이를 추천합니다.

### ydotool

`ydotool`은 어떤 창 관리자에서도 동작해야 하지만, 백그라운드 서비스가 실행 중이어야 합니다. 배포판에서 systemd 사용자 서비스를 제공한다면 다음 명령으로 시작하세요.

```shell
systemctl start --user ydotool
```

로그인 시 자동으로 서비스를 시작하려면 다음 명령을 실행하세요.

```shell
systemctl enable --user ydotool
```

반드시 충분히 최신 버전의 `ydotool`을 설치해 주세요. Ubuntu 26.04 이전 버전에 포함된 버전은 너무 오래되었습니다. 해결 방법은 토론 스레드를 참고하세요.

`ydotool` 관련 논의는 이 토론 스레드에서 해 주세요: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool`은 X11을 실행하는 컴퓨터용입니다. X11은 역사적으로 대부분의 Linux 컴퓨터에서 사용되어 왔지만, 최근에는 Wayland가 점점 더 널리 쓰이고 있습니다.

X11의 특징 중 하나는 X 서버 접근을 허용해야 할 수 있다는 점입니다. `Authorization required` 오류가 난다면 이 문제가 원인일 수 있습니다. 이를 해결하는 방법은 여러 가지가 있지만, 저에게 효과가 있었던 두 가지 방법은 다음과 같습니다.

`xauth list` 명령에 아무 항목도 보이지 않으면 `.Xauthority` 파일을 생성해 보세요.

```shell
xauth generate :0 . trusted
```

그래도 해결되지 않으면 `xhost`를 사용해 접근 권한을 부여해 보세요.

```shell
xhost +local:$USER
```

`xhost` 명령은 부팅할 때마다 다시 실행해야 합니다. bash 스크립트를 만들고 로그인 시 자동 시작되도록 설정하면 자동화할 수 있습니다.

`xdotool` 관련 논의는 이 토론 스레드에서 해 주세요: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype`는 Wayland용 `xdotool`과 비슷한 도구입니다.

원래는 마우스 제어를 지원하지 않지만, 제가 마우스 지원을 추가한 수정 버전을 만들었습니다. 마우스 지원이 필요하다면 이 버전을 설치해 주세요: <https://github.com/stefansundin/wtype>

`Compositor does not support the virtual keyboard protocol` 오류가 발생하면 `ydotool` 같은 다른 도구를 사용해 보시길 권장합니다.

`wtype` 관련 논의는 이 토론 스레드에서 해 주세요: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool`은 `ydotool`과 비슷하게 어떤 창 관리자에서도 동작해야 합니다. 다만 제한적인 테스트에서는 `ydotool`보다 훨씬 느렸습니다.

`dotool` 관련 논의는 이 토론 스레드에서 해 주세요: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

이 프리셋은 제 하드웨어에서 직접 확인하지 못했기 때문에 실험적입니다. 피드백을 환영합니다.

`cec-client` 관련 논의는 이 토론 스레드에서 해 주세요: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

macOS에서는 AppleScript 명령으로 VLC를 제어하는 프리셋이 있습니다. 키보드나 마우스 이벤트 전송을 지원하는 도구는 아직 알지 못합니다.

macOS 관련 논의는 이 토론 스레드에서 해 주세요: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Android 기기에 SSH 서버가 함께 제공된다면 연결해서 입력 이벤트를 전송할 수 있을지도 모릅니다. Raspberry Pi의 KonstaKANG 같은 커스텀 ROM을 사용하는 경우 그럴 가능성이 더 높습니다.

마우스 지원을 동작시키는 방법은 아직 찾지 못했습니다.

Android 관련 논의는 이 토론 스레드에서 해 주세요: <https://github.com/stefansundin/SSHRemote/discussions/9>

## 스마트 볼륨 제어

리모컨을 편집할 때 메뉴에서 "스마트" 볼륨 제어 설정을 찾을 수 있습니다. 이 기능은 앱에 현재 컴퓨터의 볼륨을 표시하고 슬라이더로 빠르게 볼륨을 설정할 수 있게 해 줍니다. 또한 기기의 하드웨어 버튼으로 볼륨 올리기/내리기 명령을 빠르게 보낼 수도 있습니다.

현재 볼륨 읽기와 슬라이더를 이용한 새 볼륨 설정은 현재 `pactl` 사용으로 고정되어 있습니다.

`pactl`이 들어 있는 패키지 이름은 보통 `pulseaudio-utils` 또는 `libpulse`입니다.

## SSH 키

앱 설정에서 SSH 키를 가져오거나 생성할 수 있습니다. SSH 키를 사용한 연결은 비밀번호를 사용하는 것보다 더 안전합니다.

기존 SSH 키를 컴퓨터에서 가져오는 가장 쉬운 방법은 QR 코드를 스캔하는 것입니다. `qrencode` 프로그램을 사용해 QR 코드 이미지를 만들 수 있습니다. 다음과 같은 명령으로 QR 코드를 생성할 수 있습니다.

```shell
# SSH 키 디렉터리로 이동:
cd ~/.ssh

# 터미널에 QR 코드 표시:
qrencode -r id_ed25519 -t ansiutf8

# 또는 이미지 파일 생성:
qrencode -r id_ed25519 -o qr.png

# 4096비트 RSA 키는 QR 코드에 너무 큽니다. gzip을 사용하면 간신히 넣을 수 있습니다:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

메뉴의 `공개 키 전송` 기능을 사용하면 공개 SSH 키를 서버로 보낼 수 있습니다. 이 기능은 선택한 SSH 키를 `~/.ssh/authorized_keys` 파일 끝에 추가합니다. 이를 통해 비밀번호 로그인에서 SSH 키 로그인으로 쉽게 전환할 수 있습니다.

암호화된 SSH 키도 가져와 사용할 수 있지만, 현재 앱에서는 생성할 수 없습니다.

## 보안

앱에서 SSH 키의 개인 키 부분이나 저장된 비밀번호를 내보내거나 추출하는 것은 불가능합니다. 이 데이터는 256비트 AES로 암호화되며, 암호화 키는 Android Keystore에 저장됩니다. 암호화된 데이터는 Android 백업에서 제외됩니다.

이 앱에는 충돌 보고 소프트웨어가 없습니다. 텔레메트리도 없습니다. 광고도 없습니다. 네트워크 요청은 SSH 연결 외에는 없습니다.

이 앱의 보안은 아직 감사를 받지 않았습니다. Android 보안이나 SSH 보안에 경험이 있다면 소스 코드를 살펴보고 이 GitHub 이슈에 의견을 남겨 주세요.

<https://github.com/stefansundin/SSHRemote/issues/1>

## 기능 요청

GitHub 저장소에 기능 요청과 버그 제보를 자유롭게 남겨 주세요. 영어를 사용해 주세요. 예의를 지켜 주세요. 무례한 댓글은 삭제될 수 있으며 저장소에서 차단될 수도 있습니다.

질문이 이미 올라왔거나 답변되었는지 기존 이슈와 토론 스레드를 먼저 확인해 주세요.

서로 존중해 주세요. 저는 이 앱을 여가 시간에 만들었고 무료로 배포하고 있습니다. 무엇보다도 이 앱은 제가 직접 사용하기 위해 만들고 있습니다.

질문은 이메일로 보내지 말아 주세요. 다른 사람들에게도 도움이 되므로 가능하면 GitHub에서 대화를 이어가 주세요. GitHub의 토론 섹션에서 질문할 수 있습니다.

원한다면 언제든지 앱을 포크하여 원하는 기능을 직접 구현해도 됩니다. 훌륭한 학습 방법입니다. 유용한 기능이라면 기여도 고려해 주세요.

소스 코드는 GNU GPLv3 라이선스로 배포됩니다. 이 앱의 수정 버전을 배포한다면 소스 코드도 함께 공개해야 합니다.

<https://github.com/stefansundin/SSHRemote>

## 후원

감사와 응원의 마음을 표현하고 싶다면 후원하실 수 있습니다.

<https://stefansundin.github.io/donate/>

후원해 주셨다면 제가 답변드리는 질문은 가능한 한 최선을 다해 도와드리겠습니다. 문의는 영어로 작성해 주세요.

감사합니다!
