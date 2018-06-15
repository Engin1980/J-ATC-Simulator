package eng.jAtcSim.lib.global.logging;


public class ApplicationLog extends Log {

  public ApplicationLog() {
    super("Applicatoin write", false, new WriterSaver(System.out, false));
  }

  public enum eType{
    info,
    warning,
    critical
  }

  public void writeLine(eType type, String format, Object ... params){
    String s = String.format(format,params);
    super.writeLine("JAtcSim - %s: %s", type, s);
  }
}
