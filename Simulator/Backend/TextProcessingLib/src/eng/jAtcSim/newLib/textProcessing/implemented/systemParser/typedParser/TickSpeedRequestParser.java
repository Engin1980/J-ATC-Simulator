package eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.UnexpectedValueException;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.speeches.system.user2system.TickSpeedRequest;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class TickSpeedRequestParser extends TextSpeechParser<TickSpeedRequest> {
  private static final IReadOnlyList<String> patterns = EList.of(
          "TICK (\\d{1,})",
          "TICK");


  @Override
  public String getHelp() {
    return super.buildHelpString(
            "TICK",
            "TICK - returns the current second interval in miliseconds\n" +
                    "TICK {interval} - sets the tick interval in seconds.",
            "Gets or changes the length of second interval of the simulation.",
            "-TICK\n-TICK 400\n-TICK 1000");
  }

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public TickSpeedRequest parse(int patternIndex, RegexUtils.RegexGroups groups) {
    TickSpeedRequest ret;
    switch (patternIndex) {
      case 0:
        int interval = groups.getInt(1);
        ret = TickSpeedRequest.createSet(interval);
        break;
      case 1:
        ret = TickSpeedRequest.createGet();
        break;
      default:
        throw new UnexpectedValueException(patternIndex);
    }
    return ret;
  }
}
