import java.io.File;
import java.util.Date;
import java.util.ArrayList;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteJob;

public class TvQueries extends SQLiteQueries{
  public TvQueries( SQLiteQueue queue, boolean log ){
    super( queue, log );
  }

  public TvQueries( File db, boolean log ){
    super( db, log );
  }

  /** get all Show ID's from database
    * 
    * @return ArrayList<Integer>
    */  
  public ArrayList<Integer> getShowids(){
    return queue.execute( new SQLiteJob<ArrayList<Integer>>(){
      protected ArrayList<Integer> job(SQLiteConnection connection){
	ArrayList<Integer> toReturn = new ArrayList<Integer>();
	try{
	  SQLiteStatement st = connection.prepare( "SELECT tvKey from tv" );

	  try {
	    while( st.step() ){
	      toReturn.add( st.columnInt(0) );
	    }
	  } finally {
	    st.dispose();
	  }
	}catch( Exception e ){
	  if( log ) System.out.println("DB Select Error: Could not get ID's" );
	  e.printStackTrace();
	}
	return toReturn;
      }
    }).complete();
  }

  /** Get all shows from database with the given status
    * 
    * @param status The status of the shows to be returned
    * 
    * @return Arraylist<TVEntity> 
    */
  public ArrayList<TVEntity> getShows( final String status ){
    return queue.execute( new SQLiteJob<ArrayList<TVEntity>>(){
      protected ArrayList<TVEntity> job(SQLiteConnection connection){
	ArrayList<TVEntity> toReturn = new ArrayList<TVEntity>();
	SQLiteStatement st = null;

	try{
	  if( status.equals( "all" ) ) st = connection.prepare( "SELECT * from tv" );
	  else{
	    st = connection.prepare( "SELECT * from tv WHERE status=?;" );
	    st.bind( 1, status );
	  }
	  try {
	    while( st.step() ){
	      if( !st.columnString(6).equals( "" ) ) toReturn.add( new TVEntity( st.columnInt(0), st.columnString(1), st.columnString(2), st.columnString(3), st.columnString(4), st.columnString(5), st.columnInt(6), st.columnString(7), st.columnInt(8), st.columnInt(9), st.columnString(10) ) );
	      else toReturn.add( new TVEntity( st.columnInt(0), st.columnString(1), st.columnString(2), st.columnString(3), st.columnString(4), st.columnString(5), st.columnInt(6) ) );
	    }
	  } finally {
	    st.dispose();
	  }
	}catch( Exception e ){
	  if( log ) System.out.println("DB Select Error: Could not get '" + status + "' shows" );
	  e.printStackTrace();
	}
	return toReturn;
      }
    }).complete();
  }

  /** Request a new show to be added to the database
    * <ul>
    * <li>Adds a show and its details to the database if found on tv Rage</li>
    * <li>Returns the name of the show if found on TV Rage</li>
    * </ul>
    * 
    * @param tv TVLookup Object
    * 
    * @return String The shows title
    */
  public String requestShow( final TVRageLookup tv ){
    return queue.execute( new SQLiteJob<String>(){
      protected String job(SQLiteConnection connection){
	SQLiteStatement st = null;

	try{
	  st = connection.prepare("INSERT into tv ( tvKey, showname, airtime, airday, timezone, status, runtime ) VALUES( ?, ?, ?, ?, ?, 'requested', ? );");
	  st.bind(1, tv.getShowid() );
	  st.bind(2, tv.getShowname() );
	  st.bind(3, tv.getAirtime() );
	  st.bind(4, tv.getAirday() );
	  st.bind(5, tv.getTimezone() );
	  st.bind(6, tv.getRuntime() );

	  try{
	    st.step();
	    st.dispose();
	  }catch( SQLiteException e ){
	    if( log ) System.out.println( "DB Insert Error: show already in database" );
	    st = connection.prepare("SELECT showname FROM tv WHERE tvKey = ? ");
	    st.bind(1, tv.getShowid() );
	    st.step();
	    st.dispose();
	  }	           	

	}catch( SQLiteException e ){
	  if( log ) System.out.println( "DB Insert Error: inserting requested show" );
	  return tv.getShowname();
	}

	return tv.getShowname();
      }
    }).complete();
  }

