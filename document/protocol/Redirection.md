# TIM Protocol

## Redirection

### S ->  `08 25 31 02`(may be another)

#### Decryption

skip 14 bytes  
if (flag == "08 25 31 02")  
    data = decrypt (read bytes 14..length-1, #redirectionKey)  
else data = decrypt (read bytes 14..length-1, #_0825key)

#### Packet data - Requiring

**read byte == 0xFE**  
skip 94 bytes  
String serverIp = read 4 bytes and join them with separator "."

#### Packet data - Not Requiring

**read byte == 0x00**  
skip 4 bytes  
56bytes token0825 = read 56 bytes  
skip 6 bytes  
int loginTime = read int  
skip 1 byte
String loginIP = read 4 bytes and join them with separator "."
16bytes tgtgtKey = random 16 bytes

### C -> S - Requiring `08 25 31 02`

#### Var

type | var name | value/from
---- | ---|---
int | qq | |
String | server ip | from redirection packet

#### Packet data

type | value
---- | ---
hex |#head
hex |#ver
hex |08 25 31 02
int |qq
hex |#fixver
hex |#redirectionKey
bytes |[TEA encrypted data](#tea-encrypted-data)

##### TEA encrypted data
Key : #redirectionKey

type | value
---- | ---
hex |#_0825data0
hex |#_0825data2
int |qq
hex |00 01 00 00 03 09 00 0C 00 01
4bytes |g_server(split with "." and convert to byte)
hex |01 6F A1 58 22 01 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 03 00 19
hex | #publicKey

#### Note

Send the packet to new server via port 8000


### C -> S - Not Requiring(Submitting password) `08 36 31 03`

#### Var

type | var name | value/from
---- | ---|---
int | qq | |
String | password | |
String | device name | UTF8 encoding. Sample: DESKTOP-M19QRYU
16bytes | tgtgtKey | |
bytes | MD5_1 | md5(password)
bytes | MD5_2 | md5(MD5_1 + bytes{0, 0, 0, 0}} + qq.tobytes)

#### Packet data

type | value
---- | ---
hex |#head
hex |#ver
hex |08 36 31 03
int |qq
hex |03 00 00 00 01 01 01 00 00 68 20 00 00 00 00 00 01 01 03 00 19
hex |#publicKey
hex | 00 00 00 10
hex | EF 4A 36 6A 16 A8 E6 3D 2E EA BD 1F 98 C1 3C DA
bytes |[TEA encrypted  data](#tea-encrypted--data)

##### TEA encrypted  data
Key : #shareKey

type | value
---- | ---
hex |01 12
hex |00 38
int |token0825(from [Packet data - Not Requiring](#packet-data---not-requiring))
hex |03 0F
int | device name length + 2
int | device name length
bytes | device name
hex | 00 05 00 06 00 02
int | qq
hex | 00 06 00 78
bytes | [TLV0006](Get_tlv_0006.md) Using md5 that you just calculated in
hex | fix = 00 15 00 30 00 01 01 27 9B C7 F5 00 10 65 03 FD 8B 00 00 00 00 00 00 00 00 00 00 00 00 02 90 49 55 33 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B
hex | 00 1A 00 40
bytes | TEAEncrypt(fix, tgtgtKey)
hex | #_0825data0
hex | #_0825data2
int | qq
hex | 00 00 00 00
hex | 01 03 00 14 00 01 00 10 60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6 03 12 00 05 01 00 00 00 01 05 08 00 05 01 00 00 00 00 03 13 00 19 01 01 02 00 10 04 EA 78 D1 A4 FF CD CC 7C B8 D4 12 7D BB 03 AA
hex | 00 00 00 00
hex | 01 02 00 62 00 01 04 EB B7 C1 86 F9 08 96 ED 56 84 AB 50 85 2E 48 00 38 E9 AA 2B 4D 26 4C 76 18 FE 59 D5 A9 82 6A 0C 04 B4 49 50 D7 9B B1 FE 5D 97 54 8D 82 F3 22 C2 48 B9 C9 22 69 CA 78 AD 3E 2D E9 C9 DF A8 9E 7D 8C 8D 6B DF 4C D7 34 D0 D3 00 14
bytes | CRCKey = random 16
bytes | getCRC(CRCKey) //do it yourself
