/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.stats;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.validation.Validator;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirproxType;
import eng.jAtcSim.lib.airplanes.moods.MoodResult;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.stats.read.StatisticsView;
import eng.jAtcSim.lib.stats.write.WriteSet;
import eng.jAtcSim.lib.stats.write.WriteSetList;
import eng.jAtcSim.lib.stats.write.specific.*;

/**
 * @author Marek
 */
public class Statistics {

  public static class CurrentPlanes {

    public int arrivals;
    public int departures;
    public int appArrivals;
    public int appDepartures;

    public void update() {
      arrivals = 0;
      departures = 0;
      appArrivals = 0;
      appDepartures = 0;
      for (Airplane airplane : Acc.planes()) {
        if (airplane.isArrival()) {
          arrivals++;
          if (airplane.getTunedAtc().getType() == Atc.eType.app) {
            appArrivals++;
          }
        } else {
          departures++;
          if (airplane.getTunedAtc().getType() == Atc.eType.app) {
            appDepartures++;
          }
        }
      }
    }
  }

  private WriteSetList writeSetList;
  private ETime nextWriteSetTime;
  private int setLengthIntervalInMinutes = 5;
  @XmlIgnore
  private CurrentPlanes currentPlanes = new CurrentPlanes();

  public SecondStats getSecondStats() {
    return writeSetList.getCurrent().secondStats;
  }

  public PlanesStats getPlanes() {
    return writeSetList.getCurrent().planes;
  }

  public MoodStatsItem getPlanesMood() {
    return writeSetList.getCurrent().planesMood;
  }

  public HoldingPointStats getHoldingPoint() {
    return writeSetList.getCurrent().holdingPoint;
  }

  public ErrorsStats getErrors() {
    return writeSetList.getCurrent().errors;
  }

  public static String toTime(double seconds) {
    String ret;
    int tmp = (int) Math.floor(seconds);
    int hrs = tmp / 3600;
    tmp = tmp % 3600;
    int min = tmp / 60;
    tmp = tmp % 60;
    int sec = tmp;
    if (hrs == 0) {
      ret = String.format("%d:%02d", min, sec);
    } else {
      ret = String.format("%d:%02d:%02d", hrs, min, sec);
    }
    return ret;
  }

  public StatisticsView createView(ETime fromTime){
    StatisticsView ret = null;
    IReadOnlyList<WriteSet> writeSets = this.writeSetList.getByTime(fromTime);
    ret = ReadToWriteConverter.convert(writeSets);
    return ret;
  }

  public Statistics(int setLengthIntervalInMinutes) {
    Validator.check(setLengthIntervalInMinutes > 0);
    this.setLengthIntervalInMinutes = setLengthIntervalInMinutes;
  }

  @XmlConstructor
  private Statistics() {
  }

  public void init(){
    this.writeSetList = new WriteSetList();
    this.writeSetList.createNewSet();
    this.nextWriteSetTime = Acc.now().addMinutes(5); //TODO this should be taken from app-settings
  }

  public void secondElapsed() {

    if (Acc.now().isAfter(this.nextWriteSetTime))
    {
      this.writeSetList.createNewSet();
      this.nextWriteSetTime = Acc.now().addMinutes(this.setLengthIntervalInMinutes);
    }

    this.currentPlanes.update();

    WriteSet ws = this.writeSetList.getCurrent();
    ws.planes.getPlanesUnderApp().getArrivals().add(this.currentPlanes.appArrivals);
    ws.planes.getPlanesUnderApp().getDepartures().add(this.currentPlanes.appDepartures);

    ws.planes.getPlanesInSim().getArrivals().add(this.currentPlanes.arrivals);
    ws.planes.getPlanesInSim().getDepartures().add(this.currentPlanes.departures);

    ws.holdingPoint.getCount().add(Acc.atcTwr().getNumberOfPlanesAtHoldingPoint());

    int tmp;

    tmp = Acc.planes().count(q->q.getAirprox() == AirproxType.full);
    ws.errors.getAirproxes().add(tmp);

    tmp = Acc.planes().count(q->q.isMrvaError());
    ws.errors.getMrvas().add(tmp);
  }


  public int getCountOfPlanes() {
    return Acc.planes().size();
  }

  public IReadOnlyList<MoodResult> getFullMoodHistory() {
    IList<MoodResult> ret = this.writeSetList.getFullMoodHistory();
    return ret;
  }
}

