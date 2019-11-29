package eng.jAtcSim.lib.speaking.fromAtc;

import eng.jAtcSim.lib.messaging.IMessageContent;
import eng.jAtcSim.lib.speaking.IFromAtc;

public interface IAtc2Atc extends IFromAtc, IMessageContent {
  boolean isRejection();
}
