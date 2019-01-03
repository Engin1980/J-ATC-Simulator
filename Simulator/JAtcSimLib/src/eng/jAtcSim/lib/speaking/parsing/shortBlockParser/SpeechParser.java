package eng.jAtcSim.lib.speaking.parsing.shortBlockParser;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.utilites.ReflectionUtils;
import eng.jAtcSim.lib.speaking.IFromAtc;

import java.lang.reflect.Type;
import java.util.Arrays;

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

  public abstract String getHelp();

  public abstract T parse(IList<String> blocks);

  public String getCommandName(){
    Type[] types = ReflectionUtils.getParameterizedTypes(this);
    String ret = types[0].getTypeName();
    return ret;
  }

  protected int getInt(IList<String> lst, int index) {
    String s = lst.get(index);
    int ret;
    try {
      ret = Integer.parseInt(s);
    } catch (Exception ex) {
      throw new EApplicationException("Unable to parseOld " + s + " to integer.");
    }
    return ret;
  }

  protected String buildHelpString(String name, String[][] patterns, String description, String examples) {
    EStringBuilder esb = new EStringBuilder();
    for (int i = 0; i < patterns.length; i++) {
      esb.appendItems(Arrays.asList(patterns[i]), "\n");
    }
    String ret = this.buildHelpString(name, esb.toString()  , description, examples );
    return ret;
  }
  protected String buildHelpString(String name, String syntax, String description, String examples) {
    EStringBuilder esb = new EStringBuilder();
    esb.append("** ").append(name).appendLine(" **");
    esb.appendLine("* Description *");
    esb.appendLine(description.replace("\n", "\r\n"));
    esb.appendLine("* Syntax *");
    esb.appendLine(syntax.replace("\n", "\r\n"));
    esb.appendLine("* Examples *");
    esb.appendLine(examples.replace("\n", "\r\n"));
    return esb.toString();
  }
}
