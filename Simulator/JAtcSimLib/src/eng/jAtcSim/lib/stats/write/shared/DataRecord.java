package eng.jAtcSim.lib.stats.write.shared;

import eng.jAtcSim.lib.stats.read.shared.DataView;

public class DataRecord {
  private double minimum;
  private double maximum;
  private double sum;
  private double count;
  private double last;
  private boolean isNew = true;

  public void add(double value) {
    if (isNew) {
      isNew = false;
      minimum = value;
      maximum = value;
      sum = value;
      count = 1;
    } else {
      if (minimum > value) minimum = value;
      if (maximum < value) maximum = value;
      sum += value;
      count++;
    }
    last = value;
  }

  public DataView toView() {
    double mean = count == 0 ? 0 : sum / count;
    return new DataView(minimum, maximum, sum, count, last);
  }
}
