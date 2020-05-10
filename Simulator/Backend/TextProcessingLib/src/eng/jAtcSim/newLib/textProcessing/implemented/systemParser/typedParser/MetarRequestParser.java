package eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.speeches.system.user2system.MetarRequest;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class MetarRequestParser extends TextSpeechParser<MetarRequest> {
  private static final String[][] patterns =
      {{"METAR"}};

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public String getHelp() {
    return super.buildHelpString(
        "METAR",
        "METAR",
        "Returns current weather/metar",
        "-METAR"
    );
  }

  @Override
  public MetarRequest parse(IList<String> blocks) {
    return new MetarRequest();
  }
}
