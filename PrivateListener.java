import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Chat;

/** This Class handles all incomming private messages
  *
  * @author stylesuxx
  * @version 0.1 
  */
class PrivateListener implements MessageListener{
  ProcessInput pi;

  /** Default Constructor
    * 
    * @param pc ProcessInput Queue to use
    */
  public PrivateListener( ProcessInput pi ){
    this.pi = pi;
  }

  /** Process incomming private messages
    * 
    * @param chat Chat Object to talk back to user
    * @param m Message to process
    */
  public void processMessage( Chat chat, Message m ){
    pi.newMessage( m.getBody(), new UserEntity( m.getFrom().split( "/" )[0], chat ), "private" );
  }

}