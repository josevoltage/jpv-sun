package mx.lux.pos.ui.view.dialog

import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import mx.lux.pos.ui.model.Item
import mx.lux.pos.ui.resources.UI_Standards
import net.miginfocom.swing.MigLayout
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang3.time.DateUtils

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.List

class WarrantySelectionDialog extends JDialog {

  private def sb = new SwingBuilder()

  private DefaultTableModel warrantyModel
  private JTable tWarrantis
  private JLabel lblWarning
  private JRadioButton rbArm1
  private JRadioButton rbArm2
  private JRadioButton rbArm3
  private JRadioButton rbArm4

  private Boolean arm3 = false
  private Boolean arm4 = false
  private Item armazon1 = new Item()
  private Item armazon2 = new Item()
  private Item armazon3 = new Item()
  private Item armazon4 = new Item()

  Item selection
  Boolean canceled
  String garantia
  private List<Item> lstItems = new ArrayList<>() as List<Item>

  WarrantySelectionDialog( List<Item> lstArm, Item itemGar ) {
    this.lstItems.addAll( lstArm )
    armazon1 = lstItems.get(0)
    armazon2 = lstItems.get(1)
    if( lstItems.size() == 3 ){
      arm3 = true
      armazon3 = lstItems.get(2)
    }
    if( lstItems.size() == 4 ){
      arm4 = true
      armazon4 = lstItems.get(3)
    }
    garantia = itemGar.name
    canceled = false
    buildUI()
  }

  // UI Layout Definition
  void buildUI( ) {
    sb.dialog( this,
        title: "Seleccionar Armazon",
        resizable: true,
        pack: true,
        modal: true,
        preferredSize: [ 250, 200 ],
        location: [ 200, 250 ],
    ) {
      panel() {
        borderLayout()
        panel( constraints: BorderLayout.CENTER, layout: new MigLayout( "wrap ", "[grow,fill]", "10[]10[]" ) ) {
          ButtonGroup group = new ButtonGroup()
          rbArm1 = radioButton( text: armazon1.name )
          group.add(rbArm1)
          rbArm1.addActionListener( new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
              selection = armazon1
            }
          })
          rbArm2 = radioButton( text: armazon2.name )
          group.add(rbArm2)
          rbArm2.addActionListener( new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
              selection = armazon2
            }
          })
          rbArm3 = radioButton( text: armazon3.name, constraints: 'hidemode 3', visible: arm3 )
          group.add(rbArm3)
          rbArm3.addActionListener( new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
              selection = armazon3
            }
          })
          rbArm4 = radioButton( text: armazon4.name, constraints: 'hidemode 3', visible: arm4 )
          group.add(rbArm4)
          rbArm4.addActionListener( new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
              selection = armazon4
            }
          })
          lblWarning = label( text: "Seleccione un armazon.", foreground: UI_Standards.WARNING_FOREGROUND, constraints: 'hidemode 3', visible: false )
        }
        panel( constraints: BorderLayout.PAGE_END ) {
          borderLayout()
          panel( constraints: BorderLayout.LINE_END ) {
            button( text: "Aplicar", preferredSize: UI_Standards.BUTTON_SIZE,
                actionPerformed: { onButtonOk() }
            )
            button( text: "Cancelar", preferredSize: UI_Standards.BUTTON_SIZE,
                actionPerformed: { onButtonCancel() }
            )
          }
        }
      }

    }
  }

  // UI Management
  protected void refreshUI( ) {

  }

  // Public Methods
  void activate( ) {
    refreshUI()
    setVisible( true )
  }

  Item getSelectedFrame( ) {
    return selection
  }

  Boolean getCanceled( ) {
    return canceled
  }

  // UI Response
  protected void onButtonCancel( ) {
    selection = null
    canceled = true
    dispose()
  }

  protected void onButtonClose( ) {
    selection = null
    dispose()
  }

  protected void onButtonOk( ) {
    if (rbArm1.selected || rbArm2.selected || rbArm3.selected || rbArm4.selected) {
      //selection = this.lstItems.getAt(index)
      dispose()
    } else {
      lblWarning.visible = true
      /*sb.doLater {
        dispose()
      }*/
    }

  }
}
