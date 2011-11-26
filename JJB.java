/**Java Jabber Bot
 * <p>This is a Java Jabber Bot based on the Smack and SQLite4java Library. This should work Crossplattform, although it was only tested on Linux.</p>
 * <p>Some features:</p>
 * <ul>
 * <li>User register and have to be aproved by staff before they can interact with the bot</li>
 * <li>List of Tv shows the occupants like to watch</li>
 * </ul>
 * 
 * @author stylesuxx
 * @version 0.1
 */

public class JJB{

  /**
   * Main Method for the JJB, parses the Command Line Parameters logs the Bot in and joins a room
   * @param args Command Line Parameters
   */
  public static void main( String[] args ){
    // Main variables must be set
    String botJid = null;
    String botPass = null;
    String botNick = null;
    String botRoom = null;
    String botServer = null;
    String botAdmin = null;

    // This are the optional arguments, if not set these are the default values
    String botResource = "JavaJabberBot v0.1";
    String botAuth = "DIGEST-MD5";

    // Try to get the command Line parameters or die trying
    try {
      Options opt = new Options();
      
      opt.addOption( "h", false, "Print this Help" );
      opt.addOption( "jid", true, "Jid to use" );
      opt.addOption( "pass", true, "The password to use" );
      opt.addOption( "server", true, "The server to join" );
      opt.addOption( "room", true, "The room to join" );
      opt.addOption( "nick", true, "The nick to use in the channel" );
      opt.addOption( "resource", true, "Resource" );
      opt.addOption( "admin", true, "JID of admin" );
      opt.addOption( "enc", true, "Encription [ PLAIN | DIGEST-MD5 ]" );

      BasicParser parser = new BasicParser();
      CommandLine cl = parser.parse( opt, args );

      // In case user wants help or some main arguments are missing print the Help
      if( cl.hasOption( 'h' ) || !cl.hasOption( "admin" ) || !cl.hasOption( "jid" ) || !cl.hasOption( "pass" ) || !cl.hasOption( "server" ) || !cl.hasOption( "room" ) || !cl.hasOption( "nick" ) ) {
	HelpFormatter f = new HelpFormatter();
        f.printHelp( "javabot -jid <user> -pass <Password> -server <Server> -room <Room> -nick <Nick> -admin <JID>", opt );
	System.exit(0);
      }

      // Assign arguments to variables
      else{
	botJid = cl.getOptionValue( "jid" );
	botPass = cl.getOptionValue( "pass" );
	botServer = cl.getOptionValue( "server" );
	botRoom = cl.getOptionValue( "room" );
	botNick = cl.getOptionValue( "nick" );
	botAdmin = cl.getOptionValue( "admin" );
	if( cl.hasOption( "resource" ) ) botResource = cl.getOptionValue( "resource" );
	if( cl.hasOption( "enc" ) ) botAuth = cl.getOptionValue( "enc" );
      }
    }catch( ParseException e ){
      System.out.println("Seems like you missed to provide a value..."); 
      System.exit(0);
     }

    // This is only done when all the parameters are correct
    Bot bot = new Bot( botJid, botPass, botResource, botAuth, botNick, botAdmin );
    bot.login();
    bot.joinRoom( botRoom );
  }

}