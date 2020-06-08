package eng.jAtcSim.newLib.area.airplanes.modules;

import eng.eSystem.Tuple;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.utilites.ConversionUtils;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.airplanes.Airplane;
import eng.jAtcSim.newLib.area.airplanes.behaviors.HoldBehavior;
import eng.jAtcSim.newLib.area.airplanes.commandApplications.ApplicationManager;
import eng.jAtcSim.newLib.area.airplanes.commandApplications.ApplicationResult;
import eng.jAtcSim.newLib.area.airplanes.commandApplications.ConfirmationResult;
import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.area.airplanes.interfaces.modules.IRoutingModuleRO;
import eng.jAtcSim.newLib.area.atcs.Atc;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.DelayedList;
import eng.jAtcSim.newLib.speeches.ISpeech;

import java.util.HashMap;
import java.util.Map;

public class RoutingModule extends Module implements IRoutingModuleRO {

  public enum CommandSource {
    procedure,
    atc,
    route,
    extension
  }

  private DARoute assignedRoute;
  private ActiveRunwayThreshold expectedRunwayThreshold;
  private Navaid entryExitPoint;

  private final DelayedList<ISpeech> queue = new DelayedList<>(2, 7); //Min/max item delay
  private final AfterCommandList afterCommands = new AfterCommandList();
  private final Map<Atc, SpeechList> saidText = new HashMap<>();

  public RoutingModule(IAirplaneWriteSimple parent) {
    super(parent);
  }

  public void elapseSecond() {
    processNewSpeeches();
    processAfterSpeeches();
    flushSaidTextToAtc();
  }

  public Navaid getEntryExitPoint() {
    return this.entryExitPoint;
  }

  @Override
  public ActiveRunwayThreshold getAssignedRunwayThreshold() {
    return expectedRunwayThreshold;
  }

  @Override
  public Navaid getDepartureLastNavaid() {
    if (this.parent.getFlightModule().isDeparture() == false)
        throw new EApplicationException(sf(
            "This method should not be called on departure aircraft %s.",
            this.parent.getFlightModule().getCallsign().toString()));

      Navaid ret = this.assignedRoute.getMainNavaid();
      return ret;
  }

  @Override
  public boolean hasLateralDirectionAfterCoordinate() {
    Coordinate coordinate = parent.getSha().tryGetTargetCoordinate();
    assert coordinate != null;
    return afterCommands.hasLateralDirectionAfterCoordinate(coordinate);
  }

  public DARoute getAssignedRoute() {
    return this.assignedRoute;
  }

  @Override
  public boolean isRouteEmpty() {
    return afterCommands.isRouteEmpty();
  }

  public void setRoute(DARoute route) {
    afterCommands.clearAll();
    this.assignedRoute = route;
  }

  public void setRoute(SpeechList<IFromAtc> route) {
    this.afterCommands.clearAll();
    expandThenCommands(route);
    processSpeeches(route, CommandSource.procedure);
  }

  public void setRouting(DARoute newRoute, ActiveRunwayThreshold expectedRunwayThreshold) {
    this.expectedRunwayThreshold = expectedRunwayThreshold;
    this.assignedRoute = newRoute;
    this.afterCommands.clearRoute();

    SpeechList<IFromAtc> cmds;
    cmds = new SpeechList<>();
    cmds.add(assignedRoute.getRouteCommands());
    expandThenCommands(cmds);
    processSpeeches(cmds, CommandSource.procedure);
  }

  public ActiveRunwayThreshold getExpectedRunwayThreshold() {
    return expectedRunwayThreshold;
  }

  public void addNewSpeeches(SpeechList<IFromAtc> speeches) {
    this.queue.newRandomDelay();
    expandThenCommands(speeches);
    for (ISpeech speech : speeches) {
      this.queue.add(speech);
    }
  }

  public boolean isGoingToFlightOverNavaid(Navaid navaid) {
    return afterCommands.hasProceedDirectToNavaidAsConseqent(navaid);
  }

  public void init(Navaid entryExitPoint) {
    assert entryExitPoint != null;
    this.entryExitPoint = entryExitPoint;
  }

