package mx.lux.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.lux.pos.ui.model.UpperCaseDocument
import mx.lux.pos.ui.resources.UI_Standards
import mx.lux.pos.ui.view.verifier.DateVerifier
import net.miginfocom.swing.MigLayout
import org.apache.commons.lang3.time.DateUtils

import javax.swing.*
import java.awt.*
import java.text.DateFormat
import java.text.SimpleDateFormat

class SkuByBrandSelectionFilterDialog extends JDialog {

  private DateFormat df = new SimpleDateFormat( "dd/MM/yyyy" )
  private DateVerifier dv = DateVerifier.instance
  private def sb = new SwingBuilder()

  private JCheckBox cbResume
  private JTextField txtLine
  private JRadioButton cbArticulo
  private JRadioButton cbFactura
  private ButtonGroup reportTipo
  private ButtonGroup lentTipo
  private JRadioButton cbGogle
  private JRadioButton cbOftalmico
  private JRadioButton todo
  private Date selectedDateStart
  private Date selectedDateEnd
  private String selectedArticle
  private JRadioButton rbAll
  private JRadioButton rbFrame
  private JRadioButton rbAccessory
  private ButtonGroup group

  public boolean button = false

    SkuByBrandSelectionFilterDialog(  ) {
    buildUI()
  }

  // UI Layout Definition
  void buildUI( ) {
    sb.dialog( this,
        title: "Seleccionar fechas",
        resizable: true,
        pack: true,
        modal: true,
        preferredSize: [ 450, 250 ],
        location: [ 200, 250 ],
    ) {
      panel() {
        borderLayout()
        panel( constraints: BorderLayout.CENTER, layout: new MigLayout( "wrap 2", "20[][grow,fill]60", "20[]10[]" ) ) {
          label( text: "Seleccione la Marca que desea consultar", constraints: "span 2" )
          label( text: " ", constraints: "span 2" )
          label( text: "Marca:" )
          txtLine = textField( document: new UpperCaseDocument() )
          label( text: " " )
          panel( constraints: "span", layout: new MigLayout( "wrap 3", "30[][grow,fill]30", "" ) ) {
            group = buttonGroup()
            rbAll = radioButton( text: "Todos", buttonGroup: group, selected: true )
            rbFrame = radioButton( text: "Armazon", buttonGroup: group )
            rbAccessory = radioButton( text: "Accesorio", buttonGroup: group )
          }
        }

        panel( constraints: BorderLayout.PAGE_END ) {
          borderLayout()
          panel( constraints: BorderLayout.LINE_END ) {
            button( text: "Generar", preferredSize: UI_Standards.BUTTON_SIZE,
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
    /*if ( selectedDateStart == null || selectedDateEnd == null ) {
      selectedDateStart = DateUtils.truncate( new Date(), Calendar.DATE )
	  selectedDateEnd = DateUtils.truncate( new Date(), Calendar.DATE )
	}
    txtDateStart.setText( df.format( selectedDateStart ) )
	txtDateEnd.setText( df.format( selectedDateEnd ) )*/

  }

  // Public Methods
  void activate( ) {
    refreshUI()
    setVisible( true )
  }

  boolean getCbAccessory( ) {
    return rbAccessory.selected
  }

  String getselectedArticle( ) {
    return selectedArticle
  }

  boolean getCbFrame( ) {
    return rbFrame.selected
  }

  boolean getCbTodo( ) {
    return rbAll.selected
  }

  boolean getCbOftalmico( ) {
    return cbOftalmico.selected
  }

  void setDefaultDates( Date pDateStart, Date pDateEnd ) {
    selectedDateStart = DateUtils.truncate( pDateStart, Calendar.MONTH )
    selectedDateEnd = DateUtils.truncate( pDateEnd, Calendar.DATE )
  }

  // UI Response
  protected void onButtonCancel( ) {
    selectedDateStart = null
    selectedDateEnd = null
    selectedArticle = null
    button = false
    setVisible( false )
  }

  protected void onButtonOk( ) {
    selectedArticle = txtLine.getText().trim()
    button = true
    setVisible( false )
  }
}
