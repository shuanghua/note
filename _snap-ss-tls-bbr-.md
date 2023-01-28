# Debain 10

## 更新系统

```bash
apt update
```

## 安装 ufw

```bash
apt install ufw
```

```bash
ufw allow OpenSSH                         //让 ufw 允许 ssh 连接，否则后续连接不上 vps （如果连不上了，请通过vps提供商后台修改端口）
```

#### 如果SSH服务器 ssh 登录的端口不是 22 ，则还必须需要打开该端口。

- 例如，您的 ssh 服务器监听端口为 7722，您还需要执行：

```bash
ufw allow 7722
```

## 启用 ufw [一定要开放了 ssh 端口之后再启用]

```bash
ufw enable      //输入 y 回车完成启用
```

#### 开放 443 或 80

- 443

```bash
ufw allow https
```

或者

```bash
ufw allow 443/tcp
ufw allow 443/udp
```

- 80 

```bash
ufw allow http
```

或者

```bash
ufw allow 80/tcp
ufw allow 80/udp
```

## 安装 snap

```bash
apt install snapd -y
snap install core
```

## 通过snap仓库安装 ss-rust （snap会自动更新 ss）

```bash
snap install shadowsocks-rust            //稳定版
```

```bash
snap install shadowsocks-libev --edge     //开发版
```

## 安装 golang 方法1

```bash
snap install go --channel=latest/stable --classic
reboot                                        // 重启 vps 让 golang 环境变量生效
go version                                    //测试 golang 是否可全局启用
```

## 安装 golang 方法2

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

## 安装 git

```bash
sudo apt install git-all -y
```

## 安装 v2ray-plugin

```bash
git clone https://github.com/shadowsocks/v2ray-plugin.git
cd v2ray-plugin
go mod download
go build
cd ..
```

## 申请 https 证书(申请前需先把 dns 和 vps 的地址解析好)

```bash
curl https://get.acme.sh | sh
~/.acme.sh/acme.sh --register-account -m shhua17@gmail.com
export CF_Email=""
export CF_Key=""
~/.acme.sh/acme.sh --issue --dns dns_cf -d samstring.com --force
```

## ss 配置文件

```bash
vi /root/ss-config.json
```

```bash
{
  "server": "0.0.0.0",
  "nameserver": "8.8.8.8",
  "server_port": 10244,
  "password": "你的密码",
  "method": "chacha20-ietf-poly1305",
  "timeout": 400,
  "mode": "tcp_and_udp",
  "plugin": "/root/v2ray-plugin/v2ray-plugin",
  "plugin_opts": "server;tls;fast-open;host=kotlintest.top;loglevel=none"
}
```

## 开机自启

```bash
vi /etc/systemd/system/ss.service
```

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

## 启动 ss

```bash
systemctl daemon-reload
systemctl enable ss.service
systemctl start ss.service
systemctl status ss.service
```

## 开启bbr(tcp拥塞控制算法)

- 检查当前已经启用的算法
  
  ```bash
  sysctl net.ipv4.tcp_available_congestion_control
  ```
  
  > net.ipv4.tcp_available_congestion_control = reno cubic

- 没有bbr就启用bbr
  
  ```bash
  echo "net.core.default_qdisc=fq" >> /etc/sysctl.conf
  echo "net.ipv4.tcp_congestion_control=bbr" >> /etc/sysctl.conf
  sysctl -p
  ```

- 再次检查
  
  ```bash
  sysctl net.ipv4.tcp_available_congestion_control
  ```

> net.ipv4.tcp_available_congestion_control = reno cubic bbr

- 确定bbs已经在运行
  
  ```bash
  lsmod | grep bbr
  ```
  
  > tcp_bbr                20480  1

## 其他一些命令

```bash
ufw status numbered   // 查看端口对应的编号
ufw delete 3          // 利用编号删除对应的端口
ufw disable
ufw enable
ufw reset
```
