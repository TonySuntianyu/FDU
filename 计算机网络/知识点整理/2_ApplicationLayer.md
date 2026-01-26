# 应用层

## 1. 应用层协议原理

principles of network applications  

### 1.1. 架构

#### 1.1.1. C/S Architecture(客户端-服务器架构)  

Paradigm（范式）:  

- Server：  
    - 永久在线主机  
    - 固定IP  
    - 通常在数据中心（支持拓展）  
- Client：  
    - 之间一般不直接通信  
    - 通过请求服务器获取资源  

应用：  

- HTTP  
- IMAP  
- FTP  
- DNS  

#### 1.1.2. P2P Architecture(点对点架构)

对等体系结构  

- 没有永久在线服务器  
- 任意端系统之间可直接通信  

核心优势：  
**自扩展性**  
新加入的对等方带来服务需求，同时也带来服务能力  

缺点：  
管理较为复杂  
对等方一般间接性连接且IP地址动态变化  

### 1.2. 进程通信与套接字（Socket）

网络应用程序本质：  
在不同主机的进程通过交换报文进行通信  

- C/S Process  
    发起通信的进程：Client（客户端进程）  
    响应通信的进程：Server（服务器进程）  
- Socket Interface（套接字接口）  
    进程通过套接字发送或接收报文  
    Socket：“门”  
    发送方将报文推出门  
    依赖下层设施将其交付到目的地的套接字  
- 进程寻址（Process Addressing）  
    进程必须拥有唯一标识符  
    IP地址 + 端口号 = 套接字地址  
    port e.g.  
    - HTTP: 80
    - SMTP: 25(邮件服务器)

### 1.3. 应用层协议

规定了不同端系统上的进程如何相互交换报文  

四要素：  

- 报文类型  
    例子（Request, Response）  
    - 请求报文：由客户端发起，包含请求的资源和操作  
    - 响应报文：由服务器发起，包含请求的资源和状态信息
- 报文语法（Message Syntax）  
    报文中的各个字段  
    以及其详细描述  
- 字段语义（Message Semantics）  
    字段中包含的信息的具体含义  
- 规则  
    确定进程何时、如何发送和响应报文  

协议分类：  

- 公开协议  
    定义在RTC（request for comments，请求评议）中，以便互操作  
    - HTTP（hypertext transfer protocol，超文本传输协议）  
    - SMTP（simple mail transfer protocol，简单邮件传输协议）  
- 专用协议  
    - Skype协议：用于Skype应用的专有协议  
    - 等等

### 1.4. 应用程序对传输层服务的需求

主要四个维度：  

- 数据可靠性  
    文件传输和Web应用
- 吞吐量  
    多媒体应用需求一定的最小带宽保证  
- 定时（延迟）
    交互式应用
- 安全性  
    设计机密性、完整性等需求  

### 1.5. 传输层协议的选择

Internet主要提供两种：  

- TCP（Transmission Control Protocol，传输控制协议）  
    提供面向连接、可靠、流量控制和拥塞控制的服务  
- UDP（User Datagram Protocol，用户数据报协议）
    提供无连接、不可靠的数据传输，不保证按序交付  

现有的TCP和UDP均不提供时延或带宽保证  
原生TCP不包含加密，安全应用通常在应用层通过TLS (传输层安全性) 库来实现加密连接  

## 2. Web与HTTP

Web  
以 **超文本传输协议（HTTP）** 核心  
的C/S应用架构  

### 2.1. Web Page与URL

Web Page组成：  
多个对象，HTML文件、图像等  

URL（Uniform Resource Locator，统一资源定位符）  
通过URL进行寻址  
包含：  
Hostname和Pathname  

例子：  

```txt
www.example.com/path/to/resource.html
```

### 2.2. HTTP基础

- 服务模型：
    C/S模型  
    客户端发起请求，服务器响应请求  
- 传输层支持：  
    TCP  
    默认端口80  
- 无状态性（Stateless）：  
    目的在于简化服务器设计  
    服务器不维护关于客户过去请求的任何信息  
    增加了维持用户状态的复杂性（如Cookies）  

### 2.3. HTTP连接类型与性能

Non-persistent HTTP  
非持续性连接  

- 每个TCP连接最多发送一个对象，然后关闭  
- 响应时间计算  
    **2 RTT + 文件传输时间**

RTT：  
往返时间（Round Trip Time）  
指从发送方发送一个数据包到接收方并收到确认所需的时间  

