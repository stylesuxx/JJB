import java.io.File;
import java.util.ArrayList;

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
  */
public class DataBase{
  static private File dbFile;
  static private SQLiteConnection db;
  SQLiteQueue queue;

  /** Constructor with own database
    * @param database Location of database to use
    */
  public DataBase(String database){
    dbFile = new File( database );
    queue = new SQLiteQueue( dbFile );
    queue.start();
  }
  
  /** Default Constructor
    * <ul>
    * <li>This Constructor (tries to create and) uses the file test.db as Database</li>
    * </ul>
    */
  public DataBase(){
    dbFile = new File( "test.db" );
    queue = new SQLiteQueue( dbFile );
    queue.start();

  }

  /** Conects to database or tries to create new one if given does not exist^
    */
  public void connect(){
    db = new SQLiteConnection( dbFile );
    try{
      db.open(true);
    }catch( SQLiteException e ){
      System.out.println( "Could not connect to Database." );
      System.exit(1);
    }

    // Create DB if not already existant
    try{
      SQLiteStatement[] st = {
	db.prepare( "create table user (userID INTEGER PRIMARY KEY AUTOINCREMENT, Jid TEXT, status TEXT, userStart DATE);"),
	db.prepare( "create table tv (tvKey INTEGER PRIMARY KEY, showname TEXT, airtime TEXT, airday TEXT, timezone TEXT, status TEXT);"),
	db.prepare( "create table stats (statID INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, value TEXT)" )
      };
      for(int i = 0; i < st.length; i++){
	try{
	  st[i].step();
	} finally{ 
	  st[i].dispose();
	}
      }
      System.out.println( "Created new Database" );
    }catch( Exception e ){ System.out.println("Using existing Database"); }
  }

  /** Disconnect from database
    */
  public void disconnect(){
    db.dispose();
  }

  /** Returns TVEntity of one given show
    * @param showID ID of show to return 
    * @return TVEntity 
    */
  public TVEntity getShow( int showID ){
    TVEntity toReturn = null;

    try{
      SQLiteStatement st = db.prepare( "SELECT * from tv WHERE tvKey = ?" );
      st.bind(1, showID);
      try {
	st.step();
	toReturn = new TVEntity( st.columnInt(0), st.columnString(1), st.columnString(2), st.columnString(3), st.columnString(4), st.columnString(5) );
      } finally {
	st.dispose();
      }
    }catch( Exception e ){ System.out.println("Log: Select Error: Could not get show with ID# " + showID); }

    return toReturn;
  }
 
  /** Get all shows from DB that have the status 'approved'
    * @return Arraylist<TVEntity> 
    */
  public ArrayList<TVEntity> getApproved(){
    return queue.execute( new SQLiteJob<ArrayList<TVEntity>>(){
      protected ArrayList<TVEntity> job(SQLiteConnection connection){
	ArrayList<TVEntity> toReturn = new ArrayList<TVEntity>();
	try{
	  SQLiteStatement st = connection.prepare( "SELECT * from tv WHERE status = 'approved'" );
	  try {
	    while( st.step() ){
	      toReturn.add( new TVEntity( st.columnInt(0), st.columnString(1), st.columnString(2), st.columnString(3), st.columnString(4), st.columnString(5) ) );
	    }
	  } finally {
	    st.dispose();
	  }
	}catch( Exception e ){
	  System.out.println("Log: Select Error: Could not get approved shows" );
	  e.printStackTrace();
	}
	return toReturn;
      }
    }).complete();
  }

  /** Get all shows from DB that have the status 'never'
    * @return Arraylist<TVEntity> 
    */
  public ArrayList<TVEntity> getNever(){
    return queue.execute( new SQLiteJob<ArrayList<TVEntity>>(){
      protected ArrayList<TVEntity> job(SQLiteConnection connection){
	ArrayList<TVEntity> toReturn = new ArrayList<TVEntity>();

	try{
	  SQLiteStatement st = connection.prepare( "SELECT * from tv WHERE status = 'never'" );
	  try {
	    while( st.step() ){
	      toReturn.add( new TVEntity( st.columnInt(0), st.columnString(1), st.columnString(2), st.columnString(3), st.columnString(4), st.columnString(5) ) );
	    }
	  } finally {
	    st.dispose();
	  }
	}catch( Exception e ){ System.out.println("Log: Select Error: Could not get never shows" ); }

	return toReturn;
      }
    }).complete();
  }

