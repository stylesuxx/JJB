package JJB;

import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Chat;
import java.util.*;
import com.almworks.sqlite4java.SQLiteQueue;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/** This Class processes all the Jabber Messages send to the Bot 
  * All processes are queued and processed FiFo
  * 
  * @author stylesuxx
  * @version 0.1
  */
public class InputProcessor extends Thread{
  private Date lastUpdate;
  private ArrayList<Job> jobs = new ArrayList<>();
  private MultiUserChat muc;
  private boolean running = false;
  private ArrayList<String> links = new ArrayList<>();
  private UserQueries userQ;
  private TvQueries tvQ;
  private boolean stopped = false;

  /** Default Constructor
    * 
    * @param muc The MUC we are in
    */
  public InputProcessor( SQLiteQueue queue ){
    userQ = new UserQueries( queue, true );
    tvQ = new TvQueries( queue, true );
    System.out.println( updateAll() );
  }

  public void setMuc( MultiUserChat muc ){
    this.muc = muc;
  }

  public boolean isStopped(){ return stopped; }

  public void quit(){
    stopped = true;
  }
  /** Add a new Job to the Jobqueue
    * 
    * @param message The Jobs message
    * @param user Thes User who sent the Job
    * @param resource Where the Job came from
    */  
  public void newMessage( String message, UserEntity user, String resource ){
    jobs.add( new Job( message, user, resource ) );
    if( !running ) run();
  }

  /** This is our Jobqueue
    * 
    * It is restarted as soon as it is stopped and a new Job is added
    */
  @Override
  public void run(){
    running = true;

    while( !jobs.isEmpty() ){
      processMessage( jobs.get(0) );
      jobs.remove( 0 );
    }

    running = false;
  }

  /** Update a shows next Episode
    *
    * @param showID ID of show to update
    */
  private void updateNext( int showID ){
    NextEpisode ne = new NextEpisode( showID );
    NextEpisodeEntity nee = ne.getNext();
    tvQ.updateShow( showID, nee );
  }

