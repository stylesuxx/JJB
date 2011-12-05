import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

/** <h1>Java Jabber Bot</h1>
 * <p>This is a Java Jabber Bot based on the Smack and SQLite4java Library. This should work Crossplattform, although it was only tested on Linux.</p>
 * <p>Some features:</p>
 * <ul>
 *  <li>User register and have to be approved by staff before they can interact with the bot</li>
 *  <li>List Tv shows with countdown, which the Muc occupants like to watch (global Watchlist)</li>
 *  <li>Manage your own Tv Show Countdown list (personal Watchlist)</li>
 *  <li>List all shows added by all users to find something new</li>
 *  <li>Helpfile which lists all functions</li>
 * </ul>
 * 
 * @author stylesuxx
 * @version 0.1
 */
public class JJB{

  /**
   * Main Method for the JJB, parses the Command Line Parameters, check if the parameters are valid and start the bot.
   * 
   * @param args Command Line Parameters
   */
  public static void main( String[] args ){
    // Main arguments must be set via commandline
    String botJid = null;
    String botPass = null;
    String botNick = null;
    String botRoom = null;
    String botAdmin = null;

    // This are the optional arguments, if not set via commandline these are the default values
    String botResource = "JavaJabberBot v0.1";
    String botAuth = "DIGEST-MD5";
    String botDB = "JJB.db";
    int botPort = 5222;
    boolean log = false;

    // Try to get the command Line parameters or die trying
    try {
      Options opt = new Options();
      
      opt.addOption( "h", false, "Print this Help" );
      opt.addOption( "jid", true, "Jid to use" );
      opt.addOption( "pass", true, "The password to use" );
      opt.addOption( "room", true, "The room to join" );
      opt.addOption( "nick", true, "The nick to use in the room" );
      opt.addOption( "res", true, "The bot's resource" );
      opt.addOption( "admin", true, "Jid of admin" );
      opt.addOption( "enc", true, "Encription [ PLAIN | DIGEST-MD5 ]" );
      opt.addOption( "port", true, "Port to use (default 5222)" );
      opt.addOption( "log", false, "Print warnings");

      BasicParser parser = new BasicParser();
      CommandLine cl = parser.parse( opt, args );

      // In case user wants help or some main arguments are missing, print the Help
      if( cl.hasOption( 'h' ) || !cl.hasOption( "admin" ) || !cl.hasOption( "jid" ) || !cl.hasOption( "pass" ) || !cl.hasOption( "room" ) || !cl.hasOption( "nick" ) ) {
	HelpFormatter f = new HelpFormatter();
        f.printHelp( "javabot -jid <user> -pass <Password> -room <Room> -nick <Nick> -admin <JID>", opt );
	System.exit(0);
      }

      // Assign arguments to variables
      else{
	// set main variables and check if they are valid
	botJid = cl.getOptionValue( "jid" );
	botPass = cl.getOptionValue( "pass" );
	botRoom = cl.getOptionValue( "room" );
	botNick = cl.getOptionValue( "nick" );
	botAdmin = cl.getOptionValue( "admin" );
        
        if( !isValidJid(botJid) ){
          System.out.println("Your bot Jid is not valid.");
          System.exit(0);
        }
        if( !isValidJid(botAdmin) ){
          System.out.println("Your admin Jid is not valid.");
          System.exit(0);
        }
        if( !isValidJid(botRoom) ){
          System.out.println("Your room is is not valid.");
          System.exit(0);
        }        

	// set optional variables and check if they are valid
	if( cl.hasOption( "res" ) ) botResource = cl.getOptionValue( "res" );
	if( cl.hasOption( "enc" ) ) botAuth = cl.getOptionValue( "enc" );
	if( cl.hasOption( "port" ) ) botPort = Integer.parseInt( cl.getOptionValue( "port" ) );
	if( cl.hasOption( "log" ) ) log = true;
        
        if( !botAuth.equals("PLAIN") && !botAuth.equals("DIGEST-MD5") ){
          System.out.println("Your authentification Type is not supported.");
          System.exit(0);
        }   

      }
    }catch( ParseException e ){
      System.out.println( "Seems like you missed to provide a value." ); 
      System.exit(0);
     }

    // Create bot, login to Server and joing Muc
    Bot bot = new Bot( botJid, botPass, botResource, botAuth, botAdmin, botPort, new File(botDB), log );
    bot.login();
    bot.joinRoom( botRoom, botNick );

    // While Bot is running
    while( !bot.isStopped() ){
      try{
	Thread.sleep( 50 );
      }catch( Exception e){}
    }
    System.out.println("Bot shut down.");
  }

  /** Check if a Jid is valid
   * 
   * @param jid String to check if valid Jid.
   * @return 
   */
  static private boolean isValidJid(String jid){
    Pattern p = Pattern.compile("^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");
    Matcher m = p.matcher(jid);
    return m.matches();
  }
}