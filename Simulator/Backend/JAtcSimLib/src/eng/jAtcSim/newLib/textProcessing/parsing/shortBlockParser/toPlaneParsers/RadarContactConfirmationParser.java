package eng.jAtcSim.newLib.area.textProcessing.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.area.speaking.fromAtc.notifications.RadarContactConfirmationNotification;
import eng.jAtcSim.newLib.area.textProcessing.parsing.shortBlockParser.SpeechParser;

public class RadarContactConfirmationParser extends SpeechParser<RadarContactConfirmationNotification> {

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
