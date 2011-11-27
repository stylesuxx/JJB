import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/** Lookup a Shows Details on TV Rage
  * 
  * @author stylesuxx 
  * @version 0.1
  *
  */

public class TVRageLookup{
  private String showname = "init";
  private String showlink = "init";
  private String origin_country = "init";
  private String status = "init";
  private String classification = "init";
  private String network = "init";
  private String airtime = "init";
  private String airday = "init";
  private String timezone = "init";
  private int seasons = -1;
  private int started = -1;
  private int showid = -1;
  private int runtime = -1;  
  private Calendar startdate = new GregorianCalendar();
  private Calendar ended = new GregorianCalendar();
  private boolean validshow = false;
  private ArrayList<String> genres = new ArrayList<String>();
  
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
  // boolean akas = false;
  // boolean aka = false;

  /** Default Constructor
    *
    * @param showid TV Rage ID of the show to lookup
    *
    */
  public TVRageLookup( int showid ){
    this.showid = showid;
  }

  /** get the XML
    *
    *
    */
  public void lookup(){
    //XMLReader reader = XMLReaderFactory.createXMLReader();
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
	    timezone = new String( ch, start, length );
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
	    }catch( Exception e ){ } //runtime stays default
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
    //saxParser.parse("file.xml", handler);
    saxParser.parse( new InputSource(new URL("http://services.tvrage.com/feeds/showinfo.php?sid=" + showid).openStream()), handler);
   
   }catch( Exception e ){ 
      System.out.println("TV Rage Offline,..");
      e.printStackTrace();
    }
   }

  private Calendar Array2Date(String[] dateA ){
    if ( dateA.length != 3 ) return null;
    Calendar tmp = new GregorianCalendar();
    int month = -1;
    int day = -1;
    int year = -1;

    if( dateA[0].equals("Jan") ) month = 1;
    else if( dateA[0].equals("Feb") ) month = 2;
    else if( dateA[0].equals("Mar") ) month = 3;
    else if( dateA[0].equals("Apr") ) month = 4;
    else if( dateA[0].equals("May") ) month = 5;
    else if( dateA[0].equals("Jun") ) month = 6;
    else if( dateA[0].equals("Jul") ) month = 7;
    else if( dateA[0].equals("Aug") ) month = 8;
    else if( dateA[0].equals("Sep") ) month = 9;
    else if( dateA[0].equals("Oct") ) month = 10;
    else if( dateA[0].equals("Nov") ) month = 11;
    else if( dateA[0].equals("Dec") ) month = 12;
    day = Integer.parseInt( dateA[1] );
    year = Integer.parseInt( dateA[2] );
    tmp.set( year, month, day );
    return tmp;
  } 

  
  /** Returns Showname
    * @return String
    */
  public String getShowname(){ return showname; }
  /** Returns link to show
    * @return String
    */
  public String getShowlink(){ return showlink; }
  /** Returns origin Country of the show
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
  /** Returns Network which first aired the show
    * @return String
    */
  public String getNetwork(){ return network; }
  /** Returns time on which the show aired originally
    * @return String
    */
  public String getAirtime(){ return airtime; }
  /** Returns the day on which the show aired origiinally
    * @return String
    */
  public String getAirday(){ return airday; }
  /** Returns the timezone in which the show was aired originally
    * @return String
    */
  public String getTimezone(){ return timezone; }
  /** Returns runtime of show
    * @return int
    */
  public int getRuntime(){ return runtime; }
  /** Returns the year the show started
    * @return int
    */
  public int getStarted(){ return started; }
  /** Returns the nr of seasons of the show
    * @return int
    */
  public int getSeasons(){ return seasons; }
  /** Returns  show ID
    * @return int
    */
  public int getShowid(){ return showid; }
  /** Returns Date when the show originally started
    * @return Calendar
    */
  public Calendar getStartdate(){ return startdate; }
  /** Returns Date when the show originally ended
    * @return Calendar
    */
  public Calendar getEnded(){ return ended; }
  /** Returns if the ID matches to a actual show
    * @return boolean
    */
  public boolean isValidshow(){ return validshow; }
  /** Returns the Genres as An Arraylist
    * @return ArrayList
    */  
  public ArrayList<String> getGenres(){ return genres; }
  /** Returns the Genres as an Array
    * @return String[]
    */
  public String[] getGenresArray(){ return Arrays.copyOf(genres.toArray(), genres.toArray().length, String[].class); }
}