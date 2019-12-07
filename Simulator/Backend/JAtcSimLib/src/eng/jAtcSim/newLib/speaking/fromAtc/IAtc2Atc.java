package eng.jAtcSim.newLib.speaking.fromAtc;

import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.speaking.IFromAtc;

public interface IAtc2Atc extends IFromAtc, IMessageContent {
  boolean isRejection();
}
