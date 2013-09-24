package com.echo8.jchunker.fileformat;

import java.io.File;
import java.util.List;

import com.echo8.jchunker.common.JChunkerException;

/**
 * <p>
 * An interface for the format of the data file that contains the training data.
 * </p>
 */
public interface DataFileFormat {
    /**
     * <p>
     * Reads the training data from the given file.
     * </p>
     * 
     * @param dataFile
     *              The data file that contains the training data.
     * @return
     *              The training data in a {@link List} of {@link String}
     *              arrays.
     * @throws JChunkerException
     *              when an error occurs while reading the training data.
     */
    List<String[]> readDataFile(File dataFile) throws JChunkerException;
}
