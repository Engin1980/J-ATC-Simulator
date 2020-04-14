package eng.jAtcSim.newLib.airplanes.other;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.area.approaches.ApproachStage;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.speeches.SpeechList;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class NewApproachInfo {

  private final SpeechList<ICommand> iafRouteCommands;
  private final SpeechList<ICommand> beforeStagesCommands;
  private final IList<ApproachStage> stages;


  public NewApproachInfo(ApproachEntry entry, Approach approach) {
    EAssert.isTrue(approach.getEntries().contains(entry));
    this.iafRouteCommands = new SpeechList<>(entry.getIafRoute().getRouteCommands());
    this.beforeStagesCommands = new SpeechList<>(approach.getBeforeStagesCommands());
    this.stages = approach.getStages().select(q->q.createCopy());
  }
}