  /** Get all shows from DB that have the status 'requested'
    * @return Arraylist<TVEntity> 
    */
  public ArrayList<TVEntity> getRequested(){
    return queue.execute( new SQLiteJob<ArrayList<TVEntity>>(){
      protected ArrayList<TVEntity> job(SQLiteConnection connection){
	ArrayList<TVEntity> toReturn = new ArrayList<TVEntity>();

	try{
	  SQLiteStatement st = connection.prepare( "SELECT * from tv WHERE status = 'requested'" );
	  try {
	    while( st.step() ){
	      toReturn.add( new TVEntity( st.columnInt(0), st.columnString(1), st.columnString(2), st.columnString(3), st.columnString(4), st.columnString(5) ) );
	    }
	  } finally {
	    st.dispose();
	  }
	}catch( Exception e ){ System.out.println("Log: Select Error: Could not get requested shows" ); }

	return toReturn;
      }
    }).complete();
  }

  /** Request a new show to be added to the database
    * <ul>
    * <li>Adds a show and its details to the database if found on tv Rage</li>
    * <li>Returns the name of the show if found on TV Rage</li>
    * </ul>
    * @param showID ID of show to request
    * @return String 
    */
  public String requestShow( final int showID, final String showname, final String airtime, final String airday, final String timezone ){
    return queue.execute( new SQLiteJob<String>(){
      protected String job(SQLiteConnection connection){
	SQLiteStatement st = null;

	try{
	  st = connection.prepare("INSERT into tv ( tvKey, showname, airtime, airday, timezone, status ) VALUES( ?, ?, ?, ?, ?, 'requested' );");
	  st.bind(1, showID);
	  st.bind(2, showname);
	  st.bind(3, airtime);
	  st.bind(4, airday);
	  st.bind(5, timezone);

	  try{
	    st.step();
	    st.dispose();
	  }catch( SQLiteException e ){
	    System.out.println( "Show is already in Database" );
	    st = db.prepare("SELECT showname FROM tv WHERE tvKey = ? ");
	    st.bind(1, showID );
	    st.step();
	    st.dispose();
	  }	           	

	}catch( SQLiteException e ){
	  System.out.println( "Error inserting entry!" );
	  return showname;
	}

	return showname;
      }
    }).complete();
  }

  /** Approves a show
    * @param showID ID of the show to approve
    * @return boolean True if show was in database
    */
  public boolean approveShow( final int showID ){
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
	boolean toReturn = false;

	try{
	  st = connection.prepare("UPDATE tv SET status='approved' WHERE tvKey = ? ");
	  st.bind( 1, showID );
	  st.step();
	  st.dispose();
	}catch( SQLiteException e ){
	  System.out.println( "Error updating entry!" );
	  return false;
	}
	
	return true;
      }
    }).complete();
  }

  /** Set the show on the never List
    * @param showID ID of show to set on the never List
    * @return boolean
    */
  public boolean neverShow( final int showID ){
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
	boolean toReturn = false;

	try{
	  st = db.prepare("UPDATE tv SET status='never' WHERE tvKey = ? ");
	  st.bind( 1, showID );
	  st.step();
	  st.dispose();
	}catch( SQLiteException e ){
	  System.out.println( "Log: Update Error: No show with ID# " + showID );
	  return false;
	}
	
	return true;
      }
    }).complete();
  }

  /** Deletes show from database
    * @param showID ID of show to delete
    * @return boolean 
    */
  public boolean deleteShow( final int showID ){ 
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
	boolean toReturn = false;

	try{
	  st = db.prepare("DELETE FROM tv WHERE tvKey = ? ");
	  st.bind( 1, showID );
	  st.step();
	  st.dispose();
	}catch( SQLiteException e ){
	  System.out.println( "Log: Delete Error: No show with ID# " + showID );
	  return false;
	}  

	return true;
      }
    }).complete();
  }
}