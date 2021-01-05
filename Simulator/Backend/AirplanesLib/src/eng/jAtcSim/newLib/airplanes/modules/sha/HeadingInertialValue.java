package eng.jAtcSim.newLib.airplanes.modules.sha;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Headings;
import eng.newXmlUtils.annotations.XmlConstructor;
import eng.newXmlUtils.annotations.XmlConstructorParameter;
import exml.ISimPersistable;
import exml.XContext;

class HeadingInertialValue implements ISimPersistable {
  private final double maxInertia;
  private final double maxInertiaChange;
  protected double value;
  private IList<Double> thresholds = new EList<>();
  private int inertiaStep = 0;

  @Override
  public void save(XElement elm, XContext ctx) {
    ctx.saver.saveFieldItems(this, "thresholds", Double.class,  elm);
    ctx.saver.saveRemainingFields(this, elm);
  }

  @Override
  public void load(XElement elm, XContext ctx) {

  }

  @XmlConstructor
  @XmlConstructorParameter(index = 1, valueString = "10")
  @XmlConstructorParameter(index = 2, valueString = "2")
  HeadingInertialValue(double value,
                       double maxInertia,
                       double maxInertiaChange) {
    this.value = value;
    this.maxInertia = maxInertia;
    this.maxInertiaChange = maxInertiaChange;
    buildHashMap();
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
    IList<Double> tmp = new EList<>();
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

    this.thresholds = tmp;
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
