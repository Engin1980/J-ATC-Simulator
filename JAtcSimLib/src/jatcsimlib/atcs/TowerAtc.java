/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.atcs;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.AirplaneList;
import jatcsimlib.commands.ClearedForTakeoffCommand;
import jatcsimlib.commands.CommandList;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.ETime;
import jatcsimlib.global.Headings;
import jatcsimlib.messaging.GoingAroundStringMessage;
import jatcsimlib.messaging.Message;
import jatcsimlib.weathers.Weather;
import jatcsimlib.world.Runway;
import jatcsimlib.world.RunwayThreshold;
import java.util.List;

/**
 *
 * @author Marek
 */
public class TowerAtc extends ComputerAtc {

  private static final int RUNWAY_CHANGE_INFO_UPDATE_INTERVAL = 10 * 60;

  private final AirplaneList readyForTakeoff = new AirplaneList();
  private final AirplaneList departings = new AirplaneList();
  private RunwayChangeInfo runwayChangeInfo = null;
  private RunwayThreshold runwayThresholdInUse = null;

  public TowerAtc(AtcTemplate template) {
    super(template);
  }

  @Override
  protected void _registerNewPlane(Airplane plane) {
    if (plane.isArrival()) {
      throw new ERuntimeException("Arriving plane cannot be registered using this method.");
    }
  }

  public RunwayThreshold getRunwayThresholdInUse() {
    return runwayThresholdInUse;
  }
  

  @Override
  protected void _elapseSecond() {
    List<Message> msgs = Acc.messenger().getMy(this, true);

    esRequestPlaneSwitchFromApp();

    for (Message m : msgs) {
      if (m.source instanceof Airplane) {
        if (m.content instanceof GoingAroundStringMessage) {
          // predavame na APP
          super.requestSwitch((Airplane) m.source);
          waitingRequestsList.add((Airplane) m.source);
        }
        continue;
      } else if (m.source != Acc.atcApp()) {
        continue;
      }

      Airplane p = m.getAsPlaneSwitchMessage().plane;
      if (waitingRequestsList.contains(p) == false) {
        p = null;
      }

      if (p == null) {
        // APP -> TWR, muze?
        p = m.getAsPlaneSwitchMessage().plane;
        if (canIAcceptFromApp(p)) {
          super.confirmSwitch(p);
          super.approveSwitch(p);
        } else {
          super.refuseSwitch(p);
        }

      } else {
        // potvrzene od APP, TWR predava na APP
        waitingRequestsList.remove(p);
        readyForTakeoff.add(p);
      }
    }

    processReadyForTakeoff();
    switchDepartings();

    // runway change
    checkForRunwayChange();
  }

  private void processReadyForTakeoff() {
    if (readyForTakeoff.isEmpty() == false && canTakeOffSomebodyNow()) {
      Airplane p = readyForTakeoff.get(0);
      readyForTakeoff.remove(0);
      departings.add(p);

      CommandList cmdList = new CommandList();
      cmdList.add(new ClearedForTakeoffCommand());
      Acc.messenger().addMessage(Message.create(this, p, cmdList));
    }
  }

