/** This Objects holds all content of a shows DB Entry
  */
public class TVEntity{
  private int showID = 0;
  private String showname = null;
  private String airtime = null;
  private String airday = null;
  private String timezone = null;
  private String status = null;

  /** Default Constructor
    * @param showID ID of show
    * @param showname Name of the show
    * @param airtime Time the show airs
    * @param airday Day the show airs
    * @param timezone Timezone the show airs in
    * @param status The status of the show in the database
    */
  public TVEntity(int showID, String showname, String airtime, String airday, String timezone, String status){
    this.showID = showID;
    this.showname = showname;
    this.airtime = airtime;
    this.airday = airday;
    this.timezone = timezone;
    this.status = status;
  }

  /**Returns the shows ID
    * @return int
    */
  public int getShowid(){ return showID; }

  /**Returns the shows name
    * @return String
    */
  public String getShowname(){ return showname; };

  /**Returns the shows airtime
    * @return String
    */
  public String getAirtime(){ return airtime; };

  /**Returns the shows airday
    * @return String
    */
  public String getAirday(){ return airday; };

  /**Returns the shows timezone
    * @return String
    */
  public String getTimezone(){ return timezone; };

  /**Returns the shows status
    * @return String
    */
  public String getStatus(){ return status; };

  /**Returns the String representation of this Object
    * @return String
    */
  public String toString(){
    String toReturn = "[ID: " + showID + "]";
    toReturn += ",[showname: " + showname + "]";
    toReturn += ",[airtime: " + airtime + "]";
    toReturn += ",[airday: " + airday + "]";
    toReturn += ",[timezone: " + timezone + "]";
    toReturn += ",[status: " + status + "]";
    return toReturn;
  }
}