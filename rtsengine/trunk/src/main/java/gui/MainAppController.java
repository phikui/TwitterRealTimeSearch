package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import gui.MainApp;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * Created by Guerki on 16/5/15.
 */
public class MainAppController implements Initializable {
    @FXML
    Button start;
    @FXML
    ToggleButton streamTweets;
    @FXML
    ToggleButton streamQueries;
    @FXML
    ComboBox<String> indexType;
    @FXML
    TextField numberOfThreads;
    @FXML
    TextField numberOfTweets;
    @FXML
    TextField ratio;
    @FXML
    TextField wSignificance;
    @FXML
    TextField wSimilarity;
    @FXML
    TextField wFreshness;

    public void initialize(URL url, ResourceBundle rsrcs) {
        start.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                /**
                 * Get all the information from the GUI
                 */
                int nNumberOfThreads = Integer.parseInt(numberOfThreads.getCharacters().toString());
                int nNumberOfTweets = Integer.parseInt(numberOfTweets.getCharacters().toString());
                int nRatio = Integer.parseInt(ratio.getCharacters().toString());
                int nWSignificance = Integer.parseInt(wSignificance.getCharacters().toString());
                int nWSimilarity = Integer.parseInt(wSimilarity.getCharacters().toString());
                int nWFreshness = Integer.parseInt(wFreshness.getCharacters().toString());
                String nIndexType = indexType.getSelectionModel().getSelectedItem().toString();
                Boolean nStreamTweets = streamTweets.isSelected();
                Boolean nStreamQueries = streamQueries.isSelected();

                /**
                 * Check if data is valid
                 */
                if (nWFreshness+nWSignificance+nWSimilarity != 100){
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Wrong Weights Ratio");
                    alert.setHeaderText(null);
                    alert.setContentText("The sum of all the weights needs to equal 100%.");
                    alert.showAndWait();
                    return;
                }
                if (nRatio < 0 || nRatio > 100){
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Wrong Ratio");
                    alert.setHeaderText(null);
                    alert.setContentText("The Ratio must be between 0% and 100%.");
                    alert.showAndWait();
                    return;
                }

                /**
                 * Save all the parameters into the ConfigurationObject
                 */
                ConfigurationObject.setIndexType(nIndexType);
                ConfigurationObject.setNumberOfThreads(nNumberOfThreads);
                ConfigurationObject.setNumberOfTweets(nNumberOfTweets);
                ConfigurationObject.setRatio(nRatio);
                ConfigurationObject.setStreamQueries(nStreamQueries);
                ConfigurationObject.setStreamTweets(nStreamTweets);
                ConfigurationObject.setwFreshness(nWFreshness);
                ConfigurationObject.setwSignificance(nWSignificance);
                ConfigurationObject.setwSimilarity(nWSimilarity);
            }
        });
    }
}
