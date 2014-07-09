package mx.lux.pos.ui.view.dialog

import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import mx.lux.pos.model.IPromotionAvailable
import mx.lux.pos.ui.model.OrderItem
import mx.lux.pos.ui.resources.UI_Standards
import mx.lux.pos.ui.view.renderer.MoneyCellRenderer
import mx.lux.pos.ui.view.verifier.DateVerifier
import net.miginfocom.swing.MigLayout
import org.apache.commons.lang3.time.DateUtils

import javax.swing.*
import java.awt.*
import java.awt.event.MouseEvent
import java.util.List
import java.text.DateFormat
import java.text.SimpleDateFormat

class PromotionSelectionDialog extends JDialog {

  private DateFormat df = new SimpleDateFormat( "dd/MM/yyyy" )
  private DateVerifier dv = DateVerifier.instance
  private def sb = new SwingBuilder()
  private JLabel lblWarning

  private DefaultTableModel promotionModel
  private List<IPromotionAvailable> promotionList
  private IPromotionAvailable promotionSelected
  private List<IPromotionAvailable> promotionListSelected = new ArrayList<>()
  private List<IPromotionAvailable> promotionSelectedList

  public boolean button = false
  String title

    PromotionSelectionDialog( List<IPromotionAvailable> listPromo, Integer idArticulo ) {
    promotionListSelected.addAll( listPromo )
    title = "Promociones para articulo: ${idArticulo}"
    buildUI()
  }

  // UI Layout Definition
  void buildUI( ) {
    sb.dialog( this,
        title: title,
        resizable: true,
        pack: true,
        modal: true,
        preferredSize: [ 560, 300 ],
        location: [ 200, 250 ],
    ) {
      panel() {
        borderLayout()
        panel( constraints: BorderLayout.CENTER, layout: new MigLayout( "wrap", "[grow,fill]", "20[]10[]" ) ) {
            scrollPane( border: titledBorder( title: "Promociones" ),
                    //mouseClicked: { MouseEvent ev -> onMouseClickedAtPromotions( ev ) },
                    //mouseReleased: { MouseEvent ev -> onMouseClickedAtPromotions( ev ) }
            ) {
                table( selectionMode: ListSelectionModel.SINGLE_SELECTION,
                        mouseClicked: doShowItemClick,
                        //mouseClicked: { MouseEvent ev -> onMouseClickedAtPromotions( ev ) },
                        //mouseReleased: { MouseEvent ev -> onMouseClickedAtPromotions( ev ) }
                ) {
                    promotionModel = tableModel( list: promotionListSelected ) {
                        /*closureColumn( header: "", type: Boolean, maxWidth: 25,
                                read: { row -> row.applied },
                                write: { row, newValue ->
                                    onTogglePromotion( row, newValue )
                                }
                        )*/
                        propertyColumn( header: "Descripci\u00f3n", propertyName: "description", editable: false, minWidth: 100 )
                        //propertyColumn( header: "Art\u00edculo", propertyName: "partNbrList", maxWidth: 100, editable: false )
                        closureColumn( header: "Precio Base",
                                read: { IPromotionAvailable promotion -> promotion.baseAmount },
                                minWidth: 80,
                                cellRenderer: new MoneyCellRenderer()
                        )
                        closureColumn( header: "Descto",
                                read: { IPromotionAvailable promotion -> promotion.discountAmount },
                                minWidth: 80,
                                cellRenderer: new MoneyCellRenderer()
                        )
                        closureColumn( header: "Promoci\u00f3n",
                                read: { IPromotionAvailable promotion -> promotion.promotionAmount },
                                minWidth: 80,
                                cellRenderer: new MoneyCellRenderer()
                        )
                    } as DefaultTableModel
                }
            }
        }
        panel( constraints: BorderLayout.PAGE_END ) {
          borderLayout()
          panel( constraints: BorderLayout.LINE_END ) {
            lblWarning = label( "" )
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

    private def doShowItemClick = { MouseEvent ev ->
      if ( SwingUtilities.isLeftMouseButton( ev ) ) {
        if ( ev.clickCount == 2 ) {
          promotionSelected = ev.source.selectedElement
          onButtonOk()
        }
      }
    }
  // Public Methods

  IPromotionAvailable getPromotionSelected( ) {
    return promotionSelected
  }

  // UI Response
  protected void onButtonCancel( ) {
    dispose()
  }

  protected void onButtonOk( ) {
    if(promotionModel.rowModel.value != null){
      promotionSelected = ev.source.selectedElement
      dispose()
    } else {
      lblWarning.setText( "Seleccione una promocion" )
    }
  }
}
