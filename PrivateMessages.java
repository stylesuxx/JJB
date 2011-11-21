import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Chat;


public class PrivateMessages extends Thread{
  private String message, nick, admin;
  private Chat chat;

  PrivateMessages( String message, String nick, String admin, Chat chat ){
    this.message = message;
    this.admin = admin;
    this.nick = nick.split("/")[0];
    this.chat = chat;

    System.out.println( this.nick );
  }

  private void sendMessage( String msg ){
    try{
      chat.sendMessage( msg );
    } catch( XMPPException e ) { System.out.println( "Could not send message to MUC." ); }
  }

  private void adminCmd(){
    if( !nick.equals( admin ) ) sendMessage( "I am not allowed to talk to strangers." );
    else sendMessage( "Yes Sir." );
  }

  public void run(){
    // Use this to check if Bot is alive
    if( message.equals( "ping" ) ) sendMessage( "pong" );
    if( message.equals( "quit" )) adminCmd();
    else sendMessage( "rec." );
  }
}