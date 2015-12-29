package com.gmail.jwcard.hashsummer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
     *            the string representing the Java Security MessageDigest algorithm name
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
    private long fileLen;

    public long getTotalBytes() {
        return fileLen;
    }

    public String getFilename() {
        return filename.get();
    }

    public String getHash() {
        return hash.get();
    }

    /**
     * Computes the hash based on the desired algorithm. algorithm is assumed to be supported by the current JCA.
     * 
     * @param file
     *            compute the message digest for this file
     * @param algorithm
     *            compute the message digest using this algorithm
     * @return the hash string or null if some error occured
     */
    public String computeHash(File file, String algorithm) {
        String hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);

            fileLen = file.length();
            long len = fileLen;
            byte[] buffer = new byte[1024 * 1024];
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
            while (len > 0) {
                long bytesRead = input.read(buffer, 0, buffer.length);
                md.update(buffer, 0, (int) bytesRead);
                curPos = curPos + bytesRead;
                len -= bytesRead;
                bytesProcessed.set(curPos);
                if (SummerController.isCancelled()) {
                    break;
                }
            }
            input.close();

            if (!SummerController.isCancelled()) {
                byte[] hashValue = md.digest();
                // convert the byte to hex format method 1
                StringBuffer hashCodeBuffer = new StringBuffer();
                for (int i = 0; i < hashValue.length; i++) {
                    hashCodeBuffer.append(Integer.toString((hashValue[i] & 0xff) + 0x100, 16).substring(1));
                }
                hash = hashCodeBuffer.toString();
                this.hash = new SimpleStringProperty(hash);
            } else {
                this.hash = new SimpleStringProperty("<<Cancelled>>");
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            this.hash = new SimpleStringProperty(e.getMessage());
        }

        return hash;
    }

    /**
     * Compares the original hash against the hash based on the desired algorithm. algorithm is assumed to be supported by the
     * current JCA.
     * 
     * @param file
     *            compute the message digest for this file
     * @param algorithm
     *            compute the message digest using this algorithm
     * @param origHash
     *            original hash value to compare against
     * @return the hash string or null if some error occured
     */
    public boolean compareHash(File file, String algorithm, String origHash) {
        String hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);

            fileLen = file.length();
            long len = fileLen;
            byte[] buffer = new byte[1024 * 1024];
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
            while (len > 0) {
                long bytesRead = input.read(buffer, 0, buffer.length);
                md.update(buffer, 0, (int) bytesRead);
                curPos = curPos + bytesRead;
                len -= bytesRead;
                bytesProcessed.set(curPos);
                if (SummerController.isCancelled()) {
                    break;
                }
            }
            input.close();

            if (!SummerController.isCancelled()) {
                byte[] hashValue = md.digest();
                // convert the byte to hex format method 1
                StringBuffer hashCodeBuffer = new StringBuffer();
                for (int i = 0; i < hashValue.length; i++) {
                    hashCodeBuffer.append(Integer.toString((hashValue[i] & 0xff) + 0x100, 16).substring(1));
                }
                hash = hashCodeBuffer.toString();
            } else {
                this.hash = new SimpleStringProperty("<<Cancelled>>");
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            this.hash = new SimpleStringProperty(e.getMessage());
        }

        boolean isMatch = false;
        if (hash != null) {
            isMatch = hash.toLowerCase().equals(origHash.toLowerCase());
            if (isMatch) {
                this.hash = new SimpleStringProperty("Matches");
            } else {
                this.hash = new SimpleStringProperty("Does not match");
            }
        }
        return isMatch;
    }
}
