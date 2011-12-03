package JJB;

import java.util.Date;
import java.util.ArrayList;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteJob;

/** This class is responsible for all user queries
 *  
 * @author stylesuxx
 * @version 0.2
 */
public class UserQueries extends SQLiteQueries{

  /** Default Constructor
   * 
   * @param queue The queue for all SQLite Queries.
   * @param log If errors should be logged.
   */
  public UserQueries( SQLiteQueue queue, boolean log ){
    super( queue, log );
  }

  /** Get users with certain status
   *
   * @param status Status of users to lookup.
   * 
   * @return ArrayList<String>
   */
  public ArrayList<String> getUsers( final String status ){
    return queue.execute( new SQLiteJob<ArrayList<String>>(){
      protected ArrayList<String> job(SQLiteConnection connection){
	ArrayList<String> toReturn = new ArrayList<>();

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
	  }
	}

	return toReturn;
      }
    }).complete();
  }

  /** Register a new User
   * 
   * @param jid Jid to register.
   * 
   * @return boolean True if User could be registered, false if he already is registered.
   */
  public boolean registerUser( final String jid ){
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;

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

  /** Delete User from database
   * 
   * @param jid Jid to remove from database.
   * 
   * @return boolean True if user was in Database and is removed from database.
   */
  public boolean deleteUser( final String jid ){
  return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;

	try{
          // check if user is in Database
          st = connection.prepare("select * FROM user WHERE Jid = ? ");
	  st.bind(1, jid);
	  st.step(); 
          if( st.hasRow() ){
              st.dispose();
              st = connection.prepare("DELETE FROM user WHERE Jid = ? ");
              st.bind(1, jid);
              st.step();
              st.dispose();
          }
          else return false;
	}catch( SQLiteException e ){
	  if( log ) System.out.println( "User " + jid + " could not be deleted from database." );
	  return false;
	}

	return true;
      }
    }).complete();  
  }

  /** Set the Status of a user
   *
   * @param jid Jid of user who's status to change.
   * @param status Status to change to.
   * 
   * @return boolean
   */ 
  public boolean setUserStatus( final String jid, final String status){
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;

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

  /** Returns TRUE if the Jid is admin
   * 
   * @param jid Jid to check.
   * 
   * @return boolean
   */
  public boolean isAdminUser( final String jid ){ 
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;

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
	  return false;	}

	return false;
      }
    }).complete();
  }

  /** Returns TRUE if the Jid is approved
   * 
   * @param jid Jid to check.
   * 
   * @return boolean
   */
  public boolean isApprovedUser( final String jid ){
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;

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

  /** Returns TRUE if the Jid is registered      
   * 
   * @param jid Jid to check.
   * 
   * @return boolean
   */
  public boolean isRegisteredUser( final String jid ){
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;

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