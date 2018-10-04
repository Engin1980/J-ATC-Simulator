package eng.jAtcSim.lib.stats.read.shared;

public class DataView {
  protected double minimum;
  protected double maximum;
  protected double mean;
  protected double count;
  protected double current;

  public DataView(DataView other) {
    this.minimum = other.minimum;
    this.maximum = other.maximum;
    this.mean = other.mean;
    this.count = other.count;
    this.current = other.current;
  }

  public DataView(double minimum, double maximum, double mean, double count, double current) {
    this.minimum = minimum;
    this.maximum = maximum;
    this.mean = mean;
    this.count = count;
    this.current = current;
  }

  public void mergeWith(DataView other){
    this.minimum = Math.min(this.minimum, other.minimum);
    this.maximum = Math.max(this.maximum, other.maximum);
    double sum = this.mean * this.count + other.mean * other.count;
    this.count = this.count + other.count;
    if (this.count > 0)
      this.mean = sum /this.count;
    else
      this.mean = 0;
    this.current = other.current;
  }
}
