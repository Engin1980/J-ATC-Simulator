package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.speeches.INotification;

public abstract class NotificationApplication<T extends INotification> {

  public abstract ConfirmationResult confirm(IAirplaneWriteSimple plane, T c);

  public abstract ApplicationResult apply(IAirplaneWriteSimple plane, T c);
}
