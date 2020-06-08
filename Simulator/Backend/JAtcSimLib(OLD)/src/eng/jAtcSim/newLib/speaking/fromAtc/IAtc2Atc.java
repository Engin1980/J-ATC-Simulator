package eng.jAtcSim.newLib.area.speaking.fromAtc;

import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.area.speaking.IFromAtc;

public interface IAtc2Atc extends IFromAtc, IMessageContent {
  boolean isRejection();
}
