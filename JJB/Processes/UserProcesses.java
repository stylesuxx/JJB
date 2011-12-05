package JJB.Processes;

import JJB.SQLite.UserQueries;
import java.util.ArrayList;

/** This Class holds all the User Processes
 *
 * @author stylesuxx
 */
public class UserProcesses {
  UserQueries userQ;
  
  public UserProcesses( UserQueries userQ ){
    this.userQ = userQ;
  }
  
  /** Adds a new user to the database with status "registered"
   * 
   * @param user User to register.
   * @return String
   */
  public String registerUser( String user ){
    if( userQ.registerUser( user ) ) return "You have successfully registered, staff will approve you soon.";
    return "You already are registered - staff will approve you soon.";
  }
  
  /** Approve user - set status to "approved"
   *
   * @param user User to approve
   * @return String
   */
  public String approveUser( String user ){
    if( userQ.setUserStatus( user, "approved" ) ) return "You approved " + user + "!";
    else return "Could not promote " + user + "!";
  }
  
  /** Promote user to admin - set status to "admin"
   *
   * @param user User to promote to admin.
   * @return Sring
   */ 
  public String adminUser( String user ){
    if( userQ.setUserStatus( user, "admin" ) ) return "You promoted " + user + " to Admin!";
    else return "Could not promote " + user + "!";
  }
  
  /** Delete user
   *
   * @param user User to delete
   * @return Sring
   */
  public String deleteUser( String user ){
    if( userQ.deleteUser( user ) ) return "You removed " + user + "!";
    else return "Could not remove " + user + " - check JID!";
  }
  
  /** Set the usergroup of a User
   * 
   * @param user User whos usergroup to set.
   * @param group Group to add the user to.
   * @return String
   */
  public String setUserGroup( String user, String group ){
   return null;
  }
    
  /** Get users with certain status and return formatted string
   * 
   * @param status Status of users to lookup.
   * @return String  
   */
  public String getUsers( String status ){
    String toReturn = "";
    ArrayList<String> tmp = null;
    boolean valid = true;
      
    switch( status ){
      case "blocked": toReturn = "Blocked Users:"; break;
      case "registered": toReturn = "Registered Users:"; break;
      case "approved": toReturn = "Approved Users:"; break;
      case "admin": toReturn = "Admin Users:"; break;
      default:{
        toReturn = "No such Usergroup.";
        valid = false;
      } break;
    }
    
    // Only look up the users if a valid Usergroup is provided
    if( valid ){
      tmp = userQ.getUsers( status ); 
      for(int i=0; i<tmp.size(); i++ ){
        toReturn += "\n" + tmp.get( i );
      }	
      if(tmp.size() > 0 && valid )
        return toReturn;
      else return "There are no users in this group.";
    }
    else return toReturn;
  }
  
}
