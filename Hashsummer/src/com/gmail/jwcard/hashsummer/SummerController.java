package com.gmail.jwcard.hashsummer;

import java.net.URL;
import java.security.Security;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public class SummerController {

    @FXML // fx:id="root"
    private BorderPane root; // Value injected by FXMLLoader

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="hashResultTextArea"
    private TextArea hashResultTextArea; // Value injected by FXMLLoader

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

    @FXML // This method is called by the FXMLLoader when initialization is
	  // complete
    void initialize() {
	assert hashResultTextArea != null : "fx:id=\"hashResultTextArea\" was not injected: check your FXML file 'Summer.fxml'.";
	assert clearButton != null : "fx:id=\"clearButton\" was not injected: check your FXML file 'Summer.fxml'.";
	assert algorithmButton != null : "fx:id=\"algorithmButton\" was not injected: check your FXML file 'Summer.fxml'.";
	assert root != null : "fx:id=\"root\" was not injected: check your FXML file 'Summer.fxml'.";
	assert stopButton != null : "fx:id=\"stopButton\" was not injected: check your FXML file 'Summer.fxml'.";
	assert statusWindow != null : "fx:id=\"statusWindow\" was not injected: check your FXML file 'Summer.fxml'.";
	assert saveButton != null : "fx:id=\"saveButton\" was not injected: check your FXML file 'Summer.fxml'.";
	assert calcHashButton != null : "fx:id=\"calcHashButton\" was not injected: check your FXML file 'Summer.fxml'.";
	assert cmpHashButton != null : "fx:id=\"cmpHashButton\" was not injected: check your FXML file 'Summer.fxml'.";
	
	Set<String> digests = Security.getAlgorithms("MessageDigest");
	String[] options = digests.toArray(new String[digests.size()]);
	Arrays.sort(options);
	algorithmButton.setItems(FXCollections.observableArrayList(options));
	// always pick the last one which will be typically a SHA algorithm
	algorithmButton.setValue(options[options.length - 1]);
    }
}
