package JJB.Processes;

import JJB.Tv.NextEpisode;
import JJB.Tv.NextEpisodeEntity;
import JJB.SQLite.TvQueries;
import JJB.Tv.TVEntity;
import JJB.Tv.TVRageLookup;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

/**
 *
 * @author stylesuxx
 */
public class ShowsProcesses {
  TvQueries tvQ;
  private Date lastUpdate;
  
  public ShowsProcesses( TvQueries tvQ ){
    this.tvQ = tvQ;
    
    // Update all shows
    updateAll();
  }
  
  public Date lastUpdate(){ return lastUpdate; }
  
  /** Update a shows next Episode
   *
   * @param showID ID of show to update.
   */
  private void updateNext( int showID ){
    NextEpisode ne = new NextEpisode( showID );
    NextEpisodeEntity nee = ne.getNext();
    tvQ.updateShow( showID, nee );
  }

  /** Update all shows next episodes 
   * Based on updateNext 
   * 
   * @returns String
   */
  public String updateAll(){
    ArrayList<Integer> toUpdate = tvQ.getShowids();
    for(int i = 0; i < toUpdate.size(); i++)
      updateNext( toUpdate.get(i) );
    lastUpdate = new Date();
    return "All " + toUpdate.size() + " shows in database updated." ;
  }
  
  /** Returns the approved shows to the MUC
    * 
    * @return String 
    */
  public String showsList(){
    // Update the db all 24hrs, there should be a better place than here - we will see
    if( new Date().getTime() - lastUpdate.getTime() > ( 1000 * 3600 * 24 ) ) updateAll();
    HashMap<String,Date> map = new HashMap<>();
    DateComparator bvc =  new DateComparator(map);
    TreeMap<String,Date> sorted_map = new TreeMap<>(bvc);
    Date today = new Date();
    String toReturn;

    toReturn = "TV Time:";
    ArrayList<TVEntity> approved = tvQ.getShows( "approved" );
    if( approved.size() > 0 ){
      for( int i = 0; i < approved.size(); i++ ){
	if( approved.get( i ).hasNext() ){
	  String title = approved.get( i ).getShowname() +" S" + approved.get( i ).getNextSeason() + "E" + approved.get( i ).getNextEpisode() + ": " + approved.get( i ).getNextTitle();
	  Date next = approved.get( i ).getNextDate().getTime();
	  map.put( title, next );
	}
      }
      sorted_map.putAll(map);

      for (String key : sorted_map.keySet()) {
	long sec = (sorted_map.get(key).getTime() - today.getTime()) / 1000;
	Boolean ago = false;

	if ( sec < 0 ){
	  sec *= -1;
	  ago = true;
	}
	long min = ( sec/60 ) % 60;
	long hours = ( sec/3600 ) % 3600 % 24;
	long days = ( sec/3600 ) / 24;

	String time = "";
	if( days > 0 ) time += days + 1 + "d ";
	else{
	  if( hours > 0 ) time += hours + "h ";
	  time += min + "min ";
	}
	if( ago ) time += "ago";
	if( ( ago && days < 3 ) || !ago )
	  toReturn += "\n" + key + " ( "+ time +" )";
      }

    }
    if(map.size() > 0)
      return toReturn;
    else
    return "No shows on the Watchlist.";
  }
  
  /** Get shows with status - this is basically a wrapper method for all get shows methods
   * 
   * @param status Status of shows to return
   */
  private ArrayList<TVEntity> getShows( String status ){
    return tvQ.getShows( status );
  }

  /** Returns the never shows as formatted String ready to print
   * The String only contains the shows name - not more details
   *
   * @return String
   */
  public String showsNever(){
    String toReturn = "Shows we will never watch:";
    ArrayList<TVEntity> approved = getShows( "never" );
    if( approved.size() > 0 ){
      for( int i = 0; i < approved.size(); i++ )
	toReturn += "\n" + approved.get( i ).getShowname();
      return toReturn;
    }
    else return "There are no shows on the never list."; 
  }

  /** Returns the requested shows as formatted String ready to print
   * The info contains the full info we have in the database
   * 
   * @return String
   */
  public String showsRequested(){
    String toReturn = "This shows are requested:";
    ArrayList<TVEntity> approved = getShows( "requested" );
    if( approved.size() > 0 ){
      for( int i = 0; i < approved.size(); i++ )
	toReturn += "\n" + approved.get( i ).toString();
      return toReturn;
    }
    else return "Currently there are no requested shows.";
  }

  public String deleteShow( int showID){
    tvQ.deleteShow( showID );
    return "Show deleted";
  }

  /** Returns all shows by name in the database
    * 
    * @return String
    */
  public String showsAll(){
    String toReturn = "All shows:";
    ArrayList<TVEntity> approved = tvQ.getShows( "all" );
    if( approved != null )
      for( int i = 0; i < approved.size(); i++ )
	toReturn += "\n" + approved.get( i ).toString();
    return toReturn;
  }

  /** Returns the name of the requested show and adds it to the database if it exists
    * 
    * @param showID String Array with show ID's
    * 
    * @return String
    */
  // Check why invalid shows are being add to the db,....
  public String requestShow( String[] shows, String status ){
    int showID;
    TVRageLookup tv = null;
    NextEpisode ne;
    NextEpisodeEntity nee = null;
    String toReturn = "";
    for( int i = 1; i < shows.length; i++ ){
      try{
	showID = Integer.parseInt( shows[i] );
	tv = new TVRageLookup( showID );
	ne = new NextEpisode( showID );
	nee = ne.getNext();
      }catch( Exception e ){}
      // If has next episode, set additional Infos
      if( nee != null ) toReturn += "\n'" + tvQ.requestShow( tv, nee, status ) + "' has been added to the request list. (upcomming episode)";
      else if( tv != null ) toReturn += "\n'" + tvQ.requestShow( tv, status ) + "' has been added to the request list.";
    }

    return toReturn;
  }

  /** Returns the name of the approved show and approves it in the database if it exists
    * 
    * @param showID ID of show to request
    * 
    * @return String
    */
  // does not check if show approved, should return a string,...
  public String approveShow( int showID ){
    tvQ.setShowStatus( showID, "approved" );
    TVEntity tv = tvQ.getShowInfo( showID );
    if( tv != null ){
      tvQ.setShowStatus( showID, "approved" );
      return "'" + tv.getShowname() + "' has been approved.";
    }
    else return "This show is not requested, try requesting it before approving.";
  }
  
  public String neverShow( int showID ){
    tvQ.setShowStatus( showID, "never" );
    TVEntity tv = tvQ.getShowInfo( showID );
    if( tv != null ){
      tvQ.setShowStatus( showID, "never" );
      return "'" + tv.getShowname() + "' has been add to the never list.";
    }
    else return "This show is not in the database.";
  }
  
  public String approveAllShows(){
    String toReturn = "";
    ArrayList<TVEntity> approved = tvQ.getShows( "requested" );
    if( approved.size() > 0 ){
      for( int i = 0; i < approved.size(); i++ )
	toReturn += "\n" + approveShow( approved.get( i ).getShowid() );
      return toReturn;
    }
    else return "Try requesting some shows before approving them.";
  }

}
