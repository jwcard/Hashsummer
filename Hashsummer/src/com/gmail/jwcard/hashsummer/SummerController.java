package com.gmail.jwcard.hashsummer;

import java.io.File;
import java.net.URL;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

public class SummerController {

    @FXML // fx:id="root"
    private BorderPane root; // Value injected by FXMLLoader

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="clearButton"
    private Button clearButton; // Value injected by FXMLLoader

    @FXML // fx:id="algorithmButton"
    private ChoiceBox<String> algorithmButton; // Value injected by FXMLLoader

    @FXML // fx:id="stopButton"
    private Button stopButton; // Value injected by FXMLLoader

    @FXML // fx:id="statusWindow"
    private TextField statusWindow; // Value injected by FXMLLoader

    @FXML // fx:id="saveButton"
    private Button saveButton; // Value injected by FXMLLoader

    @FXML // fx:id="calcHashButton"
    private Button calcHashButton; // Value injected by FXMLLoader

    @FXML // fx:id="cmpHashButton"
    private Button cmpHashButton; // Value injected by FXMLLoader
    
    @FXML // fx:id="hashTable"
    private TableView<?> hashTable; // Value injected by FXMLLoader

    @FXML // fx:id="fileColumn"
    private TableColumn<?, ?> fileColumn; // Value injected by FXMLLoader

    @FXML // fx:id="hashColumn"
    private TableColumn<?, ?> hashColumn; // Value injected by FXMLLoader

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
	assert clearButton != null : "fx:id=\"clearButton\" was not injected: check your FXML file 'Summer.fxml'.";
	assert algorithmButton != null : "fx:id=\"algorithmButton\" was not injected: check your FXML file 'Summer.fxml'.";
	assert root != null : "fx:id=\"root\" was not injected: check your FXML file 'Summer.fxml'.";
	assert stopButton != null : "fx:id=\"stopButton\" was not injected: check your FXML file 'Summer.fxml'.";
	assert statusWindow != null : "fx:id=\"statusWindow\" was not injected: check your FXML file 'Summer.fxml'.";
	assert saveButton != null : "fx:id=\"saveButton\" was not injected: check your FXML file 'Summer.fxml'.";
	assert calcHashButton != null : "fx:id=\"calcHashButton\" was not injected: check your FXML file 'Summer.fxml'.";
	assert cmpHashButton != null : "fx:id=\"cmpHashButton\" was not injected: check your FXML file 'Summer.fxml'.";
	assert hashTable != null : "fx:id=\"hashTable\" was not injected: check your FXML file 'Summer.fxml'.";
	assert fileColumn != null : "fx:id=\"fileColumn\" was not injected: check your FXML file 'Summer.fxml'.";
	assert hashColumn != null : "fx:id=\"hashColumn\" was not injected: check your FXML file 'Summer.fxml'.";

	Set<String> digests = Security.getAlgorithms("MessageDigest");
	String[] options = digests.toArray(new String[digests.size()]);
	Arrays.sort(options);
	algorithmButton.setItems(FXCollections.observableArrayList(options));
	// always pick the last one which will be typically a SHA algorithm
	algorithmButton.setValue(options[options.length - 1]);
	algorithmButton.setTooltip(new Tooltip("Message digest algorithm"));
    }

    @FXML
    void doCalculateHash(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Calculate hash on files");
        String home = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(home));

        List<File> files = fileChooser
                .showOpenMultipleDialog(root.getScene().getWindow());
        if (files != null) {
            handleCompute(files);
        }
    }

    private void handleCompute(final List<File> files) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (File f : files) {
                    hashFile(f);
                }
                return null;
            }

            private void hashFile(File f) {
                if (f.isFile()) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            statusWindow.setText(f.getName());
                        }
                    });

                    tempDelay();

                    // TODO add dummy table values

                    tempDelay();
                }
            }

            private void tempDelay() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    //
                }
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(false);
        th.start();
    }

    @FXML
    void doCompareHash(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Newsletter File");
        String home = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(home));
        fileChooser.getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("Hash file", "*.sum"));

        File sumFile = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (sumFile != null) {
        }
    }
}
