package eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.speeches.atc.user2atc.PlaneSwitchRequest;
import eng.jAtcSim.newLib.speeches.atc.user2atc.PlaneSwitchRequestCancelation;
import eng.jAtcSim.newLib.textProcessing.old.text.ITextParser;
import eng.jAtcSim.newLib.textProcessing.parsing.shortcuts.ShortcutList;
import eng.jAtcSim.newLib.textProcessing.parsing.textParsing.SpeechParser;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class PlaneSwitchRequestCancelationParser extends SpeechParser<PlaneSwitchRequestCancelation> {

  private static final String[][] patterns = {
      {"disreg","\\d{4}"}
  };

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
        "Cancels plane switch (hang-off) request (plane is selected by squawk code)",
        "disreg {sqwk} - planes squawk code",
        "Cancels the misplaced ATC hang-off request.",
        "-disreg 1234");
    return ret;
  }

  @Override
  public PlaneSwitchRequestCancelation parse(IList<String> blocks) {
    String sqwk = blocks.get(1);
    return new PlaneSwitchRequestCancelation(Squawk.create(sqwk));
  }
}
