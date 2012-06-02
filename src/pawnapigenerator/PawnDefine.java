package pawnapigenerator;

/**
 *
 * @author Michel "Mauzen" Soll
 */
public class PawnDefine extends PawnStatement {
    
    private String name;
    
    private String full;
    
    private String value;
    
    public PawnDefine(String n, String f, String v) {
        name = n;
        value = v;
        full = f;
        type = StatementType.DEFINE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getFull() {
        return full;
    }

    public void setFull(String full) {
        this.full = full;
    }
    
    

    @Override
    public String getIndexName() {
        return name;
    }

    @Override
    public String toXML() {
        return "\t\t<KeyWord name=\"" + name + "\" />\r\n";
    }
    
    
}
