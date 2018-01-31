package parts.GUI;

import common.Database;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import parts.logic.Part;
import parts.logic.PartInventory;

/**
 * FXML Controller class
 *
 * @author Mia
 */
public class RecentStockChangesViewController implements Initializable {

    @FXML
    private TableView<DataForTableRow> partView;
    @FXML
    private DatePicker fromDate;
    @FXML
    private DatePicker toDate;
    @FXML
    private ChoiceBox<String> showRecentChoice;
    
    private PartInventory inventory;
    @FXML
    private TableColumn<DataForTableRow, String> partNameColumn;
    @FXML
    private TableColumn<DataForTableRow, Integer> stockColumn;
    private Tab parentTab;
    private Tab bookingsTab;
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Database db = Database.getInstance();
        inventory = PartInventory.getInstance();
        showRecentChoice.setItems(FXCollections.observableArrayList("Additions", "Withdrawals"));
        showRecentChoice.getSelectionModel().selectFirst();
        stockColumn.setText("Stock Additions");
        showRecentChoice.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>(){
                    @Override
                    public void changed(ObservableValue ov, Number value, Number newValue){
                        if(newValue.intValue() == 0){
                            stockColumn.setText("Stock Additions");
                        }else if(newValue.intValue() == 1){
                            stockColumn.setText("Stock Withdrawals");
                        }
                        show(null);
                    }
                });
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate from = LocalDate.parse(db.getCurrentDate(), formatter);
        LocalDate to = LocalDate.parse(db.getCurrentDate(), formatter);
        fromDate.setValue(from.minusWeeks(1));
        toDate.setValue(to);
        fromDate.valueProperty().addListener((ov, oldValue, newValue) -> {
            show(null);
        });
        fromDate.setDayCellFactory(dateCellFactoryForFrom());
        toDate.valueProperty().addListener((ov, oldValue, newValue) -> {
            show(null);
        });
        toDate.setDayCellFactory(dateCellFactoryForTo());
        partView.setPlaceholder(new Label("No Part Changes During this Period"));
        show(null);
    }    
    
    /**
     * creates the date cell factory for the 'from' datepicker, so that any dates 'from' can't
     * be after 'to'
     * Code from: https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/date-picker.htm
     * @return the date cell factory greyed out where necessary 
     */
    private Callback<DatePicker, DateCell> dateCellFactoryForFrom(){
        return 
            new Callback<DatePicker, DateCell>() {
                @Override
                public DateCell call(final DatePicker datePicker) {
                    return new DateCell() {
                        @Override
                        public void updateItem(LocalDate item, boolean empty) {
                            super.updateItem(item, empty);
                           
                            if (item.isAfter(
                                    toDate.getValue())
                                ) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #C0C0C0;");
                            }   
                    }
                };
            }
        };
    }
    
    /**
     * creates the date cell factory for the 'to' datepicker, so that any dates 'to'
     * cant be before 'from' 
     * Code from: https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/date-picker.htm
     * @return the date cell factory greyed out where necessary 
     */
    private Callback<DatePicker, DateCell>dateCellFactoryForTo(){
        return 
            new Callback<DatePicker, DateCell>() {
                @Override
                public DateCell call(final DatePicker datePicker) {
                    return new DateCell() {
                        @Override
                        public void updateItem(LocalDate item, boolean empty) {
                            super.updateItem(item, empty);
                           
                            if (item.isBefore(
                                    fromDate.getValue())
                                ) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #C0C0C0;");
                            }   
                    }
                };
            }
        };
    
    }

    /**
     * Takes the user to the part home screen
     * @param event The event that causes this method to run
     */
    @FXML
    private void goHome(ActionEvent event) {
        // gets current stage
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); 
        Parent root;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("searchForParts.fxml"));
        try {
            // load document I want to go to
            root = loader.load();
            SearchForPartsController controller = loader.getController();
            controller.setTabs(parentTab, bookingsTab);
            parentTab.setContent(root);
            
        } catch (IOException ex) {
            Logger.getLogger(SearchForPartsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Shows the recent additions/withdrawals for the time period given in the 
     * GUI
     * @param event The event that causes this method to run
     */
    @FXML
    private void show(ActionEvent event) {
        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();
        String fromDate = from.toString();
        String toDate = to.toString();
        ArrayList<Part> parts = new ArrayList<>();
        
        if(showRecentChoice.getSelectionModel().getSelectedIndex() == 0){
            parts = inventory.getRecentAdditions(fromDate, toDate);
        }else if(showRecentChoice.getSelectionModel().getSelectedIndex() == 1){
            parts = inventory.getRecentWithdraws(fromDate, toDate);
        }
        
        ArrayList<DataForTableRow> rows = new ArrayList<>();
        if(!parts.isEmpty()){
            HashMap<String, Integer> countsOfStockChanges = new HashMap<>();
            for(int i = 0; i < parts.size(); i++){
                if(countsOfStockChanges.containsKey(parts.get(i).getName())){
                    Integer count = countsOfStockChanges.get(parts.get(i).getName()) + 1;
                    countsOfStockChanges.replace(parts.get(i).getName(), count);
                }else{
                    countsOfStockChanges.put(parts.get(i).getName(), 1);
                }
            }
            
            Iterator<String> keys = countsOfStockChanges.keySet().iterator();
            for(int i = 0; i < countsOfStockChanges.size(); i++){
                String part = keys.next();
                rows.add(new DataForTableRow(part, countsOfStockChanges.get(part)));
            }
        }
        partNameColumn.setCellValueFactory(new PropertyValueFactory<>("partName"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockChange"));
        partView.setItems(FXCollections.observableList(rows));
    }
    
    /**
     * Class used to populate tableview in this view.
     */
    public class DataForTableRow{
        private final String partName;
        private final int stockChange;

        private DataForTableRow(String name, int count){
            partName = name;
            stockChange = count;
        }
        
        public String getPartName(){
            return this.partName;
        }
        
        public int getStockChange(){
            return this.stockChange;
        }
        
    }
    
    /**
     * Sets the parent tab and the bookings tab to display part view properly and 
     * bookings tab where required. 
     * 
     * @param parent The parent tab for the part views
     * @param booking The tab that contains the booking views
     */
    public void setTabs(Tab parent, Tab booking){
        parentTab = parent;
        bookingsTab = booking;
    }
    
}
