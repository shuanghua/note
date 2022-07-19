# shadowsocks-libev v2ray-plugin 插件的安装使用
对于 shadowsocks-rust 也适用



## 1.请确保已经正确安装好了 [ss-libev](https://github.com/shadowsocks/shadowsocks-libev)
没有安装则请看: [如何部署一台抗封锁的Shadowsocks-libev服务器](https://gfw.report/blog/ss_tutorial/zh/)




## 2.购买域名或申请免费域名
如果不是在 Cloudflare 购买域名, 还需要在域名提供商的管理后台将域名 DNS 解析替换成 Cloudflare DNS
例如阿里云,在域名列表,找到域名,点击最右边的管理,找到修改 DNS, 填入 Cloudflare 的 DNS(把域名添加到 Cloudflare 时会提供 Cloudflare 的 DNS)



## 3.登录 Cloudflare 并配置 dns 解析
本文参照的 Cloudflare 英文语言页面 
- cloudflare -> DNS 
  Add Record 一条 A 记录；name=你的域名，ipv4=服务器IP地址, ProxyStatus=关闭(灰色) , TTL=Auto 配置完成后再设置成橙色代理模式

- cloudflare -> SSL 
  Full (strict)  总是开启 HTTPS ( 自签名证书选 Full )

- 检查 DNS 解析
在本地电脑上 ping 域名, 确认和服务器的 ip 地址一致(由于 dns缓存 等原因可能需要过些时间才生效)

> 由于下面使用 acme 脚本申请的证书是 CA 颁发的，而不是自签证书，所以选择 Full (strict) 或者 Full 都行。又因为使用的是免费版套餐(cloudflare 的服务器在国外)， 选择 Flexible 也行；
不管选 Flexible 、Full 还是 Full-strict , 想要 CDN 代理都需要把 cloudflare 的小云朵设置成橙色时才有意义。



## 4.申请免费 CA 证书
- 和官方文档一样使用 [acme](https://github.com/Neilpang/acme.sh)  脚本自动申请免费证书  (免费证书有效期是 3 个月，该脚本会每隔 60 天自动更新一次证书有效期)

- 准备 cloudflare 注册时的 Email

- 准备 cloudflare api key ( 点击头像，然后点击 My Profile ，点击左侧列表的 API KEYs -> Global API KEY)

- 申请证书(服务器)
```bash
export CF_Email="cloudflare 邮箱"
export CF_Key="cloudflare api key"
```
```bash
curl https://get.acme.sh | sh
~/.acme.sh/acme.sh --register-account -m 你的个人邮箱
~/.acme.sh/acme.sh --issue --dns dns_cf -d 你的域名
```

执行完上面命令，acme 脚本就会自动帮你申请好证书，证书存放在 root/.acme.sh/你的域名/ 目录下。里面包含了 fullchain.cer 和 .key 等文件。

v2ray-plugin 会自动识别并且引用 root/.acme.sh 下的证书文件；但更建议把证书拷贝到另外的目录为好.

- 复制证书文件到指定目录(acme 脚本会自动更新对于的目录证书)
```bash
/root/.acme.sh/./acme.sh --install-cert -d 你的域名 --key-file /root/ssl/你的域名.key --fullchain-file /root/ssl/fullchain.cer
```



## 5.安装 golang(不推荐)
> 因为 v2ray-plugin 源码已处于稳定, 不需要频繁更新, 建议直接下载使用可执行的二进制文件; 同时还能解决 golang 版本差异导致编译失败等问题

安装 golang 是编译 v2ray-plugin 源码需要, 如果直接使用v2ray-plugin二进制可执行文件请跳过
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




## 6.安装 v2ray-plugin
- 方式1: 源码编译(不推荐)
```bash
git clone https://github.com/shadowsocks/v2ray-plugin.git
cd v2ray-plugin
go mod download
go build
cp v2ray-plugin /usr/bin/v2ray-plugin
cd ..
```

- 方式2:直接下载使用官方编译好的可执行二进制文件
在 [v2ray-plugin](https://github.com/shadowsocks/v2ray-plugin/releases) 页面直接下载对应 v2ray-plugin 执行文件, 解压后把文件移动到 /usr/bin/ 目录下
也可以使用 windows 下载到本地重命名为 v2ray-plugin, 然后再上传到服务器, 这里我使用的是 amd 64 位架构的文件作为例子:
```bash
wget https://github.com/shadowsocks/v2ray-plugin/releases/download/v1.3.1/v2ray-plugin-linux-amd64-v1.3.1.tar.gz
tar -zxvf v2ray-plugin-linux-amd64-v1.3.1.tar.gz
rm v2ray-plugin-linux-amd64-v1.3.1.tar.gz
mv v2ray-plugin_linux_amd64 /usr/bin
```
最后记得将配置文件的 v2ray-plugin 文件名字修改成解压后的文件名字: v2ray-plugin_linux_amd64
如果 snap 的 ss 不能访问 /bin/v2ray-plugin , 就放到 snap 能访问的目录,如 root 或者 var




## 7. ss + v2ray-plugin 配置
- https(websocket + tls)
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
  
  "plugin": "root/v2ray-plugin",
  "plugin_opts": "server;tls;mode=websocket;host=你的域名;cert=/证书目录/fullchain.cer;key=/证书目录/你的域名.key"
}
```


- http3(quic)
```bash
{
  "server": "0.0.0.0",
  "nameserver": "8.8.8.8",
  "server_port": 443,
  "password": "你的密码",
  "method": "chacha20-ietf-poly1305",
  "timeout": 400
  "no_delay": true,
  "mode": "tcp_only",
  
  "plugin": "v2ray-plugin",
  "plugin_opts": "server;mode=quic;host=你的域名;cert=/证书目录/fullchain.cer;key=/证书目录/你的域名.key"
}
````
> Quic 默认强制启用了 tls，所以不需要再在 plugin-opts 中再添加 tls 选项
> Quic 的内部使用的是 udp, 为了让 v2ray-plugin 的 quic 处理 udp, 所以不要在 ss 配置中使用 udp


- 重启 ss
然后在浏览器上用 https 访问域名，是否返回显示 Bad Request, 并查看 https 是否安全(点击地址栏左边的小锁头),
如果没问题就可以去配置客户端来使用了

- 配置文件中需要注意：
1. v2ray-plugin 和 ss-server 文件的路径要正确

2. 开放 443 端口

3. host=你的域名（不包含 https 或 http）

4. cert=证书存放的位置/fullchain.cer

5. ket=证书存放的位置/xxxxxx.com.key

3. cloudflare -> Network 打开 Http/3 (如果使用 quic)



## 8.Windows
- 请先把 v2ray-plugin-win.exe 文件下载到本地 SS 目录下
- 服务器地址: xxxxxxx.com
- 端口: 443
- 密码和加密看自己的配置文件
- 插件程序：v2ray-plugin-win
- 插件选项: tls;host=xxxxxx.com



## 9.Android
- 和 PC 差不多，从 github 或 谷歌应用市场下载安装 v2ray-plugin-android.apk,
- Trasport mode 选择: websocket-tls
- Hostname：xxxxxxxx.com
- 剩下默认即可(如果使用自签名证书,需要下载证书到本地使用)



## 10.最后
- 如果你看重的是隐蔽性，那么将 cloudflare -> DNS 的灰色云朵改为橙色，利用 cloudflare 中转代理, 此时数据会从客户端先发往 cloudflare
- 如果服务器在香港等临近的区域, 不建议开启中转代理, 因为会让流量绕一大圈才到达客户端(延迟增大)
- 如果中转代理速度实在太慢，就把小云朵改成灰色的 dnsonly。数据依然是 websocket + tls 隐蔽加密传输的。
- 由于 quic 的普及程度还不够高,受 qos 等限制, 选择 quic 可能会很慢. 
  

