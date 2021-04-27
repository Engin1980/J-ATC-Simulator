package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeAltitudeCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class ChangeAltitudeParser extends TextSpeechParser<ChangeAltitudeCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "MA (\\d{1,3})",
          "CM (\\d{1,3})",
          "DM (\\d{1,3})");

  @Override
  public IReadOnlyList<String> getPatterns() {
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
  public ChangeAltitudeCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    ChangeAltitudeCommand ret;
    ChangeAltitudeCommand.eDirection d;
    int a;

    switch (patternIndex) {
      case 0:
        d = ChangeAltitudeCommand.eDirection.any;
        break;
      case 1:
        d = ChangeAltitudeCommand.eDirection.climb;
        break;
      case 2:
        d = ChangeAltitudeCommand.eDirection.descend;
        break;
      default:
        throw new UnsupportedOperationException("Invalid prefix for Maintain-altitude command.");
    }

    a = groups.getInt(1) * 100;

    ret = ChangeAltitudeCommand.create(d, a);
    return ret;
  }
}
