package com.echo8.jchunker.featvec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.echo8.jchunker.classifier.Classifier;
import com.echo8.jchunker.common.JChunkerException;

/**
 * <p>
 * A basic {@link FeatVecGenerator} implementation that generates a binary feature
 * vector and supports a fixed feature window and label history.
 * </p>
 * 
 * <p>
 * The size of the feature window specifies how many rows preceding and following
 * the current row will be included in its feature vector. For example, if the
 * feature window size is 2 and we are generating the feature vector for the
 * row at position zero in the data below:
 * </p>
 * 
 * <p>
 * <table border="1">
 * <tr>
 * <td>Position</td>
 * <td align="center">Data</td>
 * </tr>
 * <tr>
 * <td align="center">-3</td>
 * <td>Lilly NNP B-NP</td>
 * </tr>
 * <tr>
 * <td align="center">-2</td>
 * <td>shares NNS I-NP</td>
 * </tr>
 * <tr>
 * <td align="center">-1</td>
 * <td>closed VBD B-VP</td>
 * </tr>
 * <tr>
 * <td align="center">0</td>
 * <td>yesterday NN B-NP</td>
 * </tr>
 * <tr>
 * <td align="center">1</td>
 * <td>in IN B-PP</td>
 * </tr>
 * <tr>
 * <td align="center">2</td>
 * <td>composite JJ B-NP</td>
 * </tr>
 * <tr>
 * <td align="center">3</td>
 * <td>trading NN I-NP</td>
 * </tr>
 * </table>
 * </p>
 * 
 * <p>
 * then features the in rows at positions -2, -1, 1, and 2 will also be included in
 * this feature vector.
 * </p>
 * 
 * <p>
 * The size of the label history controls how many previous labels will be included
 * in the current feature vector. During chunking, these label features will be
 * generated dynamically as the new data is being chunked.
 * </p>
 * 
 * <p>
 * If a break is included in the data, then features and labels before that break will
 * not be included any longer. A break is denoted by either leaving a blank line in a
 * data file, or adding a zero length {@link String} array to the data's {@link List}.
 * These are often inserted between sentences so that at the beginning of the sentence
 * irrelevant features from the previous sentence are not included.
 * </p>
 */
public class BasicFeatVecGenerator implements FeatVecGenerator {
    /**
     * The default feature window size.
     */
    private final int DEFAULT_FEAT_WINDOW_SIZE = 2;
    
    /**
     * The default label history size.
     */
    private final int DEFAULT_LABEL_HISTORY_SIZE = 2;
    
    /**
     * The default {@link FeatDictionary} used if none is specified.
     */
    private final FeatDictionary DEFAULT_FEAT_DICTIONARY = new HashMapFeatDictionary();
    
    /**
     * 
     */
    private final String FEAT_DICTIONARY_CLASS_FILE_EXTENSION = ".feat-dic-cls";
    
    private int featWindowSize;
    private int labelHistorySize;
    
    private FeatDictionary featDictionary;
    private List<String> labelHistoryList = new LinkedList<String>();
    
    public BasicFeatVecGenerator() {
        featWindowSize = DEFAULT_FEAT_WINDOW_SIZE;
        labelHistorySize = DEFAULT_LABEL_HISTORY_SIZE;
        featDictionary = DEFAULT_FEAT_DICTIONARY;
    }
    
    /**
     * 
     * @param featWindowSize
     *              The feature window size.
     * @param labelHistorySize
     *              The label history size.
     */
    public BasicFeatVecGenerator(int featWindowSize, int labelHistorySize) {
        this.featWindowSize = featWindowSize;
        this.labelHistorySize = labelHistorySize;
        featDictionary = DEFAULT_FEAT_DICTIONARY;
    }
    
