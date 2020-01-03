import net.mamoe.mirai.utils.PlatformUtilsAndroidKt;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class EcdhCrypt {
    public static final String DEFAULT_PUB_KEY = "020b03cf3d99541f29ffec281bebbd4ea211292ac1f53d7128";
    public static final String DEFAULT_SHARE_KEY = "4da0f614fc9f29c2054c77048a6566d7";
    public static final String S_PUB_KEY = "04928D8850673088B343264E0C6BACB8496D697799F37211DEB25BB73906CB089FEA9639B4E0260498B51A992D50813DA8";
    public static final String X509_S_PUB_KEY = "3046301006072A8648CE3D020106052B8104001F03320004928D8850673088B343264E0C6BACB8496D697799F37211DEB25BB73906CB089FEA9639B4E0260498B51A992D50813DA8";
    public static byte[] _c_pri_key;
    public static byte[] _c_pub_key;
    private static byte[] _g_share_key;
    private static boolean initFlg;
    public static PrivateKey pkcs8PrivateKey;
    private static boolean userOpenSSLLib;
    public static PublicKey x509PublicKey;

    static {
        EcdhCrypt.initFlg = false;
        EcdhCrypt.userOpenSSLLib = true;
        EcdhCrypt._c_pub_key = new byte[0];
        EcdhCrypt._c_pri_key = new byte[0];
        EcdhCrypt._g_share_key = new byte[0];
    }

    public EcdhCrypt() {
        /// util.loadLibrary("wtecdh", context);
    }

    public static String buf_to_string(final byte[] array) {
        String s;
        if (array == null) {
            s = "";
        } else {
            String string = "";
            int n = 0;
            while (true) {
                s = string;
                if (n >= array.length) {
                    break;
                }
                string = string + Integer.toHexString(array[n] >> 4 & 0xF) + Integer.toHexString(array[n] & 0xF);
                ++n;
            }
        }
        return s;
    }

    private byte[] calShareKeyByBouncycastle(final byte[] pubKey) {
        String str = "3046301006072A8648CE3D020106052B8104001F03320004";
        try {
            if (pubKey.length < 30) {
                str = "302E301006072A8648CE3D020106052B8104001F031A00";
            }
            final PublicKey constructX509PublicKey = this.constructX509PublicKey(str + buf_to_string(pubKey));
            final KeyAgreement instance = KeyAgreement.getInstance("ECDH", "BC");
            instance.init(EcdhCrypt.pkcs8PrivateKey);
            instance.doPhase(constructX509PublicKey, true);
            final byte[] generateSecret = instance.generateSecret();
            return PlatformUtilsAndroidKt.md5(generateSecret);
        } catch (ExceptionInInitializerError | Exception exceptionInInitializerError) {
            exceptionInInitializerError.printStackTrace();
            return null;
        }
    }

    private byte[] calShareKeyByOpenSSL(final String s, final String str, final String s2) {
        //if (this.GenECDHKeyEx(s2, str, s) == 0) {
        return EcdhCrypt._g_share_key;
        //}
        //   return null;
    }

    private PublicKey constructX509PublicKey(final String str) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance("EC", "BC").generatePublic(new X509EncodedKeySpec(string_to_buf(str)));
    }

    public static byte[] string_to_buf(final String s) {
        int i = 0;
        if (s == null) {
            return new byte[0];
        }
        final byte[] array = new byte[s.length() / 2];
        while (i < s.length() / 2) {
            array[i] = (byte) ((get_char((byte) s.charAt(i * 2)) << 4) + get_char((byte) s.charAt(i * 2 + 1)));
            ++i;
        }
        return array;
    }

    public static byte get_char(final byte b) {
        if (b >= 48 && b <= 57) {
            return (byte) (b - 48);
        }
        if (b >= 97 && b <= 102) {
            return (byte) (b - 97 + 10);
        }
        if (b >= 65 && b <= 70) {
            return (byte) (b - 65 + 10);
        }
        return 0;
    }

    private int initShareKeyByBouncycastle() {
        try {
            final KeyPairGenerator instance = KeyPairGenerator.getInstance("EC", "BC");
            instance.initialize(new ECGenParameterSpec("secp192k1"));
            final KeyPair genKeyPair = instance.genKeyPair();
            final PublicKey public1 = genKeyPair.getPublic();
            final byte[] encoded = public1.getEncoded();
            final PrivateKey private1 = genKeyPair.getPrivate();
            private1.getEncoded();
            final PublicKey constructX509PublicKey = this.constructX509PublicKey("3046301006072A8648CE3D020106052B8104001F03320004928D8850673088B343264E0C6BACB8496D697799F37211DEB25BB73906CB089FEA9639B4E0260498B51A992D50813DA8");
            final KeyAgreement instance2 = KeyAgreement.getInstance("ECDH", "BC");
            instance2.init(private1);
            instance2.doPhase(constructX509PublicKey, true);
            EcdhCrypt._g_share_key = PlatformUtilsAndroidKt.md5(instance2.generateSecret());
            System.arraycopy(encoded, 23, EcdhCrypt._c_pub_key = new byte[49], 0, 49);
            EcdhCrypt.x509PublicKey = public1;
            EcdhCrypt.pkcs8PrivateKey = private1;
            return 0;
        } catch (ExceptionInInitializerError exceptionInInitializerError) {
            exceptionInInitializerError.printStackTrace();
            return -1;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -2;
        }
    }

    private int initShareKeyByOpenSSL() {
        // if (Build$VERSION.SDK_INT >= 23 || this.GenereateKey() != 0) {
        //     return -1;
        // }
        if (EcdhCrypt._c_pub_key == null || EcdhCrypt._c_pub_key.length == 0 || EcdhCrypt._c_pri_key == null || EcdhCrypt._c_pri_key.length == 0 || EcdhCrypt._g_share_key == null || EcdhCrypt._g_share_key.length == 0) {
            return -2;
        }
        return 0;
    }

    public native int GenECDHKeyEx(final String p0, final String p1, final String p2);

    public int GenereateKey() {
        try {
            synchronized (EcdhCrypt.class) {
                return this.GenECDHKeyEx("04928D8850673088B343264E0C6BACB8496D697799F37211DEB25BB73906CB089FEA9639B4E0260498B51A992D50813DA8", "", "");
            }
        } catch (UnsatisfiedLinkError unsatisfiedLinkError) {
            unsatisfiedLinkError.printStackTrace();
            return -1;
        } catch (RuntimeException ex) {
            return -2;
        } catch (Exception ex2) {
            return -3;
        } catch (Error error) {
            return -4;
        }
    }

    public byte[] calShareKeyMd5ByPeerPublicKey(final byte[] array) {
        if (EcdhCrypt.userOpenSSLLib) {
            return this.calShareKeyByOpenSSL(buf_to_string(EcdhCrypt._c_pri_key), buf_to_string(EcdhCrypt._c_pub_key), buf_to_string(array));
        }
        return this.calShareKeyByBouncycastle(array);
    }

    public byte[] get_c_pub_key() {
        return EcdhCrypt._c_pub_key.clone();
    }

    public byte[] get_g_share_key() {
        return EcdhCrypt._g_share_key.clone();
    }

    public int initShareKey() {
        if (EcdhCrypt.initFlg) {
            return 0;
        }
        EcdhCrypt.initFlg = true;
        if (this.initShareKeyByOpenSSL() == 0) {
            EcdhCrypt.userOpenSSLLib = true;
            return 0;
        }
        if (this.initShareKeyByBouncycastle() == 0) {
            EcdhCrypt.userOpenSSLLib = false;
            return 0;
        }
        return this.initShareKeyByDefault();
    }

    public int initShareKeyByDefault() {
        // EcdhCrypt._c_pub_key = util.string_to_buf("020b03cf3d99541f29ffec281bebbd4ea211292ac1f53d7128");
        // EcdhCrypt._g_share_key = util.string_to_buf("4da0f614fc9f29c2054c77048a6566d7");
        return 0;
    }

    public void set_c_pri_key(final byte[] array) {
        if (array != null) {
            EcdhCrypt._c_pri_key = array.clone();
            return;
        }
        EcdhCrypt._c_pri_key = new byte[0];
    }

    public void set_c_pub_key(final byte[] array) {
        if (array != null) {
            EcdhCrypt._c_pub_key = array.clone();
            return;
        }
        EcdhCrypt._c_pub_key = new byte[0];
    }

    public void set_g_share_key(final byte[] array) {
        if (array != null) {
            EcdhCrypt._g_share_key = array.clone();
            return;
        }
        EcdhCrypt._g_share_key = new byte[0];
    }
}
