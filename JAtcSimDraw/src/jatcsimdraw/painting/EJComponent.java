/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.painting;

import jatcsimdraw.shared.EventListener;
import jatcsimdraw.shared.EventManager;
import java.awt.Graphics;
import javax.swing.JComponent;

/**
 *
 * @author Marek
 */
public class EJComponent extends JComponent {

  public EJComponent() {
    this.setFocusable(true); // to let key-press is working
  }
  
  @Override
  public void paint(Graphics g) {
    paintEM.raise(g);
  }
  
  public final EventManager<EJComponent, EventListener<EJComponent, Graphics>, Graphics> paintEM = 
      new EventManager(this);
}
