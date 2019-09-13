import kotlin.ranges.IntRange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.mamoe.mirai.network.Protocol;
import net.mamoe.mirai.network.packet.ClientPacketKt;
import net.mamoe.mirai.utils.UtilsKt;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * This could be used to check packet encoding..
 * but better to run under UNIX
 *
 * @author NaturalHG
 */
public class HexComparator {

    /**
     * a string result
     */

    private static final String RED = "\033[31m";

    private static final String GREEN = "\033[33m";

    private static final String UNKNOWN = "\033[30m";

    private static final String BLUE = "\033[34m";

    public static final List<HexReader> consts = new LinkedList<>() {{
        add(new HexReader("90 5E 39 DF 00 02 76 E4 B8 DD 00"));
    }};

    private static class ConstMatcher {
        private static final List<Field> CONST_FIELDS = new LinkedList<>() {{
            List.of(Protocol.class).forEach(aClass -> Arrays.stream(aClass.getDeclaredFields()).peek(this::add).forEach(Field::trySetAccessible));
            List.of(TestConsts.class).forEach(aClass -> Arrays.stream(aClass.getDeclaredFields()).peek(this::add).forEach(Field::trySetAccessible));
        }};

        @SuppressWarnings({"unused", "NonAsciiCharacters"})
        private static class TestConsts {
            private static final String NIU_BI = UtilsKt.toUHexString("牛逼".getBytes(), " ");
            private static final String _1994701021 = ClientPacketKt.toUHexString(1994701021, " ");
            private static final String _1040400290 = ClientPacketKt.toUHexString(1040400290, " ");
            private static final String _580266363 = ClientPacketKt.toUHexString(580266363, " ");

            private static final String _1040400290_ = "3E 03 3F A2";
            private static final String _1994701021_ = "76 E4 B8 DD";
            private static final String _jiahua_ = "B1 89 BE 09";
            private static final String _Him188moe_ = UtilsKt.toUHexString("Him188moe".getBytes(), " ");
            private static final String 发图片 = UtilsKt.toUHexString("发图片".getBytes(), " ");
            private static final String 群 = UtilsKt.toUHexString("发图片".getBytes(), " ");

            private static final String SINGLE_PLAIN_MESSAGE_HEAD = "00 00 01 00 09 01";

            private static final String MESSAGE_TAIL_10404 = "0E  00  07  01  00  04  00  00  00  09 19  00  18  01  00  15  AA  02  12  9A  01  0F  80  01  01  C8  01  00  F0  01  00  F8  01  00  90  02  00".replace("  ", " ");
            //private static final String MESSAGE_TAIL2_10404 ="".replace("  ", " ");

        }

        private final List<Match> matches = new LinkedList<>();

        private ConstMatcher(String hex) {
            CONST_FIELDS.forEach(field -> {
                for (IntRange match : match(hex, field)) {
                    matches.add(new Match(match, field.getName()));
                }
            });
        }

        private String getMatchedConstName(int hexNumber) {
            for (Match match : this.matches) {
                if (match.range.contains(hexNumber)) {
                    return match.constName;
                }
            }
            return null;
        }

