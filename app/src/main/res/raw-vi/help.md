## Giới thiệu về SSH Remote

Bản dịch tiếng Việt này được tạo bằng AI bởi GitHub Copilot và có thể vẫn còn lỗi dịch thuật.

SSH Remote là một ứng dụng miễn phí và mã nguồn mở cho phép bạn điều khiển máy tính từ xa bằng SSH.

Bạn có thể tùy chỉnh hoàn toàn các lệnh được thực thi, và có sẵn các cấu hình cho những thiết lập phổ biến.

Tôi dùng ứng dụng này để điều khiển hệ thống HTPC của mình, đang chạy Raspberry Pi OS. Điều khiển HTPC là tình huống sử dụng chính mà ứng dụng này được tối ưu.

Ứng dụng này không phải là một trình giả lập terminal, nhưng vẫn cho phép bạn chạy `apt-get install` trong trường hợp khẩn cấp.

## Bắt đầu

Nếu bạn muốn dùng khóa SSH để kết nối, trước tiên hãy mở phần cài đặt ứng dụng và nhập hoặc tạo một khóa.

Thêm máy chủ mới bằng cách nhấn nút `+` ở góc dưới bên phải. Nhập thông tin kết nối và lưu lại.

Lần đầu tiên kết nối, bạn sẽ được yêu cầu chọn một cấu hình sẵn. Lựa chọn này sẽ cấu hình các nút điều khiển từ xa để hoạt động tốt trên nhiều loại máy tính khác nhau. Xem bên dưới để biết mô tả về các cấu hình sẵn hiện có. Nếu muốn, bạn có thể bắt đầu mà không có cấu hình nào bằng cách chọn `Không có cấu hình sẵn`.

Nếu bạn không biết máy tính Linux của mình đang chạy X11 hay Wayland, hãy chạy lệnh này trong terminal:

```shell
echo $XDG_SESSION_TYPE
```

Lệnh này sẽ in ra `x11` hoặc `wayland`. Bạn phải chạy lệnh này bên trong môi trường desktop.

## Điều khiển từ xa

Sau khi kết nối, bạn có thể dùng giao diện điều khiển từ xa để gửi lệnh. Chuyển tab để truy cập các phương thức nhập khác nhau.

Mỗi lần nhấn nút sẽ thực thi một lệnh trên máy chủ. Đây là khá nhiều chi phí cho một thao tác đơn giản như nhấn phím, và bạn có thể gặp độ trễ khá cao so với bàn phím thông thường. Tôi hy vọng sẽ cải thiện điều này trong các phiên bản sau.

Dùng menu để vào chế độ chỉnh sửa. Hiện tại chưa thể chỉnh sửa bố cục hoặc biểu tượng nút. Tôi hy vọng sẽ làm được điều này trong phiên bản tương lai.

## Cấu hình sẵn

Bạn sẽ cần cài đặt công cụ phù hợp với môi trường desktop của mình.

Tôi khuyên dùng `ydotool` vì theo thử nghiệm của tôi, nó có hiệu năng tốt nhất và hoạt động được trên cả X11 lẫn Wayland.

### ydotool

`ydotool` sẽ hoạt động với bất kỳ trình quản lý cửa sổ nào, nhưng bạn cần có một dịch vụ nền đang chạy. Nếu bản phân phối của bạn cung cấp dịch vụ người dùng systemd thì hãy khởi động nó bằng lệnh:

```shell
systemctl start --user ydotool
```

Để tự động khởi động dịch vụ khi đăng nhập, chạy:

```shell
systemctl enable --user ydotool
```

Hãy chắc chắn rằng bạn cài đặt phiên bản `ydotool` đủ mới. Các phiên bản Ubuntu trước 26.04 cung cấp phiên bản quá cũ. Xem chủ đề thảo luận để biết cách khắc phục.

Vui lòng thảo luận về `ydotool` trong chủ đề này: <https://github.com/stefansundin/SSHRemote/discussions/5>

### xdotool

`xdotool` dành cho các máy tính chạy X11. X11 là hệ thống mà phần lớn máy tính Linux đã dùng trong lịch sử, dù Wayland đang ngày càng phổ biến hơn.

