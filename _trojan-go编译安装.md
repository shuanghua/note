### 安装

```bash
git clone https://github.com/p4gefau1t/trojan-go.git
```

```bash
cd trojan-go
```

```bash
go get -t .
```

```bash
CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -tags "full"
```

### 配置

- trojan-go配置
  
  ```json
  {
    "run_type": "server",
    "local_addr": "0.0.0.0",
    "local_port": 443,
    "remote_addr": "127.0.0.1",
    "remote_port": 80,
    "log_level": 2,
    "udp_timeout": 60,
    "password": [
        "password"
    ],
    "ssl": {
        "cert": "/root/fullchain.cer",
        "key": "/root/yourdomain.com.key",
        "fallback_port": 1234,
        "fallback_addr": "yourdomain.com"
    },
    "websocket": {
    "enabled": true,
    "path": "/home/www/yourdomain.com",
    "host": "yourdomain.com"
    }
  }
  ```

- Caddy配置
  
  ```
  127.0.0.1:80 {
    root * /var/www/html/index.html
    file_server
  }
  yourdomain.com:1234 {
   root * /var/www/html/error.html
   file_server
   tls /root/fullchain.cer /root/yourdomain.com.key
  }
  ```

```
上面的配置必须是对应的 "remote_addr": "127.0.0.1",  "remote_port": 80 对应 Caddyfile里面的 127.0.0.1:80
```

```
127.0.0.1:80 {
 root * /var/www/html/index.html
 file_server
}
```

```
"fallback_port": 1234 和 "fallback_addr": "yourdomain.com" 对应 yourdomain.com:1234，同时注意 fallback_addr 的域名尽量上 tls
```

```
yourdomain.com:1234 {
 root * /var/www/html/error.html
 file_server
 tls /root/fullchain.cer /root/yourdomain.com.key
}
```