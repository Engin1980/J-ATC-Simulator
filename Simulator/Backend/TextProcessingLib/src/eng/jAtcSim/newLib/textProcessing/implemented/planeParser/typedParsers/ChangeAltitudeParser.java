package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeAltitudeCommand;
import eng.jAtcSim.newLib.textProcessing.parsing.textParsing.SpeechParser;

public class ChangeAltitudeParser extends SpeechParser<ChangeAltitudeCommand> {

  private static final String[][] patterns = {
      {"MA", "\\d{1,3}"},
      {"CM", "\\d{1,3}"},
      {"DM", "\\d{1,3}"}
  };

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
        "Change altitude",
        "MA {altitude} - Maintain (climb or descend) to altitude\n" +
            "CM {altitude} - Climb to altitude\n" +
            "DM {altitude} - Descend to altitude",
        "When reaching speed",
        "MA 120\n" + "CM 120\n" + "DM 120");
    return ret;
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

    a = super.getInt(blocks, 1) * 100;

    ret = ChangeAltitudeCommand.create(d, a);
    return ret;
  }
}