Một điểm đặc biệt của X11 là bạn có thể cần cho phép truy cập vào máy chủ X. Đây là nguyên nhân nếu bạn gặp lỗi "Authorization required". Bạn có một vài cách để khắc phục vấn đề này; dưới đây là hai cách đã hiệu quả với tôi:

Nếu `xauth list` không hiển thị mục nào thì hãy thử tạo tệp `.Xauthority`:

```shell
xauth generate :0 . trusted
```

Nếu cách đó không hiệu quả thì hãy thử cấp quyền truy cập bằng `xhost`:

```shell
xhost +local:$USER
```

Bạn sẽ cần chạy lệnh `xhost` sau mỗi lần khởi động. Bạn có thể tự động hóa việc này bằng cách tạo một script bash và cấu hình để tự khởi động khi đăng nhập.

Vui lòng thảo luận về `xdotool` trong chủ đề này: <https://github.com/stefansundin/SSHRemote/discussions/3>

### wtype

`wtype` giống như `xdotool`, nhưng dành cho Wayland.

Thông thường, nó không hỗ trợ điều khiển chuột, nhưng tôi đã tạo một phiên bản sửa đổi có thêm hỗ trợ chuột. Hãy cài đặt nó nếu bạn cần hỗ trợ chuột: <https://github.com/stefansundin/wtype>

Nếu bạn gặp lỗi `Compositor does not support the virtual keyboard protocol` thì tôi khuyên bạn nên thử một công cụ khác, chẳng hạn như `ydotool`.

Vui lòng thảo luận về `wtype` trong chủ đề này: <https://github.com/stefansundin/SSHRemote/discussions/4>

### dotool

`dotool` sẽ hoạt động với bất kỳ trình quản lý cửa sổ nào, tương tự như `ydotool`. Trong thử nghiệm hạn chế của tôi, nó chậm hơn `ydotool` khá nhiều.

Vui lòng thảo luận về `dotool` trong chủ đề này: <https://github.com/stefansundin/SSHRemote/discussions/6>

### cec-client

Cấu hình sẵn này vẫn đang ở mức thử nghiệm vì tôi chưa thể tự xác minh trên phần cứng của mình. Mọi phản hồi đều được hoan nghênh.

Vui lòng thảo luận về `cec-client` trong chủ đề này: <https://github.com/stefansundin/SSHRemote/discussions/7>

### macOS

Có một cấu hình sẵn để điều khiển VLC trên macOS, sử dụng các lệnh AppleScript. Tôi không biết công cụ nào hỗ trợ gửi sự kiện bàn phím hoặc chuột.

Vui lòng thảo luận về macOS trong chủ đề này: <https://github.com/stefansundin/SSHRemote/discussions/8>

### Android

Nếu thiết bị Android của bạn có sẵn máy chủ SSH, bạn có thể kết nối tới nó và gửi các sự kiện nhập liệu. Điều này có khả năng xảy ra cao hơn nếu thiết bị của bạn chạy ROM tùy chỉnh, chẳng hạn như KonstaKANG trên Raspberry Pi.

Tôi vẫn chưa tìm ra cách để hỗ trợ chuột hoạt động.

Vui lòng thảo luận về Android trong chủ đề này: <https://github.com/stefansundin/SSHRemote/discussions/9>

## Điều khiển âm lượng thông minh

Khi chỉnh sửa điều khiển từ xa, bạn có thể tìm thấy cài đặt điều khiển âm lượng "thông minh" trong menu. Tính năng này có thể hiển thị mức âm lượng hiện tại của máy tính trong ứng dụng và cho phép bạn nhanh chóng đặt âm lượng bằng thanh trượt. Bạn cũng có thể dùng các nút phần cứng của thiết bị để nhanh chóng gửi lệnh tăng/giảm âm lượng.

Việc đọc mức âm lượng hiện tại và đặt mức âm lượng mới bằng thanh trượt hiện được mã hóa cứng để dùng `pactl`.

Gói chứa `pactl` thường có tên là `pulseaudio-utils` hoặc `libpulse`.

## Khóa SSH

Bạn có thể nhập hoặc tạo khóa SSH trong phần cài đặt ứng dụng. Kết nối bằng khóa SSH an toàn hơn so với dùng mật khẩu.

