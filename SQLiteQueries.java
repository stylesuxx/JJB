import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteJob;

// This is the Obertyp off all *Queries classes
public abstract class SQLiteQueries{
  public SQLiteQueue queue;
  public boolean log;

  public SQLiteQueries( SQLiteQueue queue, boolean log ){
    this.queue = queue;
    this.log = log;
  }

  public SQLiteQueries( File db, boolean log ){
    queue = new SQLiteQueue( db );
    queue.start();
    this.log = log;
  }

  public void close(){
    queue.stop( true );
  }
}