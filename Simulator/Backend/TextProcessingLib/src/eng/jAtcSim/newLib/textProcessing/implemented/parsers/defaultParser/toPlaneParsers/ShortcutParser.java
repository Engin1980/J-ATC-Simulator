package eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.atc2airplane.ShortcutCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.common.SpeechParser;

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
    ShortcutCommand ret = new ShortcutCommand(ns);
    return ret;
  }
}