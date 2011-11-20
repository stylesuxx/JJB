/**
  * The Jabberbot needs the following arguments
  * * JID, which already must be registered on the corresponding server
  * * Password for the account
  * * Room to Join
  * * Server where the room is
  * * Nick the bot should use in the channel
  *
  * This Bot is based on the JAVA Smack Jabber Library
  */
import java.util.*;
import java.io.*;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.SASLAuthentication;

class javabot implements MessageListener{
  XMPPConnection con;

  // Login to Server
  public void login(String user, String pass, String server, String res, String auth) throws XMPPException{
    ConnectionConfiguration config = new ConnectionConfiguration( server, 5222, server );
    con = new XMPPConnection( config );
    con.connect();
    SASLAuthentication.supportSASLMechanism( auth, 0 );
    con.login( user, pass, res );
  }

  // Dissconect from Server
  public void disconnect(){
    con.disconnect();
  }

  // Send message to User
  public void sendMessage(String message, String to) throws XMPPException{
    Chat chat = con.getChatManager().createChat(to, this);
    chat.sendMessage(message);
  }

  // Process messages
  public void processMessage(Chat chat, Message message){
    if(message.getType() == Message.Type.chat)
    System.out.println(chat.getParticipant() + " says: " + message.getBody());
  }

  public static void main(String args[]) throws XMPPException, IOException{
    // Main variables must be set
    String botJid, botPass, botNick, botRoom, botServer;

    // This are the optional arguments, if not set these are the default values
    String botResource = "JavaBot v0.1";
    String botAuth = "DIGEST-MD5";

    try {
      Options opt = new Options();
      
      opt.addOption( "h", false, "Print this Help" );
      opt.addOption( "j", true, "The Username to use" );
      opt.addOption( "p", true, "The password to use" );
      opt.addOption( "s", true, "The server to join" );
      opt.addOption( "r", true, "The room to join" );
      opt.addOption( "n", true, "The nick to use in the channel" );
      opt.addOption( "c", true, "Resource" );
      opt.addOption( "e", true, "Encription [ PLAIN | DIGEST-MD5 ]" );

      BasicParser parser = new BasicParser();
      CommandLine cl = parser.parse( opt, args );

      // In case user wants help or some main arguments are missing print the Help
      if( cl.hasOption( 'h' ) || !cl.hasOption( 'j' ) || !cl.hasOption( 'p' ) || !cl.hasOption( 's' ) || !cl.hasOption( 'r' ) || !cl.hasOption( 'n' ) ) {
	HelpFormatter f = new HelpFormatter();
        f.printHelp( "javabot -j <Jid> -p <Password> -s <Server> -r <Room> -n <Nick>", opt );
      }
      // Here is where the magic happens
      else{
	// Assign arguments to variables
	botJid = cl.getOptionValue( "j" );
	botPass = cl.getOptionValue( "p" );
	botServer = cl.getOptionValue( "s" );
	botRoom = cl.getOptionValue( "r" );
	botNick = cl.getOptionValue( "n" );
	if( cl.hasOption( 'c' ) ) botResource = cl.getOptionValue( "c" );
	if( cl.hasOption( 'e' ) ) botResource = cl.getOptionValue( "e" );

	// declare variables
	javabot c = new javabot();
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	String msg;

	// turn on the enhanced debugger
	//XMPPConnection.DEBUG_ENABLED = true;
        
	// Enter your login information here
	c.login( botJid, botPass, botServer, botResource, botAuth );
    
	//c.displayBuddyList();
    
	System.out.println("-----");
    
	System.out.println("Who do you want to talk to? - Type contacts full email address:");
	String talkTo = br.readLine();
    
	System.out.println("-----");
	System.out.println("All messages will be sent to " + talkTo);
	System.out.println("Enter your message in the console:");
	System.out.println("-----\n");
    
	while( !(msg=br.readLine()).equals("bye"))
	{
	    c.sendMessage(msg, talkTo);
	}
    
	c.disconnect();
	System.exit(0);
      }
    } catch(ParseException e){ System.out.println("Seems like you missed to provide a value..."); }
  }
}