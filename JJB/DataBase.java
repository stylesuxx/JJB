package JJB;

import java.io.File;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

/** This class connects to an existing database or creates a new one with all needed tables and an initial admin user. 
 * 
 * This Database Object has to be held open as long as a queue is still holding database jobs. 
 * 
 * @author stylesuxx
 * @version 0.1
 */
public class DataBase{
  private File dbFile;
  private SQLiteConnection db;
  private boolean log;
  private String admin;

  /** Default constructor
    * 
    * @param database Location of database to use
    * @param admin User who is to add as admin to the database
    */
  public DataBase( String admin, File dbFile){
    log = true;
    this.admin = admin;
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

      System.out.println( "Created new Database: " + dbFile.toString() );
    }catch( Exception e ){ System.out.println("Using existing Database: " + dbFile.toString() ); }

    // Try to create admin if not already exists
    createAdmin( admin );
  }

  /** Crate an initial admin user, so someone has control over the bot
    * 
    * @param jid Jid of the admin user to create
    * 
    * @return boolean 
    */ 
  private boolean createAdmin( final String jid ){
    SQLiteStatement st = null;

    // If user does not exist create new one, if not admin promote admin, if admin do nothing
    try{
      st = db.prepare("SELECT status FROM user WHERE Jid=?");
      st.bind( 1, jid );
      st.step();
      if( st.hasRow() ){
       //st.dispose();
        if( st.columnString(0).equals( "admin" ) ){
          System.out.println("Admin user exists");
        }
        else{st.dispose();
          st = db.prepare("UPDATE user SET status = 'admin' WHERE Jid = ? ");
          st.bind( 1, jid );
          st.step();
          System.out.println("User in DB and promoted to admin");
        }
      }
      else{
        st.dispose();
        st = db.prepare("INSERT into user ( Jid, status, date ) VALUES( ?, 'admin', 'date' );");
        st.bind( 1, jid );
        st.step();
        System.out.println("Created admin user.");
      }
      st.dispose();
    }catch( SQLiteException e ){
        if( log ) System.out.println( "Error creating initial admin User." );
        e.printStackTrace();
        return false;
    }
    return true;
  }
  
  public void close(){
    db.dispose();
  }

}