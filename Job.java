import org.jivesoftware.smack.Chat;

public class Job{
  private String message, nick, resource;
  private Chat chat = null;

  public Job( String message, String nick, String resource){
    this.message = message;
    this.nick = nick;
    this.resource = resource;
  }

  public Job( String message, String nick, String resource, Chat chat){
    this.message = message;
    this.nick = nick;
    this.resource = resource;
    this.chat = chat;
  }

  public String getMessage(){ return message; }
  public String getNick(){ return nick; }
  public String getResource(){ return resource; }
  public Chat getChat(){ return chat; }
}