package JJB.Processes;

/** A Job the Bot has to process
 * 
 * @author stylesuxx
 * @version 0.2
 */
public class Job{
  private String message, resource;
  private User user;

  /** Default Constructor
   * 
   * @param message A Jobs message.
   * @param usr Infos about the User who sent the Job.
   * @param resource Where the message is comming from ( muc | private ).
   */
  public Job( String message, User user, String resource){
    this.message = message;
    this.user = user;
    this.resource = resource;
  }

  /** Return the Jobs message
   * 
   * @return String
   */
  public String getMessage(){ return message; }

  /** Return the Jobs User
   * 
   * @return UserEntity
   */
  public User getUser(){ return user; }

  /** Return the Jobs Resource
   * 
   * @return String muc | private
   */
  public String getResource(){ return resource; }

}