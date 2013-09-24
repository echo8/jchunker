package com.echo8.jchunker.classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

import com.echo8.jchunker.common.JChunkerException;
import com.echo8.jchunker.featvec.FeatVec;
import com.echo8.jchunker.featvec.FeatVecEntry;
import com.echo8.jchunker.featvec.FeatVecGenerator;

/**
 * <p>
 * An implementation of {@link Classifier} based on the LIBSVM library.
 * </p>
 * 
 * <p>
 * Please see <a href="http://www.csie.ntu.edu.tw/~cjlin/libsvm/">LIBSVM's homepage</a> 
 * for more information about its features and usage.
 * </p>
 */
public class LibSVMClassifier implements Classifier {
    /**
     * The file extension used when saving the classifier's model.
     */
    private static final String MODEL_FILE_EXTENSION = ".libsvm-model";
    
    /**
     * The file extension used when saving the label map.
     */
    private static final String LABEL_MAP_FILE_EXTENSION = ".label-map";
    
    private svm_parameter svmParameter;
    private svm_model svmModel;
    
    private Map<String,Double> labelToDoubleMap = new HashMap<String,Double>();
    private Map<Double,String> doubleToLabelMap = new HashMap<Double,String>();
    
    public LibSVMClassifier() {
        svmParameter = getDefaultSvmParameter();
    }
    
    /**
     * 
     * @param svmParameter
     *              LIBSVM's settings.
     */
    public LibSVMClassifier(svm_parameter svmParameter) {
        this.svmParameter = svmParameter;
    }
    
    /**
     * <p>
     * The default settings for LIBSVM. These are the same as the default
     * settings for LIBSVM's command line tool.
     * </p>
     * 
     * @return
     *              An instance of {@link svm_parameter} that contains the 
     *              default settings for LIBSVM.
     */
    private static svm_parameter getDefaultSvmParameter() {
        svm_parameter svmParameter = new svm_parameter();
        svmParameter.svm_type = svm_parameter.C_SVC;
        svmParameter.kernel_type = svm_parameter.LINEAR;
        svmParameter.C = 1.0;
        svmParameter.cache_size = 100.0;
        svmParameter.coef0 = 0.0;
        svmParameter.degree = 3;
        svmParameter.eps = 0.001;
        svmParameter.nu = 0.5;
        svmParameter.p = 0.1;
        svmParameter.probability = 0;
        svmParameter.shrinking = 1;
        
        return svmParameter;
    }
    
    /**
     * <p>
     * Performs chunking on the given data with LIBSVM.
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
    public List<String> chunk(List<String[]> data, FeatVecGenerator featVecGenerator) {
        // The list of labels to be returned.
        List<String> labelList = new ArrayList<String>();
        
        for (int i = 0; i < data.size(); i++) {
            // Generate the feature vector.
            FeatVec featVec = featVecGenerator.generateFeatVecNoLabel(data, i);
            
            // If a feature vector is returned then predict a label for it and add it to the label list.
            if (featVec != null) {
                String resultLabel = convertToStringLabel(svm.svm_predict(svmModel, convertToSvmNodeArray(featVec.getFeatVec())));
                labelList.add(resultLabel);
                featVecGenerator.addResult(resultLabel);
            }
            else {
                // If no feature vector was returned that means it was a break in the data.
                // In this case simply add an empty string to the label list.
                labelList.add("");
            }
        }
        
        // Finally, clear the label history in the feature vector generator by passing in a zero length array (i.e. a break).
        featVecGenerator.generateFeatVecNoLabel(Arrays.asList(new String[][]{new String[]{}}), 0);
        
        return labelList;
    }
    
    /**
     * <p>
     * Loads LIBSVM's model and the label maps from a given directory.
     * </p>
     * 
     * @param modelDirectory
     *              The directory where the model files are stored.
     * @param modelPrefix
     *              The prefix of the model files.
     * @throws JChunkerException
     *              if an error occurs when loading the model files.
     */
    public void loadModel(File modelDirectory, String modelPrefix) throws JChunkerException {
        // Load LIBSVM's model.
        try {
            svmModel = svm.svm_load_model(new File(modelDirectory, modelPrefix + MODEL_FILE_EXTENSION).getAbsolutePath());
            
            // Load the model's parameters.
            svmParameter = svmModel.param;
        } catch (IOException e) {
            throw new JChunkerException("Could not load LIBSVM's model.", e);
        }
        
        // Load the label maps.
        loadLabelMap(modelDirectory, modelPrefix);
    }
    
