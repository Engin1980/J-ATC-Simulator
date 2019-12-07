package eng.jAtcSim.newLib.textProcessing.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.exceptions.EInvalidCommandException;
import eng.jAtcSim.newLib.speaking.fromAtc.commands.ShortcutCommand;
import eng.jAtcSim.newLib.textProcessing.parsing.shortBlockParser.SpeechParser;
import eng.jAtcSim.newLib.world.Navaid;

public class ShortcutParser extends SpeechParser<ShortcutCommand> {

  private static final String [][]patterns = {{"SH","\\S+"}};
  public String getHelp() {
    String ret = super.buildHelpString(
        "Shortcut command",
        "SH {fixName}",
        "Gives the airplane shortcut to fix on its route.",
        "SH KENOK");
    return ret;
  }
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
