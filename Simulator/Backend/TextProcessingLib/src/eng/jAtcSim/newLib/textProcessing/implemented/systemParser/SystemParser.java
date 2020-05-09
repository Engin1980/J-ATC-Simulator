package eng.jAtcSim.newLib.textProcessing.implemented.systemParser;

import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.speeches.system.ISystemSpeech;
import eng.jAtcSim.newLib.textProcessing.parsing.ISystemParser;

public class SystemParser implements ISystemParser {
  @Override
  public ISystemSpeech parse(Object input) {
    implement this parsers needs to be implemented too
  }

  @Override
  public boolean acceptsType(Class<?> type) {
    return String.class.equals(type);
  }

  @Override
  public boolean canAccept(Object input) {
    if (input == null) return true;
    String tmp = (String) input;
    tmp = tmp.trim();
    return tmp.length() == 0 || tmp.charAt(0) == '?';
  }
}
