package dev.xyat.adventuresystems.tips.config;

/** Visible Unicode characters, independent of display scale and legacy color codes. */
public final class TipTextLimits {
    public static final int CHARACTERS_PER_LINE=16;
    public static final int MAX_LINES=3;
    private TipTextLimits() {}
    public static String normalizeLines(String text) {
        return text.replace("\r\n","\n").replace('\r','\n');
    }
    public static boolean isValid(String text) {
        if(text==null||text.isBlank()||text.length()>2048)return false;
        String normalized=normalizeLines(text);
        String[] lines=normalized.split("\n",-1);
        if(lines.length>MAX_LINES)return false;
        boolean visible=false;
        for(String line:lines){
            String plain=line.replaceAll("(?i)§[0-9A-FK-OR]","");
            if(plain.codePointCount(0,plain.length())>CHARACTERS_PER_LINE||plain.codePoints().anyMatch(Character::isISOControl))return false;
            visible|=!plain.isBlank();
        }
        return visible;
    }
    public static String toEditor(String text) {
        return text==null?"":text.replace("\\","\\\\").replace("\r\n","\n").replace('\r','\n').replace("\n","\\n");
    }
    public static String fromEditor(String text) {
        var result=new StringBuilder();
        for(int i=0;i<text.length();i++){
            char c=text.charAt(i);
            if(c=='\\'&&i+1<text.length()){
                char next=text.charAt(i+1);
                if(next=='n'){result.append('\n');i++;continue;}
                if(next=='\\'){result.append('\\');i++;continue;}
            }
            result.append(c);
        }
        return result.toString();
    }
}
