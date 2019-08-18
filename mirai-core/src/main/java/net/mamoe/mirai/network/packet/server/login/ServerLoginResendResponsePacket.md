
# ServerLoginResendResponsePacket

## Dispose_0836
data ＝ TeaDecrypt (取文本中间 (data, 43, 取文本长度 (data) － 45), #shareKey)
data ＝ TeaDecrypt (data, m_tgtgtKey)
.如果真 (data ≠ “”)
    _0836_tlv0006_encr ＝ 取文本中间 (data, 76, 359)
    token ＝ 选择 (flag ＝ “08 36 31 03”, 取文本中间 (data, 460, 167), m_00BaToken)  ' token
    m_tgtgtKey ＝ 取文本中间 (data, 16, 47)  ' tgtgtKey
    SetTips (“正在获取帐号信息...”, 9)
    .如果 (flag ＝ “08 36 31 03”)
        SendUdp (Construct_0836_686 (“31 04”, 假, token, _0836_tlv0006_encr))  ' 正常发送
    .否则
        SendUdp (Construct_0836_686 (“31 06”, 假, token, _0836_tlv0006_encr))  ' 第二次发送
    .如果结束

.如果真结束


## Construct_0836_686(
                      .参数 seq, 文本型
                      .参数 isVerify, 逻辑型, , 是否需要验证码登录
                      .参数 token, 文本型
                      .参数 tlv_0006_encr, 文本型, 可空)

fix1 ＝ “03 00 00 00 01 01 01 00 00 68 20 00 00 00 00 00 01 01 03 00 19 ”
fix2 ＝ “00 15 00 30 00 01 01 27 9B C7 F5 00 10 65 03 FD 8B 00 00 00 00 00 00 00 00 00 00 00 00 02 90 49 55 33 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B ”
qd ＝ “00 32 00 63 3E 00 63 02 04 03 06 02 00 04 00 52 D9 00 00 00 00 A9 58 3E 6D 6D 49 AA F6 A6 D9 33 0A E7 7E 36 84 03 01 00 00 68 20 15 8B 00 00 01 02 00 00 03 00 07 DF 00 0A 00 0C 00 01 00 04 00 03 00 04 20 5C 00 ” ＋ MD5_32 ＋ “68 ”  ' 修改

_0836key1 ＝ “EF 4A 36 6A 16 A8 E6 3D 2E EA BD 1F 98 C1 3C DA ”
PCName ＝ BytesToStr (Ansi转Utf8 (取主机名 ()))
PCName ＝ 取文本左边 (PCName, 取文本长度 (PCName) － 3)
MD51 ＝ 删尾空 (StrSplit (取数据摘要 (到字节集 (g_pass))))
MD52 ＝ 删尾空 (StrSplit (取数据摘要 (HexToBytes (MD51 ＋ “ 00 00 00 00 ” ＋ g_QQ))))
crc32_code ＝ GetRandomKey (16)
crc32_data ＝ 取Crc32 (crc32_code)
.如果真 (isVerify)
    tlv_0006_encr ＝ Get_tlv_0006 ()
.如果真结束
pack.Empty ()
pack.PutTag (“01 12”)
pack.PutLength (“00 38”)
pack.PutValue (m_0825token)
pack.PutTag (“03 0F”)
pack.putDwordLength (GetDataLength (PCName) ＋ 2)
pack.putDwordLength (GetDataLength (PCName))
pack.PutValue (PCName)
pack.PutFix (“00 05 00 06 00 02”)
pack.PutQQ ()
pack.PutTag (“00 06”)
pack.PutLength (“00 78”)
pack.PutValue (tlv_0006_encr)
pack.PutKey (fix2)
pack.PutTag (“00 1A”)
pack.PutLength (“00 40”)
pack.PutValue (TeaEncrypt (fix2, m_tgtgtKey))
pack.PutValue (#_0825data0)
pack.PutValue (#_0825date2)
pack.PutQQ ()
pack.PutZero (4)
pack.PutTag (“01 03”)
pack.PutLength (“00 14”)
pack.PutTag (“00 01”)
pack.PutLength (“00 10”)
pack.PutKey (“60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6”)
' ****************
' 多出来的167字节
pack.PutTag (“01 10”)
pack.PutLength (“00 3C”)
pack.PutTag (“00 01”)
pack.PutLength (“00 38”)
pack.PutValue (token)
' ****************
pack.PutTag (“03 12”)
pack.PutLength (“00 05”)
pack.PutValue (“01 00 00 00 01”)
pack.PutTag (“05 08”)
pack.PutLength (“00 05”)
pack.PutValue (“01 00 00 00 00”)
pack.PutTag (“03 13”)
pack.PutLength (“00 19”)
pack.PutByte (“01”)
pack.PutTag (“01 02”)
pack.PutLength (“00 10”)
pack.PutKey (“04 EA 78 D1 A4 FF CD CC 7C B8 D4 12 7D BB 03 AA”)  ' 两次0836包相同
pack.PutZero (3)
pack.PutByte (“00”)  ' 可能为00,0F,1F
pack.PutTag (“01 02”)
pack.PutLength (“00 62”)
pack.PutWord (“00 01”)
pack.PutKey (“04 EB B7 C1 86 F9 08 96 ED 56 84 AB 50 85 2E 48”)  ' 两次0836包不同
pack.PutLength (“00 38”)
pack.PutValue (“E9 AA 2B 4D 26 4C 76 18 FE 59 D5 A9 82 6A 0C 04 B4 49 50 D7 9B B1 FE 5D 97 54 8D 82 F3 22 C2 48 B9 C9 22 69 CA 78 AD 3E 2D E9 C9 DF A8 9E 7D 8C 8D 6B DF 4C D7 34 D0 D3”)
pack.PutLength (“00 14”)
pack.PutKey (crc32_code)
pack.PutDword (crc32_data)
调试输出 (pack.GetPacket ())
ret ＝ #head ＋ “37 13 08 36 ” ＋ seq ＋ “ ” ＋ g_QQ ＋ fix1 ＋ #publicKey ＋ “ 00 00 00 10 ” ＋ _0836key1 ＋ TeaEncrypt (pack.GetPacket (), #shareKey) ＋ #tail
返回 (ret)
