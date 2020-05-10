package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;

public abstract class NonCommandApplication<T extends IForPlaneSpeech> {

  public abstract ConfirmationResult confirm(Airplane plane, T c);

  public abstract ApplicationResult apply(Airplane plane, T c);
}