  public void clearAfterCommands(){
    this.afterCommands.clearAll();
  }

  public void applyShortcut(Navaid navaid) {
    SpeechList<IFromAtc> skippedCommands = this.afterCommands.doShortcutTo(navaid);
    this.processSpeeches(skippedCommands, CommandSource.procedure);
  }

  public void processNewSpeeches() {
    SpeechList current = new SpeechList<>(this.queue.getAndElapse());

    if (current.isEmpty()) return;

    parent.getRecorderModule().logProcessedCurrentSpeeches(current);

    // if has not confirmed radar contact and the first command in the queue is not radar contact confirmation
    if (parent.getAtcModule().getSecondsWithoutRadarContact() > 0
        && !(current.get(0) instanceof RadarContactConfirmationNotification)) {
      say(new RequestRadarContactNotification());
      this.queue.clear();
    } else {
      processSpeeches(current, CommandSource.atc);
    }
  }

  private void processAfterSpeeches() {

    SpeechList<IAtcCommand> cmds;

    Coordinate targetCoordinate = parent.getSha().tryGetTargetCoordinate();
    if (targetCoordinate == null && parent.getBehaviorModule().is(HoldBehavior.class)) {
      HoldBehavior hb = parent.getBehaviorModule().getAs(HoldBehavior.class);
      targetCoordinate = hb.navaid.getCoordinate();
    }

    cmds = afterCommands.getAndRemoveSatisfiedCommands(
        parent, targetCoordinate, AfterCommandList.Type.extensions);
    parent.getRecorderModule().logProcessedAfterSpeeches(cmds, "extensions");
    processSpeeches(cmds, CommandSource.extension);

    cmds = afterCommands.getAndRemoveSatisfiedCommands(
        parent, targetCoordinate, AfterCommandList.Type.route);
    parent.getRecorderModule().logProcessedAfterSpeeches(cmds, "route");
    processSpeeches(cmds, CommandSource.route);
  }

  private void processSpeeches(SpeechList<? extends IFromAtc> queue, CommandSource cs) {
    while (!queue.isEmpty()) {
      IFromAtc cmd = queue.get(0);
      if (cmd instanceof AfterCommand) {
        processAfterSpeechWithConsequents(queue, cs);
      } else {
        processNormalSpeech(queue, cmd, cs, parent);
      }
    }
  }

  private void processNormalSpeech(
      SpeechList<? extends IFromAtc> queue, IFromAtc cmd,
      CommandSource cs, IAirplaneWriteSimple plane) {

    ConfirmationResult cres = ApplicationManager.confirm(plane, cmd, cs == CommandSource.atc, true);
    if (cres.rejection != null) {
      // command was rejected
      say(cres.rejection);
    } else {
      affectAfterCommands(cmd, cs);
      // new commands from atc when needs to be confirmed, are confirmed
      if (cs == CommandSource.atc && cres.confirmation != null)
        say(cres.confirmation);
      // command is applied
      ApplicationResult ares = ApplicationManager.apply(parent, cmd);
      assert ares.rejection == null : "This should not be rejected as was confirmed a few moments before.";
      ares.informations.forEach(q -> say(q));
    }

    queue.removeAt(0);
  }

