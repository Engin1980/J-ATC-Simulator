package eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.atc2airplane.AltitudeRestrictionCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.common.SpeechParser;

public class AltitudeRestrictionCommandParser extends SpeechParser<AltitudeRestrictionCommand> {

  private static final String[][] patterns = {
      {"AC"},
      {"AM", "\\d{1,3}"},
      {"AL", "\\d{1,3}"},
      {"AE", "\\d{1,3}"}
  };

  public String getHelp() {
    String ret = super.buildHelpString(
        "Altitude restrictions",
        "AC - Clears current altitude restriction\n" +
            "AM {altitude} - Sets upper altitude restriction (at most)\n" +
            "AL {altitude} - Sets lower altitude restriction (at least)\n" +
            "AE {altitude} - Sets exact altitude restriction (exactly)",
        "Sets/clears altitude restrictions. Supposed for SID/STARS definitions only.",
        "AC\n" +
            "AM 050\n" +
            "AL 40\n" +
            "AE 120");
    return ret;
  }

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public AltitudeRestrictionCommand parse(IList<String> blocks) {
    AltitudeRestrictionCommand ret;
    if (blocks.size() == 1)
      ret = AltitudeRestrictionCommand.createClearRestriction();
    else {
      AboveBelowExactly res;
      int val = super.getInt(blocks, 1) * 100;
      switch (blocks.get(0).charAt(1)) {
        case 'M':
          res = AboveBelowExactly.above;
          break;
        case 'L':
          res = AboveBelowExactly.below;
          break;
        case 'E':
          res = AboveBelowExactly.exactly;
          break;
        default:
          throw new UnsupportedOperationException();
      }
      ret = AltitudeRestrictionCommand.create(res, val);
    }

    return ret;
  }
}
