package com.echo8.jchunker.fileformat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.echo8.jchunker.common.JChunkerException;

/**
 * <p>
 * An implementation of {@link DataFileFormat} that will read training data
 * from a data file that has the same format as CoNLL's chunking task.
 * </p>
 * 
 * <p>
 * For more information about the format used for training/testing data in
 * CoNLL's chunking task, please see:
 * <a href="http://www.cnts.ua.ac.be/conll2000/chunking/">
 * http://www.cnts.ua.ac.be/conll2000/chunking/
 * </a>
 * </p>
 */
public class CoNLLDataFileFormat implements DataFileFormat {
    /**
     * The regular expression for matching whitespace.
     */
    private static final String REGEX_WHITESPACE = "\\s+";
    
    /**
     * The default encoding used to read data files when none is specified.
     */
    private static final String DEFAULT_DATA_FILE_ENCODING = "UTF-8";
    
    private String dataFileEncoding;
    
    public CoNLLDataFileFormat() {
        dataFileEncoding = DEFAULT_DATA_FILE_ENCODING;
    }
    
    /**
     * 
     * @param dataFileEncoding
     *              The encoding of the data file.
     */
    public CoNLLDataFileFormat(String dataFileEncoding) {
        this.dataFileEncoding = dataFileEncoding;
    }
    
    /**
     * <p>
     * Reads the training data in CoNLL format from the given file.
     * </p>
     * 
     * @param dataFile
     *              The data file that contains the training data.
     * @return
     *              The training data in a {@link List} of {@link String}
     *              arrays. Each array corresponds with a line in the
     *              data file, while each entry of the array corresponds
     *              with each column in the data file (the last being the label).
     *              A blank line in the data file is added to the {@link List}
     *              as a zero length array.
     * @throws JChunkerException
     *              when an error occurs while reading the training data.
     */
    public List<String[]> readDataFile(File dataFile) throws JChunkerException {
        List<String[]> dataList = new ArrayList<String[]>();
        
        BufferedReader dataFileReader = null;
        try {
            dataFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), dataFileEncoding));
            
            String line;
            while ((line = dataFileReader.readLine()) != null) {
                // Split this line by whitespace to get its columns.
                String dataLine[] = line.split(REGEX_WHITESPACE);
                
                if (dataLine.length == 1) {
                    // If the line was blank add a zero length array to the result list.
                    dataList.add(new String[]{});
                }
                else {
                    dataList.add(dataLine);
                }
            }
        } catch (FileNotFoundException e) {
            throw new JChunkerException("The data file was not found.", e);
        } catch (UnsupportedEncodingException e) {
            throw new JChunkerException("The data file is not in the correct encoding.", e);
        } catch (IOException e) {
            throw new JChunkerException("An error occurred while reading the data file.", e);
        }
        finally {
            if (dataFileReader != null) {
                try {
                    dataFileReader.close();
                } catch (IOException e) {
                    throw new JChunkerException("An error occurred while closing the data file.", e);
                }
            }
        }
        
        return dataList;
    }
}
