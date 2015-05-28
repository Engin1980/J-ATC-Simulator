/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.commands;

/**
 *
 * @author Marek Vajgl
 */
public abstract class Answer extends Command {
  
  private final Command origin;

  public Answer(Command origin) {
    this.origin = origin;
  }

  public Command getOrigin() {
    return origin;
  }
}
