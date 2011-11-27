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
  private static private File dbFile;
  private static private SQLiteConnection db;
  private SQLiteQueue queue;
  private boolean log = true;

  /** Constructor with existing database
    * 
    * @param database Location of database to use
    * @param admin User who is to add as admin to the database
    */
  public DataBase( String admin, String database){
    dbFile = new File( database );
    queue = new SQLiteQueue( dbFile );
    queue.start();
    createAdmin( admin );
  }
  
  /** Default Constructor
    * <ul>
    * <li>This Constructor (tries to create and) uses the file test.db as Database</li>
    * </ul>
    * 
    * @param admin User who is to add as admin to the database
    */
  public DataBase( String admin ){
    dbFile = new File( "test.db" );
    queue = new SQLiteQueue( dbFile );
    queue.start();
    createAdmin( admin );
  }

  /** Conects to database or tries to create new one if given does not exist
    */
  public void connect(){
    db = new SQLiteConnection( dbFile );
    try{
      db.open(true);
    }catch( SQLiteException e ){
      System.out.println( "Could not connect to Database." );
      System.exit(-1);
    }

    // Create DB if not already exists
    try{
      SQLiteStatement[] st = {
	db.prepare( "create table user (Jid TEXT PRIMARY KEY, status TEXT, date TEXT);"),
	db.prepare( "create table tv (tvKey INTEGER PRIMARY KEY, showname TEXT, airtime TEXT, airday TEXT, timezone TEXT, status TEXT, length INTEGER, nextEpisode INTEGER, nextSeason INTEGER, nextTitle TEXT, nextDate TEXT);"),
	db.prepare( "create table stats (statID INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, value TEXT);" )
      };
      for(int i = 0; i < st.length; i++){
	try{
	  st[i].step();
	} finally{ 
	  st[i].dispose();
	}
      }
      System.out.println( "Created new Database: " + dbFile.toString() );
    }catch( Exception e ){ System.out.println("Using existing Database: " + dbFile.toString(); ) }
  }

  /** Disconnect from database
    */
  public void disconnect(){
    db.dispose();
    queue.stop( true );
  }

  //TODO: get next Episode Infos
  /** Returns TVEntity of one given show
    * 
    * @param showID ID of show to return
    *  
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
 
  // The following three methods can be merged to one which just fills in a sstring instead of static
  /** Get all shows from database with status 'approved'
    * 
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
	  System.out.println("DB Select Error: Could not get approved shows" );
	  e.printStackTrace();
	}
	return toReturn;
      }
    }).complete();
  }

  /** Get all shows from database with status 'never'
    * 
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
	}catch( Exception e ){ System.out.println("DB Select Error: Could not get never shows" ); }

	return toReturn;
      }
    }).complete();
  }

  /** Get all shows from database with status 'requested'
    * 
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
	}catch( Exception e ){ if( log ) System.out.println("DB Select Error: Could not get requested shows" ); }

	return toReturn;
      }
    }).complete();
  }

  /** Crate an admin user - this is used for the user provided when the bot is started
    * 
    * @param jid Jid of the admin user to create
    * 
    * @return boolean 
    */ 
  public boolean createAdmin( final String jid ){
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;

	try{
	  st = connection.prepare("INSERT into user ( Jid, status, date ) VALUES( ?, 'admin', 'date' );");
	  st.bind(1, jid);
	  st.step();
	  st.dispose();

	}catch( SQLiteException e ){
	  if( log ) System.out.println( "DB Insert Error: Admin User already exists" );
	  return false;
	}

	return true;
      }
    }).complete();
  }

  // TODO: update for next show
  /** Request a new show to be added to the database
    * <ul>
    * <li>Adds a show and its details to the database if found on tv Rage</li>
    * <li>Returns the name of the show if found on TV Rage</li>
    * </ul>
    * 
    * @param showID ID of show
    * @param showName Name of the show
    * @param airtime Airtime of the show
    * @param airday Airday of the show
    * @param timezone Timezone of the show
    * @param length Length of the show
    * 
    * @return String 
    */
  public String requestShow( final int showID, final String showname, final String airtime, final String airday, final String timezone, final int length ){
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
	    if( log ) System.out.println( "DB Insert Error: show already in database" );
	    st = connection.prepare("SELECT showname FROM tv WHERE tvKey = ? ");
	      st.bind(1, showID );
	    st.step();
	    st.dispose();
	  }	           	

	}catch( SQLiteException e ){
	  if( log ) System.out.println( "DB Insert Error: inserting requested show" );
	  return showname;
	}

	return showname;
      }
    }).complete();
  }

  // TODO: the following two methods can be merged to one

  // TODO: no error if show not in DB
  /** Approves a show
    * 
    * @param showID ID of the show to approve
    * 
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
    * 
    * @param showID ID of show to set on the never List
    * 
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
    * 
    * @param showID ID of show to delete
    * 
    * @return boolean 
    */
  public boolean deleteShow( final int showID ){ 
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
	boolean toReturn = false;

	try{
	  st = connection.prepare("DELETE FROM tv WHERE tvKey = ? ");
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

  // This part is for the Users

  /** Register a new User
    * 
    * @param jid Jid to register
    * 
    * @return boolean True if User could be added
    * TESTED
    */
  public boolean registerUser( final String jid ){
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
	boolean toReturn = false;

	try{
	  st = connection.prepare("INSERT into user ( Jid, status, date ) VALUES( ?, ?, ? );");
	  st.bind(1, jid);
	  st.bind(2, "registered");
	  st.bind(3, "Here comes the date and timestamp");
	  st.step();
	  st.dispose();
	}catch( SQLiteException e ){
	  if( log )System.out.println( "Log: Insert Error: User already in database" );
	  return false;
	}
	return true;
      }
    }).complete();
  }

  /** Deletes User from database
    * 
    * @param jid JID to remove from database
    * 
    * @return boolean True if usere was removed from database
    * TESTED
    * - no exception if user is not in database
    */
  public boolean unregisterUser( final String jid ){
  return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
	boolean toReturn = false;

	try{
	  st = connection.prepare("DELETE FROM user WHERE Jid = ? ");
	  st.bind(1, jid);
	  st.step();
	  st.dispose();
	}catch( SQLiteException e ){
	  if( log ) System.out.println( "Log: Delete Error: User not in database" );
	  return false;
	}
	return true;
      }
    }).complete();  
  }

  /** Approves a User which has already registered
    * 
    * @param jid Jid of user to approve
    * 
    * @return boolean
    * TESTED
    * - Does nt throw exception if user not in DB
    */
  public boolean approveUser( final String jid ){
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
	boolean toReturn = false;

	try{
	  st = connection.prepare("UPDATE user SET status='approved' WHERE Jid = ? ");
	  st.bind( 1, jid );
	  st.step();
	  st.dispose();
	}catch( SQLiteException e ){
	  System.out.println( "Log: Error updating User entry!" );
	  return false;
	}
	
	return true;
      }
    }).complete();
  }

  /** Promote user to admin
    * 
    * @param jid Jid of user to promote
    * 
    * @return boolen
    * TESTED
    * - does not throw Exception if user to promote does not exist
    */
  public boolean setAdmin( final String jid ){
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
	boolean toReturn = false;

	try{
	  st = connection.prepare("UPDATE user SET status='admin' WHERE Jid = ? ");
	  st.bind( 1, jid );
	  st.step();
	  st.dispose();
	}catch( SQLiteException e ){
	  System.out.println( "Log: Error updating User entry!" );
	  return false;
	}
	
	return true;
      }
    }).complete();
  }

  /** Returns true if the JID is admin
    * 
    * @param jid Jid to check
    * 
    * @return boolean
    * TESTED
    */
  public boolean isAdminUser( final String jid ){ 
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
	boolean toReturn = false;

	try{
	  st = connection.prepare("select status FROM user WHERE Jid = ? ");
	  st.bind( 1, jid );
	  st.step();
	  if( st.columnString(0).equals( "admin" ) ){
	    st.dispose();
	    return true;
	  }
	}catch( SQLiteException e ){
	  if( log ) System.out.println( "Log: Select Error: No such Jid found" );
	  //e.printStackTrace();
	  return false;
	}
	return false;
      }
    }).complete();
  }

  /** Returns true if the JID is approved
    * 
    * @param jid Jid to check
    * 
    * @return boolean
    * TESTED
    */
  public boolean isApprovedUser( final String jid ){
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
	boolean toReturn = false;

	try{
	  st = connection.prepare("select status FROM user WHERE Jid = ? ");
	  st.bind( 1, jid );
	  st.step();
	  if( st.columnString(0).equals( "approved" ) | st.columnString(0).equals( "admin" ) ){
	    st.dispose();
	    return true;
	  }
	}catch( SQLiteException e ){
	  System.out.println( "Log: Select Error: No such Jid found" );
	  //e.printStackTrace();
	  return false;
	}
	return false;
      }
    }).complete();
  }

  /** Returns true if the JID is registered      
    * 
    * @param jid Jid to check
    * 
    * @return boolean
    * TESTED
    */
  public boolean isRegisteredUser( final String jid ){
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
	boolean toReturn = false;

	try{
	  st = connection.prepare("select status FROM user WHERE Jid = ? ");
	  st.bind( 1, jid );
	  st.step();
	  if( st.columnString(0).equals( "registered" ) ){
	    st.dispose();
	    return true;
	  }
	}catch( SQLiteException e ){
	  System.out.println( "Log: Select Error: No such Jid found" );
	  //e.printStackTrace();
	  return false;
	}
	return false;
      }
    }).complete();
  }
  
  // TODO: The next three methods can be merged to one
  // TESTED
  public ArrayList<String> getRegisteredUsers(){
    return queue.execute( new SQLiteJob<ArrayList<String>>(){
      protected ArrayList<String> job(SQLiteConnection connection){
	ArrayList<String> toReturn = new ArrayList<String>();

	try{
	  SQLiteStatement st = connection.prepare( "SELECT Jid from user WHERE status = 'registered'" );
	  try {
	    while( st.step() ){
	      toReturn.add( st.columnString(0) );
	    }
	  } finally {
	    st.dispose();
	  }
	}catch( Exception e ){ System.out.println("Log: Select Error: Could not get requested users" ); }

	return toReturn;
      }
    }).complete();
  }

  // TESTED
  public ArrayList<String> getApprovedUsers(){
    return queue.execute( new SQLiteJob<ArrayList<String>>(){
      protected ArrayList<String> job(SQLiteConnection connection){
	ArrayList<String> toReturn = new ArrayList<String>();

	try{
	  SQLiteStatement st = connection.prepare( "SELECT Jid from user WHERE status = 'approved' OR status = 'admin'" );
	  try {
	    while( st.step() ){
	      toReturn.add( st.columnString(0) );
	    }
	  } finally {
	    st.dispose();
	  }
	}catch( Exception e ){ System.out.println("Log: Select Error: Could not get requested users" ); }

	return toReturn;
      }
    }).complete();
  }

  //TESTED
  public ArrayList<String> getAdminUsers(){
    return queue.execute( new SQLiteJob<ArrayList<String>>(){
      protected ArrayList<String> job(SQLiteConnection connection){
	ArrayList<String> toReturn = new ArrayList<String>();

	try{
	  SQLiteStatement st = connection.prepare( "SELECT Jid from user WHERE status = 'admin'" );
	  try {
	    while( st.step() ){
	      toReturn.add( st.columnString(0) );
	    }
	  } finally {
	    st.dispose();
	  }
	}catch( Exception e ){ System.out.println("Log: Select Error: Could not get requested users" ); }

	return toReturn;
      }
    }).complete();
  }

}