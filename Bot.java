import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Chat;

public class Bot{
  XMPPConnection con;
  MultiUserChat muc;
  ProcessInput pc;
  ChatManager chatmanager;
  String user, nick, pass, server, res, auth, room;

  public Bot(String user, String pass, String server, String res, String auth, String nick){
    this.user = user;
    this.pass = pass;
    this.server = server;
    this.res = res;
    this.auth = auth;
    this.nick = nick;
  }

  public void login(){
    try{
      ConnectionConfiguration config = new ConnectionConfiguration( server, 5222, server );
      con = new XMPPConnection( config );
      con.connect();
      SASLAuthentication.supportSASLMechanism( auth, 0 );
      con.login( user, pass, res );
    }catch( XMPPException e ){ 
      System.out.println( "Connection failed: " + e.getMessage() );
      System.exit(-1);
     }
    chatmanager = con.getChatManager();
    con.getChatManager().addChatListener(new ChatManagerListener(){
      public void chatCreated(final Chat chat, final boolean createdLocally){
	chat.addMessageListener( new PrivateListener( pc ) );
      }
    });
    System.out.println( "Connected to Jabber Server" );
  }

  public void joinRoom( String room ){
    muc = new MultiUserChat( con, room + "@conference." + server );
    DiscussionHistory hist = new DiscussionHistory();
    hist.setMaxChars(0);
    try{
      muc.join( nick, pass, hist, 1000 );
    }catch( XMPPException e ){
      System.out.println( "Could not join the MUC: " + e.getMessage() );
      System.exit(-1);
     }    
    pc = new ProcessInput( muc );
    //pc.start();
    muc.addMessageListener( new MucListener( pc ,  room + "@conference." + server + "/" + nick ) );
    System.out.println( "Joined room" );
  }
  
  public void disconnect(){
    con.disconnect();
    //dBase.disconnect();
  }
}