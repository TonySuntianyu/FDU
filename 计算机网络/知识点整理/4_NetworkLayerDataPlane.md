# 网络层：数据平面

网络层分为数据平面和控制平面  
核心任务是将 **数据报（datagram）** 从 **发送主机** 通过路径传输到 **接收主机**  

## 1. 网络层概览

### 1.1. 核心功能

总共两个：  

- **转发（forwarding）**  
    将packets从routers的输入链路接口转发到适当的输出链路接口的本地动作  
    类似于通过单个交叉路口的过程  
- **路由（routing）**  
    决定分组从源（Src）到目的地（Dst）所采取路径的网络范围逻辑  
    类似于规划从起点到终点的整个旅程  

### 1.2. 两个平面

- **数据平面（Data Plane）**  
    - 属于本地，在每个路由器上执行  
    - 决定了router输入端口的datagram如何被转发到输出端口  
    - 主要由硬件实现，处理时间在ns级别  
- **控制平面（Control Plane）**  
    - 属于网络范围的逻辑  
    - 决定了datagram在host-to-host路径上所经过的路由器之间如何进行路由  
    - 主要由软件实现，处理时间在ms级别  
    - 实现方式：传统路由算法（实现在路由器内部）和软件定义网络（SDN，远程集中式控制器实现）  

### 1.3. 服务模型

Internet网络层提供  
**Best Effort** 服务模型  

- 无保证项目  
    不保证成功交付  
    不保证交付延时  
    不保证分组按序交付  
    也不保证最小带宽  
- 设计哲学  
    虽然没有任何保证  
    但是机制的简单性促成了Internet的广泛采用和部署  
    这些问题可以通过提供充足的带宽和在应用层进行的拥塞控制来缓解  
    使得模型在实践中运作良好  

### 1.4. 路由器宏观结构

路由器时数据平面的物理实现  
基本架构：  

1. **输入端口（Input Port）**  
    执行物理接收、链路处理、根据转发表查找输出端口  
2. **交换结构（Switching Fabric）**  
    将分组从输入端口传输到输出端口的核心  
3. **输出端口（Output Port）**  
    缓存从交换结构收到的分组并传输到链路上  
4. **路由处理器（Routing Processor）**  
    执行控制平面功能  
    例如运行路由协议和维护路由表  

## 2. 路由器架构（Router Architecture）

总结见上

### 2.1. 输入端口

路由器输入端口是路由器架构中执行数据平面（Data Plane）功能的关键组件  

#### 2.1.1. 三层功能结构

输入端口从物理链路接收bit stream，并逐层处理至网络层逻辑  

1. **物理层（线路端接，line termination）**  
    执行bit-level接收功能  
2. **数据链路层（协议处理，protocol processing）**  
    执行解封装（decapsulation），例如以太网协议（Ethernet）  
    接受链路层Frame，提取出datagram  
3. **网络层（查找与转发，lookup and forwarding）**  
    最核心的功能！  
    逻辑：”匹配+动作“（Match Plus Action）  
    根据datagram header在转发表中查找对应的输出端口  

#### 2.1.2. 查找机制与去中心化交换

**去中心化查找**  
（Decentralized Lookup）  
每个输入端口都有转发表的Shader Copy（影子副本）  
查找过程独立完成  
无需每次都询问路由处理器  
旨在达成”线速率转发“（Line-Rate Forwarding）  

**最长前缀匹配**  
（Longest Prefix Match）  
在基于目的地的转发中，如果匹配到多个entry（条目）  
路由器选择前缀最长的那个进行转发  

**硬件加速**  
（TCAM，Ternary Content Addressable Memory）  
现代路由器通常使用三态内容寻址存储器（TCAM）来加速查找过程  
支持通配符匹配  

##### 2.1.2.1. LPM具体介绍

