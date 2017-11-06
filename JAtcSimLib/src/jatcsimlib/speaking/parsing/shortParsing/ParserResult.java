package jatcsimlib.speaking.parsing.shortParsing;

import jatcsimlib.speaking.ICommand;

class ParserResult {

  public final ICommand command;
  public final String restOfLine;

  public ParserResult(ICommand command, String restOfLine) {
    this.command = command;
    this.restOfLine = restOfLine;
  }
}
