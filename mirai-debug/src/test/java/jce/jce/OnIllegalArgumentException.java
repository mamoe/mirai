package jce.jce;

import java.nio.ByteBuffer;

public interface OnIllegalArgumentException {
   void onException(IllegalArgumentException var1, ByteBuffer var2, int var3, int var4);
}
