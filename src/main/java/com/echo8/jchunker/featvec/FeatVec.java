package com.echo8.jchunker.featvec;

import java.util.List;

/**
 * <p>
 * A feature vector in JChunker's internal format. Contains the
 * feature vector's entries as well as its label.
 * </p>
 */
public class FeatVec {
    String label;
    List<FeatVecEntry> featVec;
    
    /**
     * 
     * @param label
     *              The feature vector's label.
     * @param featVec
     *              A {@link List} of the feature vector's entries.
     */
    public FeatVec(String label, List<FeatVecEntry> featVec) {
        this.label = label;
        this.featVec = featVec;
    }
    
    /**
     * 
     * @return
     *              The feature vector's label.
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * 
     * @return
     *              A {@link List} of the feature vector's entries.
     */
    public List<FeatVecEntry> getFeatVec() {
        return featVec;
    }
}
