# TIM Protocol

## Touch

C -> S

### Var
type | var name | value/from
---- | ---|---
int |g_qq | qq number
int| g_server| server ip 
### Packet data

type | value
---- | ---
hex | #head   
hex | #ver
int | 08 25 31 01  
int | g_qq
hex |#fixVer  
hex |#_0825key 
?bytes |TEA加密1 
hex |#tail 


TEA加密1, key = #_0825key:  

type | value
---- | ---
hex | #_0825data0
hex | #_0825data2
int | g_qq
hex | 00 00 00 00 03 09 00 08 00 01 
int | g_server
hex | 00 02 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 02 00 19
hex | #publicKey

TEA加密以上, key=MD52