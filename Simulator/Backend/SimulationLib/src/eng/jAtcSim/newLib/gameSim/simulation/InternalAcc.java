package eng.jAtcSim.newLib.gameSim.simulation;

import eng.eSystem.functionalInterfaces.Producer;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.TimerController;
import eng.jAtcSim.newLib.textProcessing.parsing.IParser;

public class InternalAcc {
  private static Producer<TimerController> timerProviderProducer;
  private static Producer<IParser> parserProducer;

  public static IParser getParser() {
    return parserProducer.produce();
  }

  public static TimerController getTimerProvider() {
    return timerProviderProducer.produce();
  }

  public static void setParserProducer(Producer<IParser> parserProducer) {
    InternalAcc.parserProducer = parserProducer;
  }

  public static void setTimerProviderProducer(Producer<TimerController> timerProviderProducer) {
    InternalAcc.timerProviderProducer = timerProviderProducer;
  }
}
