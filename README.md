# 环境
jre 1.7.8
jdk 7u80  
maven 3.0.5  
vsftpd-2.2.2-21.el6.x86_64  
nginx-1.10.2  

# v1.x
## 通用模块
* ServerResponse 消息响应类
  * 消息码/消息内容/传递数据
* 字段加入Json序列化
  * Const 常量类
  * 存放各子模块的常量值 、对应的描述信息、常用方法
  * 用户角色常量，防止用户纵向越权
* ResponseCode 消息码类
  * 存放各种服务消息状态代码-对应描述 及 方法
* TokenCache Token令牌缓存类
  * 存放UUID生成的token令牌
  * 防止横向越权的行为发生
* RedisPool
  * 存放Redis连接
  * 可以快速创建/释放连接，减少开销
* 测试通用模块

## 用户模块
* 功能
  * 登录/用户名验证/注册
  * 忘记密码/提交问题答案/重置密码
  * 获取用户信息/更新用户信息/退出登录
* 要点
  * 防止横向越权、纵向越权的安全漏洞
    * 设置用户角色防止纵向越权
    * UUID随机字符串吗，配合令牌缓存类防止横向越权
  * MD5明文加密 及 增加salt值
  * Guava缓存的使用
  * 高复用服务响应对象的设计思想及抽象封装
  * Mybatis-plugin使用
* 测试用户模块

## 分类管理模块
* 功能
  * 获取分类节点/增加分类节点/修改分类名字
  * 获取分类ID/递归子节点ID
* 要点
  * 如何设计及封装无限层级的树状数据结构
  * 递归算法的设计思想
  * 如何处理复杂对象排重
  * 重写 hashcode 和 equals 的注意事项
* 测试分类管理模块

## 商品模块
* 功能
  * 前台
    * 商品搜索/动态排序列表/商品详情
  * 后台
    * 商品列表/商品搜索/商品图片上传
    * 富文本上传/商品详情/商品上下架
    * 增加商品/更新商品
* 要点
  * FTP服务的对接及VSFTPD服务配置
  * SpringMVC文件上传
  * 流读取Properties配置文件
  * 抽象POJO/BO/VO对象之间的转换关系及解决思路
  * joda-time快速入门
  * 静态块
  * Mybatis-PageHelper高效准确地分页及动态排序
  * Mybatis对List遍历的实现方法
  * Mybatis对where语句的动态拼装的几个版本的演变
* 测试商品模块

## 购物车模块
* 功能
  * 加入商品/更新商品数/查询商品数
  * 购物车列表/移出商品
  * 单选/取消/全选
* 要点
  * 购物车模块的设计思想
  * 如何封装一个高服用购物车核心方法
  * 浮点型商业运算中丢失精度的问题
* 测试商品模块

## 收货地址模块
* 功能
  * 添加地址/删除地址/更新地址
  * 地址列表/地址分页/地址详情
* 要点
  * SpringMVC数据绑定中对象绑定
  * mybatis自动生成主键、配置和使用
  * 如何避免横向越权漏洞
* 测试收获地址模块

## 支付模块
* 功能
  * 支付宝沙箱/支付宝集成/支付回调
  * 查询支付状态
* 要点
  * 沙箱调试环境（买家账号测试、商家账号测试）
  * 支付宝扫码支付主业务流程
  * 支付宝官方文档
  * 支付宝扫码支付流程
  * 支付宝扫码支付重要字段
  * 支付宝扫码支付重要细节
  * 支付宝扫码支付对接技巧
  * 支付宝扫码支付官方Demo调试
  * 熟悉支付宝对接核心文档，调通支付宝支付功能官方Demo
  * 解析支付宝SDK对接源码
  * RSA1 和 RSA2 验证签名及加解密
  * 避免支付宝重复通知和数据校验
  * natapp外网穿透 和 tomcat remote debug
  * 生成二维码，并持久化到图片服务器
* 测试支付模块
  * 支付宝沙箱支付对接

## 订单模块
* 功能
  * 前台功能
    * 创建订单/商品信息/订单列表
    * 订单详情/取消订单 
  * 后台功能
    * 订单列表/订单搜索/订单详情
    * 订单发货
* 要点
  * 避免业务逻辑中横向越权和纵向越权等安全漏洞
  * 设计实用、安全、扩展性强大的常量枚举类
  * 订单号生成规则、订单严谨性判断
  * POJO 和 VO 之间的实际操练
  * mybatis批量插入
* 测试订单模块

## 辅助工具模块
* MD5 加密模块
  * 用于用户注册模块等必要的加密重要信息
* DateTime 日期时间辅助模块
  * 主要进行日期时间 和 字符串 之间的转换处理操作
  * 使用joda.time库
* FTP 模块
  * 负责 上传/下载 远程FTP服务器数据
  * 设置远程FTP相关配置信息 
* PropertiesUtil 配置文件读取初始化模块
  * 用于读取配置文件（如:FTP服务器配置信息，MYSQL服务器配置信息）并初始化为java数据对象
* BigDecimalUtil 商业BigDecimal数学精确计算类
  * BigDecimalUtil统一采用String类型构造器进行初始化
  * 避免因小数计算精度问题而导致支付宝对账失败
* RedisPoolUtil
  * Redis连接池，管理Jedis连接
* 辅助工具模块测试

# v2.x - 施工完成的部分
## Lombok导入
## Maven快速部署
## Nginx + Tomcat集群
## 集成Redis客户端Jedis
* Jedis API封装
## Redis连接池管理Jedis连接
* RedisPool
* RedisPoolUtil
## Jackson封装JsonUtil及调试
* Jackson ObjectMapper源码
## Cookie池封装
## SessionExpireFilter重置session有效期
## Redis对接用户session相关的模块
## Guava cache迁移到Redis缓存
## Redis分布式及保证分布式一致性
## SpringMVC全局异常处理