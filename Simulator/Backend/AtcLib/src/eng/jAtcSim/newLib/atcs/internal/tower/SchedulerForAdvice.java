package eng.jAtcSim.newLib.atcs.internal.tower;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class SchedulerForAdvice {

  private EDayTimeStamp scheduledTime;
  private int lastAnnouncedSecond;
  private int[] checkIntervals;
  private boolean approved;

  public SchedulerForAdvice(EDayTimeStamp scheduledTime, int[] checkIntervalsInMinutes) {
    resetScheduledTime(scheduledTime);
    this.scheduledTime = scheduledTime;
    this.checkIntervals = new int[checkIntervalsInMinutes.length];
    for (int i = 0; i < checkIntervalsInMinutes.length; i++) {
      this.checkIntervals[i] = checkIntervalsInMinutes[i] *60;
    }
  }

  public void nowAnnounced() {
    this.lastAnnouncedSecond = this.getSecondsLeft();
  }

  public EDayTimeStamp getScheduledTime() {
    return scheduledTime;
  }

  public void setApprovedTrue() {
    this.approved = true;
  }

  public void setCheckIntervals(int[] checkIntervals) {
    assert checkIntervals != null && checkIntervals.length > 0;
    this.checkIntervals = checkIntervals;
  }

  public final void resetScheduledTime(EDayTimeStamp time) {
    this.scheduledTime = time;
    this.lastAnnouncedSecond = Integer.MAX_VALUE;
  }

  public boolean shouldBeAnnouncedNow() {
    boolean ret = false;
    int secLeft = this.getSecondsLeft();
    for (int dit : this.checkIntervals) {
      if (this.lastAnnouncedSecond > dit && secLeft < dit) {
        this.lastAnnouncedSecond = secLeft;
        ret = true;
        break;
      }
    }
    return ret;
  }

  public int getMinutesLeft() {
    int ret = scheduledTime.getValue() - SharedAcc.getNow().getValue();
    return ret;
  }

  public int getSecondsLeft() {
    int ret = scheduledTime.getValue() - SharedAcc.getNow().getValue();
    return ret;
  }

  public boolean isElapsed() {
    boolean ret = approved ||
        (scheduledTime != null && scheduledTime.isBeforeOrEq(SharedAcc.getNow().toStamp()));
    return ret;
  }
}
