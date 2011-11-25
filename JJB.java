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

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;


public class JJB{
  public static void main( String[] args ){
    // Main variables must be set
    String botJid = null;
    String botPass = null;
    String botNick = null;
    String botRoom = null;
    String botServer = null;
    String admin = null;

    // This are the optional arguments, if not set these are the default values
    String botResource = "JavaJabberBot v0.1";
    String botAuth = "DIGEST-MD5";

    // get the command Line parameters
    try {
      Options opt = new Options();
      
      opt.addOption( "h", false, "Print this Help" );
      opt.addOption( "j", true, "The Username to use" );
      opt.addOption( "p", true, "The password to use" );
      opt.addOption( "s", true, "The server to join" );
      opt.addOption( "r", true, "The room to join" );
      opt.addOption( "n", true, "The nick to use in the channel" );
      opt.addOption( "c", true, "Resource" );
      opt.addOption( "admin", true, "JID of admin" );
      opt.addOption( "e", true, "Encription [ PLAIN | DIGEST-MD5 ]" );

      BasicParser parser = new BasicParser();
      CommandLine cl = parser.parse( opt, args );

      // In case user wants help or some main arguments are missing print the Help
      if( cl.hasOption( 'h' ) || !cl.hasOption( "admin" ) || !cl.hasOption( 'j' ) || !cl.hasOption( 'p' ) || !cl.hasOption( 's' ) || !cl.hasOption( 'r' ) || !cl.hasOption( 'n' ) ) {
	HelpFormatter f = new HelpFormatter();
        f.printHelp( "javabot -j <user> -p <Password> -s <Server> -r <Room> -n <Nick> -admin <JID>", opt );
	System.exit(0);
      }
      // Here is where the magic happens
      else{
	// Assign arguments to variables
	botJid = cl.getOptionValue( "j" );
	botPass = cl.getOptionValue( "p" );
	botServer = cl.getOptionValue( "s" );
	botRoom = cl.getOptionValue( "r" );
	botNick = cl.getOptionValue( "n" );
	admin = cl.getOptionValue( "admin" );
	if( cl.hasOption( 'c' ) ) botResource = cl.getOptionValue( "c" );
	if( cl.hasOption( 'e' ) ) botAuth = cl.getOptionValue( "e" );
      }
    }catch(ParseException e){
      System.out.println("Seems like you missed to provide a value..."); 
      System.exit(0);
     }

    // This is only done when all the parameters are correct
    Bot bot = new Bot( botJid, botPass, botServer, botResource, botAuth, botNick );
    bot.login();
    bot.joinRoom( botRoom );

    // Loop forever
    // TODO  - clean close
    boolean run = true;
    while( run ){}

    bot.disconnect();
  }
}