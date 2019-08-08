#include <stdio.h>
#include <stdlib.h>
#include <memory.h>
#include <string.h>
#include <time.h>

//#define CRYPT_ONE_BYTE

typedef char int8 ;
typedef unsigned char uint8 ;
typedef short int16 ;
typedef unsigned short uint16 ;
typedef long int32 ;
typedef unsigned long uint32 ;

typedef struct tagTEACTX
{
	uint8 buf[8] ;
	uint8 bufPre[8] ;
	const uint8 *pKey ; //指向16字节的key
	uint8 *pCrypt ;
	uint8 *pCryptPre ;
} TEACTX, *LPTEACTX ;

uint16 Host2NetShort(uint16 usHost)
{
	const uint16 us = 0x1234 ;
	return ((uint8 *)&us)[0] == 0x12 ? usHost : ((usHost>>8) | (usHost<<8)) ;
}

uint16 Net2HostShort(uint16 usNet)
{
	return Host2NetShort(usNet) ;
}

uint32 Host2NetLong(uint32 ulHost)
{
	const uint16 us = 0x1234 ;
	return ((uint8 *)&us)[0] == 0x12 ? ulHost : (((ulHost>>8) & 0xFF00) |
		((ulHost<<8) & 0xFF0000) | (ulHost<<24) | (ulHost>>24)) ;
}

uint32 Net2HostLong(uint32 ulHost)
{
	return Host2NetLong(ulHost) ;
}

//TEA加密。v明文8字节。k密钥16字节。w密文输出8字节。
void EnCipher(const uint32 *const v, const uint32 *const k, uint32 *const w)
{
	register uint32
		y     = Host2NetLong(v[0]),
		z     = Host2NetLong(v[1]),
		a     = Host2NetLong(k[0]),
		b     = Host2NetLong(k[1]),
		c     = Host2NetLong(k[2]),
		d     = Host2NetLong(k[3]),
		n     = 0x10,       /* do encrypt 16 (0x10) times */
		sum   = 0,
		delta = 0x9E3779B9; /*  0x9E3779B9 - 0x100000000 = -0x61C88647 */

	while (n-- > 0)
	{
		sum += delta;
		y += ((z << 4) + a) ^ (z + sum) ^ ((z >> 5) + b);
		z += ((y << 4) + c) ^ (y + sum) ^ ((y >> 5) + d);
	}

	w[0] = Net2HostLong(y);
	w[1] = Net2HostLong(z);
}

//TEA解密。v密文8字节。k密钥16字节。w明文输出8字节。
void DeCipher(const uint32 *const v, const uint32 *const k, uint32 *const w)
{
	register uint32
		y     = Host2NetLong(v[0]),
		z     = Host2NetLong(v[1]),
		a     = Host2NetLong(k[0]),
		b     = Host2NetLong(k[1]),
		c     = Host2NetLong(k[2]),
		d     = Host2NetLong(k[3]),
		n     = 0x10,
		sum   = 0xE3779B90,
		/* why this ? must be related with n value*/
		delta = 0x9E3779B9;

	/* sum = delta<<5, in general sum = delta * n */
	while (n-- > 0)
	{
		z -= ((y << 4) + c) ^ (y + sum) ^ ((y >> 5) + d);
		y -= ((z << 4) + a) ^ (z + sum) ^ ((z >> 5) + b);
		sum -= delta;
	}

	w[0] = Net2HostLong(y);
	w[1] = Net2HostLong(z);
}

uint32 Random(void)
{
	return (uint32)rand();
	//return 0xdead ;
}

//每次8字节加密
static void EncryptEach8Bytes(TEACTX *pCtx)
{
#ifdef CRYPT_ONE_BYTE
	uint32 i ;
	uint8 *pPlain8, *pPlainPre8, *pCrypt8, *pCryptPre8 ;
	pPlain8 = (uint8 *)pCtx->buf ;
	pPlainPre8 = (uint8 *)pCtx->bufPre ;
	pCrypt8 = (uint8 *)pCtx->pCrypt ;
	pCryptPre8 = (uint8 *)pCtx->pCryptPre ;
	//本轮明文与上一轮的密文异或
	for(i=0; i<8; i++)
		pPlain8[i] ^= pCryptPre8[i] ;
	//再对异或后的明文加密
	EnCipher((uint32 *)pPlain8, (uint32 *)pCtx->pKey, (uint32 *)pCrypt8) ;
	//将加密后的密文与上一轮的明文(其实是上一轮明文与上上轮密文异或结果)异或
	for(i=0; i<8; i++)
		pCrypt8[i] ^= pPlainPre8[i] ;
	//
	for(i=0; i<8; i++)
		pPlainPre8[i] = pPlain8[i] ;
#else
	uint32 *pPlain8, *pPlainPre8, *pCrypt8, *pCryptPre8 ;
	pPlain8 = (uint32 *)pCtx->buf ;
	pPlainPre8 = (uint32 *)pCtx->bufPre ;
	pCrypt8 = (uint32 *)pCtx->pCrypt ;
	pCryptPre8 = (uint32 *)pCtx->pCryptPre ;
	pPlain8[0] ^= pCryptPre8[0] ;
	pPlain8[1] ^= pCryptPre8[1] ;
	EnCipher(pPlain8, (const uint32 *)pCtx->pKey, pCrypt8) ;
	pCrypt8[0] ^= pPlainPre8[0] ;
	pCrypt8[1] ^= pPlainPre8[1] ;
	pPlainPre8[0] = pPlain8[0] ;
	pPlainPre8[1] = pPlain8[1] ;
#endif
	pCtx->pCryptPre = pCtx->pCrypt ;
	pCtx->pCrypt += 8 ;
}

