package eng.jAtcSim.lib.speaking.parsing.shortParsing;

import eng.jAtcSim.lib.speaking.ICommand;
import eng.jAtcSim.lib.speaking.ICommand;

class ParserResult {

  public final ICommand command;
  public final String restOfLine;

  public ParserResult(ICommand command, String restOfLine) {
    this.command = command;
    this.restOfLine = restOfLine;
  }
}
