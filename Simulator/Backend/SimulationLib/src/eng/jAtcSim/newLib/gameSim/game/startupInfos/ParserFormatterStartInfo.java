package eng.jAtcSim.newLib.gameSim.game.startupInfos;

import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IPlaneSpeech;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.speeches.base.ISpeech;
import eng.jAtcSim.newLib.speeches.system.ISystemNotification;
import eng.jAtcSim.newLib.textProcessing.formatting.IAtcFormatter;
import eng.jAtcSim.newLib.textProcessing.formatting.IPlaneFormatter;
import eng.jAtcSim.newLib.textProcessing.formatting.ISystemFormatter;
import eng.jAtcSim.newLib.textProcessing.parsing.IAtcParser;
import eng.jAtcSim.newLib.textProcessing.parsing.IPlaneParser;
import eng.jAtcSim.newLib.textProcessing.parsing.ISystemParser;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ParserFormatterStartInfo {

  public static class Parsers {
    public final IAtcParser atcParser;
    public final IPlaneParser planeParser;
    public final ISystemParser systemParser;

    public Parsers(IPlaneParser planeParser, IAtcParser atcParser, ISystemParser systemParser) {
      this.planeParser = planeParser;
      this.atcParser = atcParser;
      this.systemParser = systemParser;
    }
  }

  public static class Formatters<T> {
    public final IAtcFormatter<T> atcFormatter;
    public final IPlaneFormatter<T> planeFormatter;
    public final ISystemFormatter<T> systemFormatter;

    public Formatters(IPlaneFormatter<T> planeFormatter, IAtcFormatter<T> atcFormatter, ISystemFormatter<T> systemFormatter) {
      this.planeFormatter = planeFormatter;
      this.atcFormatter = atcFormatter;
      this.systemFormatter = systemFormatter;
    }
  }

  public final Formatters<?> formatters;
  public final Parsers parsers;

  public ParserFormatterStartInfo(Parsers parsers, Formatters<?> formatters) {
    this.parsers = parsers;
    this.formatters = formatters;
  }
}
