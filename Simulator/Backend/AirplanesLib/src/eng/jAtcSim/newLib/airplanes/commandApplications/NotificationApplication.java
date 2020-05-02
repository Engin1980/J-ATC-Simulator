package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.speeches.INotification;

public abstract class NotificationApplication<T extends INotification> {

  public abstract ConfirmationResult confirm(Airplane plane, T c);

  public abstract ApplicationResult apply(Airplane plane, T c);
}