    /**
     * 
     * @param featWindowSize
     *              The feature window size.
     * @param labelHistorySize
     *              The label history size.
     * @param featDictionary
     *              The feature dictionary implemenation to be used.
     */
    public BasicFeatVecGenerator(int featWindowSize, int labelHistorySize, FeatDictionary featDictionary) {
        this.featWindowSize = featWindowSize;
        this.labelHistorySize = labelHistorySize;
        this.featDictionary = featDictionary;
    }
    
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
    public FeatVec generateFeatVec(List<String[]> data, int dataPos) {
        // If this row is a break, return null.
        if (data.get(dataPos).length == 0) {
            return null;
        }
        
        // Generate the feature vector using the specified row
        // and feature window size.
        List<FeatVecEntry> featVec = getFeatVec(data, dataPos, true);
        
        // Add the previous labels as features. Since the data passed into
        // this method already has the labels, we simply extract them out of there.
        for (int i = -1; i >= -labelHistorySize && dataPos+i >= 0; i--) {
            String[] dataLine = data.get(dataPos+i);
            
            if (dataLine.length == 0) {
                break;
            }
            
            int index = getLabelIndex(dataLine[dataLine.length-1], i, true);
            
            if (index >= 0) {
                featVec.add(new FeatVecEntry(index, 1.0));
            }
        }
        
        // Get this feature vector's label.
        String label = data.get(dataPos)[data.get(dataPos).length-1];
        
        return new FeatVec(label, featVec);
    }

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
    public FeatVec generateFeatVecNoLabel(List<String[]> data, int dataPos) {
        // If this row is a break, clear the label history and return null.
        if (data.get(dataPos).length == 0) {
            labelHistoryList.clear();
            return null;
        }
        
        // Generate the feature vector using the specified row
        // and feature window size.
        List<FeatVecEntry> featVec = getFeatVec(data, dataPos, false);
        
        // Add the previous labels as features. Since the data given here does
        // not have labels, we use the label history that is being dynamically
        // updated by the classifier as it is chunking this data.
        int labelPos = -1;
        for(Iterator<String> iter = labelHistoryList.iterator(); iter.hasNext();) {
            String label = iter.next();
            int index = getLabelIndex(label, labelPos, false);
            
            if (index >= 0) {
                featVec.add(new FeatVecEntry(index, 1.0));
            }
            
            labelPos--;
        }
        
        // We do not know the label of this feature vector yet so leave it blank.
        return new FeatVec("", featVec);
    }

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
    @SuppressWarnings("unchecked")
    public void load(File modelDirectory, String modelPrefix) throws JChunkerException {
        ObjectInputStream featDictClassInputStream = null;
        
        try {
            featDictClassInputStream = new ObjectInputStream(new FileInputStream(new File(modelDirectory, modelPrefix + FEAT_DICTIONARY_CLASS_FILE_EXTENSION)));
            
            // Get the feature dictionary's class name.
            String featDictClassName = featDictClassInputStream.readUTF();
            
            // Load the feature dictionary's class.
            Class<? extends FeatDictionary> featDictionaryClass = (Class<? extends FeatDictionary>) Class.forName(featDictClassName);
            featDictionary = featDictionaryClass.newInstance();
            
            // Load the feature dictionary.
            featDictionary.load(modelDirectory, modelPrefix);
        } catch (FileNotFoundException e) {
            throw new JChunkerException("The feature dictionary settings file was not found.", e);
        } catch (IOException e) {
            throw new JChunkerException("An error occurred when loading the feature dictionary settings.", e);
        } catch (ClassNotFoundException e) {
            throw new JChunkerException("The feature dictionary's class could not be found.", e);
        } catch (InstantiationException e) {
            throw new JChunkerException("The feature dictionary's class could not be instantiated.", e);
        } catch (IllegalAccessException e) {
            throw new JChunkerException("The feature dictionary's class was not accessed properly.", e);
        } finally {
            try {
                featDictClassInputStream.close();
            } catch (IOException e) {
                throw new JChunkerException("An error occurred when closing the feature dictionary's settings file.", e);
            }
        }
    }
    
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
    public void save(File modelDirectory, String modelPrefix) throws JChunkerException {
        ObjectOutputStream featDictClassOutputStream = null;
        
        try {
            featDictClassOutputStream = new ObjectOutputStream(new FileOutputStream(new File(modelDirectory, modelPrefix + FEAT_DICTIONARY_CLASS_FILE_EXTENSION)));
            
            // Write out the feature dictionary's class name.
            featDictClassOutputStream.writeUTF(featDictionary.getClass().getName());
            
            // Save the feature dictionary.
            featDictionary.save(modelDirectory, modelPrefix);
        } catch (FileNotFoundException e) {
            throw new JChunkerException("The feature dictionary's settings file was not found.", e);
        } catch (IOException e) {
            throw new JChunkerException("An error occurred when saving the feature dictionary's settings.", e);
        } finally {
            try {
                featDictClassOutputStream.close();
            } catch (IOException e) {
                throw new JChunkerException("An error occurred when closing the feature dictionary's settings file.", e);
            }
        }
    }
    
