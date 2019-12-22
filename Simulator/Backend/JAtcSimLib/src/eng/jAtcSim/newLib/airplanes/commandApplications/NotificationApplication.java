package eng.jAtcSim.newLib.area.airplanes.commandApplications;

import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.area.speaking.fromAtc.IAtcNotification;

public abstract class NotificationApplication<T extends IAtcNotification> {

  public abstract ConfirmationResult confirm(IAirplaneWriteSimple plane, T c);

  public abstract ApplicationResult apply(IAirplaneWriteSimple plane, T c);
}
