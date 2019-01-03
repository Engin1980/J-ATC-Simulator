package eng.jAtcSim.lib.speaking.fromAtc.atc2atc;

import eng.jAtcSim.lib.speaking.fromAtc.IAtc2Atc;

public class StringResponse implements IAtc2Atc {
  public final boolean rejection;
  public final String text;

  public StringResponse(boolean rejection, String format, Object ... params) {
    this.rejection = rejection;
    this.text = String.format(format, params);
  }

  public static StringResponse create(String format, Object ... params){
    StringResponse ret = new StringResponse(false, format, params);
    return ret;
  }

  public static StringResponse createRejection(String format, Object ... params){
    StringResponse ret = new StringResponse(true, format, params);
    return ret;
  }

  @Override
  public boolean isRejection() {
    return rejection;
  }

  @Override
  public String toString() {
    String ret = text;
    if (rejection)
      ret += " (rejection)";
    ret += " {StringResponse-Atc}";
    return ret;
  }
}
