/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JJB.Processes;

import JJB.SQLite.UserQueries;
import JJB.SQLite.TvQueries;
import JJB.Tv.TVEntity;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

/**
 *
 * @author stylesuxx
 */
public class PersonalProcesses{
  TvQueries tvQ;
  UserQueries userQ;
  
  public PersonalProcesses( TvQueries tvQ, UserQueries userQ ){
    this.tvQ = tvQ;
    this.userQ = userQ;
  }
  
  private String myAdd( User user, String[] showIDs ){
    int counter = 0;
    for(int i = 2; i < showIDs.length; i++ ){
    try{
      // if show in database, add to userlist
      if( tvQ.getShowInfo( Integer.parseInt( showIDs[i] ) ) != null ){
        userQ.addUserShow( user.getJid(), showIDs );
        counter ++;
      }
      else{
        String[] tmp = { "placeholder", showIDs[i] };
        if( !requestShow( tmp , "user" ).equals( "") ){
          userQ.addUserShow( user.getJid(), showIDs );
          counter ++;
        }
      }
    }catch( Exception e ){}
    
    }
    return counter + "/" + (showIDs.length-2) + " shows added.";
  }
  
  private String myDel( User user, String[] showIDs ){
    for(int i = 2; i < showIDs.length; i++)
      tvQ.deleteUserShow(Integer.parseInt( showIDs[i] ));
    return "Deleted show(s) from your list.";
  }
  
  private String myShows( User user ){
    HashMap<String,Date> map = new HashMap<>();
    DateComparator bvc =  new DateComparator(map);
    TreeMap<String,Date> sorted_map = new TreeMap<>(bvc);
    Date today = new Date();
    String toReturn;

    toReturn = "TV Time:";
    ArrayList<TVEntity> approved = tvQ.getUserShows( user.getJid() );
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
}
