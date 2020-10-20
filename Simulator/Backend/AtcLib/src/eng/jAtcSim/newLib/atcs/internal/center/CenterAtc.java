package eng.jAtcSim.newLib.atcs.internal.center;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.atcs.internal.computer.ComputerAtc;
import eng.jAtcSim.newLib.atcs.internal.computer.RequestResult;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoodDayNotification;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.*;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterNavaidCommand;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.speeches.atc.atc2user.AtcRejection;
import eng.jAtcSim.newLib.speeches.atc.planeSwitching.PlaneSwitchRequestRouting;

public class CenterAtc extends ComputerAtc {

  private final IList<IAirplane> closeArrivals = new EList<>();
  private int ctrAcceptDistance = 40;
  private int ctrNavaidAcceptDistance = 15;
  private final IList<IAirplane> farArrivals = new EList<>();
  private final IList<IAirplane> middleArrivals = new EList<>();

  public CenterAtc(eng.jAtcSim.newLib.area.Atc template) {
    super(template);
    if (template.getCtrAcceptDistance() != null)
      this.ctrAcceptDistance = template.getCtrAcceptDistance();
    if (template.getCtrNavaidAcceptDistance() != null)
      this.ctrNavaidAcceptDistance = template.getCtrNavaidAcceptDistance();
  }

  @Override
  public void elapseSecond() {
    super.elapseSecond();

    if (Context.getShared().getNow().getValue() % 16 == 0) {
      double dist;

      IList<IAirplane> tmp = new EList<>();

      for (IAirplane plane : middleArrivals) {
        try {
          evaluateMiddleArrivalsForCloseArrivals(tmp, plane);
        } catch (Exception ex) {
          throw new EApplicationException("Failed to evaluate " + plane.getCallsign() + ".", ex);
        }
      }
      middleArrivals.removeMany(tmp);
      closeArrivals.addMany(tmp);
      tmp.clear();

      for (IAirplane plane : farArrivals) {
        dist = Coordinates.getDistanceInNM(plane.getRouting().getEntryExitPoint().getCoordinate(), plane.getCoordinate());
        if (dist < 50) {
          if (plane.getSha().getAltitude() > 29_000) {
            int newAlt = Context.getApp().getRnd().nextInt(25, 29) * 1_000;
            SpeechList<IForPlaneSpeech> sl = new SpeechList<>();
            sl.add(ChangeAltitudeCommand.create(ChangeAltitudeCommand.eDirection.descend, newAlt));
            Message m = new Message(
                    Participant.createAtc(this.getAtcId()),
                    Participant.createAirplane(plane.getCallsign()),
                    sl);
            super.sendMessage(m);
          }
          tmp.add(plane);
        }
      }
      farArrivals.removeMany(tmp);
      middleArrivals.addMany(tmp);
      tmp.clear();
    }
  }

  public int getCtrAcceptDistance() {
    return ctrAcceptDistance;
  }

  public int getCtrNavaidAcceptDistance() {
    return ctrNavaidAcceptDistance;
  }

  @Override
  public void registerNewPlaneInGame(Callsign callsign, boolean finalRegistration) {
    IAirplane plane = Context.Internal.getPlane(callsign);
    if (plane.isArrival())
      farArrivals.add(plane);
  }

  @Override
  public void unregisterPlaneDeletedFromGame(Callsign callsign, boolean isForcedDeletion) {
    IAirplane plane = Context.Internal.getPlane(callsign);
    if (plane.isArrival()) {
      farArrivals.tryRemove(plane);
      middleArrivals.tryRemove(plane);
      closeArrivals.tryRemove(plane);
      //TODO here somewhere should be stats after finished departer, don't they?
    }
  }

  @Override
  protected boolean acceptsNewRouting(IAirplane plane, PlaneSwitchRequestRouting routing) {
    boolean ret;
    DARoute daRoute = Context.getArea().getAirport().getDaRoutes().getFirst(q -> q.getName().equals(routing.getRouteName()));
    ret = daRoute.isValidForCategory(plane.getType().category)
            && (daRoute.getType() == DARouteType.vectoring
            || daRoute.getType() == DARouteType.star
            || daRoute.getType() == DARouteType.transition);
    return ret;
  }

  @Override
  protected void processConfirmedOutgoingPlaneSwitch(Squawk squawk) {
    AtcId newTargetAtc = getAtcIdWhereIAmSwitchingPlanes();
    IAirplane plane = Context.Internal.getPlane(squawk);
    Message msg = new Message(
            Participant.createAtc(this.getAtcId()),
            Participant.createAirplane(plane.getCallsign()),
            new SpeechList<>(
                    new ContactCommand(newTargetAtc)));
    Context.getMessaging().getMessenger().send(msg);
  }

