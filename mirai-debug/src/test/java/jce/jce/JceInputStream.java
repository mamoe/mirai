package jce.jce;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class JceInputStream {
    // $FF: renamed from: bs java.nio.ByteBuffer
    private ByteBuffer buffer;
    protected String sServerEncoding = "GBK";

    public JceInputStream() {
    }

    public JceInputStream(ByteBuffer var1) {
        this.buffer = var1;
    }

    public JceInputStream(byte[] var1) {
        this.buffer = ByteBuffer.wrap(var1);
    }

    public JceInputStream(byte[] var1, int var2) {
        this.buffer = ByteBuffer.wrap(var1);
        this.buffer.position(var2);
    }

    public static void main(String[] var0) {
    }

    private int peakHead(JceInputStream$HeadData var1) {
        return readHead(var1, this.buffer.duplicate());
    }

    private <T> T[] readArrayImpl(T var1, int var2, boolean var3) {
        Object[] var7;
        if (this.skipToTag(var2)) {
            JceInputStream$HeadData var5 = new JceInputStream$HeadData();
            this.readHead(var5);
            if (var5.type == 9) {
                int var4 = this.read(0, 0, true);
                if (var4 < 0) {
                    throw new JceDecodeException("size invalid: " + var4);
                } else {
                    Object[] var6 = (Object[]) Array.newInstance(var1.getClass(), var4);
                    var2 = 0;

                    while (true) {
                        var7 = var6;
                        if (var2 >= var4) {
                            return (T[]) var7;
                        }

                        var6[var2] = this.read(var1, 0, true);
                        ++var2;
                    }
                }
            }
            throw new JceDecodeException("type mismatch.");
        } else if (var3) {
            throw new JceDecodeException("require field not exist.");
        } else {
            var7 = null;
            return (T[]) var7;
        }
    }

    public static int readHead(JceInputStream$HeadData var0, ByteBuffer var1) {
        byte var2 = var1.get();
        var0.type = (byte) (var2 & 15);
        var0.tag = (var2 & 240) >> 4;
        if (var0.tag == 15) {
            var0.tag = var1.get() & 255;
            return 2;
        } else {
            return 1;
        }
    }

    private <K, V> Map<K, V> readMap(Map<K, V> var1, Map<K, V> var2, int var3, boolean var4) {
        Map<K, V> var8;
        if (var2 != null && !var2.isEmpty()) {
            Entry<K, V> var9 = var2.entrySet().iterator().next();
            K var6 = var9.getKey();
            V var7 = var9.getValue();
            if (this.skipToTag(var3)) {
                JceInputStream$HeadData var10 = new JceInputStream$HeadData();
                this.readHead(var10);
                if (var10.type == 8) {
                    int var5 = this.read(0, 0, true);
                    if (var5 < 0) {
                        throw new JceDecodeException("size invalid: " + var5);
                    }

                    var3 = 0;

                    while (true) {
                        var8 = var1;
                        if (var3 >= var5) {
                            return var8;
                        }

                        var1.put((K) this.read(var6, 0, true), (V) this.read(var7, 1, true));
                        ++var3;
                    }
                }
                throw new JceDecodeException("type mismatch.");
            } else {
                var8 = var1;
                if (var4) {
                    throw new JceDecodeException("require field not exist.");
                }
            }
        } else {
            var8 = new HashMap<>();
        }

        return var8;
    }

    private void skip(int var1) {
        this.buffer.position(this.buffer.position() + var1);
    }

    private void skipField() {
        JceInputStream$HeadData var1 = new JceInputStream$HeadData();
        this.readHead(var1);
        this.skipField(var1.type);
    }

    private void skipField(byte var1) {
        byte var3 = 0;
        byte var2 = 0;
        int var5;
        switch (var1) {
            case 0:
                this.skip(1);
                break;
            case 1:
                this.skip(2);
                return;
            case 2:
                this.skip(4);
                return;
            case 3:
                this.skip(8);
                return;
            case 4:
                this.skip(4);
                return;
            case 5:
                this.skip(8);
                return;
            case 6:
                byte var7 = this.buffer.get();
                var5 = var7;
                if (var7 < 0) {
                    var5 = var7 + 256;
                }

                this.skip(var5);
                return;
            case 7:
                this.skip(this.buffer.getInt());
                return;
            case 8:
                int var8 = this.read(0, 0, true);

                for (var5 = var2; var5 < var8 * 2; ++var5) {
                    this.skipField();
                }

                return;
            case 9:
                int var6 = this.read(0, 0, true);

                for (var5 = var3; var5 < var6; ++var5) {
                    this.skipField();
                }

                return;
            case 10:
                this.skipToStructEnd();
                return;
            case 11:
            case 12:
                break;
            case 13:
                JceInputStream$HeadData var4 = new JceInputStream$HeadData();
                this.readHead(var4);
                if (var4.type != 0) {
                    throw new JceDecodeException("skipField with invalid type, type value: " + var1 + ", " + var4.type);
                }

                this.skip(this.read(0, 0, true));
                return;
            default:
                throw new JceDecodeException("invalid type.");
        }

    }

    public JceStruct directRead(JceStruct var1, int var2, boolean var3) {
        JceInputStream$HeadData var4 = null;
        if (this.skipToTag(var2)) {
            try {
                var1 = var1.newInit();
            } catch (Exception var5) {
                throw new JceDecodeException(var5.getMessage());
            }

            var4 = new JceInputStream$HeadData();
            this.readHead(var4);
            if (var4.type != 10) {
                throw new JceDecodeException("type mismatch.");
            }

            var1.readFrom(this);
            this.skipToStructEnd();
        } else {
            var1 = null;
            if (var3) {
                throw new JceDecodeException("require field not exist.");
            }
        }

        return var1;
    }

    public ByteBuffer getBs() {
        return this.buffer;
    }

    public byte read(byte var1, int var2, boolean var3) {
        if (this.skipToTag(var2)) {
            JceInputStream$HeadData var4 = new JceInputStream$HeadData();
            this.readHead(var4);
            switch (var4.type) {
                case 0:
                    return this.buffer.get();
                case 12:
                    return 0;
                default:
                    throw new JceDecodeException("type mismatch.");
            }
        } else if (var3) {
            throw new JceDecodeException("require field not exist.");
        } else {
            return var1;
        }
    }

    public double read(double var1, int var3, boolean var4) {
        if (this.skipToTag(var3)) {
            JceInputStream$HeadData var5 = new JceInputStream$HeadData();
            this.readHead(var5);
            switch (var5.type) {
                case 4:
                    return this.buffer.getFloat();
                case 5:
                    return this.buffer.getDouble();
                case 12:
                    return 0.0D;
                default:
                    throw new JceDecodeException("type mismatch.");
            }
        } else if (var4) {
            throw new JceDecodeException("require field not exist.");
        } else {
            return var1;
        }
    }

    public float read(float var1, int var2, boolean var3) {
        if (this.skipToTag(var2)) {
            JceInputStream$HeadData var4 = new JceInputStream$HeadData();
            this.readHead(var4);
            switch (var4.type) {
                case 4:
                    return this.buffer.getFloat();
                case 12:
                    return 0.0F;
                default:
                    throw new JceDecodeException("type mismatch.");
            }
        } else if (var3) {
            throw new JceDecodeException("require field not exist.");
        } else {
            return var1;
        }
    }

    public int read(int var1, int var2, boolean var3) {
        if (this.skipToTag(var2)) {
            JceInputStream$HeadData var4 = new JceInputStream$HeadData();
            this.readHead(var4);
            switch (var4.type) {
                case 0:
                    return this.buffer.get();
                case 1:
                    return this.buffer.getShort();
                case 2:
                    return this.buffer.getInt();
                case 12:
                    return 0;
                default:
                    throw new JceDecodeException("type mismatch.");
            }
        } else if (var3) {
            throw new JceDecodeException("require field not exist.");
        } else {
            return var1;
        }
    }

    public long read(long var1, int var3, boolean var4) {
        if (this.skipToTag(var3)) {
            JceInputStream$HeadData var5 = new JceInputStream$HeadData();
            this.readHead(var5);
            switch (var5.type) {
                case 0:
                    return this.buffer.get();
                case 1:
                    return this.buffer.getShort();
                case 2:
                    return this.buffer.getInt();
                case 3:
                    return this.buffer.getLong();
                case 12:
                    return 0L;
                default:
                    throw new JceDecodeException("type mismatch.");
            }
        } else if (var4) {
            throw new JceDecodeException("require field not exist.");
        } else {
            return var1;
        }
    }

    public JceStruct read(JceStruct var1, int var2, boolean var3) {
        JceInputStream$HeadData var4 = null;
        if (this.skipToTag(var2)) {
            try {
                var1 = var1.getClass().newInstance();
            } catch (Exception var5) {
                throw new JceDecodeException(var5.getMessage());
            }

            var4 = new JceInputStream$HeadData();
            this.readHead(var4);
            if (var4.type != 10) {
                throw new JceDecodeException("type mismatch.");
            }

            var1.readFrom(this);
            this.skipToStructEnd();
        } else {
            var1 = null;
            if (var3) {
                throw new JceDecodeException("require field not exist.");
            }
        }

        return var1;
    }

    public <T> Object read(T var1, int var2, boolean var3) {
        if (var1 instanceof Byte) {
            return this.read((byte) 0, var2, var3);
        } else if (var1 instanceof Boolean) {
            return this.read(false, var2, var3);
        } else if (var1 instanceof Short) {
            return this.read((short) 0, var2, var3);
        } else if (var1 instanceof Integer) {
            return this.read(0, var2, var3);
        } else if (var1 instanceof Long) {
            return this.read(0L, var2, var3);
        } else if (var1 instanceof Float) {
            return this.read(0.0F, var2, var3);
        } else if (var1 instanceof Double) {
            return this.read(0.0D, var2, var3);
        } else if (var1 instanceof String) {
            return this.readString(var2, var3);
        } else if (var1 instanceof Map) {
            return this.readMap((Map) var1, var2, var3);
        } else if (var1 instanceof List) {
            return this.readArray((List) var1, var2, var3);
        } else if (var1 instanceof JceStruct) {
            return this.read((JceStruct) var1, var2, var3);
        } else if (var1.getClass().isArray()) {
            if (!(var1 instanceof byte[]) && !(var1 instanceof Byte[])) {
                if (var1 instanceof boolean[]) {
                    return this.read((boolean[]) null, var2, var3);
                } else if (var1 instanceof short[]) {
                    return this.read((short[]) null, var2, var3);
                } else if (var1 instanceof int[]) {
                    return this.read((int[]) null, var2, var3);
                } else if (var1 instanceof long[]) {
                    return this.read((long[]) null, var2, var3);
                } else if (var1 instanceof float[]) {
                    return this.read((float[]) null, var2, var3);
                } else {
                    return var1 instanceof double[] ? this.read((double[]) null, var2, var3) : this.readArray((Object[]) var1, var2, var3);
                }
            } else {
                return this.read((byte[]) null, var2, var3);
            }
        } else {
            throw new JceDecodeException("read object error: unsupport type.");
        }
    }

    public String read(String var1, int var2, boolean var3) {
        if (this.skipToTag(var2)) {
            JceInputStream$HeadData var8 = new JceInputStream$HeadData();
            this.readHead(var8);
            String var5;
            byte[] var9;
            switch (var8.type) {
                case 6:
                    byte var4 = this.buffer.get();
                    var2 = var4;
                    if (var4 < 0) {
                        var2 = var4 + 256;
                    }

                    var9 = new byte[var2];
                    this.buffer.get(var9);

                    try {
                        var5 = new String(var9, this.sServerEncoding);
                        return var5;
                    } catch (UnsupportedEncodingException var7) {
                        return new String(var9);
                    }
                case 7:
                    var2 = this.buffer.getInt();
                    if (var2 <= 104857600 && var2 >= 0 && var2 <= this.buffer.capacity()) {
                        var9 = new byte[var2];
                        this.buffer.get(var9);

                        try {
                            var5 = new String(var9, this.sServerEncoding);
                            return var5;
                        } catch (UnsupportedEncodingException var6) {
                            return new String(var9);
                        }
                    }

                    throw new JceDecodeException("String too long: " + var2);
                default:
                    throw new JceDecodeException("type mismatch.");
            }
        } else if (var3) {
            throw new JceDecodeException("require field not exist.");
        } else {
            return var1;
        }
    }

    public short read(short var1, int var2, boolean var3) {
        if (this.skipToTag(var2)) {
            JceInputStream$HeadData var4 = new JceInputStream$HeadData();
            this.readHead(var4);
            switch (var4.type) {
                case 0:
                    return this.buffer.get();
                case 1:
                    return this.buffer.getShort();
                case 12:
                    return 0;
                default:
                    throw new JceDecodeException("type mismatch.");
            }
        } else if (var3) {
            throw new JceDecodeException("require field not exist.");
        } else {
            return var1;
        }
    }

    public boolean read(boolean var1, int var2, boolean var3) {
        var1 = this.read((byte) 0, var2, var3) != 0;

        return var1;
    }

    public byte[] read(byte[] var1, int var2, boolean var3) {
        var1 = null;
        if (this.skipToTag(var2)) {
            JceInputStream$HeadData var6 = new JceInputStream$HeadData();
            this.readHead(var6);
            int var4;
            switch (var6.type) {
                case 9:
                    var4 = this.read(0, 0, true);
                    if (var4 >= 0 && var4 <= this.buffer.capacity()) {
                        byte[] var7 = new byte[var4];
                        var2 = 0;

                        while (true) {
                            var1 = var7;
                            if (var2 >= var4) {
                                return var1;
                            }

                            var7[var2] = this.read(var7[0], 0, true);
                            ++var2;
                        }
                    }

                    throw new JceDecodeException("size invalid: " + var4);
                case 13:
                    JceInputStream$HeadData var5 = new JceInputStream$HeadData();
                    this.readHead(var5);
                    if (var5.type != 0) {
                        throw new JceDecodeException("type mismatch, tag: " + var2 + ", type: " + var6.type + ", " + var5.type);
                    }

                    var4 = this.read(0, 0, true);
                    if (var4 < 0 || var4 > this.buffer.capacity()) {
                        throw new JceDecodeException("invalid size, tag: " + var2 + ", type: " + var6.type + ", " + var5.type + ", size: " + var4);
                    }

                    var1 = new byte[var4];
                    this.buffer.get(var1);
                    break;
                default:
                    throw new JceDecodeException("type mismatch.");
            }
        } else if (var3) {
            throw new JceDecodeException("require field not exist.");
        }

        return var1;
    }

    public double[] read(double[] var1, int var2, boolean var3) {
        var1 = null;
        if (this.skipToTag(var2)) {
            JceInputStream$HeadData var6 = new JceInputStream$HeadData();
            this.readHead(var6);
            if (var6.type == 9) {
                int var4 = this.read(0, 0, true);
                if (var4 < 0) {
                    throw new JceDecodeException("size invalid: " + var4);
                } else {
                    double[] var5 = new double[var4];
                    var2 = 0;

                    while (true) {
                        var1 = var5;
                        if (var2 >= var4) {
                            return var1;
                        }

                        var5[var2] = this.read(var5[0], 0, true);
                        ++var2;
                    }
                }
            }
            throw new JceDecodeException("type mismatch.");
        } else if (var3) {
            throw new JceDecodeException("require field not exist.");
        } else {
            return var1;
        }
    }

    public float[] read(float[] var1, int var2, boolean var3) {
        var1 = null;
        if (this.skipToTag(var2)) {
            JceInputStream$HeadData var6 = new JceInputStream$HeadData();
            this.readHead(var6);
            if (var6.type == 9) {
                int var4 = this.read(0, 0, true);
                if (var4 < 0) {
                    throw new JceDecodeException("size invalid: " + var4);
                } else {
                    float[] var5 = new float[var4];
                    var2 = 0;

                    while (true) {
                        var1 = var5;
                        if (var2 >= var4) {
                            return var1;
                        }

                        var5[var2] = this.read(var5[0], 0, true);
                        ++var2;
                    }
                }
            }
            throw new JceDecodeException("type mismatch.");
        } else if (var3) {
            throw new JceDecodeException("require field not exist.");
        } else {
            return var1;
        }
    }

    public int[] read(int[] var1, int var2, boolean var3) {
        var1 = null;
        if (this.skipToTag(var2)) {
            JceInputStream$HeadData var6 = new JceInputStream$HeadData();
            this.readHead(var6);
            if (var6.type == 9) {
                int var4 = this.read(0, 0, true);
                if (var4 < 0) {
                    throw new JceDecodeException("size invalid: " + var4);
                } else {
                    int[] var5 = new int[var4];
                    var2 = 0;

                    while (true) {
                        var1 = var5;
                        if (var2 >= var4) {
                            return var1;
                        }

                        var5[var2] = this.read(var5[0], 0, true);
                        ++var2;
                    }
                }
            }
            throw new JceDecodeException("type mismatch.");
        } else if (var3) {
            throw new JceDecodeException("require field not exist.");
        } else {
            return var1;
        }
    }

    public long[] read(long[] var1, int var2, boolean var3) {
        var1 = null;
        if (this.skipToTag(var2)) {
            JceInputStream$HeadData var6 = new JceInputStream$HeadData();
            this.readHead(var6);
            if (var6.type == 9) {
                int var4 = this.read(0, 0, true);
                if (var4 < 0) {
                    throw new JceDecodeException("size invalid: " + var4);
                } else {
                    long[] var5 = new long[var4];
                    var2 = 0;

                    while (true) {
                        var1 = var5;
                        if (var2 >= var4) {
                            return var1;
                        }

                        var5[var2] = this.read(var5[0], 0, true);
                        ++var2;
                    }
                }
            }
            throw new JceDecodeException("type mismatch.");
        } else if (var3) {
            throw new JceDecodeException("require field not exist.");
        } else {
            return var1;
        }
    }

    public JceStruct[] read(JceStruct[] var1, int var2, boolean var3) {
        return (JceStruct[]) this.readArray((Object[]) var1, var2, var3);
    }

    public String[] read(String[] var1, int var2, boolean var3) {
        return (String[]) this.readArray((Object[]) var1, var2, var3);
    }

    public short[] read(short[] var1, int var2, boolean var3) {
        var1 = null;
        if (this.skipToTag(var2)) {
            JceInputStream$HeadData var6 = new JceInputStream$HeadData();
            this.readHead(var6);
            if (var6.type == 9) {
                int var4 = this.read(0, 0, true);
                if (var4 < 0) {
                    throw new JceDecodeException("size invalid: " + var4);
                } else {
                    short[] var5 = new short[var4];
                    var2 = 0;

                    while (true) {
                        var1 = var5;
                        if (var2 >= var4) {
                            return var1;
                        }

                        var5[var2] = this.read(var5[0], 0, true);
                        ++var2;
                    }
                }
            }
            throw new JceDecodeException("type mismatch.");
        } else if (var3) {
            throw new JceDecodeException("require field not exist.");
        } else {
            return var1;
        }
    }

    public boolean[] read(boolean[] var1, int var2, boolean var3) {
        var1 = null;
        if (this.skipToTag(var2)) {
            JceInputStream$HeadData var6 = new JceInputStream$HeadData();
            this.readHead(var6);
            if (var6.type == 9) {
                int var4 = this.read(0, 0, true);
                if (var4 < 0) {
                    throw new JceDecodeException("size invalid: " + var4);
                } else {
                    boolean[] var5 = new boolean[var4];
                    var2 = 0;

                    while (true) {
                        var1 = var5;
                        if (var2 >= var4) {
                            return var1;
                        }

                        var5[var2] = this.read(var5[0], 0, true);
                        ++var2;
                    }
                }
            }
            throw new JceDecodeException("type mismatch.");
        } else if (var3) {
            throw new JceDecodeException("require field not exist.");
        } else {
            return var1;
        }
    }

    public <T> List<T> readArray(List<T> var1, int var2, boolean var3) {
        byte var4 = 0;
        if (var1 != null && !var1.isEmpty()) {
            Object[] var6 = this.readArrayImpl(var1.get(0), var2, var3);
            if (var6 == null) {
                return null;
            } else {
                ArrayList var5 = new ArrayList();

                for (var2 = var4; var2 < var6.length; ++var2) {
                    var5.add(var6[var2]);
                }

                return var5;
            }
        } else {
            return new ArrayList<>();
        }
    }

    public <T> T[] readArray(T[] var1, int var2, boolean var3) {
        if (var1 != null && var1.length != 0) {
            return this.readArrayImpl(var1[0], var2, var3);
        } else {
            throw new JceDecodeException("unable to get type of key and value.");
        }
    }

    public String readByteString(String var1, int var2, boolean var3) {
        if (this.skipToTag(var2)) {
            JceInputStream$HeadData var5 = new JceInputStream$HeadData();
            this.readHead(var5);
            byte[] var6;
            switch (var5.type) {
                case 6:
                    byte var4 = this.buffer.get();
                    var2 = var4;
                    if (var4 < 0) {
                        var2 = var4 + 256;
                    }

                    var6 = new byte[var2];
                    this.buffer.get(var6);
                    return HexUtil.bytes2HexStr(var6);
                case 7:
                    var2 = this.buffer.getInt();
                    if (var2 <= 104857600 && var2 >= 0 && var2 <= this.buffer.capacity()) {
                        var6 = new byte[var2];
                        this.buffer.get(var6);
                        return HexUtil.bytes2HexStr(var6);
                    }

                    throw new JceDecodeException("String too long: " + var2);
                default:
                    throw new JceDecodeException("type mismatch.");
            }
        } else if (var3) {
            throw new JceDecodeException("require field not exist.");
        } else {
            return var1;
        }
    }

    public void readHead(JceInputStream$HeadData var1) {
        readHead(var1, this.buffer);
    }

    public List<java.io.Serializable> readList(int var1, boolean var2) {
        ArrayList<java.io.Serializable> var6 = new ArrayList<java.io.Serializable>();
        if (this.skipToTag(var1)) {
            JceInputStream$HeadData var7 = new JceInputStream$HeadData();
            this.readHead(var7);
            if (var7.type == 9) {
                int var5 = this.read(0, 0, true);
                if (var5 < 0) {
                    throw new JceDecodeException("size invalid: " + var5);
                }

                var1 = 0;

                for (; var1 < var5; ++var1) {
                    var7 = new JceInputStream$HeadData();
                    this.readHead(var7);
                    switch (var7.type) {
                        case 0:
                            this.skip(1);
                            break;
                        case 1:
                            this.skip(2);
                            break;
                        case 2:
                            this.skip(4);
                            break;
                        case 3:
                            this.skip(8);
                            break;
                        case 4:
                            this.skip(4);
                            break;
                        case 5:
                            this.skip(8);
                            break;
                        case 6:
                            byte var4 = this.buffer.get();
                            int var3 = var4;
                            if (var4 < 0) {
                                var3 = var4 + 256;
                            }

                            this.skip(var3);
                            break;
                        case 7:
                            this.skip(this.buffer.getInt());
                        case 8:
                        case 9:
                            break;
                        case 10:
                            try {
                                JceStruct var9 = (JceStruct) Class.forName(JceStruct.class.getName()).getConstructor().newInstance();
                                var9.readFrom(this);
                                this.skipToStructEnd();
                                var6.add(var9);
                                break;
                            } catch (Exception var8) {
                                var8.printStackTrace();
                                throw new JceDecodeException("type mismatch." + var8);
                            }
                        case 11:
                        default:
                            throw new JceDecodeException("type mismatch.");
                        case 12:
                            var6.add(new Integer(0));
                    }
                }
            } else {
                throw new JceDecodeException("type mismatch.");
            }
        } else if (var2) {
            throw new JceDecodeException("require field not exist.");
        }

        return var6;
    }

    public <K, V> HashMap<K, V> readMap(Map<K, V> var1, int var2, boolean var3) {
        return (HashMap<K, V>) this.readMap(new HashMap<K, V>(), var1, var2, var3);
    }

    public String readString(int var1, boolean var2) {
        String var4 = null;
        if (this.skipToTag(var1)) {
            JceInputStream$HeadData var8 = new JceInputStream$HeadData();
            this.readHead(var8);
            switch (var8.type) {
                case 6:
                    byte var3 = this.buffer.get();
                    var1 = var3;
                    if (var3 < 0) {
                        var1 = var3 + 256;
                    }

                    byte[] var10 = new byte[var1];
                    this.buffer.get(var10);

                    try {
                        var4 = new String(var10, this.sServerEncoding);
                        break;
                    } catch (UnsupportedEncodingException var7) {
                        return new String(var10);
                    }
                case 7:
                    var1 = this.buffer.getInt();
                    if (var1 <= 104857600 && var1 >= 0 && var1 <= this.buffer.capacity()) {
                        byte[] var9 = new byte[var1];
                        this.buffer.get(var9);

                        try {
                            String var5 = new String(var9, this.sServerEncoding);
                            return var5;
                        } catch (UnsupportedEncodingException var6) {
                            return new String(var9);
                        }
                    }

                    throw new JceDecodeException("String too long: " + var1);
                default:
                    throw new JceDecodeException("type mismatch.");
            }
        } else if (var2) {
            throw new JceDecodeException("require field not exist.");
        }

        return var4;
    }

    public Map<String, String> readStringMap(int var1, boolean var2) {
        HashMap<String, String> var4 = new HashMap<>();
        if (this.skipToTag(var1)) {
            JceInputStream$HeadData var5 = new JceInputStream$HeadData();
            this.readHead(var5);
            if (var5.type == 8) {
                int var3 = this.read(0, 0, true);
                if (var3 < 0) {
                    throw new JceDecodeException("size invalid: " + var3);
                }

                for (var1 = 0; var1 < var3; ++var1) {
                    var4.put(this.readString(0, true), this.readString(1, true));
                }
            } else {
                throw new JceDecodeException("type mismatch.");
            }
        } else if (var2) {
            throw new JceDecodeException("require field not exist.");
        }

        return var4;
    }

    public int setServerEncoding(String var1) {
        this.sServerEncoding = var1;
        return 0;
    }

    public void skipToStructEnd() {
        JceInputStream$HeadData var1 = new JceInputStream$HeadData();

        do {
            this.readHead(var1);
            this.skipField(var1.type);
        } while (var1.type != 11);

    }

    public boolean skipToTag(int n2) {
        try {
            JceInputStream$HeadData jceInputStream$HeadData = new JceInputStream$HeadData();
            do {
                int n3 = this.peakHead(jceInputStream$HeadData);
                if (jceInputStream$HeadData.type == 11) {
                    return false;
                }
                if (n2 <= jceInputStream$HeadData.tag) {
                    return n2 == jceInputStream$HeadData.tag;
                }
                this.skip(n3);
                this.skipField(jceInputStream$HeadData.type);
            } while (true);
        } catch (JceDecodeException jceDecodeException) {
            return false;
        } catch (BufferUnderflowException bufferUnderflowException) {
            // empty catch block
        }
        return false;
    }

    public void warp(byte[] var1) {
        this.wrap(var1);
    }

    public void wrap(byte[] var1) {
        this.buffer = ByteBuffer.wrap(var1);
    }
}
