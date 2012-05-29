package pawnapigenerator;

/**
 * Parses header lines to FunctionHeader objects.
 * @author Michel "Mauzen" Soll
 */
public class HeaderParser {
    
    public static FunctionHeader parse(String h) {
        FunctionType t = FunctionType.UNKNOWN;
        String n = "";
        String[] p = null;
        String r = "";
        
        // find function type
        if (h.trim().startsWith("public ")) {
            t = FunctionType.PUBLIC;
            h = h.substring(7);
        } else if (h.trim().startsWith("stock ")) {
            t = FunctionType.STOCK;
            h = h.substring(6);
        } else if (h.trim().startsWith("native ")) {
            t = FunctionType.NATIVE;
            h = h.substring(7);
        } else {
            t = FunctionType.NONE;
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
    
    
    
}
