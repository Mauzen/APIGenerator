package pawnapigenerator;

import java.text.Collator;

/**
 * Data representation of a function header.
 * @author Michel "Mauzen" Soll
 */
public class FunctionHeader implements Comparable<FunctionHeader> {
    private FunctionType type;
    
    private String name;
    
    private String[] params;
    
    private String retVal;
    
    public FunctionHeader(FunctionType t, String n, String[] p, String r) {
        type = t;
        name = n;
        params = p;
        retVal = r;
    }
    
    public FunctionHeader(String keyname) {
        type = FunctionType.KEYWORD;
        name = keyname;
    }
    
    public String toXML() {
        String xml;
        if (type == FunctionType.KEYWORD) {
            xml = "\t\t<KeyWord name=\"" + name + "\" />\r\n";
            return xml;
        }
        xml = "\t\t<KeyWord name=\"" + name + "\" func=\"yes\">\r\n"
		+	"\t\t\t<Overload retVal=\"" + retVal + "\">\r\n";
        
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (params[i].length() == 0) continue;
                xml += "\t\t\t\t<Param name=\"" + HeaderParser.transformForXML(params[i]) + "\" />\r\n";                
            }
        }
        
        xml += "\t\t\t</Overload>\r\n"
                + "\t\t</KeyWord>\r\n";

        return xml;
    }

    public String getName() {
        return name;
    }

    public String[] getParams() {
        return params;
    }

    public String getRetVal() {
        return retVal;
    }

    public FunctionType getType() {
        return type;
    }
    
    

    @Override
    public int compareTo(FunctionHeader o) {
        Collator c = Collator.getInstance();
        return c.compare(name, o.name);
    }
}
