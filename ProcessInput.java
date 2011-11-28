import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import java.util.*;
import java.io.*;
import com.almworks.sqlite4java.SQLiteQueue;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/** This Class processes all the Jabber Messages send to the Bot 
  * All processes are queued and processed in a thread
  */
public class ProcessInput extends Thread{
  private DataBase dbase;
  private boolean process = true;
  private ArrayList<Job> jobs = new ArrayList<Job>();
  MultiUserChat muc;
  private boolean running = false;
  String admin;
  ArrayList<String> links = new ArrayList<String>();

  /** Default Constructor
    * @param muc The MUC we are in
    */
  public ProcessInput( MultiUserChat muc, String admin ){
    this.muc = muc;
    this.admin=admin;
    dbase = new DataBase( admin );
    dbase.connect();
  } 

  /** Add a new Job to the Jobqueue
    * @param message The Jobs message
    * @param user Thes User who sent the Job
    * @param resource Where the Job came from
    */  
  public void newMessage( String message, UserEntity user, String resource ){
    System.out.println( user.getJid() + " wrote: " + message + " in: " + resource );
    jobs.add( new Job( message, user, resource ) );
    if( !running ) run();
  }

  /** This is our Jobqueue
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

  /** Terminate this class gracefully
    */
  public void close(){
    running = false;
    stop();
    dbase.disconnect();
  }

  private void updateNext( int showID ){
    NextEpisode ne = new NextEpisode( showID );
    NextEpisodeEntity nee = ne.getNext();
    dbase.updateShow( showID, nee );
  }

  /** Returns the approved shows to the MUC
    * 
    * @return String 
    */
  private String showsList(){
    HashMap<String,Date> map = new HashMap<String,Date>();
    DateComparator bvc =  new DateComparator(map);
    TreeMap<String,Date> sorted_map = new TreeMap(bvc);

    Date today = new Date();

System.out.println(today.toString());

    String toReturn;

      toReturn = "TV Time:";
      ArrayList<TVEntity> approved = dbase.getShows( "approved" );
      if( approved != null )
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

      toReturn += "\n" + key + " ( "+ time +" )";
    }

