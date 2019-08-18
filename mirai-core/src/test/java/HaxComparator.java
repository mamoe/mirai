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

    public static String compare(String hax1s, String hax2s){
        StringBuilder builder = new StringBuilder();

        String[] hax1 = hax1s.trim().split(" ");
        String[] hax2 = hax2s.trim().split(" ");

        if(hax1.length == hax2.length){
            builder.append(GREEN).append("长度一致:").append(hax1.length);
        }else{
            builder.append(RED).append("长度不一致").append(hax1.length).append("/").append(hax2.length);
        }

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

            hax1b.append(h1).append(" ");
            hax2b.append(h2).append(" ");
            if(isDif){
                ++dif;
            }
        }

        return (builder.append(" ").append(dif).append(" 个不同").append("\n").append(hax1b).append("\n").append(hax2b)).toString();
    }


    public static void main(String[] args){
        System.out.println(HaxComparator.compare("00 01 09 00 70 00 01 23 7B FE 83 D1 37 64 46 84 9D E9 9C E7 BB 8E 44 00 38 9B A4 3B C2 BB 49 4C DA B0 A5 5C C8 27 29 74 EF CB 38 59 4E 03 C8 15 C6 F9 BF 3F 88 22 7E 22 5B 48 02 71 59 1A 2C C8 42 BA 81 76 66 0C 46 91 89 6C B2 17 BF 2A 00 F8 8B 00 20 7C 28 07 3D AA 24 EF B4 49 9D 85 7F 4C F5 41 56 F4 1F AD 53 81 9F C1 03 F3 03 65 DD 0C 04 CC 68 00 00 01 03 00 14 00 01 00 10 60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6","AA CC AA DD EE GG HH"));
    }
}
