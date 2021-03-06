package com.gmail.jwcard.hashsummer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
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

    @FXML // fx:id="progressBar"
    private ProgressBar progressBar; // Value injected by FXMLLoader

    @FXML // fx:id="saveButton"
    private Button saveButton; // Value injected by FXMLLoader

    @FXML // fx:id="calcHashButton"
    private Button calcHashButton; // Value injected by FXMLLoader

    @FXML // fx:id="cmpHashButton"
    private Button cmpHashButton; // Value injected by FXMLLoader

    @FXML // fx:id="hashTable"
    private TableView<HashValue> hashTable; // Value injected by FXMLLoader

    @FXML // fx:id="fileColumn"
    private TableColumn<?, ?> fileColumn; // Value injected by FXMLLoader

    @FXML // fx:id="hashColumn"
    private TableColumn<?, ?> hashColumn; // Value injected by FXMLLoader

    // ================ end of JavaFX declarations

    protected static ObservableList<HashValue> data = FXCollections.<HashValue> observableArrayList();

    // Only one task can run at a time since buttons are disabled once processing starts.
    private static Task<Void> task = null;

    // indicates whether or not an error was ever generated when computing hashes
    private boolean errorExists = false;
    
    private LastOp lastOp = LastOp.HASH;
    
    // Indicates whether any action has been taken (hash or compare)
    private boolean tableDirty = false;
    
//    private static List<String> arguments = null;
    
    final static int FILENAME = 0;
    final static int RESULT = 1;
    final static int NUM_FIELDS = 2;

	@FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert clearButton != null : "fx:id=\"clearButton\" was not injected: check your FXML file 'Summer.fxml'.";
        assert algorithmButton != null : "fx:id=\"algorithmButton\" was not injected: check your FXML file 'Summer.fxml'.";
        assert root != null : "fx:id=\"root\" was not injected: check your FXML file 'Summer.fxml'.";
        assert stopButton != null : "fx:id=\"stopButton\" was not injected: check your FXML file 'Summer.fxml'.";
        assert statusWindow != null : "fx:id=\"statusWindow\" was not injected: check your FXML file 'Summer.fxml'.";
        assert progressBar != null : "fx:id=\"progressBar\" was not injected: check your FXML file 'Summer.fxml'.";
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

        // Not really used at the moment
        FilteredList<HashValue> filteredData = new FilteredList<>(data, n -> true);
        hashTable.setItems(filteredData);

        fileColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
        hashColumn.setCellValueFactory(new PropertyValueFactory<>("hash"));
        
//        if ((arguments != null) && (arguments.size() > 0)) {
//        	File file = new File(arguments.get(0));
//        	if (file.exists() && file.isFile()) {
//        	    showAlert(file.toString());
//        	    processHashFile(file);
//        	}
//        }
    }

