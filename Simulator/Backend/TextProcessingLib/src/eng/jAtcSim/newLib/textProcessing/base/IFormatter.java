package eng.jAtcSim.newLib.textProcessing.base;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.speeches.base.ISpeech;

public interface IFormatter<TSource extends ISpeech, TTarget> {
  TTarget format(TSource speech);


}
