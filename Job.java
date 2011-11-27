/** This class represents a Job the Bot has to process
  */
public class Job{
  private String message, resource;
  private UserEntity user;

  /** Default MUC Constructor
    * @param message A Jobs message 
    * @param nick Infos about the User who sent the Job
    * @param resource Where the message is comming from ( muc | private )
    */
  public Job( String message, UserEntity user, String resource){
    this.message = message;
    this.user = user;
    this.resource = resource;
  }

  /** Return the Jobs message
    * @return String
    */
  public String getMessage(){ return message; }

  /** Return the Jobs User
    * @return UserEntity
    */
  public UserEntity getUser(){ return user; }

  /** Return the Jobs Resource
    * @return String muc | private
    */
  public String getResource(){ return resource; }

}