//    private void showAlert(String s) {
//        Alert alert = new Alert(AlertType.ERROR);
//        alert.setContentText(s);
//        alert.showAndWait();
//    }

    @FXML
    void doCalculateHash(ActionEvent event) {
        // If the last operation was a compare hash then clear the data as the results column is incompatible with the save format
        if (lastOp == LastOp.CMPHASH) {
            clearInfo();
        }

        lastOp = LastOp.HASH;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Calculate hash on files");
        String home = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(home));

        List<File> files = fileChooser.showOpenMultipleDialog(root.getScene().getWindow());
        if (files != null) {
            handleCompute(files);
            saveButton.setDisable(false); // enable the save button now

            tableDirty = true;
            algorithmButton.setDisable(true);
        }
    }

    /*
     * Spin off a JavaFX Task in the background to handle the computing of hash values for the selected files.
     */
    private void handleCompute(final List<File> files) {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                disableButtons(true);

                String algorithm = algorithmButton.getValue();
                for (File file : files) {
                    // if cancelled was pressed then save button is not valid
                    if (isCancelled()) {
                        saveButton.setDisable(true);
                        break;
                    }
                    if (hashFile(file, algorithm) == null) {
                        errorExists = true;
                    }
                }

                updateProgress(-1, 0); // reset to indeterminate state
                disableButtons(false);

                if (errorExists) {
                    saveButton.setDisable(true);
                }

				doneStatus();

                return null;
            }

            /*
             * returns null if there was an error of any kind otherwise returns the desired message digest for file
             */
            private String hashFile(File file, String algorithm) {
                String hashResult = null;
                if (file.isFile()) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            statusWindow.setText(file.getName());
                        }
                    });

                    HashValue hash = new HashValue(file);
                    hash.bytesProcessedProperty().addListener((obs, oldValue, newValue) -> {
                        updateProgress(newValue.longValue(), hash.getTotalBytes());
                    });

                    hashResult = hash.computeHash(file, algorithm);
                    data.add(hash);
                }
                return hashResult;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());

        Thread th = new Thread(task);
        th.setDaemon(false);
        th.start();
    }

    @FXML
    void doCompareHash(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Compare hash");
        String home = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(home));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Hash file", "*.sum"));

        File sumFile = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (sumFile != null) {
            processHashFile(sumFile);
        }
    }

    private void processHashFile(File sumFile) {
        clearInfo();

        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(sumFile));
            List<String[]> myEntries = reader.readAll();
            handleCompare(sumFile.getParent(), myEntries);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        tableDirty = true;
        algorithmButton.setDisable(true);
        lastOp = LastOp.CMPHASH;
        saveButton.setDisable(true); // TODO figure out why save button doesn't get disabled
    }

    /*
     * Spin off a JavaFX Task in the background to handle the comparison of hash values for the selected files.
     */
    private void handleCompare(final String homeDir, final List<String[]> myEntries) {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
            	final String SEPARATOR = System.getProperty("file.separator");
                disableButtons(true);

                String algorithm = algorithmButton.getValue();
                for (String[] s : myEntries) {
                    if (isCancelled()) {
                        break;
                    }

                    String filename = s[FILENAME];
                    String origHash = s[RESULT];

                    File file = new File(homeDir + SEPARATOR + filename);
                    compareHash(file, algorithm, origHash);
                }

                updateProgress(-1, 0); // reset to indeterminate state
                disableButtons(false);
                doneStatus();

                return null;
            }

            private boolean compareHash(final File file, final String algorithm, final String origHash) {
                boolean hashResult = false;
                if (file.isFile()) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            statusWindow.setText(file.getName());
                        }
                    });

                    HashValue hash = new HashValue(file);
                    hash.bytesProcessedProperty().addListener((obs, oldValue, newValue) -> {
                        updateProgress(newValue.longValue(), hash.getTotalBytes());
                    });

                    hashResult = hash.compareHash(file, algorithm, origHash);
                    data.add(hash);
                }
                return hashResult;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());

        Thread th = new Thread(task);
        th.setDaemon(false);
        th.start();
    }

    @FXML
    void doSave(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save hash values");
        String home = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(home));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Hash file", "*.sum"));

        File sumFile = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (sumFile != null) {
            try {
                CSVWriter writer = new CSVWriter(new FileWriter(sumFile), ',');
                String[] entries = new String[NUM_FIELDS];
                int rowCnt = fileColumn.getTableView().getItems().size();
                for (int i = 0; i < rowCnt; i++) {
                    entries[FILENAME] = (String) fileColumn.getCellData(i);
                    entries[RESULT] = (String) hashColumn.getCellData(i);
                    writer.writeNext(entries);
                }
                writer.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @FXML
    void doStop(ActionEvent event) {
        task.cancel();
    }

    @FXML
    void doClear(ActionEvent event) {
    	clearInfo(); // start anew...
        saveButton.setDisable(true); // disable the save button now
        errorExists = false; // clear the error indicator
        
        tableDirty = true;
        algorithmButton.setDisable(false);
    }

    /*
     * @param value state of general buttons
     */
    private void disableButtons(boolean value) {
        clearButton.setDisable(value);
        algorithmButton.setDisable(value | tableDirty); // don't reenable if there is data in the table
        calcHashButton.setDisable(value);
        cmpHashButton.setDisable(value);
        saveButton.setDisable(value);
        stopButton.setDisable(!value);
    }

	/**
	 * Allows main to pass the arguments to this class
	 * 
	 * @param args
	 *            List<String> copy of the command line arguments
	 */
//	public static void setArgs(final List<String> args) {
//		arguments = args;
//	}

	/**
	 * Allows other objects to query whether they should abort their processing
	 * 
	 * @return true if a cancel request has been made otherwise false
	 */
    static public boolean isCancelled() {
        return task.isCancelled();
    }

	/**
	 * Clear out the generated data from the display
	 */
	private void clearInfo() {
		data.clear();
        statusWindow.setText("");
	}

	/**
	 * 
	 */
	private void doneStatus() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				statusWindow.setText("*** Done ***");
			}
		});
	}
}
