package JJB.Tv;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.net.URL;
import java.util.ArrayList;

/** <h1>Lookup a Shows Details on TV Rage</h1>
 * <p>Looks up a show on the TVRage Site. A valid show ID is needed, this can be found on the search site on the TV Rage Homepage</p>
 * <p>Depending on how old the show is, it is possible that not all values are set, these are the defaul values if not set</p>
 * <ul>
 *  <li>String: null</li>
 *  <li>int: -1</li>
 *  <li>Object: null</li>
 * </ul>
 * 
 * @author stylesuxx 
 * @version 0.1
 *
 */
public class TVRageLookup{
  private String showname = null;
  private String showlink = null;
  private String origin_country = null;
  private String status = null;
  private String classification = null;
  private String network = null;
  private String airtime = null;
  private String airday = null;
  private String timezone = null;
  private int seasons = -1;
  private int started = -1;
  private int showid = -1;
  private int runtime = -1;  
  private Calendar startdate = new GregorianCalendar();
  private Calendar ended = new GregorianCalendar();
  private boolean validshow = false;
  private ArrayList<String> genres = new ArrayList<>();
  
  // This is for the tv rage showinfo xml
  private boolean Xshowname = false;
  private boolean Xshowlink = false;
  private boolean Xseasons = false;
  private boolean Xstarted = false;
  private boolean Xstartdate = false;
  private boolean Xended = false;
  private boolean Xorigin_country = false;
  private boolean Xstatus = false;
  private boolean Xclassification = false;
  private boolean Xgenre = false;
  private boolean Xruntime = false;
  private boolean Xnetwork = false;
  private boolean Xairtime = false;
  private boolean Xairday = false;
  private boolean Xtimezone = false;

  /** <h2>Default Constructor<h2>
    * <p>Connects to TV Rage site and sets all the values so the getters have something to return.</p>
    * 
    * @param showid TV Rage ID of the show to lookup
    */
  public TVRageLookup( int showid ){
    this.showid = showid;
    SAXParserFactory factory = SAXParserFactory.newInstance();

    try{
    SAXParser saxParser = factory.newSAXParser();
    DefaultHandler handler = new DefaultHandler() {
      // Checks wchich start Elements are set for further processing
      public void startElement(String uri, String localName,String qName, Attributes attributes){
	if (qName.equalsIgnoreCase("showname")) Xshowname = true;
	if (qName.equalsIgnoreCase("genre")) Xgenre = true;
	if (qName.equalsIgnoreCase("showlink")) Xshowlink = true;
	if (qName.equalsIgnoreCase("seasons")) Xseasons = true;
	if (qName.equalsIgnoreCase("started")) Xstarted = true;
	if (qName.equalsIgnoreCase("startdate")) Xstartdate = true;
	if (qName.equalsIgnoreCase("ended")) Xended = true;
	if (qName.equalsIgnoreCase("origin_country")) Xorigin_country = true;
	if (qName.equalsIgnoreCase("status")) Xstatus = true;
	if (qName.equalsIgnoreCase("runtime")) Xruntime = true;
	if (qName.equalsIgnoreCase("network")) Xnetwork = true;
	if (qName.equalsIgnoreCase("airtime")) Xairtime = true;
	if (qName.equalsIgnoreCase("airday")) Xairday = true;
	if (qName.equalsIgnoreCase("timezone")) Xtimezone = true;
	if (qName.equalsIgnoreCase("classification")) Xclassification = true;
      }
  
      public void characters(char ch[], int start, int length){
	  if( Xgenre ){ 
	    genres.add( new String( ch, start, length ) );
	    Xgenre = false;
	  } 
	  if( Xclassification ){ 
	    classification = new String( ch, start, length );
	    Xclassification = false;
	  } 
	  if( Xtimezone ){
	    timezone = new String( ch, start, length ).split( " " )[0];
	    Xtimezone = false;
	  } 
	  if( Xairday ){
	    airday = new String( ch, start, length );
	    Xairday = false;
	  } 
	  if( Xairtime ){
	    airtime = new String( ch, start, length );
	    Xairtime = false;
	  } 
	  if( Xnetwork ){
	    network = new String( ch, start, length );
	    Xnetwork = false;
	  } 
	  if( Xruntime ){
	    try{
	      runtime = Integer.parseInt( new String( ch, start, length ) );;
	    }catch( Exception e ){} //runtime stays default
	    Xruntime = false;
	  } 
	  if( Xstatus ){
	    status = new String( ch, start, length );
	    Xstatus = false;
	  } 
	  if( Xorigin_country ){
	    origin_country = new String( ch, start, length );
	    Xorigin_country = false;
	  } 
	  if( Xended ){
	    ended = Array2Date( new String( ch, start, length ).split("/") );
	    Xended = false;
	  } 
	  if( Xstartdate ){
	    startdate = Array2Date( new String( ch, start, length ).split("/") );
	    Xstartdate = false;
	  } 
	  if( Xstarted ){
	    try{
	      started = Integer.parseInt( new String( ch, start, length ) );;
	    }catch( Exception e ){ }//started stays default
	    Xstarted = false;
	  } 
	  if( Xseasons ){
	    try{
	      seasons = Integer.parseInt( new String( ch, start, length ) );;
	    }catch( Exception e ){ }//seasons stays default
	    Xseasons = false;
	  } 
	  if( Xshowlink ){
	    showlink = new String(ch, start, length); 
	    Xshowlink = false;
	  } 
	  if( Xshowname ){
	    showname = new String(ch, start, length);
	    validshow = true;
	    Xshowname = false;
	  } 
      }
    };
    saxParser.parse( new InputSource(new URL("http://services.tvrage.com/feeds/showinfo.php?sid=" + showid).openStream()), handler);
   
   }catch( ParserConfigurationException | SAXException | IOException e ){ 
      System.out.println("TV Rage Offline,..");
    }
   }

