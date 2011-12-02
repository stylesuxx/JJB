import java.io.File;
import java.util.Date;
import java.util.ArrayList;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteJob;

public class UserQueries extends SQLiteQueries{
  public UserQueries( SQLiteQueue queue, boolean log ){
    super( queue, log );
  }

  public UserQueries( File db, boolean log ){
    super( db, log );
  }

  /** Get users with certain status
    *
    * @param status Status of users to lookup
    * 
    * @return ArrayList<String>
    */
  public ArrayList<String> getUsers( final String status ){
    return queue.execute( new SQLiteJob<ArrayList<String>>(){
      protected ArrayList<String> job(SQLiteConnection connection){
	ArrayList<String> toReturn = new ArrayList<String>();

	try{
	  SQLiteStatement st = connection.prepare( "SELECT Jid from user WHERE status = ?" );
	  st.bind( 1, status );
	  try {
	    while( st.step() ){
	      toReturn.add( st.columnString(0) );
	    }
	  } finally {
	    st.dispose();
	  }
	}catch( Exception e ){
	  if( log ){
	    System.out.println( "DB Select Error: Could not get users with status: " + status );
	    e.printStackTrace();
	  }
	}

	return toReturn;
      }
    }).complete();
  }

  /** Register a new User
    * 
    * @param jid Jid to register
    * 
    * @return boolean True if User could be added
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
	  st.bind(3, new Date().toString() );
	  st.step();
	  st.dispose();
	}catch( SQLiteException e ){
	  if( log ) System.out.println( "DB Insert Error: user: " + jid +" already in database" );
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
	  if( log ) System.out.println( "DB Delete Error: could not delete User" );
	  return false;
	}

	return true;
      }
    }).complete();  
  }

  /** Set the Status of a user
    *
    * @param jid Jid of users who's status to change
    * @param status Status to change to
    * 
    * @return boolean
    */ 
  public boolean setUserStatus( final String jid, final String status){
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
	boolean toReturn = false;

	try{
	  st = connection.prepare("UPDATE user SET status = ? WHERE Jid = ? ");
	  st.bind( 2, jid );
	  st.bind( 1, status );
	  st.step();
	  st.dispose();
	}catch( SQLiteException e ){
	  if( log ) System.out.println( "DB Update Error: updating user status" );
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
	  if( log ) System.out.println( "DB Select Error: Jid not found: " + jid );
	  e.printStackTrace();
	  return false;	}

	return false;
      }
    }).complete();
  }

  /** Returns true if the JID is approved
    * 
    * @param jid Jid to check
    * 
    * @return boolean
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
	  if( st.columnString(0).equals( "approved" ) || st.columnString(0).equals( "admin" ) ){
	    st.dispose();
	    return true;
	  }
	}catch( SQLiteException e ){
	  if( log ) System.out.println( "DB Select Error: Jid not found: " + jid );
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
	  if( log ) System.out.println( "DB Select Error: Jid not found: " + jid );
	  return false;
	}

	return false;
      }
    }).complete();
  }

}