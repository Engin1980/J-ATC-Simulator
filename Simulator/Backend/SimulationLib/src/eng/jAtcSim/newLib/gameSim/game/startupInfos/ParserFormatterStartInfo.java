package eng.jAtcSim.newLib.gameSim.game.startupInfos;

import eng.jAtcSim.newLib.textProcessing.formatting.IAtcFormatter;
import eng.jAtcSim.newLib.textProcessing.formatting.IPlaneFormatter;
import eng.jAtcSim.newLib.textProcessing.formatting.ISystemFormatter;
import eng.jAtcSim.newLib.textProcessing.parsing.IAtcParser;
import eng.jAtcSim.newLib.textProcessing.parsing.IPlaneParser;
import eng.jAtcSim.newLib.textProcessing.parsing.ISystemParser;

public class ParserFormatterStartInfo {
  public static class Parsers {
    public final IPlaneParser planeParser;
    public final IAtcParser atcParser;
    public final ISystemParser systemParser;

    public Parsers(IPlaneParser planeParser, IAtcParser atcParser, ISystemParser systemParser) {
      this.planeParser = planeParser;
      this.atcParser = atcParser;
      this.systemParser = systemParser;
    }
  }

  public static class Formatters<T> {
    public final IPlaneFormatter<T> planeFormatter;
    public final IAtcFormatter<T> atcFormatter;
    public final ISystemFormatter<T> systemFormatter;

    public Formatters(IPlaneFormatter<T> planeFormatter, IAtcFormatter<T> atcFormatter, ISystemFormatter<T> systemFormatter) {
      this.planeFormatter = planeFormatter;
      this.atcFormatter = atcFormatter;
      this.systemFormatter = systemFormatter;
    }
  }

  public final Parsers parsers;
  public final Formatters<?> formatters;

  public ParserFormatterStartInfo(Parsers parsers, Formatters<?> formatters) {
    this.parsers = parsers;
    this.formatters = formatters;
  }
}
