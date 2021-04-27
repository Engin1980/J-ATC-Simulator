package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.UnexpectedValueException;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.AltitudeRestrictionCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class AltitudeRestrictionCommandParser extends TextSpeechParser<AltitudeRestrictionCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "AC",
          "AM (\\d{1,3})",
          "AL (\\d{1,3})",
          "AE (\\d{1,3})");

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
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public AltitudeRestrictionCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    AltitudeRestrictionCommand ret;
    if (patternIndex == 0) {
      ret = AltitudeRestrictionCommand.createClearRestriction();
    } else {
      int val = groups.getInt(1);
      AboveBelowExactly res;
      switch (patternIndex) {
        case 1:
          res = AboveBelowExactly.above;
          break;
        case 2:
          res = AboveBelowExactly.below;
          break;
        case 3:
          res = AboveBelowExactly.exactly;
          break;
        default:
          throw new UnexpectedValueException(patternIndex);
      }
      ret = AltitudeRestrictionCommand.create(res, val);
    }

    return ret;
  }

}