当为给定的目的 IP 地址查找转发表条目时，路由器会选择与该地址匹配的最长地址前缀所对应的链路接口  
在采用 CIDR（无类别域间路由）的网络中，地址前缀长度可变且可能重叠。例如，一个地址可能同时匹配一个 16 位前缀和一个 24 位前缀，此时必须选择更具体的 24 位匹配项  
为了避免低效的线性搜索，实际应用中常使用 Trie（字典树） 或 PATRICIA tree 等数据结构来加速查找过程  
且硬件方面加速  
通过 LPM，互联网能够实现高效的路由聚合（Route Aggregation），从而缩小全局转发表的规模  

#### 2.1.3. 输入排队与性能挑战

Switching Fabric的处理速度低于输入端口的到达总速率时  
可能产生现象：  

- **输入排队**  
    datagram在输入端口buffer中排队  
    如果溢出则会丢包  
- **队头阻塞（HOL blocking，Head-of-Line Blocking）**  
    输入排队特有问题  
    排在队列首部的datagram因为目标输出端口忙碌而等待  
    这会阻塞后续datagram（即使他们的目标输出端口是空闲的）  

### 2.2. 交换结构（Switching Fabric）

连接输入端口到输出端口的核心组件  
负责将分组从输入链路实际移动到适当的输出链路  

#### 2.2.1. 主要类型

三种主要类型：  

- **经内存交换**  
    （Switching via Memory）  
    - 早期路由器本质是个普通计算机，交换过程直接由CPU控制  
    - Packets被copy到系统内存中，CPU提取目的地址然后复制到输出端口的buffer  
    - 瓶颈：内存带宽限制  
- **经总线交换**  
    （Switching via Bus）  
    - 无序CPU干预，输入端口通过一条共享bus直接将包传送至输出端口
    - 任何给定时间内只能有一个输入端口使用总线（即只能有一个包通过总线）  
    - 瓶颈：总线带宽限制  
- **经互联网络交换**  
    （Switching via Interconnection Network）  
    - 使用复杂互联矩阵  
    - 优势：支持并行交换，多个分组可以同时通过不同的路径传送  

#### 2.2.2. 交换结构的拥塞问题

导致在输入端口中分析过的问题  
此外输出端口也可能拥塞，在对应章节继续讨论  

### 2.3. 输出端口

路由器的输出端口主要负责将交换结构传来的分组进行缓存处理  
并通过特定的调度算法将其发送到输出链路上  

调度算法（如 WFQ）和丢弃策略（如 RED）共同决定了不同应用流的延迟和吞吐量表现

#### 2.3.1. 核心功能组件

硬件和逻辑组件  

- **排队与缓存**  
    （Queuing and Buffering）  
    Switching Fabric将Packets交付给输出端口的速度快于链路传输速率时  
    Packets必须在缓存中排队等待  
- **数据链路层处理**  
    （Data Link Layer Processing）  
    输出端口执行链路层封装（encapsulation）  
    例如以太网帧封装  
- **线路端接**  
    （Line Termination）  
    负责物理层的bit-level传输  
    将数字信号转换为模拟信号发送到链路上  

#### 2.3.2. 关键机制：排队与丢弃

Buffer有限  
输出端口必须管理队列  

- **排队延时与丢包**  
    到达速率持续超过输出链路容量  
    队列会增长导致时延增加  
    缓存填满后，后续到达的Packets会被丢弃（Packet Loss）
- **丢弃策略**  
    （Drop Policies）  
    - **尾部丢弃（Tail Drop）**  
        最简单的丢弃策略  
        当buffer满时，丢弃新到达的Packets  
    - **随机早期检测（RED）**  
        缓存完全填满之前，按一定概率随机丢弃Packets  
        以向发送端提前发送拥塞信号  
- **拥塞标记**  
    例如ECN机制（Explicit Congestion Notification）  
    路由器不再丢弃Packets  
    而是在分组header设置标记通知终端系统发生了拥塞  

#### 2.3.3. 调度算法

Scheduling  
调度决定了下一个发送那个分组  
这直接关系到QoS（Quality of Service，服务质量）  

- **FCFS/FIFO（先进先出）**  
    按照顺序发送  
    不区分流量类别  
