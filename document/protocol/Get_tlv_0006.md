# TIM Protocol

## Get_tlv_0006

C 构建包, 近 C 使用

#### Var
type | var name | value/from
---- | ---|---
?bytes | MD51 | md5(raw password)
?bytes | MD52 | md5((MD51 ＋ “ 00 00 00 00 ” ＋ g_QQ).hextobytes())
4bytes |m_loginIP | 服务器提供(Dispose_0825)
16bytes| m_tgtgtKey| |
#### Packet data

type | value
---- | ---
int | random   
hex |00 02  
int |qq  
hex |#_0825data2  
hex |00 00 01  
bytes|MD51  
int |m_loginTime  
byte | 0
bytes | 12 zero
int|m_loginIP
bytes | 8 zero
hex | 00 10
hex | 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B
bytes | m_tgtgtKey

TEA加密以上, key=MD52