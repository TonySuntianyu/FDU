# 计算机网络 Homework 3 习题详细解析

## 目录
1. [P3-1: HTTP协议判断题](#p3-1-http协议判断题)
2. [P3-2: HTTP GET请求消息分析](#p3-2-http-get请求消息分析)
3. [P3-3: HTTP响应消息分析](#p3-3-http响应消息分析)
4. [P3-4: DNS查找和RTT计算](#p3-4-dns查找和rtt计算)
5. [P3-5: 非持久和持久HTTP时间计算](#p3-5-非持久和持久http时间计算)
6. [P3-6: 并行HTTP下载分析](#p3-6-并行http下载分析)
7. [P3-7: 文件分发时间计算](#p3-7-文件分发时间计算)
8. [P3-8: 客户端-服务器文件分发方案](#p3-8-客户端-服务器文件分发方案)
9. [P3-9: TCP/UDP客户端服务器实验](#p3-9-tcpudp客户端服务器实验)

---

## P3-1: HTTP协议判断题

### 题目内容

**P3-1. True or false?**

a. A user requests a Web page that consists of some text and three images. For this page, the client will send one request message and receive four response messages.

b. Two distinct Web pages (for example, www.mit.edu/research.html and www.mit.edu/students.html) can be sent over the same persistent connection.

c. With nonpersistent connections between browser and origin server, it is possible for a single TCP segment to carry two distinct HTTP request messages.

d. The Date: header in the HTTP response message indicates when the object in the response was last modified.

e. HTTP response messages never have an empty message body.

---

### 题目分析
本题考察对HTTP协议基本概念的理解，包括请求-响应模式、持久连接、TCP段、HTTP头部等。

### 详细解答

#### a. 一个Web页面包含文本和三个图片，客户端会发送一个请求消息并接收四个响应消息。

**答案：False**

**解析：**
- 对于非持久HTTP连接，每个对象（文本+3个图片）都需要单独的TCP连接
- 客户端会发送**4个请求消息**（1个HTML + 3个图片），接收**4个响应消息**
- 即使使用持久HTTP，虽然可以复用连接，但每个对象仍需要单独的HTTP请求-响应对
- 因此，客户端会发送4个请求，接收4个响应

#### b. 两个不同的Web页面可以通过同一个持久连接发送。

**答案：True**

**解析：**
- 持久连接（Persistent Connection）允许在同一个TCP连接上发送多个HTTP请求-响应
- 只要连接保持打开状态，就可以传输多个不同的Web页面
- 这是HTTP/1.1中持久连接的主要优势之一

#### c. 在浏览器和源服务器之间的非持久连接中，单个TCP段可能携带两个不同的HTTP请求消息。

**答案：False**

**解析：**
- 非持久连接中，每个HTTP请求都需要建立新的TCP连接
- 每个TCP连接建立后，发送一个HTTP请求，接收响应后立即关闭连接
- 因此，一个TCP连接只对应一个HTTP请求-响应对
- 单个TCP段不可能包含两个不同的HTTP请求消息

#### d. HTTP响应消息中的Date头部表示响应中对象最后一次修改的时间。

**答案：False**

**解析：**
- **Date头部**：表示服务器生成响应消息的当前时间
- **Last-Modified头部**：才表示对象最后一次修改的时间
- 这两个头部字段的含义不同，不能混淆

#### e. HTTP响应消息的消息体永远不会为空。

**答案：False**

**解析：**
- HTTP响应消息的消息体可以为空
- 例如：HEAD请求的响应、某些状态码（如204 No Content）的响应
- 响应消息体是否为空取决于请求类型和响应状态

---

## P3-2: HTTP GET请求消息分析

### 题目内容

**P3-2.** Consider the following string of ASCII characters that were captured by Wireshark when the browser sent an HTTP GET message (i.e., this is the actual content of an HTTP GET message). The characters `<cr><lf>` are carriage return and line-feed characters (that is, the italized character string `<cr>` in the text below represents the single carriage-return character that was contained at that point in the HTTP header). Answer the following questions, indicating where in the HTTP GET message below you find the answer.

```
GET /cs453/index.html HTTP/1.1<cr><lf>Host: gaia.cs.umass.e
du<cr><lf>User-Agent: Mozilla/5.0 (Windows;U; Windows NT 5.
1; en-US; rv:1.7.2) Gecko/20040804 Netscape/7.2 (ax) <cr><l
f>Accept:ext/xml, application/xml, application/xhtml+xml, t
ext/html;q=0.9, text/plain;q=0.8,image/png,*/*;q=0.5<cr><lf
>Accept-Language: en-us,en;q=0.5<cr><lf>AcceptEncoding: zi
p,deflate<cr><lf>Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q
=0.7<cr><lf>Keep-Alive: 300<cr><lf>Connection:keep-alive<cr
><lf><cr><lf>
```

a. What is the URL of the document requested by the browser?

b. What version of HTTP is the browser running?

c. Does the browser request a non-persistent or a persistent connection?

d. What is the IP address of the host on which the browser is running?

e. What type of browser initiates this message? Why is the browser type needed in an HTTP request message?

---

### 题目分析
本题要求分析一个实际的HTTP GET请求消息，理解HTTP协议的格式和各个字段的含义。

### 详细解答

#### a. 浏览器请求的文档URL是什么？

**答案：** `http://gaia.cs.umass.edu/cs453/index.html`

**解析：**
- 从请求行 `GET /cs453/index.html HTTP/1.1` 中提取路径：`/cs453/index.html`
- 从Host头部 `Host: gaia.cs.umass.edu` 中提取主机名：`gaia.cs.umass.edu`
- 完整URL = 协议 + 主机名 + 路径 = `http://gaia.cs.umass.edu/cs453/index.html`

#### b. 浏览器运行的HTTP版本是什么？

**答案：** HTTP/1.1

**解析：**
- 在请求行中明确标注：`GET /cs453/index.html HTTP/1.1`
- 版本号位于请求行的最后部分

#### c. 浏览器请求的是非持久连接还是持久连接？

**答案：** 持久连接（Persistent Connection）

**解析：**
- 请求头中有：`Connection: keep-alive`
- `keep-alive` 表示请求使用持久连接
- 如果是非持久连接，Connection头部应该是 `close`

#### d. 运行浏览器的主机的IP地址是什么？

**答案：** 无法从HTTP GET消息中直接获取

**解析：**
- HTTP是应用层协议，运行在TCP之上
- HTTP消息本身不包含IP地址信息
- IP地址信息在IP层（网络层）的IP数据报中
- 要获取IP地址，需要查看IP数据报的头部，而不是HTTP消息

#### e. 发起此消息的浏览器类型是什么？为什么HTTP请求消息中需要浏览器类型？

**答案：** 
- **浏览器类型：** Netscape 7.2（基于Mozilla/5.0引擎）
- **位置：** `User-Agent: Mozilla/5.0 (Windows;U; Windows NT 5.1; en-US; rv:1.7.2) Gecko/20040804 Netscape/7.2 (ax)`

**为什么需要浏览器类型：**
1. **内容协商**：服务器可以根据浏览器类型返回不同格式的内容（如HTML版本、支持的媒体类型）
2. **兼容性处理**：服务器可以针对特定浏览器进行优化或修复兼容性问题
3. **统计分析**：服务器可以统计不同浏览器的使用情况
4. **功能检测**：服务器可以判断浏览器是否支持某些功能（如JavaScript、CSS等）

---

## P3-3: HTTP响应消息分析

### 题目内容

**P3-3.** The text below shows the reply sent from the server in response to the HTTP GET message in the question above. Answer the following questions, indicating where in the message below you find the answer.

```
HTTP/1.1 200 OK<cr><lf>Date: Tue, 07 Mar 200812:39:45GMT<cr
><lf>Server: Apache/2.0.52 (Fedora)<cr><lf>Last-Modified: S
at, 10 Dec2005 18:27:46GMT<cr><lf>ETag: "526c3-f22-a88a4c8
0"<cr><lf>AcceptRanges: bytes<cr><lf>Content-Length: 3874<c
r><lf>Keep-Alive: timeout=max=100<cr><lf>Connection: Keep-A
live<cr><lf>Content-Type: text/html; charset=ISO-8859-1<cr>
<lf><cr><lf><!doctype html public "-//w3c//dtd html 4.0tran
sitional//en"><lf><html><lf><head><lf> <meta http-equiv="Co
ntent-Type"content="text/html; charset=iso-8859-1"><lf> <me
taname="GENERATOR" content="Mozilla/4.79 [en] (Windows NT5.
0; U) Netscape]"><lf> <title>CMPSCI 453 / 591 / NTU-ST550AS
pring 2005 homepage</title><lf></head><lf><much more docume
nt text following here (not shown)>
```

a. Was the server able to successfully find the document or not? What time was the document reply provided?

b. When was the document last modified?

c. How many bytes are there in the document being returned?

d. What are the first 5 bytes of the document being returned? Did the server agree to a persistent connection?

---

### 题目分析
本题要求分析HTTP响应消息，理解状态码、头部字段的含义。

### 详细解答

#### a. 服务器是否成功找到文档？文档回复是什么时间提供的？

**答案：**
- **是否成功：** 是，成功找到文档
- **回复时间：** 2008年3月7日，星期二，12:39:45 GMT

**解析：**
- 状态行：`HTTP/1.1 200 OK`，状态码200表示请求成功
- Date头部：`Date: Tue, 07 Mar 2008 12:39:45 GMT`

#### b. 文档最后一次修改是什么时间？

**答案：** 2005年12月10日，星期六，18:27:46 GMT

**解析：**
- Last-Modified头部：`Last-Modified: Sat, 10 Dec 2005 18:27:46 GMT`

#### c. 返回的文档有多少字节？

**答案：** 3874字节

**解析：**
- Content-Length头部：`Content-Length: 3874`

#### d. 返回文档的前5个字节是什么？服务器是否同意持久连接？

**答案：**
- **前5个字节：** `<!doc`（从消息体中：`<!doctype html public...`）
- **持久连接：** 是，服务器同意持久连接

**解析：**
- 消息体开始部分：`<!doctype html public...`，前5个字符是 `<!doc`
- Connection头部：`Connection: Keep-Alive`，表示服务器同意保持连接

---

## P3-4: DNS查找和RTT计算

### 题目内容

**P3-4.** Suppose within your Web browser you click on a link to obtain a Web page. The IP address for the associated URL is not cached in your local host, so a DNS lookup is necessary to obtain the IP address. Suppose that n DNS servers are visited before your host receives the IP address from DNS; the successive visits incur an RTT of RTT₁, ⋯, RTTₙ. Further suppose that the Web page associated with the link contains exactly one object, consisting of a small amount of HTML text. Let RTT₀ denote the RTT between the local host and the server containing the object. Assuming zero transmission time of the object, how much time elapses from when the client clicks on the link until the client receives the object?

---

### 题目分析
本题计算从点击链接到接收对象的总时间，需要考虑DNS查找的RTT和对象传输的RTT。

### 详细解答

**已知条件：**
- n个DNS服务器，RTT分别为 RTT₁, RTT₂, ..., RTTₙ
- 本地主机到Web服务器的RTT为 RTT₀
- 对象传输时间假设为0（可忽略）

**解答过程：**

1. **DNS查找时间：**
   - 需要依次访问n个DNS服务器
   - 总DNS查找时间 = RTT₁ + RTT₂ + ... + RTTₙ = Σᵢ₌₁ⁿ RTTᵢ

2. **HTTP请求-响应时间：**
   - 获得IP地址后，建立TCP连接（假设已包含在RTT₀中）
   - 发送HTTP请求并接收响应
   - 时间 = RTT₀（往返时间）

3. **总时间：**
   ```
   总时间 = DNS查找时间 + HTTP请求-响应时间
          = Σᵢ₌₁ⁿ RTTᵢ + RTT₀
   ```

**答案：** 总时间 = RTT₁ + RTT₂ + ... + RTTₙ + RTT₀ = Σᵢ₌₁ⁿ RTTᵢ + RTT₀

---

## P3-5: 非持久和持久HTTP时间计算

### 题目内容

**P3-5.** Referring to Problem P3-4, suppose the HTML file references eight very small objects on the same server. Neglecting transmission times, how much time elapses with

a. Non-persistent HTTP with no parallel TCP connections?

b. Non-persistent HTTP with the browser configured for 6 parallel connections?

c. Persistent HTTP?

---

### 题目分析
本题比较不同HTTP连接方式下，下载8个小对象所需的时间。

### 详细解答

**已知条件：**
- HTML文件 + 8个小对象
- 所有对象在同一服务器上
- 传输时间可忽略（对象很小）
- 只考虑RTT时间

#### a. 非持久HTTP，无并行TCP连接

**解答：**
- 每个对象需要：建立TCP连接 + 发送HTTP请求 + 接收响应
- 每个对象需要1个RTT（假设TCP三次握手和HTTP请求-响应在一个RTT内完成）
- 总共需要：1个HTML + 8个对象 = 9个RTT

**答案：** 9 × RTT₀

#### b. 非持久HTTP，6个并行连接

**解答：**
- 浏览器可以同时打开6个TCP连接
- HTML文件：1个RTT
- 8个对象分两批下载：
  - 第一批：6个对象并行，需要1个RTT
  - 第二批：2个对象并行，需要1个RTT
- 总时间：1 + 1 + 1 = 3个RTT

**答案：** 3 × RTT₀

#### c. 持久HTTP

**解答：**
- 持久HTTP只需要建立一次TCP连接
- HTML文件：1个RTT（建立连接+请求+响应）
- 8个对象：在同一个连接上依次请求
- 每个对象需要1个RTT（请求+响应）
- 总时间：1 + 8 = 9个RTT

**注意：** 如果使用HTTP/1.1的流水线（pipelining），可以并行发送多个请求，但通常浏览器按顺序处理，所以仍然是9个RTT。

**答案：** 9 × RTT₀（或如果支持流水线，可能更少）

---

## P3-6: 并行HTTP下载分析

### 题目内容

**P3-6.** Consider a short, 10-meter link, over which a sender can transmit at a rate of 150 bits/sec in both directions. Suppose that packets containing data are 100,000 bits long, and packets containing only control (e.g., ACK or handshaking) are 200 bits long. Assume that *N* parallel connections each get 1/*N* of the link bandwidth. Now consider the HTTP protocol, and suppose that each downloaded object is 100 Kbits long, and that the initial downloaded object contains 10 referenced objects from the same sender. Would parallel downloads via parallel instances of non-persistent HTTP make sense in this case? Now consider persistent HTTP. Do you expect significant gains over the non-persistent case? Justify and explain your answer.

---

### 题目分析
本题分析在特定网络条件下，并行HTTP下载是否有效，以及持久HTTP相比非持久HTTP的优势。

### 已知条件
- 链路长度：10米
- 传输速率：150 bits/sec（双向）
- 数据包：100,000 bits
- 控制包：200 bits
- N个并行连接，每个获得1/N带宽
- 每个对象：100 Kbits
- 初始对象包含10个引用对象

### 详细解答

#### 1. 非持久HTTP并行下载是否合理？

**分析过程：**

**传输时间计算：**
- 单个对象大小：100 Kbits = 100,000 bits
- 单个连接速率：150/N bits/sec
- 传输时间：100,000 / (150/N) = 100,000N / 150 秒

**建立连接的开销：**
- TCP三次握手：需要发送控制包（200 bits）
- 每个连接建立时间：200 / 150 = 1.33秒
- HTTP请求-响应：也需要控制包传输

**关键考虑：**
- 链路很短（10米），传播延迟可忽略
- 但传输延迟是主要因素
- 对于小对象（100 Kbits），建立连接的开销相对较大

**结论：**
- 如果N很大，每个连接获得的带宽很小（150/N），传输时间会很长
- 建立连接的开销（控制包传输）相对于数据包传输来说较小
- 但对于小对象，并行下载可能带来一定收益，但收益有限

**答案：** 并行下载可能有一定收益，但收益有限，因为：
1. 对象较小（100 Kbits），传输时间相对较短
2. 建立连接的开销（控制包）相对于数据包较小
3. 但并行连接会分割带宽，可能影响总体性能

#### 2. 持久HTTP相比非持久HTTP是否有显著优势？

**分析：**

**非持久HTTP：**
- 每个对象需要建立新连接
- 11个对象（1个HTML + 10个引用对象）= 11次连接建立
- 每次连接建立需要传输控制包（200 bits）

**持久HTTP：**
- 只需建立一次连接
- 后续对象复用同一连接
- 节省了10次连接建立的开销

**优势计算：**
- 节省的连接建立时间：10 × (200 / 150) = 13.3秒
- 对于小对象，这个节省是显著的

**答案：** 是的，持久HTTP有显著优势：
1. **减少连接建立开销**：只需建立1次连接，而非11次
2. **减少控制包传输**：节省10次连接建立的控制包传输时间
3. **提高效率**：对于小对象，连接建立开销相对较大，持久连接能显著减少总时间

---

## P3-7: 文件分发时间计算

### 题目内容

**P3-7.** Consider distributing a file of *F* = 20 Gbits to *N* peers. The server has an upload rate of *uₛ* = 30 Mbps, and each peer has a download rate of *dᵢ* = 2 Mbps and an upload rate of *u*. For *N* = 10, 100, and 1,000 and *u* = 300 Kbps, 700 Kbps, and 2 Mbps, prepare a chart giving the minimum distribution time for each of the combinations of *N* and *u* for both client-server distribution and P2P distribution.

---

### 题目分析
本题比较客户端-服务器（C/S）和P2P两种文件分发方式的最小分发时间。

### 已知条件
- 文件大小：F = 20 Gbits
- 服务器上传速率：uₛ = 30 Mbps
- 每个对等节点下载速率：dᵢ = 2 Mbps
- 每个对等节点上传速率：u（变量）
- N = 10, 100, 1000
- u = 300 Kbps, 700 Kbps, 2 Mbps

### 详细解答

#### 客户端-服务器（C/S）分发时间

**分析：**
在C/S模式下，服务器需要向N个节点分发文件。假设服务器可以同时向多个节点传输（流体模型）。

**公式：**
```
T_C/S = max(NF/uₛ, F/d_min)
```

**解释：**
- 如果服务器同时向所有N个节点传输，每个节点获得 uₛ/N 的速率
- 如果 uₛ/N ≤ d_min，则受限于服务器容量，时间 = NF/uₛ
- 如果 uₛ/N > d_min，则受限于节点下载速率，时间 = F/d_min

**计算：**
- uₛ = 30 Mbps, d_min = 2 Mbps
- 当N=10时：uₛ/N = 3 Mbps > 2 Mbps，所以 T = F/d_min = 10,000秒
- 当N=100时：uₛ/N = 0.3 Mbps < 2 Mbps，所以 T = NF/uₛ = 100×20,000/30 = 66,667秒
- 当N=1000时：uₛ/N = 0.03 Mbps < 2 Mbps，所以 T = NF/uₛ = 1000×20,000/30 = 666,667秒

**注意：** C/S模式下，当节点数较少时，受限于节点下载速率；当节点数较多时，受限于服务器上传容量。

#### P2P分发时间

**公式：**
```
T_P2P = max(F/uₛ, F/d_min, NF/(uₛ + Σuᵢ))
```

其中：
- F/uₛ：服务器上传时间
- F/d_min：最慢节点下载时间
- NF/(uₛ + Σuᵢ)：总上传容量限制

**计算表格：**

| N | u | uₛ + Σuᵢ | NF/(uₛ + Σuᵢ) | T_P2P |
|---|-----|----------|----------------|-------|
| 10 | 300 Kbps | 30 + 10×0.3 = 33 Mbps | 200/33 = 6.06秒 | max(666.67, 10000, 6.06) = **10,000秒** |
| 10 | 700 Kbps | 30 + 10×0.7 = 37 Mbps | 200/37 = 5.41秒 | **10,000秒** |
| 10 | 2 Mbps | 30 + 10×2 = 50 Mbps | 200/50 = 4秒 | **10,000秒** |
| 100 | 300 Kbps | 30 + 100×0.3 = 60 Mbps | 2000/60 = 33.33秒 | **10,000秒** |
| 100 | 700 Kbps | 30 + 100×0.7 = 100 Mbps | 2000/100 = 20秒 | **10,000秒** |
| 100 | 2 Mbps | 30 + 100×2 = 230 Mbps | 2000/230 = 8.70秒 | **10,000秒** |
| 1000 | 300 Kbps | 30 + 1000×0.3 = 330 Mbps | 20000/330 = 60.61秒 | **10,000秒** |
| 1000 | 700 Kbps | 30 + 1000×0.7 = 730 Mbps | 20000/730 = 27.40秒 | **10,000秒** |
| 1000 | 2 Mbps | 30 + 1000×2 = 2030 Mbps | 20000/2030 = 9.85秒 | **10,000秒** |

**注意：** 在所有情况下，瓶颈都是节点的下载速率（2 Mbps），而不是上传容量。

**完整答案表格：**

| 分发方式 | N | u | 最小分发时间（秒） |
|---------|---|-----|------------------|
| C/S | 10 | - | 10,000 |
| C/S | 100 | - | 66,667 |
| C/S | 1000 | - | 666,667 |
| P2P | 10 | 300 Kbps | 10,000 |
| P2P | 10 | 700 Kbps | 10,000 |
| P2P | 10 | 2 Mbps | 10,000 |
| P2P | 100 | 300 Kbps | 10,000 |
| P2P | 100 | 700 Kbps | 10,000 |
| P2P | 100 | 2 Mbps | 10,000 |
| P2P | 1000 | 300 Kbps | 10,000 |
| P2P | 1000 | 700 Kbps | 10,000 |
| P2P | 1000 | 2 Mbps | 10,000 |

**结论：** 
- **C/S模式**：当N较小时（如10），受限于节点下载速率（2 Mbps），时间为10,000秒；当N较大时（如100, 1000），受限于服务器上传容量，时间显著增加。
- **P2P模式**：在所有情况下，由于节点下载速率（2 Mbps）是瓶颈，分发时间都是10,000秒，与N和u无关。
- **P2P优势**：当N较大时，P2P模式明显优于C/S模式，因为P2P可以利用所有节点的上传带宽。

---

## P3-8: 客户端-服务器文件分发方案

### 题目内容

**P3-8.** Consider distributing a file of *F* bits to *N* peers using a client-server architecture. Assume a fluid model where the server can simultaneously transmit to multiple peers, transmitting to each peer at different rates, as long as the combined rate does not exceed *uₛ*.

a. Suppose that *uₛ*/*N* ≤ *d_min*. Specify a distribution scheme that has a distribution time of *NF*/*uₛ*.

b. Suppose that *uₛ*/*N* ≥ *d_min*. Specify a distribution scheme that has a distribution time of *F*/*d_min*.

c. Conclude that the minimum distribution time is in general given by max{*NF*/*uₛ*, *F*/*d_min*}.

---

### 题目分析
本题要求在流体模型下，设计文件分发方案，使得分发时间达到理论最小值。

### 详细解答

#### a. 当 uₛ/N ≤ d_min 时，设计分发方案使分发时间为 NF/uₛ

**分析：**
- 条件：uₛ/N ≤ d_min，即每个节点平均分配到的服务器带宽不超过最小下载速率
- 目标：分发时间 = NF/uₛ

**方案设计：**
1. **服务器分配策略**：服务器将上传带宽平均分配给N个节点
   - 每个节点获得：uₛ/N 的上传速率
   - 由于 uₛ/N ≤ d_min，节点下载速率不是瓶颈

2. **传输过程**：
   - 服务器同时向所有N个节点传输
   - 每个节点以 uₛ/N 的速率接收
   - 总传输量：NF（N个节点，每个F bits）
   - 总上传容量：uₛ
   - 时间 = NF/uₛ

**答案：**
- 服务器同时向所有N个节点传输
- 每个节点分配 uₛ/N 的上传速率
- 由于 uₛ/N ≤ d_min，节点可以以分配速率接收
- 分发时间 = NF/uₛ

#### b. 当 uₛ/N ≥ d_min 时，设计分发方案使分发时间为 F/d_min

**分析：**
- 条件：uₛ/N ≥ d_min，即每个节点平均分配到的服务器带宽超过最小下载速率
- 目标：分发时间 = F/d_min

**方案设计：**
1. **服务器分配策略**：服务器优先保证最慢节点（d_min）的下载需求
   - 最慢节点需要 d_min 的上传速率
   - 剩余带宽 uₛ - d_min 可以分配给其他节点

2. **传输过程**：
   - 最慢节点以 d_min 速率接收，需要时间 F/d_min
   - 其他节点可以以更高速率接收，但受限于最慢节点
   - 所有节点在 F/d_min 时间内完成下载

**答案：**
- 服务器向最慢节点（d_min）分配 d_min 的上传速率
- 剩余带宽 uₛ - d_min 分配给其他节点
- 所有节点在 F/d_min 时间内完成下载（受最慢节点限制）

#### c. 证明最小分发时间为 max{NF/uₛ, F/d_min}

**证明：**

**情况1：uₛ/N ≤ d_min**
- 根据(a)，最优分发时间 = NF/uₛ
- 由于 uₛ/N ≤ d_min，有 NF/uₛ ≥ F/d_min
- 因此，max{NF/uₛ, F/d_min} = NF/uₛ ✓

**情况2：uₛ/N ≥ d_min**
- 根据(b)，最优分发时间 = F/d_min
- 由于 uₛ/N ≥ d_min，有 NF/uₛ ≤ F/d_min
- 因此，max{NF/uₛ, F/d_min} = F/d_min ✓

**结论：**
- 最小分发时间 = max{NF/uₛ, F/d_min}
- 这表示分发时间受两个因素限制：
  1. **服务器上传容量限制**：NF/uₛ（需要传输的总数据量除以服务器上传速率）
  2. **最慢节点下载速率限制**：F/d_min（文件大小除以最慢下载速率）

---

## P3-9: TCP/UDP客户端服务器实验

### 题目内容

**P3-9.** Install and compile the Python programs TCPClient and UDPClient on one host and TCPServer and UDPServer on another host.

a. Suppose you run TCPClient before you run TCPServer. What happens? Why?

b. Suppose you run UDPClient before you run UDPServer. What happens? Why?

c. What happens if you use different port numbers for the client and server sides?

---

### 题目分析
本题要求理解TCP和UDP协议的区别，特别是连接建立和错误处理方面的差异。

### 详细解答

#### a. 在运行TCPServer之前运行TCPClient会发生什么？为什么？

**答案：**
- **现象：** TCPClient会失败，无法建立连接
- **原因：**
  1. TCP是面向连接的协议
  2. 客户端尝试连接到服务器时，需要服务器处于监听状态
  3. 如果服务器未运行，客户端的connect()调用会失败
  4. 通常会返回"Connection refused"错误

**详细解释：**
- TCP三次握手需要服务器主动监听（listen）和接受（accept）连接
- 如果服务器未运行，客户端的SYN包无法得到响应
- 客户端会收到RST（Reset）包或超时错误

#### b. 在运行UDPServer之前运行UDPClient会发生什么？为什么？

**答案：**
- **现象：** UDPClient可能不会立即报错，但发送的数据会丢失
- **原因：**
  1. UDP是无连接协议
  2. 客户端可以发送数据，不需要服务器预先建立连接
  3. 但如果服务器未运行，数据包会被丢弃
  4. 客户端不会收到确认，但也不会立即知道错误

**详细解释：**
- UDP的sendto()调用会成功返回（从应用层角度看）
- 但数据包发送到网络后，如果目标端口没有监听程序，会被丢弃
- 客户端不会收到ICMP错误消息（取决于系统配置），或者收到"Port unreachable"错误
- 与TCP不同，UDP客户端不会在发送时立即知道服务器是否存在

#### c. 如果客户端和服务器使用不同的端口号会发生什么？

**答案：**
- **TCP情况：**
  - 连接会失败
  - 客户端尝试连接到服务器的端口，如果该端口没有监听程序，连接会被拒绝
  - 返回"Connection refused"错误

- **UDP情况：**
  - 数据包会发送到指定端口
  - 如果该端口没有监听程序，数据包会被丢弃
  - 可能收到ICMP"Port unreachable"错误消息
  - 客户端不会收到预期的响应

**关键点：**
- 客户端和服务器必须使用匹配的端口号
- 客户端需要知道服务器的端口号
- 服务器监听特定端口，客户端连接到该端口

---

## 知识点总结

### 1. HTTP协议
- **非持久连接 vs 持久连接**：持久连接可以复用TCP连接，减少建立连接的开销
- **HTTP消息格式**：请求行、头部、空行、消息体
- **HTTP头部字段**：Date、Last-Modified、Content-Length、Connection等

### 2. DNS查找
- DNS查找需要依次访问多个DNS服务器
- 总时间 = 所有DNS服务器RTT之和 + 最终RTT

### 3. 文件分发
- **客户端-服务器模式**：服务器是瓶颈，分发时间与节点数无关（如果服务器带宽足够）
- **P2P模式**：可以利用所有节点的上传带宽，但受限于最慢节点的下载速率
- **最小分发时间**：max{服务器容量限制, 最慢节点限制}

### 4. TCP vs UDP
- **TCP**：面向连接，需要服务器先运行，连接失败会立即报错
- **UDP**：无连接，可以发送数据，但服务器不存在时数据会丢失

---

## 常见错误分析

### 1. HTTP请求-响应数量混淆
- **错误**：认为一个Web页面只需要一个请求-响应
- **正确**：每个对象（HTML、图片、CSS等）都需要单独的请求-响应

### 2. 持久连接理解错误
- **错误**：认为持久连接可以同时传输多个对象
- **正确**：持久连接可以复用连接，但仍需要依次发送请求

### 3. 文件分发时间计算错误
- **错误**：忽略最慢节点的限制
- **正确**：分发时间 = max{服务器容量限制, 最慢节点限制}

### 4. TCP/UDP行为混淆
- **错误**：认为UDP也需要服务器先运行
- **正确**：UDP是无连接协议，可以发送数据，但服务器不存在时数据会丢失

---

**文档生成时间**：2025年
**适用课程**：计算机网络
**作业编号**：Homework 3

