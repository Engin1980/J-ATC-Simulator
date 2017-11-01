package jatcsimlib.speaking.parsing.shortParsing;

import jatcsimlib.speaking.commands.Command;

class ParserResult {

  public final Command command;
  public final String restOfLine;

  public ParserResult(Command command, String restOfLine) {
    this.command = command;
    this.restOfLine = restOfLine;
  }
}
