package com.echo8.jchunker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.echo8.jchunker.classifier.Classifier;
import com.echo8.jchunker.classifier.LibSVMClassifier;
import com.echo8.jchunker.common.JChunkerException;
import com.echo8.jchunker.featvec.BasicFeatVecGenerator;
import com.echo8.jchunker.featvec.FeatVecGenerator;
import com.echo8.jchunker.fileformat.CoNLLDataFileFormat;
import com.echo8.jchunker.fileformat.DataFileFormat;

/**
 * <p>
 * The main JChunker class. Use this class to train the chunker and
 * to chunk new text.
 * </p>
 */
public class JChunker {
    /**
     * The default {@link Classifier} used if none is specified.
     */
    private static final Classifier DEFAULT_CLASSIFIER = new LibSVMClassifier();
    
    /**
     * The default {@link FeatVecGenerator} used if none is specified.
     */
    private static final FeatVecGenerator DEFAULT_FEAT_VEC_GENERATOR = new BasicFeatVecGenerator();
    
    /**
     * The {@link DataFileFormat} that the data file is assumed to be in if none is specified.
     */
    private static final DataFileFormat DEFAULT_DATA_FILE_FORMAT = new CoNLLDataFileFormat();
    
    /**
     * The file extension used when saving the classifier's settings.
     */
    private static final String CLASSIFIER_FILE_EXTENSION = ".classifier";
    
    /**
     * The file extension used when saving the feature vector generator's settings.
     */
    private static final String FEAT_VEC_GENERATOR_FILE_EXTENSION = ".fvgen";
    
    private Classifier classifier;
    private FeatVecGenerator featVecGenerator;
    
    public JChunker() {
        classifier = DEFAULT_CLASSIFIER;
        featVecGenerator = DEFAULT_FEAT_VEC_GENERATOR;
    }
    
    /**
     * 
     * @param classifier
     *              The classifier that will be used to train the chunker.
     */
    public JChunker(Classifier classifier) {
        this.classifier = classifier;
        this.featVecGenerator = DEFAULT_FEAT_VEC_GENERATOR;
    }
    
    /**
     * 
     * @param featVecGenerator
     *              The feature vector generator that will be used.
     */
    public JChunker(FeatVecGenerator featVecGenerator) {
        this.classifier = DEFAULT_CLASSIFIER;
        this.featVecGenerator = featVecGenerator;
    }
    
    /**
     * 
     * @param classifier
     *              The classifier that will be used to train the chunker.
     * @param featVecGenerator
     *              The feature vector generator that will be used.
     */
    public JChunker(Classifier classifier, FeatVecGenerator featVecGenerator) {
        this.classifier = classifier;
        this.featVecGenerator = featVecGenerator;
    }
    
    /**
     * <p>
     * Train the chunker using the training data in the given data file.
     * </p>
     * 
     * @param dataFile
     *              The file containing the training data.
     * @throws JChunkerException
     *              if an error occurs while reading the data file.
     */
    public void train(File dataFile) throws JChunkerException {
        train(dataFile, DEFAULT_DATA_FILE_FORMAT);
    }
    
    /**
     * <p>
     * Reads the training data from the given data file using the given file format, and 
     * then trains the chunker. 
     * </p>
     * 
     * @param dataFile
     *              The file containing the training data.
     * @param dataFileFormat
     *              The file format used to read the training data from the data file.
     * @throws JChunkerException
     *              if an error occurs while reading the data file.
     */
    public void train(File dataFile, DataFileFormat dataFileFormat) throws JChunkerException {
        List<String[]> dataList = dataFileFormat.readDataFile(dataFile);
        train(dataList);
    }
    
    /**
     * <p>
     * Trains the chunker using the given training data.
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
     */
    public void train(List<String[]> data) {
        classifier.train(data, featVecGenerator);
    }
    
    /**
     * <p>
     * Performs chunking on the given data.
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
     * @return
     *              A {@link List} containing the labels for the given data.
     */
    public List<String> chunk(List<String[]> data) {
        return classifier.chunk(data, featVecGenerator);
    }
    
    /**
     * <p>
     * Loads a previously saved JChunker model.
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
        loadClassifier(modelDirectory, modelPrefix);
        loadFeatVecGenerator(modelDirectory, modelPrefix);
    }
    
    /**
     * <p>
     * Saves a JChunker model to the given directory. All of the model's files are named
     * using the given file prefix.
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
        saveClassifier(modelDirectory, modelPrefix);
        saveFeatVecGenerator(modelDirectory, modelPrefix);
    }
    
    /**
     * <p>
     * Loads a previously saved classifier.
     * </p>
     * 
     * @param modelDirectory
     *              The directory where the classifier's files are stored.
     * @param modelPrefix
     *              The prefix of the classifier's files.
     * @throws JChunkerException
     *              if an error occurs when loading the classifier's files.
     */
    @SuppressWarnings("unchecked")
    private void loadClassifier(File modelDirectory, String modelPrefix) throws JChunkerException {
        ObjectInputStream classifierInputStream = null;
        
        try {
            classifierInputStream = new ObjectInputStream(new FileInputStream(new File(modelDirectory, modelPrefix + CLASSIFIER_FILE_EXTENSION)));
            
            // Get the classifier's class name.
            String classifierClassName = classifierInputStream.readUTF();
            
            // Load the classifier's class.
            Class<? extends Classifier> classifierClass = (Class<? extends Classifier>) Class.forName(classifierClassName);
            classifier = classifierClass.newInstance();
            
            // Load the classifier's model.
            classifier.loadModel(modelDirectory, modelPrefix);
        } catch (FileNotFoundException e) {
            throw new JChunkerException("The classifier settings file was not found.", e);
        } catch (IOException e) {
            throw new JChunkerException("An error occurred when loading the classifier's settings.", e);
        } catch (ClassNotFoundException e) {
            throw new JChunkerException("The classifier's class could not be found.", e);
        } catch (InstantiationException e) {
            throw new JChunkerException("The classifier's class could not be instantiated.", e);
        } catch (IllegalAccessException e) {
            throw new JChunkerException("The classifier's class was not accessed properly.", e);
        } finally {
            try {
                classifierInputStream.close();
            } catch (IOException e) {
                throw new JChunkerException("An error occurred when closing the classifier's settings file.", e);
            }
        }
    }
    
