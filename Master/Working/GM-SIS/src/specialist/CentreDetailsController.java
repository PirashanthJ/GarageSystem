
package specialist;

import common.Database;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Pirashanth
 * Add,edit and delete bookings
 */
public class CentreDetailsController implements Initializable 
{
    Database db= Database.getInstance();
    Connection connection= db.getConnection();
    
    @FXML
    private Font x1;
    @FXML
    private ListView centre;
    @FXML
    private Button editButton;
    @FXML
    private Button showSPCButton;
    @FXML
    private Button deleteSPC;
    @FXML
    private Button addSPCButton;
    @FXML
    private TextField idT;
    @FXML
    private TextField addressT;
    @FXML
    private TextField emailT;
    @FXML
    private TextField numberT;
    @FXML
    private TextField nameT;
    @FXML
    private Button doneB;
    private Tab parentTab;
    private boolean isAdmin;
    @FXML
    private TableView<SPC> centreDetails;
    @FXML
    private TableColumn<SPC, String> name;
    @FXML
    private TableColumn<SPC, Integer> id;
    @FXML
    private TableColumn<SPC, String> email;
    @FXML
    private TableColumn<SPC, Integer> number;
    @FXML
    private TableColumn<SPC, String> address;
    @FXML
    private Label idLabel;
    
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        idT.setEditable(false);
        idT.setVisible(false);
        idLabel.setVisible(false);
        doneB.setVisible(false);
        // Code by "Evan Knowles" on StackOverflow - What is the recommended way to make a numeric TextField in JavaFX?
        numberT.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    numberT.setText(newValue.replaceAll("[^\\d]", "")); // Only allows integer values
                }
            }
        });
        

    }    

    @FXML
    private void addSPC(ActionEvent event)throws SQLException
    {
        if(nameT.getText().isEmpty()||addressT.getText().isEmpty()||emailT.getText().isEmpty()||numberT.getText().isEmpty()||!isValidEmailAddress(emailT.getText())){showError("Error", "Please check the following:\n-All the required details of the centre has been entered\n-Email is in correct format e.g:(softEng@qmul.ac.uk)");return;}
        if(checkSize()<10)// if already 10 SPC's in database no more can be added, if not add SPC
        {        
            int confirmed=showConfirm("","Add SPC","Do you want to add this SPC?");
            if(confirmed==0){return;}    
            
            SPC spc= new SPC(nameT.getText(),addressT.getText(),numberT.getText(),emailT.getText());//Call SPC instance and pass the details to add SPC
            showSPC();
            nameT.clear();addressT.clear();numberT.clear();emailT.clear();
        }
        else// show Error if reached 10 SPC's
        {
            showError("Reached Limit","Maximum number of 10 centres allowed has been reached");return;
        }
    }

    @FXML
    private void showSPC() // show list of all SPC's
    {
        String insertInto= "select * from SPCDetails";
        Statement statement;
        ArrayList<SPC> store= new ArrayList<SPC>();
        try
        {
            statement= connection.createStatement();
            ResultSet rs= statement.executeQuery(insertInto);
            while(rs.next())
            {
              SPC spc=new SPC(rs.getInt("SPCID"),rs.getString("SPCName"),rs.getString("SPCAddress"),rs.getString("SPCPhoneNo"),rs.getString("SPCEmail"));
              store.add(spc);
            } 
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        address.setCellValueFactory(new PropertyValueFactory<>("address"));
        number.setCellValueFactory(new PropertyValueFactory<>("number"));
        email.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        ObservableList<SPC> items =FXCollections.observableArrayList (store);
        centreDetails.setItems(items);
            
        }
        catch(SQLException err)
        {
            showError("Database Error", err.getMessage());
            Logger.getLogger(CentreDetailsController.class.getName()).log(Level.SEVERE, null, err);
            
        }
    }
       
    @FXML
    private void deleteSPC(ActionEvent event) throws SQLException 
    {
        if(centreDetails.getSelectionModel().getSelectedItem() == null){showError("Error", "Please check the following:\n- A centre has been chosen");return;}
        if(checkSize()==1){showError("Error", "The SPC cannot be deleted since else you will be left with no SPC's");return;}
        int confirmed=showConfirm("WARNING","Delete SPC","If you delete this SPC:\nAll bookings associated with the centre will also be deleted\nDo you want to delete this SPC?");
        if(confirmed==0){return;}
        int selectedID= centreDetails.getSelectionModel().getSelectedItem().SPCID();
        String toDelete="delete from SPCDetails where SPCID=?";
        PreparedStatement ps;
        try
        {
          ps=connection.prepareStatement(toDelete);
          ps.setInt(1, selectedID);
          ps.executeUpdate();
          showSPC();
        }
        catch(SQLException err)
        {
            showError("Database Error", err.getMessage());
            Logger.getLogger(CentreDetailsController.class.getName()).log(Level.SEVERE, null, err);
        }
        
    }
    @FXML
    private void editSPC()
            
    {
        if(centreDetails.getSelectionModel().getSelectedItem() == null){showError("Error", "Please check the following:\n- A centre has been chosen");return;}
        int confirmed=showConfirm("","Edit SPC","Do you want to edit this SPC?");
        if(confirmed==0){return;}
        addSPCButton.setDisable(true);
        editButton.setDisable(true);
        showSPCButton.setDisable(true);
        deleteSPC.setDisable(true);
        doneB.setVisible(true);
        nameT.setEditable(true);
        emailT.setEditable(true);
        numberT.setEditable(true);
        addressT.setEditable(true);
        int selectedID=centreDetails.getSelectionModel().getSelectedItem().SPCID();
        String toEdit="select * from SPCDetails where SPCID='"+selectedID+"'";
        Statement statement;
        try
        {
          statement=connection.createStatement();
          ResultSet rs= statement.executeQuery(toEdit);
          nameT.setText(rs.getString("SPCName"));
          numberT.setText((rs.getString("SPCPhoneNo")));
          emailT.setText(rs.getString("SPCEmail"));
          addressT.setText(rs.getString("SPCAddress"));
          idT.setText(Integer.toString(rs.getInt("SPCID")));
        }
        catch(SQLException err)
        {
            showError("Database Error", err.getMessage());
            Logger.getLogger(CentreDetailsController.class.getName()).log(Level.SEVERE, null, err);
        }
        
        
    }
    @FXML
    private void doneEditing()
    {
        int confirmed=showConfirm("","Finished editing SPC details","Do you want to update details?");
        if(confirmed==0){doneB.setVisible(false);nameT.clear();addressT.clear();emailT.clear();numberT.clear();idT.clear();return;} 
        if(nameT.getText().isEmpty()||addressT.getText().isEmpty()||emailT.getText().isEmpty()||numberT.getText().isEmpty()||!isValidEmailAddress(emailT.getText())){showError("Error", "Please check the following:\n-All the required details of the centre has been entered\n-Email is in correct format e.g:(softEng@qmul.ac.uk)");return;}
        
        int selectedID=Integer.parseInt(idT.getText());
        String toEdit=("update SPCDetails set SPCName=?, SPCAddress=?,SPCPhoneNo=?,SPCEmail=? where SPCID='"+selectedID+"'");
        
        try
        {
          PreparedStatement ps=connection.prepareStatement(toEdit);
          ps.setString(1,nameT.getText());
          ps.setString(2,addressT.getText());
          if(numberT.getText().matches(".*\\d+.*")){ps.setString(3,numberT.getText());}
          else{showError("Database Error", "Invalid phone number\nPlease try again");throw new NumberFormatException();}
          ps.setString(4,emailT.getText());
          
          
          ps.executeUpdate();
          showSPC();
          doneB.setVisible(false);
          nameT.clear();addressT.clear();emailT.clear();numberT.clear();idT.clear();
          addSPCButton.setDisable(false);
          editButton.setDisable(false);
          showSPCButton.setDisable(false);
          deleteSPC.setDisable(false);
          showSPC();
          showAlert("","Successfully Edited","SPC has been edited");
        }
        catch(SQLException err)
        {
            showError("Database Error", err.getMessage());
            Logger.getLogger(CentreDetailsController.class.getName()).log(Level.SEVERE, null, err);
        }
        
    }
    private int checkSize()throws SQLException// method to check the number of SPC's
    {
        Statement statement=connection.createStatement();
        ArrayList<Integer>id= new ArrayList<>();
        ResultSet rs= statement.executeQuery("select SPCID from SPCDetails");
        while(rs.next())
        {
            id.add(rs.getInt("SPCID"));
        }
        return id.size();
    }
        @FXML
    private void mainPage(ActionEvent event) throws IOException 
    {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); 
        Parent root;
        FXMLLoader spcloader=new FXMLLoader(getClass().getResource("MainPage.fxml"));
        try 
        {
            parentTab.setContent(spcloader.load());
            MainPageController controller= spcloader.<MainPageController>getController();// get the controller and pass the tab to setContent with new fxml file
            controller.setParentTab(parentTab);
            controller.setAdmin(isAdmin);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(CentreDetailsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void setParentTab(Tab tab)
    {
        this.parentTab=tab;
    }
    public void setAdmin(boolean admin)
    {
        isAdmin=admin;
    }
    
    //from http://stackoverflow.com/questions/624581/what-is-the-best-java-email-address-validation-method
    public boolean isValidEmailAddress(String email) 
    {
           String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
           java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
           java.util.regex.Matcher m = p.matcher(email);
           return m.matches();
    }
     private void showError(String title, String content) {
        Alert alert;
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
      private int showConfirm(String title,String header,String content)
    {
        Alert alert;
        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){return 1;}
        else{ return 0;}
    }
      private void showAlert(String title, String header, String content) {
        Alert alert ;
        alert= new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();
    }
    
}
