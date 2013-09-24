package com.echo8.jchunker.featvec;

/**
 * <p>
 * A single entry in a feature vector.
 * </p>
 */
public class FeatVecEntry {
    int index;
    double value;
    
    /**
     * 
     * @param index
     *              The entry's index in the feature vector.
     * @param value
     *              The entry's value.
     */
    public FeatVecEntry(int index, double value) {
        this.index = index;
        this.value = value;
    }
    
    /**
     * 
     * @return
     *              The entry's index in the feature vector.
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * 
     * @return
     *              The entry's value.
     */
    public double getValue() {
        return value;
    }
}
