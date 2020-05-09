package eng.jAtcSim.newLib.textProcessing.implemented.atcParser;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.textProcessing.IWithHelp;
import eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers.PlaneSwitchRequestCancelationParser;
import eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers.PlaneSwitchRequestParser;
import eng.jAtcSim.newLib.textProcessing.old.base.EInvalidCommandException;
import eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers.RunwayMaintenanceRequestParser;
import eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers.RunwayInUseRequestParser;
import eng.jAtcSim.newLib.textProcessing.parsing.textParsing.SpeechParser;
import eng.jAtcSim.newLib.textProcessing.parsing.IAtcParser;
import eng.jAtcSim.newLib.textProcessing.parsing.shortcuts.IWithShortcuts;
import eng.jAtcSim.newLib.textProcessing.parsing.shortcuts.ShortcutList;

import java.util.Arrays;
import java.util.regex.Pattern;

public class AtcParser implements IAtcParser, IWithShortcuts<String>, IWithHelp {

  private static final IList<SpeechParser<? extends IAtcSpeech>> atcParsers;
  private final ShortcutList<String> shortcuts = new ShortcutList<>();

  private static SpeechParser<? extends IAtcSpeech> getAtcSpeechParser(
  IReadOnlyList<String> tokens) {
    SpeechParser<? extends IAtcSpeech> ret = getParserByPrefix(tokens.get(0));
    return ret;
  }

  private static SpeechParser<? extends IAtcSpeech> getParserByPrefix(String prefix) {
    for (SpeechParser<? extends IAtcSpeech> tmp : AtcParser.atcParsers) {
      for (String s : tmp.getPrefixes()) {
        Pattern p = Pattern.compile("^(" + s + ")$");
        if (p.matcher(prefix).find()) {
          return tmp;
        }
      }
    }
    return null;
  }

  static {
    atcParsers = new EList<>();
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
    EStringBuilder ret = new EStringBuilder();
    ret.appendLine("* ATC commands *");
    ret.appendLine("ATC commands must be prefixed by an atc mark -/+.");
    for (SpeechParser<? extends IAtcSpeech> parser : atcParsers) {
      String[] prefixes = parser.getPrefixes();
      ret.appendItems(Arrays.asList(prefixes), "/");
      ret.append(" - " + parser.getCommandName());
      ret.appendLine();
    }
    return ret.toString();
  }

  @Override
  public String getHelp(Object input) {
    String tag = (String) input;
    SpeechParser<? extends IAtcSpeech> p = getParserByPrefix(tag);
    if (p == null)
      return null;
    else
      return p.getHelp();
  }

  @Override
  public ShortcutList<String> getShortcuts() {
    return shortcuts;
  }

  @Override
  public IAtcSpeech parse(Object input) {
    String line = (String) input;
    line = line.trim();
    IList<String> toDo = tokenize(line);
    IList<String> done = new EList<>();

    SpeechParser<? extends IAtcSpeech> p = getAtcSpeechParser(toDo);

    if (p == null)
      throw new EInvalidCommandException("Failed to parseOld atc message prefix.",
          toLineString(toDo),
          toLineString(done));

    IList<String> used;
    try {
      used = getInterestingBlocks(toDo, done, p);
    } catch (Exception ex) {
      throw new EInvalidCommandException("Failed to parseOld command via parser " + p.getClass().getName() + ". Probably invalid syntax?.",
          toLineString(toDo),
          toLineString(done));
    }

    IAtcSpeech ret = p.parse(used);
    return ret;
  }

  private IList<String> getInterestingBlocks(IList<String> toDo, IList<String> done, SpeechParser<? extends IAtcSpeech> p) {
    IList<String> ret;
    int index = getMatchingPatternIndex(toDo, p);
    if (index < 0) {
      ret = null;
    } else {
      //moving pattern from "toDo" to "done" and to "ret"
      ret = new EList<>();
      for (int i = 0; i < p.getPatterns()[index].length; i++) {
        String s = toDo.get(0);
        toDo.removeAt(0);
        done.add(s);
        ret.add(s);
      }
    }
    return ret;
  }

  private int getMatchingPatternIndex(IList<String> toDo, SpeechParser<? extends IAtcSpeech> parser) {
    int ret = -1;

    for (int i = 0; i < parser.getPatterns().length; i++) {
      String[] pattern = parser.getPatterns()[i];

      if (pattern.length > toDo.size()) continue; // pattern longer than input

      ret = i;
      for (int j = 0; j < pattern.length; j++) {
        Pattern p = Pattern.compile("^(" + pattern[j] + ")$");
        String pt = toDo.get(j);
        if (p.matcher(pt).find() == false) { // some part of pattern does not fit
          ret = -1;
          break;
        }
      }

      if (ret != -1) break;
    }

    return ret;
  }

  private String toLineString(IReadOnlyList<String> processed) {
    EStringBuilder ret = new EStringBuilder();
    ret.appendItems(processed, " ");
    return ret.toString();
  }

  private IList<String> tokenize(String line) {
    IList<String> ret;
    if (line == null || line.isEmpty()) {
      return new EList<>();
    }
    line = line.toUpperCase();
    String[] pts = line.split(" ");
    ret = new EList<>(pts);
    ret.remove(q -> q.length() == 0);
    return ret;
  }

  private IList<String> tryExpandByShortcut(IReadOnlyList<String> tokens) {
    IList<String> ret;
    String firstWord = tokens.get(0);
    String exp = this.getShortcuts().tryGet(firstWord);
    if (exp == null)
      ret = null;
    else {
      IList<String> tmp = tokenize(exp);
      ret = new EList<>();
      ret.add(tmp);
      ret.add(tokens);
      ret.removeAt(tmp.size());
    }

    return ret;
  }
}
