package gui;

import indices.IRTSIndex;
import indices.lsii.AppendOnlyIndex;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;


import java.io.IOException;
import java.net.URL;
import java.util.*;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import gui.MainApp;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.TermDictionary;
import model.TransportObject;
import model.TweetDictionary;
import model.TweetObject;
import twitter4j.GeoLocation;
import twitter4j.Place;
import utilities.RandomObjectFactory;
import test.RTSIndexTestCase;

/**
 * Created by Guerki on 16/5/15.
 */
public class MainAppController implements Initializable {
    @FXML
    Button start;
    @FXML
    ToggleButton stream;
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
    @FXML
    TableColumn usernameCol;
    @FXML
    TableColumn contentCol;
    @FXML
    TableColumn timestampCol;
    @FXML
    TableColumn followersCol;
    @FXML
    TableView<TweetObject> tweetTable;
    @FXML
    ScrollPane scroll;

    final static ObservableList<TweetObject> displaysTweets = FXCollections.observableArrayList();

    public static void fillTable() {
        displaysTweets.clear();
        IRTSIndex index = RTSIndexTestCase.main(null);
        TransportObject transportObjectQuery = new TransportObject("testterm", new Date(), ConfigurationObject.getNumberOfTweets());
        List<String> terms = new LinkedList<String>();
        List<Integer> termIDs = new LinkedList<Integer>();
        terms.add("testterm");
        termIDs.add(TermDictionary.getTermID("testterm"));
        transportObjectQuery.setTerms(terms);
        transportObjectQuery.setTermIDs(termIDs);
        List<Integer> tweetIdList = index.searchTweetIDs(transportObjectQuery);
        List<TweetObject> tweetlist = new ArrayList<TweetObject>();
        for (Integer id : tweetIdList) {
            TweetObject tweet = TweetDictionary.getTweetObject(id);
            tweetlist.add(tweet);
            System.out.println(tweet.getText());
        }
        displaysTweets.addAll(tweetlist);
    }
    public void initialize(URL url, ResourceBundle rsrcs) {
        usernameCol.setCellValueFactory(new PropertyValueFactory<TweetObject,String>("username"));
        contentCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        followersCol.setCellValueFactory(new PropertyValueFactory<>("numberOfAuthorFollowers"));
        tweetTable.setItems(displaysTweets);

        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);
        // start button pushed
        start.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                begin();

            }
        });
        // Enter Key is Pressed while in a textfield
        numberOfTweets.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    begin();
                }
            }
        });
        numberOfThreads.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    begin();
                }
            }
        });
        ratio.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    begin();
                }
            }
        });
        wSignificance.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    begin();
                }
            }
        });
        wFreshness.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    begin();
                }
            }
        });
        wSimilarity.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    begin();
                }
            }
        });

    }

    private void begin() {
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
        Boolean nStream = stream.isSelected();

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
        ConfigurationObject.setStream(nStream);
        ConfigurationObject.setwFreshness(nWFreshness);
        ConfigurationObject.setwSignificance(nWSignificance);
        ConfigurationObject.setwSimilarity(nWSimilarity);

        fillTable();
    }
}
