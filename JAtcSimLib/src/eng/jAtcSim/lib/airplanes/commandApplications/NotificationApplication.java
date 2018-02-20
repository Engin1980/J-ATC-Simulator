package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcNotification;

public abstract class NotificationApplication<T extends IAtcNotification> {

  public abstract ConfirmationResult confirm(Airplane.Airplane4Command plane, T c);

  public abstract ApplicationResult apply(Airplane.Airplane4Command plane, T c);
}