- **优先级调度（Priority Scheduling）**  
    优先发送高优先级队列中的分组  
    只有其为空时才发送低优先级队列中的分组  
- **循环调度（Round Robin）**  
    轮流从不同类别的队列中提取分组发送  
    确保每一类流量都能获得发送机会  
- **加权公平排队（Weighted Fair Queuing，WFQ）**  
    循环调度的推广，为不同类别分配权重，保证每类流量获得固定比例的链路带宽  

### 2.4. 路由处理器

主要负责控制平面的功能  
此处不做过多展开  

## 3. IP（Internet Protocol）

IP 协议（Internet Protocol）是网络层数据平面的核心协议  

### 3.1. IP Datagram Format

IP数据报格式  

#### 3.1.1. IPv4 Datagram Format

Header: 一般20Bytes  
字段按32bit（4Bytes）对齐以便于软件处理  

**Version（版本）**  
4bits，固定为4  

**HLen（头部长度）**
4bits，以32位字（4Bytes）为单位表示头部长度  
无Options为5  

**TOS/DiffServ（Type of Service，服务类型）**
8bits，用于区分不同的应用的需求  
（例如low delay，high reliability等）  
包括差分服务（DiffServ）字段  
和显式拥塞通知（ECN）字段  

**Length（总长度）**
16bits，整个数据包（header+data）的长度，以Bytes为单位表示  
最大为65535Bytes  

**分片相关字段（Identifier，Flags，Fragment Offset）**  
总共占32bits  
用于在路径MTU（Maximum Transmission Unit）较小时对大分组进行分片和重组（Fragmentation and Reassembly）  
其中Offset（偏移量）以8Bytes为单位计算，表示分片在原始数据报偏移的位置  

**TTL（Time to Live，生存时间）**
8bits，防止分组再环路中无限传输  
经过一个路由器后，TTL值减1，当TTL为0时，分组被丢弃  

**Protocol（上层协议）**
8bits，标识数据应该交付的高层协议  
（例如TCP为6，UDP为17）  

**Header Checksum（头部校验和）**  
16bits，仅对header部分进行错误检测（注意，不包括data部分）  

**Source/Destination IP Address（源/目的IP地址）**  
各32bits，标志全球唯一的主机地址  

**Options（可选字段）**  
0-40Bytes，可选字段，提供额外功能，如时间戳、路由记录等  

#### 3.1.2. IP fragmentation/reassembly

IP分片与重组  
网络层为了处理不同链路技术对数据包大小限制（MTU）不一致而设计的机制  

##### 3.1.2.1. 核心触发机制

核心触发条件：MTU  
MTU（Maximum Transmission Unit，最大传输单元）  
每种类型的链路层协议都有其能承载的最大Frame大小  

触发场景：  
当路由器接收到的 IP 数据报大小超过了输出链路的 MTU 时，就必须将其“分段”成多个较小的 IP 数据报（即分片）才能发送  

##### 3.1.2.2. 关键header字段

IPv4 Header使用了三个关键字段  

1. **Identifier（标识符）**
    同一原始数据报的所有分片都具有相同的 ID，以便接收方识别哪些分片属于同一组  
2. **Flags（标志位）**  
    其中的 M 位（More fragments） 极为重要。M=1 表示后续还有分片；M=0 表示该分片是最后一个  
3. **Fragment Offset（片偏移量）**  
    标识该分片在原始数据报中的起始位置。注意： 该字段以 8 字节为单位进行计数（因为 Offset 字段只有 13 位，必须通过 8 字节对齐来支持 65535 字节的最大长度）  

##### 3.1.2.3. 分片与重组过程

分片地点：分片可以发生在源主机或路径上的任何路由器  
重组地点：重组仅在目的主机进行，中间路由器不负责重组，以减轻路由器的处理负担并提高转发效率  
异常处理：如果某一个分片在传输中丢失，接收方将无法完成重组，最终会丢弃已收到的所有相关分片  

##### 3.1.2.4. 注意

