package com.echo8.jchunker.featvec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.echo8.jchunker.common.JChunkerException;

/**
 * <p>
 * An implementation of {@link FeatDictionary} that is based on a 
 * {@link HashMap} (meaning that all of the mappings will be stored
 * in main memory).
 * </p>
 */
public class HashMapFeatDictionary implements FeatDictionary {
    /**
     * The file extension used when saving the feature dictionary.
     */
    private static final String FEAT_DICT_FILE_EXTENSION = ".feat-dict";
    
    private Map<String,Integer> featureToIndexMap = new HashMap<String,Integer>();
    
    public HashMapFeatDictionary() {}
    
    /**
     * <p>
     * Gets the feature vector index for a given feature value.
     * </p>
     * 
     * @param feature
     *              The feature value.
     * @param addIfNoneExists
     *              If set to <code>true</code>, adds the feature value to the
     *              feature dictionary if it does not already exist.
     * @return
     *              The feature vector index of the feature value. If 
     *              <code>addIfNoneExists</code> is set to <code>false</code> 
     *              and the feature value does not exist in the feature 
     *              dictionary, then -1 will be returned.
     */
    public int getFeatIndex(String feature, boolean addIfNoneExists) {
        if (featureToIndexMap.containsKey(feature)) {
            return featureToIndexMap.get(feature);
        }
        else if (addIfNoneExists) {
            int index = featureToIndexMap.size();
            featureToIndexMap.put(feature, index);
            return index;
        }
        
        return -1;
    }
    
    /**
     * <p>
     * Loads the feature dictionary from a given directory.
     * </p>
     * 
     * @param modelDirectory
     *              The directory where the feature dictionary file is stored.
     * @param modelPrefix
     *              The prefix of the feature dictionary file.
     * @throws JChunkerException
     *              if an error occurs when loading the feature dictionary file.
     */
    public void load(File modelDirectory, String modelPrefix) throws JChunkerException {
        // Clear the current mappings before loading in new ones.
        featureToIndexMap.clear();
        
        ObjectInputStream modelInputStream = null;
        
        try {
            modelInputStream = new ObjectInputStream(new FileInputStream(new File(modelDirectory, modelPrefix + FEAT_DICT_FILE_EXTENSION)));
            
            // First get the size of the feature dictionary.
            int dictSize = modelInputStream.readInt();
            for (int i = 0; i < dictSize; i++) {
                // Read in the feature and index and then add the mapping to the feature dictionary.
                featureToIndexMap.put(modelInputStream.readUTF(), modelInputStream.readInt());
            }
        } catch (FileNotFoundException e) {
            throw new JChunkerException("The feature dictionary file was not found.", e);
        } catch (IOException e) {
            throw new JChunkerException("An error occurred when loading the feature dictionary.", e);
        } finally {
            try {
                modelInputStream.close();
            } catch (IOException e) {
                throw new JChunkerException("An error occurred when closing the feature dictionary file.", e);
            }
        }
    }
    
    /**
     * <p>
     * Saves the feature dictionary to a given directory.
     * </p>
     * 
     * @param modelDirectory
     *              The directory where the feature dictionary file is saved.
     * @param modelPrefix
     *              The prefix of the feature dictionary file.
     * @throws JChunkerException
     *              if an error occurs when saving the feature dictionary file.
     */
    public void save(File modelDirectory, String modelPrefix) throws JChunkerException {
        ObjectOutputStream modelOutputStream = null;
        
        try {
            modelOutputStream = new ObjectOutputStream(new FileOutputStream(new File(modelDirectory, modelPrefix + FEAT_DICT_FILE_EXTENSION)));
            
            // First write out the size of the feature dictionary.
            modelOutputStream.writeInt(featureToIndexMap.size());
            for (Iterator<String> iter = featureToIndexMap.keySet().iterator(); iter.hasNext();) {
                String key = iter.next();
                // Write the feature.
                modelOutputStream.writeUTF(key);
                // Write the index of the feature.
                modelOutputStream.writeInt(featureToIndexMap.get(key));
            }
        } catch (FileNotFoundException e) {
            throw new JChunkerException("The feature dictionary file was not found.", e);
        } catch (IOException e) {
            throw new JChunkerException("An error occurred when saving the feature dictionary.", e);
        } finally {
            try {
                modelOutputStream.close();
            } catch (IOException e) {
                throw new JChunkerException("An error occurred when closing the feature dictionary file.", e);
            }
        }
    }
}