        private static List<IntRange> match(String hex, Field field) {
            final String constValue;
            try {
                constValue = ((String) field.get(null)).trim();
                if (constValue.length() / 3 <= 3) {//Minimum numbers of const hex bytes
                    return new LinkedList<>();
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (ClassCastException ignored) {
                return new LinkedList<>();
            }
            return new LinkedList<>() {{
                int index = -1;
                while ((index = hex.indexOf(constValue, index + 1)) != -1) {
                    add(new IntRange(index / 3, (index + constValue.length()) / 3));
                }
            }};
        }

        @ToString
        @Getter
        @AllArgsConstructor
        private static class Match {
            private IntRange range;
            private String constName;
        }
    }

    private static void buildConstNameChain(int length, ConstMatcher constMatcher, StringBuilder constNameBuilder) {
        //System.out.println(constMatcher.matches);
        for (int i = 0; i < length; i++) {
            constNameBuilder.append(" ");
            String match = constMatcher.getMatchedConstName(i / 4);
            if (match != null) {
                int appendedNameLength = match.length();
                constNameBuilder.append(match);
                while (match.equals(constMatcher.getMatchedConstName(i++ / 4))) {
                    if (appendedNameLength-- < 0) {
                        constNameBuilder.append(" ");
                    }
                }

                constNameBuilder.append(" ".repeat(match.length() % 4));
            }
        }
    }

    private static String compare(String hex1s, String hex2s) {
        StringBuilder builder = new StringBuilder();

        String[] hex1 = hex1s.trim().replace("\n", "").split(" ");
        String[] hex2 = hex2s.trim().replace("\n", "").split(" ");
        ConstMatcher constMatcher1 = new ConstMatcher(hex1s);
        ConstMatcher constMatcher2 = new ConstMatcher(hex2s);

        if (hex1.length == hex2.length) {
            builder.append(GREEN).append("长度一致:").append(hex1.length);
        } else {
            builder.append(RED).append("长度不一致").append(hex1.length).append("/").append(hex2.length);
        }


        StringBuilder numberLine = new StringBuilder();
        StringBuilder hex1ConstName = new StringBuilder();
        StringBuilder hex1b = new StringBuilder();
        StringBuilder hex2b = new StringBuilder();
        StringBuilder hex2ConstName = new StringBuilder();
        int dif = 0;

        int length = Math.max(hex1.length, hex2.length) * 4;
        buildConstNameChain(length, constMatcher1, hex1ConstName);
        buildConstNameChain(length, constMatcher2, hex2ConstName);


        for (int i = 0; i < Math.max(hex1.length, hex2.length); ++i) {
            String h1 = null;
            String h2 = null;
            boolean isDif = false;
            if (hex1.length <= i) {
                h1 = RED + "__";
                isDif = true;
            } else {
                String matchedConstName = constMatcher1.getMatchedConstName(i);
                if (matchedConstName != null) {
                    h1 = BLUE + hex1[i];
                }
            }
            if (hex2.length <= i) {
                h2 = RED + "__";
                isDif = true;
            } else {
                String matchedConstName = constMatcher2.getMatchedConstName(i);
                if (matchedConstName != null) {
                    h2 = BLUE + hex2[i];
                }
            }

            if (h1 == null && h2 == null) {
                h1 = hex1[i];
                h2 = hex2[i];
                if (h1.equals(h2)) {
                    h1 = GREEN + h1;
                    h2 = GREEN + h2;
                } else {
                    h1 = RED + h1;
                    h2 = RED + h2;
                    isDif = true;
                }
            } else {
                if (h1 == null) {
                    h1 = RED + hex1[i];
                }
                if (h2 == null) {
                    h2 = RED + hex2[i];
                }
            }

            numberLine.append(UNKNOWN).append(getFixedNumber(i)).append(" ");
            hex1b.append(" ").append(h1).append(" ");
            hex2b.append(" ").append(h2).append(" ");
            if (isDif) {
                ++dif;
            }

            //doConstReplacement(hex1b);
            //doConstReplacement(hex2b);
        }

        return (builder.append(" ").append(dif).append(" 个不同").append("\n")
                .append(numberLine).append("\n")
                .append(hex1ConstName).append("\n")
                .append(hex1b).append("\n")
                .append(hex2b).append("\n")
                .append(hex2ConstName).append("\n")
        )
                .toString();


    }


    private static void doConstReplacement(StringBuilder builder) {
        String mirror = builder.toString();
        HexReader hexs = new HexReader(mirror);
        for (AtomicInteger i = new AtomicInteger(0); i.get() < builder.length(); i.addAndGet(1)) {
            hexs.setTo(i.get());
            consts.forEach(a -> {
                hexs.setTo(i.get());
                List<Integer> posToPlaceColor = new LinkedList<>();
                AtomicBoolean is = new AtomicBoolean(false);

                a.readFully((c, d) -> {
                    if (c.equals(hexs.readHex())) {
                        posToPlaceColor.add(d);
                    } else {
                        is.set(false);
                    }
                });

                if (is.get()) {
                    AtomicInteger adder = new AtomicInteger();
                    posToPlaceColor.forEach(e -> {
                        builder.insert(e + adder.getAndAdd(BLUE.length()), BLUE);
                    });
                }
            });
        }
    }

    private static String getFixedNumber(int number) {
        if (number < 10) {
            return "00" + number;
        }
        if (number < 100) {
            return "0" + number;
        }
        return String.valueOf(number);
    }

    private static String getClipboardString() {
        Transferable trans = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                return (String) trans.getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Hex1: ");
            var hex1 = scanner.nextLine();
            System.out.println("Hex2: ");
            var hex2 = scanner.nextLine();
            System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
            System.out.println(HexComparator.compare(hex1, hex2));
            System.out.println();
        }
/*
        System.out.println(HexComparator.compare(
                //mirai

                "2A 22 96 29 7B 00 40 00 01 01 00 00 00 00 00 00 00 4D 53 47 00 00 00 00 00 EC 21 40 06 18 89 54 BC Protocol.messageConst1 00 00 01 00 0A 01 00 07 E7 89 9B E9 80 BC 21\n"
                ,
                //e
                "2A 22 96 29 7B 00 3F 00 01 01 00 00 00 00 00 00 00 4D 53 47 00 00 00 00 00 5D 6B 8E 1A FE 39 0B FC Protocol.messageConst1 00 00 01 00 0A 01 00 07 6D 65 73 73 61 67 65"
        ));


        /*
        System.out.println(HexComparator.compare(
                //e
                "90 5E 39 DF 00 02 76 E4 B8 DD 00 00 04 53 00 00 00 01 00 00 15 85 00 00 01 55 35 05 8E C9 BA 16 D0 01 63 5B 59 4B 59 52 31 01 B9 00 00 00 00 00 00 00 00 00 00 00 00 00 7B 7B 7B 7B 00 00 00 00 00 00 00 00 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B AA BB CC DD EE FF AA BB CC",
                //mirai
                "6F 0B DF 92 00 02 76 E4 B8 DD 00 00 04 53 00 00 00 01 00 00 15 85 00 00 01 55 35 05 8E C9 BA 16 D0 01 63 5B 59 4B 59 52 31 01 B9 00 00 00 00 00 00 00 00 00 00 00 00 00 E9 E9 E9 E9 00 00 00 00 00 00 00 00 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B AA BB CC DD EE FF AA BB CC\n\n\n"
        ));*/
    }
}

class HexReader {
    private String s;
    private int pos = 0;
    private int lastHaxPos = 0;


