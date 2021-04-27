package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.UnexpectedValueException;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.HoldCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class HoldParser extends TextSpeechParser<HoldCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "H (\\S{1,5}) (\\d{3}) ([RL])",
          "H (\\S{1,5})");

  public String getHelp() {
    String ret = super.buildHelpString(
            "Hold",
            "H {fixName} - for published hold\n" +
                    "H {fixName} {inboundRadial} {L/R} - for custom hold. L=left turns, R=right turns",
            "Hold over specified fix. When short version used, hold must be published.\nOtherwise hold parameters must be specified.\nFix can be also specified using fix/radial/distance format.",
            "H ERASU\nH SIGMA 040 R\nH SIGMA/030/20.5 250 R");
    return ret;
  }

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public HoldCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {

    HoldCommand ret;
    String ns = groups.getString(1);

    switch (patternIndex) {
      case 0:
        int hdg = groups.getInt(2);
        LeftRight lr = groups.getChar(3) == 'L' ? LeftRight.left : LeftRight.right;
        ret = HoldCommand.createExplicit(ns, hdg, lr);
        break;
      case 1:
        ret = HoldCommand.createPublished(ns);
        break;
      default:
        throw new UnexpectedValueException(patternIndex);
    }

    return ret;

  }
}
