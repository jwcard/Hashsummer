package com.gmail.jwcard.hashsummer;

import java.io.File;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author SESA259377
 *
 */
public class HashValue {
    private ObjectProperty<File> file;  //TODO this type preferred
    private final SimpleStringProperty filename;
    private final SimpleStringProperty hash;
    
    /**
     * 
     */
    public HashValue(String filename, String hash) {
        this.filename = new SimpleStringProperty(filename);
        this.hash = new SimpleStringProperty(hash);
    }

    public String getFilename() {
        return filename.get();
    }
    
    public void setFilename(String filename) {
        this.filename.set(filename);
    }
    
    public String getHash() {
        return hash.get();
    }
    
    public void setHash(String hash) {
        this.hash.set(hash);
    }
}
