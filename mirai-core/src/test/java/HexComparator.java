import kotlin.ranges.IntRange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.mamoe.mirai.network.Protocol;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
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

    public static final List<HexReader> consts  = new LinkedList<>(){{
        add(new HexReader("90 5E 39 DF 00 02 76 E4 B8 DD 00"));
    }};

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

            //doConstReplacement(hex1b);
            //doConstReplacement(hex2b);
        }

        return (builder.append(" ").append(dif).append(" 个不同").append("\n")
                .append(numberLine).append("\n")
                .append(hex1b).append("\n")
                .append(hex2b))
                .toString();


    }


    private static void doConstReplacement(StringBuilder builder){
        String mirror = builder.toString();
        HexReader hexs = new HexReader(mirror);
        for (AtomicInteger i=new AtomicInteger(0);i.get()<builder.length();i.addAndGet(1)){
            hexs.setTo(i.get());
            consts.forEach(a -> {
                hexs.setTo(i.get());
                List<Integer> posToPlaceColor = new LinkedList<>();
                AtomicBoolean is = new AtomicBoolean(false);

                a.readFully((c,d) -> {
                    if(c.equals(hexs.readHex())){
                        posToPlaceColor.add(d);
                    }else{
                        is.set(false);
                    }
                });

                if(is.get()){
                    AtomicInteger adder = new AtomicInteger();
                    posToPlaceColor.forEach(e -> {
                        builder.insert(e + adder.getAndAdd(BLUE.length()),BLUE);
                    });
                }
            });
        }
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
        /*
        System.out.println(HexComparator.compare(
                //mirai
                "01 12 00 38 D7 B4 57 68 68 BA 91 9D 2C 6F F0 B8 1A 77 4A DC 10 FE 74 E6 03 CE EA 90 5A 43 FD 19 21 94 EF 41 C3 2B 48 B7 91 70 DD AE 47 EA C7 EC 69 5A 6D 16 3B CA E8 DC 82 81 24 74 03 0F 00 11 00 0F 44 45 53 4B 54 4F 50 2D 4D 31 37 4A 52 45 55 00 05 00 06 00 02 76 E4 B8 DD 00 06 00 78 78 F4 FC F9 F2 B0 04 32 4A 01 F7 0B 23 7A 5E 30 E9 42 83 89 C4 D2 7A F7 7D 6E 71 FF FC 97 81 3A FC 11 75 3B D6 7C E8 C2 04 3E CD B1 34 57 C3 B4 AF 40 87 BD 06 18 7F 7B E8 8E F5 42 0F A8 A8 87 44 3A A5 51 69 61 F1 12 F4 94 DE BA 37 D9 50 5C 03 ED CB DE 6E 68 B4 DE 79 6E BF A9 07 D2 E5 56 7A AF 35 12 00 0C 4D 9D F4 E5 0C 36 9C 5C 5A CE F7 2D 95 28 82 8A FC E5 00 15 00 30 00 01 01 27 9B C7 F5 00 10 65 03 FD 8B 00 00 00 00 00 00 00 00 00 00 00 00 02 90 49 55 33 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B 00 1A 00 40 56 5C 1A 38 F9 6A 88 F8 34 EF 68 FC 83 9D 61 9B 7A 07 F6 37 CD 1E C4 9A C9 B2 81 B3 F5 67 C4 74 63 23 30 64 E8 32 6F BD 35 14 0D 75 2D DF 0F 38 80 D3 3C 11 0D 1B 74 EF C8 6D 54 42 E7 DB 94 B9 00 18 00 16 00 01 00 00 04 53 00 00 00 01 00 00 15 85 76 E4 B8 DD 00 00 00 00 01 03 00 14 00 01 00 10 60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6 01 10 00 3C 00 01 00 38 E0 65 0C 69 9A EE D9 90 CE 5D AF 23 6B 49 03 0D 98 47 25 3F B5 BD 88 BB 54 50 72 E0 37 8E 66 ED A6 37 18 77 71 5D 66 A6 A3 CD A5 BC 9B CD 87 42 DB 41 59 3E 54 A7 90 DC 03 12 00 05 01 00 00 00 01 05 08 00 05 10 00 00 00 00 03 13 00 19 01 01 02 00 10 04 EA 78 D1 A4 FF CD CC 7C B8 D4 12 7D BB 03 AA 00 00 00 00 01 02 00 62 00 01 04 EB B7 C1 86 F9 08 96 ED 56 84 AB 50 85 2E 48 00 38 E9 AA 2B 4D 26 4C 76 18 FE 59 D5 A9 82 6A 0C 04 B4 49 50 D7 9B B1 FE 5D 97 54 8D 82 F3 22 C2 48 B9 C9 22 69 CA 78 AD 3E 2D E9 C9 DF A8 9E 7D 8C 8D 6B DF 4C D7 34 D0 D3 00 14 32 8C B5 CB 10 2E 0A 31 F2 EB 6D FC 13 CB 14 83 31 CD 75 C6"
                ,
                //e
                "01 12 00 38 D5 CC FC 5E BF 39 4C 07 7F FF AE 3C C2 10 E0 0E 3D C1 7B 6C 1C 58 9C 97 AB DE DC 4C B7 8E AB DA 77 BE 5F AD 8D 3C EA 7D B8 3D 5E B3 5B 6B DD 32 E6 A5 0C 6F B7 93 E4 C3 03 0F 00 11 00 0F 44 45 53 4B 54 4F 50 2D 4D 31 37 4A 52 45 55 00 05 00 06 00 02 76 E4 B8 DD 00 06 00 78 64 5C 48 D5 BF 98 6A 81 8F B5 09 DA A5 83 0E 45 BB 99 9B 03 42 2A 87 95 48 88 52 0D 5F 0B C1 4D A7 5F BF 60 4F 3D A1 04 D3 B4 E4 D2 45 71 5C 74 95 80 86 45 E0 26 EA B2 B1 09 0B 56 22 68 7C 5D 8D 9E 69 E4 C5 4E 0C EA F5 6F 90 FF 4B 43 43 EB 4F 76 45 70 DA 12 C7 1E A5 14 B8 5B 78 79 75 5E 2C F3 5D 1A C4 39 D5 AE 1A 70 EC AF A1 F5 FF D6 D3 B9 C6 DA 71 7E 15 52 00 15 00 30 00 01 01 27 9B C7 F5 00 10 65 03 FD 8B 00 00 00 00 00 00 00 00 00 00 00 00 02 90 49 55 33 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B 00 1A 00 40 9D AB 45 74 6A E5 3F E2 8E 81 16 6C BB FA 0D A1 37 28 2F B9 02 3D EB 07 C7 ED 95 99 F9 35 27 35 58 67 4A FA 6E E4 89 37 8A 00 3B 19 C5 15 7E F6 83 D5 CF 66 9C FD 10 9F 27 90 31 3B 2E 98 F9 4C 00 18 00 16 00 01 00 00 04 53 00 00 00 01 00 00 15 85 76 E4 B8 DD 00 00 00 00 01 03 00 14 00 01 00 10 60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6 01 10 00 3C 00 01 00 38 3A 51 49 BC 3C 44 78 A1 A7 6D B7 98 03 05 9F 42 E1 15 E5 53 0C C1 03 82 5E AE AD FC 44 C6 E9 85 66 51 F2 E2 67 B4 60 DC 89 EC E4 56 13 52 E6 AA C4 5A D1 FA 3D E7 10 92 03 12 00 05 01 00 00 00 01 05 08 00 05 01 00 00 00 00 03 13 00 19 01 01 02 00 10 04 EA 78 D1 A4 FF CD CC 7C B8 D4 12 7D BB 03 AA 00 00 00 00 01 02 00 62 00 01 04 EB B7 C1 86 F9 08 96 ED 56 84 AB 50 85 2E 48 00 38 E9 AA 2B 4D 26 4C 76 18 FE 59 D5 A9 82 6A 0C 04 B4 49 50 D7 9B B1 FE 5D 97 54 8D 82 F3 22 C2 48 B9 C9 22 69 CA 78 AD 3E 2D E9 C9 DF A8 9E 7D 8C 8D 6B DF 4C D7 34 D0 D3 00 14 D5 4E 31 3E 5E B3 28 6C 94 FB 25 CA E8 C4 A4 28 09 02 B2 58"
        ));
        */

        System.out.println(HexComparator.compare(
                //e
                "90 5E 39 DF 00 02 76 E4 B8 DD 00 00 04 53 00 00 00 01 00 00 15 85 00 00 01 55 35 05 8E C9 BA 16 D0 01 63 5B 59 4B 59 52 31 01 B9 00 00 00 00 00 00 00 00 00 00 00 00 00 7B 7B 7B 7B 00 00 00 00 00 00 00 00 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B AA BB CC DD EE FF AA BB CC",
                //mirai
                "6F 0B DF 92 00 02 76 E4 B8 DD 00 00 04 53 00 00 00 01 00 00 15 85 00 00 01 55 35 05 8E C9 BA 16 D0 01 63 5B 59 4B 59 52 31 01 B9 00 00 00 00 00 00 00 00 00 00 00 00 00 E9 E9 E9 E9 00 00 00 00 00 00 00 00 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B AA BB CC DD EE FF AA BB CC\n\n\n"
        ));
    }


}

