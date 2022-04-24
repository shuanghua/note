---
toc: true
comments: true
description: debian10 从 snap 上安装 ss-libev
title: Debian 从 snap 上安装 ss-libev
tags:
  - Linux
id: 23-4
categories:
  - Linux
date: 2019-10-11
---



# 安装 snap 
```bash
sudo apt update
sudo apt install snapd -y
sudo snap install core
```



<!-- more -->




# 安装 ss-libev 开发版(非稳定版)
```bash
sudo snap install shadowsocks-libev --classic --edge
```



# 配置 ss

配置文件 config.json 一定要放在 **/snap/bin/** 中, 也可以放在 **/root** 根目录下

- 配置文件
```json
{
  "server": "0.0.0.0",
  "nameserver": "8.8.8.8",
  "server_port": 443,
  "password": "你的密码",
  "method": "chacha20-ietf-poly1305",
  "timeout": 600,
  "no_delay": true,
  "mode": "tcp_and_udp",
  "plugin": "v2ray-plugin",
  "plugin_opts": "server;tls;fast-open;host=xxxxxxx.com;cert=/证书目录/fullchain.cer;key=/证书目录/xxxxxxx.com.key;loglevel=none"
}
```




- 非 snap 启动的 ss 的命令 (**这里只用做对比参考**)
```bash
[Unit]
Description=Shadowsocks Server
After=network.target
[Service]
Restart=on-abnormal
ExecStart=/usr/local/bin/ss-server -c /etc/shadowsocks-libev/config.json
[Install]
WantedBy=multi-user.target
```

- snap 启动的 ss 的命令 (开机自启文件)
```bash
[Unit]
Description=Shadowsocks Server
After=network.target
[Service]
Restart=on-abnormal
ExecStart=/snap/bin/shadowsocks-libev.ss-server -c /snap/bin/config.json
[Install]
WantedBy=multi-user.target
```
就是 ss-server 变成了 shadowsocks-libev.ss-server ； 然后就是配置文件存放的目录变了。


- ss-rust
```bash
[Unit]
Description=ss start service
After=network.target
[Service]
Restart=on-abnormal
ExecStart=/snap/bin/shadowsocks-rust.ssserver -c /snap/bin/ssconfig.json
[Install]
WantedBy=multi-user.target
```


# snap 更新 ss
```bash
snap refresh shadowsocks-libev --edge
```


# snap 安装 Golang 
```bash
snap install go --channel=latest/stable --classic
```


# 另附
- [安装 v2ray-plugin](https://gist.github.com/shuanghua/c9c448f9bd12ebbfd720b34f4e1dd5c6)


- [Caddy2 反代 ss-libev](https://gist.github.com/shuanghua/22c7f5fb558fdac27411b6c83614c04b)