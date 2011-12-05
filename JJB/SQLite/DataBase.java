package JJB.SQLite;

import java.io.File;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

/** This class connects to an existing database or creates a new one with all needed tables. 
 * 
 * This Database Object has to be held open as long as a queue is still holding database jobs. 
 * 
 * @author stylesuxx
 * @version 0.2
 */
public class DataBase{
  private File dbFile;
  private SQLiteConnection db;

  /** Default Constructor
    * 
    * @param dbFile Database File to use.
    */
  public DataBase(File dbFile){
    this.dbFile = dbFile;
  }
  
  /** Connects to database or tries to create new one if given does not exist
    */
  public void connect(){
    db = new SQLiteConnection( dbFile );
    SQLiteStatement st = null;
    try{
      db.open(true);
    }catch( SQLiteException e ){
      System.out.println( "Error: Could not connect to Database." );
      System.exit(-1);
    }

    // Create DB if not already exists
    try{
      st = db.prepare( "create table user (Jid TEXT PRIMARY KEY, status TEXT, date TEXT);");
      st.step();
      st.dispose();
      st = db.prepare( "create table tv (tvKey INTEGER PRIMARY KEY, showname TEXT, airtime TEXT, airday TEXT, timezone TEXT, status TEXT, runtime INTEGER, nextTitle TEXT, nextEpisode INTEGER, nextSeason INTEGER, nextDate TEXT);");
      st.step();
      st.dispose();
      st = db.prepare( "create table stats (statID INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, value TEXT);" );
      st.step();
      st.dispose();
      st = db.prepare( "create table userTv (Jid TEXT, show INTEGER);" );
      st.step();
      st.dispose();

      System.out.println( "Created new Database: " + dbFile.toString() );
    }catch( Exception e ){ System.out.println("Using existing Database: " + dbFile.toString() ); }
  }

  /** Close the database  
   */
  public void close(){
    db.dispose();
  }

}