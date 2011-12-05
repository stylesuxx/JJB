package JJB.SQLite;

import com.almworks.sqlite4java.SQLiteQueue;

// This is the Obertyp off all *Queries classes
public abstract class SQLiteQueries{
  public SQLiteQueue queue;
  public boolean log;

  public SQLiteQueries( SQLiteQueue queue, boolean log ){
    this.queue = queue;
    this.log = log;
  }

}