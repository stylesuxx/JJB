import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

/** This is the listener for the MUC
  * Handles all incomming messages from MUC 
  */
class MucListener implements PacketListener{
  ProcessInput pc;
  String ignore = null;

  /** Default Constructor
    * @param pc ProcessInput Queue to use
    * @param ignore Nick to ignore in MUC, this is the Bot's handle
    */
  public MucListener( ProcessInput pc, String ignore ){
    this.pc = pc;
    this.ignore = ignore;
  }

  /** Process incomming MUC packages
    * @param p Packet to process
    */
  public void processPacket( Packet p ){
    if( p instanceof Message ){
      Message msg = ( Message ) p;
      if( !msg.getFrom().equals( ignore ) )
	pc.newMucMessage( msg.getBody(), msg.getFrom() );
    }
  }
}