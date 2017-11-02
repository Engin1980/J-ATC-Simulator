package jatcsimlib.speaking.parsing;

import jatcsimlib.speaking.Speech;
import jatcsimlib.speaking.SpeechList;
import jatcsimlib.speaking.commands.Command;
import jatcsimlib.speaking.commands.CommandList;

public abstract class Parser {
  public abstract Speech parseOne(String text);
  public abstract SpeechList parseMulti(String text);
  public abstract String getHelp();
  public abstract String getHelp(String commandPrefix);
}
