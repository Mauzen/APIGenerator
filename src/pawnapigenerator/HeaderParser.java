package pawnapigenerator;

import java.util.regex.Matcher;

/**
 * Parses header lines to FunctionHeader objects.
 * @author Michel "Mauzen" Soll
 */
public class HeaderParser {
    
    public static FunctionHeader parse(String h) {
        StatementType t = StatementType.UNKNOWN;
        String n = "";
        String[] p = null;
        String r = "";
        
        // find function type
        if (h.trim().startsWith("public ")) {
            t = StatementType.PUBLIC;
            h = h.substring(7);
        } else if (h.trim().startsWith("stock ")) {
            t = StatementType.STOCK;
            h = h.substring(6);
        } else if (h.trim().startsWith("native ")) {
            t = StatementType.NATIVE;
            h = h.substring(7);
        } else {
            t = StatementType.NONE;
        }
        
        // Return type (bugged for BLABLA:name defines)
        String[] rets = h.substring(0, h.indexOf("(")).split(":");
        if (rets.length > 1) {
            r = rets[0];
            h = h.substring(rets[0].length() + 1).trim();
        }
        
        // Get name
        int openB = h.indexOf("(");
        if (openB > 0) {
            n = h.substring(0, openB);
        } else {
            System.out.println("NULL1: " + h);
            return null;
        }
        
        // Parse params
        int closeB = h.lastIndexOf(")");
        if (closeB > openB) {
            h = h.substring(openB + 1, closeB);
            p = h.split(",");
            for (int i = 0; i < p.length; i++) {
                p[i] = transformForXML(p[i]);
            }
        } else {
            System.out.println("NULL2: " + h);
            return null;
        }
        
        return new FunctionHeader(t, n, p, r);        
    }
    
    public static String transformForXML(String s) {
        String clean = s.replaceAll("&", "&amp;");
        clean = clean.replaceAll("\"", "&quot;");
        clean = clean.replaceAll("'", "&apos;");
        clean = clean.replaceAll("<", "&lt;");
        clean = clean.replaceAll(">", "&gt;");
        clean = clean.trim();
        
        return clean;
    }
    
    /**
     * Generates a FunctionHeader instance from a normal function header.
     * @param m Regex match of the header
     * @return Parsed header
     */
    public static FunctionHeader parseFromRegEx(Matcher m) {
        StatementType t = null;
        String n = m.group(5);
        String ret = m.group(4);
        
        for (String k : PawnAPIGenerator.KEYWORDS) {
            if (n.equals(k)) return null;
        }
        
        if (m.group(1) == null) t = StatementType.NONE;
        else if (m.group(2).equals("public")) t = StatementType.PUBLIC;
        else if (m.group(2).equals("stock")) t = StatementType.STOCK;

        if (ret == null) ret = "";
        
        return new FunctionHeader(t, n, m.group(6).split(","), ret);
    }
    
    /**
     * Generates a FunctionHeader instance from a native function header.
     * @param m Regex match of the header
     * @return Parsed header
     */
    public static FunctionHeader parseFromNativeRegEx(Matcher m) {
        String n = m.group(3);
        String ret = m.group(2);
        
        for (String k : PawnAPIGenerator.KEYWORDS) {
            if (n.equals(k)) return null;
        }
        
        if (ret == null) ret = "";
        
        return new FunctionHeader(StatementType.NATIVE, n, m.group(4).split(","), ret);
    }
    
    
    /**
     * Generates a Functionheader instance
     * @param m
     * @return 
     */
    public static PawnDefine parseDefineRegEx(Matcher m) {
        PawnDefine p = new PawnDefine(m.group(2), m.group(1), m.group(6));
        return p;
    }
    
    
}
