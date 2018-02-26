/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.stats;

/**
 *
 * @author Marek
 */
public class Meaner {

  private double sum = 0;
  private int count = 0;

  public void add(double value) {
    sum += value;
    count++;
  }

  public double get() {
    if (count == 0) {
      return 0;
    } else {
      return sum / (double) count;
    }
  }
  
  public int getCount(){
    return  count;
  }
  public double getSum(){
    return sum;
  }
}