    /**
     * <p>
     * Saves LIBSVM's model and the label maps to the given directory.
     * </p>
     * 
     * @param modelDirectory
     *              The directory where the model files are saved.
     * @param modelPrefix
     *              The prefix of the model files.
     * @throws JChunkerException
     *              if an error occurs when saving the model files.
     */
    public void saveModel(File modelDirectory, String modelPrefix) throws JChunkerException {
        // Save LIBSVM's model.
        try {
            svm.svm_save_model(new File(modelDirectory, modelPrefix + MODEL_FILE_EXTENSION).getAbsolutePath(), svmModel);
        } catch (IOException e) {
            throw new JChunkerException("Could not save LIBSVM's model.", e);
        }
        
        // Save the label maps.
        saveLabelMap(modelDirectory, modelPrefix);
    }
    
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
    public void train(List<String[]> data, FeatVecGenerator featVecGenerator) {
        // List of labels given in the training data.
        List<Double> labelList = new ArrayList<Double>();
        // List of feature vectors in LIBSVM's format.
        List<svm_node[]> svmNodeList = new ArrayList<svm_node[]>();
        
        for (int i = 0; i < data.size(); i++) {
            // Generate the feature vector.
            FeatVec featVec = featVecGenerator.generateFeatVec(data, i);
            
            // If a feature vector was returned add its label and vector to the 
            // two corresponding lists. If nothing was returned that means it was
            // a break so we do nothing.
            if (featVec != null) {
                labelList.add(convertToDoubleLabel(featVec.getLabel()));
                svmNodeList.add(convertToSvmNodeArray(featVec.getFeatVec()));
            }
        }
        
        // Convert all of the training data into LIBSVM's format.
        svm_problem svmProblem = new svm_problem();
        svmProblem.l = svmNodeList.size();
        svmProblem.x = convertTo2dSvmNodeArray(svmNodeList);
        svmProblem.y = convertToPrimitiveDoubleArray(labelList);
        
        // Train the LIBSVM model.
        svmModel = svm.svm_train(svmProblem, svmParameter);
    }
    
    /**
     * <p>
     * Converts a feature vector from JChunker's internal format to
     * LIBSVM's format.
     * </p>
     * 
     * @param featVec
     *              A feature vector in JChunker's internal format.
     * @return
     *              A feature vector in LIBSVM's format.
     */
    private svm_node[] convertToSvmNodeArray(List<FeatVecEntry> featVec) {
        svm_node[] svmNodes = new svm_node[featVec.size()];
        
        for (int i = 0; i < featVec.size(); i++) {
            FeatVecEntry featVecEntry = featVec.get(i);
            svm_node svmNode = new svm_node();
            
            svmNode.index = featVecEntry.getIndex();
            svmNode.value = featVecEntry.getValue();
            
            svmNodes[i] = svmNode;
        }
        
        return svmNodes;
    }
    
    /**
     * <p>
     * Converts a {@link List} of LIBSVM feature vectors to a two-dimensional array
     * of LIBSVM feature vector entries.
     * </p>
     * 
     * @param svmNodeList
     *              A {@link List} of LIBSVM feature vectors.
     * @return
     *              A two-dimensional array of LIBSVM feature vector entries.
     */
    private svm_node[][] convertTo2dSvmNodeArray(List<svm_node[]> svmNodeList) {
        svm_node[][] x = new svm_node[svmNodeList.size()][];
        
        for (int i = 0; i < svmNodeList.size(); ++i) {
            x[i] = svmNodeList.get(i);
        }
        
        return x;
    }
    
    /**
     * <p>
     * Converts a {@link List} of {@link Double} to an array of <code>double</code>.
     * Used when converting the list of labels into LIBSVM's format.
     * </p>
     * 
     * @param doubleList
     *              A {@link List} of {@link Double}.
     * @return
     *              An array of <code>double</code>.
     */
    private double[] convertToPrimitiveDoubleArray(List<Double> doubleList) {
        double[] doubles = new double[doubleList.size()];
        
        for (int i = 0; i < doubleList.size(); i++) {
            doubles[i] = doubleList.get(i).doubleValue();
        }
        
        return doubles;
    }
    
