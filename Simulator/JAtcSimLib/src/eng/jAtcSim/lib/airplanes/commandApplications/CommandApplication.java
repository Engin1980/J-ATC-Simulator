package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilot5Command;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotWriteSimple;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Confirmation;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;

public abstract class CommandApplication<T extends IAtcCommand> {

  //region Public methods
  public ConfirmationResult confirm(IPilotWriteSimple pilot, T c, boolean checkStateSanity, boolean checkCommandSanity) {
    ConfirmationResult ret = new ConfirmationResult();
    if (checkStateSanity)
      ret.rejection = checkStateSanity(pilot.getPlane().getState(), c);
    if (ret.rejection == null && checkCommandSanity) {
      ret.rejection = checkCommandSanity(pilot, c);
    }
    if (ret.rejection == null)
      ret.confirmation = new Confirmation(c);
    return ret;
  }

  public ApplicationResult apply(IPilotWriteSimple pilot, T c, boolean checkStateSanity) {
    ApplicationResult ret;
    IFromAirplane rejection = null;
    if (checkStateSanity)
      rejection = checkStateSanity(pilot.getPlane().getState(), c);
    if (rejection == null)
      rejection = checkCommandSanity(pilot, c);
    if (rejection == null) {
      ret = adjustAirplane(pilot, c);
    } else {
      ret = new ApplicationResult();
      ret.rejection = rejection;
    }
    return ret;
  }
  //endregion

  //region Protected methods

  protected abstract IFromAirplane checkCommandSanity(IPilotWriteSimple pilot, T c);

  protected abstract Airplane.State [] getInvalidStates();

  protected abstract ApplicationResult adjustAirplane(IPilotWriteSimple pilot, T c);

  protected IFromAirplane getRejection(IAtcCommand c, String reason) {
    IFromAirplane ret = new Rejection("Unable to comply the command in the current state.", c);
    return ret;
  }

  protected IFromAirplane getUnableDueToState(IAtcCommand c) {
    IFromAirplane ret = getRejection(c, "Unable to comply the command in the current state.");
    return ret;
  }

  protected boolean isUnableDueToState(IPilotWriteSimple pilot, IAtcCommand c, Airplane.State... states) {
    boolean ret = pilot.getPlane().getState().is(states);
    return ret;
  }

  //endregion

  //region Private methods

  private IFromAirplane checkStateSanity(Airplane.State state, IAtcCommand cmd){
    IFromAirplane ret;
    Airplane.State [] invalidStates = getInvalidStates();
    if (state.is(invalidStates)) {
      ret = new Rejection("Unable to comply a command now, does not fit our state.", cmd);
    } else
      ret = null;
    return ret;
  }

  //endregion

}