//加密。pPlain指向待加密的明文。ulPlainLen明文长度。pKey密钥16字节。
//pOut指向密文输出缓冲区。pOutLen输入输出参数，指示输出缓冲区长度、密文长度。
uint32 Encrypt(TEACTX *pCtx, const uint8 *pPlain, uint32 ulPlainLen,
	const uint8 *pKey, uint8 *pOut, uint32 *pOutLen)
{
	uint32 ulPos, ulPadding, ulOut ;
	const uint8 *p ;
	if(pPlain == NULL || ulPlainLen == 0 || pOutLen == NULL)
		return 0 ;
	//计算需要填充的字节数
	//整个加密流程下来，不管明文长度多少，填充10个字节是固定的，
	//然后再根据明文的长度计算还需要填充的字节数。
	ulPos = (8 - ((ulPlainLen + 10) & 0x07)) & 0x07 ;
	//计算加密后的长度
	ulOut = 1 + ulPos + 2 + ulPlainLen + 7 ;
	if(*pOutLen < ulOut)
	{
		*pOutLen = ulOut ;
		return 0 ;
	}
	*pOutLen = ulOut ;
	memset(pCtx, 0, sizeof(TEACTX)) ;
	pCtx->pCrypt = pOut ;
	pCtx->pCryptPre = pCtx->bufPre ;
	pCtx->pKey = pKey ;
	//buf[0]的最低3bit位等于所填充的长度
	pCtx->buf[0] = (uint8)((Random() & 0xF8) | ulPos) ;
	//用随机数填充上面计算得到的填充长度(每个字节填充的内容是一样的)。
	//这里填充的起始位置是&buf[1]。
	memset(pCtx->buf+1, (uint8)Random(), ulPos++) ;
	//至少再填充两字节
	for(ulPadding=0; ulPadding<2; ulPadding++)
	{
		if(ulPos == 8)
		{
			EncryptEach8Bytes(pCtx) ;
			ulPos = 0 ;
		}
		pCtx->buf[ulPos++] = (uint8)Random() ;
	}
	p = pPlain ;
	while(ulPlainLen > 0)
	{
		if(ulPos == 8)
		{
			EncryptEach8Bytes(pCtx) ;
			ulPos = 0 ;
		}
		pCtx->buf[ulPos++] = *(p++) ;
		ulPlainLen-- ;
	}
	//末尾再添加7字节0后加密，在解密过程的时候可以用来判断key是否正确。
	for(ulPadding=0; ulPadding<7; ulPadding++)
		pCtx->buf[ulPos++] = 0x00 ;
	//
	EncryptEach8Bytes(pCtx) ;
	return ulOut ;
}

//每次8字节进行解密
static void DecryptEach8Bytes(TEACTX *pCtx)
{
#ifdef CRYPT_ONE_BYTE
	uint32 i ;
	uint8 bufTemp[8] ;
	uint8 *pBuf8, *pBufPre8, *pCrypt8, *pCryptPre8 ;
	pBuf8 = (uint8 *)pCtx->buf ;
	pBufPre8 = (uint8 *)pCtx->bufPre ;
	pCrypt8 = (uint8 *)pCtx->pCrypt ;
	pCryptPre8 = (uint8 *)pCtx->pCryptPre ;
	//当前的密文与前一轮明文(实际是前一轮明文与前前轮密文异或结果)异或
	for(i=0; i<8; i++)
		bufTemp[i] = pCrypt8[i] ^ pBufPre8[i] ;
	//异或后的结果再解密(解密后得到当前名文与前一轮密文异或的结果，并非真正明文)
	DeCipher((uint32 *)bufTemp, (uint32 *)pCtx->pKey, (uint32 *)pBufPre8) ;
	//解密后的结果与前一轮的密文异或，得到真正的明文
	for(i=0; i<8; i++)
		pBuf8[i] = pBufPre8[i] ^ pCryptPre8[i] ;
#else
	uint32 bufTemp[2] ;
	uint32 *pBuf8, *pBufPre8, *pCrypt8, *pCryptPre8 ;
	pBuf8 = (uint32 *)pCtx->buf ;
	pBufPre8 = (uint32 *)pCtx->bufPre ;
	pCrypt8 = (uint32 *)pCtx->pCrypt ;
	pCryptPre8 = (uint32 *)pCtx->pCryptPre ;
	bufTemp[0] = pCrypt8[0] ^ pBufPre8[0] ;
	bufTemp[1] = pCrypt8[1] ^ pBufPre8[1] ;
	DeCipher(bufTemp, (const uint32 *)pCtx->pKey, pBufPre8) ;
	pBuf8[0] = pBufPre8[0] ^ pCryptPre8[0] ;
	pBuf8[1] = pBufPre8[1] ^ pCryptPre8[1] ;
#endif
	pCtx->pCryptPre = pCtx->pCrypt ;
	pCtx->pCrypt += 8 ;
}

