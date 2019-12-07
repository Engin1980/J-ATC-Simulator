package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.speaking.fromAtc.IAtcNotification;

public abstract class NotificationApplication<T extends IAtcNotification> {

  public abstract ConfirmationResult confirm(IAirplaneWriteSimple plane, T c);

  public abstract ApplicationResult apply(IAirplaneWriteSimple plane, T c);
}
