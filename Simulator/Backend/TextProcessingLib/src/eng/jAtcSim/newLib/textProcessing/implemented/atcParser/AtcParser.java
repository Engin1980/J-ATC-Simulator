package eng.jAtcSim.newLib.textProcessing.implemented.atcParser;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.textProcessing.IWithHelp;
import eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers.PlaneSwitchRequestCancelationParser;
import eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers.PlaneSwitchRequestParser;
import eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers.RunwayInUseRequestParser;
import eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers.RunwayMaintenanceRequestParser;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextParsing;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParserList;
import eng.jAtcSim.newLib.textProcessing.parsing.EInvalidCommandException;
import eng.jAtcSim.newLib.textProcessing.parsing.IAtcParser;
import eng.jAtcSim.newLib.textProcessing.parsing.shortcuts.IWithShortcuts;
import eng.jAtcSim.newLib.textProcessing.parsing.shortcuts.ShortcutList;

public class AtcParser implements IAtcParser, IWithShortcuts<String>, IWithHelp {

  private static final TextSpeechParserList<IAtcSpeech> atcParsers;
  private final ShortcutList<String> shortcuts = new ShortcutList<>();

  static {
    atcParsers = new TextSpeechParserList<>();
    atcParsers.add(new RunwayMaintenanceRequestParser());
    atcParsers.add(new RunwayInUseRequestParser());
    atcParsers.add(new PlaneSwitchRequestCancelationParser());
    atcParsers.add(new PlaneSwitchRequestParser());
  }

  @Override
  public boolean acceptsType(Class<?> type) {
    return String.class.equals(type);
  }

  @Override
  public boolean canAccept(Object input) {
    if (input == null) return true;
    String tmp = (String) input;
    tmp = tmp.trim();
    return (tmp.length() == 0 || tmp.charAt(0) == '+' || tmp.charAt(0) == '-');
  }

  @Override
  public String getHelp() {
    return atcParsers.getHelp();
  }

  @Override
  public String getHelp(Object cmd) {
    String tag = (String) cmd;
    return atcParsers.getHelp(tag);
  }

  @Override
  public ShortcutList<String> getShortcuts() {
    return shortcuts;
  }

  @Override
  public IAtcSpeech parse(Object input) {
    String line = (String) input;
    line = line.trim();
    IList<String> toDo = TextParsing.tokenize(line);
    IList<String> done = new EList<>();

    TextSpeechParser<? extends IAtcSpeech> p = atcParsers.get(toDo);

    if (p == null)
      throw new EInvalidCommandException("Failed to parseOld atc message prefix.",
          TextParsing.toLineString(toDo),
          TextParsing.toLineString(done));

    IList<String> used;
    try {
      used = TextParsing.getInterestingBlocks(toDo, done, p);
    } catch (Exception ex) {
      throw new EInvalidCommandException("Failed to parseOld command via parser " + p.getClass().getName() + ". Probably invalid syntax?.",
          TextParsing.toLineString(toDo),
          TextParsing.toLineString(done));
    }

    IAtcSpeech ret = p.parse(used);
    return ret;
  }

}
