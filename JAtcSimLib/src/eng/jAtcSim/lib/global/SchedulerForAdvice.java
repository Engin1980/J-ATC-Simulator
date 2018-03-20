package eng.jAtcSim.lib.global;

import eng.jAtcSim.lib.Acc;

public class SchedulerForAdvice {

  private ETime scheduledTime;
  private int lastAnnouncedSecond;
  private int[] checkIntervals = new int[]{30 * 60, 15 * 60, 10 * 60, 5 * 60};
  private boolean approved;

  public SchedulerForAdvice(ETime scheduledTime) {
    resetScheduledTime(scheduledTime);
    this.scheduledTime = scheduledTime;
  }

  public void nowAnnounced() {
    this.lastAnnouncedSecond = this.getSecondsLeft();
  }

  public ETime getScheduledTime() {
    return scheduledTime;
  }

  public void setApprovedTrue() {
    this.approved = true;
  }

  public void setCheckIntervals(int[] checkIntervals) {
    assert checkIntervals != null && checkIntervals.length > 0;
    this.checkIntervals = checkIntervals;
  }

  public final void resetScheduledTime(ETime time) {
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
    int ret = scheduledTime.getTotalMinutes() - Acc.now().getTotalMinutes();
    return ret;
  }

  public int getSecondsLeft() {
    int ret = scheduledTime.getTotalSeconds() - Acc.now().getTotalSeconds();
    return ret;
  }

  public boolean isElapsed() {
    boolean ret = approved ||
        (scheduledTime != null && scheduledTime.isBeforeOrEq(Acc.now()));
    return ret;
  }
}
