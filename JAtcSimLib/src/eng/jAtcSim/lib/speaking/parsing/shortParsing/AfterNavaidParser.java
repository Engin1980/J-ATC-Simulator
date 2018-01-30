package eng.jAtcSim.lib.speaking.parsing.shortParsing;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.exceptions.EInvalidCommandException;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterNavaidCommand;
import eng.jAtcSim.lib.world.Navaid;
import jatcsimlib.Acc;
import jatcsimlib.exceptions.EInvalidCommandException;
import jatcsimlib.speaking.ICommand;
import jatcsimlib.speaking.fromAtc.commands.afters.AfterNavaidCommand;
import jatcsimlib.world.Navaid;

class AfterNavaidParser extends SpeechParser<AfterNavaidCommand> {

  private static final String[] prefixes = new String[]{"AN"};
  private static final String pattern = "AN (\\S+)";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  AfterNavaidCommand parse(RegexGrouper rg) {
    String ns = rg.getString(1);
    Navaid n = Acc.area().getNavaids().tryGet(ns);
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", rg.getMatch());
    }
    AfterNavaidCommand ret = new AfterNavaidCommand(n);
    return ret;
  }
}
