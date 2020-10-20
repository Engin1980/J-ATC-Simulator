package eng.jAtcSim.newLib.atcs.internal.computer;

public class RequestResult {
  public final boolean isAccepted;
  public final String message;

  public RequestResult(boolean isAccepted, String message) {
    this.isAccepted = isAccepted;
    this.message = message;
  }
}