    return toReturn;
  }

  /** Returns the never shows
    * @return String
    */
  private String showsNever(){
    String toReturn = "Shows we will never watch:";
    ArrayList<TVEntity> approved = dbase.getShows( "never" );
    if( approved != null )
      for( int i = 0; i < approved.size(); i++ )
	toReturn += "\n" + approved.get( i ).getShowname();
    return toReturn;
  }

  /** Returns the requested shows
    * @return String
    */
  private String showsRequested(){
    String toReturn = "This shows are requested:";
    ArrayList<TVEntity> approved = dbase.getShows( "requested" );
    if( approved != null )
      for( int i = 0; i < approved.size(); i++ )
	toReturn += "\n" + approved.get( i ).toString();
    return toReturn;
  }

  /** Returns the name of the requested show and adds it to the database if it exists
    * @param showID ID of show to request
    * @return String
    */
  private String requestShow( int showID ){
    TVRageLookup tv = new TVRageLookup( showID );
    NextEpisode ne = new NextEpisode( showID );
    NextEpisodeEntity nee = ne.getNext();

    // If has next episode, set additional Infos
    if( nee != null ){
      return "'" + dbase.requestShow( tv, nee ) + "' has been added to the request list. (upcomming episode)";
    }
    return "'" + dbase.requestShow( tv ) + "' has been added to the request list.";
  }

  /** Returns the name of the approved show and approves it in the database if it exists
    * @param showID ID of show to request
    * @return String
    */
  // does not check if show approved, should return a string,...
  private String approveShow( int showID ){
    dbase.setShowStatus( showID, "approved" );
    return null;
    //return "Show approved";
    //return "Show not in database";
  }

  private String getRegisteredUsers(){
    String toReturn = "Registered Users:";
    ArrayList<String> tmp = dbase.getUsers( "registered" ); 
    for(int i=0; i<tmp.size(); i++ ){
      toReturn += "\n" + tmp.get( i );
    }	
    return toReturn;
  }

  private String getApprovedUsers(){
    String toReturn = "Approved Users:";
    ArrayList<String> tmp = dbase.getUsers( "approved" ); 
    for(int i=0; i<tmp.size(); i++ ){
      toReturn += "\n" + tmp.get( i );
    }	
    return toReturn;
  }

  private String getAdminUsers(){
    String toReturn = "Admin Users:";
    ArrayList<String> tmp = dbase.getUsers( "admin" ); 
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
    try{
      muc.sendMessage( message );
    } catch( XMPPException e ) { System.out.println( "Could not send message to MUC. (" + message + ")" ); }
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
    * @param job Job to process
    */
  private void processPrivateMessage( Job job ){
    try{
      job.getUser().getChat().sendMessage( "acc" );
    }catch( Exception e ){ System.out.println( "Could not send message" ); }
  }

  /** Register a new User to the database
    * @param user User to register
    * @return String
    * TESTED
    */
  private String registerUser( UserEntity user ){
    if( dbase.registerUser( user.getJid() ) ) return "You have successfully registered, staff will approve you soon.";
    return "You already are registered.";
  }

  // does not get exception never getting in else
  private String promoteAdmin( String user ){
    if( dbase.setUserStatus( user, "admin" ) ) return "You promoted " + user + " to Admin!";
    else return "Could not promote " + user + "!";
  }

  private String approveUser( String user ){
    if( dbase.setUserStatus( user, "approved" ) ) return "You approved " + user + "!";
    else return "Could not promote " + user + "!";
  }

  private String deleteUser( String user ){
    if( dbase.unregisterUser( user ) ) return "You removed " + user + "!";
    else return "Could not remove " + user + "!";
  }

  private void checkLink( String message, UserEntity user ){
    String regex = ".*(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    Pattern pattern = Pattern.compile( regex );
    Matcher matcher = pattern.matcher( message );
    if( matcher.find() ) links.add( message + " ("+ user.getJid() +")" );
  }

  private String getLinks(){
    String toReturn = "Links";
    int max;
    if( links.size() > 50 ) max = 50;
    else max = links.size();
    for( int i = (links.size()-1); i >= 0; i-- )
      toReturn += "\n" + links.get(i);
    return toReturn;
  }

  /** Process a MUC Job
    * @param job Job to process
    */
  private int processMessage( Job job ){
    final Job j = job;
    String[] command = j.getMessage().split( " " );

    try{
    switch( command.length ){
      case 1: {
		if( command[0].equals( "ping" ) && j.getResource().equals( "muc" ) && dbase.isApprovedUser( j.getUser().getJid() ) ) muc.sendMessage( "pong" );
		else if( command[0].equals( "help" ) ) sendPrivateMessage( help( j.getUser() ), j.getUser() );
		else if( command[0].equals( "links" ) && dbase.isApprovedUser( j.getUser().getJid() ) ) sendPrivateMessage( getLinks(), j.getUser() );
		else if( command[0].equals( "shows" ) && dbase.isApprovedUser( j.getUser().getJid() ) ) muc.sendMessage( showsList() );
		else if( command[0].equals( "register" ) ) sendPrivateMessage( registerUser( j.getUser() ), j.getUser() );
	      } break;
      case 2: { 
		if ( command[0].equals( "shows" ) ){
		  switch( command[1] ){
		    case "never": muc.sendMessage( showsNever() ); break;
		    case "req": {
				  if( dbase.isAdminUser( j.getUser().getJid() ) )
				    sendPrivateMessage( showsRequested(), j.getUser() );
				} break;
		    default: break;
		  }
		}

		if( command[0].equals( "approve" ) && dbase.isAdminUser( j.getUser().getJid() ) ){
		  sendPrivateMessage( approveUser( command[1] ), j.getUser() );
		}

		if( command[0].equals( "admin" ) && dbase.isAdminUser( j.getUser().getJid() ) ){
		  sendPrivateMessage( promoteAdmin( command[1] ), j.getUser() );
		}
	  
		if( command[0].equals( "delete" ) && dbase.isAdminUser( j.getUser().getJid() ) ){
		  sendPrivateMessage( deleteUser( command[1] ), j.getUser() );
		}
	      }

	      if( command[0].equals( "request" ) && dbase.isApprovedUser( j.getUser().getJid() ) ){
		muc.sendMessage( requestShow( Integer.parseInt(command[1]) ) );
	      }

	      if( command[0].equals( "approve" ) && dbase.isAdminUser( j.getUser().getJid() ) ){
		dbase.setShowStatus( Integer.parseInt(command[1]), "approved" );
	      }

	      if( command[0].equals( "users" ) && dbase.isAdminUser( j.getUser().getJid() ) ){
		switch( command[1] ){
		  case "reg": sendPrivateMessage( getRegisteredUsers(), j.getUser() ); break;
		  case "app": sendPrivateMessage( getApprovedUsers(), j.getUser() ); break;
		  case "admin": sendPrivateMessage( getAdminUsers(), j.getUser() ); break;
		  default: break;
		}
	      }

      default: checkLink( j.getMessage(), j.getUser() ); break;
    }
  }catch( Exception e ){ 
        System.out.println("Could not send Muc message");
        e.printStackTrace();
    }
    return 1;
  }

  // Print different help message depending on user status
  private String help( UserEntity user ){
    String toReturn = "Help";
    if( dbase.isAdminUser( user.getJid() ) ){
      toReturn = "Admin Help";
      toReturn += "\nshows req\t show requested shows" +
		  "\nusers reg\t\t show registered Users waiting for approval" +
		  "\nusers app\t show approved users" +
		  "\nusers admin\t show admin users" +
		  "\napprove Jid/ID\t approve a user or a show";
      toReturn += "\n-------------------------------------------------------------";
    }
    if( dbase.isApprovedUser( user.getJid() ) || dbase.isRegisteredUser( user.getJid() ) ){
      toReturn += "\nping \t Check if Bot is alive." +
		  "\nshows \t Prints all the shows we are watching." +
		  "\nrequest # Request a TV show with TV-Rage ID." +
		  "\nlinks \t Print the last 50 posted links";
    }
    if( dbase.isRegisteredUser( user.getJid() ) ){
      toReturn += "\n\nYou have to be approved for this commands to work.";
    }
    if( !dbase.isApprovedUser( user.getJid() ) && !dbase.isRegisteredUser( user.getJid() ) && !dbase.isAdminUser( user.getJid() ) ) 
      toReturn += "If you want to be able to use the Bot type \"register\"";
    return toReturn;
  }

  private Chat createChat( String name ){ return null; }
}