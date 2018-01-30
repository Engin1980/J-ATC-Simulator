package eng.jAtcSim.lib.speaking.parsing.shortParsing;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.exceptions.EInvalidCommandException;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;
import eng.jAtcSim.lib.world.Navaid;
import jatcsimlib.Acc;
import jatcsimlib.exceptions.EInvalidCommandException;
import jatcsimlib.speaking.ICommand;
import jatcsimlib.speaking.fromAtc.commands.ProceedDirectCommand;
import jatcsimlib.world.Navaid;

class ProceedDirectParser extends SpeechParser<ProceedDirectCommand> {

  private static final String[] prefixes = new String[]{"PD"};
  private static final String pattern = "PD (\\S+)";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  ProceedDirectCommand parse(RegexGrouper rg) {
    String ns = rg.getString(1);

    Navaid n = Acc.area().getNavaids().tryGet(ns);
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", rg.getMatch());
    }
    ProceedDirectCommand ret = new ProceedDirectCommand(n);
    return ret;
  }
}