  @Override
  protected RequestResult canIAcceptPlaneIncomingFromAnotherAtc(IAirplane plane) {
    RequestResult ret;
    if (plane.isArrival()) {
      ret = new RequestResult(false, String.format("%s is an arrival.", plane.getCallsign().toString()));
    } else {
      if (plane.getRouting().isOnWayToPassDeparturePoint() == false) {
        ret = new RequestResult(false,
                String.format("%s is not heading (or on the route to) departure fix %s",
                        plane.getCallsign().toString(),
                        plane.getRouting().getEntryExitPoint().getName()));
      } else {
        if (plane.getSha().getAltitude() > super.getAcceptAltitude() || plane.getSha().getAltitude() > (plane.getType().maxAltitude * .666)) {
          ret = new RequestResult(true, null);
        } else {
          double aipDist = Coordinates.getDistanceInNM(plane.getCoordinate(), Context.getArea().getAirport().getLocation());
          if (aipDist > this.ctrAcceptDistance) {
            ret = new RequestResult(true, null);
          } else {
            double navDist = Coordinates.getDistanceInNM(plane.getCoordinate(), plane.getRouting().getEntryExitPoint().getCoordinate());
            if (navDist < this.ctrNavaidAcceptDistance) {
              ret = new RequestResult(true, null);
            } else {
              ret = new RequestResult(false, String.format(
                      "%s is too far from departure fix %s, or not enough far from airport, or not enough high.",
                      plane.getCallsign().toString(),
                      plane.getRouting().getAssignedDARouteName()
              ));
            }
          }
        }
      }
    }
    return ret;
  }

  @Override
  protected boolean isPlaneReadyToSwitchToAnotherAtc(IAirplane plane) {
    boolean ret;
    if (plane.isArrival()) {
      if (plane.isEmergency())
        ret = true;
      else {
        if (closeArrivals.contains(plane) == false) {
          ret = false;
        } else {
          Navaid n = plane.getRouting().getEntryExitPoint();
          double dist = Coordinates.getDistanceInNM(plane.getCoordinate(), n.getCoordinate());
          ret = dist <= 10;
        }
      }
    } else
      ret = false;
    return ret;
  }

  @Override
  protected AtcId getAtcIdWhereIAmSwitchingPlanes() {
    return Context.Internal.getAtc(AtcType.app).getAtcId();
  }

  @Override
  protected void processMessagesFromPlane(IAirplane plane, SpeechList<IFromPlaneSpeech> spchs) {
    for (Object o : spchs) {
      if (o instanceof GoodDayNotification) {
        if (((GoodDayNotification) o).isRepeated()) continue; // repeated g-d-n are ignored
        if (plane.isDeparture() && this.isResponsibleFor(plane.getCallsign())) {
          SpeechList<ICommand> cmds = new SpeechList<>();

          cmds.add(
                  ChangeAltitudeCommand.create(ChangeAltitudeCommand.eDirection.climb, getDepartureRandomTargetAltitude(plane)));
          cmds.add(
                  AltitudeRestrictionCommand.createClearRestriction());
          cmds.add(
                  ChangeSpeedCommand.createResumeOwnSpeed());

          // order to continue after last fix
          Navaid n = plane.getRouting().getEntryExitPoint();
          cmds.add(AfterNavaidCommand.create(n.getName()));
          cmds.add(ChangeHeadingCommand.createContinueCurrentHeading());

          Message m = new Message(
                  Participant.createAtc(this.getAtcId()),
                  Participant.createAirplane(plane.getCallsign()),
                  cmds);
          super.sendMessage(m);
        }
      }

    }
    // nothing to process
  }

  @Override
  protected void processNonPlaneSwitchMessageFromAtc(Message m) {
    // do nothing , ctr has no messages acceptable from ATC
    EAssert.isTrue(m.getContent() instanceof IAtcSpeech);
    IAtcSpeech origin = m.getContent();
    super.sendMessage(new Message(
            Participant.createAtc(this.getAtcId()),
            m.getSource(),
            new AtcRejection(origin, "Unable.")));
  }

  @Override
  protected IReadOnlyList<IAirplane> getPlanesUnderControl() {
    IList<IAirplane> ret = new EList<>();
    ret.addMany(this.farArrivals);
    ret.addMany(this.middleArrivals);
    ret.addMany(this.closeArrivals);
    return ret;
  }