    public HexReader(String s) {
        this.s = s;
    }

    public String readHex() {
        boolean isStr = false;
        String next = "";
        for (; pos < s.length() - 2; ++pos) {

            char s1 = ' ';
            if (pos != 0) {
                s1 = this.s.charAt(0);
            }
            char s2 = this.s.charAt(pos + 1);
            char s3 = this.s.charAt(pos + 2);
            char s4 = ' ';
            if (this.s.length() != (this.pos + 3)) {
                s4 = this.s.charAt(pos + 3);
            }
            if (
                    Character.isSpaceChar(s1) && Character.isSpaceChar(s4)
                            &&
                            (Character.isDigit(s2) || Character.isAlphabetic(s2))
                            &&
                            (Character.isDigit(s3) || Character.isAlphabetic(s3))
            ) {
                this.pos += 2;
                this.lastHaxPos = this.pos + 1;
                return String.valueOf(s2) + s3;
            }
        }
        return "";
    }

    public void readFully(BiConsumer<String, Integer> processor) {
        this.reset();
        String nextHax = this.readHex();
        while (!nextHax.equals(" ")) {
            processor.accept(nextHax, this.lastHaxPos);
            nextHax = this.readHex();
        }
    }

    public void setTo(int pos) {
        this.pos = pos;
    }

    public void reset() {
        this.pos = 0;
    }
}

