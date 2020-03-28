package eng.jAtcSim.newLib.textProcessing.base;

import eng.jAtcSim.newLib.speeches.ISpeech;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.common.ShortcutList;

public abstract class Parser {
  public abstract String getHelp();

  public abstract String getHelp(String commandPrefix);

  public abstract ShortcutList getShortcuts();

  public abstract <T extends ISpeech> SpeechList<T> parse(String text);

  public abstract ISpeech parseAtc2Atc(String text);

  //TODO delete
//  public SpeechList<IAtcCommand> parseMultipleCommands(String text) {
//    SpeechList lst = this.parseMulti(text);
//    SpeechList<IAtcCommand> ret = lst.convertTo();
//    return ret;
//  }
}