Cách dễ nhất để nhập một khóa SSH hiện có từ máy tính là quét mã QR. Bạn có thể dùng chương trình `qrencode` để tạo hình ảnh mã QR. Hãy chạy lệnh tương tự như sau để tạo mã QR:

```shell
# Di chuyển tới thư mục chứa khóa SSH của bạn:
cd ~/.ssh

# Hiển thị mã QR trong terminal:
qrencode -r id_ed25519 -t ansiutf8

# Hoặc tạo tệp hình ảnh:
qrencode -r id_ed25519 -o qr.png

# Khóa RSA 4096-bit quá lớn cho mã QR. Bạn có thể dùng gzip để vừa khít:
cat id_rsa_4096 | gzip | base64 | qrencode -o qr.png
```

Bạn có thể đẩy khóa SSH công khai lên máy chủ bằng tính năng `Đẩy khóa công khai` trong menu. Tính năng này sẽ nối thêm khóa SSH đã chọn vào tệp `~/.ssh/authorized_keys`. Nhờ đó, bạn có thể dễ dàng chuyển từ đăng nhập bằng mật khẩu sang đăng nhập bằng khóa SSH.

Bạn có thể nhập và sử dụng khóa SSH được mã hóa, nhưng hiện tại không thể tạo các khóa này trong ứng dụng.

## Bảo mật

Không thể xuất hoặc trích xuất phần riêng tư của khóa SSH, hoặc các mật khẩu đã lưu, khỏi ứng dụng. Dữ liệu này được mã hóa bằng AES 256-bit, và khóa mã hóa được lưu trong Android Keystore. Dữ liệu đã mã hóa bị loại trừ khỏi các bản sao lưu Android.

Ứng dụng này không có phần mềm báo cáo sự cố. Không có telemetry. Không có quảng cáo. Không có yêu cầu mạng nào ngoài kết nối SSH.

Tính bảo mật của ứng dụng này chưa được kiểm toán. Nếu bạn có kinh nghiệm về bảo mật Android hoặc SSH, vui lòng xem mã nguồn và báo cáo phát hiện của bạn trong issue GitHub này:

<https://github.com/stefansundin/SSHRemote/issues/1>

## Yêu cầu tính năng

Hãy thoải mái gửi yêu cầu tính năng và báo lỗi trong kho lưu trữ GitHub. Vui lòng dùng tiếng Anh. Vui lòng giữ bình luận lịch sự. Những bình luận thiếu tôn trọng sẽ bị xóa và người dùng có thể bị chặn khỏi kho lưu trữ.

Vui lòng xem qua các issue và chủ đề thảo luận hiện có để biết câu hỏi của bạn đã được hỏi hoặc trả lời chưa.

Xin hãy tôn trọng. Tôi xây dựng ứng dụng này trong thời gian rảnh và đang phát hành miễn phí. Trước hết, tôi xây dựng ứng dụng này cho nhu cầu sử dụng của chính mình.

Vui lòng không gửi email câu hỏi cho tôi. Hãy cố gắng giữ cuộc trò chuyện trên GitHub, vì điều đó cũng giúp ích cho người khác! Bạn có thể đặt câu hỏi trong phần thảo luận trên GitHub.

Bạn luôn được chào đón để fork ứng dụng và tự triển khai các tính năng của riêng mình. Đây là một cách tuyệt vời để học hỏi. Hãy cân nhắc đóng góp các tính năng hữu ích.

Mã nguồn được cấp phép theo GNU GPLv3. Nếu bạn phân phối các phiên bản đã sửa đổi của ứng dụng này thì bạn cũng phải cung cấp mã nguồn.

<https://github.com/stefansundin/SSHRemote>

## Quyên góp

Nếu bạn muốn thể hiện sự biết ơn và trân trọng của mình, tôi có nhận quyên góp.

<https://stefansundin.github.io/donate/>

Nếu bạn đã quyên góp, tôi sẽ cố gắng hết sức để trả lời bất kỳ câu hỏi nào của bạn. Vui lòng viết mọi thắc mắc bằng tiếng Anh.

Cảm ơn bạn đã ủng hộ!