    /**
     * <p>
     * Converts a string label to a <code>double</code> value.
     * </p>
     * 
     * @param label
     *              A string label.
     * @return
     *              A <code>double</code> value.
     */
    private double convertToDoubleLabel(String label) {
        if (labelToDoubleMap.containsKey(label)) {
            return labelToDoubleMap.get(label);
        }
        else {
            Double doubleLabel = (double)labelToDoubleMap.size() + 1.0;
            labelToDoubleMap.put(label, doubleLabel);
            doubleToLabelMap.put(doubleLabel, label);
            
            return doubleLabel;
        }
    }
    
    /**
     * <p>
     * Converts a <code>double</code> value back into a string label.
     * </p>
     * 
     * @param doubleLabel
     *              A <code>double</code> value.
     * @return
     *              A string label.
     */
    private String convertToStringLabel(double doubleLabel) {
        return doubleToLabelMap.get(doubleLabel);
    }
    
    /**
     * <p>
     * Loads the label maps from a given directory.
     * </p>
     * 
     * @param modelDirectory
     *              The directory where the label maps are stored.
     * @param modelPrefix
     *              The prefix of the label map file.
     * @throws JChunkerException
     *              if an error occurs when loading the label map file.
     */
    private void loadLabelMap(File modelDirectory, String modelPrefix) throws JChunkerException {
        // Clear the current label maps.
        labelToDoubleMap.clear();
        doubleToLabelMap.clear();
        
        ObjectInputStream labelMapInputStream = null;
        
        try {
            labelMapInputStream = new ObjectInputStream(new FileInputStream(new File(modelDirectory, modelPrefix + LABEL_MAP_FILE_EXTENSION)));
            
            // First read the size of the maps.
            int labelMapSize = labelMapInputStream.readInt();
            for (int i = 0; i < labelMapSize; i++) {
                // Read the string label.
                String label = labelMapInputStream.readUTF();
                // Read the string label's double value.
                Double doubleLabel = labelMapInputStream.readDouble();
                
                // Insert that label into the label maps.
                labelToDoubleMap.put(label, doubleLabel);
                doubleToLabelMap.put(doubleLabel, label);
            }
        } catch (FileNotFoundException e) {
            throw new JChunkerException("The label map file was not found.", e);
        } catch (IOException e) {
            throw new JChunkerException("An error occurred when reading the label map file.", e);
        } finally {
            try {
                labelMapInputStream.close();
            } catch (IOException e) {
                throw new JChunkerException("An error occurred when trying to close the label map file.", e);
            }
        }
    }
    
    /**
     * <p>
     * Saves the label maps to a given directory.
     * </p>
     * 
     * @param modelDirectory
     *              The directory where the label maps are saved.
     * @param modelPrefix
     *              The prefix of the label map file.
     * @throws JChunkerException
     *              if an error occurs when saving the label map file.
     */
    private void saveLabelMap(File modelDirectory, String modelPrefix) throws JChunkerException {
        ObjectOutputStream labelMapOutputStream = null;
        
        try {
            labelMapOutputStream = new ObjectOutputStream(new FileOutputStream(new File(modelDirectory, modelPrefix + LABEL_MAP_FILE_EXTENSION)));
            
            // First write the size of the maps.
            labelMapOutputStream.writeInt(labelToDoubleMap.size());
            for (Iterator<String> iter = labelToDoubleMap.keySet().iterator(); iter.hasNext();) {
                String key = iter.next();
                // Write the string label.
                labelMapOutputStream.writeUTF(key);
                // Write the string label's double value.
                labelMapOutputStream.writeDouble(labelToDoubleMap.get(key));
            }
        } catch (FileNotFoundException e) {
            throw new JChunkerException("The label map file was not found.", e);
        } catch (IOException e) {
            throw new JChunkerException("An error occurred when writing to the label map file.", e);
        } finally {
            try {
                labelMapOutputStream.close();
            } catch (IOException e) {
                throw new JChunkerException("An error occurred when trying to close the label map file.", e);
            }
        }
    }
}
