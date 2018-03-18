package eng.jAtcSim.lib.speaking.parsing;

import eng.eSystem.collections.ReadOnlyList;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtc2Atc;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.ICommand;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;

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
