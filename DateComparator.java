import java.util.*;

class DateComparator implements Comparator {

  Map base;
  public DateComparator( Map base ) {
      this.base = base;
  }

  public int compare( Object a, Object b ){    
    Date aa = (Date)base.get(a);
    Date bb = (Date)base.get(b);
    return aa.compareTo(bb);
  }
}