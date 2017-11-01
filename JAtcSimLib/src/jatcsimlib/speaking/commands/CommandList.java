/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.speaking.commands;

import jatcsimlib.messaging.IContent;

import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author Marek
 */
public class CommandList extends LinkedList<Command> implements IContent {
 
  public CommandList(){}
  public CommandList(Collection<Command> commands){
    super(commands);
  }
  
  @Override
  public CommandList clone(){
    return new CommandList(this);
  }
  
  public boolean contains (Class commandTypeClass){
    for (Command cmd : this) {
      if (cmd.getClass().equals(commandTypeClass))
        return true;
    }
    return false;
  }
}
