package mx.lux.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.lux.pos.service.business.Registry
import mx.lux.pos.ui.controller.OrderController
import mx.lux.pos.ui.resources.UI_Standards
import mx.lux.pos.ui.view.verifier.DateVerifier
import net.miginfocom.swing.MigLayout
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang3.time.DateUtils
import mx.lux.pos.ui.model.Order
import mx.lux.pos.ui.model.Payment

import javax.swing.*
import java.awt.*
import java.text.DateFormat
import java.text.SimpleDateFormat

class ReprintVoucherDialog extends JDialog {

  private def sb = new SwingBuilder()

  private JTextField txtTicket
  private JLabel lblWarning

  private static final String TAG_TARJETA_CREDITO = "TC"
  private static final String TAG_TARJETA_DEBITO = "TD"

    ReprintVoucherDialog( ) {
    buildUI()
  }

  // UI Layout Definition
  void buildUI( ) {
    sb.dialog( this,
        title: "Reimpresion de voucher",
        resizable: true,
        pack: true,
        modal: true,
        preferredSize: [ 270, 170 ],
        location: [ 200, 250 ],
    ) {
      panel() {
        borderLayout()
        panel( constraints: BorderLayout.CENTER, layout: new MigLayout( "wrap 2", "20[][grow,fill]40", "20[]10[]" ) ) {
          label( text: "Ticket:" )
          txtTicket = textField( maximumSize: [ 70, 30 ] )
          lblWarning = label( text: "<html>La factura no existe<br>o no tiene pagos con tarjeta.</html>", constraints: 'hidemode 3,span', visible: false, foreground: UI_Standards.WARNING_FOREGROUND )
        }
        panel( constraints: BorderLayout.PAGE_END ) {
          borderLayout()
          panel( constraints: BorderLayout.LINE_END ) {
            button( text: "Imprimir", preferredSize: UI_Standards.BUTTON_SIZE,
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

  // UI Management

  // Public Methods

  // UI Response
  protected void onButtonCancel( ) {
    dispose()
  }

  protected void onButtonOk( ) {
    String ticket = StringUtils.trimToEmpty((Registry.currentSite+2000).toString())+"-"+StringUtils.trimToEmpty(txtTicket.text)
    Order order = OrderController.findOrderByTicket( ticket )
    Boolean hasPayCard = false
    for(Payment pago : order?.payments){
      if( StringUtils.trimToEmpty(pago.terminalId).contains("|") ){
        hasPayCard = true
      }
    }
    if( order != null && hasPayCard ){
      OrderController.printVoucherTpv( order.id, true )
      OrderController.saveLogTpv( StringUtils.trimToEmpty(order.id) )
      dispose()
    } else {
      lblWarning.visible = true
    }
  }
}
