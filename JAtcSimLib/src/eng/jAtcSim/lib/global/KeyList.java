/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.global;

import eng.jAtcSim.lib.exceptions.ERuntimeException;

/**
 *
 * @author Marek
 * @param <TValue> Type of item in Collection.
 * @param <TKey> Type of key of the item.
 */
public class KeyList<TValue extends KeyItem<TKey>, TKey> extends java.util.ArrayList<TValue> {
  public TValue tryGet(TKey key){
    for (TValue item : this){
      if (item.getKey().equals(key)){
        return item;
      }
    }
    return null;
  }
  
  public TValue get(TKey key){
    for (TValue item : this){
      if (item.getKey().equals(key)){
        return item;
      }
    }
    throw new ERuntimeException("No such element in KeyList - key: " + key.toString());
  }
}