  private Calendar Array2Date(String[] dateA ){
    if ( dateA.length != 3 ) return null;
    Calendar tmp = new GregorianCalendar();
    int month = -1;
    int day = -1;
    int year = -1;
    switch (dateA[0]) {
      case "Jan": month = 1; break;
      case "Feb": month = 2; break;
      case "Mar": month = 3; break;
      case "Apr": month = 4; break;
      case "May": month = 5; break;
      case "Jun": month = 6; break;
      case "Jul": month = 7; break;
      case "Aug": month = 8; break;
      case "Sep": month = 9; break;
      case "Oct": month = 10; break;
      case "Nov": month = 11; break;
      case "Dec": month = 12; break;
    }
    day = Integer.parseInt( dateA[1] );
    year = Integer.parseInt( dateA[2] );
    tmp.set( year, month, day );
    return tmp;
  } 

  
  /** Returns name of show
    * @return String
    */
  public String getShowname(){ return showname; }

  /** Returns link to show
    * @return String
    */
  public String getShowlink(){ return showlink; }

  /** Returns origin country of the show
    * @return String
    */
  public String getOrigin_country(){ return origin_country; }

  /** Returns status of the show
    * @return String
    */
  public String getStatus(){ return status; }

  /** Returns classification of the show
    * @return String
    */
  public String getClassification(){ return classification; }

  /** Returns Network on which the show aired first
    * @return String
    */
  public String getNetwork(){ return network; }

  /** Returns time on which the show originally aired
    * @return String
    */
  public String getAirtime(){ return airtime; }

  /** Returns the day on which the show originally aired
    * @return String
    */
  public String getAirday(){ return airday; }

  /** Returns the timezone in which the show was originally aired
    * @return String
    */
  public String getTimezone(){ return timezone; }

  /** Returns runtime of the show
    * @return int
    */
  public int getRuntime(){ return runtime; }

  /** Returns the year the show started
    * @return int
    */
  public int getStarted(){ return started; }

  /** Returns the show's seasons
    * @return int
    */
  public int getSeasons(){ return seasons; }

  /** Returns the shows ID
    * @return int
    */
  public int getShowid(){ return showid; }

  /** Returns date when the show originally started
    * @return Calendar
    */
  public Calendar getStartdate(){ return startdate; }

  /** Returns Date when the show originally ended
    * @return Calendar
    */
  public Calendar getEnded(){ return ended; }

  /** Returns if the given ID matches to an actual show
    * @return boolean
    */
  public boolean isValidshow(){ return validshow; }

  /** Returns the show's Genres
    * @return ArrayList
    */  
  public ArrayList<String> getGenres(){ return genres; }

}