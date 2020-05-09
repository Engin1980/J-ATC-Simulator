package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.HoldCommand;
import eng.jAtcSim.newLib.textProcessing.parsing.textParsing.SpeechParser;

public class HoldParser extends SpeechParser<HoldCommand> {

  private static final String[][] patterns = {
      {"H", "\\S{1,5}", "\\d{3}", "R|L"},
      {"H", "\\S{1,5}"},
  };

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
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public HoldCommand parse(IList<String> rg) {
    HoldCommand ret;

    String ns = rg.get(1);
    if (rg.size() == 2) {
      ret = HoldCommand.createPublished(ns);
    } else {
      int heading = getInt(rg, 2);
      char leftOrRight = rg.get(3).charAt(0);

      LeftRight turn = leftOrRight == 'L' ? LeftRight.left : LeftRight.right;
      ret = HoldCommand.createExplicit(ns, heading, turn);
    }

    return ret;
  }
}
