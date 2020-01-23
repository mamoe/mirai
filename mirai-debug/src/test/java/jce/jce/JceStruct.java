package jce.jce;

import java.io.Serializable;

public abstract class JceStruct implements Serializable {
   public static final byte BYTE = 0;
   public static final byte DOUBLE = 5;
   public static final byte FLOAT = 4;
   public static final byte INT = 2;
   public static final int JCE_MAX_STRING_LENGTH = 104857600;
   public static final byte LIST = 9;
   public static final byte LONG = 3;
   public static final byte MAP = 8;
   public static final byte SHORT = 1;
   public static final byte SIMPLE_LIST = 13;
   public static final byte STRING1 = 6;
   public static final byte STRING4 = 7;
   public static final byte STRUCT_BEGIN = 10;
   public static final byte STRUCT_END = 11;
   public static final byte ZERO_TAG = 12;

   public static String toDisplaySimpleString(JceStruct var0) {
      if (var0 == null) {
         return null;
      } else {
         StringBuilder var1 = new StringBuilder();
         var0.displaySimple(var1, 0);
         return var1.toString();
      }
   }

   public boolean containField(String var1) {
      return false;
   }

   public void display(StringBuilder var1, int var2) {
   }

   public void displaySimple(StringBuilder var1, int var2) {
   }

   public Object getFieldByName(String var1) {
      return null;
   }

   public JceStruct newInit() {
      return null;
   }

   public abstract void readFrom(JceInputStream var1);

   public void recyle() {
   }

   public void setFieldByName(String var1, Object var2) {
   }

   public byte[] toByteArray() {
      JceOutputStream var1 = new JceOutputStream();
      this.writeTo(var1);
      return var1.toByteArray();
   }

   public byte[] toByteArray(String var1) {
      JceOutputStream var2 = new JceOutputStream();
      var2.setServerEncoding(var1);
      this.writeTo(var2);
      return var2.toByteArray();
   }

   public String toString() {
      StringBuilder var1 = new StringBuilder();
      this.display(var1, 0);
      return var1.toString();
   }

   public abstract void writeTo(JceOutputStream var1);
}