    /**
     * <p>
     * Loads a previously saved feature vector generator.
     * </p>
     * 
     * @param modelDirectory
     *              The directory where the feature vector generator's files are stored.
     * @param modelPrefix
     *              The prefix of the feature vector generator's files.
     * @throws JChunkerException
     *              if an error occurs when loading the feature vector generator's files.
     */
    @SuppressWarnings("unchecked")
    private void loadFeatVecGenerator(File modelDirectory, String modelPrefix) throws JChunkerException {
        ObjectInputStream fvGeneratorInputStream = null;
        
        try {
            fvGeneratorInputStream = new ObjectInputStream(new FileInputStream(new File(modelDirectory, modelPrefix + FEAT_VEC_GENERATOR_FILE_EXTENSION)));
            
            // Get the feature vector generator's class name.
            String fvGeneratorClassName = fvGeneratorInputStream.readUTF();
            
            // Load the feature vector generator's class.
            Class<? extends FeatVecGenerator> fvGeneratorClass = (Class<? extends FeatVecGenerator>) Class.forName(fvGeneratorClassName);
            featVecGenerator = fvGeneratorClass.newInstance();
            
            // Load the feature vector generator.
            featVecGenerator.load(modelDirectory, modelPrefix);
        } catch (FileNotFoundException e) {
            throw new JChunkerException("The feature vector generator settings file was not found.", e);
        } catch (IOException e) {
            throw new JChunkerException("An error occurred when loading the feature vector generator settings.", e);
        } catch (ClassNotFoundException e) {
            throw new JChunkerException("The feature vector generator's class could not be found.", e);
        } catch (InstantiationException e) {
            throw new JChunkerException("The feature vector generator's class could not be instantiated.", e);
        } catch (IllegalAccessException e) {
            throw new JChunkerException("The feature vector generator's class was not accessed properly.", e);
        } finally {
            try {
                fvGeneratorInputStream.close();
            } catch (IOException e) {
                throw new JChunkerException("An error occurred when closing the feature vector generator's settings file.", e);
            }
        }
    }
    
    /**
     * <p>
     * Saves a classifer to a given directory.
     * </p>
     * 
     * @param modelDirectory
     *              The directory where the classifer's files are saved.
     * @param modelPrefix
     *              The prefix of the classifier's files.
     * @throws JChunkerException
     *              if an error occurs when saving the classifier's files.
     */
    private void saveClassifier(File modelDirectory, String modelPrefix) throws JChunkerException {
        ObjectOutputStream classifierOutputStream = null;
        
        try {
            classifierOutputStream = new ObjectOutputStream(new FileOutputStream(new File(modelDirectory, modelPrefix + CLASSIFIER_FILE_EXTENSION)));
            
            // Write out the classifier's class name.
            classifierOutputStream.writeUTF(classifier.getClass().getName());
            
            // Save the classifier's model.
            classifier.saveModel(modelDirectory, modelPrefix);
        } catch (FileNotFoundException e) {
            throw new JChunkerException("The classifer's settings file was not found.", e);
        } catch (IOException e) {
            throw new JChunkerException("An error occurred when saving the classifier's settings.", e);
        } finally {
            try {
                classifierOutputStream.close();
            } catch (IOException e) {
                throw new JChunkerException("An error occurred when closing the classifier's settings file.", e);
            }
        }
    }
    
    /**
     * <p>
     * Saves a feature vector generator to a given directory.
     * </p>
     * 
     * @param modelDirectory
     *              The directory where the feature vector generator's files are saved.
     * @param modelPrefix
     *              The prefix of the feature vector generator's files.
     * @throws JChunkerException
     *              if an error occurs when saving the feature vector generator's files.
     */
    private void saveFeatVecGenerator(File modelDirectory, String modelPrefix) throws JChunkerException {
        ObjectOutputStream fvGeneratorOutputStream = null;
        
        try {
            fvGeneratorOutputStream = new ObjectOutputStream(new FileOutputStream(new File(modelDirectory, modelPrefix + FEAT_VEC_GENERATOR_FILE_EXTENSION)));
            
            // Write out the feature vector generator's class name.
            fvGeneratorOutputStream.writeUTF(featVecGenerator.getClass().getName());
            
            // Save the feature vector generator.
            featVecGenerator.save(modelDirectory, modelPrefix);
        } catch (FileNotFoundException e) {
            throw new JChunkerException("The feature vector generator's settings file was not found.", e);
        } catch (IOException e) {
            throw new JChunkerException("An error occurred when saving the feature vector generator's settings.", e);
        } finally {
            try {
                fvGeneratorOutputStream.close();
            } catch (IOException e) {
                throw new JChunkerException("An error occurred when closing the feature vector generator's settings file.", e);
            }
        }
    }
}
