package JJB;

import java.io.*;
import java.net.*;
import java.util.regex.*;
import java.util.HashMap;

/** This Class retrieves information about a shows upcomming episode
  *
  * @author stylesuxx
  * @version 0.1 
  */
public class NextEpisode{
  private int showID = -1;;
  private String next = null;
  private String dayTime = null;
  private String showName = null;

  private InputStream is = null;
  private URL url;
  private BufferedReader dis;

  /** Default Constructor
    * @param showID The ID oh the show of which to check for upcomming episode
    */
  public NextEpisode( int showID ){
    this.showID = showID;
  }

  /** Parses TV Rages Quickinfo to get all the needed information
    *
    * @return NextEpisodeEntity 
    */ 
  public NextEpisodeEntity getNext(){
    String line;
    Pattern p;
    Matcher m;     
    HashMap< String, String> prop = new HashMap< >();

    try{
      url = new URL( "http://services.tvrage.com/tools/quickinfo.php?sid=" + showID );
      is = url.openStream();
      dis = new BufferedReader( new InputStreamReader( is ) );

      while( ( line = dis.readLine() ) != null ){
	String[] split = line.split( "@" );
	if( split.length > 1 ){
	  prop.put( split[0], split[1] ) ;
        }
      }

      is.close();
    }catch( Exception e ){
      // This catches multiple errors, but it does not matter - if we catch something TV Rage was down.
      System.out.println( "Could not connect to TV Rage." );
      System.exit(-1);
    }

    if( prop.containsKey( "Show Name" ) ){
      showName = prop.get( "Show Name" );
    }
    if( prop.containsKey( "Next Episode" ) ){
      next = prop.get( "Next Episode" );
    }
    else return null;
    if( prop.containsKey( "RFC3339" ) ){
      dayTime = prop.get( "RFC3339" );
    }

    return new NextEpisodeEntity( showID, next, showName, dayTime );
  }

  /** Use this for testing - prints next Episode Info about a given show
    *
    * @param args Command Line Parameter show to look up
    */
  public static void main( String[] args ){
    NextEpisode next = new NextEpisode(  3628  );
    NextEpisodeEntity nee = next.getNext();

    if( nee == null ) System.out.println( "There is no such show or no next Episode." );
    else{
      System.out.println( "ID: " + nee.getShowID() );
      System.out.println( "Show Name: " + nee.getShowTitle() );
      System.out.println( "Episode Name: " + nee.getEpisodeTitle() );
      System.out.println( "Season: " + nee.getSeason() );
      System.out.println( "Episode: " + nee.getEpisode() );
      System.out.println( "Date Time: " + nee.getDateTime().toString() );
    }
  }

}