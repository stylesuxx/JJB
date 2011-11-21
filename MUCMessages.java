import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smack.XMPPException;
/** This class is responsible for handling the messages from the muc. 
  * This happens threaded.
  *
  *
  *
  *
  *
  *
  */

class MUCMessages extends Thread{
  String message, nick;
  MultiUserChat muc;

  MUCMessages( String message, String nick, MultiUserChat muc ){
    this.message = message;
    this.nick = nick;
    this.muc = muc;
  }

  void sendMessage( String msg ){
    try{
      muc.sendMessage( msg );
    } catch( XMPPException e ) { System.out.println( "Could not send message to MUC. (" + message + ")" ); }
  }

  public void run(){
    // Use this to check if Bot is alive
    if( message.equals( "ping" ) ) sendMessage( "pong" );
  }
}