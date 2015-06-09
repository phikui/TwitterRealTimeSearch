package gui;

import iocontroller.IOController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import model.ConfigurationObject;
import model.QueryReturnObject;
import model.TransportObject;
import model.TweetObject;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Guerki on 16/5/15.
 */
public class MainAppController implements Initializable {
    volatile static ObservableList<TweetObject> displaysTweets = FXCollections.observableArrayList();
    IOController ioController = new IOController();
    @FXML
    Button start;
    @FXML
    ComboBox<String> indexType;
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
    @FXML
    TextField queryfield;


    // Send Queryresults to the GUI
    public static void sendQueryResults(QueryReturnObject result){
        displaysTweets.clear();
        displaysTweets.addAll(result.getResults());
        System.out.println(result.getResults() + result.getQuery());
    }

    // Convert Index String to Enum
    private ConfigurationObject.IndexTypes toIndex(String index){
        switch (index) {
            case "AO":
                return ConfigurationObject.IndexTypes.APPEND_ONLY;
            case "TPL":
                return ConfigurationObject.IndexTypes.TRIPLE_POSTING_LIST;
            case "LSII":
                return ConfigurationObject.IndexTypes.LSII;
            default:
                return ConfigurationObject.IndexTypes.APPEND_ONLY;
        }
    }

    public void initialize(URL url, ResourceBundle rsrcs) {
        usernameCol.setCellValueFactory(new PropertyValueFactory<TweetObject,String>("username"));
        contentCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        followersCol.setCellValueFactory(new PropertyValueFactory<>("numberOfAuthorFollowers"));
        tweetTable.setItems(displaysTweets);
        ConfigurationObject.setIndexType(ConfigurationObject.IndexTypes.APPEND_ONLY);
        ioController.startAll();
        ioController.collectTweets();
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);

        // start button pushed
        start.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                startButtonPushed();

            }
        });

        // Listen for changes to the indexType
        indexType.setOnAction(this::indexChanged);

        // Enter Key is Pressed while in a textfield
        numberOfTweets.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    startButtonPushed();
                }
            }
        });
        queryfield.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    startButtonPushed();
                }
            }
        });
        numberOfThreads.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    startButtonPushed();
                }
            }
        });
        ratio.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    startButtonPushed();
                }
            }
        });
        wSignificance.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    startButtonPushed();
                }
            }
        });
        wFreshness.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    startButtonPushed();
                }
            }
        });
        wSimilarity.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    startButtonPushed();
                }
            }
        });

    }

    // Attach a listener to the index combobox so the indices get filled before anything is searched
    private void indexChanged(ActionEvent event) {
        //ioController.stopcollectingTweets();
        ConfigurationObject.setIndexType(toIndex(indexType.getSelectionModel().getSelectedItem()));
        //ioController.collectTweets();
    }
    private void startButtonPushed() {
        /**
         * Get all the information from the GUI
         */
        int nNumberOfTweets = Integer.parseInt(numberOfTweets.getCharacters().toString());
        int nWSignificance = Integer.parseInt(wSignificance.getCharacters().toString());
        int nWSimilarity = Integer.parseInt(wSimilarity.getCharacters().toString());
        int nWFreshness = Integer.parseInt(wFreshness.getCharacters().toString());
        ConfigurationObject.IndexTypes nIndexType = toIndex(indexType.getSelectionModel().getSelectedItem());
        String queries = queryfield.getText();

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
        if (nNumberOfTweets < 1){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Number of Tweets to small");
            alert.setHeaderText(null);
            alert.setContentText("The Number of Tweets bigger than 0.");
            alert.showAndWait();
            return;
        }
        /**
         * Save all the parameters into the ConfigurationObject
         */
        ConfigurationObject.setIndexType(nIndexType);
        ConfigurationObject.setNumberOfTweets(nNumberOfTweets);
        ConfigurationObject.setwFreshness(nWFreshness);
        ConfigurationObject.setwSignificance(nWSignificance);
        ConfigurationObject.setwSimilarity(nWSimilarity);
        System.out.println(queries);
        TransportObject query = new TransportObject(queries, Calendar.getInstance().getTime(), nNumberOfTweets);
        ioController.addTransportObject(query);
    }
}
