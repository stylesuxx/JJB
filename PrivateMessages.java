class PrivateMessages extends Thread{
  String message, nick;
  Chat chat;

  MUCMessages( String message, String nick, Chat chat ){
    this.message = message;
    this.nick = nick;
    this.chat = chat;
  }

  void sendMessage( String msg, String to ){
    try{
      muc.sendMessage( msg );
    } catch( XMPPException e ) { System.out.println( "Could not send message to MUC." ); }
  }

  public void run(){
    // Use this to check if Bot is alive
    if( message.equals( "ping" ) ) sendMessage( "pong" );
  }
}