package eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.speeches.system.user2system.MetarRequest;
import eng.jAtcSim.newLib.textProcessing.parsing.textParsing.SpeechParser;

import java.util.regex.Pattern;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class MetarRequestParser extends SpeechParser<MetarRequest> {
  private static final Pattern SYSMES_METAR = Pattern.compile("METAR");

  private static final String[][] patterns =
      {{"METAR"}};

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public String getHelp() {
    return null;
  }

  @Override
  public MetarRequest parse(IList<String> blocks) {
    return null;
  }
}
