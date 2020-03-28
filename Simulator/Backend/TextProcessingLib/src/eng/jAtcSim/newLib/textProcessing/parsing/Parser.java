package eng.jAtcSim.newLib.textProcessing.parsing;

import eng.jAtcSim.newLib.area.speaking.IFromAtc;
import eng.jAtcSim.newLib.area.speaking.SpeechList;
import eng.jAtcSim.newLib.area.speaking.fromAtc.IAtc2Atc;
import eng.jAtcSim.newLib.area.speaking.fromAtc.IAtcCommand;

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
