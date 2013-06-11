package mx.lux.pos.ui.view.dialog.dailyclose

import groovy.swing.SwingBuilder
import mx.lux.pos.ui.controller.DailyCloseController
import mx.lux.pos.ui.controller.PaymentController
import mx.lux.pos.ui.model.Payment
import mx.lux.pos.ui.model.Plan
import mx.lux.pos.ui.model.Terminal
import net.miginfocom.swing.MigLayout

import java.awt.event.ActionEvent
import java.awt.event.ItemEvent
import java.text.NumberFormat
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JDialog
import javax.swing.JOptionPane

class EditPaymentDialog extends JDialog {

  private SwingBuilder sb
  private Payment tmpPayment
  private JComboBox terminal
  private JComboBox plan
  private List<Terminal> terminals
  private List<Plan> plans

  EditPaymentDialog( final Payment payment ) {
    this.tmpPayment = payment ?: new Payment()
    sb = new SwingBuilder()
    terminals = PaymentController.findTerminals()
    plans = [ ]
    buildUI()
    terminalChanged( payment )
    doBindings()
  }

  private void buildUI( ) {
    String amount = NumberFormat.getCurrencyInstance( Locale.US ).format( tmpPayment?.amount ?: 0 )
    sb.dialog( this,
        title: "Factura: ${tmpPayment?.factura}, Importe: ${amount}",
        resizable: false,
        pack: true,
        modal: true,
        layout: new MigLayout( 'fill,wrap', '[fill]' )
    ) {
      panel( layout: new MigLayout( 'fill,wrap 2', '[fill,120][fill,180]' ) ) {
        label( 'Factura' )
        label( tmpPayment?.factura )
        label( 'Importe' )
        label( amount )
        label( 'Terminal' )
        terminal = comboBox( items: terminals*.description, itemStateChanged: terminalChanged )
        label( 'Plan' )
        plan = comboBox( items: plans*.description, itemStateChanged: planChanged )
      }

      panel( layout: new MigLayout( 'fill', '[right]' ) ) {
        button( text: 'Guardar', actionPerformed: doSave )
        button( text: 'Cancelar', actionPerformed: {dispose()} )
      }
    }
  }

  private void doBindings( ) {
    sb.build {
      bean( terminal, selectedItem: bind( source: tmpPayment, sourceProperty: 'terminal', mutual: true ) )
      bean( plan, selectedItem: bind( source: tmpPayment, sourceProperty: 'plan', mutual: true ) )
    }
  }

  private def terminalChanged = { ItemEvent ev ->
    if ( ev.stateChange == ItemEvent.SELECTED ) {
      Terminal terminalTmp = terminals.find { Terminal tmp ->
        tmp?.description?.equalsIgnoreCase( ev.item as String )
      }
      tmpPayment.terminalId = terminalTmp?.id
      plans = PaymentController.findPlansByTerminal( terminalTmp?.id )
      plans?.each { Plan tmp ->
        plan.addItem( tmp?.description )
      }
      plan.selectedIndex = -1
    } else {
      tmpPayment.terminalId = null
      plan.removeAllItems()
    }
  }

  private void terminalChanged ( Payment payment ) {
    Terminal terminalTmp = terminals.find { Terminal tmp ->
        tmp?.description?.equalsIgnoreCase( payment.terminal )
      }
      tmpPayment.terminalId = terminalTmp?.id
      plans = PaymentController.findPlansByTerminal( terminalTmp?.id )
      plans?.each { Plan tmp ->
        plan.addItem( tmp?.description )
      }
      plan.selectedIndex = -1
  }

  private def planChanged = { ItemEvent ev ->
    if ( ev.stateChange == ItemEvent.SELECTED ) {
      Plan planTmp = plans.find { Plan tmp ->
        tmp?.description?.equalsIgnoreCase( ev.item as String )
      }
      tmpPayment.planId = planTmp?.id
    } else {
      tmpPayment.planId = null
    }
  }

  private def doSave = { ActionEvent ev ->
    JButton source = ev.source as JButton
    source.enabled = false
    Payment payment = DailyCloseController.updatePayment( tmpPayment )
    if ( payment?.id ) {
      sb.optionPane().showMessageDialog( null, 'Se ha aztualizado correctamente el Terminal y/o Plan del Pago', 'Ok', JOptionPane.INFORMATION_MESSAGE )
      dispose()
    } else {
      sb.optionPane().showMessageDialog( null, 'Se ha producido un error al actualizar el Terminal y/o Plan del Pago', 'Error', JOptionPane.ERROR_MESSAGE )
    }
    source.enabled = true
  }
}
