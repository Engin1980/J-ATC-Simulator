/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim.startup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Marek
 */
public class XmlFileSelectorExtender {

  private final JTextField txt;
  private final JButton btn;
  private File file = null;
  private XmlFileSelectorFileChangedHandler handler;

  public XmlFileSelectorExtender(JTextField txt, JButton btn, File file) {
    this(txt, btn, file, null);
  }

  public XmlFileSelectorExtender(JTextField txt, JButton btn, File file,
    XmlFileSelectorFileChangedHandler handler) {
    this.txt = txt;
    this.btn = btn;
    this.btn.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        processDialog();
      }
    });
    this.file = file;
    this.handler = handler;
    if (file != null) {
      if (handler != null) {
        handler.fileChanged(file.getAbsolutePath());
      }
    } else {
      // file == null
      this.txt.setEditable(false);
      this.txt.setText("< browse for file >");
      this.btn.setText("(browse)");
    }
  }

  public XmlFileSelectorExtender(JTextField txt, JButton btn) {
    this(txt, btn, null, null);
  }

  public XmlFileSelectorExtender(JTextField txt, JButton btn, XmlFileSelectorFileChangedHandler handler) {
    this(txt, btn, null, handler);
  }

  public final void setFile(String file){
    File f = new File(file);
    setFile(f);
  }
  
  public final void setFile(File file) {
    this.file = file;
    txt.setText(file.getAbsolutePath());
    if (handler != null) {
      handler.fileChanged(file.getAbsolutePath());
    }
  }

  private void processDialog() {
    JFileChooser jfc = new JFileChooser();

    jfc.setFileFilter(new FileNameExtensionFilter("XML file", "xml"));

    int res = jfc.showOpenDialog(txt);

    if (res != JFileChooser.APPROVE_OPTION) {
      return; // cancel etc.
    }
    setFile(jfc.getSelectedFile());
    if (handler != null) {
      handler.fileChanged(jfc.getSelectedFile().getAbsolutePath());
    }
  }

  public File getFile() {
    return file;
  }

}
