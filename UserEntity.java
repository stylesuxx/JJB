import org.jivesoftware.smack.Chat;

/** This class holds Jid and Chat for a User
  */
public class UserEntity{
  String jid = null;
  Chat chat = null;

  /** Default Constructor
    * @param jid The users Jid
    */
  public UserEntity( String jid ){
    this.jid = jid;
  }

  /** Constructor
    * @param jid The Users Jid
    * @param chat If from a private message we already have a Chat Object
    */
  public UserEntity( String jid, Chat chat ){
    this.jid = jid;
    this.chat = chat;
  }

  /** Returns the Users Jid
    * @return String
    */
  public String getJid(){ return jid; }

  /** Returns a Chat Object to reply to this user
    * @return Chat
    */
  public Chat getChat(){
    if( chat == null ) return null; //Create new Chatobject
    else return chat;
  }
}