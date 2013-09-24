package com.echo8.jchunker.classifier;

import java.io.File;
import java.util.List;

import com.echo8.jchunker.common.JChunkerException;
import com.echo8.jchunker.featvec.FeatVecGenerator;

/**
 * <p>
 * The interface for the classifier that is used to train the chunker.
 * </p>
 */
public interface Classifier {
    /**
     * <p>
     * Generates feature vectors using the given training data and feature vector generator
     * and then trains the chunker.
     * </p>
     * 
     * <p>
     * The training data here is given as a {@link List} of {@link String} arrays. The last
     * element in each array is assumed to be the label. Zero length arrays in the {@link List}
     * are assumed to be breaks (i.e. between sentences) in the data.
     * </p>
     * 
     * @param data
     *              The training data.
     * @param featVecGenerator
     *              The {@link FeatVecGenerator} used to generate feature vectors.
     */
    void train(List<String[]> data, FeatVecGenerator featVecGenerator);
    
    /**
     * <p>
     * Performs chunking on the given data with the classifier.
     * </p>
     * 
     * <p>
     * The data here is given as a {@link List} of {@link String} arrays. Each array is
     * assumed to <b>not</b> contain the label. Zero length arrays in the {@link List}
     * are assumed to be breaks (i.e. between sentences) in the data.
     * </p>
     * 
     * @param data
     *              The data to chunk.
     * @param featVecGenerator
     *              The {@link FeatVecGenerator} used to generate feature vectors.
     * @return
     *              A {@link List} containing the labels for the given data.
     */
    List<String> chunk(List<String[]> data, FeatVecGenerator featVecGenerator);
    
    /**
     * <p>
     * Loads the classifier's model from a given directory.
     * </p>
     * 
     * @param modelDirectory
     *              The directory where the model files are stored.
     * @param modelPrefix
     *              The prefix of the model files.
     * @throws JChunkerException
     *              if an error occurs when loading the model files.
     */
    void loadModel(File modelDirectory, String modelPrefix) throws JChunkerException;
    
    /**
     * <p>
     * Saves the classifier's model to the given directory.
     * </p>
     * 
     * @param modelDirectory
     *              The directory where the model files are saved.
     * @param modelPrefix
     *              The prefix of the model files.
     * @throws JChunkerException
     *              if an error occurs when saving the model files.
     */
    void saveModel(File modelDirectory, String modelPrefix) throws JChunkerException;
}
