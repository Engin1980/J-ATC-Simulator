package eng.jAtcSim.newLib.textProcessing.implemented.systemParser;

import eng.eSystem.Triple;
import eng.eSystem.collections.ISet;
import eng.eSystem.utilites.RegexUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.speeches.system.ISystemSpeech;
import eng.jAtcSim.newLib.speeches.system.ISystemUserRequest;
import eng.jAtcSim.newLib.textProcessing.IWithHelp;
import eng.jAtcSim.newLib.textProcessing.implemented.ParsingProviderUtils;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParserList;
import eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser.*;
import eng.jAtcSim.newLib.textProcessing.parsing.EInvalidCommandException;
import eng.jAtcSim.newLib.textProcessing.parsing.ISystemParsingProvider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemParsingProvider implements ISystemParsingProvider, IWithHelp {

  private static final TextSpeechParserList<ISystemUserRequest> systemParsers;

  static {
    systemParsers = new TextSpeechParserList<>();
    systemParsers.add(new MetarRequestParser());
    systemParsers.add(new ShortcutRequestParser());
    systemParsers.add(new TickSpeedRequestParser());
    systemParsers.add(new DeletePlaneRequestParser());
    systemParsers.add(new GetHelpRequestParser());
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
    StringBuilder todo = new StringBuilder();
    StringBuilder done = new StringBuilder();
    ParsingProviderUtils.prepareStringBuilders(input, todo, done);

    ISet<TextSpeechParser<? extends ISystemUserRequest>> parsers = systemParsers.getAllByPatterns(todo.toString());
    if (parsers.size() == 0)
      throw new EInvalidCommandException("Failed to parse system command.",
              done.toString(), todo.toString());
    else if (parsers.size() > 1)
      throw new EInvalidCommandException("There are multiple ways to parse system command (probably internal error?).",
              done.toString(), todo.toString());

    ISystemSpeech ret = ParsingProviderUtils.parseWithParser(parsers.getFirst(), todo, done);
    return ret;
  }
}
