package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.RadarContactConfirmationNotification;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class RadarContactConfirmationParser extends TextSpeechParser<RadarContactConfirmationNotification> {

  private static final String[][] patterns = {{"RC"}};

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  public String getHelp() {
    String ret = super.buildHelpString(
        "Radar contact confirmation",
        "RC",
        "Informs airplane about radar contact. Without this confirmation the airplane will refuse all other commands.",
        "RC");
    return ret;
  }

  @Override
  public RadarContactConfirmationNotification parse(IList<String> blocks) {
    RadarContactConfirmationNotification ret = new RadarContactConfirmationNotification();
    return ret;
  }
}
