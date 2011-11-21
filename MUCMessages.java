import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Chat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.net.*;
import org.apache.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.*;
import java.io.*;

/** This class is responsible for handling the messages from the muc. 
  * This happens threaded.
  *
  *
  *
  *
  *
  *
  */

class MUCMessages extends Thread{
  String message, nick;
  MultiUserChat muc;

  MUCMessages( String message, String nick, MultiUserChat muc ){
    this.message = message;
    this.nick = nick;
    this.muc = muc;
  }
  
  // Send private message to room occupant
  private void messageUser( String msg ){
    Chat c = muc.createPrivateChat( nick, new MessageListener(){ public void processMessage( Chat chat, Message message ){} } );
    try{
      c.sendMessage( msg );
    }catch(XMPPException e) { System.out.println("Could not send private Message."); }
  }

  void sendMessage( String msg ){
    try{
      muc.sendMessage( msg );
    } catch( XMPPException e ) { System.out.println( "Could not send message to MUC. (" + message + ")" ); }
  }

  public void run(){
    if( message.equals( "ping" ) ) sendMessage( "pong" ); 		// Use this to check if Bot is alive
    if( message.equals( "time") ) sendMessage( timeDate() );		// Print time and Date
    if( message.equals( "shows") ) sendMessage( showUpcomingTV() );	// Print upcoming shows
    if( message.equals( "help" ) ) messageUser( help() );		// send Help to user
  }

  // Returns time and date
  private String timeDate(){
    String toReturn = "";
    Calendar cal = new GregorianCalendar();

    toReturn += cal.get(Calendar.HOUR);
    toReturn += ":" + cal.get(Calendar.MINUTE);
    toReturn += " ";
    switch( cal.get( Calendar.AM_PM ) ){
      case 1: toReturn += "pm"; break;
      default: toReturn += "am";
    }
    toReturn += " - ";
    switch( cal.get(Calendar.DAY_OF_WEEK) ){
      case 1: toReturn += "Sunday"; break;
      case 2: toReturn += "Monday"; break;
      case 3: toReturn += "Tuesday"; break;
      case 4: toReturn += "Wednesday"; break;
      case 5: toReturn += "Thursday"; break;
      case 6: toReturn += "Friday"; break;
      case 7: toReturn += "Saturday"; break;
      default: toReturn += "Day of the Tentacle";
    }    
    toReturn += " - " + cal.get(Calendar.DAY_OF_MONTH);
    toReturn += "."  + cal.get(Calendar.MONTH);
    toReturn += "." + cal.get(Calendar.YEAR);
    return toReturn;
  }

  // Return upcoming shows from TV Countdown
  private String showUpcomingTV(){
    String toReturn = "Upcoming shows:";
    String shows = "";
    URL u;
    String url = "http://tvcountdown.com/?c881=1&c244=1&c219=1&c539=1&c2=1&c98=1&c41=1&c12=1&c133=1&c29=1&c370=1&c3=1&c68=1&c22=1&c79=1";
    
    try{
      u = new URL( url );
      try{
	URLConnection con = u.openConnection();
	Scanner scanner = new Scanner(con.getInputStream());
	scanner.useDelimiter("\\Z");
	String content = scanner.next().split("<table class=\"episode_list_table\">")[1].split("</table>")[0];
	String[] lines = content.split("</tr>");
      
	for(int i = 1; i< lines.length; i++){
	  if( lines[i].startsWith( "<tr class=\"bc_t\">" ) ){
	    String regex = "<tr class=\"bc_t\"><td.*>.*<a.*>(.*)</a></td><td.*>(.*)</td><td.*>(.*)</td><td.*><div.*></div></td><td.*><div.*></div></td>";
	    Pattern pattern = Pattern.compile( regex );
	    Matcher matcher =  pattern.matcher( lines[i] );
	    matcher.find();
	    shows += "\n" + matcher.group(1) + " - " + matcher.group(3) + " (" + matcher.group(2) + ")";
	  }
	}
	
	if( !shows.equals("") ) toReturn += shows;
	else toReturn = "There are no upcoming shows today.";

      }catch(IOException e){ System.out.println("Could not fetch tvcountdown");}
    }catch(MalformedURLException e){ System.out.println("Could not fetch tvcountdown"); }
    return toReturn;
  }

  // Returns the helpfile
  private String help(){
    return "Help:\n"
	  +"ping\t\t Bot answers with \"pong\".\n"
	  +"time\t Print current time and date.\n"
	  +"shows\t Shows airing in the next 24h.";
  }
}