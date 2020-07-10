# Ptm Source code process analysis

## 1项目结构

├── argon2 hash函数库,类似的函数还有pbkdf2、bcrypt、 scrypt
├── cli 使用picocli实现的命令行tessera（包含子命令keygen|keyupdate|admin）
├── config 配置数据模型,给各个模块使用的配置
├── config-migration 提供命令行可以将Constellation TOML转换成Tessera JSON
├── data-migration 创建表结构
├── ddls ddl语句,包含两张表(支持mysq、oracle、postgresql、h2、hsql、sqllite)
├── enclave 提供加密、解密接口/Restful api(调用encryption模块)
├── encryption 生成主密钥、共享密钥、加密解密payload、生成随机数等.实现有ec、jnacl、kalium三种
├── key-generation 公钥私钥生成,包含aws、azure、hashcorp三种实现
├── key-vault 密钥保险箱 ,有aws、azure、hashcorp三种实现,可将密钥保存在这些在线服务上
├── security ssl通信相关工具
├── server 包含两个TesseraServer的实现:1、使用Jersey和Jetty 实现的RestServer;2 WebSocketServer
├── service-locator 获取服务实例,默认使用spring 配置文件 tessera-spring.xml中的定义(不使用注解?)
├── shared 大杂烩,包含一些工具类:控制台密码读取、ReflectCallback、JaxbCallback等CallBack
├── tessera-context 上下文,见下面的RuntimeContext
├── tessera-core 主要包含TransactionManager
├── tessera-data 主要包含EncryptedTransactionDAO、EncryptedRawTransactionDAO的实现
├── tessera-dist 系统launcher入口,包含tessera-spring.xml配置文件
├── tessera-jaxrs 系统RESTful API(OpenAPI)定义
├── tessera-partyinfo 参与者之间的服务发现、p2p连接、推送EncodedPayload到其他节点
├── tessera-sync peer节点之间Transaction的同步
├── test-utils 测试mock工具
└── tests 测试用例

## 2.数据库结构

```
CREATE TABLE ENCRYPTED_TRANSACTION (
ENCODED_PAYLOAD BLOB NOT NULL, 
HASH VARBINARY(100) NOT NULL, 
TIMESTAMP BIGINT, PRIMARY KEY (HASH)
);

CREATE TABLE ENCRYPTED_RAW_TRANSACTION (
ENCRYPTED_KEY BLOB NOT NULL,
ENCRYPTED_PAYLOAD BLOB NOT NULL,
NONCE BLOB NOT NULL,
SENDER BLOB NOT NULL, 
TIMESTAMP BIGINT, 
HASH VARBINARY(100) NOT NULL, PRIMARY KEY (HASH)
);
```

## 3服务启动流程

a. 首先通过cli从配置文件config.json读取配置，根据配置创建运行时上下文（上下文持有当前节点公私钥对，peers列表等引用）
b. 再将当前partyInfo保存到集合中(内存)
c. 根据的serverConfigs循环创建ThirdPartyRestApp、P2PRestApp、Q2TRestApp(未包含EnclaveApplication)Restful服务
d 启动服务监听
![å¨è¿éæå¥å¾çæè¿°](https://img-blog.csdnimg.cn/20200328235721310.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2trMzkwOQ==,size_16,color_FFFFFF,t_70)

## 4交易流程

a.收到交易请求后，将请求交给TransactionManager处理，TransactionManager调用Enclave加密tx(详见下一个流程【加密交易】)，根据加密的payload，调用MessageHashFactory生成tx Hash，
b. 调用DAO将数据保存到数据库
c. 循环接收者列表，将加密了的playload推送给其他Tessera节点处理
d.将tx hash使用base64编码后返回给quorum几点
![å¨è¿éæå¥å¾çæè¿°](https://img-blog.csdnimg.cn/20200329131900327.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2trMzkwOQ==,size_16,color_FFFFFF,t_70)

## 5加密交易

a. 生成随机主密钥（RMK：NonceMasterKey）和随机数Nonce、接收者随机数Nonce
b.使用步骤a的随机数Nonce和RMK加密message(Transaction Payload)。
c. 根据发送者的公钥从keymanager中获取发送者私钥
d.遍历接收者列表：根据发送者的私钥和接收者的公钥生成共享秘钥，根据共享密钥和接收者随机数加密RMK，最后返回RMK列表
e.返回加密的playload、随机数、RMKs给Transaction Manager
![å¨è¿éæå¥å¾çæè¿°](https://img-blog.csdnimg.cn/20200329141827544.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2trMzkwOQ==,size_16,color_FFFFFF,t_70)