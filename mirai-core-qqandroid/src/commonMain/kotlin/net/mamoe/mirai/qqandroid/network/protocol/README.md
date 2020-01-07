# QQAndroid Protocol

## Overview

Note: `head` and `body` functions do nothing. They just work as
notations

PseudoCode:
```
OutgoingPacket { 
    int head.size + body.size + 4
    head {
        int      0x0A
        byte     0x02
        int      extra data size + 4
        byte[]   extra data // initially={}
        byte     0
        int      uinAccount.length + 4
        byte[]   uinAccount // =qqNumber.toString()
    } 
    body { // encrypted by `ByteArray(16)` when login, after which by sessionKey
        SSOPacket {
            int head.size + 4
            head {
                int      sequenceId
                int      subAppId
                int      subAppId
                hex      "01 00 00 00 00 00 00 00 00 00 01 00" // unknown values
                int      extraData.size + 4
                byte[]   extraData
                int      commandName.length + 4
                byte[]   commandName
                int      4 + 4
                int      0x02B05B8B
                int      imei.length + 4
                byte[]   imei
                int      0 + 4
                int      ksid.length + 4
                byte[]   ksid
                int      0 + 4
            }
            
            int body.size + 4
            body {
                OicqRequestPacket {
                    head {
                        byte     2 // head flag
                        short    27 + 2 + remaining.length
                        ushort   client.protocolVersion // const 8001
                        ushort   0x0001
                        uint     client.account.id
                        byte     3 // const
                        ubyte    encryptMethod.value // [EncryptMethod]
                        byte     0 // const
                        int      2 // const
                        int      client.appClientVersion
                        int      0 // const
                    }
                    
                    body {
                        // only write one of the following two structures!!
                    
                        // if encryption method is ECDH
                        EncryptionMethodECDH {
                            head {
                                byte     1
                                byte     1
                                byte[]   [ECDH.privateKey]
                                short    258
                                short    [ECDH.publicKey].size // always 49
                                byte[]   [ECDH.publicKey]
                            }
                            
                            body {
                                // real body
                            }
                        }
                        
                        // if encryption method is SessionKey
                        EncryptionMethodSessionKey {
                            head {
                                byte     1
                                byte     if (currentLoginState == 2) 3 else 2
                                fully    key
                                short    258
                                short    0
                            }
                            
                            body {
                                // real body
                            }
                        }
                    }
                    tail {
                        byte     3 // tail flag
                    }
                }
            }
        }
    }
}
```

## Packet bodies

### LoginPacket - SubCommand 9
**TO BE UPDATED**

PseudoCode:
```
short 9 // subCommand
tlvList {
    
}
```
