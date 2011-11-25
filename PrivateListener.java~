import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Chat;

class PrivateListener implements MessageListener{
  ProcessInput pc;

  public PrivateListener( ProcessInput pc ){
    this.pc = pc;
  }

  public void processMessage( Chat chat, Message m ){
    pc.newPrivateMessage( m.getBody(), m.getFrom(), chat );
  }
}