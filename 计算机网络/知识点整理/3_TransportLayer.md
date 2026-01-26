# 传输层

传输层  
直接为运行在不同主机上的应用程序（即应用层）  
提供逻辑通信  

而需要探讨的重要问题就是  
如何将底层网络提供的“端到端（host-to-host）”交付  
转变为应用进程之间的 **逻辑通信（logical communication）** 服务  

## 1. 主要功能

1. Multiplexing and Demultiplexing（多路复用与解复用）  

2. Reliable Data Transfer（RDT，可靠数据传输）  

3. Flow Control（流量控制）  

4. Congestion Control（拥塞控制）  

### 1.1. Multiplexing and Demultiplexing  

传输层的基本原理  
确保数据能交付给正确的应用进程  

**Multiplexing（多路复用）**  
发送端从多个Sockets收集数据  
并为每个Block封装（Encapsulate）一个传输层Header  
直观理解：多路（processes）数据流合并为一路数据流发送到网络层  
因为网络层只处理host-to-host的交付  

**Demultiplexing（多路解复用）**  
接收端根据Header信息将Message交付给正确的Socket  
直观理解：重新将网络层收到的一个数据流拆分为多个数据流交付给不同的processes  

#### 1.1.1. Multiplexing 细节

无论是 UDP 还是 TCP，头部都必须包含以下两个字段  

- 源端口号（Source Port Number）：标识发送端主机上的发起进程。
- 目的端口号（Destination Port Number）：标识接收端主机上的目标进程。  

端口号字段通常为 16 位，允许每台主机拥有多达 65535 个不同的端口。

除上述字段外，Header中还有其他Fields  
内容取决于所使用的协议（UDP 或 TCP）  

问题？源和目的IP在哪？  
IP 首部里明确记录了源 IP 地址和目的 IP 地址  

#### 1.1.2. Demultiplexing in UDP and TCP

UDP 为 Connectionless Demultiplexing（无连接解复用）  
TCP 为 Connection-oriented Demultiplexing（面向连接解复用）  

- **UDP** 使用 “二元组” （目标IP、目标Port）进行Demultiplexing  
    具体来说，同一个二元组对应目标主机上的同一个Socket  
    因而只由目标IP和目标Port决定数据包的去向  
- **TCP** 使用 “四元组” （源IP、源Port、目标IP、目标Port）进行Demultiplexing（即区分不同的连接）  
    TCP需要处理连接，除目标IP、Port外  
    还通过源IP、Port区分不同链接  
    因此可以在同目标IP、Port的情况下，根据不同的源IP、Port区分不同的连接  
    也就是有不同的Sockets  
    因而可以复用目标IP上的同一个Port给不同的连接使用  

而Receiver端根据这些信息将数据交付给正确的Socket  
*Socket Address = IP + Port*  

### 1.2. Reliable Data Transfer（RDT，可靠数据传输）

由于底层网络（IP层）不可靠  
Transport Layer需要通过一系列机制实现可靠性  

**差错检测（Error Detection）**  
通过 Checksum（校验和）检测位错误  

**接收方反馈（Acknowledgment）**  
通过 **ACK（确认）** 和 **NAK（否认）** 反馈接收状态  

**重传机制（Retransmission）**  
发送方收到 NAK 或 **Timeout（超时）** 时重新发送数据  

**序列号（Sequence Number）**  
处理由于重传导致的重复Packets问题  

**流水线技术（Pipelining）**  
克服“停-等”协议效率底下的问题  
允许发送方在等待确认前发送多个Packets  
（例如Go-Back-N，GBN协议和Selective Repeat协议）  

### 1.3. Flow Control（流量控制）

防止发送方过快地发送数据而淹没接收方  

**Mechanism**：  
接收方在Message Header中“宣告（Advertise）”其 **接收窗口（Receive Window，rwnd）** 大小  
表示当前可用的Buffer空间  

**保证（Guarantee）**：  
发送方根据 `rwnd` 限制已发送但未确认的数据量  
确保接收Buffer不会溢出  
（类似Store-and-Forward结构中过快发送数据淹没中间节点的问题）  

### 1.4. Congestion Control（拥塞控制）

针对整个 **网络核心** 的保护机制  
防止过多的源以过快的速度发送数据导致网络瘫痪（拥塞）  

