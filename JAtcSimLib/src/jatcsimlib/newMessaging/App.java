package jatcsimlib.newMessaging;

public class App implements  IMessageParticipant {
  @Override
  public String getName() {
    return "SYSTEM";
  }

  private static App i = new App();

  private App(){

  }

  public static App me(){
    return i;
  }
}
