package eng.jAtcSim.newLib.area.textProcessing.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.area.exceptions.EInvalidCommandException;
import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.afters.AfterNavaidCommand;
import eng.jAtcSim.newLib.area.textProcessing.parsing.shortBlockParser.SpeechParser;
import eng.jAtcSim.newLib.world.Navaid;

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
