package eng.jAtcSim.newLib.gameSim.simulation;

import eng.eSystem.functionalInterfaces.Producer;
import eng.jAtcSim.newLib.gameSim.simulation.modules.TimerModule;
import eng.jAtcSim.newLib.textProcessing.parsing.IParser;

public class InternalAcc {
  private static Producer<TimerModule> timerProviderProducer;
  private static Producer<IParser> parserProducer;

  public static IParser getParser() {
    return parserProducer.invoke();
  }

  public static TimerModule getTimerProvider() {
    return timerProviderProducer.invoke();
  }

  public static void setParserProducer(Producer<IParser> parserProducer) {
    InternalAcc.parserProducer = parserProducer;
  }

  public static void setTimerProviderProducer(Producer<TimerModule> timerProviderProducer) {
    InternalAcc.timerProviderProducer = timerProviderProducer;
  }
}
