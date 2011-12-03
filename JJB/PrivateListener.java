package JJB;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Chat;

/** This Class handles all incomming private messages
  *
  * @author stylesuxx
  * @version 0.1 
  */
class PrivateListener implements MessageListener{
  InputProcessor ip;

  /** Default Constructor
    * 
    * @param pc ProcessInput Queue to use
    */
  public PrivateListener( InputProcessor ip ){
    this.ip = ip;
  }

  /** Process incomming private messages
    * 
    * @param chat Chat Object to talk back to user
    * @param m Message to process
    */
  public void processMessage( Chat chat, Message m ){
    ip.newMessage( m.getBody(), new UserEntity( m.getFrom().split( "/" )[0], chat ), "private" );
  }

}