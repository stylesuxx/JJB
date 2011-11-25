import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Chat;

/** This Class handles all private incomming messages
  */
class PrivateListener implements MessageListener{
  ProcessInput pc;

  /** Default Constructor
    * @param pc ProcessInput Queue to use
    */
  public PrivateListener( ProcessInput pc ){
    this.pc = pc;
  }

  /** Process incomming private messages
    * @param chat Chat Object to talk back to user
    * @param m Message to process
    */
  public void processMessage( Chat chat, Message m ){
    pc.newPrivateMessage( m.getBody(), m.getFrom(), chat );
  }
}