package jatcsimlib.speaking.parsing;

import jatcsimlib.speaking.commands.Command;
import jatcsimlib.speaking.commands.CommandList;

public abstract class Parser {
  public abstract Command parseOne(String text);
  public abstract CommandList parseMulti(String text);
  public abstract String getHelp();
  public abstract String getHelp(String commandPrefix);
}
