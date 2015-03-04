package mx.lux.pos.ui.view.panel

import groovy.model.DefaultTableModel
import groovy.swing.SwingBuilder
import mx.lux.pos.model.IPromotionAvailable
import mx.lux.pos.model.IngresoPorDia
import mx.lux.pos.model.MontoGarantia
import mx.lux.pos.model.PromotionApplied
import mx.lux.pos.model.PromotionAvailable
import mx.lux.pos.model.PromotionCombo
import mx.lux.pos.model.PromotionDiscount
import mx.lux.pos.model.PromotionSingle
import mx.lux.pos.model.SalesWithNoInventory
import mx.lux.pos.service.business.Registry
import mx.lux.pos.ui.MainWindow
import mx.lux.pos.ui.resources.UI_Standards
import mx.lux.pos.ui.view.component.DiscountContextMenu
import mx.lux.pos.ui.view.driver.PromotionDriver
import mx.lux.pos.ui.view.renderer.MoneyCellRenderer
import net.miginfocom.swing.MigLayout
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateUtils

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Font
import java.text.DateFormat
import java.text.NumberFormat

import mx.lux.pos.ui.controller.*
import mx.lux.pos.ui.model.*
import mx.lux.pos.ui.view.dialog.*

import java.awt.event.*
import javax.swing.*
import org.slf4j.LoggerFactory
import org.slf4j.Logger

import java.text.ParseException
import java.text.SimpleDateFormat

class OrderPanel extends JPanel implements IPromotionDrivenPanel, FocusListener {

  static final String MSG_INPUT_QUOTE_ID = 'Indique el número de cotización'
  static final String TXT_QUOTE_TITLE = 'Seleccionar cotización'
  private static final String TXT_BTN_CLOSE = 'Vendedor'
  private static final String TXT_BTN_QUOTE = 'Cotizar'
  private static final String TXT_BTN_ANNULE = 'Anular Venta'
  //private static final String TXT_BTN_PRINT = 'Imprimir'
  private static final String TXT_BTN_PAY = 'Pagar'
  private static final String TXT_NO_ORDER_PRESENT = 'Se debe agregar al menos un artículo.'
  private static final String TXT_PAYMENTS_PRESENT = 'Elimine los pagosregistrados y reintente.'
  private static final String MSJ_VENTA_NEGATIVA = 'No se pueden agregar art\u00edculos sin existencia.'
  private static final String MSJ_FECHA_INCORRECTA = 'Verifique la fecha de la computadora.'
  private static final String MSJ_ARTICULO_PROMOCIONAL_PARAM_NO = 'Esta promocion incluye un articulo de regalo ¿Desea agregarlo?'
  private static final String TXT_VENTA_NEGATIVA_TITULO = 'Error al agregar artículo'
  private static final String TXT_FECHA_INCORRECTA_TITULO = 'Error al crear orden'
  private static final String TXT_ARTICULO_PROMOCIONAL_TITULO = 'Articulo Promocional'
  static final String MSG_NO_STOCK = 'Articulo sin existencia ¿Desea continuar?'
  static final String TXT_NO_STOCK = 'Articulo sin existencia'
  private static final String MSJ_QUITAR_PAGOS = 'Elimine los pagos antes de cerrar la sesion.'
  private static final String TXT_QUITAR_PAGOS = 'Error al cerrar sesion.'
  private static final String MSJ_CAMBIAR_VENDEDOR = 'Esta seguro que desea salir de esta sesion.'
  private static final String TXT_CAMBIAR_VENDEDOR = 'Cerrar Sesion'
  private static final String TAG_GENERICO_SEGUROS = 'J'
  private static final String TAG_GENERICO_ARMAZON = 'A'

  private static final String TAG_TC = 'TC'
  private static final String TAG_TD = 'TD'

  private Logger logger = LoggerFactory.getLogger(this.getClass())
  private SwingBuilder sb
  private Order order
  private Customer customer
  private JComboBox operationType
  private JButton customerName
  private JButton closeButton
  private JButton quoteButton
  private JButton annuleButton
  private JButton printButton
  private JTextArea comments
  private JTextField itemSearch
  private List<IPromotionAvailable> promotionList
  private List<IPromotionAvailable> promotionListSelected
  private List<IPromotionAvailable> promotionSelectedList
  private List<String> lstPromotioSelected
  private List<Warranty> lstWarranty = new ArrayList<>()
  private DefaultTableModel itemsModel
  private DefaultTableModel paymentsModel
  private DefaultTableModel promotionModel
  private JLabel folio
  private JLabel bill
  private JLabel date
  private JLabel total
  private JLabel paid
  private JLabel due
  private JLabel change
  private BigDecimal cambio = BigDecimal.ZERO
  private DiscountContextMenu discountMenu
  private String autorizacion
  private OperationType currentOperationType
  private Boolean canceledWarranty
  private MainWindow mainWindow

  private String MSJ_ERROR_WARRANTY = ""
  private String TXT_ERROR_WARRANTY = ""

  DateFormat df = new SimpleDateFormat( "dd/MM/yyyy" )

  OrderPanel( MainWindow mainWindow ) {
    sb = new SwingBuilder()
    order = new Order()
    this.mainWindow = mainWindow
    customer = CustomerController.findDefaultCustomer()
    promotionList = new ArrayList<PromotionAvailable>()
    promotionListSelected = new ArrayList<>()
    promotionSelectedList = new ArrayList<PromotionAvailable>()
    promotionSelectedList = new ArrayList<PromotionAvailable>()
    lstPromotioSelected = new ArrayList<String>()
    this.promotionDriver.init( this )
    buildUI()
    doBindings()
    itemsModel.addTableModelListener( this.promotionDriver )
  }

  private PromotionDriver getPromotionDriver( ) {
    return PromotionDriver.instance
  }

