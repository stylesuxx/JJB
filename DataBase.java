import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteJob;

/** This class handles all the Databaseconnections
  * the program needs only one Object of this type
  * This is where all our operations on the database happens.
  * If we return something we try to keep it an object
  * 
  * This File has mainly three parts - equivalent to the amount of tables we have
  * 
  * @author stylesuxx
  * @version 0.1
  */
public class DataBase{
  private File dbFile;
  private SQLiteConnection db;
  private SQLiteQueue queue;
  private boolean log = true;
  private String admin;


  /** Constructor with existing database
    * 
    * @param database Location of database to use
    * @param admin User who is to add as admin to the database
    */
  public DataBase( String admin, String database){
    this.admin = admin;
    dbFile = new File( database );
    queue = new SQLiteQueue( dbFile );
    queue.start();
  }
  
  /** Default Constructor
    * <ul>
    * <li>This Constructor (tries to create and) uses the file test.db as Database</li>
    * </ul>
    * 
    * @param admin User who is to add as admin to the database
    */
  public DataBase( String admin ){
    this.admin = admin;
    dbFile = new File( "JJB.db" );
    //queue = new SQLiteQueue( dbFile );
    //queue.start();

  }

  /** Conects to database or tries to create new one if given does not exist
    */
  public void connect(){
    db = new SQLiteConnection( dbFile );

    try{
      db.open(true);
    }catch( SQLiteException e ){
      System.out.println( "Error: Could not connect to Database." );
      System.exit(-1);
    }

    // Create DB if not already exists
    try{
      SQLiteStatement[] st = {
	db.prepare( "create table user (Jid TEXT PRIMARY KEY, status TEXT, date TEXT);"),
	db.prepare( "create table tv (tvKey INTEGER PRIMARY KEY, showname TEXT, airtime TEXT, airday TEXT, timezone TEXT, status TEXT, runtime INTEGER, nextTitle TEXT, nextEpisode INTEGER, nextSeason INTEGER, nextDate TEXT);"),
	db.prepare( "create table stats (statID INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, value TEXT);" ),
      };
      for(int i = 0; i < st.length; i++){
	try{
	  st[i].step();
	} finally{ 
	  st[i].dispose();
	}
      }
      System.out.println( "Created new Database: " + dbFile.toString() );
    }catch( Exception e ){ System.out.println("Using existing Database: " + dbFile.toString() ); }
    createAdmin( admin );
  }

  /** Crate an admin user - this is used for the user provided when the bot is started
    * 
    * @param jid Jid of the admin user to create
    * 
    * @return boolean 
    */ 
  private boolean createAdmin( final String jid ){
    //return queue.execute( new SQLiteJob<Boolean>(){
      //protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;

	try{
	  st = db.prepare("INSERT into user ( Jid, status, date ) VALUES( ?, 'admin', 'date' );");
	  st.bind(1, jid);
	  st.step();
	  st.dispose();

	}catch( SQLiteException e ){
	  if( log ) System.out.println( "DB Insert Error: Admin user already exists" );
	  e.printStackTrace();
	  return false;
	}

	return true;
      //}
    //}).complete();
  }
  
  public void close(){
    db.dispose();
    queue.stop( true );
  }

}