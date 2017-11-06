package jatcsimlib.speaking.parsing;

import jatcsimlib.speaking.Speech;
import jatcsimlib.speaking.SpeechList;
import jatcsimlib.speaking.commands.Command;
import jatcsimlib.speaking.commands.CommandList;

import java.util.List;

public abstract class Parser {
  public abstract Speech parseOne(String text);
  public abstract SpeechList parseMulti(String text);

  public CommandList parseMultipleCommands(String text){
    SpeechList lst = this.parseMulti(text);
    CommandList ret = new CommandList();
    for (Speech speech : lst) {
      Command cmd = (Command) speech;
      ret.add(cmd);
    }
    return ret;
  }

  public abstract String getHelp();
  public abstract String getHelp(String commandPrefix);
}