  private void buildUI( ) {
    sb.panel( this, layout: new MigLayout( 'insets 5,fill,wrap', '[fill]', '[fill]' ) ) {
      panel( layout: new MigLayout( 'insets 0,fill', '[fill,260][fill,180][fill,300!]', '[fill]' ) ) {
        panel( border: loweredEtchedBorder(), layout: new MigLayout( 'wrap 2', '[][fill,220!]', '[top]' ) ) {
          label( 'Cliente' )
          customerName = button( enabled: false, actionPerformed: doCustomerSearch )

          label( 'Tipo' )
          operationType = comboBox( items: OperationType.values(), itemStateChanged: operationTypeChanged )
        }

        panel( border: loweredEtchedBorder(), layout: new MigLayout( 'wrap 2', '[][grow,right]', '[top]' ) ) {
            def displayFont = new Font( '', Font.BOLD, 19 )
            label( )
            date = label( font: displayFont )
            label( 'Folio' )
          folio = label()
          label( 'Factura' )
          bill = label()
          //label( 'Fecha' )
        }

        panel( border: loweredEtchedBorder(), layout: new MigLayout( 'wrap 2', '[][grow,right]', '[top]' ) ) {
          def displayFont = new Font( '', Font.BOLD, 22 )
          label( 'Venta' )
          total = label( font: displayFont )
          label( 'Pagado' )
          paid = label( font: displayFont )
          label( 'Saldo' )
          due = label( font: displayFont )
        }
      }

      itemSearch = textField( font: new Font( '', Font.BOLD, 16 ), document: new UpperCaseDocument(), actionPerformed: {doItemSearch()} )
      itemSearch.addFocusListener( this )

      scrollPane( border: titledBorder( title: 'Art\u00edculos' ) ) {
        table( selectionMode: ListSelectionModel.SINGLE_SELECTION, mouseClicked: doShowItemClick ) {
          itemsModel = tableModel( list: order.items ) {
            closureColumn(
                header: 'Art\u00edculo',
                read: {OrderItem tmp -> "${tmp?.item?.name} ${tmp?.item?.color ?: ''}"},
                minWidth: 80,
                maxWidth: 100
            )
            closureColumn(
                header: 'Descripci\u00f3n',
                read: {OrderItem tmp -> lstPromotionsAvalibles(tmp).size() <= 0 ? tmp?.description : "**"+tmp?.description}
            )
            closureColumn(
                header: 'Cantidad',
                read: {OrderItem tmp -> tmp?.quantity},
                minWidth: 70,
                maxWidth: 70
            )
            closureColumn(
                header: 'Precio',
                read: {OrderItem tmp -> tmp?.item?.price},
                minWidth: 80,
                maxWidth: 100,
                cellRenderer: new MoneyCellRenderer()
            )
            closureColumn(
                header: 'Total',
                read: {OrderItem tmp -> tmp?.item?.price * tmp?.quantity},
                minWidth: 80,
                maxWidth: 100,
                cellRenderer: new MoneyCellRenderer()
            )
          } as DefaultTableModel
        }
      }

      panel( layout: new MigLayout( 'insets 0,fill', '[fill][fill,240!]', '[fill]' ) ) {
        scrollPane( border: titledBorder( title: "Promociones" ),
            mouseClicked: { MouseEvent ev -> onMouseClickedAtPromotions( ev ) },
            mouseReleased: { MouseEvent ev -> onMouseClickedAtPromotions( ev ) }
        ) {
          table( selectionMode: ListSelectionModel.SINGLE_SELECTION,
              mouseClicked: { MouseEvent ev -> onMouseClickedAtPromotions( ev ) },
              mouseReleased: { MouseEvent ev -> onMouseClickedAtPromotions( ev ) }
          ) {
            promotionModel = tableModel( list: promotionListSelected ) {
              closureColumn( header: "", type: Boolean, maxWidth: 25,
                  read: { row -> row.applied },
                  write: { row, newValue ->
                    onTogglePromotion( row, newValue )
                  }
              )
              propertyColumn( header: "Descripci\u00f3n", propertyName: "description", editable: false )
              propertyColumn( header: "Art\u00edculo", propertyName: "partNbrList", maxWidth: 100, editable: false )
              closureColumn( header: "Precio Base",
                  read: { IPromotionAvailable promotion -> promotion.baseAmount },
                  maxWidth: 80,
                  cellRenderer: new MoneyCellRenderer()
              )
              closureColumn( header: "Descto",
                  read: { IPromotionAvailable promotion -> promotion.discountAmount },
                  maxWidth: 80,
                  cellRenderer: new MoneyCellRenderer()
              )
              closureColumn( header: "Promoci\u00f3n",
                  read: { IPromotionAvailable promotion -> promotion.promotionAmount },
                  maxWidth: 80,
                  cellRenderer: new MoneyCellRenderer()
              )
            } as DefaultTableModel
          }
        }

        scrollPane( border: titledBorder( title: 'Pagos' ), mouseClicked: doNewPaymentClick ) {
          table( selectionMode: ListSelectionModel.SINGLE_SELECTION, mouseClicked: doShowPaymentClick ) {
            paymentsModel = tableModel( list: order.payments ) {
              closureColumn( header: 'Descripci\u00f3n', read: {Payment tmp -> tmp?.description} )
              closureColumn( header: 'Monto', read: {Payment tmp -> tmp?.amount}, maxWidth: 100, cellRenderer: new MoneyCellRenderer() )
            } as DefaultTableModel
          }
        }
      }

      scrollPane( border: titledBorder( title: 'Observaciones' ) ) {
        comments = textArea( document: new UpperCaseDocument(), lineWrap: true )
      }

      // panel( layout: new MigLayout( 'insets 0,fill', '[fill,125!][fill,grow][fill,125!]', '[fill,40!]' ) ) {
      panel( minimumSize: [ 750, 45 ], border: BorderFactory.createEmptyBorder( 0, 0, 0, 0 ) ) {
        borderLayout()
        panel( constraints: BorderLayout.LINE_START, border: BorderFactory.createEmptyBorder( 0, 0, 0, 0 ) ) {
          closeButton = button( TXT_BTN_CLOSE,
              preferredSize: UI_Standards.BIG_BUTTON_SIZE,
              actionPerformed: doClose
          )
        }
        change = label( foreground: UI_Standards.WARNING_FOREGROUND, constraints: BorderLayout.CENTER )
        panel( constraints: BorderLayout.LINE_END, border: BorderFactory.createEmptyBorder( 0, 0, 0, 0 ) ) {
          annuleButton = button( TXT_BTN_ANNULE,
              preferredSize: UI_Standards.BIG_BUTTON_SIZE,
              actionPerformed: { fireRequestAnnular() },
              constraints: 'hidemode 3',
              visible: false
          )
          quoteButton = button( TXT_BTN_QUOTE,
              preferredSize: UI_Standards.BIG_BUTTON_SIZE,
              actionPerformed: { fireRequestQuote() }
          )
          printButton = button( TXT_BTN_PAY,
              preferredSize: UI_Standards.BIG_BUTTON_SIZE,
              actionPerformed: doPrint
          )
        }
      }
    }
  }

  private void doBindings( ) {
    sb.build {
      bean( customerName, text: bind {customer?.fullName} )
      bean( folio, text: bind {order.id} )
      bean( bill, text: bind {order.bill} )
      bean( date, text: bind( source: order, sourceProperty: 'date', converter: dateConverter ), alignmentX: CENTER_ALIGNMENT )
      bean( total, text: bind( source: order, sourceProperty: 'total', converter: currencyConverter ) )
      bean( paid, text: bind( source: order, sourceProperty: 'paid', converter: currencyConverter ) )
      bean( due, text: bind( source: order, sourceProperty: 'dueString' ) )
      bean( itemsModel.rowsModel, value: bind( source: order, sourceProperty: 'items', mutual: true ) )
      bean( paymentsModel.rowsModel, value: bind( source: order, sourceProperty: 'payments', mutual: true ) )
      bean( comments, text: bind( source: order, sourceProperty: 'comments', mutual: true ) )
      bean( order, customer: bind {customer} )
    }
    itemsModel.fireTableDataChanged()
    paymentsModel.fireTableDataChanged()
    change.text = OrderController.requestEmployee( order?.id )
    currentOperationType = (OperationType) operationType.getSelectedItem()
  }

