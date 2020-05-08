package eng.jAtcSim.newLib.textProcessing.implemented.atcTextParser;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.textProcessing.base.EInvalidCommandException;
import eng.jAtcSim.newLib.textProcessing.text.ShortcutList;
import eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.common.SpeechParser;
import eng.jAtcSim.newLib.textProcessing.implemented.atcTextParser.typedParsers.RunwayCheckParser;
import eng.jAtcSim.newLib.textProcessing.implemented.atcTextParser.typedParsers.RunwayUseParser;
import eng.jAtcSim.newLib.textProcessing.text.ITextParser;

import java.util.Arrays;
import java.util.regex.Pattern;

public class AtcTextParser implements ITextParser<IAtcSpeech> {

  private static final IList<SpeechParser> atcParsers;

  private static SpeechParser getAtcSpeechParser(IReadOnlyList<String> tokens) {
    SpeechParser ret = getParserByPrefix(tokens.get(0), atcParsers);
    return ret;
  }

  private static SpeechParser getParserByPrefix(String prefix, IList<SpeechParser> parsers) {
    for (SpeechParser tmp : parsers) {
      for (String s : tmp.getPrefixes()) {
        Pattern p = Pattern.compile("^(" + s + ")$");
        if (p.matcher(prefix).find()) {
          return tmp;
        }
      }
    }
    return null;
  }
  private ShortcutList shortcuts = new ShortcutList();

  static {
    atcParsers = new EList<>();
    atcParsers.add(new RunwayCheckParser());
    atcParsers.add(new RunwayUseParser());
  }

  @Override
  public String getHelp() {
    EStringBuilder ret = new EStringBuilder();
    ret.appendLine("* ATC commands *");
    ret.appendLine("ATC commands must be prefixed by an atc mark -/+.");
    for (SpeechParser parser : atcParsers) {
      String[] prefixes = parser.getPrefixes();
      ret.appendItems(Arrays.asList(prefixes), "/");
      ret.append(" - " + parser.getCommandName());
      ret.appendLine();
    }
    return ret.toString();
  }

  @Override
  public String getHelp(String commandPrefix) {
    SpeechParser p = getParserByPrefix(commandPrefix, atcParsers);
    if (p == null)
      return null;
    else
      return p.getHelp();
  }

  @Override
  public ShortcutList getShortcuts() {
    return shortcuts;
  }

  @Override
  public SpeechList<IAtcSpeech> parse(String line) {
    IList<String> toDo = tokenize(line);
    IList<String> done = new EList<>();

    SpeechParser p = getAtcSpeechParser(toDo);

    if (p == null)
      throw new EInvalidCommandException("Failed to parseOld atc message prefix.",
          toLineString(toDo),
          toLineString(done));

    IList<String> used = null;
    try {
      used = getInterestingBlocks(toDo, done, p);
    } catch (Exception ex) {
      throw new EInvalidCommandException("Failed to parseOld command via parser " + p.getClass().getName() + ". Probably invalid syntax?.",
          toLineString(toDo),
          toLineString(done));
    }

    IAtcSpeech tmp = (IAtcSpeech) p.parse(used);
    SpeechList<IAtcSpeech> ret = new SpeechList<>();
    ret.add(tmp);
    return ret;
  }

  private IList<String> getInterestingBlocks(IList<String> toDo, IList<String> done, SpeechParser p) {
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

  private int getMatchingPatternIndex(IList<String> toDo, SpeechParser parser) {
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
