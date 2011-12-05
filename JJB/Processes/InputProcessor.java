package JJB.Processes;

import JJB.SQLite.UserQueries;
import JJB.SQLite.TvQueries;
import JJB.Tv.TVEntity;
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
  private ArrayList<Job> jobs = new ArrayList<>();
  private MultiUserChat muc;
  private boolean running = false;
  private ArrayList<String> links = new ArrayList<>();
  private UserQueries userQ;
  private TvQueries tvQ;
  private boolean stopped = false;
  private final UserProcesses userP;
  private final ShowsProcesses tvP;
  private Help h = new Help();

  /** Default Constructor
    * 
    * @param muc The MUC we are in
    */
  public InputProcessor( SQLiteQueue queue, String admin ){
    userQ = new UserQueries( queue, true, admin );
    tvQ = new TvQueries( queue, true );
    userP = new UserProcesses( userQ );
    tvP = new ShowsProcesses( tvQ );
  }

  /** Set the Muc for this Input Processor
   * @oaram muc The Muc to process.
   */
  public void setMuc( MultiUserChat muc ){
    this.muc = muc;
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

  /** Check if the input processor is still running
   * 
   * @return boolean
   */
  public boolean isStopped(){ return stopped; }

  /** Quit the Input Processor - also the bot is quit then
   */
  public void quit(){
    stopped = true;
  }
  
  /** Add a new Job to the Jobqueue
   * 
   * @param message The Jobs message
   * @param user The User who sent the Job
   * @param resource Where the Job came from
   */  
  public void newMessage( String message, User user, String resource ){
    jobs.add( new Job( message, user, resource ) );
    if( !running ) run();
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
  private void sendPrivateMessage( String message, User user ){
    Chat chat = user.getChat();
    try{
      chat.sendMessage( message );
    }catch(XMPPException e){ System.out.println("Could not send private message."); }
  }
 
  /** Check if Message contains a link and save in ArrayList
    *
    * @param message Message the user sent
    * @param user User who sent the message
    */
  private void checkLink( String message, User user ){
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
        if( tvP.requestShow( tmp , "user" ).equals( "") ){
          userQ.addUserShow( user.getJid(), showIDs );
          counter ++;
        }
      }
    }catch( Exception e ){}
    
    }
    return counter + "/" + (showIDs.length-2) + " shows added.";
  }
  
  private String myDel( User user, String[] showIDs ){
    int counter = 0;
    for(int i = 2; i < showIDs.length; i++){
      try{
        tvQ.deleteUserShow( user.getJid(), Integer.parseInt( showIDs[i] ));
        counter++;
      }catch(Exception e){}
    }
    return "Deleted " + counter + "/" + (showIDs.length-2) + "from your list.";
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
  
  // This should be threaded - each message an own job
  private boolean processMessage( Job job ){
    String jid = job.getUser().getJid();
    User user = job.getUser();
    String status = userQ.getStatus( jid );
    String[] command = job.getMessage().split(" ");
    
    // we first switch for user status and let it fall through than we switch by first command, second and so on
    // As soon as one matches we return, since it is not possible we match multiple cases.
    switch( status ){
      // If user is admin
      case "admin":{
        System.out.println("Checking admin commands");
        switch(command.length){
          
          case 1:{
            // If admin quit bot
            switch( command[0] ){
              case "quit": quit(); return true;
              default: break;
            }
          }break;
            
          case 2:{
            switch(command[0]){
              
              // if admin promotes other user to admin
              case "admin": sendPrivateMessage( userP.adminUser( command[1] ), user ); return true;
              
              // If admin updates show database
              case "update":{
                switch(command[1]){
                  case "shows": tvP.updateAll(); break;
                  default: break;
                }
              } return true;
                
              // If admin deletes show or Jid
              case "delete": {
                try{
                    sendPrivateMessage( tvP.deleteShow( Integer.parseInt(command[1] ) ), user );
                }catch( Exception e ){
                    sendPrivateMessage( userP.deleteUser( command[1] ), user );
                }
              } return true;
                
              // If admin nevers a show
              case "never": sendMucMessage( tvP.neverShow( Integer.parseInt(command[1] ) ) ); return true;
                
              // If admin approves one or all shows
              case "approve":{
                if( command[1].equals("all")) sendMucMessage( tvP.approveAllShows() );
                else{
                  try{
                    sendMucMessage( tvP.approveShow( Integer.parseInt( command[1] ) ) );
                  }catch( Exception e ){ sendPrivateMessage( userP.approveUser( command[1] ), user ); }
                }
              } return true;
                
              // if admin wants to view requested shows
              case "shows":{
                switch(command[1]){
                  case "requested": sendPrivateMessage( tvP.showsRequested(), user ); return true;
                    
                  default: break;
                }
              } break;
                
              // if admin wants to show users in group
              case "users": sendPrivateMessage( userP.getUsers( command[1] ), user ); return true;
                
              default: break;
            }
          }break;
          default:{
            switch(command[0]){
              //case "approve": sendMucMessage( tvP.( command ) ); return true;
            }
          
          }break;
        }
      }
        
      // If user is approved
      case "approved": {
        System.out.println("Checking approved commands");
        switch(command.length){
          case 1:{
            switch(command[0]){
              case "shows": sendMucMessage( tvP.showsList() ); return true;
              case "ping": sendMucMessage( "pong" ); return true;
              case "links": sendPrivateMessage( getLinks( 50 ), user ); return true;
              default: break;
            }
          }break;
          case 2:{
            switch(command[0]){
              case "my":{      
                switch( command[1] ){
                  case "shows": sendPrivateMessage( myShows( user ), user ); return true;
                  default: return true;
                }
              }
              case "shows":{
                switch(command[1]){
                  case "never": sendPrivateMessage( tvP.showsNever(), user ); return true;
                  case "all": sendPrivateMessage( tvP.showsAll(), user ); return true;
                  default: return true;
                }
              }
                
              //request one show
              case"request": sendMucMessage( tvP.requestShow( command, "requested" ) ); return true;
              default: return true;
            }
          }
            
          // if approved and length > 2
          default:{
            switch(command[0]){
              // if approved and requested more than one show
              case "request": sendMucMessage( tvP.requestShow( command, "requested" ) ); return true;
              case "my":{      
                switch( command[1] ){
                  case "add": sendPrivateMessage( myAdd( user, command ), user ); return true;
                  case "del": sendPrivateMessage( myDel( user, command ), user ); return true;
                  default: return true;
                }
              }
              default: break;
            }
          }break;
        }
      }
        
      // If user is registered
      case "registered": { System.out.println("Checking registered commands"); }
        
      // If user is not even registered - this is checked every time
      default:{
        System.out.println("Checking guest commands"); 
        switch(command.length){
          case 1:{
            switch(command[0]){
              case "help": sendPrivateMessage( h.getHelp( userQ.getStatus(user.getJid()) ), user ); return true;
              case "register": sendPrivateMessage( userP.registerUser( jid ), user ); return true;
              default: break;
            }
          } break;
          default: break;
        }
      }
    }
    checkLink(job.getMessage(), job.getUser());
    return false;
  }

}