  private void updateOrder( String pOrderId ) {
    Order tmp = OrderController.getOrder( pOrderId )
    if ( tmp?.id ) {
      tmp.country = order?.country
      order = tmp
      doBindings()
    }
  }

  private def dateConverter = { Date val ->
    val?.format( 'dd-MM-yyyy' )
  }

  private def currencyConverter = {
    NumberFormat.getCurrencyInstance( Locale.US ).format( it ?: 0 )
  }

  private def doCustomerSearch = { ActionEvent ev ->
    JButton source = ev.source as JButton
    source.enabled = false
    CustomerSearchDialog dialog = new CustomerSearchDialog( ev.source as Component, order )
    dialog.show()
    if ( !dialog.canceled ) {
      customer = dialog.customer
    }
    doBindings()
    source.enabled = true
  }

  private def operationTypeChanged = { ItemEvent ev ->
    if ( ev.stateChange == ItemEvent.SELECTED ) {
      switch ( ev.item ) {
        case OperationType.DEFAULT:
          customer = CustomerController.findDefaultCustomer()
          customerName.enabled = false
          break
        case OperationType.WALKIN:
          customer = new Customer( type: CustomerType.DOMESTIC )
          ForeignCustomerDialog dialog = new ForeignCustomerDialog( ev.source as Component, customer, false )
          dialog.show()
          if ( !dialog.canceled ) {
            customer = dialog.customer
          }
          break
        case OperationType.DOMESTIC:
          customer = new Customer( type: CustomerType.DOMESTIC )
          CustomerSearchDialog dialog = new CustomerSearchDialog( ev.source as Component, order )
          dialog.show()
          if ( !dialog.canceled ) {
            customer = dialog.customer
          }
          break
        case OperationType.FOREIGN:
          customer = new Customer( type: CustomerType.FOREIGN )
          ForeignCustomerDialog dialog = new ForeignCustomerDialog( ev.source as Component, customer, false )
          dialog.show()
          if ( !dialog.canceled ) {
            customer = dialog.customer
          }
          break
        case OperationType.QUOTE:
          String orderNbr = OrderController.requestOrderFromQuote( this )
          sb.doLater {
            if (StringUtils.trimToNull(orderNbr) != null) {
              Customer tmp = OrderController.getCustomerFromOrder( orderNbr )
              if (tmp != null) {
                customer = tmp
              }
              updateOrder( orderNbr )
            } else {
              operationType.setSelectedItem( currentOperationType )
            }
          }
          break
        case OperationType.AGREEMENT:
          operationType.setSelectedItem( OperationType.DEFAULT )
          break
      }
      this.setCustomerInOrder()
      doBindings()
    } else {
      customerName.enabled = true
    }
  }

  private def doItemSearch( ) {
    String input = itemSearch.text
    Boolean newOrder = (StringUtils.trimToNull(StringUtils.trimToEmpty(order?.id)) == null)
    if ( StringUtils.isNotBlank( input ) ) {
      sb.doOutside {
        List<Item> results = ItemController.findItemsByQuery( input )
        if ( ( results.size() == 0 ) && ( input.length() > 6 ) ) {
          input = input.substring( 0, 6 )
          results = ItemController.findItemsByQuery( input )
        }
        if ( results?.any() ) {
          if ( results.size() == 1 ) {
              if( results?.first().stock <= 0 && ItemController.isInventoried(results?.first().id) ){
                Integer question =JOptionPane.showConfirmDialog( new JDialog(), MSG_NO_STOCK, TXT_NO_STOCK,
                        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE )
                if( question == 0){
                  Item item = results.first()
                  validarVentaNegativa( item )
                }
              } else {
                Item item = results.first()
                validarVentaNegativa( item )
              }
          } else {
            SuggestedItemsDialog dialog = new SuggestedItemsDialog( itemSearch, input, results )
            dialog.show()
            Item item = dialog.item
            if ( item?.id ) {
              if( item?.stock <= 0 && ItemController.isInventoried(results?.first().id) ){
                Integer question =JOptionPane.showConfirmDialog( new JDialog(), MSG_NO_STOCK, TXT_NO_STOCK,
                        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE )
                if( question == 0){
                  validarVentaNegativa( item )
                }
              } else {
                validarVentaNegativa( item )
              }
            }
          }
          updateOrder( order?.id )
          if ( !order.customer.equals(customer) ) {
            order.customer = customer
          }
        } else {
          optionPane( message: "No se encontraron resultados para: ${input}", optionType: JOptionPane.DEFAULT_OPTION )
              .createDialog( new JTextField(), "B\u00fasqueda: ${input}" )
              .show()
        }
        if (newOrder && (StringUtils.trimToNull(order?.id) != null) && (StringUtils.trimToNull(customer?.id) != null)) {
          this.setCustomerInOrder()
        }
      }
      sb.doLater {
        itemSearch.text = null
      }
    } else {
      sb.optionPane( message: 'Es necesario ingresar una b\u00fasqeda v\u00e1lida', optionType: JOptionPane.DEFAULT_OPTION )
          .createDialog( new JTextField(), "B\u00fasqueda inv\u00e1lida" )
          .show()
    }
  }

  private def doShowItemClick = { MouseEvent ev ->
    if( !annuleButton.visible ){
      if ( SwingUtilities.isLeftMouseButton( ev ) ) {
            if ( ev.clickCount == 2 ) {
                new ItemDialog( ev.component, order, ev.source.selectedElement ).show()
                if( this.promotionSelectedList.size() > 0 ){
                    List<IPromotionAvailable> lstPromo = new ArrayList<IPromotionAvailable>()
                    lstPromo.addAll( promotionSelectedList )
                    for(IPromotionAvailable promotion : lstPromo){
                        onTogglePromotion( promotion, false )
                    }
                }
                updateOrder( order?.id )
            }
      } else if ( SwingUtilities.isRightMouseButton( ev ) && ev.source.selectedElement != null ) {
            OrderItem orderItem = ev.source.selectedElement
            List<IPromotionAvailable> lstPromosArt = lstPromotionsAvalibles( orderItem )
            Collections.sort(lstPromosArt, new Comparator<IPromotionAvailable>() {
                @Override
                int compare(IPromotionAvailable o1, IPromotionAvailable o2) {
                    return o1.promotion.idPromotion.compareTo(o2.promotion.idPromotion)
                }
            })
            PromotionSelectionDialog promotionSelectionDialog = new PromotionSelectionDialog( lstPromosArt, orderItem.item.id )
            promotionSelectionDialog.show()
            if( promotionSelectionDialog.promotionSelected != null ){
                promotionListSelected.add( promotionSelectionDialog.promotionSelected )
                onTogglePromotion( promotionSelectionDialog.promotionSelected, true )
                doBindings()
            }
      }
    }
  }