**Presentation**：  
分组丢失（Packet Loss，丢包）：Router Buffer Overflow  
长时延（Long Delay）：Router Queueing Delay增加  

**AIMD（Additive Increase Multiplicative Decrease，增量增加乘法减少）**：  
这是Congestion Control的核心策略  

发送方通过观察丢包事件来调整 **拥塞窗口（Congestion Window，cwnd）** 大小  
从而动态控制发送速率  

## 2. Transport Layer Protocols

提供两种主要的传输层协议：  

1. UDP（User Datagram Protocol，用户数据报协议）  
2. TCP（Transmission Control Protocol，传输控制协议）  

在下面进行详细讲解  

## 3. Connectionless Transport Protocol: UDP

UDP（User Datagram Protocol，用户数据报协议）  
Internet传输层中一种“简单、无修饰”的协议  
直接透传了底层IP协议的“尽力而为（best-effort）”服务模型  

### 3.1. 核心特性

**Connectionless（无连接）**  
发送方（Sender）和接收方（Receiver）之间没有建立连接的过程  
即无需握手（Handshake）  
每个UDP 报文段（Datagram）的发送都是独立的  
因此也没有建立连接带来的RTT延迟  

**Best-Effort（尽力而为）**  
UDP不保证数据交付  
Datagram可能会丢失、重复或乱序到达  

**Simple（简单性）**  
Sender和Receiver都不需要维护连接状态（例如窗口大小、序列号等）  
且Header开销很小（仅8 Bytes）  

**No Congestion Control（无拥塞控制）**  
UDP允许Applications以任意其期望的速率发送数据  
即使发生了网络拥塞也不会自动减慢发送速率  

### 3.2. 应用

- Streaming Multimedia Apps（流媒体应用）  
    loss tolerant  
    rate sensitive  
- DNS（Domain Name System，域名系统）
- SNMP（Simple Network Management Protocol，简单网络管理协议）  
- HTTP/3（基于UDP的HTTP新版本）  

如果应用需要基于UDP的可靠传输（例如HTTP/3）  
需要应用层实现可靠性  
并在应用层添加拥塞控制  

### 3.3. UDP Datagram Structure

UDP 仅提供最基本的复用服务，其Header非常简单（仅 8 字节）  
包含：

- 源端口号（Source Port #）（2 Bytes）。
- 目的端口号（Destination Port #）（2 Bytes）。
- 长度（Length）：指示整个报文段（头部+数据）的字节数。
- 校验和（Checksum）：用于检测报文段在传输过程中是否发生了位错误（如比特翻转）

### 3.4. 校验机制（Checksum）

目标：  
检测被传输的数据段的错误（例如比特翻转）  

Checksum计算  
计算式不仅包含UDP Header和Payload（Application Data）  
还引入了 **伪首部（Pseudo Header）**  
（包含源/目的IP、协议号、长度字段，“伪”表示只在计算时逻辑上存在）  
确保Datagram被交付到正确端点  

若检测到错误  
Datagram会被丢弃  

#### 3.4.1. 注意

Checksum只提供Weak Protection（弱保护）  
不能保证100%检测到所有错误  

可见课件Transport Layer：3-31~32  

### 3.5. Why UDP？

Delay Sensitive  
DNS 查询和实时流媒体应用（如语音/视频通话）需要极低延迟，宁可容忍少量丢包也不愿接受 TCP 的重传和拥塞控制延迟  

Simple  
状态维护简单  
适合需要支持大量活跃客户的服务器  
（如SNMP管理系统）  

## 4. Principles of RDT

RDT Protocol演进了多个版本的模型  
逐步解决 **位错（Bit Error）** 和 **丢失（Loss）** 问题  

*注意此处我们为了简化，课件中大部分只考虑了单向传输data，实际上control info是双向的*  

课件上采用了许多有限状态机来表示sender和receiver的状态变化  

### 4.1. History

- RDT 1.0：  
    Reliable Channel上的RDT  
    此时sender和receiver状态独立  
    - 假设底层信道（Channel）100%可靠  
        没有位错和丢包  
    - 机制简单：发送方只管发，接收方只管收
