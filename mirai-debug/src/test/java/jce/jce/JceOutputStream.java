package jce.jce;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class JceOutputStream {
   // $FF: renamed from: bs java.nio.ByteBuffer
   private ByteBuffer field_80728;
   private OnIllegalArgumentException exceptionHandler;
   protected String sServerEncoding;

   public JceOutputStream() {
      this(128);
   }

   public JceOutputStream(int var1) {
      this.sServerEncoding = "GBK";
      this.field_80728 = ByteBuffer.allocate(var1);
   }

   public JceOutputStream(ByteBuffer var1) {
      this.sServerEncoding = "GBK";
      this.field_80728 = var1;
   }

   public static void main(String[] var0) {
      JceOutputStream var2 = new JceOutputStream();
      var2.write(1311768467283714885L, 0);
      ByteBuffer var1 = var2.getByteBuffer();
      System.out.println(HexUtil.bytes2HexStr(var1.array()));
      System.out.println(Arrays.toString(var2.toByteArray()));
   }

   private void writeArray(Object[] var1, int var2) {
      this.reserve(8);
      this.writeHead((byte) 9, var2);
      this.write(var1.length, 0);
      int var3 = var1.length;

      for (var2 = 0; var2 < var3; ++var2) {
         this.write(var1[var2], 0);
      }

   }

   public ByteBuffer getByteBuffer() {
      return this.field_80728;
   }

   public OnIllegalArgumentException getExceptionHandler() {
      return this.exceptionHandler;
   }

   public void reserve(int var1) {
      if (this.field_80728.remaining() < var1) {
         int var2 = (this.field_80728.capacity() + var1) * 2;

         ByteBuffer var3;
         try {
            var3 = ByteBuffer.allocate(var2);
            var3.put(this.field_80728.array(), 0, this.field_80728.position());
         } catch (IllegalArgumentException var4) {
            if (this.exceptionHandler != null) {
               this.exceptionHandler.onException(var4, this.field_80728, var1, var2);
            }

            throw var4;
         }

         this.field_80728 = var3;
      }

   }

   public void setExceptionHandler(OnIllegalArgumentException var1) {
      this.exceptionHandler = var1;
   }

   public int setServerEncoding(String var1) {
      this.sServerEncoding = var1;
      return 0;
   }

   public byte[] toByteArray() {
      byte[] var1 = new byte[this.field_80728.position()];
      System.arraycopy(this.field_80728.array(), 0, var1, 0, this.field_80728.position());
      return var1;
   }

   public void write(byte var1, int var2) {
      this.reserve(3);
      if (var1 == 0) {
         this.writeHead((byte) 12, var2);
      } else {
         this.writeHead((byte) 0, var2);
         this.field_80728.put(var1);
      }
   }

   public void write(double var1, int var3) {
      this.reserve(10);
      this.writeHead((byte) 5, var3);
      this.field_80728.putDouble(var1);
   }

   public void write(float var1, int var2) {
      this.reserve(6);
      this.writeHead((byte) 4, var2);
      this.field_80728.putFloat(var1);
   }

   public void write(int var1, int var2) {
      this.reserve(6);
      if (var1 >= -32768 && var1 <= 32767) {
         this.write((short) var1, var2);
      } else {
         this.writeHead((byte) 2, var2);
         this.field_80728.putInt(var1);
      }
   }

   public void write(long var1, int id) {
      this.reserve(10);
      if (var1 >= -2147483648L && var1 <= 2147483647L) {
         this.write((int) var1, id);
      } else {
         this.writeHead((byte) 3, id);
         this.field_80728.putLong(var1);
      }
   }

   public void write(JceStruct var1, int var2) {
      this.reserve(2);
      this.writeHead((byte) 10, var2);
      var1.writeTo(this);
      this.reserve(2);
      this.writeHead((byte) 11, 0);
   }

   public void write(Boolean var1, int var2) {
      this.write((boolean) var1, var2);
   }

   public void write(Byte var1, int var2) {
      this.write((byte) var1, var2);
   }

   public void write(Double var1, int var2) {
      this.write((double) var1, var2);
   }

   public void write(Float var1, int var2) {
      this.write((float) var1, var2);
   }

   public void write(Integer var1, int var2) {
      this.write((int) var1, var2);
   }

   public void write(Long var1, int var2) {
      this.write((long) var1, var2);
   }

   public void write(Object var1, int var2) {
      if (var1 instanceof Byte) {
         this.write((Byte) var1, var2);
      } else if (var1 instanceof Boolean) {
         this.write((Boolean) var1, var2);
      } else if (var1 instanceof Short) {
         this.write((Short) var1, var2);
      } else if (var1 instanceof Integer) {
         this.write((Integer) var1, var2);
      } else if (var1 instanceof Long) {
         this.write((Long) var1, var2);
      } else if (var1 instanceof Float) {
         this.write((Float) var1, var2);
      } else if (var1 instanceof Double) {
         this.write((Double) var1, var2);
      } else if (var1 instanceof String) {
         this.write((String) var1, var2);
      } else if (var1 instanceof Map) {
         this.write((Map) var1, var2);
      } else if (var1 instanceof List) {
         this.write((List) var1, var2);
      } else if (var1 instanceof JceStruct) {
         this.write((JceStruct) var1, var2);
      } else if (var1 instanceof byte[]) {
         this.write((byte[]) var1, var2);
      } else if (var1 instanceof boolean[]) {
         this.write((boolean[]) var1, var2);
      } else if (var1 instanceof short[]) {
         this.write((short[]) var1, var2);
      } else if (var1 instanceof int[]) {
         this.write((int[]) var1, var2);
      } else if (var1 instanceof long[]) {
         this.write((long[]) var1, var2);
      } else if (var1 instanceof float[]) {
         this.write((float[]) var1, var2);
      } else if (var1 instanceof double[]) {
         this.write((double[]) var1, var2);
      } else if (var1.getClass().isArray()) {
         this.writeArray((Object[]) var1, var2);
      } else if (var1 instanceof Collection) {
         this.write((Collection) var1, var2);
      } else {
         throw new JceEncodeException("write object error: unsupport type. " + var1.getClass());
      }
   }

   public void write(Short var1, int var2) {
      this.write((short) var1, var2);
   }

   public void write(String var1, int var2) {
      byte[] var5;
      label16:
      {
         byte[] var3;
         try {
            var3 = var1.getBytes(this.sServerEncoding);
         } catch (UnsupportedEncodingException var4) {
            var5 = var1.getBytes();
            break label16;
         }

         var5 = var3;
      }

      this.reserve(var5.length + 10);
      if (var5.length > 255) {
         this.writeHead((byte) 7, var2);
         this.field_80728.putInt(var5.length);
      } else {
         this.writeHead((byte) 6, var2);
         this.field_80728.put((byte) var5.length);
      }
      this.field_80728.put(var5);
   }

   public <T> void write(Collection<T> var1, int var2) {
      this.reserve(8);
      this.writeHead((byte) 9, var2);
      if (var1 == null) {
         var2 = 0;
      } else {
         var2 = var1.size();
      }

      this.write(var2, 0);
      if (var1 != null) {

         for (T t : var1) {
            this.write(t, 0);
         }
      }

   }

   public <K, V> void write(Map<K, V> var1, int var2) {
      this.reserve(8);
      this.writeHead((byte) 8, var2);
      if (var1 == null) {
         var2 = 0;
      } else {
         var2 = var1.size();
      }

      this.write(var2, 0);
      if (var1 != null) {

         for (Entry<K, V> kvEntry : var1.entrySet()) {
            this.write(((Entry) kvEntry).getKey(), 0);
            this.write(((Entry) kvEntry).getValue(), 1);
         }
      }

   }

   public void write(short var1, int var2) {
      this.reserve(4);
      if (var1 >= -128 && var1 <= 127) {
         this.write((byte) var1, var2);
      } else {
         this.writeHead((byte) 1, var2);
         this.field_80728.putShort(var1);
      }
   }

   public void write(boolean var1, int var2) {
      byte var3;
      if (var1) {
         var3 = 1;
      } else {
         var3 = 0;
      }

      this.write(var3, var2);
   }

   public void write(byte[] var1, int var2) {
      this.reserve(var1.length + 8);
      this.writeHead((byte) 13, var2);
      this.writeHead((byte) 0, 0);
      this.write(var1.length, 0);
      this.field_80728.put(var1);
   }

   public void write(double[] var1, int var2) {
      this.reserve(8);
      this.writeHead((byte) 9, var2);
      this.write(var1.length, 0);
      int var3 = var1.length;

      for (var2 = 0; var2 < var3; ++var2) {
         this.write(var1[var2], 0);
      }

   }

   public void write(float[] var1, int var2) {
      this.reserve(8);
      this.writeHead((byte) 9, var2);
      this.write(var1.length, 0);
      int var3 = var1.length;

      for (var2 = 0; var2 < var3; ++var2) {
         this.write(var1[var2], 0);
      }

   }

   public void write(int[] var1, int var2) {
      this.reserve(8);
      this.writeHead((byte) 9, var2);
      this.write(var1.length, 0);
      int var3 = var1.length;

      for (var2 = 0; var2 < var3; ++var2) {
         this.write(var1[var2], 0);
      }

   }

   public void write(long[] var1, int var2) {
      this.reserve(8);
      this.writeHead((byte) 9, var2);
      this.write(var1.length, 0);
      int var3 = var1.length;

      for (var2 = 0; var2 < var3; ++var2) {
         this.write(var1[var2], 0);
      }

   }

   public <T> void write(T[] var1, int var2) {
      this.writeArray(var1, var2);
   }

   public void write(short[] var1, int var2) {
      this.reserve(8);
      this.writeHead((byte) 9, var2);
      this.write(var1.length, 0);
      int var3 = var1.length;

      for (var2 = 0; var2 < var3; ++var2) {
         this.write(var1[var2], 0);
      }

   }

   public void write(boolean[] var1, int var2) {
      this.reserve(8);
      this.writeHead((byte) 9, var2);
      this.write(var1.length, 0);
      int var3 = var1.length;

      for (var2 = 0; var2 < var3; ++var2) {
         this.write(var1[var2], 0);
      }

   }

   public void writeByteString(String var1, int var2) {
      this.reserve(var1.length() + 10);
      byte[] var3 = HexUtil.hexStr2Bytes(var1);
      if (var3.length > 255) {
         this.writeHead((byte) 7, var2);
         this.field_80728.putInt(var3.length);
         this.field_80728.put(var3);
      } else {
         this.writeHead((byte) 6, var2);
         this.field_80728.put((byte) var3.length);
         this.field_80728.put(var3);
      }
   }

   public void writeHead(byte var1, int tag) {
       byte var3;
       if (tag < 15) {
           var3 = (byte) (tag << 4 | var1);
           this.field_80728.put(var3);
       } else if (tag < 256) {
           var3 = (byte) (var1 | 240);
           this.field_80728.put(var3);
           this.field_80728.put((byte) tag);
       } else {
           throw new JceEncodeException("tag is too large: " + tag);
       }
   }

   public void writeStringByte(String var1, int var2) {
      byte[] var3 = HexUtil.hexStr2Bytes(var1);
      this.reserve(var3.length + 10);
      if (var3.length > 255) {
         this.writeHead((byte) 7, var2);
         this.field_80728.putInt(var3.length);
         this.field_80728.put(var3);
      } else {
         this.writeHead((byte) 6, var2);
         this.field_80728.put((byte) var3.length);
         this.field_80728.put(var3);
      }
   }
}
