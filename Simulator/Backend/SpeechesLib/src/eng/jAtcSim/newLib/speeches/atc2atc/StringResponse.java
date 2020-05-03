package eng.jAtcSim.newLib.speeches.atc2atc;

import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.speeches.INotification;
import eng.jAtcSim.newLib.speeches.IRejectable;

public class StringResponse implements INotification, IRejectable, IMessageContent {
  public static StringResponse create(String format, Object... params) {
    StringResponse ret = new StringResponse(false, format, params);
    return ret;
  }

  public static StringResponse createRejection(String format, Object... params) {
    StringResponse ret = new StringResponse(true, format, params);
    return ret;
  }
  public final boolean rejection;
  public final String text;

  public StringResponse(boolean rejection, String format, Object... params) {
    this.rejection = rejection;
    this.text = String.format(format, params);
  }

  @Override
  public boolean isRejection() {
    return rejection;
  }

  @Override
  public String toString() {
    String ret = text;
    if (rejection)
      ret += " (messageType)";
    ret += " {StringResponse-Atc}";
    return ret;
  }
}