- RDT 2.0：  
    具有 **位错（Bit Error）** 的信道  
    之物理层面干扰可能导致的某些二进制位的“翻转”，可以理解为bit层面的out-of-order  
    虽然此时有反馈信息机制了，但是两者状态依然实际上相对独立  
    - 核心机制：  
        引入 **Checksum** 检测位错  
        使用 **ACK/NAK** 反馈机制（收到NAK时重传）  
        *ACK：Acknowledgment（确认）；NAK：Negative Acknowledgment（否定确认）*  
    - 致命缺陷（fatal flaw）：  
        如果 ACK/NAK 本身出错，发送方无法判断接收状态  
    - 其他缺陷：  
        此时已经引入了Stop-and-Wait机制，效率低下  
- RDT 2.1 & 2.2：  
    处理损坏或丢失的Feedback（ACK/NAK）  
    - 2.1：  
        为Packets添加 **序列号（Sequence Number）**  
        此处的SeqNum实际上只是在发送时标识0/1两种状态，如果正确运转，应该是两个SeqNum交替出现  
        收到损坏的ACK/NAK时同样会重传当前Packet  
        Receiver通过SeqNum识别并丢弃重复Packets，并重新发送ACK  
    - 2.2：  
        实现 **NAK-Free**（TCP也采用了此机制）  
        Receiver只发ACK  
        但是ACK必须包含 **最后正确收到** 的分组SeqNum（seq #，“#”表示number的缩写）  
        Sender收到重复需要的ACK等同于收到NAK  
        依然处理不了丢包问题（不管是Data还是ACK）  
- RDT 3.0：  
    具有位错和丢失的信道  
    errors and loss  
    - 引入 **Timer（计时器）**  
        如果Sender在规定时间内未收到ACK（可能由于Packet或ACK丢失）  
        触发Timeout重传  
        注意此时仅留下了Timeout Retransition机制（也就是会忽略重复ACK）  
    - 采用 **Stop-and-Wait** 机制  
        每次只发送一个Packet，等待ACK后再发送下一个  
        效率极低，RTT期间信道利用率低
    - rdt3.0具有自纠正能力  
        能够处理一般的位错和丢包问题  
        但是还是可能发生静默数据包丢失（课件Transport Layer：3-60(e)图如果后续pkt1没收到就会发生）  
        TCP后续会采用大seq#范围、TTL/MSL（包最大寿命）等机制避免这种情况（幽灵Packet）  

### 4.2. Pipelining and Sliding Window

流水线技术与滑动窗口  

#### 4.2.1. Pipelining

首先，为了解决 RDT 3.0 中 Stop-and-Wait 的低效率问题  
引入 **Pipelining（流水线技术）**  
允许发送方在等待ACK前发送多个Packets  

方法：  

- 拓展SeqNum范围  
    来标识多个Packets  
- 在sender和/或receiver维护buffer  
    具体表现为滑动窗口（Sliding Window）机制  

#### 4.2.2. Go-Back-N (GBN)

回退N步  

- 发送方：  
    - 维护长度为N的窗口  
        采用 **累计确认**  
        即 ACK(n) 表示序号不大于n的所有Packets均已正确收到  
        *直观理解，从seq# 0开始凑ACKed Packets到n，后面会从receiver方理解*  
        收到 ACK(n) 触发窗口滑动，队首移动至 n+1  
        按队列发送窗口内的Packets *（因此Packets可能有四种状态：ACKed，in-flight，可用【窗口内待发送】，暂不可用；这四种状态必定依序出现）*  
    - 维护单个定时器  
        只为最早未确认的Packet计时  
        其确认后重置定时器，并开始为下一个最早未确认的Packet计时  
- 接收方：  
    - ACK-only  
        只发送ACK(n)  
        n指 **最高** **依序（In-Order）** seq#  
        因此只需要记住rcv_base（下一个期望收到的seq#）  
        注意这可能会产生重复ACKs  
    - Out-of-Order Packets  
        可能会被丢弃或缓存  
        重发送ACK(n)  
- 异常处理：  
    如果Timeout（timeout(n)）  
    重传窗口内 **所有** Packets（从n开始）  

#### 4.2.3. Selective Repeat (SR)

选择重传  
为了解决GBN中可能出现的不必要重传问题  

- Sender：  
    为每个未确认的Packet维护单独的定时器  
    *Packet依然还是4状态，但是窗口内还可能出现ACKed状态（乱序接收缓存）*  
    接收ACK(n)后，将n标记为ACKed  
    只有当n为当前窗口最小seq#(send_base)时，才会触发窗口滑动  
    接收到重复ACK(n)时忽略  
