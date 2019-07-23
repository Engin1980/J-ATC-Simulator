//package eng.jAtcSim.lib.airplanes.behaviors;
//
//import eng.eSystem.EStringBuilder;
//import eng.eSystem.geo.Coordinate;
//import eng.eSystem.geo.Coordinates;
//import eng.eSystem.utilites.EnumUtils;
//import eng.eSystem.validation.Validator;
//import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
//import eng.jAtcSim.lib.Acc;
//import eng.jAtcSim.lib.airplanes.Airplane;
//import eng.jAtcSim.lib.airplanes.AirproxType;
//import eng.jAtcSim.lib.airplanes.moods.Mood;
//import eng.jAtcSim.lib.airplanes.pilots.Pilot;
//import eng.jAtcSim.lib.airplanes.approachStagePilots.ApproachInfo;
//import eng.jAtcSim.lib.global.Headings;
//import eng.jAtcSim.lib.speaking.IFromAtc;
//import eng.jAtcSim.lib.speaking.SpeechList;
//import eng.jAtcSim.lib.speaking.fromAirplane.notifications.EstablishedOnApproachNotification;
//import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
//import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
//import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
//import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;
//import eng.jAtcSim.lib.speaking.fromAtc.commands.ThenCommand;
//import eng.jAtcSim.lib.weathers.Weather;
//import eng.jAtcSim.lib.world.Navaid;
//import eng.jAtcSim.lib.world.approachesOld.Approach;
//
//public class ApproachBehavior extends Behavior {
//
//  private ApproachInfo approachInfo;
//  private Pilot.ApproachLocation location = Pilot.ApproachLocation.unset;
//
//  @XmlConstructor
//  private ApproachBehavior() {
//  }
//
//  public ApproachBehavior(ApproachInfo approachInfo) {
//    Validator.isNotNull(approachInfo);
//    this.approachInfo = approachInfo;
//
//    Pilot.this.gaReason = null;
//
//    if (approachInfo.getIafRoute().isEmpty() == false) {
//      expandThenCommands(approach.getIafRoute());
//      Pilot.this.processSpeeches(approach.getIafRoute(), Pilot.CommandSource.procedure);
//      this.setState(Airplane.State.flyingIaf2Faf);
//    } else {
//      SpeechList<IFromAtc> tmp = new SpeechList();
//      tmp.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, approach.getInitialAltitude()));
//      Pilot.this.processSpeeches(tmp, Pilot.CommandSource.procedure);
//      this.setState(Airplane.State.approachEnter);
//    }
//  }
//
//  public void goAround(GoingAroundNotification.GoAroundReason reason) {
//    assert reason != null;
//
//    Pilot.this.isAfterGoAround = true;
//    boolean isAtcFail = EnumUtils.is(reason,
//        new GoingAroundNotification.GoAroundReason[]{
//            GoingAroundNotification.GoAroundReason.lostTrafficSeparationInApproach,
//            GoingAroundNotification.GoAroundReason.noLandingClearance,
//            GoingAroundNotification.GoAroundReason.notStabilizedApproachEnter,
//            GoingAroundNotification.GoAroundReason.notStabilizedAirplane
//        });
//    if (isAtcFail)
//      Pilot.this.parent.getMood().addExperience(Mood.ArrivalExperience.goAroundNotCausedByPilot);
//
//    Pilot.this.gaReason = reason;
//    parent.adviceGoAroundToAtc(atc, reason);
//
//    super.setBehaviorAndState(
//        new TakeOffBehavior(null), Airplane.State.takeOffGoAround);
//
//    parent.setSpeedOrders(parent.getKind().vDep);
//    parent.setAltitudeOrders((int) parent.getAltitude());
//    parent.setTargetHeading(approach.getThreshold().getCourse());
//
//    Pilot.this.afterCommands.clearAll();
//
//    SpeechList<IFromAtc> gas = new SpeechList<>(this.approach.getGaRoute());
//    ChangeAltitudeCommand cac = null; // remember climb command and add it as first at the end
//    if (gas.get(0) instanceof ChangeAltitudeCommand) {
//      cac = (ChangeAltitudeCommand) gas.get(0);
//      gas.removeAt(0);
//    }
//    gas.insert(0, new ChangeHeadingCommand((int) this.approach.getThreshold().getCourse(), ChangeHeadingCommand.eDirection.any));
//
//    // check if is before runway threshold.
//    // if is far before, then first point will still be runway threshold
//    if (isBeforeRunwayThreshold()) {
//      String runwayThresholdNavaidName =
//          this.approach.getThreshold().getParent().getParent().getIcao() + ":" + this.approach.getThreshold().getName();
//      Navaid runwayThresholdNavaid = Acc.area().getNavaids().getOrGenerate(runwayThresholdNavaidName);
//      gas.insert(0, new ProceedDirectCommand(runwayThresholdNavaid));
//      gas.insert(1, new ThenCommand());
//    }
//
//    if (cac != null)
//      gas.insert(0, cac);
//
//    expandThenCommands(gas);
//    processSpeeches(gas, Pilot.CommandSource.procedure);
//  }
//
//  private boolean isBeforeRunwayThreshold() {
//    double dist = Coordinates.getDistanceInNM(parent.getCoordinate(), this.approach.getThreshold().getCoordinate());
//    double hdg = Coordinates.getBearing(parent.getCoordinate(), this.approach.getThreshold().getCoordinate());
//    boolean ret;
//    if (dist < 3)
//      ret = false;
//    else {
//      ret = Headings.isBetween(this.approach.getCourse() - 70, hdg, this.approach.getCourse() + 70);
//    }
//    return ret;
//  }
//
//  private double getMinimalAllowedAltitudeAfterThisStep() {
//    double maxVS;
//    switch (Pilot.this.parent.getState()) {
//      case longFinal:
//        maxVS = -1500;
//        break;
//      case shortFinal:
//        maxVS = -1100;
//        break;
//      default:
//        maxVS = -2500;
//
//    }
//    double ret = Pilot.this.parent.getAltitude() + maxVS / 60d;
//    return ret;
//  }
//
//  private boolean updateAltitudeOnApproach(boolean checkIfIsAfterThreshold) {
//    int currentTargetAlttiude = parent.getAltitudeOrders();
//    double distToLand;
//    int newAltitude;
//    if (location == Pilot.ApproachLocation.afterThreshold) {
//      newAltitude = Acc.airport().getAltitude() - 100; // I need to lock the airplane on runway
//    } else {
//      int minAltByState = 0; // (int) getMinimalAllowedAltitudeAfterThisStep();
//      switch (approach.getKind()) {
//        case visual:
//          if (location == Pilot.ApproachLocation.beforeFaf) {
//            // TODO check and evaluate
//            // experimental, trying to fix descend rate after FAF to lower values
//            // newAltitude = parent.getAltitudeOrders();
//            newAltitude = (int) Math.max(parent.getAltitudeOrders(), parent.getAltitude() - 1000);
//          } else {
//            double dist = Coordinates.getDistanceInNM(parent.getCoordinate(), approach.getThreshold().getCoordinate());
//            double delta = dist * this.approach.getSlope();
//            newAltitude = (int) delta + Acc.airport().getAltitude();
//          }
//          break;
//        default:
//          if (location == Pilot.ApproachLocation.beforeFaf)
//            newAltitude = parent.getAltitudeOrders();
//          else {
//            double dist = Coordinates.getDistanceInNM(parent.getCoordinate(), approach.getMapt());
//            double delta = dist * this.approach.getSlope();
//            newAltitude = (int) delta + Acc.airport().getAltitude();
//          }
//      }
//      newAltitude = Math.max(newAltitude, minAltByState);
//      newAltitude = Math.min(newAltitude, parent.getAltitudeOrders());
//      if (location == Pilot.ApproachLocation.beforeMapt)
//        newAltitude = Math.max(newAltitude, approach.getDecisionAltitude());
//      newAltitude = Math.max(newAltitude, Acc.airport().getAltitude());
//    }
//    parent.setAltitudeOrders(newAltitude);
//    boolean ret = (location != Pilot.ApproachLocation.beforeFaf) && (currentTargetAlttiude > parent.getAltitudeOrders());
//
//    return ret;
//  }
//
//  private boolean isBehindFaf() {
//    double courseToFaf = Coordinates.getBearing(parent.getCoordinate(), approach.getFaf());
//    double diff = Headings.getDifference(courseToFaf, approach.getThreshold().getCourse(), true);
//    boolean ret = diff > 90;
//    return ret;
//  }
//
//  private boolean isPassingFaf() {
//    double dist = Coordinates.getDistanceInNM(parent.getCoordinate(), approach.getFaf());
//    boolean ret = dist < 1.0;
//    return ret;
//  }
//
//  private boolean isBehindThreshold() {
//    double course = Coordinates.getBearing(parent.getCoordinate(), approach.getThreshold().getCoordinate());
//    boolean ret = Headings.getDifference(course, approach.getThreshold().getCourse(), true) > 90;
//    return ret;
//  }
//
//  private boolean isBehindMAPt() {
//    double courseToFaf = Coordinates.getBearing(parent.getCoordinate(), approach.getMapt());
//    boolean ret = Headings.getDifference(courseToFaf, approach.getThreshold().getCourse(), true) > 90;
//    return ret;
//  }
//
//  private void updateHeadingOnApproach(Coordinates.eHeadingToRadialBehavior radialBehavior) {
//    double newHeading;
//    Coordinate planePos = parent.getCoordinate();
//    if (location == Pilot.ApproachLocation.beforeFaf) {
//      newHeading = Coordinates.getBearing(planePos, approach.getFaf());
//    } else if (location == Pilot.ApproachLocation.beforeMapt) {
//      Coordinate point = approach.getMapt();
//      double course = approach.getFaf2MaptCourse();
//      newHeading = Coordinates.getHeadingToRadial(
//          planePos, point, course, radialBehavior);
//    } else if (location == Pilot.ApproachLocation.beforeThreshold) {
//      double dist = Coordinates.getDistanceInNM(planePos, this.approach.getThreshold().getCoordinate());
//      if (dist < 2)
//        newHeading = Coordinates.getBearing(planePos, this.approach.getThreshold().getCoordinate());
//      else
//        newHeading = Coordinates.getHeadingToRadial(planePos, this.approach.getThreshold().getCoordinate(), this.approach.getThreshold().getCourse(), radialBehavior);
//    } else {
//      // afther threshold
//      newHeading = (int) Coordinates.getBearing(planePos, this.approach.getThreshold().getOtherThreshold().getCoordinate());
//    }
//    parent.setTargetHeading(newHeading);
//  }
//
//  private boolean canSeeRunwayFromCurrentPosition() {
//    Weather w = Acc.weather();
//    if ((w.getCloudBaseInFt() + Acc.airport().getAltitude()) < parent.getAltitude()) {
//      return false;
//    }
//    double d = Coordinates.getDistanceInNM(parent.getCoordinate(), approach.getThreshold().getCoordinate());
//    if (w.getVisibilityInMilesReal() < d) {
//      return false;
//    }
//    return true;
//  }
//
//  private void updateApproachLocation(boolean isPrecise) {
//    if (location == Pilot.ApproachLocation.unset) {
//      if (isPassingFaf() || isPrecise)
//        location = Pilot.ApproachLocation.beforeMapt;
//      else
//        location = Pilot.ApproachLocation.beforeFaf;
//    } else if (location == Pilot.ApproachLocation.beforeFaf && isPassingFaf())
//      location = Pilot.ApproachLocation.beforeMapt;
//    else if (location == Pilot.ApproachLocation.beforeMapt && isBehindMAPt())
//      location = Pilot.ApproachLocation.beforeThreshold;
//    else if (location == Pilot.ApproachLocation.beforeThreshold && isBehindThreshold())
//      location = Pilot.ApproachLocation.afterThreshold;
//  }
//
//  private void flyIAFtoFAFPhase() {
//    if (targetCoordinate != null) {
//
//      double heading = Coordinates.getBearing(parent.getCoordinate(), targetCoordinate);
//      heading = Headings.to(heading);
//      if (heading != parent.getTargetHeading()) {
//        parent.setTargetHeading(heading);
//      }
//    }
//
//    if (Pilot.this.afterCommands.isRouteEmpty()) {
//      this.setState(Airplane.State.approachEnter);
//      this.isAfterStateChange = true;
//      // TODO here he probably should again check the position against the runway
//    }
//  }
//
//  private void flyApproachingPhase() {
//
//    switch (parent.getState()) {
//      case approachDescend:
//      case longFinal:
//      case shortFinal:
//        if (parent.getAirprox() == AirproxType.full) {
//          goAround(GoingAroundNotification.GoAroundReason.lostTrafficSeparationInApproach);
//          return;
//        }
//    }
//
//    Pilot.ApproachLocation last = this.location;
//    updateApproachLocation(this.approach.isPrecise());
//
//    if (last == Pilot.ApproachLocation.beforeMapt && this.location == Pilot.ApproachLocation.beforeThreshold) {
//      if (canSeeRunwayFromCurrentPosition() == false) {
//        goAround(GoingAroundNotification.GoAroundReason.runwayNotInSight);
//        return;
//      }
//    }
//
//    switch (parent.getState()) {
//
//      case flyingIaf2Faf:
//        throw new UnsupportedOperationException("Not supposed to be here. See flyIAFtoFAFPhase()");
//
//      case approachEnter:
//        if (isAfterStateChange && this.approach.getKind() == Approach.ApproachType.visual) {
//          if (canSeeRunwayFromCurrentPosition() == false) {
//            goAround(GoingAroundNotification.GoAroundReason.runwayNotInSight);
//            return;
//          }
//        }
//        isAfterStateChange = false;
//        // this is when app is cleared for approach
//        // this only updates speed and changes to "entering"
//        updateHeadingOnApproach(Coordinates.eHeadingToRadialBehavior.standard);
//        boolean isDescending = updateAltitudeOnApproach(false);
//        if (isDescending) {
//          isAfterStateChange = true;
//          super.setState(Airplane.State.approachDescend);
//        }
//        break;
//      case approachDescend:
//        if (isAfterStateChange) {
//          if (this.approach.isPrecise()) {
//            // check if not descending to ILS path and not yet established in ILS LOC
//            if (Headings.getDifference(
//                parent.getTargetHeading(), this.approach.getFaf2MaptCourse(), true) > 15) {
//              goAround(GoingAroundNotification.GoAroundReason.notStabilizedApproachEnter);
//              return;
//            }
//            // check if should descend but is not leveled at initial altitude
//            if (parent.getAltitude() > this.approach.getInitialAltitude() + 100) {
//              goAround(GoingAroundNotification.GoAroundReason.notStabilizedApproachEnter);
//              return;
//            }
//          }
//          this.isAfterStateChange = false;
//        }
//        // plane on descend slope
//        // updates speed, then changes to "descending"
//        isAfterStateChange = false;
//        updateHeadingOnApproach(Coordinates.eHeadingToRadialBehavior.standard);
//        updateAltitudeOnApproach(false);
//        if (parent.getAltitude() < this.finalAltitude) {
//          isAfterStateChange = true;
//          super.setState(Airplane.State.longFinal);
//        }
//        break;
//      case longFinal:
//        // plane under final altitude
//        // yells if it have not own speed or if not switched to atc
//        // TODO see above
//        updateHeadingOnApproach(Coordinates.eHeadingToRadialBehavior.aggresive);
//        updateAltitudeOnApproach(false);
//
//        if (isAfterStateChange) {
//          // moc nizko, uz pod stabilized altitude
//          int MAX_LONG_FINAL_HEADING_DIFF = 30;
//          if (Math.abs(parent.getTargetHeading() - this.approach.getCourse()) > MAX_LONG_FINAL_HEADING_DIFF) {
//            goAround(GoingAroundNotification.GoAroundReason.notStabilizedAirplane);
//            return;
//          }
//
//          // neni na twr, tak GA
//          if (pilot.atc != Acc.atcTwr()) {
//            parent.adviceToAtc(atc, new EstablishedOnApproachNotification(this.approach.getThreshold()));
//          }
//          isAfterStateChange = false;
//        }
//
//        if (parent.getAltitude() < this.shortFinalAltitude) {
//          isAfterStateChange = true;
//          super.setState(Airplane.State.shortFinal);
//        }
//        break;
//      case shortFinal:
//        updateAltitudeOnApproach(true);
//        updateHeadingOnApproach(Coordinates.eHeadingToRadialBehavior.aggresive);
//        if (isAfterStateChange) {
//          int MAX_SHORT_FINAL_HEADING_DIFF = 10;
//          double diff = Math.abs(parent.getTargetHeading() - this.approach.getCourse());
//          if (diff > MAX_SHORT_FINAL_HEADING_DIFF) {
//            goAround(GoingAroundNotification.GoAroundReason.notStabilizedAirplane);
//            return;
//          }
//
//          // neni na twr, tak GA
//          if (pilot.atc != Acc.atcTwr()) {
//            goAround(GoingAroundNotification.GoAroundReason.noLandingClearance);
//            return;
//          }
//          isAfterStateChange = false;
//        }
//
//        if (parent.getAltitude() == Acc.airport().getAltitude()) {
//          double gaProbability = getGoAroundProbabilityDueToWind();
//          if (Acc.rnd().nextDouble() < gaProbability) {
//            goAround(GoingAroundNotification.GoAroundReason.windGustBeforeTouchdown);
//            return;
//          } else {
//            isAfterStateChange = true;
//            super.setState(Airplane.State.landed);
//          }
//        }
//        break;
//      case landed:
//        if (Pilot.this.parent.isEmergency())
//          Pilot.this.parent.getMood().addExperience(Mood.ArrivalExperience.landedAsEmergency);
//        isAfterStateChange = false;
//        updateHeadingOnApproach(Coordinates.eHeadingToRadialBehavior.gentle);
//        break;
//      default:
//        super.throwIllegalStateException();
//    }
//  }
//
//  private double getGoAroundProbabilityDueToWind() {
//    double windGustBase =
//        Acc.weather().getWindGustSpeedInKts() - Acc.weather().getWindSpeetInKts();
//    double windRelativeHeading =
//        Headings.getDifference(Pilot.this.parent.getHeading(), Acc.weather().getWindHeading(), true);
//    double gaProbability = 0;
//    if (windGustBase > 0) {
//      double windCauseGoAroundProbability =
//          Math.sin(windRelativeHeading * DEGREES_TO_RADS);
//      if (windRelativeHeading > 90)
//        windCauseGoAroundProbability += 2 * Math.sin((windRelativeHeading - 90) * DEGREES_TO_RADS);
//      windCauseGoAroundProbability /= 2;
//      windCauseGoAroundProbability *= windGustBase;
//      gaProbability = windCauseGoAroundProbability;
//    }
//    return gaProbability;
//  }
//
//  @Override
//  public void fly() {
//    if (parent.getState() == Airplane.State.flyingIaf2Faf) {
//      flyIAFtoFAFPhase();
//    } else
//      flyApproachingPhase();
//  }
//
//
//  @Override
//  public String toLogString() {
//
//    EStringBuilder sb = new EStringBuilder();
//
//    sb.appendFormat("APP %s%s",
//        this.approach.getKind().toString(),
//        this.approach.getThreshold().getName());
//
//    return sb.toString();
//  }
//}
