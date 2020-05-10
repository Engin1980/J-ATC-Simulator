package eng.jAtcSim.newLib.textProcessing.implemented.systemParser;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.system.ISystemSpeech;
import eng.jAtcSim.newLib.speeches.system.ISystemUserRequest;
import eng.jAtcSim.newLib.textProcessing.IWithHelp;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextParsing;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParserList;
import eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser.MetarRequestParser;
import eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser.ShortcutRequestParser;
import eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser.TickSpeedRequestParser;
import eng.jAtcSim.newLib.textProcessing.parsing.EInvalidCommandException;
import eng.jAtcSim.newLib.textProcessing.parsing.ISystemParser;

public class SystemParser implements ISystemParser, IWithHelp {

  private static final TextSpeechParserList<ISystemUserRequest> systemParsers;

  static {
    systemParsers = new TextSpeechParserList<>();
    systemParsers.add(new MetarRequestParser());
    systemParsers.add(new ShortcutRequestParser());
    systemParsers.add(new TickSpeedRequestParser());
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
    return tmp.length() == 0 || tmp.charAt(0) == '?';
  }

  @Override
  public String getHelp() {
    return systemParsers.getHelp();
  }

  @Override
  public String getHelp(Object cmd) {
    return systemParsers.getHelp((String) cmd);
  }

  @Override
  public ISystemSpeech parse(Object input) {
    String line = (String) input;
    IList<String> tokens = TextParsing.tokenize(line);

    IList<String> toDo = new EList<>(tokens);
    IList<String> done = new EList<>();

    TextSpeechParser<? extends ISystemUserRequest> p = systemParsers.get(toDo);

    if (p == null)
      throw new EInvalidCommandException("Failed to parseOld command prefix.",
          TextParsing.toLineString(done),
          TextParsing.toLineString(toDo));

    IList<String> used;
    try {
      used = TextParsing.getInterestingBlocks(toDo, done, p);
    } catch (Exception ex) {
      throw new EInvalidCommandException("Failed to parseOld command via parser " + p.getClass().getName() + ". Probably invalid syntax?.",
          TextParsing.toLineString(done),
          TextParsing.toLineString(toDo));
    }
    if (used == null) {
      throw new EInvalidCommandException("Failed to parseOld command via parser " + p.getClass().getName() + ". Probably invalid syntax?.",
          TextParsing.toLineString(done),
          TextParsing.toLineString(toDo));
    }

    ISystemUserRequest ret = p.parse(used);
    return ret;
  }
}
