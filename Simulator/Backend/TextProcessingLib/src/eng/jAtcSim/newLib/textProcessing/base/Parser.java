package eng.jAtcSim.newLib.textProcessing.base;

import eng.jAtcSim.newLib.speeches.ISpeech;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.textProcessing.implemented.parsers.shortBlockParser.ShortcutList;

public abstract class Parser {
  public abstract String getHelp();

  public abstract String getHelp(String commandPrefix);

  public abstract ShortcutList getShortcuts();

  public abstract <T extends ISpeech> T parse(String text);

  public abstract <T extends ISpeech> SpeechList<T> parseMulti(String text);

  //TODO delete
//  public SpeechList<IAtcCommand> parseMultipleCommands(String text) {
//    SpeechList lst = this.parseMulti(text);
//    SpeechList<IAtcCommand> ret = lst.convertTo();
//    return ret;
//  }
}
