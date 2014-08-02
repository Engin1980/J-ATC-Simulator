/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.types;

/**
 *
 * @author Marek
 * @param <T> Type of item in Collection.
 * @param <K> Type of key of the item.
 */
public class KeyList<T extends KeyItem<K>, K> extends java.util.ArrayList<T> {
  public T get(K key){
    for (T item : this){
      if (item.getKey().equals(key)){
        return item;
      }
    }
    return null;
  }
}
