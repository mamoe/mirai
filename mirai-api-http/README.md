# mirai-api-http

<b>
Mirai-API-http provides adapter for ALL langugae to access mirai via HTTP protocol.<br>
</b>

**[中文](README_CH.md)**  
  

### Start Session-Authorize

```php
Path: /auth
Method: POST
```
this verify your session to one bot and you could have full access to that bot<br>
NOTE that only 1 bot could be control under 1 session, you could have multiple session to control all bots.

#### Request:<br>

|  name    | type | optional|example|note|
| --- | --- | --- | --- | --- |
| key  |  String |false|U9HSaDXl39ksd918273hU|MIRAI API HTTP key, this could be found after initialize|
| qq   |  String |false|1040400290|bot QQ number you want to access|

 
#### Response if success:<br>

|  name    | type | example|note|
| --- | --- | ---  | --- |
| success |Boolean |true|if this session is authorized|
| session |String |UANSHDKSLAOISN|your session key|


#### Response if failed:<br>

|  name    | type | example|note|
| --- | --- | ---  | --- |
| success |Boolean |false|if this session is authorized|
| session |String |null|your session key|
| error |int |0|error code|

#### Error:<br>

|  code    | reason|
| --- | --- |
| 0 | wrong MIRAI API HTTP key |
| 1 | unknown bot number |


 without session key, you are not able to access any method below.</br>
 session key should be attached to your <b>cookies</b> like this:
 
 |  name    | value |
 | --- | --- |
 | session |your session key here |
 
 if you were getting HTTP error code 403, you should ask for a new session key.