  private def doNewPaymentClick = { MouseEvent ev ->
    if ( SwingUtilities.isLeftMouseButton( ev ) && order.payments.size() > 0 ) {
      if ( ev.clickCount == 1 ) {
        if ( order.due ) {
            /*new PaymentDialog( ev.component, order, null, order.due ).show()
            updateOrder( order?.id )*/
          payDue( false )
        } else {
          sb.optionPane(
              message: 'No hay saldo para aplicar pago',
              messageType: JOptionPane.ERROR_MESSAGE
          ).createDialog( this, 'Pago sin saldo' )
              .show()
        }
      }
    }
  }

  private def doShowPaymentClick = { MouseEvent ev ->
    if ( SwingUtilities.isLeftMouseButton( ev ) ) {
      if ( ev.clickCount == 2 ) {
        Payment pay = ev.source.selectedElement
        if(Registry.activeTpv && (pay.paymentTypeId.startsWith("TV") || pay.paymentTypeId.startsWith("DV") ||
                pay.paymentTypeId.startsWith("UV") || pay.paymentTypeId.startsWith("AV")) ){

        } else {
          new PaymentDialog( ev.component, order, ev.source.selectedElement, BigDecimal.ZERO ).show()
          updateOrder( order?.id )
          if( order?.payments.size() <= 0 ){
            printButton.enabled = true
          }
        }
      }
    }
  }

  private void reviewForTransfers( String newOrderId ) {
    if ( CancellationController.orderHasTransfers( newOrderId ) ) {
      List<Order> lstOrders = CancellationController.findOrderToResetValues( newOrderId )
      for(Order order : lstOrders){
        CancellationController.resetValuesofCancellation( order.id )
      }
      List<String> sources = CancellationController.findSourceOrdersWithCredit( newOrderId )
      if ( sources?.any() ) {
        new RefundDialog( this, sources.first(), true ).show()
      } else {
        CancellationController.printCancellationsFromOrder( newOrderId )
      }
    }
  }


  private void validarVentaNegativa( Item item ) {
    if( OrderController.validDate() ){
        if( OrderController.validateCloseDate() ){
            if ( item.stock > 0 ) {
                order = OrderController.addItemToOrder( order.id, item )
                if (customer != null) {
                    order.customer = customer
                }
            } else {
                SalesWithNoInventory onSalesWithNoInventory = OrderController.requestConfigSalesWithNoInventory()
                if ( SalesWithNoInventory.ALLOWED.equals( onSalesWithNoInventory ) ) {
                    order = OrderController.addItemToOrder( order.id, item )
                } else if ( SalesWithNoInventory.REQUIRE_AUTHORIZATION.equals( onSalesWithNoInventory ) ) {
                    boolean authorized
                    if ( AccessController.authorizerInSession ) {
                        authorized = true
                    } else {
                        AuthorizationDialog authDialog = new AuthorizationDialog( this, "Operaci\u00f3n requiere autorizaci\u00f3n", false )
                        authDialog.show()
                        authorized = authDialog.authorized
                    }
                    if ( authorized ) {
                        order = OrderController.addItemToOrder( order.id, item )
                    }
                } else {
                    sb.optionPane( message: MSJ_VENTA_NEGATIVA, messageType: JOptionPane.ERROR_MESSAGE, )
                            .createDialog( this, TXT_VENTA_NEGATIVA_TITULO )
                            .show()
                }
            }
        } else {
            sb.optionPane( message: MSJ_FECHA_INCORRECTA, messageType: JOptionPane.ERROR_MESSAGE, )
                    .createDialog( this, TXT_FECHA_INCORRECTA_TITULO )
                    .show()
        }
    } else {
        sb.optionPane( message: MSJ_FECHA_INCORRECTA, messageType: JOptionPane.ERROR_MESSAGE, )
                .createDialog( this, TXT_FECHA_INCORRECTA_TITULO )
                .show()
    }
  }

  private def doClose = {
    sb.doLater {
        doBindings()
        if( order.payments.size() == 0 ){
            Integer question =JOptionPane.showConfirmDialog( new JDialog(), MSJ_CAMBIAR_VENDEDOR, TXT_CAMBIAR_VENDEDOR,
                    JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE )
            if( question == 0){
                MainWindow.instance.requestLogout()
            }
        } else {
            sb.optionPane( message: MSJ_QUITAR_PAGOS, messageType: JOptionPane.INFORMATION_MESSAGE, )
                    .createDialog( this, TXT_QUITAR_PAGOS )
                    .show()
        }
    }
  }

  private def doPrint = { ActionEvent ev ->
    JButton source = ev.source as JButton
    source.enabled = false
    if( OrderController.validDate() ){
      if( validWarranty( ) ){
          if ( isValidOrder() ) {
            if( operationType.selectedItem.toString().trim().equalsIgnoreCase(OperationType.WALKIN.value) ||
                    operationType.selectedItem.toString().trim().equalsIgnoreCase(OperationType.DOMESTIC.value) ){
                String estadoCliente = CustomerController.stateCustomer( order )
                order.country = "MEXICO,${estadoCliente }"
                //saveOrder()
                payDue( true )
            } else if( operationType.selectedItem.toString().trim().equalsIgnoreCase(OperationType.FOREIGN.value) ){
                String paisCliente = CustomerController.countryCustomer( order )
                if( paisCliente.length() > 0 ){
                    order.country = paisCliente
                    //saveOrder()
                    payDue( true )
                } else {
                    CountryCustomerDialog dialog = new CountryCustomerDialog( MainWindow.instance )
                    dialog.show()
                    if( dialog.button == true ){
                        order.country = dialog.pais
                        //saveOrder()
                        payDue( true )
                    } else {
                      printButton.enabled = true
                      quoteButton.enabled = true
                      closeButton.enabled = true
                      operationType.enabled = true
                      mainWindow.ordersMenu.enabled = true
                      mainWindow.inventoryMenu.enabled = true
                      mainWindow.sendReceivedInventoryMenu.enabled = true
                      mainWindow.reportsMenu.enabled = true
                      mainWindow.toolsMenu.enabled = true
                    }
                }
            } else if( operationType.selectedItem.toString().trim().equalsIgnoreCase(OperationType.DEFAULT.value) ){
                CountryCustomerDialog dialog = new CountryCustomerDialog( MainWindow.instance )
                dialog.show()
                if( dialog.button == true ){
                    order.country = dialog.pais
                    //saveOrder()
                    payDue( true )
                } else {
                    printButton.enabled = true
                    quoteButton.enabled = true
                    closeButton.enabled = true
                    operationType.enabled = true
                    mainWindow.ordersMenu.enabled = true
                    mainWindow.inventoryMenu.enabled = true
                    mainWindow.sendReceivedInventoryMenu.enabled = true
                    mainWindow.reportsMenu.enabled = true
                    mainWindow.toolsMenu.enabled = true
                }
            }
        } else {
          printButton.enabled = true
          quoteButton.enabled = true
          closeButton.enabled = true
          operationType.enabled = true
          mainWindow.ordersMenu.enabled = true
          mainWindow.inventoryMenu.enabled = true
          mainWindow.sendReceivedInventoryMenu.enabled = true
          mainWindow.reportsMenu.enabled = true
          mainWindow.toolsMenu.enabled = true
        }
      } else {
        lstWarranty.clear()
        if( !canceledWarranty ){
          TXT_ERROR_WARRANTY = "No se puede registrar la venta"
          if( MSJ_ERROR_WARRANTY.length() <= 0 ){
            MSJ_ERROR_WARRANTY = "Error al asignar las garantias, Verifiquelas e intente nuevamente."
          }
          sb.optionPane(
             message: MSJ_ERROR_WARRANTY,
             messageType: JOptionPane.ERROR_MESSAGE
          ).createDialog( this, TXT_ERROR_WARRANTY )
            .show()
        }
        printButton.enabled = true
        quoteButton.enabled = true
        closeButton.enabled = true
        operationType.enabled = true
        mainWindow.ordersMenu.enabled = true
        mainWindow.inventoryMenu.enabled = true
        mainWindow.sendReceivedInventoryMenu.enabled = true
        mainWindow.reportsMenu.enabled = true
        mainWindow.toolsMenu.enabled = true
      }
    } else {
        sb.optionPane( message: MSJ_FECHA_INCORRECTA, messageType: JOptionPane.ERROR_MESSAGE, )
                .createDialog( this, TXT_FECHA_INCORRECTA_TITULO )
                .show()
      printButton.enabled = true
      quoteButton.enabled = true
      closeButton.enabled = true
      operationType.enabled = true
      mainWindow.ordersMenu.enabled = true
      mainWindow.inventoryMenu.enabled = true
      mainWindow.sendReceivedInventoryMenu.enabled = true
      mainWindow.reportsMenu.enabled = true
      mainWindow.toolsMenu.enabled = true
    }
    //source.enabled = true
  }

