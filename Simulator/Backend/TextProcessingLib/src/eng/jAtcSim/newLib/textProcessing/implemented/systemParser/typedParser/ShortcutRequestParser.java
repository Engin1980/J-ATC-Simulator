package eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.system.user2system.ShortcutRequest;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class ShortcutRequestParser extends TextSpeechParser<ShortcutRequest> {
  private static final String[][] patterns = {
      {"SHORTCUT", "SET", "[A-Z0-9]+", ".+"},
      {"SHORTCUT", "DEL", "[A-Z0-9]+"},
      {"SHORTCUT"}
  };

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
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public ShortcutRequest parse(IList<String> blocks) {
    ShortcutRequest ret;
    if (blocks.count() == 1)
      ret = ShortcutRequest.createGet();
    else if (blocks.get(1).equals("DEL"))
      ret = ShortcutRequest.createDelete(blocks.get(2));
    else if (blocks.get(1).equals("SET"))
      ret = ShortcutRequest.createSet(blocks.get(2), blocks.get(3));
    else
      throw new UnsupportedOperationException();
    return ret;
  }
}
