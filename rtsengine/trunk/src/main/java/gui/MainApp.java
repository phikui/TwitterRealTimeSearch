package gui;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import indices.IRTSIndex;
import iocontroller.IOController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.WindowEvent;
import model.TermDictionary;
import model.TransportObject;
import model.TweetDictionary;
import model.TweetObject;
import test.RTSIndexTestCase;

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    IOController ioController;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("RTS");

        initRootLayout();
        showMainView();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                ioController.stopAll();
                System.exit(0);
            }
        });
    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/gui/view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();
            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the main view inside the root layout.
     */
    public void showMainView() {
        try {
            // Load main view.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/gui/view/MainView.fxml"));
            AnchorPane main = (AnchorPane) loader.load();
            MainAppController controller = loader.getController();
            ioController = controller.getIOController();
            rootLayout.setCenter(main);

            main.setMinSize(rootLayout.getWidth(),rootLayout.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }


}