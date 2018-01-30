/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.global;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Marek
 */
public class ReadOnlyList<T> implements Iterable<T> {
  
  private final List<T> inner;
  
  public ReadOnlyList(List<T> list){
    inner = list;
  }

  @Override
  public Iterator<T> iterator() {
    return inner.iterator();
  }
  
  public int size(){
    return inner.size();
  }
  
  public T get(int index){
    return inner.get(index);
  }
}
