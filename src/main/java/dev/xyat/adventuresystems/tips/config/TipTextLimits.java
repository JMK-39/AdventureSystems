package dev.xyat.adventuresystems.tips.config;

/** Visible Unicode characters, independent of display scale and legacy color codes. */
public final class TipTextLimits {
    public static final int CHARACTERS_PER_LINE=16;
    public static final int MAX_LINES=3;
    private static final java.lang.reflect.Method TEXT_EFFECT_STRIPPER=findTextEffectStripper();
    private TipTextLimits() {}
    public static String normalizeLines(String text) {
        return text.replace("\r\n","\n").replace('\r','\n');
    }
    public static boolean isValid(String text) {
        return problem(text)==null;
    }

    /** Why a tip cannot be saved. {@code line} is 1-based; {@code count} is the line or character count found. */
    public record Problem(Kind kind,int line,int count) {
        public enum Kind {EMPTY,TOO_LONG,TOO_MANY_LINES,LINE_TOO_LONG,CONTROL_CHARACTER}
    }

    /** The first rule the text breaks, or null when it can be saved. */
    public static Problem problem(String text) {
        if(text==null||text.isBlank())return new Problem(Problem.Kind.EMPTY,0,0);
        if(text.length()>2048)return new Problem(Problem.Kind.TOO_LONG,0,text.length());
        String normalized=normalizeLines(text);
        String[] lines=normalized.split("\n",-1);
        if(lines.length>MAX_LINES)return new Problem(Problem.Kind.TOO_MANY_LINES,0,lines.length);
        boolean visible=false;
        for(int i=0;i<lines.length;i++){
            String plain=visibleText(lines[i]).replaceAll("(?i)§[0-9A-FK-OR]","");
            int length=plain.codePointCount(0,plain.length());
            if(length>CHARACTERS_PER_LINE)return new Problem(Problem.Kind.LINE_TOO_LONG,i+1,length);
            if(plain.codePoints().anyMatch(Character::isISOControl))return new Problem(Problem.Kind.CONTROL_CHARACTER,i+1,0);
            visible|=!plain.isBlank();
        }
        return visible?null:new Problem(Problem.Kind.EMPTY,0,0);
    }

    private static java.lang.reflect.Method findTextEffectStripper() {
        try {
            return Class.forName("dev.xyat.textstudio.font.common.text.InputMetrics")
                    .getMethod("stripControlCodes",String.class);
        } catch (ReflectiveOperationException | LinkageError unavailable) {
            return null;
        }
    }

    private static String visibleText(String text) {
        if(TEXT_EFFECT_STRIPPER!=null) {
            try {
                return (String)TEXT_EFFECT_STRIPPER.invoke(null,text);
            } catch (ReflectiveOperationException | LinkageError unavailable) {
                // Without the optional formatter, every original code point still counts.
            }
        }
        return text;
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
