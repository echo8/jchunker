package com.echo8.jchunker.featvec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class BasicFeatVecGeneratorTest {
    private List<String[]> testData;
    private List<String[]> testDataNoLabels;
    
    @Before
    public void setup() {
        testData = new ArrayList<String[]>();
        testData.add(new String[]{"Cardiac", "NNP", "B-NP"});
        testData.add(new String[]{"Pacemakers", "NNPS", "I-NP"});
        testData.add(new String[]{"Inc.", "NNP", "I-NP"});
        testData.add(new String[]{"units", "NNS", "I-NP"});
        testData.add(new String[]{"led", "VBD", "B-VP"});
        testData.add(new String[]{});
        
        testDataNoLabels = new ArrayList<String[]>();
        testDataNoLabels.add(new String[]{"Cardiac", "NNP"});
        testDataNoLabels.add(new String[]{"Pacemakers", "NNPS"});
        testDataNoLabels.add(new String[]{"Inc.", "NNP"});
        testDataNoLabels.add(new String[]{"units", "NNS"});
        testDataNoLabels.add(new String[]{"led", "VBD"});
        testDataNoLabels.add(new String[]{});
    }
    
    @Test
    public void testWithLabels() {
        BasicFeatVecGenerator fvg = new BasicFeatVecGenerator();
        
        FeatVec fv = fvg.generateFeatVec(testData, 2);
        assertEquals(testData.get(2)[2], fv.getLabel());
        assertEquals(12, fv.getFeatVec().size());
        
        fv = fvg.generateFeatVec(testData, 0);
        assertEquals(6, fv.getFeatVec().size());
        
        fv = fvg.generateFeatVec(testData, 1);
        assertEquals(9, fv.getFeatVec().size());
        
        fv = fvg.generateFeatVec(testData, 4);
        assertEquals(8, fv.getFeatVec().size());
        
        fv = fvg.generateFeatVec(testData, 5);
        assertNull(fv);
    }
    
    @Test
    public void testWithoutLabels() {
        BasicFeatVecGenerator fvg = new BasicFeatVecGenerator();
        
        FeatVec fv = fvg.generateFeatVecNoLabel(testDataNoLabels, 2);
        assertEquals("", fv.getLabel());
        assertEquals(0, fv.getFeatVec().size());
        
        for (int i = 0; i < testData.size(); i++) {
            fvg.generateFeatVec(testData, i);
        }
        
        fv = fvg.generateFeatVecNoLabel(testDataNoLabels, 0);
        assertEquals(6, fv.getFeatVec().size());
        
        fv = fvg.generateFeatVecNoLabel(testDataNoLabels, 1);
        assertEquals(8, fv.getFeatVec().size());
        
        fv = fvg.generateFeatVecNoLabel(testDataNoLabels, 4);
        assertEquals(6, fv.getFeatVec().size());
        
        fv = fvg.generateFeatVecNoLabel(testDataNoLabels, 5);
        assertNull(fv);
        
        fvg.addResult("I-NP");
        fvg.addResult("I-NP");
        
        fv = fvg.generateFeatVecNoLabel(testDataNoLabels, 4);
        assertEquals(8, fv.getFeatVec().size());        
    }
}
