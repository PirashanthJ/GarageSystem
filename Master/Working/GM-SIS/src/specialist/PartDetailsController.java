/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package specialist;

import common.Database;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Pirashanth
 */
public class PartDetailsController implements Initializable {

    Database db= Database.getInstance();
    Connection connection=db.getConnection();
    /**
     * show part details
     */
    @FXML
    private TextField name;
    @FXML
    private TextArea description;
    @FXML
    private TextField id;
    @FXML
    private TextField installDate;
    private Tab parentTab;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        
    }   
    @FXML
    public void partDetails(String partID)
    {
       Statement statement;ResultSet rs;
       name.setEditable(false);
       description.setEditable(false);
       id.setEditable(false);
       installDate.setEditable(false);

       try
       {
           statement= connection.createStatement();
           rs=statement.executeQuery("select PartInfo.Name,PartInfo.Description, Part.ID, Part.InstallDate from Part inner join PartInfo on PartInfo.Name=Part.Name where Part.ID="+Integer.parseInt(partID));
           name.setText(rs.getString("Name"));
           description.setText(rs.getString("Description"));
           id.setText(rs.getString("ID"));
           installDate.setText(rs.getString("InstallDate"));

       }
       catch(NullPointerException e)
       {
            showError("Unable to retrieve", "Cannot get all parts details");
       } 
       catch (IllegalArgumentException e) 
       {
            showError("Validation Error", e.getMessage());
       } 
       catch (SQLException ex) 
       {
            showError("Database Error", ex.getMessage());
            Logger.getLogger(PartDetailsController.class.getName()).log(Level.SEVERE, null, ex);
       }
        
    }
    public void setParentTab(Tab tab)
    {
        this.parentTab=tab;
    }
    private void showError(String title, String content) 
    {
        Alert alert;
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
}
