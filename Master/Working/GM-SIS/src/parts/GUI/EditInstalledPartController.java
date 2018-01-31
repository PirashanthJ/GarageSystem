package parts.GUI;

import common.Database;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Callback;
import parts.logic.InstalledPart;
import parts.logic.Utility;

/**
 * FXML Controller class
 *
 * @author Mia
 */
public class EditInstalledPartController implements Initializable {

    @FXML
    private Label partNameText;
    @FXML
    private DatePicker installDatePicker;
    @FXML
    private Label expiryDateText;
    
    private InstalledPart part;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    /**
     * By default populates the information fields in the 
     * @param part 
     */
    public void initializeTextViews(InstalledPart part){
        this.part = part;
        partNameText.setText(part.getPart().getName());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dateTime = LocalDate.parse(part.getInstallDate(), formatter);
        installDatePicker.setValue(dateTime);
        
        // add listener to update new expiry date when install date is changed
        // Code by ItachiUchiha (StackOverflow) - JavaFX datepicker - how to update date in second datepicker object?
        installDatePicker.valueProperty().addListener((ov, oldValue, newValue) -> {
            expiryDateText.setText(newValue.plusYears(1).toString());
        });
        installDatePicker.setDayCellFactory(getDateCellFactory());
        expiryDateText.setText(part.getWarrantyEndDate());
    }
    
    /**
     * Gets a date cell factory with invalid dates greyed out and unselectable 
     * Code from: https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/date-picker.htm
     * @return The date cell factory with invalid dates greyed out 
     */
    private Callback<DatePicker, DateCell> getDateCellFactory(){
        ArrayList<LocalDate> holidays = getHolidays();
        return 
            new Callback<DatePicker, DateCell>() {
                @Override
                public DateCell call(final DatePicker datePicker) {
                    return new DateCell() {
                        @Override
                        public void updateItem(LocalDate item, boolean empty) {
                            super.updateItem(item, empty);
                           
                            if (item.getDayOfWeek() == DayOfWeek.SUNDAY || holidays.contains(item)){
                                setDisable(true);
                                setStyle("-fx-background-color: #C0C0C0;");
                            }
                    }
                };
            }
        };
    }
    
    /**
     * Gets the garage holidays
     * 
     * @return An arraylist containing the dates of the holidays  
     */
    private ArrayList<LocalDate> getHolidays(){
        String getHolidays = "Select * FROM Holidays";
        Database db = Database.getInstance();
        Connection conn = db.getConnection();
        Statement statement = null;
        ResultSet rs = null;
        ArrayList<LocalDate> holidayDates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate from = LocalDate.parse(db.getCurrentDate(), formatter);
        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(getHolidays);
            while(!rs.isClosed() && rs.next()){
                holidayDates.add(LocalDate.parse(rs.getString("Date"), formatter));
            }
        } catch (SQLException ex) {
            Logger.getLogger(EditInstalledPartController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            Utility.closeStatementAndConnection(statement, conn);
        }
        return holidayDates;
    }
        

    /**
     * Updates the installed part and closes the window
     * 
     * @param event The event that causes this method to run
     */
    @FXML
    private void updateInstalledPart(ActionEvent event) {
        // if the install date is different update needed
        if(!installDatePicker.getValue().toString().equals(part.getInstallDate())){
            String updateInstalledPart = "UPDATE Part SET InstallDate=date('" 
                    + installDatePicker.getValue().toString() + "'), WithdrawDate=('" 
                    + expiryDateText.getText() + "') WHERE ID=" + part.getPart().getID();
            Database db = Database.getInstance();
            Connection conn = db.getConnection();
            Statement statement = null;
            try {
                statement = conn.createStatement();
                statement.executeUpdate(updateInstalledPart);
            } catch (SQLException ex) {
                Logger.getLogger(EditInstalledPartController.class.getName()).log(Level.SEVERE, null, ex);
            }finally{
                Utility.closeStatementAndConnection(statement, conn);
            }
        }// if there was no change no need to update anything
        // gets the popup window and closes it once part added
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
    
}