    private void saveOrder (){
      //if( validWarranty( ) ){
        Order newOrder = OrderController.placeOrder( order )
        //CustomerController.saveOrderCountries( order.country )
        this.promotionDriver.requestPromotionSave()
        if ( StringUtils.isNotBlank( newOrder?.id ) ) {
            OrderController.printOrder( newOrder.id )
            for(Warranty warranty : lstWarranty){
              ItemController.printWarranty( warranty.amount, warranty.idItem)
            }
            lstWarranty.clear()
            OrderController.printVoucherTpv( newOrder.id, false )
            reviewForTransfers( newOrder.id )
            this.reset( )
        } else {
            sb.optionPane(
                    message: 'Ocurrio un error al registrar la venta, intentar nuevamente',
                    messageType: JOptionPane.ERROR_MESSAGE
            ).createDialog( this, 'No se puede registrar la venta' )
                    .show()
        }
      /*} else {
        TXT_ERROR_WARRANTY = "No se puede registrar la venta"
        if( MSJ_ERROR_WARRANTY.length() <= 0 ){
          MSJ_ERROR_WARRANTY = "Error al asignar las garantias, Verifiquelas e intente nuevamente."
        }
        sb.optionPane(
                  message: MSJ_ERROR_WARRANTY,
                  messageType: JOptionPane.ERROR_MESSAGE
        ).createDialog( this, TXT_ERROR_WARRANTY )
                .show()
      }*/
    }

  private boolean isValidOrder( ) {
    if ( itemsModel.size() == 0 ) {
      sb.optionPane(
          message: 'Se debe agregar al menos un art\u00edculo a la venta',
          messageType: JOptionPane.ERROR_MESSAGE
      ).createDialog( this, 'No se puede registrar la venta' )
          .show()
      return false
    }
    /*if ( order.due > 0 ) {
      sb.optionPane(
          message: 'Se debe cubrir el total del saldo.',
          messageType: JOptionPane.ERROR_MESSAGE
      ).createDialog( this, 'No se puede registrar la venta' )
          .show()
      return false
    }*/
    if ( order.due < 0 ) {
      sb.optionPane(
          message: 'Los pagos no deben ser mayores al total de la venta.',
          messageType: JOptionPane.ERROR_MESSAGE
      ).createDialog( this, 'No se puede registrar la venta' )
          .show()
      return false
    }
    if ( order.items.size() > 1 && promotionSelectedList.size() > 0 ) {
      for(def promotionSelectedList : this.promotionSelectedList){
          Integer idPromo = 0
          //Integer artSeleccionables = 0
          Integer idPromoList = 0
          List<Integer> lstArticulos = new ArrayList<>()
          String descPromo = ""
          if( promotionSelectedList.promotion instanceof PromotionSingle ){
              idPromo = promotionSelectedList.promotion.entity.idPromocion
              descPromo = promotionSelectedList.promotion.entity.descripcion
          } else if( promotionSelectedList.promotion instanceof PromotionCombo ){
              idPromo = promotionSelectedList.promotion.base.entity.idPromocion
              descPromo = promotionSelectedList.promotion.base.entity.descripcion
          }
          List<PromotionAvailable> lstPromotionDiscount = new ArrayList<>()
          for(IPromotionAvailable promo : promotionList){
            if(!(promo instanceof PromotionDiscount)){
              lstPromotionDiscount.add( promo )
            }
          }

          for(PromotionAvailable promotion : lstPromotionDiscount){
              if( promotion.promotion instanceof PromotionSingle ){
                  idPromoList = promotion.promotion.entity.idPromocion
              } else if( promotion.promotion instanceof PromotionCombo ){
                  idPromoList = promotion.promotion.base.entity.idPromocion
              }
            if( idPromo == idPromoList ){
              for(PromotionApplied promoApp : promotion.appliesToList){
                lstArticulos.add( promoApp.orderDetail.sku )
              }
              //artSeleccionables = artSeleccionables+1
            }
          }
          if( lstArticulos.size() > 1 ){
              if( OrderController.validPromoMenorPrecio( idPromo ) ){
                Item it = new Item()
                Integer idArt = promotionSelectedList.appliesToList.get(0).orderDetail.sku
                if( ItemController.areArticlesSamePrice( lstArticulos ) ){
                  it.id = idArt
                } else {
                  it = ItemController.findArticleMinPrice( lstArticulos )
                }
                  if( it != null && it.id > -1 && idArt != it.id ){
                      sb.optionPane(
                              message: "El articulo "+/"/+idArt+/"/+" no es el de menor precio.",
                              messageType: JOptionPane.ERROR_MESSAGE
                      ).createDialog( this, 'No se puede registrar la venta' )
                              .show()
                      return false
                      break
                  }
              }
          }
      }
    }

    if( order.items.size() > 0 && promotionSelectedList.size() > 0 ){
      Boolean validCombo = false
      for(def promotionSelectedList : this.promotionSelectedList){
        if( promotionSelectedList instanceof PromotionAvailable && promotionSelectedList.promotion instanceof  PromotionCombo ){
          if( order.items.size() <= 1 ){
            sb.optionPane(
               message: "La promocion aplica en la compra de mas de un articulo.",
               messageType: JOptionPane.ERROR_MESSAGE
            ).createDialog( this, 'No se puede registrar la venta' )
               .show()
            return false
          } else {
            String generico = promotionSelectedList.promotion.base.entity.idGenerico.contains("*") ? promotionSelectedList.promotion.base.entity.idGenericoC : promotionSelectedList.promotion.base.entity.idGenerico
            String genericoC = promotionSelectedList.promotion.base.entity.idGenericoC.contains("*") ? promotionSelectedList.promotion.base.entity.idGenerico : promotionSelectedList.promotion.base.entity.idGenericoC
            if( StringUtils.trimToEmpty(generico).equalsIgnoreCase(StringUtils.trimToEmpty(genericoC)) ){
              Integer count = 0
              for(OrderItem orderItem : order.items){
                if( StringUtils.trimToEmpty(orderItem.item.type).
                        equalsIgnoreCase(StringUtils.trimToEmpty(generico)) ){
                  count = count+1
                }
              }
              if( count <= 1 ){
                sb.optionPane(
                   message: "La promocion aplica en la compra de mas de un articulo.",
                   messageType: JOptionPane.ERROR_MESSAGE
                ).createDialog( this, 'No se puede registrar la venta' )
                  .show()
                return false
              }
            } else {
              Boolean genItem1 = false
              Boolean genItem2 = false
              for(OrderItem orderItem : order.items){
                if( StringUtils.trimToEmpty(orderItem.item.type).
                            equalsIgnoreCase(StringUtils.trimToEmpty(generico)) ){
                  genItem1 = true
                } else if( StringUtils.trimToEmpty(orderItem.item.type).
                        equalsIgnoreCase(StringUtils.trimToEmpty(genericoC)) ){
                    genItem2 = true
                }
              }
              if( !genItem1 || !genItem2 ){
                sb.optionPane(
                   message: "La promocion aplica en la compra de mas de un articulo.",
                   messageType: JOptionPane.ERROR_MESSAGE
                ).createDialog( this, 'No se puede registrar la venta' )
                  .show()
                return false
              }
            }
          }
        }
      }
    }
    return true
  }

