import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

class MucListener implements PacketListener{
  ProcessInput pc;
  String ignore = null;

  public MucListener( ProcessInput pc, String ignore ){
    this.pc = pc;
    this.ignore = ignore;
  }

  public void processPacket( Packet p ){
    if( p instanceof Message ){
      Message msg = ( Message ) p;
      if( !msg.getFrom().equals( ignore ) )
	pc.newMucMessage( msg.getBody(), msg.getFrom() );
    }
  }
}