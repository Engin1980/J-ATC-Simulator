package eng.jAtcSim.newLib.simulation.internal;

import eng.eSystem.Producer;
import eng.jAtcSim.newLib.simulation.TimerProvider;
import eng.jAtcSim.newLib.textProcessing.base.Parser;

public class InternalAcc {
  private static Producer<TimerProvider> timerProviderProducer;
  private static Producer<Parser> parserProducer;

  public static Parser getParser() {
    return parserProducer.produce();
  }

  public static TimerProvider getTimerProvider() {
    return timerProviderProducer.produce();
  }

  public static void setParserProducer(Producer<Parser> parserProducer) {
    InternalAcc.parserProducer = parserProducer;
  }

  public static void setTimerProviderProducer(Producer<TimerProvider> timerProviderProducer) {
    InternalAcc.timerProviderProducer = timerProviderProducer;
  }
}