  protected void onMouseClickedAtPromotions( MouseEvent pEvent ) {
    // rld Strange no mouse event triggers popup menu
    // change isPopupTrigger to MouseClicked and button = 3
    // if ( pEvent.isPopupTrigger() ) {
    if ( SwingUtilities.isRightMouseButton( pEvent ) && ( pEvent.getID() == MouseEvent.MOUSE_CLICKED ) &&
            !annuleButton.visible ) {
      if ( discountMenu == null ) {
        discountMenu = new DiscountContextMenu( this.promotionDriver )
      }
      discountMenu.activate( pEvent )
    }
  }

  protected void onTogglePromotion( IPromotionAvailable pPromotion, Boolean pNewValue ) {
    if ( pNewValue ) {
      this.promotionDriver.requestApplyPromotion( pPromotion )
      this.promotionSelectedList.add( pPromotion )
    } else {
      this.promotionSelectedList.remove( pPromotion )
      this.promotionDriver.requestCancelPromotion( pPromotion )
      this.promotionListSelected.remove( pPromotion )
      if(pPromotion instanceof PromotionAvailable){
        List<IPromotionAvailable> promotionListTmp = new ArrayList<>()
        promotionListTmp.addAll( promotionList )
        promotionList.clear()
        for(IPromotionAvailable prom : promotionListTmp){
          if( prom.promotion instanceof PromotionSingle ){
            FindOrCreate( promotionList, prom.promotion.entity.idPromocion, prom.appliesToList.first().orderDetail.sku, prom )
          } else if( prom.promotion instanceof PromotionCombo ){
            FindOrCreate( promotionList, prom.promotion.base.entity.idPromocion, prom.appliesToList.first().orderDetail.sku, prom )
          }
        }
      }
    }
  }

  Order getOrder( ) {
    return order
  }

  public List<IPromotionAvailable> getPromotionList( ) {
    return this.promotionList
  }

  public List<IPromotionAvailable> getPromotionListSelected( ) {
        return this.promotionListSelected
  }

  DefaultTableModel getPromotionModel( ) {
    return this.promotionModel
  }

  void refreshData( ) {
    this.promotionDriver.enableItemsTableEvents( false )
    this.getPromotionModel().fireTableDataChanged()
    updateOrder( order?.id )
    this.promotionDriver.enableItemsTableEvents( true )
  }

  public void focusGained( FocusEvent e ) {

  }

  public void focusLost( FocusEvent e ) {
    if ( itemSearch.text.length() > 0 ) {
      doItemSearch()
      itemSearch.requestFocus()
    }
  }

  private void fireRequestQuote( ) {
    if ( itemsModel.size() > 0 ) {
      if ( paymentsModel.size() == 0 ) {
        OrderController.requestSaveAsQuote( order, customer )
        this.reset()
      } else {
        this.change.text = TXT_PAYMENTS_PRESENT
      }
    } else {
      this.change.text = TXT_NO_ORDER_PRESENT
    }
  }



  private void fireRequestAnnular( ) {
    OrderController.printVoucherTpv( order.id, false )
    CancellationController.annuleTpvPayments( StringUtils.trimToEmpty(order.id) )
    this.reset()
    printButton.enabled = true
    quoteButton.enabled = true
    closeButton.enabled = true
    annuleButton.visible = false
    itemSearch.enabled = true
    operationType.enabled = true
    mainWindow.ordersMenu.enabled = true
    mainWindow.inventoryMenu.enabled = true
    mainWindow.sendReceivedInventoryMenu.enabled = true
    mainWindow.reportsMenu.enabled = true
    mainWindow.toolsMenu.enabled = true
  }

  private void reset() {
    order = new Order()
    customer = CustomerController.findDefaultCustomer()
    // Benja: Favor de no cambiar la siguiente linea. Esta comentada porque NO debe de estar
    // this.promotionList = new ArrayList<PromotionAvailable>()
    this.promotionListSelected.clear()
    this.promotionSelectedList.clear()
    this.getPromotionDriver().init( this )
    doBindings()
    annuleButton.visible = false
    operationType.setSelectedItem( OperationType.DEFAULT )
  }

  private void setCustomerInOrder() {
    if ((order?.id != null) && (customer != null)) {
      if ( !order.customer.equals(customer) ) {
        order.customer = customer
        OrderController.saveCustomerForOrder(order.id, customer.id)
      }
    }
  }

