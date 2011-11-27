import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Chat;

/** <h1>Bot</h1>
  * <p>Bot Log in an room join, the message Listeners are added here</p>
  * 
  * @author stylesuxx
  * @version 0.1
  */
public class Bot{
  private XMPPConnection con = null;
  private MultiUserChat muc = null;
  private ProcessInput pc = null;
  private ChatManager chatmanager = null;
  private String jid, user, nick, pass, server, res, auth, room, admin;
  private int port;
  
  /** Default Construcotor
    * 
    * @param jid The Bot's JID
    * @param pass The Bot's Password
    * @param res The Bot's Resource
    * @param auth The Login authenticatioin method
    * @param admin The Jid of the Bot's admin
    * @param port The port to connect to 
    */
  public Bot(String jid, String pass, String res, String auth, String admin, int port ){
    this.user = jid.split( "@" )[0];
    this.pass = pass;
    this.server = jid.split( "@" )[1];
    this.res = res;
    this.auth = auth;
    this.admin = admin;
    this.port = port;
  }

  /** Login to the Jabber Server or die trying
    */ 
  public void login(){
    try{
      ConnectionConfiguration config = new ConnectionConfiguration( server, 5222, server );
      con = new XMPPConnection( config );
      con.connect();
      SASLAuthentication.supportSASLMechanism( auth, 0 );
      con.login( user, pass, res );
    }catch( XMPPException e ){ 
      System.out.println( "Connection to Jabber Server failed: " + e.getMessage() );
      System.exit(-1);
     }
    chatmanager = con.getChatManager();
    con.getChatManager().addChatListener(new ChatManagerListener(){
      public void chatCreated(final Chat chat, final boolean createdLocally){
	chat.addMessageListener( new PrivateListener( pc ) );
      }
    });
    System.out.println( "Logged in as: " + user + "@" + server );
  }

  /** Join a room
    *
    * @param room Room to join
    * @param nick Nick to use in room
    */ 
  public void joinRoom( String room, String nick ){
    this.nick = nick;
    muc = new MultiUserChat( con, room );
    DiscussionHistory hist = new DiscussionHistory();
    hist.setMaxChars(0);
    try{
      muc.join( nick, pass, hist, 1000 );
    }catch( XMPPException e ){
      System.out.println( "Could not join the room: " + e.getMessage() );
      System.exit(-1);
     }    
    pc = new ProcessInput( muc, admin );
    muc.addMessageListener( new MucListener( pc ,  room + "/" + nick, muc ) );
    System.out.println( "Joined room: " + room + " as " + nick );
  }
  
  /** Disconnect from Server
    */
  public void disconnect(){
    con.disconnect();
  }
}