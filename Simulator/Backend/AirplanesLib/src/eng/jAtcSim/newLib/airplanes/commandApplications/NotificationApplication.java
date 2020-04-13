package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.speeches.INotification;

public abstract class NotificationApplication<T extends INotification> {

  public abstract ConfirmationResult confirm(IAirplaneCommand plane, T c);

  public abstract ApplicationResult apply(IAirplaneCommand plane, T c);
}
