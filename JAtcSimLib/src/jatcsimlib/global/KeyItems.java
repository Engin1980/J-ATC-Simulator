/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.global;

import jatcsimlib.exceptions.ERuntimeException;

/**
 *
 * @author Marek
 */
public class KeyItems {

  public static <T extends KeyItem<K>, K> T tryGet(Iterable<T> lst, K key) {
    for (T item : lst) {
      if (item.getKey().equals(key)) {
        return item;
      }
    }
    return null;
  }

  public static <T extends KeyItem<K>, K> T get(Iterable<T> lst, K key) {
    for (T item : lst) {
      if (item.getKey().equals(key)) {
        return item;
      }
    }
    throw new ERuntimeException("No such element in KeyList - key: " + key.toString());
  }
}
