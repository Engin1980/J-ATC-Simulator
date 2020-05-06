package eng.jAtcSim.newLib.gameSim.simulation;

import eng.eSystem.Producer;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.TimerController;
import eng.jAtcSim.newLib.textProcessing.base.Parser;

public class InternalAcc {
  private static Producer<TimerController> timerProviderProducer;
  private static Producer<Parser> parserProducer;

  public static Parser getParser() {
    return parserProducer.produce();
  }

  public static TimerController getTimerProvider() {
    return timerProviderProducer.produce();
  }

  public static void setParserProducer(Producer<Parser> parserProducer) {
    InternalAcc.parserProducer = parserProducer;
  }

  public static void setTimerProviderProducer(Producer<TimerController> timerProviderProducer) {
    InternalAcc.timerProviderProducer = timerProviderProducer;
  }
}
