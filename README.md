# GTIS-slove-repeat-submit
基于美团GTIS原理，解决重复提交的问题


## 基本原理
GTIS的实现思路是将每一个不同的业务操作赋予其唯一性。这个唯一性是通过对不同操作所对应的唯一的内容特性生成一个唯一的全局ID来实现的。基本原则为：相同的操作生成相同的全局ID；不同的操作生成不同的全局ID。
生成的全局ID需要存储在外部存储引擎中，数据库、Redis等等均可实现。其相应的key和value如下：

key：将对于不同的业务，采用APP_KEY+业务操作内容特性生成一个唯一标识trans_contents。然后对唯一标识进行加密生成全局ID作为Key。
value：current_timestamp + trans_contents，current_timestamp用于标识当前的操作线程。
判断是否重复，主要利用redis的SETNX方法，如果原来没有值则set且返回成功，如果已经有值则返回失败。

> 原理和实现略有差别

## 参考
> 1. 分布式系统互斥性与幂等性问题的分析与解决：https://tech.meituan.com/2016/09/29/distributed-system-mutually-exclusive-idempotence-cerberus-gtis.html
> 2. 【项目实战】Redis使用场景之防止重复提交 - 接口幂等性：https://blog.csdn.net/wstever/article/details/129469074