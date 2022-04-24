# ss-libev 插件 v2ray-plugin 的安装使用

## 请确保已经正确安装好了 [ss-libev](https://github.com/shadowsocks/shadowsocks-libev)

## 购买域名 / 或申请免费域名

## 配置 Cloudflare

1. cloudflare -> DNS 添加一条 A 记录；name=域名，value=vpsIP, ttl=automatic, status=onlyDns(灰色) 配置完成后再设置成橙色代理模式
2. cloudflare -> SSL =  Full (strict)  总是开启 HTTPS ( 自签名证书选 Full )

> 下文我用的 acme 脚本申请的证书是 ca 认证的，不是自签证书，所以我选择 Full (strict) 或者 Full 都行。又因为使用的是免费版套餐， cloudflare 的服务器在国外，我甚至选择 Flexible 也行；不管是 Flexible 、Full 还是 Full-strict 都是在 cloudflare 的小云朵设置成橙色时才有意义。

## 安装 golang

查看 [Go 的最新版本](https://golang.org/dl/ "xx") ，建议安装最新版本的前一两个版本。版本需要与 v2ray-plugin 官方代码同步，太旧和太超前都不合适。

- 下载安装 （root 下安装）
  
  ```bash
  cd ~ && curl -O https://dl.google.com/go/go1.16.10.linux-amd64.tar.gz
  ```

- 解压
  
  ```bash
  tar -C /usr/local -xzf go1.16.10.linux-amd64.tar.gz
  ```

- 添加到环境变量
  
  ```bash
  vim ~/.bash_profile
  ```

- 添加以下两行内容到文末:
  
  ```
  export GOPATH=$HOME/work
  export PATH=$PATH:/usr/local/go/bin:$GOPATH/bin
  ```

- 使环境变量生效
  
  ```bash
  source ~/.bash_profile
  ```

- 检查版本
  
  ```bash
  go version
  ```

## 安装 v2ray-plugin

- 方式1: 编译v2ray-plugin golang 源码
  
  ```bash
  git clone https://github.com/shadowsocks/v2ray-plugin.git
  cd v2ray-plugin
  go mod download
  go build
  cp v2ray-plugin /usr/bin/v2ray-plugin
  cd ..
  ```

- 方式2:直接使用编译好的可执行二进制文件
  到 [github release](https://github.com/shadowsocks/v2ray-plugin/releases "v2ray-plugin releases") 页面直接下载对应 v2ray-plugin 执行文件,然后把该文件移动到 /usr/bin/ 目录下
  
  ```bash
  cp v2ray-plugin /usr/bin/v2ray-plugin
  ```

# 申请 cloudflare 颁发的具有 ca 认证的证书

- 和官方文档一样使用 [acme](https://github.com/Neilpang/acme.sh)  脚本自动申请免费证书  (免费证书有效期一般是 3 个月，该脚本会每隔 60 天自动更新一次证书有效期)

- 准备 cloudflare 的注册邮箱

- 准备 cloudflare api key ( 点击头像，然后点击 My Profile ，在个人信息页面下拉到最后有个 API KEYs -> **Global API KEY**)

- 申请证书 （申请证书之前确保你的域名已经解析到你的服务器地址 ）
  
  ```bash
  export CF_Email="cloudflare 邮箱"
  export CF_Key="cloudflare api key"
  ```
  
  ```bash
  curl https://get.acme.sh | sh
  ~/.acme.sh/acme.sh --register-account -m 你的邮箱
  ~/.acme.sh/acme.sh --issue --dns dns_cf -d 你的域名
  ```

执行完上面命令，acme 脚本就会自动帮你申请好了证书，证书存放的目录在 root/.acme.sh/你的域名/ 。里面包含了 fullchain.cer 和 .key 等文件。如果你有网站之类的，请把证书移动到具有更安全权限的目录下去使用。

v2ray-plugin 会自动识别并且引用 acme 申请的证书文件；当然你也可以把证书链接放到指定的具体目录下，然后设置只读权限（推荐）

```bash
ln -s ~/.acme.sh/xxxx.com /etc/ssl/xxxx.com
```

# https ( websocket + tls )

- ss 配置文件
  
  ```bash
  {
    "server": "0.0.0.0",
    "nameserver": "8.8.8.8",
    "server_port": 443,
    "password": "你的密码",
    "method": "chacha20-ietf-poly1305",
    "timeout": 400
    "no_delay": true,
    "mode": "tcp_and_udp",
    "plugin": "v2ray-plugin",
    "plugin_opts": "server;tls;fast-open;host=xxxxxxxxx.com;cert=/证书目录/fullchain.cer;key=/证书目录/xxxxxxxxx.com.key;loglevel=none"
  }
  ```

- service 配置
  
  ```
  [Unit]
  Description=Shadowsocks Server
  After=network.target
  [Service]
  ExecStart=/usr/bin/ss-server -c /etc/shadowsocks-libev/config.json
  Restart=on-abort
  [Install]
  WantedBy=multi-user.target
  ```

上面的配置需要注意 5 点：

1. v2ray-plugin 和 ss-server 文件的路径要正确

2. 开放 443 端口

3. host=你的域名（不含 https 或 http）

4. cert=证书存放的位置/fullchain.cer

5. ket=证书存放的位置/xxxxxx.com.key
- 检查
  
  ```
  systemctl daemon-reload
  systemctl restart ss
  systemctl status ss -l
  ```

- 将 cloudflare 小云朵设置为灰色（onlyDns） ，然后在本地电脑上 ping 域名, 确认和服务器的 ip 地址是否一致

- 在浏览器上用 https 访问域名，是否返回显示 Bad Request,同时查看 https 是否安全（点击小锁头）

# quic ( quic + tls )

quic 核心是 udp，而大部分运营商对 udp 的支持不友好, udp 丢包严重，有时候不使用 quic 更好。(目前 v2ray-core 的 quic 还没有升级到最新的 http3 标准, 也就是说还没有支持 http3 的插件可以用，你可以通过 caddy2 启用 http3，但是本地没有支持 http3 的代理客户端可用)

- ss 配置文件 ，需要关闭 ss 的 udp，让 quic 模块去处理 udp
  
  ```json
  {
    "mode": "tcp_only"
  }
  ```

- plugin-opts 中删掉 tls,然后添加 mode=quic
  
  ```json
  {
    "plugin_opts": "server;mode=quic;host=xxxxxxxxx.com;cert=/证书目录/fullchain.cer;key=/证书目录/xxxxxxxxx.com.key;loglevel=none"
  }
  ```
  
  > 由于 v2ray-plugin 中的 quic 默认强制启用了 tls，所以你不需要再在 plugin-opts 中再添加 tls 选项。修改完后记得在客户端插件选项添加 mode=quic
3. cloudflare -> Network 打开 Http/3 (如果使用 quic)

# 最后

- **如果你看重的是隐蔽性，那么记得将 cloudflare -> DNS 的灰色云朵改为橙色，完成 cloudflare 中转代理**
  
  > 同样还可以去查查 cloudflare 延迟比较低的 ip,然后填到你的 ss 客户端地址中去使用 [https://www.xjisme.com/archives/2265.html](https://www.xjisme.com/archives/2265.html)

- 如果速度实在慢的受不了 ，把小云朵改成灰色的 dnsonly。只要设置了 tls ，那么数据依然是通过 tls 加密传输的。

# 客户端

## PC

- 请先把 v2ray-plugin-win.exe 文件下载到本地 SS 目录下
- 服务器地址: xxxxxxx.com
- 端口: 443
- 密码和加密看自己的配置文件
- 插件程序：v2ray-plugin-win
- 插件选项: tls;host=xxxxxx.com

## Android

- 和 PC 差不多，从 github 或 谷歌应用市场下载安装 v2ray-plugin-android.apk,
- Trasport mode 选择: websocket-tls
- Hostname：xxxxxxxx.com
- 剩下默认即可