package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

public class ChangeAltitudeParser extends SpeechParser<ChangeAltitudeCommand> {

  private static final String[][] patterns = {{"MA|CM|DM", "\\d{1,3}"}};

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public ChangeAltitudeCommand parse(IList<String> blocks) {
    ChangeAltitudeCommand ret;
    ChangeAltitudeCommand.eDirection d;
    int a;

    switch (blocks.get(0)) {
      case "MA":
        d = ChangeAltitudeCommand.eDirection.any;
        break;
      case "CM":
        d = ChangeAltitudeCommand.eDirection.climb;
        break;
      case "DM":
        d = ChangeAltitudeCommand.eDirection.descend;
        break;
      default:
        throw new UnsupportedOperationException("Invalid prefix for Maintain-altitude command.");
    }

    a = super.getInt(blocks, 1) *100;

    ret = new ChangeAltitudeCommand(d, a);
    return ret;
  }
}
