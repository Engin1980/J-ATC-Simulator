package eng.jAtcSim.lib.airplanes.moods;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.global.ETime;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Mood {

  private static class Experience<T> {
    public final ETime time;
    public final T type;

    public Experience(ETime time, T type) {
      this.time = time.clone();
      this.type = type;
    }
  }

  public enum ArrivalExperience {
    shortcutToIafAbove100,
    canceledFL100speedRestriction,
    leveledFlight,
    holdCycleFinished,
    goAroundNotCausedByPilot,
    landedAsEmergency,
    divertOrderedByAtcWhenNoEmergency,
    divertOrderedByCaptain
  }

  public enum DepartureExperience {
    shortcutToExitPointBelow100,
    shortctuToExitPointAbove100,
    holdCycleFinished,
    leveledFlight,
    departureAltitudeRestrictionCanceled,
    divertedAsEmergency
  }

  public enum SharedExperience {
    airprox,
    mrvaViolation,
    prohibitedAreaViolation
  }

  private static final String SHORTCUT_TO_EXIT = "Shortcut to SID final point";
  private static final String DEPARTURE_ALTITUDE_RESTRICTION_CANCELED = "Altitude restriction on departure lifted";
  private static final String DIVERTED_AS_EMERGENCY = "Diverted as emergency";
  private static final String SHORTCUT_TO_IAF_ABOVE_FL100 = "Got shortcut to IAF";
  private static final String SPEED_RESTRICTION_UNDER_FL100_CANCELED = "Speed restriction below FL100 canceled";
  private static final String NO_DESCEND_DURING_APPROACH = "No descend during approach";
  private static final String LANDED_AS_EMERGENCY = "Successful emergency landing";
  private static final String HOLDING = "Flown holding pattern";
  private static final String GO_AROUND = "Go around (ATC fault)";
  private static final String DIVERT = "Diverted";
  private static final String AIRPROX = "Separation disrupted";
  private static final String MRVA = "MRVA disrupted";
  private static final String PROHIBITED_AREA = "Prohibited area entered";
  private static final String DELAY = "Delay";
  private static final double DELAY_PER_MINUTE_POINTS = -1;

  private IList<Experience<ArrivalExperience>> arrivalExperiences;
  private IList<Experience<DepartureExperience>> departureExperiences;
  private IList<Experience<SharedExperience>> sharedExperiences;

  public Mood() {
    this.arrivalExperiences = new EList<>();
    this.departureExperiences = new EList<>();
    this.sharedExperiences = new EList<>();
  }

  public void experience(ArrivalExperience kindOfExperience) {
    this.arrivalExperiences.add(new Experience<>(Acc.now(), kindOfExperience));
  }

  public void experience(DepartureExperience kindOfExperience) {
    this.departureExperiences.add(new Experience<>(Acc.now(), kindOfExperience));
  }

  public void experience(SharedExperience kindOfExperience) {
    this.sharedExperiences.add(new Experience<>(Acc.now(), kindOfExperience));
  }

  public MoodResult evaluate(Callsign callsign, int delayMinutesPlusMinus) {
    IList<MoodExperienceResult> arrEvals = evaluateArrivals();
    IList<MoodExperienceResult> depEvals = evaluateDepartures();
    IList<MoodExperienceResult> sharedEvals = evaluateShared();
    IList<MoodExperienceResult> tmp = new EList<>();
    tmp.add(arrEvals);
    tmp.add(depEvals);
    tmp.add(sharedEvals);

    if (delayMinutesPlusMinus != 0) {
      tmp.add(new MoodExperienceResult(null,
          sf("%s (%+d minutes)",
              DELAY,
              delayMinutesPlusMinus),
          (int) (delayMinutesPlusMinus * DELAY_PER_MINUTE_POINTS)));
    }

    tmp.sort(q -> q.getTime() == null ? new ETime(0) : q.getTime());

    MoodResult ret = new MoodResult(Acc.now().clone(), callsign, tmp);
    return ret;
  }

  private IList<MoodExperienceResult> evaluateArrivals() {
    IList<MoodExperienceResult> ret = new EList<>();

    Experience<ArrivalExperience> tmp;
    IList<Experience<ArrivalExperience>> tmps;
    int cnt;

    // positive
    tmp = arrivalExperiences.tryGetFirst(q -> q.type == ArrivalExperience.shortcutToIafAbove100);
    if (tmp != null)
      ret.add(new MoodExperienceResult(tmp.time, SHORTCUT_TO_IAF_ABOVE_FL100, 5));

    tmp = arrivalExperiences.tryGetFirst(q -> q.type == ArrivalExperience.canceledFL100speedRestriction);
    if (tmp != null)
      ret.add(new MoodExperienceResult(tmp.time, SPEED_RESTRICTION_UNDER_FL100_CANCELED, 5));

    tmp = arrivalExperiences.tryGetFirst(q -> q.type == ArrivalExperience.leveledFlight);
    if (tmp == null)
      ret.add(new MoodExperienceResult(null, NO_DESCEND_DURING_APPROACH, 20));

    tmp = arrivalExperiences.tryGetFirst(q -> q.type == ArrivalExperience.landedAsEmergency);
    if (tmp != null)
      ret.add(new MoodExperienceResult(null, LANDED_AS_EMERGENCY, 50));

    // negative
    tmps = arrivalExperiences.where(q -> q.type == ArrivalExperience.holdCycleFinished);
    for (Experience<ArrivalExperience> tm : tmps) {
      ret.add(new MoodExperienceResult(tm.time, HOLDING, -3));
    }

    tmps = arrivalExperiences.where(q -> q.type == ArrivalExperience.goAroundNotCausedByPilot);
    for (Experience<ArrivalExperience> tm : tmps) {
      ret.add(new MoodExperienceResult(tm.time, GO_AROUND, -10));
    }

    tmp = arrivalExperiences.tryGetFirst(q -> q.type == ArrivalExperience.divertOrderedByAtcWhenNoEmergency);
    if (tmp != null) {
      ret.add(new MoodExperienceResult(tmp.time, DIVERT, -20));
    }

    tmp = arrivalExperiences.tryGetFirst(q -> q.type == ArrivalExperience.divertOrderedByCaptain);
    if (tmp != null) {
      ret.add(new MoodExperienceResult(tmp.time, DIVERT, -50));
    }

    return ret;
  }

  private IList<MoodExperienceResult> evaluateDepartures() {
    IList<MoodExperienceResult> ret = new EList<>();

    Experience<DepartureExperience> tmp;
    IList<Experience<DepartureExperience>> tmps;
    int cnt;

    // positive
    tmp = departureExperiences.tryGetFirst(q -> q.type == DepartureExperience.shortctuToExitPointAbove100);
    if (tmp != null)
      ret.add(new MoodExperienceResult(tmp.time, SHORTCUT_TO_EXIT, 10));
    else {
      tmp = departureExperiences.tryGetFirst(q -> q.type == DepartureExperience.shortcutToExitPointBelow100);
      if (tmp != null)
        ret.add(new MoodExperienceResult(tmp.time, SHORTCUT_TO_EXIT, 5));
    }

    tmp = departureExperiences.tryGetFirst(q -> q.type == DepartureExperience.departureAltitudeRestrictionCanceled);
    if (tmp != null)
      ret.add(new MoodExperienceResult(tmp.time, DEPARTURE_ALTITUDE_RESTRICTION_CANCELED, 5));

    tmp = departureExperiences.tryGetFirst(q -> q.type == DepartureExperience.divertedAsEmergency);
    if (tmp != null)
      ret.add(new MoodExperienceResult(null, DIVERTED_AS_EMERGENCY, 15));

    // negative
    tmp = departureExperiences.tryGetFirst(q -> q.type == DepartureExperience.leveledFlight);
    if (tmp != null)
      ret.add(new MoodExperienceResult(null, NO_DESCEND_DURING_APPROACH, -5));

    tmps = departureExperiences.where(q -> q.type == DepartureExperience.holdCycleFinished);
    for (Experience<DepartureExperience> tm : tmps) {
      ret.add(new MoodExperienceResult(tm.time, HOLDING, -10));
    }

    return ret;
  }

  private IList<MoodExperienceResult> evaluateShared() {
    IList<MoodExperienceResult> ret = new EList<>();

    Experience<DepartureExperience> tmp;
    IList<Experience<DepartureExperience>> tmps;
    int cnt;

    // positive

    // negative
    cnt = sharedExperiences.count(q -> q.type == SharedExperience.airprox);
    if (cnt > 0) {
      ret.add(new MoodExperienceResult(null, AIRPROX + sf(" (%s seconds)", cnt), -1));
    }

    cnt = sharedExperiences.count(q -> q.type == SharedExperience.airprox);
    if (cnt > 0) {
      ret.add(new MoodExperienceResult(null, MRVA + sf(" (%s seconds)", cnt), -1));
    }

    cnt = sharedExperiences.count(q -> q.type == SharedExperience.airprox);
    if (cnt > 0) {
      ret.add(new MoodExperienceResult(null, PROHIBITED_AREA + sf(" (%s seconds)", cnt), -1));
    }

    return ret;
  }

}