为什么现代网络尽量避免分片？  
性能损耗：分片和重组会消耗主机的计算和内存资源，且增加丢包率（丢一挂全）。  
IPv6 的改进：在 IPv6 中，路由器不再执行分片，若包太大则直接丢弃并返回 ICMP 消息，强制源主机执行路径 MTU 发现（Path MTU Discovery）  

### 3.2. IP Addressing（IP编址）

IP 编址（IP Addressing）是网络层的核心机制，用于唯一标识连接到网络上的接口  

#### 3.2.1. IP Address基本概念与结构  

定义：  
IP Address  
一个32bits的标识符  
不直接分配给主机或路由器  
而是分配给连接到网络的每个接口（Interface）  

接口性质：  
路由器通常有多个接口  
而主机通常有1到2个接口  
（例如有线Ethernet和无线WiFi）  

分层结构：  
IP地址分为两部分  

- **子网部分（Subnet）**  
    由高位比特组成  
    同一子网内的设备该部分相同  
- **主机部分（Host）**  
    由低位比特组成  
    标识子网内的具体设备  

#### 3.2.2. 子网划分与CIDR

**子网（Subnet）**  
指设备接口在物理上能够直接互相达到（无需通过路由器中转）的孤立网络  

**子网掩码（Subnet Mask）**  
用于定义子网部分的比特长度（如`/24`表示前24bits为子网号部分）  

**CIDR（Classless Inter-Domain Routing，无类别域间路由）**  
取代早期的A/B/C类地址划分  
采用`a.b.c.d/n`的表示法  
其中`n`表示子网部分的比特长度  
允许更灵活的地址分配  

#### 3.2.3. 地址获取机制

静态配置（Static Configuration）  
管理员在配置文件中手动设置IP地址  

**DHCP（Dynamic Host Configuration Protocol，动态主机配置协议）**  
实现“即插即用”  
主机向网络广播（broadcast）DHCP discover消息  
服务器回应DHCP offer消息  
包含：IP Address，第一跳路由地址，DNS服务器地址和子网掩码等信息  

网络号获取  
机构通常从其ISP（Internet Service Provider，互联网服务提供商）获取一个或多个网络号（Network Prefix）  
即一块连续的IP地址空间  

#### 3.2.4. 地址解析

**ARP协议（Address Resolution Protocol，地址解析协议）**  
用于将IP地址翻译为链路层的MAC地址（Media Access Control Address，48bits）  
发送端在LAN内广播ARP请求（LAN， Local Area Network）  
目标主机返回其MAC地址  
结果会被缓存在ARP表中以供后续使用  

**路由聚合（Route Aggregation）**  
分层编址允许ISP向外仅通告一条短前缀路由（例如`200.23.16.0/20`）  
即可以代表其下属的多个长前缀子网（例如`/23`或`/24`）  
从而缩短全局转发表规模  

### 3.3. NAT

**NAT（Network Address Translation，网络地址转换）**  
是 IP 协议中用于缓解 IPv4 地址枯竭、增强本地网络灵活性和安全性的关键技术  

#### 3.3.1. NAT的核心动机与定义

缓解地址枯竭：由于 IPv4 的 32 位地址空间有限，NAT 允许整个本地网络（如家庭网络）中的所有设备在外部看来仅共享一个全球唯一的 IPv4 地址  

私有地址空间：本地网络内部的设备使用“私有” IP 地址空间（如 10.0.0/24），这些地址仅在局域网内有效，不能在 Internet 中直接路由  

#### 3.3.2. NAT实现机制

**匹配+动作（Match+Action）**  
NAT 路由器通过透明地转换数据报的首部字段来工作  

**外出数据报（Outgoing）：**  
路由器将所有离开本地网络的 IP 数据报的（源 IP 地址，源端口号）替换为（NAT 公网 IP 地址，新的端口号）

**维护转换表（NAT Translation Table）**  
路由器会在内存中维护一张转换表，记录每一对“内部私有地址/端口”与“外部公网地址/新的端口”的映射关系

