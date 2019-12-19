
int __fastcall sub_F74(int a1, int a2, int a3, signed int a4, int a5, const char *a6)
{
  int v6; // r4
  int v7; // r5
  int v8; // r7
  int v9; // r0
  char *v10; // r6
  int v11; // r0
  int v12; // r0
  signed int v13; // r7
  int v14; // r0
  int v15; // r5
  signed int v16; // r5
  int v17; // r0
  int v18; // r5
  int v19; // r7
  int v20; // r5
  int v21; // r0
  signed int v22; // r5
  signed int v23; // r7
  int v24; // r0
  int v25; // r0
  int v26; // r0
  int v27; // r0
  int v28; // r3
  signed int v29; // r0
  int v30; // r2
  int result; // r0
  int v32; // r2
  int v33; // r2
  int v34; // [sp+0h] [bp-10D0h]
  int v35; // [sp+4h] [bp-10CCh]
  signed int v36; // [sp+8h] [bp-10C8h]
  int v37; // [sp+Ch] [bp-10C4h]
  const char *v38; // [sp+10h] [bp-10C0h]
  int v39; // [sp+14h] [bp-10BCh]
  int v40; // [sp+18h] [bp-10B8h]
  int v41; // [sp+1Ch] [bp-10B4h]
  int v42; // [sp+20h] [bp-10B0h]
  _DWORD *v43; // [sp+24h] [bp-10ACh]
  int v44; // [sp+28h] [bp-10A8h]
  int v45; // [sp+2Ch] [bp-10A4h]
  int v46; // [sp+30h] [bp-10A0h]
  char v47; // [sp+34h] [bp-109Ch]
  char v48; // [sp+B4h] [bp-101Ch]
  char v49; // [sp+2B4h] [bp-E1Ch]
  int v50; // [sp+4B4h] [bp-C1Ch]
  char v51; // [sp+8B4h] [bp-81Ch]
  unsigned __int8 v52; // [sp+CB4h] [bp-41Ch]
  int v53; // [sp+10B4h] [bp-1Ch]

  v6 = a2;
  v36 = a4;
  v40 = a3;
  v37 = a5;
  v7 = (int)&_stack_chk_guard;
  v8 = 676;
  v38 = a6;
  v41 = a1;
  v9 = (*(int (__fastcall **)(int, signed int, _DWORD))(*(_DWORD *)a2 + 676))(a2, a4, 0);
  v43 = &_stack_chk_guard;
  v10 = (char *)v9;
  v39 = 676;
  if ( v9 )
  {
    j_memset(&v50, 0, 1024);
    v8 = (int)"%s";
    j_snprintf(&v50, 1024, "%s", v10);
    v7 = 680;
    (*(void (__fastcall **)(int, signed int, char *))(*(_DWORD *)v6 + 680))(v6, v36, v10);
    v10 = &aJavaOicqWlogin_1[(_DWORD)&v42 + 27];
    j_memset(&aJavaOicqWlogin_1[(_DWORD)&v42 + 27], 0, 1024);
    v36 = 680;
    if ( v41 )
    {
      v11 = (*(int (__fastcall **)(int, int, _DWORD))(*(_DWORD *)v6 + 676))(v6, a5, 0);
      v7 = v11;
      if ( !v11 )
      {
        v29 = 3;
        goto LABEL_59;
      }
      j_snprintf(&aJavaOicqWlogin_1[(_DWORD)&v42 + 27], 1024, "%s", v11);
      (*(void (__fastcall **)(int, int, int))(*(_DWORD *)v6 + 680))(v6, a5, v7);
    }
    v10 = (char *)&_stack_chk_fail + (_DWORD)&v42;
    j_memset((char *)&_stack_chk_fail + (_DWORD)&v42, 0, 1024);
    if ( v41 )
    {
      v12 = (*(int (__fastcall **)(int, const char *, _DWORD))(*(_DWORD *)v6 + 676))(v6, a6, 0);
      v7 = v12;
      if ( !v12 )
      {
        v29 = 2;
        goto LABEL_59;
      }
      j_snprintf((char *)&_stack_chk_fail + (_DWORD)&v42, 1024, "%s", v12);
      (*(void (__fastcall **)(int, const char *, int))(*(_DWORD *)v6 + 680))(v6, a6, v7);
    }
    j_memset(&v47, 0, 128);
    v44 = 512;
    v37 = j_EC_KEY_new_by_curve_name(711);
    if ( !v37 )
    {
      j___android_log_print(4, "wlogin_sdk", "ERROR:EC_KEY_new_by_curve_name failed.");
      v8 = -1;
LABEL_56:
      j___android_log_print(4, "wlogin_sdk", "GenerateKey failed peerBase16PublicKey %s ret %d", &v50, v8, v35);
      v29 = 4;
      goto LABEL_59;
    }
    v7 = v52;
    if ( !v52 )
    {
      if ( j_EC_KEY_generate_key(v37) != 1 )
      {
        j___android_log_print(4, "wlogin_sdk", "EC_KEY_generate_key failed ret %d");
        v10 = (_BYTE *)(&stru_3F8 + 8);
        v36 = 128;
        v38 = (_BYTE *)(&stru_3F8 + 8);
        v13 = 2;
LABEL_51:
        v8 = -v13;
        goto LABEL_53;
      }
LABEL_35:
      v39 = j_EC_KEY_get0_group(v37);
      if ( v39 )
      {
        v24 = j_EC_KEY_get0_public_key(v37);
        v7 = v24;
        if ( !v24 )
        {
          j___android_log_print(4, "wlogin_sdk", "ERROR:EC_KEY_get0_public_key failed");
          v10 = (_BYTE *)(&stru_3F8 + 8);
          v36 = 128;
          v38 = (_BYTE *)(&stru_3F8 + 8);
          v13 = 5;
          goto LABEL_51;
        }
        v38 = (const char *)j_EC_POINT_point2oct(v39, v24, 2, &aJavaOicqWlogin_1[(_DWORD)&v42 + 27], 67, 0);
        if ( (signed int)v38 > 0 )
        {
          v25 = j_EC_KEY_get0_private_key(v37);
          v7 = v25;
          if ( v25 )
          {
            v26 = j_BN_bn2mpi(v25, 0);
            v10 = (char *)v26;
            if ( v26 > 1024 )
            {
              j___android_log_print(4, "wlogin_sdk", "ERROR:privateKeyLen %d larger than buff len %d", v26, 1024);
              v10 = (_BYTE *)(&stru_3F8 + 8);
              v23 = 5;
              v36 = 128;
              goto LABEL_45;
            }
            j_BN_bn2mpi(v7, (char *)&_stack_chk_fail + (_DWORD)&v42);
            v27 = j_strlen(&v50);
            String2Buffer(&v50, v27, &v48, &v44);
            v7 = j_EC_POINT_new(v39);
            if ( j_EC_POINT_oct2point(v39, v7, &v48, v44, 0) != 1 )
            {
              j___android_log_print(4, "wlogin_sdk", "EC_POINT_oct2point failed %d");
              v13 = 6;
              v36 = 128;
              goto LABEL_51;
            }
            v28 = j_ECDH_compute_key(&v49, 512, v7, v37, 0);
            if ( v28 > 0 )
            {
              j_MD5(&v49, v28, &v47);
              v8 = 0;
              v36 = 16;
LABEL_53:
              j_EC_KEY_free(v37);
              if ( v7 )
                j_EC_POINT_free(v7);
              if ( !v8 )
              {
                v37 = (*(int (__fastcall **)(int, int))(*(_DWORD *)v6 + 124))(v6, v40);
                v7 = (*(int (__fastcall **)(int, const char *))(*(_DWORD *)v6 + 704))(v6, v38);
                (*(void (__fastcall **)(int, int, _DWORD, const char *, char *, int))(*(_DWORD *)v6 + 832))(
                  v6,
                  v7,
                  0,
                  v38,
                  &aJavaOicqWlogin_1[(_DWORD)&v42 + 27],
                  v35);
                v38 = "([B)V";
                v39 = 704;
                v30 = (*(int (__fastcall **)(int, int, const char *, const char *))(*(_DWORD *)v6 + 132))(
                        v6,
                        v37,
                        "set_c_pub_key",
                        "([B)V");
                v42 = 832;
                if ( v30 )
                  goto LABEL_61;
                v29 = 5;
                goto LABEL_59;
              }
              goto LABEL_56;
            }
            j___android_log_print(4, "wlogin_sdk", "ERROR:Gene ShareKey failed: %d", v28);
            v36 = 128;
          }
          else
          {
            j___android_log_print(4, "wlogin_sdk", "ERROR:EC_KEY_get0_private_key failed.");
            v10 = (_BYTE *)(&stru_3F8 + 8);
            v36 = 128;
          }
          v13 = 7;
          goto LABEL_51;
        }
        j___android_log_print(4, "wlogin_sdk", "ERROR:EC_POINT_point2oct failed, pubkey len:%d.", v38);
        v36 = 128;
        v38 = (_BYTE *)(&stru_3F8 + 8);
        v10 = (_BYTE *)(&stru_3F8 + 8);
        v23 = 6;
      }
      else
      {
        j___android_log_print(4, "wlogin_sdk", "ERROR:EC_KEY_get0_group failed");
        v10 = (_BYTE *)(&stru_3F8 + 8);
        v36 = 128;
        v38 = (_BYTE *)(&stru_3F8 + 8);
        v23 = 4;
      }
LABEL_45:
      v8 = -v23;
      v7 = 0;
      goto LABEL_53;
    }
    v45 = 512;
    v14 = j_strlen((char *)&_stack_chk_fail + (_DWORD)&v42);
    String2Buffer((char *)&_stack_chk_fail + (_DWORD)&v42, v14, &v48, &v45);
    v15 = j_BN_mpi2bn(&v48, v45, 0);
    if ( !v15 )
    {
      j___android_log_print(4, "wlogin_sdk", "BN_mpi2bn failed");
      v16 = 1;
LABEL_24:
      v20 = -v16;
      goto LABEL_34;
    }
    if ( j_EC_KEY_set_private_key(v37, v15) != 1 )
    {
      j___android_log_print(4, "wlogin_sdk", "EC_KEY_set_private_key failed %d");
      j_BN_free(v15);
      v16 = 2;
      goto LABEL_24;
    }
    j_BN_free(v15);
    v17 = j_EC_KEY_get0_group(v37);
    v18 = v17;
    if ( !v17 )
    {
      j___android_log_print(4, "wlogin_sdk", "EC_KEY_get0_group failed");
      v16 = 3;
      goto LABEL_24;
    }
    v19 = j_EC_POINT_new(v17);
    if ( !v19 )
    {
      j___android_log_print(4, "wlogin_sdk", "EC_POINT_new failed");
      v16 = 4;
      goto LABEL_24;
    }
    if ( v51 )
    {
      v46 = 512;
      v21 = j_strlen(&aJavaOicqWlogin_1[(_DWORD)&v42 + 27]);
      String2Buffer(&aJavaOicqWlogin_1[(_DWORD)&v42 + 27], v21, &v49, &v46);
      if ( j_EC_POINT_oct2point(v18, v19, &v49, v46, 0) != 1 )
      {
        j___android_log_print(4, "wlogin_sdk", "EC_POINT_oct2point failed ret %d");
        v22 = 5;
        goto LABEL_32;
      }
    }
    else
    {
      v34 = 0;
      v35 = 0;
      if ( j_EC_POINT_mul(v18, v19, 0) != 1 )
      {
        j___android_log_print(4, "wlogin_sdk", "EC_POINT_mul failed ret %d");
        v22 = 6;
        goto LABEL_32;
      }
    }
    v20 = 0;
    if ( j_EC_KEY_set_public_key(v37, v19) == 1 )
      goto LABEL_33;
    j___android_log_print(4, "wlogin_sdk", "EC_KEY_set_public_key failed ret %d");
    v22 = 7;
LABEL_32:
    v20 = -v22;
LABEL_33:
    j_EC_POINT_free(v19);
    if ( !v20 )
      goto LABEL_35;
LABEL_34:
    j___android_log_print(4, "wlogin_sdk", "setECKey failed ret %d", v20, v34, v35);
    v10 = (_BYTE *)(&stru_3F8 + 8);
    v36 = 128;
    v38 = (_BYTE *)(&stru_3F8 + 8);
    v23 = 3;
    goto LABEL_45;
  }
  v29 = 1;
LABEL_59:
  result = -v29;
  while ( v53 != *v43 )
  {
LABEL_61:
    (*(void (__fastcall **)(int, int))(*(_DWORD *)v6 + 244))(v6, v40);
    (*(void (__fastcall **)(int, int))(*(_DWORD *)v6 + 92))(v6, v7);
    if ( v41 )
    {
      v7 = (*(int (__fastcall **)(int, char *))(*(_DWORD *)v6 + v39))(v6, v10);
      (*(void (__fastcall **)(int, int, int, char *, char *))(*(_DWORD *)v6 + v42))(
        v6,
        v7,
        v8,
        v10,
        (char *)&_stack_chk_fail + (_DWORD)&v42);
      v8 = *(_DWORD *)(*(_DWORD *)v6 + 132);
      v32 = ((int (__fastcall *)(int, int, const char *, const char *))v8)(v6, v37, "set_c_pri_key", v38);
      if ( !v32 )
      {
        v29 = 7;
        goto LABEL_59;
      }
      v8 = *(_DWORD *)(*(_DWORD *)v6 + 244);
      ((void (__fastcall *)(int, int, int, int))v8)(v6, v40, v32, v7);
      (*(void (__fastcall **)(int, int))(*(_DWORD *)v6 + 92))(v6, v7);
    }
    v7 = (*(int (__fastcall **)(int, signed int))(*(_DWORD *)v6 + v39))(v6, v36);
    (*(void (__fastcall **)(int, int, _DWORD, signed int, char *))(*(_DWORD *)v6 + v42))(v6, v7, 0, v36, &v47);
    v10 = *(char **)(*(_DWORD *)v6 + 132);
    v33 = ((int (__fastcall *)(int, int, const char *, const char *))v10)(v6, v37, "set_g_share_key", "([B)V");
    if ( !v33 )
    {
      v29 = 8;
      goto LABEL_59;
    }
    v10 = *(char **)(*(_DWORD *)v6 + 244);
    ((void (__fastcall *)(int, int, int, int))v10)(v6, v40, v33, v7);
    (*(void (__fastcall **)(int, int))(*(_DWORD *)v6 + 92))(v6, v7);
    result = 0;
  }
  return result;
}