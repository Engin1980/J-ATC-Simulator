package eng.jAtcSim.lib;

public class Log {
  public enum eType{
    info,
    warning,
    critical
  }

  public void log(eType type, String format, Object ... params){
    String s = String.format(format,params);
    s = String.format("JAtcSim - %s: %s", type, s);
    System.out.println(s);
  }
}