**进入数据报（Incoming）**  
当响应报文到达路由器的公网 IP 和对应端口时，路由器根据转换表，将目的地址和目的端口号替换回原始的内部私有地址和端口号

#### 3.3.3. 主要优势

节省成本：  
只需要从ISP申请一个公网IP Address就可以供多台设备上网  

管理灵活性：  
更改本地网络内部设备的IP Address无序通知外部世界  
同时，更换ISP时也无序更改局域网内部配置  

安全性：  
局域网内部设备对外不可见，且不能被外部世界直接寻址  
起到了一定的保护作用  

#### 3.3.4. 争议与局限性

- 违反host-to-host principle  
    NAT涉及针对传输层端口号的修改，这被认为违反了网络层仅处理3层信息的原则，属于“层次违反”（Layer Violation）  
- NAT穿越问题  
    如果外部客户端想要主动连接NAT内部服务器，会遇到障碍，需要特殊的穿越技术  
- IPsec兼容性问题  
    NAT与IPsec的ESP（Encapsulating Security Payload）传输模式不兼容（NAT无法在加密情况下更新TCP/UDP校验和），不过在”隧道模式”下可以工作  

## 4. IPv6

IPv6 的设计旨在解决 IPv4 的局限性并引入现代网络所需的新特性  

### 4.1. 设计动机与背景

核心动力：  
IPv4 的 32 位地址空间已在 2011 年左右耗尽，无法满足日益增长的设备（如物联网、无人机）需求  

设计目标：  
除了扩大地址空间，还旨在加快路由器的处理和转发速度，并更好地支持“流”（Flow）的分类处理  

### 4.2. IPv6 Address and Representation

空间大小：  
128bits，提供了约 3.4 x 10^38 个唯一地址  
号称“地球上的每一粒沙子都能分配一个IP地址”  

书写方式：  
一般采用十六进制书写，每16bits一组（最多4位十六进制数），用冒号分隔  

零压缩法：  
地址中连续的0可以用双冒号`::`表示，但只能使用一次以避免歧义  
e.g. `2001:0db8:0000:0000:0000:ff00:0042:8329` 可简写为 `2001:db8::ff00:42:8329`  

地址类型：  

- **单播地址（Unicast Address）**  
    标识单一接口，数据报发送到该地址时仅送达该接口  
    传统的点对点通信  
- **任播地址（Anycast Address）**  
    标识一组接口，数据报发送到该地址时会被路由到距离最近的接口  
    用于负载均衡和冗余  
    也就是发往一组接口中的“最近”一个
- **多播地址（Multicast Address）**  
    标识一组接口，数据报发送到该地址时会被送达该组中的所有接口  
    用于一对多通信，如视频会议  
- **本地链路地址（Link-Local Address）**  
    仅在当前物理网段内有效  

### 4.3. IPv6 Datagram Format

IPv6 数据报格式相较于 IPv4 有显著变化  
IPv6 采用了 **40 bytes固定长度** 的简化首部，旨在提高路由器的处理效率  

字段：  

**版本（Version）**  
4bits，固定为6  

**流量类别（Traffic Class）**  
用于标识分组优先级  
8bits，类似于IPv4的TOS字段  
也就是priority  

**流标签（Flow Label）**  
20bits，标识属于同一“流”的分组  

**有效载荷长度（Payload Length）**  
16bits，仅指首部之后的数据部分长度，不包括首部本身  

**下一个首部（Next Header）**  
8bits，
标识随后的扩展首部或者上层传输协议（如TCP/UDP）  

**跳数限制（Hop Limit）**  
8bits，类似于IPv4的TTL字段  

**源/目的地址（Source/Destination Address）**  
各128bits，标识源和目的接口的IPv6地址  

### 4.4. 关键改进与新特性

**取消Header校验和**  
为了加快转发，IPv6取消了头部校验和字段，即不再在网络层计算校验和  
交给传输层处理  

