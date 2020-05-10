package eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser;

import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.newLib.speeches.system.user2system.TickSpeedRequest;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class TickSpeedRequestParser extends TextSpeechParser<TickSpeedRequest> {
  private static final String[][] patterns = {
      {"TICK", "\\d{3,}"},
      {"TICK"}};

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
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public TickSpeedRequest parse(IList<String> blocks) {
    TickSpeedRequest ret;
    if (blocks.size() == 1)
      ret = TickSpeedRequest.createGet();
    else {
      String intervalString = blocks.get(1);
      int intervalInt;
      try {
        intervalInt = Integer.parseInt(intervalString);
      } catch (Exception ex) {
        throw new EApplicationException(sf(
            "Unable to create 'TickSpeedRequest', the value '%s' cannot be converted to miliseconds integer.",
            intervalString));
      }
      ret = TickSpeedRequest.createSet(intervalInt);
    }
    return ret;
  }
}
