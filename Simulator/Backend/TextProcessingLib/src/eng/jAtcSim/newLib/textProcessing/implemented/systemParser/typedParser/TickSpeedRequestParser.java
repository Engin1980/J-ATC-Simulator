package eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.speeches.system.user2system.TickSpeedRequest;
import eng.jAtcSim.newLib.textProcessing.parsing.textParsing.SpeechParser;

import java.util.regex.Pattern;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class TickSpeedRequestParser extends SpeechParser<TickSpeedRequest> {
  private static final Pattern SYSMES_CHANGE_SPEED = Pattern.compile("TICK (\\d+)");

  private static final String[][] patterns =
      {{"TICK", "\\d{3,}"}};

  @Override
  public String[][] getPatterns() {
    return patterns;

  @Override
  public String getHelp() {
    return null;
  }

  @Override
  public TickSpeedRequest parse(IList<String> blocks) {
    return null;
  }
}
