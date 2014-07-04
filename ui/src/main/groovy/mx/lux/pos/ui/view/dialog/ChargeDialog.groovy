package mx.lux.pos.ui.view.dialog

import groovy.swing.SwingBuilder
import mx.lux.pos.service.business.Registry
import mx.lux.pos.ui.controller.DailyCloseController
import mx.lux.pos.ui.view.verifier.DateVerifier
import net.miginfocom.swing.MigLayout

import javax.swing.*
import java.awt.*
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat

class ChargeDialog extends JDialog {

  private DateFormat df = new SimpleDateFormat( "dd/MM/yyyy" )
  private DateVerifier dv = DateVerifier.instance
  private def sb = new SwingBuilder()

  private JLabel cargando
  private String titulo
  private String contenido
  private Integer artCargado

  public boolean button = false

    ChargeDialog( String titulo, String contenido, Integer cantidad ) {
    this.artCargado = cantidad
    this.titulo = titulo
    this.contenido = contenido
    buildUI()
    //cargando()
  }

  // UI Layout Definition
  void buildUI( ) {
    sb.dialog( this,
        title: titulo,
        resizable: true,
        pack: true,
        modal: true,
        preferredSize: [ 290, 200 ],
        location: [ 200, 250 ],
        undecorated: true,
    ) {
      panel() {
        borderLayout()
        panel( constraints: BorderLayout.CENTER, layout: new MigLayout( "wrap,debug", "[grow,fill]", "20[]10[]" ) ) {
          label( text: contenido )
          cargando = label( )
        }
      }

    }
  }

  // UI Response
  protected void close( ) {
    this.visible = false
  }

  public void cargando( ){
    for(int i=0; i<=artCargado;i++){
      cargando.text = "Cargando Registro: "+ i
      Long time = timeWait()
      sleep( time )

    }
  }

  static Long timeWait(  ) {
    String seconds = 1
    Long time = 0L
    Long newTime = 0L
    try{
      time = NumberFormat.getInstance().parse( seconds.trim() ).toLong()
    } catch ( Exception e ){
      println( e )
    }
    newTime = time*1000L
    return newTime
  }

  void activate( ) {
    setVisible( true )
    cargando()
  }
}
