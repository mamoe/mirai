# mirai-api-http

<b>
Mirai-API-http provides adapter for ALL langugae to access mirai via HTTP protocol.<br>
</b>
  

#### Start Session-Authorize

```php
Path: /auth
Method: POST
```
this verify your session to one bot and you could have full access to that bot<br>
NOTE that only 1 bot could be control under 1 session, you could have multiple session to control all bots.

|  name    | type | optional|example|note|
| --- | --- | --- | --- | --- |
| key  |  String |false|U9HSaDXl39ksd918273hU|this could be found after initialize|
| qq   |  String |false|1040400290|bot QQ number you want to access|

 
 
 