Persistent HTTP  
HTTP1.1  
持续连接  

- 服务器在发送响应后保持TCP连接开启  
    后续请求可通过同一连接发送  
- 可以显著减少TCP握手开销  

### 2.4. 报文格式与状态码

#### 2.4.1. 报文格式（Message Format）  

请求报文：  
ASCII编写  

包含：  

- request line（方法，URL，版本）  
    请求行由三部分组成：
    - 方法（HTTP动词）  
        - GET 获取  
        - POST 提交  
        - HEAD 进获取头部
        - PUT 上传
    - URL  
        - 请求的资源路径
    - 版本（HTTP版本）  
        - 例如：HTTP/1.1
- header lines（头部字段）  
    提供额外信息，例如：  
    - Host（目标主机）  
    - User-Agent（浏览器类型）
    - Connection:close（告知服务器处理完即关闭连接）
- 实体主体（Body）  
    包含请求或响应的具体内容，例如：HTML文档、图像等  

响应报文：  

- 起始行：  
    e.g. `HTTP/1.1 200 OK`  
    - HTTP版本  
    - 状态码  
    - 状态短语  
- header lines  
    - Data
    - Server
    - Content-Type  
    - 用于持久连接或Cookie的首部  
    - 等
- 主体  
    被请求的数据对象等  

#### 2.4.2. HTTP状态码

三位整数，五类

- 1xx（信息行）  
    请求已收到，正在处理  
- 2xx（成功）  
    操作被成功接受和处理  
    例如`200 OK`  
- 3xx（重定向）  
    需要进一步操作以完成请求  
    - 301 Moved Permanently  
        对象已永久移动，新URL在Location首部中给出  
    - 304 Not Modified  
        client发送了GET  
        服务器对象未更新则返回此代码（节省带宽）
- 4xx（客户端错误）  
    - 400 Bad Request  
        Server无法理解报文  
    - 404 Not Found  
        请求的文档在Server上不存在
- 5xx（服务器错误）
    - 505 HTTP Version Not Supported  

### 2.5. 状态管理

HTTP协议是无状态的  
服务器不跟踪客户端状态  

重要工具：Cookie技术  

#### 2.5.1. Cookie组成

四个组成部分：  

1. 响应首部行  
    HTTP Response Message中`set-cookie`字段  
2. 请求首部行  
    Client后续Request Message中包含`cookie`字段  
3. 本地文件  
    Client浏览器管理并存储Cookie
4. 后端数据库  
    Web站点后台存储用户信息的数据库  

#### 2.5.2. 原理及流程

1. 初次访问：  
    Client发送请求，Server响应并设置Cookie  
    生成一个唯一识别码（ID）  
2. 设置Cookie  
    相应报文中包含`set-cookie:ID`  
3. 本地存储  
    在本地存储Cookie  
    记录：主机名和ID  
4. 识别用户  
    后续请求中，Client发送Cookie  
    Server通过ID识别用户  

#### 2.5.3. 应用场景

1. 身份验证  
2. 购物车  
3. 个性化推荐
4. 会话状态管理

#### 2.5.4. 问题

第一方cookie：  
由访问的网站设置的cookie  

第三方cookie：  
由其他网站（如广告商）设置的cookie  

因此保护：  
目前许多浏览器默认阻止第三方cookie  

### 2.6. Web Cache

Web缓存  
代理服务器技术  

在不访问源服务器的情况下，满足客户端请求  

#### 2.6.1. 代理服务器

双重性：  
既是客户端，又是服务器  

- 作为客户端  
    接收客户端请求  
    向源服务器发送请求  
- 作为服务器
    接收源服务器响应  
    向客户端发送响应

#### 2.6.2. 请求处理

浏览器被配置为指向Web缓存  
请求发生  

1. 缓存检查本地是否存有请求对象  
2. 若有且未过期，直接返回对象  
3. 否则，向源服务器请求对象  
4. 将响应对象存入缓存  
5. 将对象返回给客户端  

#### 2.6.3. 优势

- 减少响应延迟  
- 减少带宽负载  
- 分担服务器压力  

#### 2.6.4. 一致性维护：条件GET

HTTP引入Conditional GET机制  
防止缓存向用户提供过期对象  

- 验证过程：  
    Request报文中包含条件字段  
    - `If-Modified-Since: <date>`  
        data: 本地缓存副本的最后修改时间  
- 服务器响应：  
    - 若对象未修改，返回`304 Not Modified`  
        客户端继续使用本地缓存副本  
        正文为空  
    - 若对象已修改，返回`200 OK`和新对象  