  /** Update all shows next episodes 
    */
  private String updateAll(){
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
  private String showsList(){
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

  /** Returns the never shows
    *
    * @return String
    */
  private String showsNever(){
    String toReturn = "Shows we will never watch:";
    ArrayList<TVEntity> approved = tvQ.getShows( "never" );
    if( approved.size() > 0 ){
      for( int i = 0; i < approved.size(); i++ )
	toReturn += "\n" + approved.get( i ).getShowname();
      return toReturn;
    }
    else return "There are no shows on the never list."; 
  }

  /** Returns the requested shows
    * 
    * @return String
    */
  private String showsRequested(){
    String toReturn = "This shows are requested:";
    ArrayList<TVEntity> approved = tvQ.getShows( "requested" );
    if( approved.size() > 0 ){
      for( int i = 0; i < approved.size(); i++ )
	toReturn += "\n" + approved.get( i ).toString();
      return toReturn;
    }
    else return "Currently there are no requested shows.";
  }

  private String deleteShow( int showID){
    tvQ.deleteShow( showID );
    return "Show deleted";
  }

  /** Returns all shows by name in the database
    * 
    * @return String
    */
  private String showsAll(){
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
  private String requestShow( String[] shows ){
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
      if( nee != null ) toReturn += "\n'" + tvQ.requestShow( tv, nee ) + "' has been added to the request list. (upcomming episode)";
      else if( tv != null ) toReturn += "\n'" + tvQ.requestShow( tv ) + "' has been added to the request list.";
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
  private String approveShow( int showID ){
    tvQ.setShowStatus( showID, "approved" );
    TVEntity tv = tvQ.getShowInfo( showID );
    if( tv != null ){
      tvQ.setShowStatus( showID, "approved" );
      return "'" + tv.getShowname() + "' has been approved.";
    }
    else return "This show is not requested, try requesting it before approving.";
  }
  
  private String approveAllShows(){
    String toReturn = "";
    ArrayList<TVEntity> approved = tvQ.getShows( "requested" );
    if( approved.size() > 0 ){
      for( int i = 0; i < approved.size(); i++ )
	toReturn += "\n" + approveShow( approved.get( i ).getShowid() );
      return toReturn;
    }
    else return "Try requesting some shows before approving them.";
  }

  /** Get users with certain status and return formatted string
   * 
   * @param status Status of users to lookup.
   * @return String  
   */
  private String getUsers( String status ){
    String toReturn = "";
    ArrayList<String> tmp = null;
      
    switch( status ){
      case "registered": toReturn = "Registered Users:"; break;
      case "approved": toReturn = "Approved Users:"; break;
      case "admin": toReturn = "Admin Users:"; break;
      default: toReturn = "No such Usergroup."; break;
    }
    tmp = userQ.getUsers( status ); 
    for(int i=0; i<tmp.size(); i++ ){
      toReturn += "\n" + tmp.get( i );
    }	
    return toReturn;
  }

  /** Sends a message to the MUC
    * 
    * @param message Message to send to the MUC
    */
  private void sendMucMessage( String message ){
    if( !message.equals("") ){
      try{
        muc.sendMessage( message );
      } catch( XMPPException e ) { System.out.println( "Could not send message to MUC. (" + message + ")" ); }
    }
  }

  /** Send a private Message
    * 
    * @param message Message to send
    * @param chat Chat to use to send the message
    */
  private void sendPrivateMessage( String message, UserEntity user ){
    Chat chat = user.getChat();
    try{
      chat.sendMessage( message );
    }catch(XMPPException e){ System.out.println("Could not send private message."); }
  }

  /** Process a private Job
    * 
    * @param job Job to process
    */
  private void processPrivateMessage( Job job ){
    try{
      job.getUser().getChat().sendMessage( "acc" );
    }catch( Exception e ){ System.out.println( "Could not send message" ); }
  }

  /** Register a new User to the database
    * 
    * @param user User to register
    * 
    * @return String
    */
  private String registerUser( UserEntity user ){
    if( userQ.registerUser( user.getJid() ) ) return "You have successfully registered, staff will approve you soon.";
    return "You already are registered.";
  }

  /** Promote user to admin
    *
    * @param user User to promote to admin
    * 
    * @return Sring
    */ 
  private String promoteAdmin( String user ){
    if( userQ.setUserStatus( user, "admin" ) ) return "You promoted " + user + " to Admin!";
    else return "Could not promote " + user + "!";
  }

  private String setUserGroup( String user, String group ){
  
  
  }
  
  /** Approve user
    *
    * @param user User to approve
    * 
    * @return Sring
    */
  private String approveUser( String user ){
    if( userQ.setUserStatus( user, "approved" ) ) return "You approved " + user + "!";
    else return "Could not promote " + user + "!";
  }

  /** Delete user
    *
    * @param user User to delete
    * 
    * @return Sring
    */
  private String deleteUser( String user ){
    if( userQ.deleteUser( user ) ) return "You removed " + user + "!";
    else return "Could not remove " + user + " - check JID!";
  }

  /** Check if Message contains a link and save in ArrayList
    *
    * @param message Message the user sent
    * @param user User who sent the message
    */
  private void checkLink( String message, UserEntity user ){
    String regex = ".*(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    Pattern pattern = Pattern.compile( regex );
    Matcher matcher = pattern.matcher( message );
    if( matcher.find() ) links.add( message + " ("+ user.getJid() +")" );
  }

  /** get the last n Links
    * Links are not saved to the database, just stored locally in an ArrayList which is valid as long as the Bot is up
    *
    * @param n Amount of Links to get
    * 
    * @return String
    */
  private String getLinks( int n ){
    if( links.size() > 0 ){
      String toReturn = "Links";
      int max;
      if( links.size() > n ) max = n;
      else max = links.size();
      for( int i = (links.size()-1); i >= 0; i-- )
	toReturn += "\n" + links.get(i);
      return toReturn;
    }
    return "There are no links to display.";
  }

  /** Process a MUC Job
    * 
    * @param job Job to process
    * 
    * @return boolean 
    */
  private boolean processMessage( Job job ){
    final Job j = job;
    String[] command = j.getMessage().split( " " );

    try{
    switch( command.length ){
      case 1: {
	if( command[0].equals( "ping" ) && j.getResource().equals( "muc" ) && userQ.isApprovedUser( j.getUser().getJid() ) ) muc.sendMessage( "pong" );
	else if( command[0].equals( "help" ) ) sendPrivateMessage( help( j.getUser() ), j.getUser() );
	else if( command[0].equals( "links" ) && userQ.isApprovedUser( j.getUser().getJid() ) ) sendPrivateMessage( getLinks( 50 ), j.getUser() );
	else if( command[0].equals( "shows" ) && userQ.isApprovedUser( j.getUser().getJid() ) ) muc.sendMessage( showsList() );
	else if( command[0].equals( "quit" ) && userQ.isAdminUser( j.getUser().getJid() ) ) quit();
        else if( command[0].equals( "update" ) && userQ.isAdminUser( j.getUser().getJid() ) ) muc.sendMessage( updateAll() );
	else if( command[0].equals( "register" ) ) sendPrivateMessage( registerUser( j.getUser() ), j.getUser() );
      } break;
      case 2: { 
	switch( command[0] ){
	  case "update": {
	    switch( command[1] ){
	      case "shows": if( userQ.isAdminUser( j.getUser().getJid() ) ) updateAll(); break;
	      default: break;
	    }
	  }break;

	  case "shows": {
	    switch( command[1] ){
	      case "never": sendPrivateMessage( showsNever(), j.getUser() ); break;
	      case "req": {
		if( userQ.isAdminUser( j.getUser().getJid() ) )
		  sendPrivateMessage( showsRequested(), j.getUser() );
	      } break;
	      case "all": sendPrivateMessage( showsAll(), j.getUser() ); break;
	      default: break;
	    }
	  }break;
      
	  case "approve":{
	    if( userQ.isAdminUser( j.getUser().getJid() ) ){
              if( command[1].equals("all")){
               muc.sendMessage( approveAllShows() );
              }
              else{
                try{
                  int id = Integer.parseInt( command[1] );
                  muc.sendMessage( approveShow( id ) );
                }catch( Exception e ){ sendPrivateMessage( approveUser( command[1] ), j.getUser() ); }
              }
            }
	  }break;

	  case "admin": {
	    if( userQ.isAdminUser( j.getUser().getJid() ) )
	      sendPrivateMessage( promoteAdmin( command[1] ), j.getUser() );
	  }break;

	  case "users": {
	    if( userQ.isAdminUser( j.getUser().getJid() ) ){
              sendPrivateMessage( getUsers( command[1] ), j.getUser() );
	    }
	  }break;

	  case "delete": {
	    if( userQ.isAdminUser( j.getUser().getJid() ) ){
                try{
                    sendPrivateMessage( deleteShow( Integer.parseInt(command[1] ) ), j.getUser() );
                }catch( Exception e ){
                    sendPrivateMessage( deleteUser( command[1] ), j.getUser() );
                }
		
	      }
	  }break;
	  default:{  }break;
	}
      }break;




      default: break;
    }
    if( command[0].equals( "request" ) && userQ.isApprovedUser( j.getUser().getJid() ) ){
      muc.sendMessage( requestShow( command ) );
    }
    if( userQ.isApprovedUser( j.getUser().getJid() ) ) checkLink( j.getMessage(), j.getUser() );
  }catch( Exception e ){ 
        System.out.println("Could not process message.");
        e.printStackTrace();
	return false;
    }
    return true;
  }

  /** Retruns the Helpfile
    *
    * @param user User to send the Helpfile to
    * 
    * @return String Helpfile
    */

  private String help( UserEntity user ){
    String toReturn = "Help";
    if( userQ.isAdminUser( user.getJid() ) ){
      toReturn = "Admin Help";
      toReturn += "\nshows req\t show requested shows" +
		  "\nusers <grp>\t\t show users in <grp>" +
		  "\nupdate \t\t force update of the show database -- TODO" +
		  "\napprove Jid/ID\t approve a user or a show" +
                  "\napprove all\t approve all shows";
      toReturn += "\n-------------------------------------------------------------";
    }
    if( userQ.isApprovedUser( user.getJid() ) || userQ.isRegisteredUser( user.getJid() ) ){
      toReturn += "\nping \t\t Check if Bot is alive." +
		  "\nshows \t\t Prints all the shows we are watching with a next Episodes." +
		  "\nshows all\t\t Prints all the shows we are watching." +
		  "\nshows never\t Prints all the shows we will never watch." +
		  "\nrequest #\t Request a TV show with TV-Rage ID, multiple ID's can be provided." +
 		  "\nmy add #\t Add a TV show to your personal list. -- TOO" +
		  "\nmy list \t\t Print your personal watchlist. -- TODO" +
		  "\nmy del #\t\t Delete a show from your personal list. -- TODO" +
		  "\nlinks \t\t Print the last 50 posted links.";
    }
    if( userQ.isRegisteredUser( user.getJid() ) ){
      toReturn += "\n\nYou have to be approved for this commands to work.";
    }
    if( !userQ.isApprovedUser( user.getJid() ) && !userQ.isRegisteredUser( user.getJid() ) && !userQ.isAdminUser( user.getJid() ) ) 
      toReturn += "If you want to be able to use the Bot type \"register\"";
    return toReturn;
  }

}