import java.util.ArrayList;

public class DBTester{
  public static void main( String[] args ){
    DataBase db = new DataBase();
    db.connect();
    for( int j = 17; j < 20; j++ )
      db.setAdmin( "ID" + j + "@1337.af" );
      //db.approveUser("bla");
    ArrayList<String> tmp = db.getRegisteredUsers(); 
    for(int i=0; i<tmp.size(); i++ ){
      System.out.println( "Jid: " + tmp.get( i ) );
    }

    tmp = db.getApprovedUsers(); 
    for(int i=0; i<tmp.size(); i++ ){
      System.out.println( "App Jid: " + tmp.get( i ) );
    }

    tmp = db.getAdminUsers(); 
    for(int i=0; i<tmp.size(); i++ ){
      System.out.println( "Admin Jid: " + tmp.get( i ) );
    }

    System.out.println( db.isRegisteredUser( "ID19@1337.af" ) );
    System.out.println( db.isApprovedUser( "ID19@1337.af" ) );
    System.out.println( db.isAdminUser( "ID19@1337.af" ) );

    db.disconnect();
  }

}