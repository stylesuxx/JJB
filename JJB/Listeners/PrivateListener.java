package JJB.Listeners;

import JJB.Processes.InputProcessor;
import JJB.Processes.User;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Chat;

/** This Class handles all incoming private messages
 *
 * @author stylesuxx
 * @version 0.2
 */
public class PrivateListener implements MessageListener{
  InputProcessor ip;

  /** Default Constructor
   * 
   * @param ip Input Processor to use.
   */
  public PrivateListener( InputProcessor ip ){
    this.ip = ip;
  }

  /** Process incoming private messages
   * 
   * @param chat Chat Object to talk back to user.
   * @param m Message to process.
   */
  public void processMessage( Chat chat, Message m ){
    ip.newMessage( m.getBody(), new User( m.getFrom().split( "/" )[0], chat ), "private" );
  }

}