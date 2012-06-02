package pawnapigenerator;

/**
 * Data representation of a function header.
 * @author Michel "Mauzen" Soll
 */
public class FunctionHeader extends PawnStatement {
   
    private String name;
    
    private String[] params;
    
    private String retVal;
    
    public FunctionHeader(StatementType t, String n, String[] p, String r) {
        type = t;
        name = n;
        params = p;
        retVal = r;
    }
    
    public FunctionHeader(String keyname) {
        type = StatementType.KEYWORD;
        name = keyname;
    }
    
    @Override
    public String toXML() {
        String xml;
        if (type == StatementType.KEYWORD) {
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
    
    @Override
    public String getIndexName() {
        return name;
    }
}
