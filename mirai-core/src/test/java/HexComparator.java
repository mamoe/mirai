import kotlin.ranges.IntRange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.mamoe.mirai.network.Protocol;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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

    private static class ConstMatcher {
        private static final List<Field> CONST_FIELDS = new LinkedList<>() {{
            List.of(Protocol.class).forEach(aClass -> Arrays.stream(aClass.getDeclaredFields()).peek(this::add).forEach(Field::trySetAccessible));
        }};

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
                int index = 0;
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
        StringBuilder hex1b = new StringBuilder();
        StringBuilder hex2b = new StringBuilder();
        int dif = 0;

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

            numberLine.append(UNKNOWN).append(getNumber(i)).append(" ");
            hex1b.append(" ").append(h1).append(" ");
            hex2b.append(" ").append(h2).append(" ");
            if (isDif) {
                ++dif;
            }
        }

        return (builder.append(" ").append(dif).append(" 个不同").append("\n")
                .append(numberLine).append("\n")
                .append(hex1b).append("\n")
                .append(hex2b))
                .toString();
    }

    private static String getNumber(int number) {
        if (number < 10) {
            return "00" + number;
        }
        if (number < 100) {
            return "0" + number;
        }
        return String.valueOf(number);
    }

    public static void main(String[] args) {
        System.out.println(HexComparator.compare(
                "02 37 13 08 36 31 03 76 E4 B8 DD 03 00 00 00 01 01 01 00 00 68 20 00 00 00 00 00 01 01 03 00 19 02 6D 28 41 D2 A5 6F D2 FC 3E 2A 1F 03 75 DE 6E 28 8F A8 19 3E 5F 16 49 D3 00 00 00 10 EF 4A 36 6A 16 A8 E6 3D 2E EA BD 1F 98 C1 3C DA B4 AF 5C C0 01 12 00 38 9B 7C 43 9E FE 4D E8 AB BD A0 25 92 F6 F5 62 F4 9E D6 98 96 87 95 C0 96 47 B7 13 F5 B8 A1 13 46 FE 50 A3 8A 2C 65 95 D5 EF 1F F2 93 95 66 BA 92 39 49 1B 3C BB D5 6F AB 03 0F 00 11 00 0F 44 45 53 4B 54 4F 50 2D 4D 31 37 4A 52 45 55 00 05 00 06 00 02 76 E4 B8 DD 00 06 00 78 5C BB 33 F1 0A 1C BF DF CD 74 CB 03 0B E6 5D 5C 9B EC 02 3D 0D 1E 68 1F 24 82 4C C8 E4 15 38 58 46 23 C8 3F 95 50 4E 15 4B B2 A1 E2 E4 C2 59 A2 59 5A 9F 11 EF 3D 7F 41 D9 01 CB 48 61 37 6D E0 67 AB B8 29 29 43 D8 3E E1 2E E3 58 FB 0D AA DE 8D D8 F1 47 51 29 C7 CE 91 97 4D 1A B6 F7 F5 4D DA 0A 63 9F 81 4D 68 E2 C8 CE 22 2A E7 3A 50 41 92 A1 11 18 DB 9A 82 78 00 15 00 30 00 01 01 27 9B C7 F5 00 10 65 03 FD 8B 00 00 00 00 00 00 00 00 00 00 00 00 02 90 49 55 33 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B 00 1A 00 40 1A B3 88 3C 6E 39 D0 88 49 AC 2C 89 8E C6 CF 1D 56 BB 01 54 62 5F 87 40 B3 6C E0 E3 84 7C FD 15 1D E0 B6 39 A5 8E 9F FF 36 40 F1 E8 26 0E EB 39 03 C9 0F A2 D7 BD FA 67 91 82 E8 BE 74 2D 93 AF 00 18 00 16 00 01 00 00 04 53 00 00 00 01 00 00 15 85 76 E4 B8 DD 00 00 00 00 01 03 00 14 00 01 00 10 60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6 03 12 00 05 01 00 00 00 01 05 08 00 05 10 00 00 00 00 03 13 00 19 01 01 02 00 10 04 EA 78 D1 A4 FF CD CC 7C B8 D4 12 7D BB 03 AA 00 00 00 00 01 02 00 62 00 01 04 EB B7 C1 86 F9 08 96 ED 56 84 AB 50 85 2E 48 00 38 E9 AA 2B 4D 26 4C 76 18 FE 59 D5 A9 82 6A 0C 04 B4 49 50 D7 9B B1 FE 5D 97 54 8D 82 F3 22 C2 48 B9 C9 22 69 CA 78 AD 3E 2D E9 C9 DF A8 9E 7D 8C 8D 6B DF 4C D7 34 D0 D3 00 14 9E 12 A9 12 02 D7 77 E7 C1 61 10 F7 C5 AA F6 4E 12 CA BF D9 B2 33 05 C4 EF 23 E6 0B 90 23 7A FC EF 31 06 1A 3B 03\n\n"
                ,
                "02 37 13 08 36 31 03 76 E4 B8 DD 03 00 00 00 01 01 01 00 00 68 20 00 00 00 00 00 01 01 03 00 19 02 6D 28 41 D2 A5 6F D2 FC 3E 2A 1F 03 75 DE 6E 28 8F A8 19 3E 5F 16 49 D3 00 00 00 10 EF 4A 36 6A 16 A8 E6 3D 2E EA BD 1F 98 C1 3C DA EC CD 3D 2F 35 55 4C 8E B3 2C D9 55 E1 CE 71 DD 16 9C BB 24 E9 2C A6 98 77 A4 49 AB 2B EF 30 80 09 EB E3 04 4E AD D5 F4 E9 F3 55 54 D8 AC 9B F2 D6 78 25 F1 DF AF E2 7C 95 85 CB CF D6 FD 32 CE F5 4B 20 EA B8 D1 2D 77 69 75 40 6C 88 F0 6D 47 48 C6 5A 61 53 53 A3 58 A8 2E C2 F6 E2 44 E1 C2 8B 62 57 E2 8C 72 3C F7 5E EC 7E 48 BD 55 B4 83 38 0C D6 0A 1D 82 CA B5 CC 94 F8 53 E4 00 44 21 5E 26 39 1C EF 1E 5B 6C 94 F0 22 B9 FD 31 35 CF 8B B4 51 00 3A 0C FC 48 93 59 65 B6 C5 79 20 7B F3 3F 4F EA 1F 31 73 5F 93 0B 90 22 16 E1 02 30 DC 44 FC 87 D3 53 E4 E9 30 4A 92 E2 5B 6E EE 88 7B AC 03 17 67 89 DF 91 E4 9D 27 FF CA 04 D3 53 D9 39 52 47 63 F4 24 49 F1 1C 38 9F 01 6D DC AE 62 5A 37 7A 97 2B FA 0F AD DC 11 51 19 95 89 B9 9D DB C0 FD 3E 78 FF C0 1C 93 44 10 17 29 C1 7F F5 8B 31 E2 10 C4 F9 71 66 F5 05 D2 2A 9C EB 98 4D B7 71 5C F6 B6 7C 82 12 82 DF D6 40 0E C0 35 8F E7 22 D0 95 B4 27 42 DA CF CD 96 C1 06 6E 71 CD B7 A8 D5 0C 6D B1 09 F3 79 CB F6 DD 28 AF BC 0B 21 6F 82 37 B6 18 3D 88 CD 7E C6 FE 50 0D CE 3D 7E BB 5B B1 02 B0 B8 EE 90 59 C1 81 E8 D3 9C 6C ED C7 C5 45 F2 4B A3 40 A6 21 57 A0 0C 94 57 A3 7C 92 7C 6B 62 34 1A B1 AD EA 82 A0 55 E5 49 0D 08 E7 B3 B6 3D 0C 14 EF E9 D6 6D 0B C5 31 2B 07 23 41 1B 03 E6 5B B7 86 A7 7F 62 D8 ED 12 40 1B 8F 28 8F D1 FE 41 73 52 9D 25 08 94 B4 B6 6B B1 AB A4 50 87 9C 87 D2 1C C6 23 BD 28 50 6E 4C 20 2A 24 08 0C 08 E5 2E 7C 50 9C C8 34 D5 BD 81 D9 20 90 AB 4E FA 7F 5F B0 BB 18 54 33 65 CA 53 2E A0 6C 42 30 BD 8F EF E6 82 D9 21 1F C6 63 1C D3 57 D0 89 39 7C B7 71 EC DB 46 26 6E 5A F8 75 12 03 64 4C 94 D1 6B 7A 79 CE 28 8E 95 F4 00 CF 95 8A A0 DA 44 4A A5 5D 73 E5 F9 6C 83 2E E1 6E 03\n"
        ));

        System.out.println(HexComparator.compare(
                //e
                "90 5E 39 DF 00 02 76 E4 B8 DD 00 00 04 53 00 00 00 01 00 00 15 85 00 00 01 55 35 05 8E C9 BA 16 D0 01 63 5B 59 4B 59 52 31 01 B9 00 00 00 00 00 00 00 00 00 00 00 00 00 7B 7B 7B 7B 00 00 00 00 00 00 00 00 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B AA BB CC DD EE FF AA BB CC",
                //mirai
                "6F 0B DF 92 00 02 76 E4 B8 DD 00 00 04 53 00 00 00 01 00 00 15 85 00 00 01 55 35 05 8E C9 BA 16 D0 01 63 5B 59 4B 59 52 31 01 B9 00 00 00 00 00 00 00 00 00 00 00 00 00 E9 E9 E9 E9 00 00 00 00 00 00 00 00 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B AA BB CC DD EE FF AA BB CC\n\n\n"
        ));
    }
}
