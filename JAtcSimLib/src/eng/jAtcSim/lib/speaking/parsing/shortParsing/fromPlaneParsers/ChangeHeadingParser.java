package eng.jAtcSim.lib.speaking.parsing.shortParsing.fromPlaneParsers;

import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;

public class ChangeHeadingParser extends SpeechParser<ChangeHeadingCommand> {

  private static final String[] prefixes = new String[]{"FH", "TR", "TL"};
  private static final String pattern = "((FH)|(TR)|(TL)) ?(\\d{1,3})";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public ChangeHeadingCommand parse(RegexGrouper rg) {
    ChangeHeadingCommand.eDirection d;
    switch (rg.getString(1)) {
      case "FH":
        d = ChangeHeadingCommand.eDirection.any;
        break;
      case "TL":
        d = ChangeHeadingCommand.eDirection.left;
        break;
      case "TR":
        d = ChangeHeadingCommand.eDirection.right;
        break;
      default:
        throw new ENotSupportedException();
    }
    ChangeHeadingCommand ret;

    if (rg.getString(5) == null) {
      ret = new ChangeHeadingCommand();
    } else {
      int h = rg.getInt(5);
      ret = new ChangeHeadingCommand(h, d);
    }
    return ret;
  }

}
