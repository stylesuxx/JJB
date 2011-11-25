import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Chat;
import java.util.*;
import java.io.*;
import com.almworks.sqlite4java.SQLiteQueue;

public class ProcessInput extends Thread{
  DataBase dbase = new DataBase();
  boolean process = true;
  ArrayList<Job> jobs = new ArrayList<Job>();
  MultiUserChat muc;
  boolean running = false;

  public ProcessInput( MultiUserChat muc ){
    dbase.connect();
    this.muc = muc;
  } 

  public void newMucMessage( String message, String from ){
    jobs.add( new Job( message, from, "MUC" ) );
    if( !running ) run();
  }

  public void newPrivateMessage( String message, String from, Chat chat ){
    jobs.add( new Job( message, from, "PM", chat ) );
    if( !running ) run();
  }

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


  public void closexxx(){
    process = false;
    //dbase.disconnect();
  }

  public String showsList(){
    String toReturn = "All shows we watch:\n";
    ArrayList<TVEntity> approved = dbase.getApproved();
    if( approved != null )
      for( int i = 0; i < approved.size(); i++ )
	toReturn += approved.get( i ).getShowname() + "\n";
    return toReturn;
  }

  public String showsNever(){
    String toReturn = "Shows we will never watch:\n";
    ArrayList<TVEntity> approved = dbase.getNever();
    if( approved != null )
      for( int i = 0; i < approved.size(); i++ )
	toReturn += approved.get( i ).getShowname() + "\n";
    return toReturn;
  }

  public String showsRequested(){
    String toReturn = "This shows are requested:\n";
    ArrayList<TVEntity> approved = dbase.getRequested();
    if( approved != null )
      for( int i = 0; i < approved.size(); i++ )
	toReturn += approved.get( i ).getShowname() + "\n";
    return toReturn;
  }

  public String requestShow( int showID ){
    return dbase.requestShow( showID, "showname","airtime","airday","timezone" );
  }

  public String approveShow( int showID ){
    if( dbase.approveShow( showID ) )
      return "Show approved";
    return "Show not in database";
  }

  public void sendMucMessage( String message ){
    try{
      muc.sendMessage( message );
    } catch( XMPPException e ) { System.out.println( "Could not send message to MUC. (" + message + ")" ); }
  }

  public void sendPrivateMessage( String message, Chat chat ){
    try{
      chat.sendMessage( message );
    }catch(XMPPException e){ System.out.println("Could not send private message."); }
  }

  private void processPrivateMessage( Job job ){
    try{
      job.getChat().sendMessage( "acc" );
    }catch( Exception e ){ System.out.println( "Could not send message" ); }
  }

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