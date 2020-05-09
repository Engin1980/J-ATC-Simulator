package eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.speeches.system.user2system.ShortcutRequest;
import eng.jAtcSim.newLib.textProcessing.parsing.textParsing.SpeechParser;

public class ShortcutRequestParser extends SpeechParser<ShortcutRequest> {
  private static final String[][] patterns =
      {{"TICK", "\\d{3,}"}};

  @Override
  public String getHelp() {
    return super.buildHelpString(
        "Shortcut management",
        "SHORTCUT - print all shortcuts\n" +
            "SHORTCUT ADD {template} {replacement commands} - defines a new shortcut replacing template with replacement (overwrites if exists)\n" +
            "SHORTCUT DEL {template} - deletes old shorcut (if exists)",
        "Defines shortcuts, which can be used instead of standard commands and are expanded before execution",
        "-SHORTCUT - prints all shortcuts\n" +
            "-SHORTCUT ADD SCI DM 50 TR 240 T C I 24 - defines command SCI as a sequence of DM 50 TR 240 T C I 24\n" +
            "-SHORTCUT DEL SCI - deletes SCI shortcut"
    );
  }

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public ShortcutRequest parse(IList<String> blocks) {
    //TODO Implement this:
    throw new ToDoException("");
  }
}