  private List<IPromotionAvailable> lstPromotionsAvalibles ( OrderItem orderItem ){
      List<IPromotionAvailable> promotionListTmp = new ArrayList<>()
      promotionListTmp.addAll( promotionList )
      promotionList.clear()
      for(IPromotionAvailable prom : promotionListTmp){
        if( prom instanceof PromotionAvailable ){
          if( prom.promotion instanceof PromotionSingle ){
            FindOrCreate( promotionList, prom.promotion.entity.idPromocion, prom.appliesToList.first().orderDetail.sku, prom )
          } else if( prom.promotion instanceof PromotionCombo ){
            FindOrCreate( promotionList, prom.promotion.base.entity.idPromocion, prom.appliesToList.first().orderDetail.sku, prom )
          }
        }
      }

      List<IPromotionAvailable> lstPromosArt = new ArrayList<>()
      for(IPromotionAvailable promo : promotionList){
        if( promo instanceof PromotionAvailable ){
          if( orderItem.item.id == promo.appliesToList.get(0).orderDetail.sku ){
              Boolean valid = true
              for(IPromotionAvailable promoSelected : promotionListSelected){
                  if( promo instanceof PromotionCombo ){
                      if( promoSelected.promotion.base.entity.idPromocion == promo.promotion.base.entity.idPromocion ){
                          valid = false
                      }
                  } else if( promo instanceof PromotionSingle ){
                      if( promoSelected.promotion.base.entity.idPromocion == promo.promotion.entity.idPromocion ){
                          valid = false
                      }
                  }
                  if( valid ){
                      if( !promoSelected instanceof PromotionDiscount){
                        for(PromotionApplied applied : promoSelected.appliesToList){
                          if(applied.orderDetail.sku == orderItem.item.id){
                            valid = false
                          }
                        }
                      }
                  }
              }
              if( valid ){
                  lstPromosArt.add( promo )
              }
          }
        }
      }
    return lstPromosArt
  }


  private Boolean validWarranty( ){
    canceledWarranty = false
    Boolean valid = true
    Boolean applyValid = false
    List<Integer> lstIdGar = new ArrayList<>()
    List<Integer> lstIdArm = new ArrayList<>()
    for(OrderItem orderItem : order.items){
      if( StringUtils.trimToEmpty(orderItem.item.type).equalsIgnoreCase(TAG_GENERICO_SEGUROS) ){
        for(int i=0;i<orderItem.quantity;i++){
          lstIdGar.add(orderItem.item.id)
        }
      } else if( StringUtils.trimToEmpty(orderItem.item.type).equalsIgnoreCase(TAG_GENERICO_ARMAZON) ){
        for(int i=0;i<orderItem.quantity;i++){
          lstIdArm.add(orderItem.item.id)
        }
      }
    }

    if( lstIdGar.size() > 0 ){
      Item itemGar = ItemController.findItem(lstIdGar.first())
      if( OrderController.validWarranty( promotionListSelected, itemGar ) ){
        applyValid = true
      }
      if( applyValid ){
      if( lstIdArm.size() == 1 ){
        if( lstIdGar.size() == 1 ){
          BigDecimal amount = BigDecimal.ZERO
          for(OrderItem orderItem : order.items){
            if( orderItem.item.id == lstIdArm.first() ){
              amount = orderItem.item.price
            }
          }
          BigDecimal warrantyAmount = ItemController.warrantyValid( amount, lstIdGar.first() )
          if( warrantyAmount.compareTo(BigDecimal.ZERO) > 0 ){
            Warranty warranty = new Warranty()
            warranty.amount = amount
            warranty.idItem = lstIdArm.first()
            lstWarranty.add( warranty )
            //ItemController.printWarranty( amount, lstIdArm.first() )
            //valid = true
            lstIdGar.clear()
          } else {
            MSJ_ERROR_WARRANTY = "Garantia Invalida."
            valid = false
          }
        } else {
          MSJ_ERROR_WARRANTY = "Seleccione solo una garantia."
          valid = false
        }
      } else {
        if( lstIdArm.size() > 1 ){
          if( lstIdGar.size() <= lstIdArm.size() ){
            Boolean artSameRange = false
            if( lstIdArm.size() > lstIdGar.size() ){
              Integer count = 0
              for(Integer id : lstIdGar){
                count = 0
                MontoGarantia garantia = ItemController.findWarranty( ItemController.findItem( id ).listPrice )
                for(OrderItem item : order.items){
                  Item it = item.item
                  if(StringUtils.trimToEmpty(it.type).equalsIgnoreCase(TAG_GENERICO_ARMAZON) &&
                          it.price.compareTo(garantia.montoMinimo) >= 0 && it.price.compareTo(garantia.montoMaximo) <= 0){
                    count = count+1
                  }
                }
                if( count > 1 ){
                  artSameRange = true
                }
              }
            }
            if( artSameRange ){
                List<Integer> lstIdGarTmp = new ArrayList<>()
                lstIdGarTmp.addAll( lstIdGar )
                Integer idGarUsed = 0
                for(Integer idGar : lstIdGarTmp ){
                    List<Item> lstFrames = new ArrayList<>()
                    List<Integer> lstIdArmTmp = new ArrayList<>()
                    lstIdArmTmp.addAll( lstIdArm )
                    for( Integer id : lstIdArm ){
                      Item i = null
                      for(OrderItem orderItem1 : order.items){
                        if( orderItem1.item.id == id ){
                          i = orderItem1.item
                        }
                      }
                      if( ItemController.warrantyValid( i.price, idGar ) ){
                        lstFrames.add(i)
                      }
                    }
                    Item itemFrame = null
                    Boolean canceled = false
                    if( lstIdGar.size() > 0 && lstFrames.size() > 1 ){
                        WarrantySelectionDialog dialog = new WarrantySelectionDialog( lstFrames, ItemController.findItem( idGar ) )
                        dialog.show()
                        itemFrame = dialog.selectedFrame
                        canceled = dialog.canceled
                    } else if( lstFrames.size() == 1 ){
                      if( lstIdGar.size() > 0 ){
                            BigDecimal amount = BigDecimal.ZERO
                            for(OrderItem orderItem : order.items){
                                if( orderItem.item.id == lstFrames.first().id ){
                                    amount = orderItem.item.price
                                }
                            }
                            BigDecimal warrantyAmount = ItemController.warrantyValid( amount, idGar )
                            if( warrantyAmount.compareTo(BigDecimal.ZERO) > 0 ){
                                lstIdArm.clear()
                                for( Integer id : lstIdArmTmp ){
                                    if( id != lstFrames.first().id ){
                                        lstIdArm.add(id)
                                    }
                                }
                                Warranty warranty = new Warranty()
                                warranty.amount = amount
                                warranty.idItem = lstFrames.first().id
                                lstWarranty.add( warranty )
                                //ItemController.printWarranty( amount, itemFrame.id )
                                //valid = true
                                idGarUsed = idGarUsed+1
                            } else {
                                MSJ_ERROR_WARRANTY = "Garantia Invalida."
                                valid = false
                            }
                      }
                    }
                    if( itemFrame != null ){
                        if( lstIdGar.size() > 0 ){
                            BigDecimal amount = BigDecimal.ZERO
                            for(OrderItem orderItem : order.items){
                                if( orderItem.item.id == itemFrame.id ){
                                    amount = orderItem.item.price
                                }
                            }
                            BigDecimal warrantyAmount = ItemController.warrantyValid( amount, idGar )
                            if( warrantyAmount.compareTo(BigDecimal.ZERO) > 0 ){
                                lstIdArm.clear()
                                for( Integer id : lstIdArmTmp ){
                                    if( id != itemFrame.id ){
                                        lstIdArm.add(id)
                                    }
                                }
                                Warranty warranty = new Warranty()
                                warranty.amount = amount
                                warranty.idItem = itemFrame.id
                                lstWarranty.add( warranty )
                                //ItemController.printWarranty( amount, itemFrame.id )
                                //valid = true
                                idGarUsed = idGarUsed+1
                            } else {
                              MSJ_ERROR_WARRANTY = "Garantia Invalida."
                              valid = false
                            }
                        }
                    } else {
                      if( canceled ){
                        canceledWarranty = true
                        break
                      }
                    }
                }
                if( idGarUsed == lstIdGar.size() ){
                  lstIdGar.clear()
                }
            } else {
              List<Integer> lstIdGarTmp = new ArrayList<>()
              Integer idGarUsed = 0
              Integer idArmUsed = 0
              List<OrderItem> lstItems = new ArrayList<>()
              lstItems.addAll( order.items )
              Boolean validTmp = true
              for(Integer id : lstIdGar){
                idGarUsed = 0
                if( validTmp ){
                for(OrderItem idArm : lstItems ){
                  MontoGarantia garantia = ItemController.findWarranty( ItemController.findItem( id ).listPrice )
                  Item item = idArm.item
                   if( StringUtils.trimToEmpty(item.type).equalsIgnoreCase(TAG_GENERICO_ARMAZON) &&
                           item.price.compareTo(garantia.montoMinimo) >= 0 && item.price.compareTo(garantia.montoMaximo) <= 0){
                     Warranty warranty = new Warranty()
                     warranty.amount = item.price
                     warranty.idItem = item.id
                     lstWarranty.add( warranty )
                     //ItemController.printWarranty( item.price, item.id )
                     //valid = true
                     idGarUsed = id
                     idArmUsed = idArm.item.id
                     break
                   } else {
                     //validTmp = false
                     //break
                   }
                }

              } else {
                lstWarranty.clear()
                //break
              }
                Integer count = 0
                lstItems.clear()
                for(OrderItem itemTmp : order.items){
                  if( StringUtils.trimToEmpty(itemTmp.item.type).equalsIgnoreCase(TAG_GENERICO_ARMAZON) ){
                    if( itemTmp.item.id != idArmUsed ){
                      lstItems.add( itemTmp )
                    } else {
                      if( count == 0 ){
                        count = count + 1
                      } else {
                        lstItems.add( itemTmp )
                      }
                    }
                  }
                }
              }
              if( idGarUsed > 0 ){
                lstIdGar.clear()
              }
            }
          } else {
            sb.optionPane(
               message: "No puede regustrar mas garantias que armazones.",
               messageType: JOptionPane.ERROR_MESSAGE
            ).createDialog( this, 'No se puede registrar la venta' )
              .show()
          }
        }
      }
    }
    if( lstIdGar.size() > 0 ){
      valid = false
    } else {
      valid = true
    }
    }
    return valid
  }