- 控制机制：  
    `Cache-Control: max-age`  
    或 `Expires`首部字段  
    告知缓存对象的有效期  

### 2.7. HTTP演进

#### 2.7.1. HTTP/2

引入二进制分帧（Frame）和流（Stream）概念  
支持多路复用（解决HTTP/1.1的HOL Blocking问题）
以及服务器推送（Server Push）  

- Frame  
    二进制格式的报文片段  
    包含头部和数据部分  
    最小传输单位  
- Stream  
    一条独立的双向通信通道  
    可以在同一TCP连接上并行传输多个流  
- 多路复用  
    允许多个请求和响应在同一连接上并行传输  
    减少延迟和资源消耗  
- HOL Blocking（Head-of-Line Blocking，队头阻塞）  
    在HTTP/1.1中，如果一个请求被阻塞，后续请求也会被阻塞  
    HTTP/2通过多路复用解决了这个问题  
- 服务器推送  
    服务器可以主动向客户端推送资源  
    而不需要客户端明确请求  
    例如，当请求HTML时，服务器可以同时推送相关的CSS和JavaScript文件

#### 2.7.2. HTTP/3

QUIC协议基础  
彻底抛弃了TCP  

- QUIC（Quick UDP Internet Connections）  
    基于UDP的传输协议  
    将连接建立和加密集成在一起（仅用1 RTT完成握手）  
    为每个流提供独立的可靠传输（单个流的丢包不会影响其他流）  
    （解决了底层TCP丢包重传引起的阻塞）  

## 3. FTP

File Transfer Protocol（文件传输协议）  

C/S Architecture  

### 3.1. 机制

双重连接机制（Two Connection Mechanism）  

- 控制连接（Control Connection）  
    用于传输控制信息  
    客户端连接到服务器的端口21  
    持续存在，直到会话结束  
- 数据连接（Data Connection）  
    用于传输文件数据  
    每次传输文件时建立新的连接  
    服务器端口20用于数据传输  

### 3.2. 状态维护  

FTP服务器维护用户状态信息  
（当前目录、身份验证状态等）  

### 3.3. 数据连接建立模式

- 主动模式（Active Mode）
    服务器主动向客户端发起数据连接  
    容易被防火墙阻止  
    客户端通过其临时端口连接服务器的 21 端口建立控制连接。当需要传输文件时，客户端通过 PORT 指令告诉服务器自己的数据接收地址，随后服务器从其 20 端口主动发起向客户端的 TCP 连接  
- 被动模式（PASV）  
    客户端向服务器发起两个连接  
    客户端在控制连接中发送 PASV 指令，服务器随后开启一个随机的非特权端口 (P > 1023) 并告知客户端。客户端再从本地另一个端口主动连接服务器提供的端口 P 来建立数据通道  

### 3.4. 常用指令

- USER  
    提供用户名进行身份验证  
- PASS  
    提供密码进行身份验证  
- LIST  
    列出当前目录的文件和子目录  
- RETR（Retrieve）  
    从服务器下载文件到客户端  
- STOR（Store）
    将文件从客户端上传到服务器  

## 4. DNS

Domain Name System（域名系统）  
主要在应用层实现  

### 4.1. 基本原理

定义：  
DNS是一个由分层结构的域名服务器实现的分布式数据库系统  
也是一个允许主机查询域名信息的应用层协议  

### 4.2. 核心功能

主要服务：  

- 主机名到IP地址的映射（正向解析）  
    域名（人类易读）  
    转换为32位IP地址（机器易读）  
- 主机/邮件服务器别名解析  
    通过CNAME记录提供规范主机名对应的别名  
    通过MX记录为邮件服务器提供别名  
- 负载均衡  
    大型网站可能对应多个IP  
    DNS可以在IP之间分配负载  

不中心化的原因：  
避免单点故障，处理海量流量，解决远距离时延问题，简化维护  

### 4.3. 分层层次结构

DNS采用分布式的分层结构  
查询通常按以下顺序进行：  

- 根域名服务器（Root Name Servers）  
    全球共13个逻辑节点，解析器找不到匹配记录时的最终求助对象  
- 顶级域名服务器（Top-Level Domain, TLD Servers）  
    管理顶级域名（如.com, .org, .net等）的服务器  
- 权威域名服务器（Authoritative Name Servers）  
    负责特定域名的DNS记录  
    由组织机构自身维护，提供其所有主机的权威主机名-IP映射  
