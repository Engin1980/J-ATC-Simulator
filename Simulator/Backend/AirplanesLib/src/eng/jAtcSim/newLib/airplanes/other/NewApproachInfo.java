package eng.jAtcSim.newLib.airplanes.other;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.speeches.ICommand;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class NewApproachInfo {

  private IafRoute iafRoute;
  private IList<ICommand> beforeStagesCommands;

  public NewApproachInfo(ApproachEntry entry, Approach approach) {
    EAssert.isTrue(approach.getEntries().contains(entry));
    approach.
  }
}