  private void affectAfterCommands(IFromAtc cmd, CommandSource cs) {
    final Class[] lateralCommands = new Class[]{ProceedDirectCommand.class, ChangeHeadingCommand.class, HoldCommand.class};
    switch (cs) {
      case procedure:
        // nothing
        break;
      case route:
        // nothing
        break;
      case atc:

        if (ConversionUtils.isInstanceOf(cmd, lateralCommands)) {
          // rule 2
          this.afterCommands.clearRoute();
          this.afterCommands.clearExtensionsByConsequent(lateralCommands);
        } else if (cmd instanceof ShortcutCommand) {
          // rule 3
          // does nothing as everything is done in ShortcutCommandApplication
        } else if (cmd instanceof ChangeAltitudeCommand) {
          // rule 4
          ChangeAltitudeCommand tmp = (ChangeAltitudeCommand) cmd;
          this.afterCommands.clearChangeAltitudeClass(tmp.getAltitudeInFt(), this.parent.getFlightModule().isArrival());
        } else if (cmd instanceof ChangeSpeedCommand) {
          ChangeSpeedCommand tmp = (ChangeSpeedCommand) cmd;
          if (tmp.isResumeOwnSpeed() == false) {
            // rule 5
            this.afterCommands.clearChangeSpeedClass(
                tmp.getSpeedInKts(), this.parent.getFlightModule().isArrival(), AfterCommandList.Type.route);
            this.afterCommands.clearChangeSpeedClass(
                tmp.getSpeedInKts(), this.parent.getFlightModule().isArrival(), AfterCommandList.Type.extensions);
          } else {
            // rule 6
            this.afterCommands.clearChangeSpeedClassOfRouteWithTransferConsequent(
                null, this.parent.getFlightModule().isArrival());
            this.afterCommands.clearExtensionsByConsequent(ChangeSpeedCommand.class);
          }
        } else if (cmd instanceof ClearedToApproachCommand) {
          // rule 12
          this.afterCommands.clearAll();
        }
        break;
      case extension:
        if (ConversionUtils.isInstanceOf(cmd, lateralCommands)) {
          // rule 7
          this.afterCommands.clearRoute();
        } else if (cmd instanceof ShortcutCommand) {
          // rule 8
          // does nothing as everything is done in ShortcutCommandApplication
        } else if (cmd instanceof AfterAltitudeCommand) {
          // rule 9
          ChangeAltitudeCommand tmp = (ChangeAltitudeCommand) cmd;
          this.afterCommands.clearChangeAltitudeClass(tmp.getAltitudeInFt(), this.parent.getFlightModule().isArrival());
        } else if (cmd instanceof ChangeSpeedCommand) {
          ChangeSpeedCommand tmp = (ChangeSpeedCommand) cmd;
          if (tmp.isResumeOwnSpeed() == false) {
            // rule 10
            this.afterCommands.clearChangeSpeedClass(tmp.getSpeedInKts(), this.parent.getFlightModule().isArrival(), AfterCommandList.Type.extensions);
          } else {
            // rule 11
            this.afterCommands.clearChangeSpeedClassOfRouteWithTransferConsequent(
                null, this.parent.getFlightModule().isArrival());
            this.afterCommands.clearExtensionsByConsequent(ChangeSpeedCommand.class);
          }
        } else if (cmd instanceof ClearedToApproachCommand) {
          // rule 13
          this.afterCommands.clearAll();
        }
        break;
      default:
        throw new UnsupportedOperationException();
    }
  }

  private void processAfterSpeechWithConsequents(IList<? extends ISpeech> queue, CommandSource cs) {

    Airplane.State[] unableProcessAfterCommandsStates = {
        Airplane.State.flyingIaf2Faf,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround
    };

    AfterCommand af = (AfterCommand) queue.get(0);
    queue.removeAt(0);

    if (cs == CommandSource.atc && parent.getState().is(unableProcessAfterCommandsStates)) {
      ISpeech rej = new Rejection("Unable to process after-command during approach/take-off.", af);
      say(rej);
      return;
    }

    ConfirmationResult cres;
    boolean sayConfirmations = cs == CommandSource.atc;

    cres = ApplicationManager.confirm(parent, af, true, false);
    if (sayConfirmations) say(cres.confirmation);

    while (queue.isEmpty() == false) {
      ISpeech sp = queue.get(0);
      if (sp instanceof AfterCommand)
        break;
      else {
        assert sp instanceof IAtcCommand : "Instance of " + sp.getClass().getName() + " is not IAtcCommand";
        IAtcCommand cmd = (IAtcCommand) sp;
        if (cmd instanceof AfterCommand)
          break;

        queue.removeAt(0);
        cres = ApplicationManager.confirm(parent, cmd, true, false);
        if (sayConfirmations) say(cres.confirmation);

        if (cs == CommandSource.procedure) {
          afterCommands.addRoute(af, cmd);
        } else
          afterCommands.addExtension(af, cmd);
      }
    }
  }

