package eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.UnexpectedValueException;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.speeches.system.user2system.ShortcutRequest;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class ShortcutRequestParser extends TextSpeechParser<ShortcutRequest> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "SHORTCUT SET ([A-Z0-9]+) (.+)",
          "SHORTCUT DEL ([A-Z0-9]+)",
          "SHORTCUT");


  @Override
  public String getHelp() {
    return super.buildHelpString(
            "Shortcut management",
            "SHORTCUT - print all shortcuts\n" +
                    "SHORTCUT SET {template} {replacement commands} - defines a new shortcut replacing template with replacement (overwrites if exists)\n" +
                    "SHORTCUT DEL {template} - deletes old shortcut (if exists)",
            "Defines shortcuts, which can be used instead of standard commands and are expanded before execution",
            "-SHORTCUT - prints all shortcuts\n" +
                    "-SHORTCUT SET SCI DM 50 TR 240 T C I 24 - defines command SCI as a sequence of DM 50 TR 240 T C I 24\n" +
                    "-SHORTCUT DEL SCI - deletes SCI shortcut"
    );
  }

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public ShortcutRequest parse(int patternIndex, RegexUtils.RegexGroups groups) {
    ShortcutRequest ret;

    switch (patternIndex) {
      case 2:
        ret = ShortcutRequest.createGet();
        break;
      case 1:
        ret = ShortcutRequest.createDelete(groups.getString(1));
        break;
      case 0:
        ret = ShortcutRequest.createSet(
                groups.getString(1), groups.getString(2));
        break;
      default:
        throw new UnexpectedValueException(patternIndex);
    }
    return ret;
  }
}
