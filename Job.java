import org.jivesoftware.smack.Chat;

/** This class represents a Job
  */
public class Job{
  private String message, nick, resource;
  private Chat chat = null;

  /** Default MUC Constructor
    * @param message A Jobs message 
    * @param nick Who sent the message
    * @param resource Where is the message comming from
    */
  public Job( String message, String nick, String resource){
    this.message = message;
    this.nick = nick;
    this.resource = resource;
  }


  /** Default private Constructor
    * @param message A Jobs message 
    * @param nick Who sent the message
    * @param resource Where is the message comming from
    * @param chat Chat Object
    */
  public Job( String message, String nick, String resource, Chat chat){
    this.message = message;
    this.nick = nick;
    this.resource = resource;
    this.chat = chat;
  }

  /** Return the Jobs message
    * @return String
    */
  public String getMessage(){ return message; }

  /** Return the Jobs Nick
    * @return String
    */
  public String getNick(){ return nick; }

  /** Return the Jobs Resource
    * @return String
    */
  public String getResource(){ return resource; }

  /** Return the Jobs chat
    * @return chat
    */
  public Chat getChat(){ return chat; }
}