  private void say(ISpeech speech) {
    // if no tuned atc, nothing is said
    Atc atc = parent.getAtcModule().getTunedAtc();

    if (atc == null) return;

    if (saidText.containsKey(atc) == false) {
      saidText.put(atc, new SpeechList());
    }

    saidText.get(atc).add(speech);
  }

  private void flushSaidTextToAtc() {
    for (Atc a : saidText.keySet()) {
      SpeechList saidTextToAtc = saidText.get(a);
      if (!saidTextToAtc.isEmpty()) {
        parent.sendMessage(a, saidText.get(a));
        saidText.put(a, new SpeechList());
        // here new list must be created
        // the old one is send to messenger for further processing
      }
    }
  }

  private void expandThenCommands(SpeechList<IFromAtc> speeches) {
    if (speeches.isEmpty()) {
      return;
    }

    for (int i = 0; i < speeches.size(); i++) {
      if (speeches.get(i) instanceof ThenCommand) {
        if (i == 0 || i == speeches.size() - 1) {
          parent.sendMessage(
              new IllegalThenCommandRejection("{Then} command cannot be first or last in queue. The whole command block is ignored.")
          );
          speeches.clear();
          return;
        }
        IAtcCommand prev = (IAtcCommand) speeches.get(i - 1);

        AfterCommand n; // new
        if (prev instanceof ProceedDirectCommand) {
          n = new AfterNavaidCommand(((ProceedDirectCommand) prev).getNavaid());
        } else if (prev instanceof ChangeAltitudeCommand) {
          ChangeAltitudeCommand ca = (ChangeAltitudeCommand) prev;
          AfterAltitudeCommand.ERestriction restriction;
          switch (ca.getDirection()) {
            case any:
              restriction = AfterAltitudeCommand.ERestriction.exact;
              break;
            case climb:
              restriction = AfterAltitudeCommand.ERestriction.andAbove;
              break;
            case descend:
              restriction = AfterAltitudeCommand.ERestriction.andBelow;
              break;
            default:
              throw new UnsupportedOperationException();
          }
          n = new AfterAltitudeCommand(ca.getAltitudeInFt(), restriction);
        } else if (prev instanceof ChangeSpeedCommand) {
          n = new AfterSpeedCommand(((ChangeSpeedCommand) prev).getSpeedInKts());
        } else if (prev instanceof ChangeHeadingCommand) {
          n = new AfterHeadingCommand(((ChangeHeadingCommand) prev).getHeading());
        } else {
          parent.sendMessage(
              new IllegalThenCommandRejection("{Then} command is antecedent a strange command, it does not make sense. The whole command block is ignored."));
          speeches.clear();
          return;
        }
        n.setDerivationSource(prev);
        speeches.set(i, n);
      }
    }
  }

  private int getIndexOfNavaidInCommands(Navaid navaid) {
    for (int i = 0; i < this.queue.size(); i++) {
      if (this.queue.get(i) instanceof ProceedDirectCommand) {
        ProceedDirectCommand pdc = (ProceedDirectCommand) this.queue.get(i);
        if (pdc.getNavaid() == navaid) {
          return i;
        }
      }
    }
    return -1;

  }

  private void printAfterCommands() {
    System.out.println("## -- route ");
    for (Tuple<AfterCommand, IAtcCommand> afterCommandIAtcCommandTuple : afterCommands.getAsList(AfterCommandList.Type.route)) {
      System.out.println("  IF " + afterCommandIAtcCommandTuple.getA().toString());
      System.out.println("  THEN " + afterCommandIAtcCommandTuple.getB().toString());
    }
    System.out.println("## -- ex ");
    for (Tuple<AfterCommand, IAtcCommand> afterCommandIAtcCommandTuple : afterCommands.getAsList(AfterCommandList.Type.extensions)) {
      System.out.println("  IF " + afterCommandIAtcCommandTuple.getA().toString());
      System.out.println("  THEN " + afterCommandIAtcCommandTuple.getB().toString());
    }
  }
}
