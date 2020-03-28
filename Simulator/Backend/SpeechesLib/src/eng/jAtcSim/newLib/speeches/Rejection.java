package eng.jAtcSim.newLib.speeches;

import eng.eSystem.validation.EAssert;

public class Rejection extends Response {

  private final String reason;

  public Rejection(ICommand origin, String reason) {
    super(origin);
    EAssert.Argument.isNonEmptyString(reason);
    this.reason = reason;
  }

  public Rejection(String reason, ICommand origin) {
    this(origin,reason);
  }

  public String getReason() {
    return reason;
  }

  @Override
  public String toString(){
    String ret = "Rejection of |:" + this.getOrigin().toString() + ":| due to " + reason + " {notification}";
    return ret;
  }
}
