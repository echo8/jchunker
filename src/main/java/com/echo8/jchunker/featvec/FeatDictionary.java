package com.echo8.jchunker.featvec;

import java.io.File;

import com.echo8.jchunker.common.JChunkerException;

/**
 * <p>
 * The interface for the feature dictionary which maps feature values to
 * feature vector indexes.
 * </p>
 */
public interface FeatDictionary {
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
    int getFeatIndex(String feature, boolean addIfNoneExists);
    
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
    void load(File modelDirectory, String modelPrefix) throws JChunkerException;
    
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
    void save(File modelDirectory, String modelPrefix) throws JChunkerException;
}
