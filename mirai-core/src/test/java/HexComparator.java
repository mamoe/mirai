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
                //mirai
                "01 12 00 38 4A 89 FA EE 24 BC B5 EC 6D DD A7 EE 9E 89 2C 00 4F BF 05 F3 98 46 5A 52 9E 8A 2A 14 96 ED F3 98 06 42 72 50 D0 5A CA 85 E1 68 2F 28 D3 84 0E 92 98 3E 3C 82 3F 59 5C A8 03 0F 00 11 00 0F 44 45 53 4B 54 4F 50 2D 4D 31 37 4A 52 45 55 00 05 00 06 00 02 76 E4 B8 DD 00 06 00 78 75 2A 22 99 DD A6 EE 95 9E 7F 11 F0 C7 93 FB E7 51 C5 72 22 FF DE 73 DC 5D 13 0E C0 7D 84 CF 18 4B 6B 10 E5 04 A4 40 79 45 6E 54 69 E5 8E FE 59 7A FF 68 E4 BB 5B 90 DE 07 1D A9 D6 64 5A 25 5E 2C 2F FB CF CA 37 57 05 F9 36 B5 58 BC E0 4D 1B 8A 7F A1 54 00 86 9C 30 47 C6 2E D1 42 E1 F8 4F 7F 5D 7E 80 BB EA 84 AF EA 98 47 9A F6 93 6D 35 00 15 00 30 00 01 01 27 9B C7 F5 00 10 65 03 FD 8B 00 00 00 00 00 00 00 00 00 00 00 00 02 90 49 55 33 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B 00 1A 00 40 19 B6 E2 4C 38 36 3F 8F E9 CD 18 54 93 F5 52 A7 2E 04 6C ED 58 39 92 5B 17 B6 F7 C0 45 07 49 0B A0 AF 35 E0 E2 49 98 29 B2 FB A7 7E D6 0F 96 BB FB FD C1 56 77 E0 3A 3E 32 E8 E7 CB 78 B2 9A D0 00 18 00 16 00 01 00 00 04 53 00 00 00 01 00 00 15 85 76 E4 B8 DD 00 00 00 00 01 03 00 14 00 01 00 10 60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6 03 12 00 05 01 00 00 00 01 05 08 00 05 10 00 00 00 00 03 13 00 19 01 01 02 00 10 04 EA 78 D1 A4 FF CD CC 7C B8 D4 12 7D BB 03 AA 00 00 00 00 01 02 00 62 00 01 04 EB B7 C1 86 F9 08 96 ED 56 84 AB 50 85 2E 48 00 38 E9 AA 2B 4D 26 4C 76 18 FE 59 D5 A9 82 6A 0C 04 B4 49 50 D7 9B B1 FE 5D 97 54 8D 82 F3 22 C2 48 B9 C9 22 69 CA 78 AD 3E 2D E9 C9 DF A8 9E 7D 8C 8D 6B DF 4C D7 34 D0 D3 00 14 79 FE 6A 47 E3 97 C4 5A 0D 38 BA CB 7F 47 47 D3 4F 63 30 53\n\n"
                ,
                //e
                "01 12 00 38 B5 E3 C5 4F F9 91 D1 10 B2 A0 CE 2B 5C C3 F3 6E 1F 0C 01 F5 89 13 50 A1 5F 9C C9 BD 58 09 C3 84 3D C0 1E EA 12 3F A3 F2 47 D0 77 19 8A 24 92 87 8B AD AD 16 97 7A 66 AD 03 0F 00 11 00 0F 44 45 53 4B 54 4F 50 2D 4D 31 37 4A 52 45 55 00 05 00 06 00 02 76 E4 B8 DD 00 06 00 78 4A AE 12 18 80 AC FE D3 7E 37 CA D8 9C 93 99 68 09 2B A8 FA B1 92 4A EB 8F 6E 69 EA F9 59 25 CA 39 90 69 A5 A7 15 73 97 52 9A B7 85 16 04 8E A8 CF 97 84 CD 13 67 A1 A9 C2 41 99 E7 74 96 C0 FC F9 5E 46 06 18 71 FF 85 7E EE 8A 62 05 90 F3 83 E2 97 10 5E 3C FE 70 F6 0B 92 2D B8 C7 A2 EE FE DD EE EF 8C 5E 86 EB D9 01 66 DB F1 31 DD 27 44 83 7F E4 5E 93 9C C6 05 00 15 00 30 00 01 01 27 9B C7 F5 00 10 65 03 FD 8B 00 00 00 00 00 00 00 00 00 00 00 00 02 90 49 55 33 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B 00 1A 00 40 EB E2 37 37 17 6D DB 96 38 C8 F3 67 12 50 80 9A C5 5E CB 8C 45 55 9F E4 7D 4F 4F F3 D2 8E BA AD 4A 0B F3 3B 99 B9 A5 CD 76 D7 4A 4B 69 9D 9F 21 36 A0 0F 94 CB 69 44 7A 87 39 27 23 F6 4A 66 D1 00 18 00 16 00 01 00 00 04 53 00 00 00 01 00 00 15 85 76 E4 B8 DD 00 00 00 00 01 03 00 14 00 01 00 10 60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6 03 12 00 05 01 00 00 00 01 05 08 00 05 01 00 00 00 00 03 13 00 19 01 01 02 00 10 04 EA 78 D1 A4 FF CD CC 7C B8 D4 12 7D BB 03 AA 00 00 00 00 01 02 00 62 00 01 04 EB B7 C1 86 F9 08 96 ED 56 84 AB 50 85 2E 48 00 38 E9 AA 2B 4D 26 4C 76 18 FE 59 D5 A9 82 6A 0C 04 B4 49 50 D7 9B B1 FE 5D 97 54 8D 82 F3 22 C2 48 B9 C9 22 69 CA 78 AD 3E 2D E9 C9 DF A8 9E 7D 8C 8D 6B DF 4C D7 34 D0 D3 00 14 12 C9 35 A8 26 70 AA 5A F7 F8 0B E6 6A FC 85 19 00 D4 CE EC  "
        ));

        System.out.println(HexComparator.compare(
                //e
                "90 5E 39 DF 00 02 76 E4 B8 DD 00 00 04 53 00 00 00 01 00 00 15 85 00 00 01 55 35 05 8E C9 BA 16 D0 01 63 5B 59 4B 59 52 31 01 B9 00 00 00 00 00 00 00 00 00 00 00 00 00 7B 7B 7B 7B 00 00 00 00 00 00 00 00 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B AA BB CC DD EE FF AA BB CC",
                //mirai
                "6F 0B DF 92 00 02 76 E4 B8 DD 00 00 04 53 00 00 00 01 00 00 15 85 00 00 01 55 35 05 8E C9 BA 16 D0 01 63 5B 59 4B 59 52 31 01 B9 00 00 00 00 00 00 00 00 00 00 00 00 00 E9 E9 E9 E9 00 00 00 00 00 00 00 00 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B AA BB CC DD EE FF AA BB CC\n\n\n"
        ));
    }
}