- Receiver：  
    对每个正确到达的Packet进行 **独立确认**  
    （即 ACK(n) 仅确认序号为 n 的Packet）  
    缓存乱序到达的Packets  
    接收到重复Packet(seq # n)，丢弃包并重发ACK(n)  
- 异常处理：  
    Timeout（timeout(n)）后仅重传特定的丢失Packet(seq # n)  
- 补充：  
    一般为了防止回环（也就是旧循环的序号与新循环重叠了），窗口大小N一般会小于等于Seq#范围的一半  

### 4.3. 重要设计原则

**ARQ机制**  
ARQ（Automatic Repeat reQuest，自动重传请求）  
利用确认和超时的RDT策略统称为ARQ机制  

**端到端原则**  
（End-to-End Principle）  
可靠性应该在端系统（Transport Layer）实现  
因为底层链路的可靠性并不能保证全局可靠  

### 4.4. Other Knowledge

#### 4.4.1. 软件接口与服务抽象

服务抽象：  
传输层向应用层提供“可靠信道”的抽象  
实际实现基于不可靠的网络层  

核心接口函数：  

- `rdt_send()`:  
    Sender AppLayer调用  
    将Data交付给Sender TransLayer  
- `udt_send()`:  
    Sender TransLayer调用  
    将Packet交付给底层网络层  
- `rdt_rcv()`:  
    数据到Receiver时  
    Receiver TransLayer调用  
    将Packet交付给Receiver AppLayer  
- `deliver_data()`:  
    Receiver TransLayer调用  
    将Packet解封装（decapsulate）后交付给Receiver AppLayer  

一般将发送接收包简称为`sndpkt`和`rcvpkt`  

#### 4.4.2. 单向数据传输中的双向控制流

虽然我们通常讨论单向数据传输（即数据只从发送方流向接收方），但为了实现可靠性，控制信息（如 ACK, NAK）必须在两个方向上流动  
这意味着即使数据流是单向的，传输层协议仍然需要处理双向通信以确保数据的可靠传输。  

#### 4.4.3. Stop-and-Wait 协议的Bottleneck

Utilization（信道利用率）  
$U_{sender}$  
发送方的利用率  
定义为发送方忙于发送数据所占的时间比例  

性能计算：  

$$
U_{sender} = \frac{L/R}{RTT + L/R}
$$

因此，如果 RTT 很大（长距离），R很大（高带宽）链路上  
这个值很小  
协议限制了底层硬件的性能发挥  

#### 4.4.4. 满管道原则：带宽时延积（BDP）

核心理念：  
为了充分利用网络  
必须实现“流水线传输”以“填满管道（keep the pipe full）”  

BDP（Bandwidth-Delay Product，带宽时延积）  
定义  

R 与 RTT 的乘积决定了网络“管道”的容量  
也就是在途（in-flight）数据量的上限  

具体来说就是收到第一个ACK前  
发送方理论上可以发送的在途bit数  

$$
BDP = R \times RTT
$$

## 5. Connection-Oriented Transport Protocol: TCP

TCP（Transmission Control Protocol，传输控制协议）  

### 5.1. 核心特性

Point-to-Point（点对点连接）  

Reliable, In-Order Byte Stream（可靠、有序的字节流）  

Full Duplex Data（全双工数据传输）  
*全双工：允许同时双向传输数据*  

Cumulative Acknowledgments（累计确认）  

Pipelining（流水线技术）  

Connection Oriented（面向连接）  
Handshaking  
三次握手（Three-Way Handshake）建立连接  
四次挥手（Four-Way Handshake）断开连接  

Flow Control（流量控制）  

### 5.2. TCP Segment Structure（TCP报文段结构）

TCP Header较UDP复杂得多  
通常为20 Bytes（无选项字段时）  
并以32bit（4 Bytes）为单位对齐  
便于软件处理  

#### 5.2.1. 核心标识

源端口号（Source Port #）（2 Bytes）  
目的端口号（Destination Port #）（2 Bytes）  

与IP Header中的源/目的IP地址一起  
组成四元组（source IP, source Port, dest IP, dest Port）  
唯一标识一条TCP连接  

#### 5.2.2. 可靠传输关键（SeqNum & AckNum）

SeqNum（4 Bytes）  
Segment中第一个数据字节在字节流中的编号  
TCP对字节计数而非Packet计数  

*注意：此处Seq#注意与之前RDT中所讲的ACK(n)中的n区分开来，TCP拓展了Seq#的范围空间（32bits），避免序号太快发生回绕（重叠）*  

AckNum（4 Bytes）  
期望从对方收到的下一个字节的序号  
TCP采用 **累积确认（Cumulative Acknowledgment）**  
确认号 n 表示所有小于 n 的字节均已正确收到  
*更准确来说指的是从ISN开始的字节流中小于 n 的字节均已收到*  

#### 5.2.3. 控制与状态标志（Flags）

TCP Header包含6bit的Flags（标识位）  
用于控制连接状态和数据处理  

- SYN（Synchronize）：用于建立连接的同步标志。  
- ACK（Acknowledgment）：表示确认号字段有效。  
- FIN（Finish）：用于终止连接的结束标志。
- RST（Reset）：用于重置连接（用于强制关闭出现错误的连接）  
- PSH（Push）：提示接收方立即将数据交付给应用层（而不是在Buffer中等待）。  
- URG（Urgent）：紧急标识，配合 **Urgent Pointer（紧急指针）** 使用，指出Segment中紧急数据的结束位置。  

这些字段在Header Length字段的后的空白段之后  
在receive window字段之前  

#### 5.2.4. 流量控制字段（Window Size）

Header Length（4 Bits）  
首部长度，单位为32-bit字（4 Bytes）  
*因为Options字段存在，Header长度可变，最大为60 Bytes（15\*4 Bytes）*  

Header Length字段后有4 Bits的Reserved（保留）字段  
也就是没有实际用途，必须置0  

Receive Window（2 Bytes）  
用于流量控制  
Receiver告知Sender自己当前的可用Buffer空间大小（以字节为单位）  
防止发送方发送过多数据淹没接收方  

#### 5.2.5. 校验与选项

Checksum（2 Bytes）  
涵盖TCP Header、Data和伪首部的校验和  
检测传输中的位错误  

Options（可变长度）  
用于扩展功能  
例如：

- MSS（Maximum Segment Size，最大报文段大小）选项
- Window Scaling（窗口缩放因子）选项  
- SACK（Selective Acknowledgment，选择性确认）选项等  

### 5.3. TCP RDT

TCP 的可靠数据传输（Reliable Data Transfer, RDT）是在不可靠的 IP 协议之上，通过多种机制共同实现的可靠字节流服务  

#### 5.3.1. RDT核心组件

SeqNum  
AckNum  
Retransmissions（ARQ，automatic repeat request）  

#### 5.3.2. ReTransmission Triggers（重传触发机制）

两种方式：  

1. Timeout（超时重传）  
    Sender为最早发送的未确认Segment维护Timer  
    如果TimeoutInterval内未收到ACK  
    触发重传  
2. Fast Retransmit（快速重传）  
    由于Timeout周期一般较长，TCP引入启发式算法  
    如果Sender连续收到3个相同的ACK（即Duplicate ACKs）  
    则认为该SeqNum后的Segment丢失  
    立即在Timeout前进行重传  

#### 5.3.3. RTO动态计算

RTO（Retransmission Timeout，重传超时）  

超时时间（RTO）的动态计算
TCP 使用指数加权移动平均（EWMA）算法来预测往返时延（RTT），从而动态调整超时时间：

- SampleRTT:measured time from segment transmission until ACK receipt  
    测量的 RTT 样本值（从发送 Segment 到收到 ACK 的时间）  
    忽略重传的 Segment  
- EstimatedRTT：平滑后的 RTT 均值  
    公式为 EstimatedRTT=(1−α)⋅EstimatedRTT+α⋅SampleRTT（通常 α=0.125）。
- DevRTT：RTT 与均值的偏差估算  
    公式为 DevRTT=(1−β)⋅DevRTT+β⋅|SampleRTT−EstimatedRTT|（通常 β=0.25）。  
- TimeoutInterval：  
    公式为 TimeoutInterval=EstimatedRTT+4⋅DevRTT。  
    通过考虑偏差，TimeoutInterval 能更好地适应 RTT 的波动  
- Karn/Partridge 算法：在计算 RTT 样本时，不采样重传报文段，以避免二义性。

#### 5.3.4. Receiver的ACK生成规则

RFC 5681  
Receiver并非对每个Segment都立即回复ACK  

- **延迟确认（Delayed ACK）**  
    收到In-Order Segment，最多等到500ms，期待能与下一个In-Order Segment合并发送确认  
- **立即累计确认（Immediate Cumulative ACK）**  
    如果已有待发送的ACK，立即发送一个累计ACK覆盖两个段  
- **立即重复确认（Immediate Duplicate ACK）**  
    发现SeqNum空缺（失序，out-of-order到达）时，立即发送冗余ACK告知期望的序号  
    这是为了触发发送方的快速重传机制  

#### 5.3.5. TCP对RDT理论模型的吸收

TCP实现结合了 GBN 和 SR 的思想  

- 类似GBN  
    使用 Accumulative ACK  
    但在Sender只维护一个Timer（最早未确认的Segment）  
- 类似SR  
    许多实现会缓存Out-of-Order Segments（不直接丢弃）  
    且在Fast Retransmit中仅重传特定丢失的那一个Segment  

### 5.4. Flow Control （流量控制）

TCP 流量控制（Flow Control）的主要目的是  
**匹配** 发送方的发送速率与接收方应用程序的读取速率  
防止发送方因发送过快而淹没接收方的缓存

#### 5.4.1. 核心机制：Receive Window（接收窗口）

rwnd  

- 字段定义：  
    TCP Segment Header 中包含一个 16bits 的 rwnd 字段  
- 通告机制：  
    Receiver 通过 rwnd 字段向 Sender 实时告知自己的可用缓存空间大小（以字节为单位）  
- 缓存限制：  
    Receiver 必须确保已接受但未读取的数据  
    （LastByteRcvd - LastByteRead）  
    不超过预设的缓存总量 MacRcvBuffer  
  
#### 5.4.2. 发送方逻辑

- 计算公式  
    Sender根据收到的rwnd计算Effective Window（有效窗口）大小：  
- 限制条件  
    Sender保证其In-flight且为确认的数据量  
    （LastByteSent - LastByteAcked）  
    不大于收到的 rwnd 值  
- 动态调整：  
    如果 Receiver 应用程序处理数据变慢  
    rwnd会响应减小  
    Sender相应降低发送速率  

#### 5.4.3. 特殊情况处理：零窗口与探查

- 死锁风险：  
    Receiver通告 rwnd=0 时  
    发送方停止发送数据  
    如果随后 Receiver 缓存清空并通告新窗口的ACK丢失  
    将导致死锁  
- Zero Window Probes（零窗口探查）：  
    为防止死锁  
    Sender在 rwnd=0 时  
    会定期发送小量探查Segment（通常为1字节）  
    其会强制Receiver回送带有当前rwnd值的ACK  

#### 5.4.4. 重要原则与优化

**Silly Window Syndrome（糊涂窗口综合症）**  
为了避免频繁发送极小报文段  
Receiver通常在Buffer空间达到一个最大Segment大小（MSS）  
或者缓存的一半之前  
通告 rwnd = 0  

与Congestion Control的区别  
Flow Control控制是Edge-to-Edge（端到端）的  
旨在防止淹没Receiver  
而Congestion Control控制的是Network Core（网络核心）的  
也就是全局性的  
旨在防止网络拥塞  

### 5.5. Connection Management（连接管理）

TCP **连接管理**是确保双方进程在不可靠网络上**同步状态**、**协商参数**（如初始序号）并**建立可靠字节流传输**的关键过程  

#### 5.5.1. 连接建立：三次握手（Three-Way Handshake）

TCP建立连接必须通过三个步骤来同步ISC（Initial Sequence Number，初始序号）并确认双方存活  

1. 第一步（Client->Server）：  
    客户端发送 **SYN** Segment  
    包含随机生成初始SeqNum x  
    （SYN=1, SeqNum=x）  
    从LISTEN状态进入SYN-SENT状态  

2. 第二步（Server->Client）：  
    服务器收到SYN后  
    回复 **SYNACK** Segment  
    包含自己的初始SeqNum y和确认号 x+1  
    （SYN=1, ACK=1, SeqNum=y, AckNum=x+1）  
    从SYN-SENT状态进入SYN-RCVD状态  

3. 第三步（Client->Server）：  
    客户端发送 **ACK** Segment  
    确认服务器序号（AckNum=y+1）  
    此时开始可以携带Application Layer Data  
    （ACK=1, SeqNum=x+1, AckNum=y+1）  
    Client进入ESTABLISHED状态  
    服务器收到ACK后进入ESTABLISHED状态  

简单理解，第一步时Client告诉Server要发起连接（SYN）  
如果Server同意，就第二步回复SYNACK（此时Server方还是半连接状态）  
如果Client收到SYNACK同意连接，就第三步回复ACK（此时Client方连接建立）  
第三步的时候，已经可以携带数据了  

细节理解：  

- **ISN（Initial Sequence Number，初始序号）**  
    ISN随机化，也就是初始序号随机选择  
    防止来自旧连接（incarnations）的延迟Packets干扰当前连接  
- **为什么不能只有2次握手（Two-Way Handshake）**  
    网络存在可变延迟和Segment重传  
    2次握手可能导致“半开连接（Half-Open Connection，即no clients）”问题  
    或者重复数据问题  
    （例如Server收到已失效的连接请求并错误开启资源）  

#### 5.5.2. 连接终止：四次挥手（Four-Way Handshake）

TCP连接是全双工的，每一方都必须独立关闭自己的发送通道  

以下不分Client和Server，只分主动方和被动方（指关闭连接的一方和另一方）  

1. （主动方）发送FIN  
    主动方发送FIN Segment  
    （FIN=1, SeqNum=x）  
    进入FIN_WAIT_1状态，表示不在发送数据但仍可接收数据  
2. （主动方）接收ACK  
    被动方收到FIN后  
    回复ACK Segment，并进入CLOSE_WAIT状态  
    （ACK=1, AckNum=x+1）  
    主动方接收ACK后进入FIN_WAIT_2状态，等待被动方关闭连接  
    *因为可能还有没处理完的数据等*  
3. （被动方）发送FIN  
    被动关闭方处理完所有待发数据后，发送FIN Segment  
    （FIN=1, SeqNum=y）  
    然后进入LAST_ACK状态，等待主动方确认
4. （主动方）最终确认与TIME_WAIT  
    主动方接收FIN后，发送ACK Segment  
    （ACK=1, AckNum=y+1）  
    进入TIME_WAIT状态，等待足够时间以确保被动方收到ACK  
    然后进入CLOSED状态，连接正式终止。

**TIME_WAIT状态**  
主动方在2个MSL（最大报文寿命，一般120s）后进入CLOSED  
为了：  

- 确保最后一个ACK成功送达  
    如果丢失，对方会重发FIN  
- 本让本次链接产生的所有延迟Packets（又称”迷途分组”）过期  
    防止干扰未来的连接  

## 6. Principles of Congestion Control（拥塞控制原理）

拥塞控制（Congestion Control）  

Congestion:  
简单来说，就是太多源太快发送了过多的数据到网络，以至于网络核心设备（例如路由器）无法处理  

manifestations（表现形式）：

- long delays（长时延）  
    在路由器缓冲排队  
- packet loss（分组丢失）
    路由器缓冲溢出  

### 6.1. Cost of Congestion（拥塞的代价）

三个场景：  

- Scenario 1：infinite buffer  
    Packages达到速率接近或超过链路速率R时
    （注意应用层输入等于输出，而传输层还有丢失重传等开销）  
    Router缓冲区会不断增长  
    那么排队时延可能趋向无穷大  
- Scenario 2：finite buffer and retransmission  
    package loss导致sender必须进行retransit  
    增加了工作量  
    Sender也可能因为Timeout过早而发送不必要的重复副本  
    进一步浪费链路带宽  
- Scenario 3：多跳路径（multiple-hop paths）  
    一个Package在下游被丢弃时  
    该Package在之前所有跳段所耗费的资源都被浪费了  

### 6.2. Approaches to Congestion Control（拥塞控制方法）

主要两大类：  

1. End-to-End Congestion Control（端到端拥塞控制）  

2. Network-Assisted Congestion Control（网络辅助拥塞控制）  

#### 6.2.1. End-to-End Congestion Control（端到端拥塞控制）

- 网络层不向传输层提供显式的拥塞反馈信息  
- 终端系统通过观察Package Loss（Timeout或Duplicate ACKs）和延迟变化来推断网络拥塞状态，并调整发送速率  
- 这是TCP采用的主要方法  

#### 6.2.2. Network-Assisted Congestion Control（网络辅助拥塞控制）

- ECN（Explicit Congestion Notification，显式拥塞通知）  
    Router通过在IP Datagram Header的ToS（Type of Service）字段中设置特定比特位来指示拥塞  
- ATM协议ABR服务  
    ATM（Asynchronous Transfer Mode，异步传输模式）  
    ABR（Available Bit Rate，可用比特率）  
    交换机可以明确告知源端其允许的发送速率  
- 这些方法需要网络设备的支持和协作  

## 7. TCP Congestion Control（TCP拥塞控制）

### 7.1. 核心思想：Self-Clocking与AIMD

**Self-Clocking（自时钟机制）**  
**AIMD（Additive Increase Multiplicative Decrease，增量增加乘法减少）**  

TCP Congestion Control是Self-Clocking的  
也就是说利用ACK的到达作为信号来触发新Package的发送  
基本策略是AIMD  

**加性增** 和 **乘性减** 机制  

- **加性增**：当网络未拥塞时，逐步增加发送速率  
    具体来说，每经过一个RTT，若未发生丢包  
    将 拥塞窗口cwnd 增加1个MSS（最大报文段长度，maximum segment size）  
    线性增长探测带宽  
- **乘性减**：当检测到拥塞时，迅速减少发送速率  
    一旦检测到丢包（可能专指3 Duplicate ACKs），拥塞窗口cwnd将减半  
    （Timeout触发可能直接将cwnd减小到1个MSS）

整体上cwnd曲线会因为这种“慢增快减”而呈现锯齿形态（Sawtooth Pattern）  
其也被证明为是保证系统稳定的必要条件  

### 7.2. cwnd的演进阶段

TCP使用 cwnd 变量来限制发送方的in-flight数据量  
运行关键阶段：  

1. Slow Start（慢启动）  
    连接建立初，cwnd从1MSS开始  
    每收到一个ACK，cwnd增加1MSS  
    因而每经过一个RTT，cwnd翻倍增长  
    实际上增长整体是呈指数级的  
    旨在快速填满网络带宽  
2. Congestion Avoidance（拥塞避免）  
    cwnd 达到慢启动阈值 ssthresh 后  
    增长策略转为线性增长（即AI）  
3. Fast Recovery（快速恢复）  
    状态转换逻辑：  
    发生丢包时，ssthresh被设置为当前 cwnd 的一半  
    然后进入 Fast Recovery 阶段（具体逻辑见下一节）  

### 7.3. 丢包检测与恢复机制

TCP根据丢包信号的严重程度采取不同的响应：  

- Timeout  
    视为严重拥塞信号  
    cwnd 重置为 1 MSS  
    并重新开始 Slow Start 阶段（TCP Tahoe）  
- Fast Retransmit/Recovery  
    如果收到3个冗余ACK，说明网络还有传输功能，只是个别丢失  
    视为轻微拥塞信号  
    TCP Reno 跳过SS，直接将cwnd减半后进入CA阶段  

### 7.4. 现代演进与变体

- TCP CUBIC  
    Linux默认算法  
    使用三次函数调整cwnd  
    远离上次丢包点时快速增，接近时缓速增  
    更适合长肥网络（Long Fat Networks，LFN，即带宽大延迟长的网络）  
- 基于延迟的控制（Vegas & BBR）  
    不再仅仅以来丢包，通过检测RTT微小变化来提前规避拥塞  
    Vegas：通过RTT变化调整cwnd  
    BBR（Bottleneck Bandwidth and Round-trip propagation time）：通过估计瓶颈带宽和RTT来动态调整发送速率，最大化吞吐量同时最小化延迟。  
- ECN（Explicit Congestion Notification，显式拥塞通知）  
    允许路由器显式通知（设置IP包头部中特定部位）发送方网络拥塞状态  
    发送方据此调整cwnd，而非仅依赖丢包信号  

### 7.5. 公平性  

TCP目标为让K条通过同一瓶颈链路的连接公平分享带宽R/K  
实际上可以通过开多条连接来“占用”更多带宽  
