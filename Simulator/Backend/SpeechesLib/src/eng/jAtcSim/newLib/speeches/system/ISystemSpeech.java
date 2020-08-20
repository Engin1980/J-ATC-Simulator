package eng.jAtcSim.newLib.speeches.system;

import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.speeches.base.ISpeech;

public interface ISystemSpeech extends ISpeech, IMessageContent {
  default boolean isRejection(){
    return false;
  }
}