  private void evaluateMiddleArrivalsForCloseArrivals(IList<IAirplane> tmp, IAirplane plane) {
    double dist;
    dist = Coordinates.getDistanceInNM(plane.getRouting().getEntryExitPoint().getCoordinate(), plane.getCoordinate());
    if (dist < 27) {
      SpeechList<ICommand> cmds = new SpeechList<>();
      if (plane.getSha().getTargetAltitude() > Context.Internal.getAtc(AtcType.ctr).getOrderedAltitude())
        cmds.add(ChangeAltitudeCommand.create(
                ChangeAltitudeCommand.eDirection.descend,
                Context.Internal.getAtc(AtcType.ctr).getOrderedAltitude()));

      // assigns route
      Navaid n = plane.getRouting().getEntryExitPoint();
      Tuple<DARoute, ActiveRunwayThreshold> rrt = getRoutingForPlaneAndFix(plane, n);
      DARoute r = rrt.getA();
      ActiveRunwayThreshold rt = rrt.getB();
      cmds.add(ProceedDirectCommand.create(n.getName()));
      cmds.add(ClearedToRouteCommand.create(r.getName(), r.getType(), rt.getName()));
      Message msg = new Message(
              Participant.createAtc(this.getAtcId()),
              Participant.createAirplane(plane.getCallsign()),
              cmds);
      super.sendMessage(msg);

      tmp.add(plane);
    }
  }

  private DARoute getArrivalRouteForPlane(ActiveRunwayThreshold rt, AirplaneType type, int currentAltitude, Navaid mainNavaid, boolean canBeVectoring) {
    DARoute ret = rt.getRoutes().where(
            q -> q.getType() == DARouteType.transition
                    && q.isValidForCategory(type.category)
                    && q.getMaxMrvaAltitude() < currentAltitude
                    && q.getMainNavaid().equals(mainNavaid))
            .tryGetRandom();
    if (ret == null)
      ret = rt.getRoutes().where(
              q -> q.getType() == DARouteType.star
                      && q.isValidForCategory(type.category)
                      && q.getMaxMrvaAltitude() < currentAltitude
                      && q.getMainNavaid().equals(mainNavaid))
              .tryGetRandom();
    if (ret == null && canBeVectoring)
      ret = DARoute.createNewVectoringByFix(mainNavaid);
    return ret;
  }

//  @Override
//  protected void _save(XElement elm) {
//    super._save(elm);
//    LoadSave.saveField(elm, this, "farArrivals");
//    LoadSave.saveField(elm, this, "middleArrivals");
//    LoadSave.saveField(elm, this, "closeArrivals");
//  }
//
//  @Override
//  protected void _load(XElement elm) {
//    super._load(elm);
//    LoadSave.loadField(elm, this, "farArrivals");
//    LoadSave.loadField(elm, this, "middleArrivals");
//    LoadSave.loadField(elm, this, "closeArrivals");
//  }

  private int getDepartureRandomTargetAltitude(IAirplane p) {
    int min;
    switch (p.getType().category) {
      case 'A':
        min = 4;
        break;
      case 'B':
        min = 14;
        break;
      case 'C':
      case 'D':
        min = 20;
        break;
      default:
        throw new UnsupportedOperationException();
    }
    min = Math.max(p.getSha().getAltitude() / 1000, min);
    int ret = Context.getApp().getRnd().nextInt(min, p.getType().maxAltitude / 1000);
    ret = ret * 1000;
    return ret;
  }

  private Tuple<DARoute, ActiveRunwayThreshold> getRoutingForPlaneAndFix(IAirplane plane, Navaid n) {
    Tuple<DARoute, ActiveRunwayThreshold> ret;
    DARoute r = null;
    ActiveRunwayThreshold rt = null;
    IList<ActiveRunwayThreshold> thresholdsCopy;

    IReadOnlyList<ActiveRunwayThreshold> thresholds;
    // if is arrival, scheduled thresholds are taken into account
    if (Context.getArea().tryGetScheduledRunwayConfiguration() != null)
      thresholds = Context.getArea().tryGetScheduledRunwayConfiguration().getArrivals()
              .where(q -> q.isForCategory(plane.getType().category))
              .select(q -> q.getThreshold());
    else
      thresholds = Context.getArea().getCurrentRunwayConfiguration().getArrivals()
              .where(q -> q.isForCategory(plane.getType().category))
              .select(q -> q.getThreshold());

    thresholdsCopy = new EList<>(thresholds);
    while (r == null && !thresholdsCopy.isEmpty()) {
      rt = thresholdsCopy.getRandom();
      thresholdsCopy.remove(rt);
      r = getArrivalRouteForPlane(rt, plane.getType(), plane.getSha().getTargetAltitude(), plane.getRouting().getEntryExitPoint(), false);
    }
    if (thresholdsCopy.isEmpty() && r == null) {
      rt = thresholds.getRandom();
      r = getArrivalRouteForPlane(rt, plane.getType(), plane.getSha().getTargetAltitude(), plane.getRouting().getEntryExitPoint(), true);
    }
    assert rt != null;
    assert r != null;

    ret = new Tuple<>(r, rt);
    return ret;
  }
}
