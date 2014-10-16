/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.stats;

/**
 *
 * @author Marek
 */
public class Meaner {

  private long sum = 0;
  private long count = 0;

  public void add(int value) {
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
    return (int) count;
  }
  public int getSum(){
    return (int) sum;
  }
}
