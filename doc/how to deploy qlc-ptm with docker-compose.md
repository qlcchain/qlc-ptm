# how to deploy qlc-ptm with docker-compose

部署方式 （linux）

建立工作目录，eg: ptmnode

```
mkdir ptmnode
cd ptmnode
```

checkout db.env  docker-compose.yaml 到工作目录

建立db和ptm工作目录

```
mkdir db
mkdir ptm
```

得到工作目录如下

```
~/ptmnode$ tree 
.
├── db
├── db.env
├── docker-compose.yaml
└── ptm
```

在工作目录下，执行

```
docker-compose up
```

然后会自动拉取镜像

qlcchain/ptm:1.0.0

postgres:alpine 

并自动生成ptm默认配置文件，启动镜像

ptm目录下有对应生成的默认配置文件

```
~/ptmnode$ tree 
.
├── db           // psql 存储目录
├── db.env
├── docker-compose.yaml
└── ptm
    ├── config.json     //ptm节点配置文件
    └── target
        └── h2
            ├── tessera1.lock.db
            ├── tessera1.mv.db
            └── tessera1.trace.db
```

然后用户需要根据自己环境配置修改ptm config.json文件

默认ptm启动方式为l2db方式

example：

本地环境需要使用postgresql存储，本地ip为172.19.15.147，实际环境中另一个ptm节点在172.19.15.148

方法1，修改ptm/config.json

```
 默认数据库配置
 "jdbc": {
    "username": "qlcchain",
    "password": "",
    "url": "jdbc:h2:/ptm/target/h2/tessera1;AUTO_SERVER=TRUE;MODE=Oracle;TRACE_LEVEL_SYSTEM_OUT=0",
    "autoCreateTables": true
  },
  修改为postgresql配置
  "jdbc": {
        "username": "dbuser",
        "password": "dbpasswd",
        "url": "jdbc:postgresql://db:5432/db",
        "autoCreateTables": true
    },
    
   默认p2p节点配置
       {
      "app": "P2P",
      "enabled": true,
      "serverAddress": "http://127.0.0.1:9183",
      "bindingAddress": "http://0.0.0.0:9183",
      "sslConfig": {
        "tls": "OFF"
      },
      "communicationType": "REST"
    }
  ],
  "peer": [
    {
      "url": "http://localhost:9183"
    }
  ],
  
  修改为本地ip
	{
            "app": "P2P",
            "enabled": true,
            "serverAddress": "http://172.19.15.147:9183",
            "bindingAddress": "http://0.0.0.0:9183",
            "sslConfig": {
                "tls": "OFF"
            },
            "communicationType": "REST"
        }

    ],

    "peer": [
        {
            "url":"http://172.19.15.147:9183",
            "url":"http://172.19.15.148:9183"
        }
    ],
```

方法2，修改docker-compose.yaml,利用命令行参数重导写入本地化配置

```
默认配置
entrypoint: java -cp /usr/share/java/postgresql-jdbc.jar:/tessera/tessera-app.jar:. com.quorum.tessera.launcher.Main -configfile /ptm/config.json
修改增加override参数
entrypoint: java -cp /usr/share/java/postgresql-jdbc.jar:/tessera/tessera-app.jar:. com.quorum.tessera.launcher.Main -configfile /ptm/config.json -o jdbc.username=dbuser -o jdbc.password=dbpasswd -o jdbc.url=jdbc:postgresql://db:5432/db -o serverConfigs[2].serverAddress=http://172.19.15.147:9183 --override peer[0].url=http://172.19.15.148:9183
```

然后重启docker，这时ptm节点按照用户自己实际环境正常运行起来了

```
docker ps
CONTAINER ID        IMAGE                         COMMAND                  CREATED             STATUS              PORTS                                                                                         NAMES
6882fa102ae4        qlcchain/ptm:1.0.0            "java -cp /usr/share…"   6 minutes ago       Up 2 seconds        0.0.0.0:9182-9183->9182-9183/tcp                                                              ptm
a44425b83f67        postgres:alpine               "docker-entrypoint.s…"   6 minutes ago       Up 2 seconds        0.0.0.0:5432->5432/tcp                                                                        postgres
```

