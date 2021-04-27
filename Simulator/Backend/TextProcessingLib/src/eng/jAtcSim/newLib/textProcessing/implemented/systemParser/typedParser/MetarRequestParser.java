package eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.speeches.system.user2system.MetarRequest;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class MetarRequestParser extends TextSpeechParser<MetarRequest> {
  private static final IReadOnlyList<String> patterns = EList.of(
          "METAR");

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
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public MetarRequest parse(int patternIndex, RegexUtils.RegexGroups groups) {
    return new MetarRequest();
  }
}
