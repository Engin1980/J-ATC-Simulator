package eng.jAtcSim.newLib.atcs.internal.tower;

import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public class RunwayCheckInfo {
  private static final int[] RWY_CHECK_ANNOUNCE_INTERVALS = new int[]{30, 15, 10, 5};

  private static final int MIN_NORMAL_MAINTENANCE_INTERVAL = 200;
  private static final int MAX_NORMAL_MAINTENANCE_INTERVAL = 240;
  private static final int NORMAL_MAINTENACE_DURATION = 5;
  private static final int MIN_SNOW_MAINTENANCE_INTERVAL = 45;
  public static final int MAX_SNOW_MAINTENANCE_INTERVAL = 180;
  public static final int MIN_SNOW_INTENSIVE_MAINTENANCE_INTERVAL = 20;
  public static final int MAX_SNOW_INTENSIVE_MAINTENANCE_INTERVAL = 45;
  private static final int SNOW_MAINENANCE_DURATION = 20;
  private static final int MIN_EMERGENCY_MAINTENANCE_DURATION = 5;
  private static final int MAX_EMERGENCY_MAINTENANCE_DURATION = 45;

  public static RunwayCheckInfo createImmediateAfterEmergency() {
    int closeDuration = SharedAcc.getRnd().nextInt(MIN_EMERGENCY_MAINTENANCE_DURATION, MAX_EMERGENCY_MAINTENANCE_DURATION);
    RunwayCheckInfo ret = new RunwayCheckInfo(0, closeDuration);
    return ret;
  }

  public static RunwayCheckInfo createNormal(boolean isInitial) {
    int maxTime;
    if (isInitial)
      maxTime = SharedAcc.getRnd().nextInt(MAX_NORMAL_MAINTENANCE_INTERVAL);
    else
      maxTime = SharedAcc.getRnd().nextInt(MIN_NORMAL_MAINTENANCE_INTERVAL, MAX_NORMAL_MAINTENANCE_INTERVAL);

    RunwayCheckInfo ret = new RunwayCheckInfo(maxTime, NORMAL_MAINTENACE_DURATION);
    return ret;
  }

  public static RunwayCheckInfo createSnowCleaning(boolean isInitial, boolean isIntensive) {
    int maxTime = isIntensive
        ? SharedAcc.getRnd().nextInt(MIN_SNOW_INTENSIVE_MAINTENANCE_INTERVAL, MAX_SNOW_INTENSIVE_MAINTENANCE_INTERVAL)
        : SharedAcc.getRnd().nextInt(MIN_SNOW_MAINTENANCE_INTERVAL, MAX_SNOW_MAINTENANCE_INTERVAL);
    if (isInitial)
      maxTime = SharedAcc.getRnd().nextInt(maxTime);

    RunwayCheckInfo ret = new RunwayCheckInfo(maxTime, SNOW_MAINENANCE_DURATION);
    return ret;
  }
  private int expectedDurationInMinutes;
  private EDayTimeStamp realDurationEnd;
  private SchedulerForAdvice scheduler;

  private RunwayCheckInfo(int minutesToNextCheck, int expectedDurationInMinutes) {
    EDayTimeStamp et = SharedAcc.getNow().toStamp().addMinutes(minutesToNextCheck);
    this.scheduler = new SchedulerForAdvice(et, RWY_CHECK_ANNOUNCE_INTERVALS);
    this.expectedDurationInMinutes = expectedDurationInMinutes;
  }

  public boolean isActive() {
    return scheduler == null;
  }

  public void start() {
    double durationRangeSeconds = expectedDurationInMinutes * 60 * 0.2d;
    int realDurationSeconds = (int) SharedAcc.getRnd().nextDouble(
        expectedDurationInMinutes * 60 - durationRangeSeconds,
        expectedDurationInMinutes * 60 + durationRangeSeconds);
    realDurationEnd = SharedAcc.getNow().toStamp().addSeconds(realDurationSeconds);
    this.scheduler = null;
  }

  public int getExpectedDurationInMinutes() {
    return expectedDurationInMinutes;
  }

  public EDayTimeStamp getRealDurationEnd() {
    return realDurationEnd;
  }

  public SchedulerForAdvice getScheduler() {
    return scheduler;
  }
}