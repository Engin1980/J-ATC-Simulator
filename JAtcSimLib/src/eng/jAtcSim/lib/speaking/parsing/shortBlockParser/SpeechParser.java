package eng.jAtcSim.lib.speaking.parsing.shortBlockParser;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.lib.speaking.IFromAtc;

public abstract class SpeechParser<T extends IFromAtc> {


  public final String[] getPrefixes() {
    //TODO optimalize according to type, as this method always returns the same for the same class type
    String[] ret = new String[getPatterns().length];
    for (int i = 0; i < getPatterns().length; i++) {
      ret[i] = getPatterns()[i][0];
    }
    return ret;
  }

  public abstract String[][] getPatterns();

  public String getHelp() {
    throw new UnsupportedOperationException("TODO");
//    EStringBuilder sb = new EStringBuilder();
//    sb.appendLine(this.getClass().getSimpleName());
//    sb.appendLine("\t" + this.getPattern());
//    return sb.toString();
  }

  public abstract T parse(IList<String> blocks);

  protected int getInt(IList<String> lst, int index) {
    String s = lst.get(index);
    int ret;
    try {
      ret = Integer.parseInt(s);
    } catch (Exception ex) {
      throw new EApplicationException("Unable to parse " + s + " to integer.");
    }
    return ret;
  }
}
