package JJB.SQLite;

import JJB.Tv.NextEpisodeEntity;
import JJB.Tv.TVEntity;
import JJB.Tv.TVRageLookup;

import java.util.ArrayList;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteJob;

/** This class Holds all the Queries on the Tv table
 * 
 * @author stylesuxx
 * @version 0.2
 */
public class TvQueries extends SQLiteQueries{

  /** Default Constructor
   * 
   * @param queue The Jobqueue to use.
   * @param log If errors should be printed to the Terminal.
   */
  public TvQueries( SQLiteQueue queue, boolean log ){
    super( queue, log );
  }

  /** Get all Show ID's from database
   * 
   * @return ArrayList<Integer>
   */  
  public ArrayList<Integer> getShowids(){
    return queue.execute( new SQLiteJob<ArrayList<Integer>>(){
      protected ArrayList<Integer> job(SQLiteConnection connection){
	ArrayList<Integer> toReturn = new ArrayList<>();
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
	  if( log ) System.out.println("DB Select Error: " + e.getMessage() );
	}

        return toReturn;
      }
    }).complete();
  }

  /** Get all shows from database with the given status
   * 
   * @param status The status of the shows to be returned.
   * @return ArrayList<TVEntity> 
   */
  public ArrayList<TVEntity> getShows( final String status ){
    return queue.execute( new SQLiteJob<ArrayList<TVEntity>>(){
      protected ArrayList<TVEntity> job(SQLiteConnection connection){
	ArrayList<TVEntity> toReturn = new ArrayList<>();
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
	  if( log ) System.out.println("DB Select Error: " + e.getMessage() );
	}
        
	return toReturn;
      }
    }).complete();
  }
  
  /** Get a users Watchlist shows from database
   * 
   * @param jid The Jid of the users whos Watchlist to return.
   * @return ArrayList<TVEntity> 
   */
  public ArrayList<TVEntity> getUserShows( final String jid ){
    return queue.execute( new SQLiteJob<ArrayList<TVEntity>>(){
      protected ArrayList<TVEntity> job(SQLiteConnection connection){
	ArrayList<TVEntity> toReturn = new ArrayList<>();
	SQLiteStatement st = null;
	try{
	    st = connection.prepare( "SELECT * from tv WHERE tvKey in (select show from userTV where Jid = ?)" );
	    st.bind( 1, jid );
	  try {
	    while( st.step() ){
	      if( !st.columnString(6).equals( "" ) ) toReturn.add( new TVEntity( st.columnInt(0), st.columnString(1), st.columnString(2), st.columnString(3), st.columnString(4), st.columnString(5), st.columnInt(6), st.columnString(7), st.columnInt(8), st.columnInt(9), st.columnString(10) ) );
	      else toReturn.add( new TVEntity( st.columnInt(0), st.columnString(1), st.columnString(2), st.columnString(3), st.columnString(4), st.columnString(5), st.columnInt(6) ) );
	    }
	  } finally {
	    st.dispose();
	  }
	}catch( Exception e ){
	  if( log ) System.out.println("DB Select Error: " + e.getMessage() );
	}
	return toReturn;
      }
    }).complete();
  }
  
  /** Get Infos about a single show
   * 
   * @param showID ID of show to lookup.
   * @return TVEntity
   */
  public TVEntity getShowInfo( final int showID ){
    return queue.execute( new SQLiteJob<TVEntity>(){
      protected TVEntity job(SQLiteConnection connection){
	TVEntity toReturn = null;
	SQLiteStatement st = null;
	try{
	  st = connection.prepare( "SELECT * from tv where tvKey = ?" );
          st.bind( 1, showID );
          st.step();
          if( st.hasRow() ){
            try {
              if( !st.columnString(6).equals( "" ) ) toReturn = ( new TVEntity( st.columnInt(0), st.columnString(1), st.columnString(2), st.columnString(3), st.columnString(4), st.columnString(5), st.columnInt(6), st.columnString(7), st.columnInt(8), st.columnInt(9), st.columnString(10) ) );
              else toReturn = ( new TVEntity( st.columnInt(0), st.columnString(1), st.columnString(2), st.columnString(3), st.columnString(4), st.columnString(5), st.columnInt(6) ) );

            } finally {
              st.dispose();
            }
          }
          else return null;
	}catch( Exception e ){
	  if( log ) System.out.println("DB Select Error: " + e.getMessage() );
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
   * @param tv TVLookup Object.
   * @return String
   */
  public String requestShow( final TVRageLookup tv, final String status ){
    return queue.execute( new SQLiteJob<String>(){
      protected String job(SQLiteConnection connection){
	SQLiteStatement st = null;
	try{
	  st = connection.prepare("INSERT into tv ( tvKey, showname, airtime, airday, timezone, status, runtime ) VALUES( ?, ?, ?, ?, ?, ?, ? );");
	  st.bind(1, tv.getShowid() );
	  st.bind(2, tv.getShowname() );
	  st.bind(3, tv.getAirtime() );
	  st.bind(4, tv.getAirday() );
	  st.bind(5, tv.getTimezone() );
          st.bind( 6, status);
	  st.bind(7, tv.getRuntime() );

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
   * @param tv TVLookup Object.
   * @param nee NextEpisodeEntity Object.
   * @return String
   */
  public String requestShow( final TVRageLookup tv, final NextEpisodeEntity nee, final String status ){
    return queue.execute( new SQLiteJob<String>(){
      protected String job(SQLiteConnection connection){
	SQLiteStatement st = null;
	try{
	  st = connection.prepare( "INSERT into tv ( tvKey, showname, airtime, airday, timezone, status, runtime, nextEpisode, nextSeason, nextTitle, nextDate ) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? );" );
	  st.bind( 1, tv.getShowid() );
	  st.bind( 2, tv.getShowname() );
	  st.bind( 3, tv.getAirtime() );
	  st.bind( 4, tv.getAirday() );
	  st.bind( 5, tv.getTimezone() );
          st.bind( 6, status );
	  st.bind( 7, tv.getRuntime() );

	  st.bind( 8, nee.getEpisode() );
	  st.bind( 9, nee.getSeason() );
	  st.bind( 10, nee.getEpisodeTitle() );
	  st.bind( 11, nee.getStringDate() );

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

  /** Update a shows next episode
   *
   * @param showID ID of show to update the next episode.
   * @param nee Infos about the upcoming episode or null.
   * @return boolean
   */
  public boolean updateShow( final int showID, final NextEpisodeEntity nee ){
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
	if( showID > 0 ){
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
	  if( log ) System.out.println( "DB Update Error: " + e.getMessage() );
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
   * @param showID ID of the show to change the status.
   * @param status The status to set the show to.
   * @return boolean
   */
  // TODO: no error if show not in DB
  public boolean setShowStatus( final int showID, final String status){
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
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
   * @param showID ID of show to delete.
   * @return boolean 
   */
  public boolean deleteShow( final int showID ){ 
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
	try{
	  st = connection.prepare("DELETE FROM tv WHERE tvKey = ? ");
	  st.bind( 1, showID );
	  st.step();
	  st.dispose();
          
          st = connection.prepare("DELETE FROM userTv WHERE show = ? ");
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
  
  /** Delete a show from a private Watchlist
   * 
   * @param jid Jid of user who wants to delete a show.
   * @param showID ID of show to delete.
   * @return boolean
   */
  public boolean deleteUserShow( final String jid, final int showID ){ 
    return queue.execute( new SQLiteJob<Boolean>(){
      protected Boolean job(SQLiteConnection connection){
	SQLiteStatement st = null;
	try{
          st = connection.prepare("DELETE FROM userTv WHERE show = ? AND Jid = ?");
	  st.bind( 1, showID );
          st.bind( 2, jid );
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