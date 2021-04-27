package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.UnexpectedValueException;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeSpeedCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class ChangeSpeedParser extends TextSpeechParser<ChangeSpeedCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "SC",
          "SM (\\d{1,3})",
          "SL (\\d{1,3})",
          "SE (\\d{1,3})");

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
            "Change speed",
            "SC - Cancel current speed restriction\n" +
                    "SM {speed} - Speed more or equal to value\n" +
                    "SL {speed} - Speed less or equal to value\n" +
                    "SE {speed} - Speed exactly",
            "Changes airplane speed",
            "SC\n" +
                    "SM 200\n" +
                    "SL 200\n" +
                    "SE 200");
    return ret;
  }

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public ChangeSpeedCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    ChangeSpeedCommand ret;
    if (patternIndex == 0) {
      ret = ChangeSpeedCommand.createResumeOwnSpeed();
    } else {
      int h = groups.getInt(1);
      AboveBelowExactly d;
      switch (patternIndex) {
        case 0:
          d = AboveBelowExactly.above;
          break;
        case 1:
          d = AboveBelowExactly.below;
          break;
        case 2:
          d = AboveBelowExactly.exactly;
          break;
        default:
          throw new UnexpectedValueException(patternIndex);
      }
      ret = ChangeSpeedCommand.create(d, h);
    }
    return ret;
  }

}
