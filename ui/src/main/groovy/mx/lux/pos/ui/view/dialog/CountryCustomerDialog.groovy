package mx.lux.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.lux.pos.ui.controller.CustomerController
import mx.lux.pos.ui.resources.UI_Standards
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout

import javax.swing.*
import java.awt.Component

class CountryCustomerDialog extends JDialog {

  private def sb = new SwingBuilder()

  private String selectedCountry
  private JComboBox cbPaises
  private List<String> lstPaises = new ArrayList<String>()
  private JLabel lblWarning
  public boolean button = false
  private static final String MSJ_SELECCIONE_PAIS = 'Es necesario seleccionar un pa\u00eds'

    CountryCustomerDialog( Component parent ) {
        lstPaises = CustomerController.countries()
        buildUI( parent )
  }

  // UI Layout Definition
  void buildUI( Component parent  ) {
    sb.dialog( this,
        title: "Seleccionar Pa\u00eds",
        resizable: true,
        pack: true,
        location: parent.locationOnScreen,
        modal: true,
        preferredSize: [ 300, 220 ],
    ) {
      panel() {
        borderLayout()
        panel( constraints: BorderLayout.CENTER, layout: new MigLayout( "wrap 2", "20[][grow,fill]60", "20[]10[]" ) ) {
          label( text: "Seleccione el pa\u00eds de origen del cliente", constraints: "span 2" )
          label( text: " ", constraints: "span 2" )
          label( text: "Pa\u00eds:" )
          cbPaises = comboBox( items: lstPaises, editable: true )
          lblWarning = label( text: MSJ_SELECCIONE_PAIS, constraints: "span 2, hidemode 3", visible: false, foreground: UI_Standards.WARNING_FOREGROUND, )
        }

        panel( constraints: BorderLayout.PAGE_END ) {
          borderLayout()
          panel( constraints: BorderLayout.LINE_END ) {
            button( text: "Aceptar", preferredSize: UI_Standards.BUTTON_SIZE,
                actionPerformed: { onButtonOk() }
            )
            button( text: "Cerrar", preferredSize: UI_Standards.BUTTON_SIZE,
                actionPerformed: { onButtonCancel() }
            )
          }
        }
      }

    }
  }

    String getPais(){
        return selectedCountry
    }
  // UI Management
  protected void refreshUI( ) {

  }

  // Public Methods
  void activate( ) {
    refreshUI()
    setVisible( true )
  }

  // UI Response
  protected void onButtonCancel( ) {
    button = false
    dispose()
  }

  protected void onButtonOk( ) {
    selectedCountry = cbPaises.selectedItem.toString().trim()
      if( selectedCountry.length() > 0 ){
          button = true
          dispose()
      } else {
          lblWarning.visible = true
      }
  }
}