- 本地域名服务器（Local Name Servers）  
    由ISP提供，负责为本地用户解析域名请求  
    通常缓存查询结果以提高性能  
    主机发起查询的第一站  
    不严格属于DNS层次结构，但在查询过程中起关键加速作用  

### 4.4. 查询与解析机制

1. 迭代查询（Iterative Query）  

2. 递归查询（Recursive Query）

3. DNS 缓存（DNS Caching）  

#### 4.4.1. 迭代查询

服务器如果不知道映射关系  
返回下一级需要联系的服务器地址给客户端  

#### 4.4.2. 递归查询

被查询的服务器承担起解析域名的全部责任  
向上层层查询直到获取结果并返回给客户端  

#### 4.4.3. DNS缓存

服务器在获取映射后存在本地缓存中  
TTL（Time to Live，生存时间）过期后剔除  

### 4.5. 资源记录

DNS分布式数据库存储的条目  
Resource Records（RRs）  

格式：  
`(Name, Value, Type, TTL)`  

Type:  

- A（A记录）  
    主机名到IPv4地址的映射  
    Name: 主机名  
    Value: 32位IPv4地址  
- AAAA（AAAA记录）  
    主机名到IPv6地址的映射
- NS（Name Server记录）  
    指定域名的权威域名服务器  
    Name: 域名  
    Value: 权威域名服务器的主机名  
- CNAME（Canonical Name记录）  
    Name：别名  
    Value：规范主机名  
- MX（Mail Exchange记录）  
    指定邮件服务器  
    Name: 域名  
    Value: 邮件服务器的主机名  

### 4.6. 安全挑战

DDoS攻击（Distributed Denial of Service，分布式拒绝服务攻击）  
通过大量虚假请求淹没DNS服务器，导致合法请求无法得到响应  

欺诈攻击（DNS Spoofing）  
例如缓存投毒（Cache Poisoning）  
攻击者向DNS缓存注入伪造的记录，导致用户被重定向到恶意网站  

## 5. P2P

Peer-to-Peer（点对点）网络  
去中心化，分布式网络架构  
实际上包含了许多的不同协议和应用  

### 5.1. 基本原理

没有永久在线的服务器  
任意端系统（对等方）可以直接通信  

### 5.2. 核心优势

自扩展性（Self-Scalability）  

P2P文件分发中  
新加入的对等方带来服务需求，同时也带来服务能力  
使得系统能力随与需求同步增长  

### 5.3. 搜索与定位机制

- 中心化目录（Centralized Directory）  
    早期  
    通过中心化服务器维护资源索引  
    对等方向服务器注册资源  
    查询时向服务器请求资源位置  
    存在许多问题
- 非结构化P2P：查询泛洪（Query Flooding）  
    以Gnutella为代表，完全去中心化  
- 结构化P2P：分布式哈希表（DHT）  
    通过特定的图结构实现更高效的资源定位  
- 混合模式：例如BitTorrent  
    结合中心化和去中心化的优势  

#### 5.3.1. 查询泛洪（Query Flooding）

泛洪过程：  
节点寻找对象时，向所有邻居发送`QUERY`报文  
邻居再继续向其邻居转发报文  

控制机制：  
防止无限循环，Message中包含TTL字段  
以及唯一的查询标识符（QID）  

响应：  
如果节点拥有请求对象，沿着反向路径发送`QueryHit`报文给发起节点  

#### 5.3.2. 分布式哈希表（DHT）

一致性哈希（Consistent Hashing）：  
将节点和对象（文件名）通过Hash函数映射到  
同一个巨大的标识符（ID）空间  
（例如128位或160位）  

映射规则：  
对象通常存储在ID与其最接近的节点上  

路由机制：  
请求通过ID空间进行路由  
Pastry中，通过前缀配进行路由  

### 5.4. 优势分析示例：文件分发效率

File Distribution Efficiency  
衡量效率的核心指标：**最小分发时间 (D)**  
所有 N 个接收方都获得文件副本所需的最短时间  

#### 5.4.1. C/S 模式

服务端瓶颈：Server的上传带宽 $u_s$  
客户端瓶颈：Client下载带宽 $d_{min}$  

最小分发时间公式：  

$$D_{C/S} = max\left\{\frac{NF}{u_s}, \frac{F}{d_{min}}\right\}$$

#### 5.4.2. P2P 模式

