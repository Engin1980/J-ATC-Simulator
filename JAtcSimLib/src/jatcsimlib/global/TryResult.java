/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.global;

/**
 *
 * @author Marek Vajgl
 * @param <T>
 */
public class TryResult<T> {
  public final T result;
  public final Exception exceptionOrNull;
  public final boolean isSuccess;

  public TryResult(T result, Exception exceptionOrNull) {
    this.result = result;
    this.exceptionOrNull = exceptionOrNull;
    this.isSuccess = this.exceptionOrNull == null;
  }

  public TryResult(T result) {
    this(result, null);
  }
  
}