class HexReader{
    private String s;
    private int pos = 0;
    private int lastHaxPos = 0;


    public HexReader(String s){
        this.s = s;
    }

    public String readHex(){
        boolean isStr = false;
        String next = "";
        for (;pos<s.length()-2;++pos){

            char s1 = ' ';
            if(pos != 0){
                s1 = this.s.charAt(0);
            }
            char s2 = this.s.charAt(pos+1);
            char s3 = this.s.charAt(pos+2);
            char s4 = ' ';
            if(this.s.length() != (this.pos+3)){
                s4 = this.s.charAt(pos+3);
            }
            if(
                    Character.isSpaceChar(s1) && Character.isSpaceChar(s4)
                    &&
                            (Character.isDigit(s2) || Character.isAlphabetic(s2))
                            &&
                            (Character.isDigit(s3) || Character.isAlphabetic(s3))
            ){
                this.pos+=2;
                this.lastHaxPos = this.pos+1;
                return String.valueOf(s2) + String.valueOf(s3);
            }
        }
        return "";
    }

    public void readFully(BiConsumer<String, Integer> processor){
        this.reset();
        String nextHax = this.readHex();
        while (!nextHax.equals(" ")){
            processor.accept(nextHax,this.lastHaxPos);
            nextHax = this.readHex();
        }
    }

    public void setTo(int pos){
        this.pos = pos;
    }

    public void reset(){
        this.pos = 0;
    }

}