    /**
     * <p>
     * Adds a label to the label history. This is used by the {@link Classifier}
     * to dynamically update the label history as it is chunking new data.
     * </p>
     * 
     * @param label
     *              The label to add to the history.
     */
    public void addResult(String label) {
        labelHistoryList.add(0, label);
        
        if (labelHistoryList.size() > labelHistorySize) {
            labelHistoryList.remove(labelHistoryList.size()-1);
        }
    }
    
    /**
     * <p>
     * Generates a feature vector for a given row of the data and feature window size.
     * </p>
     * 
     * @param data
     *              The data used to generate the feature vector.
     * @param dataPos
     *              The position of the row in the data to generate the
     *              feature vector for.
     * @param withLabels
     *              Whether or not the data has labels.
     * @return
     *              A {@link List} of the feature vector's entries.
     */
    private List<FeatVecEntry> getFeatVec(List<String[]> data, int dataPos, boolean withLabels) {
        List<FeatVecEntry> featVec = new ArrayList<FeatVecEntry>();
        
        // Get the features for the preceding rows and current row.
        for (int i = 0; i >= -featWindowSize && dataPos+i >= 0; i--) {
            String[] dataLine = data.get(dataPos+i);
            
            // Stop getting features if we hit a break.
            if (dataLine.length == 0) {
                break;
            }
            
            int limit = withLabels ? dataLine.length-1 : dataLine.length;
            for (int j = 0; j < limit; j++) {
                int index = getFeatureIndex(dataLine[j], j, i, withLabels);
                
                if (index >= 0) {
                    featVec.add(new FeatVecEntry(index, 1.0));
                }
            }
        }
        
        // Get features for the following rows.
        for (int i = 1; i <= featWindowSize && dataPos+i < data.size(); i++) {
            String[] dataLine = data.get(dataPos+i);
            
            // Stop getting features if we hit a break.
            if (dataLine.length == 0) {
                break;
            }
            
            int limit = withLabels ? dataLine.length-1 : dataLine.length;
            for (int j = 0; j < limit; j++) {
                int index = getFeatureIndex(dataLine[j], j, i, withLabels);
                
                if (index >= 0) {
                    featVec.add(new FeatVecEntry(index, 1.0));
                }
            }
        }
        
        return featVec;
    }
    
    /**
     * <p>
     * Uses a {@link FeatDictionary} to get the index of a feature given its 
     * value and position in the data relative to the current row.
     * </p>
     * 
     * @param value
     *              The value of the feature.
     * @param col
     *              The column position of the feature.
     * @param row
     *              The row position of the feature relative to the current row.
     * @param addIfNoneExists
     *              If set to <code>true</code>, adds the feature value to the
     *              {@link FeatDictionary} if it does not already exist.
     * @return
     *              The index of the feature. If <code>addIfNoneExists</code> is 
     *              set to <code>false</code> and the feature does not exist in
     *              the {@link FeatDictionary}, then -1 will be returned.
     */
    private int getFeatureIndex(String value, int col, int row, boolean addIfNoneExists) {
        return featDictionary.getFeatIndex(value + "_" + col + ":" + row, addIfNoneExists);
    }
    
    /**
     * <p>
     * Uses a {@link FeatDictionary} to get the index of a label feature given its 
     * value and position in the label history.
     * </p>
     * 
     * @param value
     *              The value of the label feature.
     * @param row
     *              The position in the label history.
     * @param addIfNoneExists
     *              If set to <code>true</code>, adds the label feature value to the
     *              {@link FeatDictionary} if it does not already exist.
     * @return
     *              The index of the label feature. If <code>addIfNoneExists</code> is 
     *              set to <code>false</code> and the label feature does not exist in
     *              the {@link FeatDictionary}, then -1 will be returned.
     */
    private int getLabelIndex(String value, int row, boolean addIfNoneExists) {
        return featDictionary.getFeatIndex(value + "_label" + ":" + row, addIfNoneExists);
    }
}
