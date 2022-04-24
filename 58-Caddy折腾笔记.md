---
toc: false
comments: false
title: 折腾 Caddy 反代
description: 折腾 Caddy 反代的过程笔记
tags:
  - Linux
id: 58
categories:
  - Linux
date: 2019-5-2
---



# Caddy
现在 [Caddy 下载官网](https://caddyserver.com/download.html) 上配置你需要的插件,如果你原来就使用 acme 申请的 cloudflare 的证书,那么需要再添加 cloudflare dns 的插件,目的是为了让 Caddy 能够申请 cloudflare 的证书,然后复制相应的下载地址或安装脚本.

<!-- more -->

> Caddy 2.0 正在开发中, 主要支持 HTTP 3.0 也就是 http2 over quic ,正式对标准 quic 的支持 .

以下内容参考自: [caddy 官方教程 ](https://github.com/caddyserver/caddy/blob/master/dist/init/linux-systemd/README.md#systemd-service-unit-for-caddy)

其实也可以跟着官方教程,复制粘贴也行.



## 安装准备
- 开放 80 或 443 端口(略)
- 域名解析, 指向服务器(要先解析才能申请证书)
- 移除 SS 的 tls
> 由于 tls 已经交由 caddy 完成; 因此 ss 配置文件中的 v2ray-plugin 选项要去掉 tls 和相关证书选项



## 安装 caddy
- cloudflare 版
```bash
curl https://getcaddy.com | bash -s personal tls.dns.cloudflare
```


- cloudflare + dnspod
```
curl https://getcaddy.com | bash -s personal http.cache,tls.dns.cloudflare,tls.dns.dnspod
```


- 普通版
```bash
curl https://getcaddy.com | bash -s personal
```
> cloudflare 版和普通版只一个就行, 这里我安装的是普通版.


## 配置 caddy
- 配置 cddy 权限
```bash
sudo chown root:root /usr/local/bin/caddy
sudo chmod 755 /usr/local/bin/caddy
```

- 让 caddy 以非 root 身份来使用 80 和 443 端口
```
sudo setcap 'cap_net_bind_service=+ep' /usr/local/bin/caddy
```

- 创建 caddy 配置文件目录和证书目录(后面再往里面写内容)
```bash
sudo groupadd -g 33 www-data
sudo useradd \
  -g www-data --no-user-group \
  --home-dir /var/www --no-create-home \
  --shell /usr/sbin/nologin \
  --system --uid 33 www-data
sudo mkdir /etc/caddy
sudo chown -R root:root /etc/caddy
sudo mkdir /etc/ssl/caddy
sudo chown -R root:www-data /etc/ssl/caddy
sudo chmod 0770 /etc/ssl/caddy
```

- 创建存放网站根目录
```bash
sudo mkdir /var/www/xxxxxxx.com
sudo chown www-data:www-data /var/www/xxxxxxx.com
sudo chmod 555 /var/www/xxxxxxx.com
```

- 创建网站目录并新建一个简单网站页面
```bash
echo '<h1> Hello World! </h1>' | sudo tee /var/www/xxxxxxx.com/index.html
sudo chown www-data:www-data /var/www/xxxxxxx.com
sudo chmod 555 /var/www/xxxxxxx.com
```

- 创建 Caddyfile (caddy 配置文件)
```bash
sudo chown root:root /etc/caddy/Caddyfile
sudo chmod 644 /etc/caddy/Caddyfile
sudo vim /etc/caddy/Caddyfile
```
内容如下:
```
http://xxxxxxxxx.com, http://www.xxxxxxxxx.com{
	redir https://xxxxxxxxx.com
}
https://xxxxxxxxx.com {
	root /var/www/xxxxxxxxx.com
	gzip
	index index.html
	tls 你的邮箱
	proxy /ss http://localhost:ss端口 {
		without /ss
		websocket
		header_upstream -Origin
  }
}
```


## caddy 开机自启
- 开机自启文件,这里直接使用官方帮我们写好的,下载并放到 ```/etc/systemd/system/``` 
```bash
wget https://raw.githubusercontent.com/caddyserver/caddy/master/dist/init/linux-systemd/caddy.service
sudo cp caddy.service /etc/systemd/system/
sudo chown root:root /etc/systemd/system/caddy.service
sudo chmod 644 /etc/systemd/system/caddy.service
sudo systemctl daemon-reload
sudo systemctl start caddy.service
```


## 运行 caddy 
- 运行
```bash
systemctl enable caddy
systemctl daemon-reload && systemctl restart caddy
```

- 查看状态(或使用浏览器访问上面的站点)
```bash
systemctl status caddy
```

- 查看日志(当上面状态出现错误时,再用下面命令查看)
```bash
journalctl --boot -u caddy.service
```
