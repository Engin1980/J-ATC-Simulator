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
public class Counter {
  private int count = 0;
  
  public void add(){
    count++;
  }
  
  public int get(){
    return count;
  }
}
