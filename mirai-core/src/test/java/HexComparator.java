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

                "01 12 00 38 56 3A E4 8B B4 64 D2 72 60 FE 01 54 FC B1 5F 88 E0 BA 64 1A 55 F2 84 FC 97 D0 BF 5F 47 A8 D9 76 BB FB 4A 7A F3 5E 0E A4 8E CA 8F 27 C2 02 6E 5D E7 68 9F 7C CF 91 83 F4 03 0F 00 11 00 0F 44 45 53 4B 54 4F 50 2D 4D 31 37 4A 52 45 55 00 05 00 06 00 02 76 E4 B8 DD 00 06 00 78 0D DF 92 9C 5A 08 D1 67 FD 7D D6 DE CE D0 92 39 79 17 53 57 41 9B D6 D3 F9 F8 9A 3B E1 C2 3A E7 CF 02 6E 5E 36 B7 6D CF 33 66 77 FE AC 58 93 A3 85 E7 AF 6F 2D A2 74 E2 60 28 4B 29 17 04 79 95 39 D4 BF 4D C1 ED 61 49 13 23 9D 71 62 29 AF 87 D7 E3 42 49 88 3F D8 5C DB 9F 9E 5A 2A EA 02 F6 4F 2B D3 5B AF BE 0C B2 54 46 AE 99 1B 07 0B BE 6A C2 29 18 25 6A 95 0A 00 15 00 30 00 01 01 27 9B C7 F5 00 10 65 03 FD 8B 00 00 00 00 00 00 00 00 00 00 00 00 02 90 49 55 33 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B 00 1A 00 40 E2 9A D0 B3 7D CD 3D F4 55 DB 04 A4 68 2A AF 4E 38 46 8B E0 1F 2D 2A B8 F6 C6 AB 4F DF D1 5F 8C D4 CA 2A 91 25 14 33 A3 C5 08 F8 01 4C E6 3D 89 5F 23 38 3A EC 01 58 F6 B1 F6 7F 2E 6A 02 17 4A 00 18 00 16 00 01 00 00 04 53 00 00 00 01 00 00 15 85 76 E4 B8 DD 00 00 00 00 01 03 00 14 00 01 00 10 60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6 01 10 00 3C 00 01 00 38 57 3A 37 C3 FB A0 C3 E5 AE F3 0E B6 03 DE BF 9E E2 B5 C5 FE A0 F0 03 4F F7 8A 5C 29 5C E0 5A A2 89 D5 3F 60 E2 B2 81 FE D4 16 04 D4 E3 C6 4A D7 A9 D9 E6 FC 2E 7E 0C F3 03 12 00 05 01 00 00 00 01 05 08 00 05 10 00 00 00 00 03 13 00 19 01 01 02 00 10 04 EA 78 D1 A4 FF CD CC 7C B8 D4 12 7D BB 03 AA 00 00 00 00 01 02 00 62 00 01 04 EB B7 C1 86 F9 08 96 ED 56 84 AB 50 85 2E 48 00 38 E9 AA 2B 4D 26 4C 76 18 FE 59 D5 A9 82 6A 0C 04 B4 49 50 D7 9B B1 FE 5D 97 54 8D 82 F3 22 C2 48 B9 C9 22 69 CA 78 AD 3E 2D E9 C9 DF A8 9E 7D 8C 8D 6B DF 4C D7 34 D0 D3 00 14 10 21 DF C4 F3 DD 42 09 6F A0 93 8C 7E 0E 78 EF 22 8D 88 35"
                ,
                //e
                "01 12 00 38 56 3A E4 8B B4 64 D2 72 60 FE 01 54 FC B1 5F 88 E0 BA 64 1A 55 F2 84 FC 97 D0 BF 5F 47 A8 D9 76 BB FB 4A 7A F3 5E 0E A4 8E CA 8F 27 C2 02 6E 5D E7 68 9F 7C CF 91 83 F4 03 0F 00 11 00 0F 44 45 53 4B 54 4F 50 2D 4D 31 37 4A 52 45 55 00 05 00 06 00 02 76 E4 B8 DD 00 06 00 78 0D DF 92 9C 5A 08 D1 67 FD 7D D6 DE CE D0 92 39 79 17 53 57 41 9B D6 D3 F9 F8 9A 3B E1 C2 3A E7 CF 02 6E 5E 36 B7 6D CF 33 66 77 FE AC 58 93 A3 85 E7 AF 6F 2D A2 74 E2 60 28 4B 29 17 04 79 95 39 D4 BF 4D C1 ED 61 49 13 23 9D 71 62 29 AF 87 D7 E3 42 49 88 3F D8 5C DB 9F 9E 5A 2A EA 02 F6 4F 2B D3 5B AF BE 0C B2 54 46 AE 99 1B 07 0B BE 6A C2 29 18 25 6A 95 0A 00 15 00 30 00 01 01 27 9B C7 F5 00 10 65 03 FD 8B 00 00 00 00 00 00 00 00 00 00 00 00 02 90 49 55 33 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B 00 1A 00 40 26 2B FE EB 21 8E 08 16 BA 42 B3 A3 C5 6A 5B 31 33 A9 B3 F0 EB 16 80 BE F0 58 D3 51 98 2F A7 23 27 7C 06 97 F4 85 01 77 5C 5B D6 A2 82 54 06 A0 07 53 39 E2 E7 62 E5 09 1A 25 79 6F D9 E0 ED B6 00 18 00 16 00 01 00 00 04 53 00 00 00 01 00 00 15 85 76 E4 B8 DD 00 00 00 00 01 03 00 14 00 01 00 10 60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6 01 10 00 3C 00 01 00 38 57 3A 37 C3 FB A0 C3 E5 AE F3 0E B6 03 DE BF 9E E2 B5 C5 FE A0 F0 03 4F F7 8A 5C 29 5C E0 5A A2 89 D5 3F 60 E2 B2 81 FE D4 16 04 D4 E3 C6 4A D7 A9 D9 E6 FC 2E 7E 0C F3 03 12 00 05 01 00 00 00 01 05 08 00 05 01 00 00 00 00 03 13 00 19 01 01 02 00 10 04 EA 78 D1 A4 FF CD CC 7C B8 D4 12 7D BB 03 AA 00 00 00 00 01 02 00 62 00 01 04 EB B7 C1 86 F9 08 96 ED 56 84 AB 50 85 2E 48 00 38 E9 AA 2B 4D 26 4C 76 18 FE 59 D5 A9 82 6A 0C 04 B4 49 50 D7 9B B1 FE 5D 97 54 8D 82 F3 22 C2 48 B9 C9 22 69 CA 78 AD 3E 2D E9 C9 DF A8 9E 7D 8C 8D 6B DF 4C D7 34 D0 D3 00 14 4C 97 73 32 83 1A 6F C4 94 91 0A 7A D8 21 81 58 73 3D 24 F6"
        ));
        */

        /*
        System.out.println(HexComparator.compare(
                //e
                "90 5E 39 DF 00 02 76 E4 B8 DD 00 00 04 53 00 00 00 01 00 00 15 85 00 00 01 55 35 05 8E C9 BA 16 D0 01 63 5B 59 4B 59 52 31 01 B9 00 00 00 00 00 00 00 00 00 00 00 00 00 7B 7B 7B 7B 00 00 00 00 00 00 00 00 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B AA BB CC DD EE FF AA BB CC",
                //mirai
                "6F 0B DF 92 00 02 76 E4 B8 DD 00 00 04 53 00 00 00 01 00 00 15 85 00 00 01 55 35 05 8E C9 BA 16 D0 01 63 5B 59 4B 59 52 31 01 B9 00 00 00 00 00 00 00 00 00 00 00 00 00 E9 E9 E9 E9 00 00 00 00 00 00 00 00 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B AA BB CC DD EE FF AA BB CC\n\n\n"
        ));*/
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

