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
        System.out.println(HaxComparator.compare("AA CC AA DD EE FF","AA CC AA DD EE GG HH"));
    }
}
