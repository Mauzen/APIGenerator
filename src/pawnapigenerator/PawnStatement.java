package pawnapigenerator;

import java.text.Collator;

/**
 *
 * @author Michel "Mauzen" Soll
 */
public abstract class PawnStatement implements Comparable<PawnStatement> {
    
    protected StatementType type;
    
    /**
     * Returns the String that represents the statement for comparison.
     * @return The String that represents the statement for comparison
     */
    public abstract String getIndexName();
    
    /**
     * Returns the Statement as NPP XML segment.
     * @return The Statement as NPP XML segment
     */
    public abstract String toXML();
    
    /**
     * Compares two statements in alphabetic logic.
     * @param o Statement to compare to
     * @return Compare-value
     */
    @Override
    public int compareTo(PawnStatement o) {
        Collator c = Collator.getInstance();
        return c.compare(this.getIndexName(), o.getIndexName());        
    }
    
    public StatementType getType() {
        return type;
    }
    
    public void setType(StatementType newType) {
        type = newType;
    }
}
