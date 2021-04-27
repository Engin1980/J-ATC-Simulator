package eng.jAtcSim.newLib.gameSim.simulation;

import eng.eSystem.functionalInterfaces.Producer;
import eng.jAtcSim.newLib.gameSim.simulation.modules.TimerModule;
import eng.jAtcSim.newLib.textProcessing.parsing.IParsingProvider;

public class InternalAcc {
  private static Producer<TimerModule> timerProviderProducer;
  private static Producer<IParsingProvider> parserProducer;

  public static IParsingProvider getParser() {
    return parserProducer.invoke();
  }

  public static TimerModule getTimerProvider() {
    return timerProviderProducer.invoke();
  }

  public static void setParserProducer(Producer<IParsingProvider> parserProducer) {
    InternalAcc.parserProducer = parserProducer;
  }

  public static void setTimerProviderProducer(Producer<TimerModule> timerProviderProducer) {
    InternalAcc.timerProviderProducer = timerProviderProducer;
  }
}
