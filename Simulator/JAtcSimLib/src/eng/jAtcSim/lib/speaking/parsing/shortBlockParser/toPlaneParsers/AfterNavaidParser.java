package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.exceptions.EInvalidCommandException;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterNavaidCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;
import eng.jAtcSim.lib.world.Navaid;

import java.util.Arrays;

public class AfterNavaidParser extends SpeechParser<AfterNavaidCommand> {

  private static final String[][] patterns = {{"AN", "\\S+"}};

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
        "After navaid",
        "AN {fixName}",
        "When flying over fix",
        "AN KENOK");
    return ret;
  }

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public AfterNavaidCommand parse(IList<String> blocks) {
    String ns = blocks.get(1);
    Navaid n = Acc.area().getNavaids().tryGet(ns);
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", blocks.get(1));
    }
    AfterNavaidCommand ret = new AfterNavaidCommand(n);
    return ret;
  }
}