**禁止路由器分片**  
IPv6路由器不再执行分片  
Datagram过大直接丢弃并返回ICMP信息  
强制源主机执行路径MTU发现（Path MTU Discovery）以进行适配  

**扩展首部机制**  
Extension Headers  
Options被移出固定Header，而作为可选的扩展首部链  
从而提高了处理效率  

**无状态自动配置**  
（Stateless Address Autoconfiguration，SLAAC）  
主机可以结合接口ID（通常基于MAC地址）和路由器通告的前缀  
实现“即插即用”的地址分配（而无需DHCP服务器）  

### 4.5. 从IPv4到IPv6的过渡机制

由于无法一次性升级所有设备，而IPv4和IPv6不兼容  
因而采用了多种过渡机制  
下面列出两种主要方法：  

- **双栈（Dual Stack）**  
    节点同时运行IPv4和IPv6协议栈  
    根据目的地决定使用哪个协议进行通信  
- **隧道（Tunneling）**  
    将IPv6 Datagram作为Payload封装在IPv4 Datagram中传输  
    使其可以穿过仅支持IPv4的网络  

## 5. Generalized Forwarding: SDN

软件定义网络（SDN，Software Defined Networking）  
通用转发（Generalized Forwarding）的现代实现方式  
**通用转发（Generalized Forwarding）与软件定义网络（SDN）** 代表了从传统硬件集成架构向可编程架构的重大演进  

### 5.1. 通用转发的核心抽象：“匹配 + 动作”

通用转发不再局限于传统的“基于目的地的转发”，而是采用更灵活的 **“匹配 + 动作”（Match + Action）** 抽象模型  

“match plus action” abstraction  
匹配接收包中的bits，然后执行相应的动作（如转发、丢弃、修改等）  

传统：  
destination-based forwarding（基于目的地的转发）  
基于dest.IP address进行匹配和转发  

推广：  
Generalized Forwarding：  

- **匹配（Match）**  
    路由器可以匹配到达分组中不同层级的多个首部字段（包括链路层、网络层和传输层），甚至可以匹配入端口  
    许多Header Fields都能被用来决定Action  
- **动作（Action）**  
    不仅限于转发，还有例如drop/copy/modify/log packet等多种  

### 5.2. 重要概念：Flow Table（流表）

Flow（流）  
由header field values定义的一组分组  

Flow Table（流表）  
路由器维护一张由条目组成的表，每个条目包含匹配模式、操作集、优先级（用于解决匹配冲突）以及计数器（统计字节数和分组数）  

- match  
    Header fields中的特定模板值  
- actions  
    对于被匹配的packet  
    进行drop，forward，modify or send to controller等操作  
- priority  
    用于解决匹配冲突的优先级数值  
- counters  
    统计匹配该条目的packet和byte数量  

以上就是Flow Table Entry的组成部分（Match，Action，Stats）  
counters可以认为是第三部分（Stats）  

简单来说，Flow Table就是Router恢复传统转发表的更强大版本  
存储的是match到action的映射关系  
而非仅限于传统的仅匹配目标IP，然后绑定到输出端口的映射关系  

### 5.3. OpenFlow

OpenFlow 是软件定义网络（SDN）中的核心通信协议，它将网络控制平面与数据平面解耦，实现了网络流量的可编程化。  
是实现 SDN 架构的一种具体标准接口。它定义了远程控制器如何与交换机通信，以便计算并安装转发表（流表）到交换机中，从而实现对网络行为的逻辑集中控制  

核心：流表（Flow Table）  

#### 5.3.1. 通用转发的“统一抽象”

OpenFlow 通过“匹配+动作”的抽象，使得同一台交换设备可以通过配置不同的流表扮演多种角色  

- 路由器：匹配最长目的 IP 前缀，动作是转发。
- 二层交换机：匹配目的 MAC 地址，动作是转发或泛洪。
- 防火墙：匹配特定的端口号或 IP 地址，动作是丢弃（Drop）或允许（Allow）  
- NAT：匹配特定的 IP 和端口，动作是重写地址和端口字段  
