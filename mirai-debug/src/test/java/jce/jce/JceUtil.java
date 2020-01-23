package jce.jce;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

public final class JceUtil {
    private static final byte[] highDigits;
    private static final int iConstant = 37;
    private static final int iTotal = 17;
    private static final byte[] lowDigits;

    static {
        byte[] var1 = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};
        byte[] var2 = new byte[256];
        byte[] var3 = new byte[256];

        for (int var0 = 0; var0 < 256; ++var0) {
            var2[var0] = var1[var0 >>> 4];
            var3[var0] = var1[var0 & 15];
        }

        highDigits = var2;
        lowDigits = var3;
    }

    public static int compareTo(byte var0, byte var1) {
        if (var0 < var1) {
            return -1;
        } else {
            return var0 > var1 ? 1 : 0;
        }
    }

    public static int compareTo(char var0, char var1) {
        if (var0 < var1) {
            return -1;
        } else {
            return var0 > var1 ? 1 : 0;
        }
    }

    public static int compareTo(double var0, double var2) {
        if (var0 < var2) {
            return -1;
        } else {
            return var0 > var2 ? 1 : 0;
        }
    }

    public static int compareTo(float var0, float var1) {
        if (var0 < var1) {
            return -1;
        } else {
            return var0 > var1 ? 1 : 0;
        }
    }

    public static int compareTo(int var0, int var1) {
        if (var0 < var1) {
            return -1;
        } else {
            return var0 > var1 ? 1 : 0;
        }
    }

    public static int compareTo(long var0, long var2) {
        if (var0 < var2) {
            return -1;
        } else {
            return var0 > var2 ? 1 : 0;
        }
    }

    public static <T extends Comparable<T>> int compareTo(T var0, T var1) {
        return var0.compareTo(var1);
    }

    public static <T extends Comparable<T>> int compareTo(List<T> var0, List<T> var1) {
        Iterator var3 = var0.iterator();
        Iterator var4 = var1.iterator();

        while (var3.hasNext() && var4.hasNext()) {
            int var2 = ((Comparable) var3.next()).compareTo(var4.next());
            if (var2 != 0) {
                return var2;
            }
        }

        return compareTo(var3.hasNext(), var4.hasNext());
    }

    public static int compareTo(short var0, short var1) {
        if (var0 < var1) {
            return -1;
        } else {
            return var0 > var1 ? 1 : 0;
        }
    }

    public static int compareTo(boolean var0, boolean var1) {
        byte var3 = 1;
        byte var2;
        if (var0) {
            var2 = 1;
        } else {
            var2 = 0;
        }

        if (!var1) {
            var3 = 0;
        }

        return var2 - var3;
    }

    public static int compareTo(byte[] var0, byte[] var1) {
        int var3 = 0;

        for (int var2 = 0; var2 < var0.length && var3 < var1.length; ++var2) {
            int var4 = compareTo(var0[var2], var1[var3]);
            if (var4 != 0) {
                return var4;
            }

            ++var3;
        }

        return compareTo(var0.length, var1.length);
    }

    public static int compareTo(char[] var0, char[] var1) {
        int var3 = 0;

        for (int var2 = 0; var2 < var0.length && var3 < var1.length; ++var2) {
            int var4 = compareTo(var0[var2], var1[var3]);
            if (var4 != 0) {
                return var4;
            }

            ++var3;
        }

        return compareTo(var0.length, var1.length);
    }

    public static int compareTo(double[] var0, double[] var1) {
        int var3 = 0;

        for (int var2 = 0; var2 < var0.length && var3 < var1.length; ++var2) {
            int var4 = compareTo(var0[var2], var1[var3]);
            if (var4 != 0) {
                return var4;
            }

            ++var3;
        }

        return compareTo(var0.length, var1.length);
    }

    public static int compareTo(float[] var0, float[] var1) {
        int var3 = 0;

        for (int var2 = 0; var2 < var0.length && var3 < var1.length; ++var2) {
            int var4 = compareTo(var0[var2], var1[var3]);
            if (var4 != 0) {
                return var4;
            }

            ++var3;
        }

        return compareTo(var0.length, var1.length);
    }

    public static int compareTo(int[] var0, int[] var1) {
        int var3 = 0;

        for (int var2 = 0; var2 < var0.length && var3 < var1.length; ++var2) {
            int var4 = compareTo(var0[var2], var1[var3]);
            if (var4 != 0) {
                return var4;
            }

            ++var3;
        }

        return compareTo(var0.length, var1.length);
    }

    public static int compareTo(long[] var0, long[] var1) {
        int var3 = 0;

        for (int var2 = 0; var2 < var0.length && var3 < var1.length; ++var2) {
            int var4 = compareTo(var0[var2], var1[var3]);
            if (var4 != 0) {
                return var4;
            }

            ++var3;
        }

        return compareTo(var0.length, var1.length);
    }

    public static <T extends Comparable<T>> int compareTo(T[] var0, T[] var1) {
        int var3 = 0;

        for (int var2 = 0; var2 < var0.length && var3 < var1.length; ++var2) {
            int var4 = var0[var2].compareTo(var1[var3]);
            if (var4 != 0) {
                return var4;
            }

            ++var3;
        }

        return compareTo(var0.length, var1.length);
    }

    public static int compareTo(short[] var0, short[] var1) {
        int var3 = 0;

        for (int var2 = 0; var2 < var0.length && var3 < var1.length; ++var2) {
            int var4 = compareTo(var0[var2], var1[var3]);
            if (var4 != 0) {
                return var4;
            }

            ++var3;
        }

        return compareTo(var0.length, var1.length);
    }

    public static int compareTo(boolean[] var0, boolean[] var1) {
        int var3 = 0;

        for (int var2 = 0; var2 < var0.length && var3 < var1.length; ++var2) {
            int var4 = compareTo(var0[var2], var1[var3]);
            if (var4 != 0) {
                return var4;
            }

            ++var3;
        }

        return compareTo(var0.length, var1.length);
    }

    public static boolean equals(byte var0, byte var1) {
        return var0 == var1;
    }

    public static boolean equals(char var0, char var1) {
        return var0 == var1;
    }

    public static boolean equals(double var0, double var2) {
        return var0 == var2;
    }

    public static boolean equals(float var0, float var1) {
        return var0 == var1;
    }

    public static boolean equals(int var0, int var1) {
        return var0 == var1;
    }

    public static boolean equals(long var0, long var2) {
        return var0 == var2;
    }

    public static boolean equals(Object var0, Object var1) {
        return var0.equals(var1);
    }

    public static boolean equals(short var0, short var1) {
        return var0 == var1;
    }

    public static boolean equals(boolean var0, boolean var1) {
        return var0 == var1;
    }

    public static String getHexdump(ByteBuffer var0) {
        int var1 = var0.remaining();
        if (var1 == 0) {
            return "empty";
        } else {
            StringBuffer var4 = new StringBuffer(var0.remaining() * 3 - 1);
            int var2 = var0.position();
            int var3 = var0.get() & 255;
            var4.append((char) highDigits[var3]);
            var4.append((char) lowDigits[var3]);
            --var1;

            while (var1 > 0) {
                var4.append(' ');
                var3 = var0.get() & 255;
                var4.append((char) highDigits[var3]);
                var4.append((char) lowDigits[var3]);
                --var1;
            }

            var0.position(var2);
            return var4.toString();
        }
    }

    public static String getHexdump(byte[] var0) {
        return getHexdump(ByteBuffer.wrap(var0));
    }

    public static byte[] getJceBufArray(ByteBuffer var0) {
        byte[] var1 = new byte[var0.position()];
        System.arraycopy(var0.array(), 0, var1, 0, var1.length);
        return var1;
    }

    public static int hashCode(byte var0) {
        return var0 + 629;
    }

    public static int hashCode(char var0) {
        return var0 + 629;
    }

    public static int hashCode(double var0) {
        return hashCode(Double.doubleToLongBits(var0));
    }

    public static int hashCode(float var0) {
        return Float.floatToIntBits(var0) + 629;
    }

    public static int hashCode(int var0) {
        return var0 + 629;
    }

    public static int hashCode(long var0) {
        return (int) (var0 >> 32 ^ var0) + 629;
    }

    public static int hashCode(Object var0) {
        if (var0 == null) {
            return 629;
        } else if (var0.getClass().isArray()) {
            if (var0 instanceof long[]) {
                return hashCode((long[]) ((long[]) var0));
            } else if (var0 instanceof int[]) {
                return hashCode((int[]) ((int[]) var0));
            } else if (var0 instanceof short[]) {
                return hashCode((short[]) ((short[]) var0));
            } else if (var0 instanceof char[]) {
                return hashCode((char[]) ((char[]) var0));
            } else if (var0 instanceof byte[]) {
                return hashCode((byte[]) ((byte[]) var0));
            } else if (var0 instanceof double[]) {
                return hashCode((double[]) ((double[]) var0));
            } else if (var0 instanceof float[]) {
                return hashCode((float[]) ((float[]) var0));
            } else if (var0 instanceof boolean[]) {
                return hashCode((boolean[]) ((boolean[]) var0));
            } else {
                return var0 instanceof JceStruct[] ? hashCode((JceStruct[]) ((JceStruct[]) var0)) : hashCode((Object) ((Object[]) ((Object[]) var0)));
            }
        } else {
            return var0 instanceof JceStruct ? var0.hashCode() : var0.hashCode() + 629;
        }
    }

    public static int hashCode(short var0) {
        return var0 + 629;
    }

    public static int hashCode(boolean var0) {
        byte var1;
        if (var0) {
            var1 = 0;
        } else {
            var1 = 1;
        }

        return var1 + 629;
    }

    public static int hashCode(byte[] var0) {
        int var3;
        if (var0 == null) {
            var3 = 629;
        } else {
            int var1 = 17;
            int var2 = 0;

            while (true) {
                var3 = var1;
                if (var2 >= var0.length) {
                    break;
                }

                var1 = var1 * 37 + var0[var2];
                ++var2;
            }
        }

        return var3;
    }

    public static int hashCode(char[] var0) {
        int var3;
        if (var0 == null) {
            var3 = 629;
        } else {
            int var1 = 17;
            int var2 = 0;

            while (true) {
                var3 = var1;
                if (var2 >= var0.length) {
                    break;
                }

                var1 = var1 * 37 + var0[var2];
                ++var2;
            }
        }

        return var3;
    }

    public static int hashCode(double[] var0) {
        int var3;
        if (var0 == null) {
            var3 = 629;
        } else {
            int var1 = 17;
            int var2 = 0;

            while (true) {
                var3 = var1;
                if (var2 >= var0.length) {
                    break;
                }

                var1 = var1 * 37 + (int) (Double.doubleToLongBits(var0[var2]) ^ Double.doubleToLongBits(var0[var2]) >> 32);
                ++var2;
            }
        }

        return var3;
    }

    public static int hashCode(float[] var0) {
        int var3;
        if (var0 == null) {
            var3 = 629;
        } else {
            int var1 = 17;
            int var2 = 0;

            while (true) {
                var3 = var1;
                if (var2 >= var0.length) {
                    break;
                }

                var1 = var1 * 37 + Float.floatToIntBits(var0[var2]);
                ++var2;
            }
        }

        return var3;
    }

    public static int hashCode(int[] var0) {
        int var3;
        if (var0 == null) {
            var3 = 629;
        } else {
            int var1 = 17;
            int var2 = 0;

            while (true) {
                var3 = var1;
                if (var2 >= var0.length) {
                    break;
                }

                var1 = var1 * 37 + var0[var2];
                ++var2;
            }
        }

        return var3;
    }

    public static int hashCode(long[] var0) {
        int var3;
        if (var0 == null) {
            var3 = 629;
        } else {
            int var1 = 17;
            int var2 = 0;

            while (true) {
                var3 = var1;
                if (var2 >= var0.length) {
                    break;
                }

                var1 = var1 * 37 + (int) (var0[var2] ^ var0[var2] >> 32);
                ++var2;
            }
        }

        return var3;
    }

    public static int hashCode(JceStruct[] var0) {
        int var3;
        if (var0 == null) {
            var3 = 629;
        } else {
            int var1 = 17;
            int var2 = 0;

            while (true) {
                var3 = var1;
                if (var2 >= var0.length) {
                    break;
                }

                var1 = var1 * 37 + var0[var2].hashCode();
                ++var2;
            }
        }

        return var3;
    }

    public static int hashCode(short[] var0) {
        int var3;
        if (var0 == null) {
            var3 = 629;
        } else {
            int var1 = 17;
            int var2 = 0;

            while (true) {
                var3 = var1;
                if (var2 >= var0.length) {
                    break;
                }

                var1 = var1 * 37 + var0[var2];
                ++var2;
            }
        }

        return var3;
    }

    public static int hashCode(boolean[] var0) {
        int var3;
        if (var0 == null) {
            var3 = 629;
        } else {
            int var1 = 17;
            int var2 = 0;

            while (true) {
                var3 = var1;
                if (var2 >= var0.length) {
                    break;
                }

                byte var4;
                if (var0[var2]) {
                    var4 = 0;
                } else {
                    var4 = 1;
                }

                var1 = var4 + var1 * 37;
                ++var2;
            }
        }

        return var3;
    }
}
