package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.exceptions.EInvalidCommandException;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ShortcutCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;
import eng.jAtcSim.lib.world.Navaid;

public class ShortcutParser extends SpeechParser<ShortcutCommand> {

  private static final String [][]patterns = {{"SH","\\S+"}};

  @Override
  public String [][]getPatterns() {
    return patterns;
  }

  @Override
  public ShortcutCommand parse(IList<String> blocks) {
    String ns = blocks.get(1);

    Navaid n = Acc.area().getNavaids().tryGet(ns);
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", blocks.get(1));
    }
    ShortcutCommand ret = new ShortcutCommand(n);
    return ret;
  }
}
