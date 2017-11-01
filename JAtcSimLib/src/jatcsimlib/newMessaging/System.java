package jatcsimlib.newMessaging;

public class System implements  IMessageParticipant {
  @Override
  public String getName() {
    return "SYSTEM";
  }

  private static System i = new System();

  private System(){

  }

  public System me(){
    return i;
  }
}
