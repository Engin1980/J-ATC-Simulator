package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.UnexpectedValueException;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.shared.enums.LeftRightAny;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeHeadingCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class ChangeHeadingParser extends TextSpeechParser<ChangeHeadingCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "FH",
          "FH (\\d{1,3})",
          "TL (\\d{1,3})",
          "TR (\\d{1,3})");

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
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public ChangeHeadingCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    ChangeHeadingCommand ret;
    if (patternIndex == 0) {
      ret = ChangeHeadingCommand.createContinueCurrentHeading();
    } else {
      int h = groups.getInt(1);
      LeftRightAny d;
      switch (patternIndex) {
        case 0:
          d = LeftRightAny.any;
          break;
        case 1:
          d = LeftRightAny.left;
          break;
        case 2:
          d = LeftRightAny.right;
          break;
        default:
          throw new UnexpectedValueException(patternIndex);
      }
      ret = ChangeHeadingCommand.create(h, d);
    }
    return ret;
  }

}
