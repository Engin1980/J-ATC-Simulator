package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

public class ChangeHeadingParser extends SpeechParser<ChangeHeadingCommand> {

  private static final String[][] patterns = {
      {"TL|TR|FH"},
      {"TL|TR|FH", "\\d{1,3}?"}};


  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public ChangeHeadingCommand parse(IList<String> blocks) {
    ChangeHeadingCommand.eDirection d;
    switch (blocks.get(0)) {
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
        throw new UnsupportedOperationException();
    }
    ChangeHeadingCommand ret;

    if (blocks.size() == 1) {
      ret = new ChangeHeadingCommand();
    } else {
      int h = super.getInt(blocks, 2);
      ret = new ChangeHeadingCommand(h, d);
    }
    return ret;
  }

}
