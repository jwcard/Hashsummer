package com.gmail.jwcard.hashsummer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author James Card
 *
 */
public class HashValue {
    private final SimpleStringProperty filename;
    private SimpleStringProperty hash;
    private final ReadOnlyLongWrapper bytesProcessed = new ReadOnlyLongWrapper();
    
    /**
     * @param file
     *            Target file to generate hash on
     * @param algorithm
     *            the string representing the Java Security MessageDigest
     *            algorithm name
     */
    public HashValue(File file) {
	this.filename = new SimpleStringProperty(file.getName());
	
    }

    public final long getBytesProcessed() {
	return bytesProcessed.get();
    }

    public final ReadOnlyLongProperty bytesProcessedProperty() {
	return bytesProcessed.getReadOnlyProperty();
    }

    private long curPos;

    public long getTotalBytes() {
        return curPos;
    }

    public String getFilename() {
        return filename.get();
    }

    public String getHash() {
        return hash.get();
    }

    public String computeHash(File file, String algorithm) {
        String hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);

            long len = file.length();
            byte[] buffer = new byte[1024 * 1024];
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
            while (len > 0) {
        	
                long bytesRead = input.read(buffer, 0, buffer.length);
                md.update(buffer, 0, (int)bytesRead);
                curPos = curPos + bytesRead;
                len -= bytesRead;
            }
            input.close();
            byte[] hashValue = md.digest();
            //convert the byte to hex format method 1
            StringBuffer hashCodeBuffer = new StringBuffer();
            for (int i = 0; i < hashValue.length; i++) {
                hashCodeBuffer.append(Integer.toString((hashValue[i] & 0xff) + 0x100, 16).substring(1));
            }
            hash = hashCodeBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        this.hash = new SimpleStringProperty(computeHash(file, algorithm));
        return hash;
        }
}
