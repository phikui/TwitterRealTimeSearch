package gui;

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
import model.ConfigurationObject;
import model.TweetObject;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Guerki on 16/5/15.
 */
public class MainAppController implements Initializable {
    final static ObservableList<TweetObject> displaysTweets = FXCollections.observableArrayList();
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
    @FXML
    TextField queryfield;

    // gets tweets from the database according to the queries and displays them in the gui
    public static void fillTable(String queries) {
        /*
        displaysTweets.clear();
        String chosenIndex = ConfigurationObject.getIndexType();
        IRTSIndex index;
        switch (chosenIndex){
            case "AO": index  = new AOIIndex();
                break;
            case "TPL": index = new TPLIndex();
                break;
            case "LSII": index = new LSIIIndex();
                break;
            default: index  = new AOIIndex();
                break;
        }

        TransportObject transportObjectQuery = new TransportObject(queries, new Date(), ConfigurationObject.getNumberOfTweets());
        List<String> terms = new LinkedList<String>();
        List<Integer> termIDs = new LinkedList<Integer>();

        //split the query terms into an array of invidual terms
        List<String> individual_queries = Arrays.asList(queries.split("\\s*,\\s*"));
        for (String item : individual_queries){
            terms.add(item);
            try {
                termIDs.add(TermDictionary.getTermID(item));
            }
            catch(NullPointerException e){
                System.err.println("No Tweets matching your Queries found!");
            }
        }

        transportObjectQuery.setTerms(terms);
        transportObjectQuery.setTermIDs(termIDs);
        List<Integer> tweetIdList = index.searchTweetIDs(transportObjectQuery);
        List<TweetObject> tweetlist = new ArrayList<TweetObject>();
        for (Integer id : tweetIdList) {
            TweetObject tweet = TweetDictionary.getTransportObject(id).getTweetObject();
            tweetlist.add(tweet);
            System.out.println(tweet.getText());
        }
        displaysTweets.addAll(tweetlist);
        */
    }

    // Attach a listener to the index combobox so the indices get filled before anything is searched
    private void indexChanged(ActionEvent event) {
        ConfigurationObject.setIndexType(indexType.getSelectionModel().getSelectedItem().toString());
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

    private void startButtonPushed() {
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
        String queries = queryfield.getPromptText().toString();
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

        fillTable(queries);
    }
}