服务器端限制：  
服务器至少需要上传一份完整文件  
耗时 $\frac{F}{u_s}$  

客户端限制：  
下载速率最低用户耗时 $\frac{F}{d_{min}}$  

整体系统限制：  
上行带宽由服务器 $u_s$ 和所有对等方的上行带宽之和 $\sum_{i=1}^{N} u_{i}$ 共同决定  
发送 $NF$ bit数据至少耗时 $\frac{NF}{u_s + \sum_{i=1}^{N} u_{i}}$  

最小分发时间公式：  

$$
D_{P2P} = max\left\{\frac{NF}{u_s + \sum_{i=1}^{N} u_{i}}, \frac{F}{d_{min}}\right\}
$$

## 6. 视频流与DASH协议

解决大规模视频分发、适应网络波动的核心方案

### 6.1. 视频编码

#### 6.1.1. 编码原理

视频由一系列静态图像（帧）组成  
利用 **空间冗余** （帧内像素重复）  
和 **时间冗余** （帧间相似性）  
进行压缩编码  

#### 6.1.2. 比特率类型

- CBR（Constant Bit Rate，恒定比特率）  
    以固定比特率编码视频  
    简单但不适应网络波动  
- VBR（Variable Bit Rate，可变比特率）  
    根据视频内容复杂度动态调整比特率  
    提供更高质量但更复杂  

#### 6.1.3. 核心挑战

由于网络带宽随时间波动  
且有路径拥塞问题  
直接传输原始流会导致画面卡顿或播放延迟  

### 6.2. DASH协议

Dynamic Adaptive Streaming over HTTP（动态自适应HTTP流）  
将主动权交给客户端  
使其能根据网络状况自主调节  

#### 6.2.1. 服务端处理

- 块（Chunks）  
    将视频切分为多个短小的块  
- 每个块以不同速率（质量级别）进行预编码并存储  
- Manifest File（告示文件）  
    提供告示文件  
    列出所有的视频块及其对应的不同速率版本的URL地址  

#### 6.2.2. 客户端逻辑

- 网络监测  
    定期测量当前可用带宽和缓冲状态  
- 根据带宽估算值从Manifest File中选择质量最佳的块  
    通过HTTP GET请求下载  
- 自适应平衡  
    带宽充足时请求高画质块  
    拥塞时换成低比特率版本，优先保证流畅播放  

### 6.3. 播放缓冲机制

应对Jitter（网络抖动）和突发延迟的关键技术  
客户端建立 播放缓冲区（Buffer）  

接收速率能通过DASH动态维持在平均水平  
Buffer就能通过提前缓存视频块来平滑微小的延迟波动  
保证视频画面连续且流畅  

## 7. 内容分发网络（CDN）

Content Delivery Network（内容分发网络）  
地理上分布的节点存储内容副本  
旨在代表源服务器响应 HTTP 请求  
解决单台巨服务器的问题（单点故障、带宽瓶颈、高延迟）  

### 7.1. 部署策略

CDN提供商通常采用两种部署策略：  

- 深入（Enter Deep）  
    在大量接入网（Access Networks）内部署服务器，极度靠近终端用户，以提供极高性能，如 Akamai

- 带回（Bring Home）  
    在较少（约数十个）的大型网络节点（POPs）部署更大规模的集群，维护成本更低，如 Limelight  

### 7.2. 工作流程与重定向机制

用户请求内容时，CDN通过“重定向器”将请求转发至最佳节点  

- DNS重定向（DNS-based Redirection）  
    通过修改DNS响应，将用户请求指向最近的CDN节点  
    用户查询域名时，源站 DNS 会返回一个别名记录（CNAME），引导用户访问 CDN 的 DNS 服务器。CDN 随后根据用户的 IP 地址返回离其最近或负载最低的节点 IP，并设置极短的 TTL（如 20秒）以便快速动态调度  
- URL重写（URL Rewriting）  
    源服务器在返回的 HTML 中修改链接，使其直接指向特定的 CDN 节点  
- HTTP重定向（HTTP Redirection）  
    服务器返回 `302` 状态码，指示客户端向指定的 CDN 节点发起请求  

### 7.3. 节点选择算法：一致性哈希（Consistent Hashing）

- 原理：  
    将URL与服务器IP都映射到一个巨大的哈希环（ID空间）上  
- 优势：  
    同一URL总是趋向于映射到同一台服务器（局部性）  
    某台服务器上线或下线时，只有一小部分URL的归属会改变，减少缓存失效带来的影响  