  public static IPromotionAvailable FindOrCreate( List<IPromotionAvailable> lstPromos, Integer idPromo, Integer idArt, IPromotionAvailable promotionAvailable ) {
        IPromotionAvailable found = null;
        for ( IPromotionAvailable prom : lstPromos ) {
          if( promotionAvailable.enabledByList.size() > 1 ){
            break
          }
            if(prom.promotion instanceof PromotionSingle){
              if( prom.promotion.entity.idPromocion == idPromo && prom.appliesToList.first().orderDetail.sku == idArt ){
                found = prom;
                break;
              }
            } else if(prom.promotion instanceof PromotionCombo){
              if( prom.promotion.base.entity.idPromocion == idPromo && prom.appliesToList.first().orderDetail.sku == idArt ){
                  found = prom;
                  break;
              }
            }
        }
        if ( found == null ) {
            found = promotionAvailable
            lstPromos.add( found );
        }
        return found;
  }



  void payDue( Boolean showPaymDial ){
    if ( order.due ) {
      new PaymentDialog( this, order, null, order.due ).show()
      updateOrder( order?.id )
      processPayment( showPaymDial )
    } else if( order.due.doubleValue() <= 0.00 && order.items.size() > 0 ){
      processPayment( showPaymDial )
    } else {
      sb.optionPane(
         message: 'No hay saldo para aplicar pago',
         messageType: JOptionPane.ERROR_MESSAGE
      ).createDialog( this, 'Pago sin saldo' )
        .show()
    }
  }

  void saveOrderAfterPay(){
    saveOrder( )
    printButton.enabled = true
    quoteButton.enabled = true
    closeButton.enabled = true
    annuleButton.visible = false
    itemSearch.enabled = true
    operationType.enabled = true
    mainWindow.ordersMenu.enabled = true
    mainWindow.inventoryMenu.enabled = true
    mainWindow.sendReceivedInventoryMenu.enabled = true
    mainWindow.reportsMenu.enabled = true
    mainWindow.toolsMenu.enabled = true
  }


  void processPayment( Boolean showPaymDial ){
    if( order.due.compareTo(BigDecimal.ZERO) <= 0 ){
      saveOrderAfterPay( )
    } else {
      if( order.due.compareTo(order.total) == 0 ){
        printButton.enabled = true
        quoteButton.enabled = true
        closeButton.enabled = true
        annuleButton.visible = false
        itemSearch.enabled = true
        operationType.enabled = true
        mainWindow.ordersMenu.enabled = true
        mainWindow.inventoryMenu.enabled = true
        mainWindow.sendReceivedInventoryMenu.enabled = true
        mainWindow.reportsMenu.enabled = true
        mainWindow.toolsMenu.enabled = true
      } else {
        if( showPaymDial ){
          new PaymentDialog( this, order, null, order.due ).show()
        }
        updateOrder( order?.id )
        if( order.due.compareTo(BigDecimal.ZERO) <= 0 ){
          saveOrderAfterPay( )
        } else {
          printButton.enabled = false
          quoteButton.enabled = false
          closeButton.enabled = false
          annuleButton.visible = true
          itemSearch.enabled = false
          operationType.enabled = false
          mainWindow.ordersMenu.enabled = false
          mainWindow.inventoryMenu.enabled = false
          mainWindow.sendReceivedInventoryMenu.enabled = false
          mainWindow.reportsMenu.enabled = false
          mainWindow.toolsMenu.enabled = false
        }
      }
    }
  }


}