  private boolean canTakeOffSomebodyNow() {
    for (Airplane p : getPrm().getPlanes(this)) {
      double dst = Coordinates.getDistanceInNM(p.getCoordinate(), Acc.threshold().getCoordinate());
      if (p.isArrival()) {
        if (dst < 5) {
          return false;
        }
      } else {
        if (readyForTakeoff.contains(p)) {
          continue;
        }
        if (p.getSpeed() == 0 && departings.contains(p) == false) {
          continue;
        }
        if (dst < 3) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean canIAcceptFromApp(Airplane p) {
    if (p.isDeparture()) {
      return false;
    }
    if (Acc.atcApp().isControllingAirplane(p) == false) {
      return false;
    }
    if (p.getAltitude() > super.acceptAltitude) {
      return false;
    }
    double dist = Coordinates.getDistanceInNM(p.getCoordinate(), Acc.airport().getLocation());
    if (dist > 15) {
      return false;
    }
    
    return true;
  }

  private void esRequestPlaneSwitchFromApp() {
    // TWR -> APP, zadost na APP
    AirplaneList plns = getPlanesReadyForApp();
    for (Airplane p : plns) {
      Acc.messenger().addMessage(Message.create(this, Acc.atcApp(),
          new PlaneSwitchMessage(p, " to you")));

      getPrm().requestSwitch(this, Acc.atcApp(), p);

      waitingRequestsList.add(p);
    }
    // opakovani starych zadosti
    List<Airplane> awaitings = waitingRequestsList.getAwaitings();
    for (Airplane p : awaitings) {
      Acc.messenger().addMessage(Message.create(this, Acc.atcApp(),
          new PlaneSwitchMessage(p, " to you (repeated)")));
    }
  }

  private AirplaneList getPlanesReadyForApp() {
    AirplaneList ret = new AirplaneList();

    for (Airplane p : getPrm().getPlanes(this)) {
      if (p.isArrival()) {
        continue;
      }
      if (getPrm().isAskedToSwitch(p)) {
        continue;
      }

      ret.add(p);
    }

    return ret;
  }

  private void switchDepartings() {
    AirplaneList tmp = new AirplaneList();
    for (Airplane p : departings) {
      if (p.getAltitude() > Acc.airport().getAltitude()) {
        tmp.add(p);
      }
    }

    for (Airplane p : tmp) {
      super.approveSwitch(p);
      departings.remove(p);
    }
  }

  private void checkForRunwayChange() {
    if (Acc.now().getTotalSeconds() % RUNWAY_CHANGE_INFO_UPDATE_INTERVAL == 0) {
      RunwayThreshold suggestedThreshold = getSuggestedThreshold();
      if (suggestedThreshold != Acc.threshold()) {
        RunwayChangeInfo rci = new RunwayChangeInfo(suggestedThreshold, Acc.now().addSeconds(15 * 60)); // change in 15 minutes
        if (this.runwayChangeInfo == null || this.runwayChangeInfo.newRunwayThreshold != suggestedThreshold) {
          this.runwayChangeInfo = rci;
          Acc.messenger().addMessage(Message.create(
              this,
              Acc.atcApp(),
              "Change to runway " + rci.newRunwayThreshold.getName() + " at " + rci.changeTime.toString()));
        }
      }
    } else if (this.runwayChangeInfo != null && this.runwayChangeInfo.changeTime.isBefore(Acc.now())){
      changeRunwayInUse(this.runwayChangeInfo.newRunwayThreshold);
      this.runwayChangeInfo = null;
    }
  }
  
  @Override
  public void init(){
    RunwayThreshold suggestedThreshold = getSuggestedThreshold();
    changeRunwayInUse(suggestedThreshold);
  }
  
  private static final int MAXIMAL_SPEED_FOR_PREFERRED_RUNWAY = 5;
  public static RunwayThreshold getSuggestedThreshold() {
    Weather w = Acc.weather();
    
    RunwayThreshold rt = null;
    
    if (w.getWindSpeetInKts() <= MAXIMAL_SPEED_FOR_PREFERRED_RUNWAY){
      for (Runway r : Acc.airport().getRunways()){
        if (r.isActive() == false) continue; // skip inactive runways
        for (RunwayThreshold t : r.getThresholds()){
          if (t.isPreferred()){
            rt = t;
            break;
          }
        }
        if (rt != null) break;
      }
    }
    
    int diff = Integer.MAX_VALUE;
    if (rt == null){
      // select runway according to wind
      for (Runway r : Acc.airport().getRunways()){
        for (RunwayThreshold t : r.getThresholds()){
          int localDiff = Headings.diff(w.getWindHeading(), (int) t.getCourse());
          if (localDiff < diff){
            diff = localDiff;
            rt = t;
          }
        }
      }
    }
    
    return rt;
  }

  private void changeRunwayInUse(RunwayThreshold newRunwayInUseThreshold) {
    Acc.messenger().addMessage(Message.create(
          this,
          Acc.atcApp(),
          "Runway in use " + newRunwayInUseThreshold.getName()));
      this.runwayThresholdInUse = newRunwayInUseThreshold;
  }

}

class RunwayChangeInfo {

  public final RunwayThreshold newRunwayThreshold;
  public final ETime changeTime;

  public RunwayChangeInfo(RunwayThreshold newRunwayThreshold, ETime changeTime) {
    this.newRunwayThreshold = newRunwayThreshold;
    this.changeTime = changeTime;
  }
}
