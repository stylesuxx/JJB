import java.util.*;

class DateComparator implements Comparator {

  Map base;
  public DateComparator( Map base ) {
      this.base = base;
  }

  public int compare( Object a, Object b ){
    Date aa = (Date)a;
    Date bb = (Date)b;
    if( aa.compareTo(bb) < 0 ) {
      return 1;
    }
    else if( aa.compareTo(bb) == 0 ) {
      return 0;
    }
    else {
      return -1;
    }
  }
}