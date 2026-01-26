# 基础

## 互联网

定义

协议  
定义网络实体间交换的  

- 信息格式（语法）  
- 含义（语义）  
- 发送和接收信息时采取的动作  

### 网络边缘与核心

边缘：  
运行网络应用程序的主机（客户端和服务器）  

核心：  
由互联的路由器网络结构组成  
负责在端系统之间中继数据  

## 网络核心的两种交换方式

- 电路交换  
    circuit switching  
    复用技术  
    FDM（频分复用）  
    频率划分为多个信道  
    TDM（时分复用）  
    时间划分为多个时隙  

- 分组交换  
    packet switching  
    重要机制：  
    存储转发（Store-and-forward）  
    即数据包在被转发前必须完整接收  
    统计多路复用（Statistical multiplexing）  
    - 基于机制  
        存在现象：排队  
        且缓冲区有限，满了可能会丢包  

## 性能指标

1. 时延(delay)  

2. 丢包率(packet loss rate)  

3. 吞吐量(throughput)  
   也称为有效带宽(effective bandwidth)或数据传输率(data transfer rate)  
   指单位时间内成功传输的数据量，通常以比特每秒(bps)为单位  
   源到目的之间的比特传输速率  

### Delay

时延的组成部分：

- 处理时延(processing delay)  
    路由器检查数据包头部并决定将数据包转发到哪个输出链路所需的时间  

- 排队时延(queuing delay)  
    数据包在路由器输出链路的队列中等待传输所需的时间  

- 传播时延(propagation delay)  
    数据包在链路上传输所需的时间，取决于链路长度和信号传播速度  
    公式：$d / s$  
    其中，$d$为链路长度，$s$为信号传播速度  

- 传输时延(transmission delay)  
    将数据包的所有比特推送到链路所需的时间，取决于数据包大小和链路带宽  
    公式：$L / R$  
    其中，$L$为数据包大小（以比特为单位），$R$为链路带宽（以比特每秒为单位）  

## 协议分层与体系结构

分层：  
抽象实现模块化，层之间相互独立，便于系统更新维护  

### 模型

- OSI七层模型：  
    - Application Layer（应用层）
    - Presentation Layer（表示层）
    - Session Layer（会话层）
    - Transport Layer（传输层）
    - Network Layer（网络层）
    - Data Link Layer（数据链路层）
    - Physical Layer（物理层）
- TCP/IP五层模型：  
    互联网的实际标准  
    - Application Layer（应用层）
    - Transport Layer（传输层/UDP/TCP）
    - Network Layer（网络层/IP）
    - Data Link Layer（数据链路层）
    - Physical Layer（物理层）  

### 分层三大核心机制

#### 封装

数据从上层传递到下层时
每层会添加特定的Header（头部），也就是控制信息  
形成协议数据单元(PDU)  

具体来说：  
发送方在每一层将上层来信息（数据）（M）作为有效载荷（Payload）  
添加特定 **首部（Header）** 形成该层的 协议数据单元(PDU)  

传输层添加 $H_t$ 形成 段（Segment）  
网络层添加 $H_n$ 形成 分组（Packet）  
数据链路层添加 $H_d$ 形成 帧（Frame）  

#### 复用与解复用

Multiplexing and Demultiplexing  
原因：每层可能有多个协议实现  

Header中包含 **解复用键（Demultiplexing Key）**  
用于识别收到的数据应该传递给哪个上层实体（协议或应用程序）  

#### 接口

Interface  
每个协议定义了 **服务接口**  
即定义了本机操作，提供同机上层调用  

和 **对等接口**  
定义对等实体间交换报文的形式与意义  

### 总结

网络设计的关键是处理控制（信令）与数据（搬运）的关系  
即 **控制平面（Control Plane）** 与 **数据平面（Data Plane）** 的分离  

分层优缺点：  

- 优点：  
    模块化，便于维护和升级  
    某层实现的改变对于其他层透明  
- 缺点：  
    有时分层界限不清晰，出于性能考虑可能需要跨层设计（层间依赖）  
