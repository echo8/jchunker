package com.echo8.jchunker.featvec;

import java.io.File;
import java.util.List;

import com.echo8.jchunker.classifier.Classifier;
import com.echo8.jchunker.common.JChunkerException;

/**
 * <p>
 * The interface for the feature generator used by the {@link Classifier}.
 * </p>
 */
public interface FeatVecGenerator {
    /**
     * <p>
     * Adds a label to the label history. This is used by the {@link Classifier}
     * to dynamically update the label history as it is chunking new data.
     * </p>
     * 
     * @param label
     *              The label to add to the history.
     */
    void addResult(String label);
    
    /**
     * <p>
     * Generates the feature vector for a specified row in the data.
     * </p>
     * 
     * <p>
     * The data here is given as a {@link List} of {@link String} arrays. The last
     * element in each array is assumed to be the label. Zero length arrays in the {@link List}
     * are assumed to be breaks (i.e. between sentences) in the data.
     * </p>
     * 
     * @param data
     *              The data used to generate the feature vector.
     * @param dataPos
     *              The position of the row in the data to generate the
     *              feature vector for.
     * @return
     *              An instance of {@link FeatVec} that includes both
     *              the entries and label of the feature vector.
     *              However, if the specified row is a break (i.e. a zero length
     *              {@link String} array, then <code>null</code> is returned.
     */
    FeatVec generateFeatVec(List<String[]> data, int dataPos);
    
    /**
     * <p>
     * Generates the feature vector for a specified row in the data.
     * </p>
     * 
     * <p>
     * The data here is given as a {@link List} of {@link String} arrays. Each array is
     * assumed to <b>not</b> contain the label. Zero length arrays in the {@link List}
     * are assumed to be breaks (i.e. between sentences) in the data.
     * </p>
     * 
     * @param data
     *              The data used to generate the feature vector.
     * @param dataPos
     *              The position of the row in the data to generate the
     *              feature vector for.
     * @return
     *              An instance of {@link FeatVec} that includes both
     *              the entries and label of the feature vector.
     *              However, if the specified row is a break (i.e. a zero length
     *              {@link String} array, then <code>null</code> is returned.
     */
    FeatVec generateFeatVecNoLabel(List<String[]> data, int dataPos);
    
    /**
     * <p>
     * Loads the feature vector generator from a specified directory.
     * </p>
     * 
     * @param modelDirectory
     *              The directory where the feature vector generator files are stored.
     * @param modelPrefix
     *              The prefix of the feature vector generator files.
     * @throws JChunkerException
     *              if an error occurs when loading the feature vector generator files.
     */
    void load(File modelDirectory, String modelPrefix) throws JChunkerException;
    
    /**
     * <p>
     * Saves the feature vector generator to a specified directory.
     * </p>
     * 
     * @param modelDirectory
     *              The directory where the feature vector generator files are saved.
     * @param modelPrefix
     *              The prefix of the feature vector generator files.
     * @throws JChunkerException
     *              if an error occurs when saving the feature vector generator files.
     */
    void save(File modelDirectory, String modelPrefix) throws JChunkerException;
}
