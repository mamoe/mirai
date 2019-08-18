# TIM Protocol

## Redirection

S -> C

### Var
type | var name | value/from
---- | ---|---
int |g_qq | qq number
int| g_server| server ip 
### Packet data

skip 14
if (flag == "08 25 31 02")
    data = decrypt (read 14..length-1, #redirectionKey)
else 
    data = decrypt (read 14..length-1, #_0825key)
    
