package mx.lux.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.lux.pos.model.CriterioDet
import mx.lux.pos.model.Incidencia
import mx.lux.pos.ui.controller.CustomerController
import mx.lux.pos.ui.controller.IOController
import mx.lux.pos.ui.model.PaymentType
import mx.lux.pos.ui.model.UpperCaseDocument
import mx.lux.pos.ui.resources.UI_Standards
import mx.lux.pos.ui.view.verifier.DateVerifier
import net.miginfocom.swing.MigLayout
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateUtils
import java.awt.BorderLayout

import javax.swing.*
import java.awt.event.ItemEvent
import java.util.List
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.text.DateFormat
import java.text.SimpleDateFormat

class CaptureIncidentsDialog extends JDialog implements FocusListener {

  private DateFormat df = new SimpleDateFormat( "dd/MM/yyyy" )
  private DateVerifier dv = DateVerifier.instance
  private def sb = new SwingBuilder()

  private JTextField txtIdCompany
  private JTextField txtEmployee
  private JLabel lblEmployee
  private JComboBox cbIdCriterion
  private JTextField txtValue
  private JTextArea txtObs

  private CriterioDet criterioDet
  private List<CriterioDet> lstCriterios = new ArrayList<>()

  public boolean button = false

    CaptureIncidentsDialog( ) {
    lstCriterios.addAll( IOController.instance.findListCriterios() )
    buildUI()
  }

  // UI Layout Definition
  void buildUI( ) {
    sb.dialog( this,
        title: "Capturar Incidencias",
        resizable: true,
        pack: true,
        modal: true,
        preferredSize: [ 420, 300 ],
        location: [ 200, 250 ],
    ) {
      panel() {
        borderLayout()
        panel( constraints: BorderLayout.CENTER, layout: new MigLayout( "wrap 3", "20[][fill][grow,fill]40", "20[]10[]" ) ) {
          label( text: "Id Empresa" )
          txtIdCompany = textField( text: '7', constraints: "span 2", editable: false )
          label( text: "Id Empleado" )
          txtEmployee = textField( preferredSize: [ 50,20 ] )
          txtEmployee.addFocusListener( this )
          lblEmployee = label( text: " " )
          label( text: "Id Criterio" )
          cbIdCriterion = comboBox( items: lstCriterios*.descripcion, constraints: "span 2", itemStateChanged: typeChanged )
          label( text: "Valor" )
          txtValue = textField( constraints: "span 2" )
          label(  )
          scrollPane( border: titledBorder( title: 'Observaciones' ), constraints: "span 2" ) {
            txtObs = textArea( document: new UpperCaseDocument(), preferredSize: [ 100,100 ] )
          }
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

  // UI Management
  protected void refreshUI( ) {

  }

  // Public Methods
  void activate( ) {
    refreshUI()
    setVisible( true )
  }

  void setDefaultDates( Date pDateStart, Date pDateEnd ) {

  }

  // UI Response
  protected void onButtonCancel( ) {
      println "onButtonCancel( )"
      dispose()
  }

  private def typeChanged = { ItemEvent ev ->
    criterioDet = lstCriterios.find { CriterioDet tmp ->
      tmp.descripcion.equalsIgnoreCase( ev.item as String )
    }
  }


  protected void onButtonOk( ) {
    if( criterioDet == null ){
      criterioDet = lstCriterios.find { CriterioDet tmp ->
        tmp.descripcion.equalsIgnoreCase( cbIdCriterion.selectedItem as String )
      }
    }
    Incidencia incidencia = new Incidencia()
    incidencia.setIdEmpresaCap( StringUtils.trimToEmpty(txtIdCompany.text) )
    incidencia.setNombreCap( StringUtils.trimToEmpty(lblEmployee.text) )
    incidencia.setIdEmpleadoCap( StringUtils.trimToEmpty(txtEmployee.text) )
    incidencia.setIdCriterio( StringUtils.trimToEmpty( criterioDet.idCriterio ) )
    incidencia.setIdGrupo( StringUtils.trimToEmpty( criterioDet.idGrupo.toString() ) )
    incidencia.setValor( StringUtils.trimToEmpty( txtValue.text ) )
    incidencia.setObservacion( StringUtils.trimToEmpty( txtObs.text ) )
    incidencia.setDescripcion( StringUtils.trimToEmpty( criterioDet.getDescripcion() ) )
    IOController.instance.saveIncidence( incidencia )
    println "onButtonOk( )"
    dispose()
  }

    @Override
    void focusGained(FocusEvent e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void focusLost(FocusEvent e) {
      String idEmpleado = StringUtils.trimToEmpty( txtEmployee.text )
      if( idEmpleado != '' ){
        String employeeName = ''
        employeeName = IOController.getInstance().getNameEmployee( idEmpleado )
        if(StringUtils.trimToEmpty( employeeName ) != '' ){
          lblEmployee.setText( employeeName )
        } else {
          sb.optionPane( message: "No se encontraron resultados para: ${idEmpleado}", optionType: JOptionPane.DEFAULT_OPTION )
                  .createDialog( new JTextField(), "B\u00fasqueda: ${idEmpleado}" )
                  .show()
        }
      } else {
          sb.optionPane( message: "Introdusca una b\u00fasqueda valida", optionType: JOptionPane.DEFAULT_OPTION )
                  .createDialog( new JTextField(), "B\u00fasqueda invalida" )
                  .show()
      }
    }
}
