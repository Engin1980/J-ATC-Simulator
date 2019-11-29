package eng.jAtcSim.lib.textProcessing.parsing;

import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtc2Atc;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.IFromAtc;

public abstract class Parser {
  public abstract SpeechList<IFromAtc> parseMulti(String text);

  public SpeechList<IAtcCommand> parseMultipleCommands(String text){
    SpeechList lst = this.parseMulti(text);
    SpeechList<IAtcCommand> ret = lst.convertTo();
    return ret;
  }

  public abstract ShortcutList getShortcuts();

  public abstract String getHelp();
  public abstract String getHelp(String commandPrefix);

  public abstract IAtc2Atc parseAtc(String text);
}
