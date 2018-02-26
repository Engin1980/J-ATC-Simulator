package eng.jAtcSim.lib.stats;

public class Maxim {
  private double value = Double.MIN_VALUE;

  public void set(double value){
    if (this.value < value) this.value = value;
  }

  public double get() {
    return value;
  }
}
