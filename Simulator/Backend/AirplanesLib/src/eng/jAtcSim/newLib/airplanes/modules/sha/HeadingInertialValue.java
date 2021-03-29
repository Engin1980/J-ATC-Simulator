package eng.jAtcSim.newLib.airplanes.modules.sha;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Headings;
import eng.eSystem.validation.EAssert;
import exml.IXPersistable;
import exml.annotations.XConstructor;
import exml.loading.XLoadContext;
import exml.saving.XSaveContext;

class HeadingInertialValue implements IXPersistable {
  private final double maxInertia;
  private final double maxInertiaChange;
  protected double value;
  private final IList<Double> thresholds = new EList<>();
  private int inertiaStep = 0;

  HeadingInertialValue(double value,
                       double maxInertia,
                       double maxInertiaChange) {
    this.value = value;
    this.maxInertia = maxInertia;
    this.maxInertiaChange = maxInertiaChange;
    buildHashMap();
  }

  @XConstructor
  private HeadingInertialValue(double maxInertia, double maxInertiaChange) {
    this.maxInertia = maxInertia;
    this.maxInertiaChange = maxInertiaChange;
  }

  public void add(double val) {
    if (Math.abs(val) < maxInertiaChange) {
      this.value += val;
      this.inertiaStep = 0;
    } else {
      int stepBlock = getFromHashMap(val);
      if (stepBlock < inertiaStep)
        inertiaStep--;
      else if (stepBlock > inertiaStep)
        inertiaStep++;

      double step = inertiaStep * maxInertiaChange;
      step = Math.min(step, this.maxInertia);
      if (val > 0)
        step = Math.min(step, val);
      else
        step = Math.max(step, val);

      this.value += step;
    }

    this.value = Headings.to(this.value);
  }

  public double getInertia() {
    return inertiaStep * maxInertiaChange;
  }

  public double getMaxInertia() {
    return maxInertia;
  }

  public double getValue() {
    return value;
  }

  public void reset(double value) {
    this.value = value;
    this.inertiaStep = 0;
  }

  private void buildHashMap() {
    EAssert.isTrue(this.thresholds.isEmpty());

    IList<Double> tmp = this.thresholds;
    int index = 1;
    int cumIndex = 1;
    double maxThr = maxInertia / maxInertiaChange + 1;
    double thr = 0;
    while (thr <= maxThr) {
      thr = cumIndex * this.maxInertiaChange;
      tmp.add(thr);
      index++;
      cumIndex += index;
    }
    tmp.removeAt(0);
  }

  private int getFromHashMap(double val) {
    boolean isNeg = false;
    if (val < 0) {
      isNeg = true;
      val = -val;
    }
    int ret = 0;
    while (ret < this.thresholds.size()) {
      if (val >= this.thresholds.get(ret))
        ret++;
      else
        break;
    }
    ret = ret + 1;
    if (isNeg)
      ret = -ret;
    return ret;
  }

  void resetInertia() {
    if (this.inertiaStep != 0)
      this.inertiaStep = 0;
  }
}