//解密。pCipher指向待解密密文。ulCipherLen密文长度。pKey密钥16字节。
//pOut指向明文输出缓冲区。pOutLen输入输出参数，指示输出缓冲区长度、明文长度。
uint32 Decrypt(TEACTX *pCtx, const uint8 *pCipher, uint32 ulCipherLen,
	const uint8 *pKey, uint8 *pOut, uint32 *pOutLen)
{
	uint32 ulPos, ulPadding, ulOut, ul ;
	// 待解密的数据长度最少16字节，并且长度满足是8的整数倍。
	if(pCipher == NULL || pOutLen == NULL ||
			ulCipherLen < 16 || (ulCipherLen & 0x07) != 0)
		return 0 ;
	pCtx->pKey = pKey ; //***2016-06-15 这个忘记加了，补上***
	// 先解密头8字节，以便获取第一轮加密时填充的长度。
	DeCipher((const uint32 *)pCipher, (const uint32 *)pKey, (uint32 *)pCtx->bufPre) ;
	for(ul=0; ul<8; ul++)
		pCtx->buf[ul] = pCtx->bufPre[ul] ;
	ulPos = pCtx->buf[0] & 0x07 ; //第一轮加密时填充的长度
	if(ulPos > 1)
	{
		for(ulOut=2; ulOut<=ulPos; ulOut++)
		{
			if(pCtx->buf[1] != pCtx->buf[ulOut])
			{
				*pOutLen = 0 ;
				return 0 ; //解密失败
			}
		}
	}
	ulOut = ulCipherLen - ulPos - 10 ;
	if(ulPos + 10 > ulCipherLen || *pOutLen < ulOut)
		return 0 ;
	pCtx->pCryptPre = (uint8 *)pCipher ;
	pCtx->pCrypt = (uint8 *)pCipher + 8 ;
	ulPos++ ;
	for(ulPadding=0; ulPadding<2; ulPadding++)
	{
		if(ulPos == 8)
		{
			DecryptEach8Bytes(pCtx) ;
			ulPos = 0 ;
		}
		ulPos++ ;
	}
	//
	for(ul=0; ul<ulOut; ul++)
	{
		if(ulPos == 8)
		{
			DecryptEach8Bytes(pCtx) ;
			ulPos = 0 ;
		}
		pOut[ul] = pCtx->buf[ulPos] ;
		ulPos++ ;
	}
	//
	for(ulPadding=0; ulPadding<7; ulPadding++)
	{
		if(ulPos < 8)
		{
			if(pCtx->buf[ulPos] != 0x00)
			{
				*pOutLen = 0 ;
				return 0 ;
			}
		}
		ulPos++ ;
	}
	*pOutLen = ulOut ;
	return 1 ;
}

void PrintBuffer(const uint8 *buf, uint32 ulLen)
{
	uint32 i ;
	for(i=0; i<ulLen; i++)
	{
		printf("%.2X ", buf[i]) ;
		if((i+1) % 16 == 0)
			putchar('\n') ;
	}
	if((ulLen & 0x0F) != 0)
		putchar('\n') ;
}

int main(void)
{
	const char *pPK[][2] =
	{
		//明文--密钥
		{"tea",	"123456789abcdef"},
		{"tea",	"123456789abcdef"},
		{"123456",	"password1234567"},
		{"AABBCCD",	"aabbccddeeffggh"},
		{"Hello World 你好世界！",	"aabbccddeeffggh"}
	} ;
	TEACTX ctx ;
	uint8 bufEnc[512], bufDec[512] ;
	uint32 ulEnc, ulDec, ulRet ;
	int i ;
	for(i=0; i<sizeof(pPK)/sizeof(pPK[0]); i++)
	{
		printf("明文：%s\n密钥：%s\n", pPK[i][0], pPK[i][1]) ;
		ulEnc = sizeof(bufEnc) ;
		Encrypt(&ctx, (const uint8 *)pPK[i][0], strlen(pPK[i][0])+1,
				(const uint8 *)pPK[i][1], (uint8 *)bufEnc, &ulEnc) ;
		printf("密文：\n") ;
		PrintBuffer(bufEnc, ulEnc) ;
		ulDec = sizeof(bufDec) ;
		ulRet = Decrypt(&ctx, bufEnc, ulEnc, (const uint8 *)pPK[i][1],
				(uint8 *)bufDec, &ulDec) ;
		if(ulRet != 0)
			printf("解密后明文：%s\n", bufDec) ;
		else
			printf("解密失败！\n") ;
		putchar('\n') ;
	}
	return 0 ;
}