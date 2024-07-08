# 介绍
## libSocketCan SDK 概述

libSocketCan SDK 是一组控制 imx8m tpc_1xx 安卓设备 CAN 总线的 API 开发库，包括(配置、打开、关闭、发送和接收 CAN 数据帧)，SocketCan 仅支持 can0 接口的控制

libSocketCan SDK 为应用程序提供 API 模块，用于控制 CAN 进行各种操作，该 SDK 的软件栈如下图所示

![image-20240705172352230](https://github.com/AIM-Android/SocketCanSample/blob/master/images/image-20240705172352230.png)

libSocketCan API 主要包括两个部分

- libSocketCan.jar Java 方法库
- libSocketCan.so 本地方法库

# 用法

## SDK API 调用流程

我们导入成功 libSocketCan SDK 之后，就可以开始开发我们的 app，具体的使用流程如下图所示

![image-20240705115323975](https://github.com/AIM-Android/SocketCanSample/blob/master/images/image-20240705115323975.png)

## 在 Android app 中导入 SDK

要使用 application 访问 libSocketCan 的功能，您必须将 libSocketCan.jar 和 libSocketCan.so 导入到您的 AndroidStudio 工程中。对应的目录及配置如下图所示。

![image-20240705112254138](https://github.com/AIM-Android/SocketCanSample/blob/master/images/image-20240705112254138.png)



# API 接口说明

## 类

| **<a id="CanFrame">CanFrame</a>** | **public int id**<br>**public int len**<br>**public byte[] data** |
| :-------------------------------- | :----------------------------------------------------------- |
| **SocketCan**                     | *配置 can0 接口*<br>**public static void [configCan](#configCan)(Context context, String speed)**<br>*打开 can0 接口*<br>**public native static int [openCan](#openCan)(String canx)**<br>关闭 can0 接口<br>**public native static void [closeCan](#closeCan)**<br>发送数据帧<br>**public native static int [send](#send)(int canid, String canx, byte[] data)**<br>接收数据<br>**public native static CanFrame [recv](#recv)(CanFrame frame, String canx)** |

## 函数方法

#### <a id="configCan">configCan</a>

- Syntax

```java
public static void configCan(Context context, String speed)
```

- Description

配置 can0 接口

- Parameters
  - context：应用程序上下文
  - speed：用于配置 CAN 接口的速率，一般情况下可将速率设置为 100 000，250 000

- Returns

none

- Remarks

none

#### <a id="openCan">openCan</a>

- Syntax

```java
public native static int openCan(String canx)
```

- Description

打开 can0

- Parameters
  - canx：目标 can0 接口，**canx 仅支持 can0 接口**

- Returns

返回值大于 0 表示打开成功，否则失败

- Remarks

版本 SDK 仅支持对 can0 接口进行操作

#### <a id="closeCan">closeCan</a>

- Syntax

```java
public native static void closeCan()
```

- Description

关闭 can0

- Parameters

none

- Returns

none

- Remarks

调用此函数后，将停止 can frame 的数据接收

#### <a id="send">send</a>

- Syntax

```java
public native static int send(int canid, String canx, byte[] data)
```

- Description

发送数据帧至 can0

- Parameters
  - canid：int 整数
  - canx：发送数据帧至 can0 接口，仅支持 can0 接口
  - data：是一个byte 数组，具体使用时需将 String 数据转换为 byte 数组

- Returns

返回值大于 0 表示发送成功，否则失败

- Remarks

none

#### <a id="recv">recv</a>

- Syntax

```java
public native static CanFrame recv(CanFrame frame, String canx)
```

- Description

接受 can0 接口数据，在使用 recv 函数时，需要构造 [CanFrame](#CanFrame) 实例，并且设置 CanFrame.id (canid)

- Parameters
  - frame：[CanFrame](#CanFrame) 的实例，需要设置 frame.id (canid)
  - canx：指定从 can0 接口接收

- Returns

返回值是一个 [CanFrame](#CanFrame) 结构体，其中 frame.data 就是我们接收到的数据，frame.len 为数据的长度

- Remarks

需要对接收到的数据进行数据处理，将 byte 数组转换为 String 数据