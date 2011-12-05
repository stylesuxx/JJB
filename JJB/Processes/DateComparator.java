package JJB.Processes;

import java.util.Date;
import java.util.Map;
import java.util.Comparator;

/** Date Comparator
  *
  * @author stylesuxx
  * @version 0.1
  */
public class DateComparator implements Comparator<String> {
  private Map<String,Date> base;

  /** Default Constructor
    * 
    * @param base (Unordered) Map to look up the values
    */
  public DateComparator( Map<String,Date> base ) {
      this.base = base;
  }

  /** Compare 2 Dates
    * 
    * @param a String to Date 1
    * @param b String to Date 2
    * 
    * @return int
    */
  @Override
  public int compare( String a, String b ){    
    Date aa = base.get(a);
    Date bb = base.get(b);
    return ( aa.compareTo(bb) );
  }

}