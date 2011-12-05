/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JJB.Processes;

/**
 *
 * @author stylesuxx
 */
public class Help {
  
  /** Returns the Helpfile
   *
   * @param status Status the Help should be generated for
   * 
   * @return String
   */
  public String getHelp( String status ){
    String toReturn = "Help";
    switch(status){
      case "admin":{
        toReturn = "Admin Help";
        toReturn += "\nshows req\t Shows requested by users." +
                    "\nnever ID\t\t Set a show on the never list." +
                    "\nusers <grp>\t Show users in <grp>." +
                    "\nupdate shows\t force Update of the show database." +
                    "\napprove Jid/ID\t Approve a user or a show." +
                    "\ndelete Jid/ID\t Delete a user or a show." +
                    "\nquit \t\t\t Quit the bot." +          
                    "\napprove all\t Approve all requested shows.";
        toReturn += "\n-------------------------------------------------------------";
      }
      case "approved":{
        toReturn += "\nping \t\t Check if Bot is alive." +
                    "\nshows \t\t Prints all the shows we are watching with a next Episodes." +
                    "\nshows all\t\t Prints all the shows we are watching." +
                    "\nshows never\t Prints all the shows we will never watch." +
                    "\nrequest #\t Request a TV show with TV-Rage ID, multiple ID's may be provided." +
                    "\nmy add #\t Add a TV show to your personal list, multiple ID's may be provided." +
                    "\nmy shows \t Print your personal watchlist." +
                    "\nmy del #\t\t Delete a show from your personal list, multiple ID's may be provided." +
                    "\nlinks \t\t Print the last 50 posted links.";
      }break;
      case "registered":{
        toReturn += "\n\nYou have to be approved for this commands to work.";
      }break;
      default: toReturn += "If you want to be able to use the Bot type \"register\"."; break;
    }
    
    return toReturn;
  }
  
}
