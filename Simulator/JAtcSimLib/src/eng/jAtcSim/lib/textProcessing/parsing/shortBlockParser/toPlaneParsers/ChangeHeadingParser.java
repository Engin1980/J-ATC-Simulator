package eng.jAtcSim.lib.textProcessing.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.textProcessing.parsing.shortBlockParser.SpeechParser;

public class ChangeHeadingParser extends SpeechParser<ChangeHeadingCommand> {

  private static final String[][] patterns = {
      {"TL", "\\d{1,3}?"},
      {"TR", "\\d{1,3}?"},
      {"FH", "\\d{1,3}?"},
      {"FH"}
  };

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
        "Change heading",
        "TL {heading} - Turn left to heading\n" +
            "TR {heading} - Turn right to heading\n" +
            "FH {heading} - Fly heading (nearest tun) to heading\n" +
            "FH - Continue on current heading (when no heading is specified)",
        "Changes airplane heading",
        "TL 120\n" + "TR 120\n" + "FH 120\n" + "FH");
    return ret;
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
      int h = super.getInt(blocks, 1);
      ret = new ChangeHeadingCommand(h, d);
    }
    return ret;
  }

}
