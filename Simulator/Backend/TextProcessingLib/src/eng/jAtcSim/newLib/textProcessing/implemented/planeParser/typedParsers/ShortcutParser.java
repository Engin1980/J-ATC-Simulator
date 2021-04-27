package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ProceedDirectCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ShortcutCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class ShortcutParser extends TextSpeechParser<ShortcutCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "SH (\\S+)");

  public String getHelp() {
    String ret = super.buildHelpString(
        "Shortcut command",
        "SH {fixName}",
        "Gives the airplane shortcut to fix on its route.",
        "SH KENOK");
    return ret;
  }

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public ShortcutCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    String ns = groups.getString(1);
    ShortcutCommand ret = ShortcutCommand.create(ns);
    return ret;
  }
}
