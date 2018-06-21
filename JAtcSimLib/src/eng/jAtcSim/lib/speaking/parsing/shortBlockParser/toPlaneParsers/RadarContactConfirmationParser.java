package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

public class RadarContactConfirmationParser extends SpeechParser<RadarContactConfirmationNotification> {

  private static final String [][]patterns = {{"RC"}};

  @Override
  public String [][]getPatterns() {
    return patterns;
  }

  @Override
  public RadarContactConfirmationNotification parse(IList<String> blocks) {
    RadarContactConfirmationNotification ret = new RadarContactConfirmationNotification();
    return ret;
  }
}
