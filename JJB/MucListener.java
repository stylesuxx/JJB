package JJB;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;

/** This is the listener for the MUC
 * Handles all incomming messages from MUC and ads it to the Job Stack
 * 
 * @author stylesuxx
 * @version 0.1
 */
class MucListener implements PacketListener{
  InputProcessor ip;
  String ignore = null;
  MultiUserChat muc = null;

  /** Default Constructor
   * @param pi ProcessInput Queue to use.
   * @param ignore Nick to ignore in MUC, this is the Bot's Jid.
   * @param muc The Muc the Bot is in.
   */
  public MucListener( InputProcessor ip, String ignore, MultiUserChat muc ){
    this.ip = ip;
    this.ignore = ignore;
    this.muc = muc;
    ip.setMuc( muc );
  }

  /** Process incomming package from Muc
   * 
   * @param p Packet to process
   */
  public void processPacket( Packet p ){
    if( p instanceof Message ){
      Message msg = ( Message ) p;
      if( !msg.getFrom().equals( ignore ) ){
	ip.newMessage( msg.getBody(), new UserEntity( muc.getOccupant( msg.getFrom() ).getJid().split( "/" )[0], muc ), "muc" );
      }
    }
  }

}