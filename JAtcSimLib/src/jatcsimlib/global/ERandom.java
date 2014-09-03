/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.global;

import java.util.Random;

/**
 *
 * @author Marek
 */
public class ERandom extends Random {
  public int getInt (int fromInclusive, int toExclusive){
    return super.nextInt(toExclusive) + fromInclusive;
  }
}
