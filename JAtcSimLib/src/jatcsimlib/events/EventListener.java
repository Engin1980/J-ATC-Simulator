/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.events;

/**
 *
 * @author Marek
 */
public abstract class EventListener<Tsender, TeventArgs> {
  public abstract void raise (Tsender parent, TeventArgs e);
}
