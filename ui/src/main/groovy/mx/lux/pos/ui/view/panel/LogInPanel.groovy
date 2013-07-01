package mx.lux.pos.ui.view.panel

import groovy.swing.SwingBuilder
import mx.lux.pos.ui.controller.AccessController
import mx.lux.pos.ui.model.UpperCaseDocument
import mx.lux.pos.ui.model.User
import net.miginfocom.swing.MigLayout
import org.apache.commons.lang3.StringUtils

import java.awt.Color
import java.awt.Font
import javax.swing.border.TitledBorder
import javax.swing.*

class LogInPanel extends JPanel {

  private SwingBuilder sb
  private JTextField username
  private JPasswordField password
  private JLabel messages
  private JButton logInButton
  private Closure doAction
  private String version

  LogInPanel( Closure doAction, String version ) {
    this.version = version
    this.doAction = doAction ?: {}
    sb = new SwingBuilder()
    buildUI()
  }

  private void buildUI( ) {
    sb.panel( this, layout: new MigLayout( 'wrap,center', '[fill]', '[top]' ) ) {
      panel( border: new TitledBorder( 'Ingresa tus datos:' ),
          layout: new MigLayout( 'wrap 2', '[fill,100!][fill,130!]', '[fill,30!][fill,30!][][]' ),
      ) {
        label( 'Empleado' )
        username = textField( font: new Font( '', Font.BOLD, 14 ),
            document: new UpperCaseDocument(),
            horizontalAlignment: JTextField.CENTER,
            actionPerformed: {logInButton.doClick()}
        )

        label( 'Contrase\u00f1a' )
        password = passwordField( font: new Font( '', Font.BOLD, 14 ),
            horizontalAlignment: JTextField.CENTER,
            actionPerformed: {logInButton.doClick()}
        )

        messages = label( foreground: Color.RED, constraints: "span 2" )
        label()
        label( text: version, horizontalAlignment: JLabel.RIGHT )
      }

      logInButton = button( 'Acceder', actionPerformed: doLogIn, constraints: 'right,span,w 125!,h 40!' )
    }
  }

  private def doLogIn = {
    logInButton.enabled = false
    User user = AccessController.logIn( username.text, password.text )
    if ( StringUtils.isNotBlank( user?.username ) ) {
      messages.text = null
      messages.visible = false
      doAction()
    } else {
      messages.text = 'Empleado/Contrase\u00f1a incorrectos'
      messages.visible = true
    }
    password.text = null
    logInButton.enabled = true
  }
}