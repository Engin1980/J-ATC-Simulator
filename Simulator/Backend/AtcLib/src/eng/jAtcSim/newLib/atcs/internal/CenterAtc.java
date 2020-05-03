package eng.jAtcSim.newLib.atcs.internal;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.AreaAcc;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.atcs.planeResponsibility.PlaneResponsibilityManager;
import eng.jAtcSim.newLib.atcs.planeResponsibility.SwitchRoutingRequest;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.messaging.StringMessageContent;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.SharedAcc;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane2atc.GoodDayNotification;
import eng.jAtcSim.newLib.speeches.atc2airplane.*;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterNavaidCommand;

public class CenterAtc extends ComputerAtc {

  private int ctrAcceptDistance = 40;
  private int ctrNavaidAcceptDistance = 15;

  private IList<IAirplane> farArrivals = new EList<>();
  private IList<IAirplane> middleArrivals = new EList<>();
  private IList<IAirplane> closeArrivals = new EList<>();

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

    if (SharedAcc.getNow().getValue() % 16 == 0) {
      double dist;

      IList<IAirplane> tmp = new EList<>();

      for (IAirplane plane : middleArrivals) {
        try {
          evaluateMiddleArrivalsForCloseArrivals(tmp, plane);
        } catch (Exception ex) {
          throw new EApplicationException("Failed to evaluate " + plane.getCallsign() + ".", ex);
        }
      }
      middleArrivals.remove(tmp);
      closeArrivals.add(tmp);
      tmp.clear();

      for (IAirplane plane : farArrivals) {
        dist = Coordinates.getDistanceInNM(plane.getRouting().getEntryExitPoint().getCoordinate(), plane.getCoordinate());
        if (dist < 50) {
          if (plane.getSha().getAltitude() > 29_000) {
            int newAlt = SharedAcc.getRnd().nextInt(25, 29) * 1_000;
            SpeechList sl = new SpeechList();
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
      farArrivals.remove(tmp);
      middleArrivals.add(tmp);
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
  public void registerNewPlaneUnderControl(Callsign callsign, boolean finalRegistration) {
    IAirplane plane = InternalAcc.getPlane(callsign);
    if (plane.isArrival())
      farArrivals.add(plane);
  }

  @Override
  public void removePlaneDeletedFromGame(Callsign callsign) {
    // intentionally blank
  }

  @Override
  public void unregisterPlaneUnderControl(Callsign callsign) {
    IAirplane plane = InternalAcc.getPlane(callsign);
    if (plane.isArrival()) {
      farArrivals.tryRemove(plane);
      middleArrivals.tryRemove(plane);
      closeArrivals.tryRemove(plane);
    }
  }

  @Override
  protected boolean acceptsNewRouting(Callsign callsign, SwitchRoutingRequest srr) {
    IAirplane plane = InternalAcc.getPlane(callsign);
    boolean ret;
    ret = srr.route.isValidForCategory(plane.getType().category)
        && (srr.route.getType() == DARouteType.vectoring
        || srr.route.getType() == DARouteType.star
        || srr.route.getType() == DARouteType.transition);
    return ret;
  }

  @Override
  protected RequestResult canIAcceptPlane(Callsign callsign) {
    RequestResult ret;
    IAirplane plane = InternalAcc.getPlane(callsign);
    if (plane.isArrival()) {
      ret = new RequestResult(false, String.format("%s is an arrival.", plane.getCallsign().toString()));
    } else {
      if (plane.getRouting().isOnWayToPassDeparturePoint() == false) {
        ret = new RequestResult(false,
            String.format("%s is not heading (or on the route to) departure fix %s",
                plane.getCallsign().toString(),
                plane.getRouting().getAssignedRoute().getMainNavaid().getName()));
      } else {
        if (plane.getSha().getAltitude() > super.acceptAltitude || plane.getSha().getAltitude() > (plane.getType().maxAltitude * .666)) {
          ret = new RequestResult(true, null);
        } else {
          double aipDist = Coordinates.getDistanceInNM(plane.getCoordinate(), AreaAcc.getAirport().getLocation());
          if (aipDist > this.ctrAcceptDistance) {
            ret = new RequestResult(true, null);
          } else {
            double navDist = Coordinates.getDistanceInNM(plane.getCoordinate(), plane.getRouting().getDepartureLastNavaid().getCoordinate());
            if (navDist < this.ctrNavaidAcceptDistance) {
              ret = new RequestResult(true, null);
            } else {
              ret = new RequestResult(false, String.format(
                  "%s is too far from departure fix %s, or not enough far from airport, or not enough high.",
                  plane.getCallsign().toString(),
                  plane.getRouting().getAssignedRoute().getName()
              ));
            }
          }
        }
      }
    }
    return ret;
  }

  private void evaluateMiddleArrivalsForCloseArrivals(IList<IAirplane> tmp, IAirplane plane) {
    double dist;
    dist = Coordinates.getDistanceInNM(plane.getRouting().getEntryExitPoint().getCoordinate(), plane.getCoordinate());
    if (dist < 27) {
      SpeechList<ICommand> cmds = new SpeechList<>();
      if (plane.getSha().getTargetAltitude() > InternalAcc.getAtc(AtcType.ctr).getOrderedAltitude())
        cmds.add(ChangeAltitudeCommand.create(
            ChangeAltitudeCommand.eDirection.descend,
            InternalAcc.getAtc(AtcType.ctr).getOrderedAltitude()));

      // assigns route
      Navaid n = plane.getRouting().getEntryExitPoint();
      Tuple<DARoute, ActiveRunwayThreshold> rrt = getRoutingForPlaneAndFix(plane, n);
      DARoute r = rrt.getA();
      ActiveRunwayThreshold rt = rrt.getB();
      cmds.add(ProceedDirectCommand.create(n.getName()));
      cmds.add(ClearedToRouteCommand.create(r.getName(), rt.getName()));
      Message msg = new Message(
          Participant.createAtc(this.getAtcId()),
          Participant.createAirplane(plane.getCallsign()),
          cmds);
      super.sendMessage(msg);

      tmp.add(plane);
    }
  }

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
    int ret = SharedAcc.getRnd().nextInt(min, p.getType().maxAltitude / 1000);
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
    if (AreaAcc.getScheduledRunwayConfiguration() != null)
      thresholds = AreaAcc.getScheduledRunwayConfiguration().getArrivals()
          .where(q -> q.isForCategory(plane.getType().category))
          .select(q -> q.getThreshold());
    else
      thresholds = AreaAcc.getCurrentRunwayConfiguration().getArrivals()
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

  @Override
  protected AtcId getTargetAtcIfPlaneIsReadyToSwitch(Callsign callsign) {
    AtcId ret;
    IAirplane plane = InternalAcc.getPlane(callsign);
    if (plane.isArrival()) {
      if (plane.isEmergency())
        ret = InternalAcc.getAtc(AtcType.app).getAtcId();
      else {
        if (closeArrivals.contains(plane) == false) {
          ret = null;
        } else {
          Navaid n = plane.getRouting().getEntryExitPoint();
          double dist = Coordinates.getDistanceInNM(plane.getCoordinate(), n.getCoordinate());
          if (dist <= 10) {
            ret = InternalAcc.getAtc(AtcType.app).getAtcId();
          } else
            ret = null;
        }
      }
    } else
      ret = null;
    return ret;
  }

  @Override
  protected void processMessagesFromPlane(Callsign callsign, SpeechList spchs) {
    IAirplane plane = InternalAcc.getPlane(callsign);
    PlaneResponsibilityManager prm = InternalAcc.getPrm();
    for (Object o : spchs) {
      if (o instanceof GoodDayNotification) {
        if (((GoodDayNotification) o).isRepeated()) continue; // repeated g-d-n are ignored
        if (plane.isDeparture() && prm.forAtc().getResponsibleAtc(callsign).equals(this.getAtcId())) {
          SpeechList<ICommand> cmds = new SpeechList<>();

          cmds.add(
              ChangeAltitudeCommand.create(ChangeAltitudeCommand.eDirection.climb, getDepartureRandomTargetAltitude(plane)));
          cmds.add(
              AltitudeRestrictionCommand.createClearRestriction());
          cmds.add(
              ChangeSpeedCommand.createResumeOwnSpeed());

          // order to continue after last fix
          Navaid n = plane.getRouting().getDepartureLastNavaid();
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

  @Override
  protected void processNonPlaneSwitchMessageFromAtc(Message m) {
    // do nothing , ctr has no messages acceptable from ATC
    super.sendMessage(new Message(
        Participant.createAtc(this.getAtcId()),
        m.getSource(),
        new StringMessageContent("Unable.")
    ));
  }

  @Override
  protected boolean shouldBeSwitched(Callsign callsign) {
    return true;
  }
}
