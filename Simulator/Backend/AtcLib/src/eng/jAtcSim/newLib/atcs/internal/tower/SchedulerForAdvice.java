package eng.jAtcSim.newLib.atcs.internal.tower;

import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.newXmlUtils.annotations.XmlConstructor;

public class SchedulerForAdvice {

//                      <checkIntervals __item_type="int" __type="eng.eSystem.collections.EList">
//                      <item __type="java.lang.Integer">1800</item>
//                      <item __type="java.lang.Integer">900</item>
//                      <item __type="java.lang.Integer">600</item>
//                      <item __type="java.lang.Integer">300</item>
//                    </checkIntervals>
//  se načítá blbě, bo to čte list a má to být array

  private boolean approved;
  private int[] checkIntervals;
  private int lastAnnouncedSecond;
  private EDayTimeStamp scheduledTime;

  @XmlConstructor
  private SchedulerForAdvice() {
    PostContracts.register(this, () -> this.scheduledTime != null);
  }

  public SchedulerForAdvice(EDayTimeStamp scheduledTime, int[] checkIntervalsInMinutes) {
    resetScheduledTime(scheduledTime);
    this.scheduledTime = scheduledTime;
    this.checkIntervals = new int[checkIntervalsInMinutes.length];
    for (int i = 0; i < checkIntervalsInMinutes.length; i++) {
      this.checkIntervals[i] = checkIntervalsInMinutes[i] * 60;
    }
  }

  public int getMinutesLeft() {
    int ret = scheduledTime.getValue() - Context.getShared().getNow().getValue();
    return ret;
  }

  public EDayTimeStamp getScheduledTime() {
    return scheduledTime;
  }

  public int getSecondsLeft() {
    int ret = scheduledTime.getValue() - Context.getShared().getNow().getValue();
    return ret;
  }

  public boolean isElapsed() {
    boolean ret = approved ||
            (scheduledTime != null && scheduledTime.isBeforeOrEq(Context.getShared().getNow().toStamp()));
    return ret;
  }

  public void nowAnnounced() {
    this.lastAnnouncedSecond = this.getSecondsLeft();
  }

  public final void resetScheduledTime(EDayTimeStamp time) {
    this.scheduledTime = time;
    this.lastAnnouncedSecond = Integer.MAX_VALUE;
  }

  public void setApprovedTrue() {
    this.approved = true;
  }

  public void setCheckIntervals(int[] checkIntervals) {
    assert checkIntervals != null && checkIntervals.length > 0;
    this.checkIntervals = checkIntervals;
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
}
