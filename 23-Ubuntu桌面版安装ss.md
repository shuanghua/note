---
toc: true
comments: true
description: 平常我们都是在 Windows 下使用，现在是 Ubuntu 16+ 桌面系统上的使用
title: Ubuntu 16 桌面版 ss-libev [客户端]
tags:
  - Linux
id: 23-2
categories:
  - Linux
date: 2017-10-9
---


# Ubuntu 16 桌面版 ss-libev [客户端]
平常我们都是在 Windows 下使用，现在是 Ubuntu 16+ 桌面系统上的使用

- 安装 ss-libev
> 有两种方式来安装，看自己需要。


<!-- more -->


- 从 PPA 下载安装 ss-libev ,这种安装快速简单，但是版本更新可能不及时。
```bash
sudo apt-get install software-properties-common -y
sudo add-apt-repository ppa:max-c-lv/shadowsocks-libev -y
sudo apt-get update
sudo apt install shadowsocks-libev
```

- 从源码安装>>>>>看前面的文章


## 配置 ss-libev 文件
- 创建账号密码配置文件
```bash
vi /etc/shadowsocks-libev/config.json
```


- 内容如下：
```json
{
  "server": "ss服务器IP",
  "server_port": ss端口,
  "local_port": 1088,
  "password": "密码",
  "timeout": 600,
  "method": "xchacha20-ietf-poly1305"
}
```


## 配置 开机自启 service 文件
- 创建服务文件
```bash
vi /etc/systemd/system/ss.service
```

- 往该文件粘贴以下内容：
```
[Unit]
Description=Shadowsocks Server
After=network.target
[Service]
ExecStart=/usr/bin/ss-local -c /etc/shadowsocks-libev/config.json --plugin obfs-local --plugin-opts "obfs=http;obfs-host=www.bing.com"
Restart=on-abort
[Install]
WantedBy=multi-user.target
```
> 这里是‘配置文件+命令 ‘的组合来启动’，此处一定要注意 ss-local 所在的路径

- 使服务生效并添加到系统运行服务中
```bash
systemctl daemon-reload
systemctl enable ss
systemctl start ss
```
完成 ！

> 在深度 Deepin 系统中 ss-local 文件默认安装在 /usr/local/bin/