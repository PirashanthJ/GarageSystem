package common.gui;

import common.Database;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import parts.GUI.SearchForPartsController;


public class LoginController implements Initializable {

    @FXML
    private Text infoText;
    @FXML
    private TextField usernameBox;
    @FXML
    private TextField passwordBox;

    
    private String username;
    private boolean isAdmin = false;
    private static final Database DB = Database.getInstance(); 
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    //method to check login 
    public void login(ActionEvent Event) throws SQLException, IOException{
        String usercheckquery = "Select * From SystemUser "
                              + "Where Login = '"+usernameBox.getText()+"' "
                              + "AND Password = '"+passwordBox.getText()+"';";
        ResultSet rs = null;
        try {
            Connection conn = DB.getConnection();
            rs = conn.createStatement().executeQuery(usercheckquery);
            if (!rs.next()){ //No User data exists
                invalidLogin();
            } 
            else {
                infoText.setFill(Color.GREEN);
                infoText.setText("Authentication Succesful");
                username = rs.getString("Firstname") + " " + rs.getString("Surname");
                if (rs.getInt("isAdmin") == 1){
                    isAdmin = true;
                }
                PauseTransition delay = new PauseTransition(Duration.seconds(0.1));
                delay.setOnFinished( event -> validLogin() );
                delay.play();
            }
        } catch (SQLException ex) {
            System.err.println("Error: "+ex);
        } finally {
            rs.close();
        }
    }
    
    //displays invalid login
    private void invalidLogin(){
        infoText.setFill(Color.RED);
        infoText.setText("Invalid Login Details, please try again.");
    }
    
    //loads main frame when valid login
    public void validLogin(){
        try {
            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(getClass().getResource("MainFrame.fxml").openStream()); 
            MainFrameController controller = (MainFrameController)loader.getController();
            controller.setUsername(username);
            controller.isAdmin(isAdmin);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
            controller.setStage(stage);
            infoText.getScene().getWindow().hide();
        } catch (IOException ex) {
            Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
    
    //listener to hear if enter key is pressed
    public void keyListener(KeyEvent event) throws IOException, SQLException{
    if(event.getCode() == KeyCode.ENTER) {
          login(new ActionEvent());
    }
}
}
