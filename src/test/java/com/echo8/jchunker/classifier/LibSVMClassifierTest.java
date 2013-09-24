package com.echo8.jchunker.classifier;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.echo8.jchunker.featvec.BasicFeatVecGenerator;
import com.echo8.jchunker.featvec.FeatVecGenerator;

public class LibSVMClassifierTest {
    
    @Test
    public void testLibSVMClassifier() {
        Classifier c = new LibSVMClassifier();
        
        List<String[]> trainData = new ArrayList<String[]>();
        trainData.add(new String[]{"one", "1"});
        trainData.add(new String[]{"two", "2"});
        trainData.add(new String[]{"three", "3"});
        trainData.add(new String[]{"four", "1"});
        trainData.add(new String[]{"five", "2"});
        
        FeatVecGenerator fvg = new BasicFeatVecGenerator(0, 0);
        
        c.train(trainData, fvg);
        
        assertEquals("3", c.chunk(Arrays.asList(new String[][]{new String[]{"three"}}), fvg).get(0));
        assertEquals("2", c.chunk(Arrays.asList(new String[][]{new String[]{"five"}}), fvg).get(0));
    }
}
