

import JJB.SQLite.DataBase;
import JJB.Processes.InputProcessor;
import JJB.Listeners.PrivateListener;
import JJB.Listeners.MucListener;
import com.almworks.sqlite4java.SQLiteQueue;
import java.io.File;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Chat;

/** <h1>Bot</h1>
 * <p>Logs the bot in, joins a room and sets the message listeners</p>
 * 
 * @author stylesuxx
 * @version 0.1
 */
public class Bot extends Thread{
  private XMPPConnection con = null;
  private MultiUserChat muc = null;
  private String user, pass, server, res, auth, admin;
  private int port;
  private InputProcessor ip;
  DataBase dbase;
  SQLiteQueue queue;
  
  /** Default Construcotor
   * 
   * @param jid The Bot's JID
   * @param pass The Bot's Password
   * @param res The Bot's Resource
   * @param auth The Login authenticatioin method
   * @param admin The Jid of the Bot's admin
   * @param port The port to connect to 
   * @param ip The Input Processor
   */
  public Bot( String jid, String pass, String res, String auth, String admin, int port, File db, boolean log ){
    this.user = jid.split( "@" )[0];
    this.pass = pass;
    this.server = jid.split( "@" )[1];
    this.res = res;
    this.auth = auth;
    this.admin = admin;
    this.port = port;
    // Connect to Database
    dbase = new DataBase( admin, db, log );
    dbase.connect();
    // Create and Start Queu
    queue = new SQLiteQueue( db );
    queue.start();
    // Start Input Processor
    ip = new InputProcessor( queue, admin );
  }

  /** Login to the Jabber Server or die trying
    */ 
  public void login(){
    try{
      ConnectionConfiguration config = new ConnectionConfiguration( server, port, server );
      con = new XMPPConnection( config );
      con.connect();
      SASLAuthentication.supportSASLMechanism( auth, 0 );
      con.login( user, pass, res );
    }catch( XMPPException e ){ 
      System.out.println( "Connection to Jabber Server failed: " + e.getMessage() );
      ip.quit();
     }
    //chatmanager = con.getChatManager();
    con.getChatManager().addChatListener(new ChatManagerListener(){
      public void chatCreated(final Chat chat, final boolean createdLocally){
	chat.addMessageListener( new PrivateListener( ip ) );
      }
    });
    System.out.println( "Logged in as: " + user + "@" + server );
  }

  /** Join a room or die trying
    *
    * @param room Room to join
    * @param nick Nick to use in room
    */ 
  public void joinRoom( String room, String nick ){
    muc = new MultiUserChat( con, room );
    DiscussionHistory hist = new DiscussionHistory();
    hist.setMaxChars(0);
    try{
      muc.join( nick, pass, hist, 1000 );
    }catch( XMPPException e ){
      System.out.println( "Could not join the room: " + e.getMessage() );
      System.exit(-1);
     }    
    muc.addMessageListener( new MucListener( ip ,  room + "/" + nick, muc ) );
    System.out.println( "Joined room: " + room + " as " + nick );
  }

  public void disconnect(){
    // stop queue
    queue.stop( true );
    // disconnect DB
    dbase.close();
    // disconnect Bot
    con.disconnect();
  }
  
  public boolean isStopped(){
    if(ip.isStopped()){
     disconnect();
     return true;
    }
    return false;
  }

}