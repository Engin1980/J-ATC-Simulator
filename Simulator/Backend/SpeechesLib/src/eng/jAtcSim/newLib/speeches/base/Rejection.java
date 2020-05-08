package eng.jAtcSim.newLib.speeches.base;

public class Rejection<T> extends Response<T> {
  private final String reason;

  public Rejection(T origin, String reason) {
    super(origin);
    this.reason = reason;
  }

  public String getReason() {
    return reason;
  }

  @Override
  public String toString(){
    String ret = "Rejection of |:" + this.getOrigin().toString() + ":| due to " + reason;
    return ret;
  }
}
