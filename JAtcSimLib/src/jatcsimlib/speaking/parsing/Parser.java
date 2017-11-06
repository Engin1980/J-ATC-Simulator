package jatcsimlib.speaking.parsing;

import jatcsimlib.speaking.IFromAtc;
import jatcsimlib.speaking.ISpeech;
import jatcsimlib.speaking.SpeechList;
import jatcsimlib.speaking.ICommand;
import jatcsimlib.speaking.fromAtc.IAtcCommand;

public abstract class Parser {
  public abstract ISpeech parseOne(String text);
  public abstract SpeechList<IFromAtc> parseMulti(String text);

  public SpeechList<IAtcCommand> parseMultipleCommands(String text){
    SpeechList lst = this.parseMulti(text);
    SpeechList<IAtcCommand> ret = lst.convertTo();
    return ret;
  }

  public abstract String getHelp();
  public abstract String getHelp(String commandPrefix);
}
