import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Chat;
import java.util.*;
import java.io.*;
import com.almworks.sqlite4java.SQLiteQueue;

/** This Class processes all the Jabber Messages send to the Bot 
  * All processes are queued and processed in a thread
  */
public class ProcessInput extends Thread{
  DataBase dbase = new DataBase();
  boolean process = true;
  ArrayList<Job> jobs = new ArrayList<Job>();
  MultiUserChat muc;
  boolean running = false;

  /** Default Constructor
    * @param muc The MUC we are in
    */
  public ProcessInput( MultiUserChat muc ){
    dbase.connect();
    this.muc = muc;
  } 

  /** Process a new MUC message
    * @param message Message received from the MUC
    * @param from Who sent the message in the MUC
    */
  public void newMucMessage( String message, String from ){
    jobs.add( new Job( message, from, "MUC" ) );
    if( !running ) run();
  }

  /** Process a new private message
    * @param message Message received from the MUC
    * @param from Who sent the message in the MUC
    * @param chat The chat to use to answer the message
    */
  public void newPrivateMessage( String message, String from, Chat chat ){
    jobs.add( new Job( message, from, "PM", chat ) );
    if( !running ) run();
  }

  /** This is our Message queue
    */
  @Override
  public void run(){
    running = true;

    while( !jobs.isEmpty() ){
      if( jobs.get(0).getResource().equals( "PM" ) ) processPrivateMessage( jobs.get(0) );
      else if( jobs.get(0).getResource().equals( "MUC" ) ) processMucMessage( jobs.get(0) );
      jobs.remove( 0 );
    }

    running = false;
  }

  /** Terminate this class gracefully
    */
  public void close(){
    running = false;
    dbase.disconnect();
  }

  /** Returns the approved shows to the MUC
    * @return String 
    */
  private String showsList(){
    String toReturn = "All shows we watch:\n";
    ArrayList<TVEntity> approved = dbase.getApproved();
    if( approved != null )
      for( int i = 0; i < approved.size(); i++ )
	toReturn += approved.get( i ).getShowname() + "\n";
    return toReturn;
  }

  /** Returns the never shows
    * @return String
    */
  private String showsNever(){
    String toReturn = "Shows we will never watch:\n";
    ArrayList<TVEntity> approved = dbase.getNever();
    if( approved != null )
      for( int i = 0; i < approved.size(); i++ )
	toReturn += approved.get( i ).getShowname() + "\n";
    return toReturn;
  }

  /** Returns the requested shows
    * @return String
    */
  private String showsRequested(){
    String toReturn = "This shows are requested:\n";
    ArrayList<TVEntity> approved = dbase.getRequested();
    if( approved != null )
      for( int i = 0; i < approved.size(); i++ )
	toReturn += approved.get( i ).getShowname() + "\n";
    return toReturn;
  }

  /** Returns the name of the requested show and adds it to the database if it exists
    * @param showID ID of show to request
    * @return String
    */
  private String requestShow( int showID ){
    return dbase.requestShow( showID, "showname","airtime","airday","timezone" );
  }

  /** Returns the name of the approved show and approves it in the database if it exists
    * @param showID ID of show to request
    * @return String
    */
  private String approveShow( int showID ){
    if( dbase.approveShow( showID ) )
      return "Show approved";
    return "Show not in database";
  }

  /** Sends a message to the MUC
    * @param message Message to send to the MUC
    */
  private void sendMucMessage( String message ){
    try{
      muc.sendMessage( message );
    } catch( XMPPException e ) { System.out.println( "Could not send message to MUC. (" + message + ")" ); }
  }


  /** Send a private Message
    * @param message Message to send
    * @param chat Chat to use to send the message
    */
  private void sendPrivateMessage( String message, Chat chat ){
    try{
      chat.sendMessage( message );
    }catch(XMPPException e){ System.out.println("Could not send private message."); }
  }

  /** Process a private Job
    * @param job Job to process
    */
  private void processPrivateMessage( Job job ){
    try{
      job.getChat().sendMessage( "acc" );
    }catch( Exception e ){ System.out.println( "Could not send message" ); }
  }

  /** Process a MUC Job
    * @param job Job to process
    */
  private int processMucMessage( Job job ){
    final Job j = job;
    String[] commands = j.getMessage().split( " " );
    try{
      if( j.getMessage().equals( "ping" ) ) muc.sendMessage( "pong" );
      else if( j.getMessage().equals( "shows" ) ) muc.sendMessage( showsList() );
      else if( j.getMessage().equals( "shows never" ) ) muc.sendMessage( showsNever() );
      else if( j.getMessage().equals( "shows requested" ) ) muc.sendMessage( showsRequested() );
      else if( j.getMessage().equals( "request" ) ) muc.sendMessage( requestShow( 123 ) );
      else if( commands.length == 2 ){
	if( commands[0].equals( "approve" ) ){
	  try{
	    muc.sendMessage( approveShow( Integer.parseInt( commands[1] ) ) );
	  }catch( Exception e ){}
	}
      }
    }catch( Exception e ){ 
        System.out.println("Could not send Muc message");
        e.printStackTrace();
    }
    return 1;
  }

  private Chat createChat( String name ){ return null; }
}