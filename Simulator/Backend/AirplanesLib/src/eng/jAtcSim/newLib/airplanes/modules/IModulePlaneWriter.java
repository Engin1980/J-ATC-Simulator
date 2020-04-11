package eng.jAtcSim.newLib.airplanes.modules;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.speeches.INotification;
import eng.jAtcSim.newLib.speeches.airplane2atc.GoodDayNotification;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public interface IModulePlaneWriter {
  void divert(boolean unknown);

  void sendMessage(INotification notification);
}