  /** Request a new show to be added to the database with additional infos
    * <ul>
    * <li>Adds a show and its details to the database if found on tv Rage</li>
    * <li>Returns the name of the show if found on TV Rage</li>
    * </ul>
    * 
    * @param tv TVLookup Object
    * @param nee NextEpisodeEntity Object
    * 
    * @return String The shows title
    */
  public String requestShow( final TVRageLookup tv, final NextEpisodeEntity nee ){
    return queue.execute( new SQLiteJob<String>(){
      protected String job(SQLiteConnection connection){
	SQLiteStatement st = null;

	try{
	  st = connection.prepare( "INSERT into tv ( tvKey, showname, airtime, airday, timezone, status, runtime, nextEpisode, nextSeason, nextTitle, nextDate ) VALUES( ?, ?, ?, ?, ?, 'requested', ?, ?, ?, ?, ? );" );
	  st.bind( 1, tv.getShowid() );
	  st.bind( 2, tv.getShowname() );
	  st.bind( 3, tv.getAirtime() );
	  st.bind( 4, tv.getAirday() );
	  st.bind( 5, tv.getTimezone() );
	  st.bind( 6, tv.getRuntime() );

	  st.bind( 7, nee.getEpisode() );
	  st.bind( 8, nee.getSeason() );
	  st.bind( 9, nee.getEpisodeTitle() );
	  st.bind( 10, nee.getStringDate() );

	  try{
	    st.step();
	    st.dispose();
	  }catch( SQLiteException e ){
	    if( log ) System.out.println( "DB Insert Error: show already in database" );
	    st = connection.prepare("SELECT showname FROM tv WHERE tvKey = ? ");
	    st.bind(1, tv.getShowid() );
	    st.step();
	    st.dispose();
	  }	           	

	}catch( SQLiteException e ){
	  if( log ) System.out.println( "DB Insert Error: inserting requested show with next episode" );
	  return tv.getShowname();
	}

	return tv.getShowname();
      }
    }).complete();
  }

  /** Update a schows next episode
    *
    * @param showID ID of show to update the next episode
    * @param nee Infos about the upcomming episode or null 
    * 
    * @return boolean
    */
  public boolean updateShow( final int showID, final NextEpisodeEntity nee ){
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
	if( showID > 42 ){
      System.out.println( "updated" + showID );
	try{
	  if( nee != null ){
	    st = connection.prepare( "UPDATE tv set nextEpisode = ?, nextSeason = ?, nextTitle = ?, nextDate = ? WHERE tvKey = ?" );
	    st.bind( 1, nee.getEpisode() );
	    st.bind( 2, nee.getSeason() );
	    st.bind( 3, nee.getEpisodeTitle() );
	    st.bind( 4, nee.getStringDate() );
	    st.bind( 5, showID );
	  }
	  else{
	    st = connection.prepare( "UPDATE tv set nextTitle = '', nextSeason = -1, nextEpisode = -1, nextDate = '' WHERE tvKey = ?;" );
	    st.bind( 1, showID );
	  }
	  st.step();

	}catch( SQLiteException e ){
	  if( log ) System.out.println( "DB Update Error: updating shows(" + showID + ") next episode" );
	  e.printStackTrace();
	  return false;
	}
	  st.dispose();

	return true;
	}
	else return false;
      }
    }).complete();

  }

  /** Change status of a show
    * 
    * @param showID ID of the show to approve
    * @param status The status to set the show to
    * 
    * @return boolean True if show was in database
    */
  // TODO: no error if show not in DB
  public boolean setShowStatus( final int showID, final String status){
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
	boolean toReturn = false;

	try{
	  st = connection.prepare("UPDATE tv SET status=? WHERE tvKey = ? ");
	  st.bind( 2, showID );
	  st.bind( 1, status );
	  st.step();
	  st.dispose();
	}catch( SQLiteException e ){
	  if( log ) System.out.println( "DB Update Error: updating show status" );
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
	  if( log ) System.out.println( "DB Delete Error: No show with ID# " + showID );
	  return false;
	}  

	return true;
      }
    }).complete();
  }

}