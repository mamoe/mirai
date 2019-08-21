/**
 * @author NaturalHG
 * This could be used to check packet encoding..
 * but better to run under UNIX
 */
public class HaxComparator {

    /**
     * a string result
     */

    private static String RED = "\033[31m";

    private static String GREEN = "\033[33m";

    private static String UNKNOWN = "\033[30m";

    public static String compare(String hax1s, String hax2s){
        StringBuilder builder = new StringBuilder();

        String[] hax1 = hax1s.trim().replace("\n", "").split(" ");
        String[] hax2 = hax2s.trim().replace("\n", "").split(" ");

        if(hax1.length == hax2.length){
            builder.append(GREEN).append("长度一致:").append(hax1.length);
        }else{
            builder.append(RED).append("长度不一致").append(hax1.length).append("/").append(hax2.length);
        }


        StringBuilder numberLine = new StringBuilder();
        StringBuilder hax1b = new StringBuilder();
        StringBuilder hax2b = new StringBuilder();
        int dif = 0;

        for (int i=0;i<Math.max(hax1.length,hax2.length);++i){
            String h1 = null;
            String h2 = null;
            boolean isDif = false;
            if(hax1.length <= i){
                h1 = RED + "__";
                isDif = true;
            }
            if(hax2.length <= i){
                h2 = RED + "__";
                isDif = true;
            }
            if(h1 == null && h2 == null){
                h1 = hax1[i];
                h2 = hax2[i];
                if(h1.equals(h2)){
                    h1 = GREEN + h1;
                    h2 = GREEN + h2;
                }else{
                    h1 = RED + h1;
                    h2 = RED + h2;
                    isDif = true;
                }
            }else{
                if(h1 == null){
                    h1 = RED + hax1[i];
                }
                if(h2 == null){
                    h2 = RED + hax2[i];
                }
            }

            numberLine.append(UNKNOWN).append(getNumber(i)).append(" ");
            hax1b.append(h1).append("  ");
            hax2b.append(h2).append("  ");
            if(isDif){
                ++dif;
            }
        }

        return (builder.append(" ").append(dif).append(" 个不同").append("\n")
                .append(numberLine).append("\n")
                .append(hax1b).append("\n")
                .append(hax2b))
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


    public static void main(String[] args){
        System.out.println(HaxComparator.compare(
                "02 37 13 08 28 31 03 76 E4 B8 DD 03 00 00 00 01 01 01 00 00 68 20 00 00 00 00 00 01 01 03 00 19 02 6D 28 41 D2 A5 6F D2 FC 3E 2A 1F 03 75 DE 6E 28 8F A8 19 3E 5F 16 49 D3 00 00 00 10 EF 4A 36 6A 16 A8 E6 3D 2E EA BD 1F 98 C1 3C DA B4 4E C2 36 01 12 00 38 A9 76 88 3D C3 26 28 32 77 D1 EA 55 23 52 12 A5 75 1F EB F9 3B 6D 36 50 5E CF 8E 16 16 19 10 6B 83 FA 6F 0E D6 8E ED A4 BF BC 6C DE 34 61 D9 BB 96 16 C5 CE FE 76 27 22 03 0F 00 11 00 0F 44 45 53 4B 54 4F 50 2D 4D 31 37 4A 52 45 55 00 05 00 06 00 02 76 E4 B8 DD 00 06 00 78 9F DE A7 F7 6D 6C B3 D6 91 47 B8 3D 97 EB 79 ED 82 A6 9C 77 1E FC 74 C6 0C 31 99 DA 97 21 CD 90 00 15 00 30 00 01 01 27 9B C7 F5 00 10 65 03 FD 8B 00 00 00 00 00 00 00 00 00 00 00 00 02 90 49 55 33 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B 00 1A 00 40 2D FB 2B 26 88 BE 00 43 A9 25 00 06 5A DD D3 1F 7D 95 9C A3 50 25 C4 65 F8 C6 0E 09 A0 00 30 57 26 13 7F 5A C6 01 F8 F6 63 4C 67 13 B3 B1 0F 30 89 60 81 B8 1A CD D5 02 FB 26 45 A5 27 79 34 D0 00 18 00 16 00 01 00 00 04 53 00 00 00 01 00 00 15 85 76 E4 B8 DD 00 00 00 00 01 03 00 14 00 01 00 10 60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6 03 12 00 05 01 00 00 00 01 05 08 00 05 10 00 00 00 00 03 13 00 19 01 01 02 00 10 04 EA 78 D1 A4 FF CD CC 7C B8 D4 12 7D BB 03 AA 00 00 00 00 01 02 00 62 00 01 04 EB B7 C1 86 F9 08 96 ED 56 84 AB 50 85 2E 48 00 38 E9 AA 2B 4D 26 4C 76 18 FE 59 D5 A9 82 6A 0C 04 B4 49 50 D7 9B B1 FE 5D 97 54 8D 82 F3 22 C2 48 B9 C9 22 69 CA 78 AD 3E 2D E9 C9 DF A8 9E 7D 8C 8D 6B DF 4C D7 34 D0 D3 00 14 54 30 26 84 06 E4 97 E3 63 37 C5 8C 34 72 2D C3 6B 74 83 4C 57 17 EF 65 F7 66 94 6B 2A A4 7F E9 72 11 3B 33 0A 03\n"
                ,
                "02 37 13 08 36 31 03 76 E4 B8 DD 03 00 00 00 01 01 01 00 00 68 20 00 00 00 00 00 01 01 03 00 19 02 6D 28 41 D2 A5 6F D2 FC 3E 2A 1F 03 75 DE 6E 28 8F A8 19 3E 5F 16 49 D3 00 00 00 10 EF 4A 36 6A 16 A8 E6 3D 2E EA BD 1F 98 C1 3C DA B4 15 18 27 01 12 00 38 C3 D9 67 C5 43 ED E4 FF 64 B7 68 80 19 4E AA 17 C2 77 C0 51 82 E0 E1 35 1C E5 B6 50 49 AE 16 51 F3 59 B4 B2 00 E8 ED AD 99 6D 09 2C D6 0F 73 09 D6 44 DE 74 58 C5 24 B2 03 0F 00 11 00 0F 44 45 53 4B 54 4F 50 2D 4D 31 37 4A 52 45 55 00 05 00 06 00 02 76 E4 B8 DD 00 06 00 78 D8 20 30 26 EB 91 D2 66 ED A2 73 A1 1F BA C0 BD C6 5C F0 35 83 CB 24 0B FE 62 5F C0 1F 74 6D F0 00 15 00 30 00 01 01 27 9B C7 F5 00 10 65 03 FD 8B 00 00 00 00 00 00 00 00 00 00 00 00 02 90 49 55 33 00 10 15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B 00 1A 00 40 B1 AC 22 BA AE F3 4F 8F A9 16 BB 14 BA 10 00 BA 47 87 E3 5F A0 C6 1F 14 F5 34 AC E2 5C 9E 2F C6 F7 04 52 E1 9D FC 5C BE A4 7F 38 D8 D8 C4 08 51 D6 9E 37 16 8E 24 DF A2 5B 5D DB AB 2E 00 76 EE 00 18 00 16 00 01 00 00 04 53 00 00 00 01 00 00 15 85 76 E4 B8 DD 00 00 00 00 00 01 03 00 14 00 01 00 10 60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6 03 12 00 05 01 00 00 00 01 05 08 00 05 10 00 00 00 00 03 13 00 19 01 01 02 00 10 04 EA 78 D1 A4 FF CD CC 7C B8 D4 12 7D BB 03 AA 00 00 00 00 00 01 02 00 62 00 01 04 EB B7 C1 86 F9 08 96 ED 56 84 AB 50 85 2E 48 00 38 E9 AA 2B 4D 26 4C 76 18 FE 59 D5 A9 82 6A 0C 04 B4 49 50 D7 9B B1 FE 5D 97 54 8D 82 F3 22 C2 48 B9 C9 22 69 CA 78 AD 3E 2D E9 C9 DF A8 9E 7D 8C 8D 6B DF 4C D7 34 D0 D3 00 14 5F C9 F5 37 A0 11 D7 E6 26 37 A3 2B E3 21 11 83 F0 65 32 06 6A BC 59 C4 20 9F 84 E8 C9 FE 8F 3B 6D 96 12 F2 A4 03\n"
        ));
    }
}
