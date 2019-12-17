# mirai-api-http

<b>
Mirai-API-http 提供HTTP API供所有语言使用mirai<br>
</b>
 
### 开始会话-认证(Authorize)

```php
路径: /auth
方法: POST
```
使用此方法验证你的会话连接, 并将这个会话绑定一个BOT<br>
注意: 每个会话只能绑定一个BOT.

#### 请求:<br>

|  名字    | 类型 | 可选 | 举例 | 说明 |
| --- | --- | --- | --- | --- |
| key  |  String |false|U9HSaDXl39ksd918273hU|MIRAI API HTTP key, HTTP API的核心key|
| qq   |  String |false|1040400290|需要绑定的BOT QQ号|

 
#### 返回(成功):<br>

|  名字    | 类型 | 举例 | 说明|
| --- | --- | ---  | --- |
| success |Boolean |true|是否验证成功|
| session |String |UANSHDKSLAOISN|你的session key|


#### 返回(失败):<br>

|  name    | type | example|note|
| --- | --- | ---  | --- |
| success |Boolean |false|是否验证成功|
| session |String |null|你的session key|
| error |int |0|错误码|

#### 错误码:<br>

|  代码    | 原因|
| --- | --- |
| 0 | 错误的MIRAI API HTTP key |
| 1 | 试图绑定不存在的bot|


 session key 是使用以下方法必须携带的</br>
 session key 需要被以cookie的形式上报 <b>cookies</b> :
 
 |  name    | value |
 | --- | --- |
 | session |your session key here |
 
如果出现HTTP 403错误码，代表session key已过期, 需要重新获取