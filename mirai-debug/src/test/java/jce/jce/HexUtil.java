package jce.jce;

import java.io.UnsupportedEncodingException;

public class HexUtil {
   private static final char[] digits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
   public static final byte[] emptybytes = new byte[0];

   public static String byte2HexStr(byte var0) {
      char var1 = digits[var0 & 15];
      var0 = (byte) (var0 >>> 4);
      return new String(new char[]{digits[var0 & 15], var1});
   }

   public static String bytes2HexStr(byte[] var0) {
      if (var0 != null && var0.length != 0) {
         char[] var3 = new char[var0.length * 2];

         for (int var1 = 0; var1 < var0.length; ++var1) {
            byte var2 = var0[var1];
            var3[var1 * 2 + 1] = digits[var2 & 15];
            var2 = (byte) (var2 >>> 4);
            var3[var1 * 2 + 0] = digits[var2 & 15];
         }

         return new String(var3);
      } else {
         return null;
      }
   }

   public static byte char2Byte(char var0) {
      if (var0 >= '0' && var0 <= '9') {
         return (byte) (var0 - 48);
      } else if (var0 >= 'a' && var0 <= 'f') {
         return (byte) (var0 - 97 + 10);
      } else {
         return var0 >= 'A' && var0 <= 'F' ? (byte) (var0 - 65 + 10) : 0;
      }
   }

   public static byte hexStr2Byte(String var0) {
      byte var2 = 0;
      byte var1 = var2;
      if (var0 != null) {
         var1 = var2;
         if (var0.length() == 1) {
            var1 = char2Byte(var0.charAt(0));
         }
      }

      return var1;
   }

   public static byte[] hexStr2Bytes(String var0) {
      if (var0 != null && !var0.equals("")) {
         byte[] var4 = new byte[var0.length() / 2];

         for (int var3 = 0; var3 < var4.length; ++var3) {
            char var1 = var0.charAt(var3 * 2);
            char var2 = var0.charAt(var3 * 2 + 1);
            var4[var3] = (byte) (char2Byte(var1) * 16 + char2Byte(var2));
         }

         return var4;
      } else {
         return emptybytes;
      }
   }

   public static void main(String[] var0) {
      try {
         byte[] var2 = "Hello WebSocket World?".getBytes("gbk");
         System.out.println(bytes2HexStr(var2));
      } catch (UnsupportedEncodingException var1) {
         var1.printStackTrace();
      }
   }
}
