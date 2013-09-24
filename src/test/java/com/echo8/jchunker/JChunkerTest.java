package com.echo8.jchunker;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.echo8.jchunker.featvec.BasicFeatVecGenerator;
import com.echo8.jchunker.featvec.FeatVecGenerator;

public class JChunkerTest {
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    
    @Test
    public void testJChunkerSaveAndLoad() throws Exception {
        FeatVecGenerator fvg = new BasicFeatVecGenerator(0, 0);
        JChunker chunker = new JChunker(fvg);
        
        List<String[]> trainData = new ArrayList<String[]>();
        trainData.add(new String[]{"one", "1"});
        trainData.add(new String[]{"two", "2"});
        trainData.add(new String[]{"three", "3"});
        trainData.add(new String[]{"four", "1"});
        trainData.add(new String[]{"five", "2"});
        
        chunker.train(trainData);
        
        File modelDirectory = tempDir.getRoot();
        String modelPrefix = "test";
        chunker.saveModel(modelDirectory, modelPrefix);
        
        JChunker loadedChunker = new JChunker();
        loadedChunker.loadModel(modelDirectory, modelPrefix);
        
        assertEquals("3", chunker.chunk(Arrays.asList(new String[][]{new String[]{"three"}})).get(0));
        assertEquals("2", chunker.chunk(Arrays.asList(new String[][]{new String[]{"five"}})).get(0));
    }
}
