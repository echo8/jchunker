package com.echo8.jchunker.fileformat;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class CoNLLDataFileFormatTest {
    
    @Test
    public void testCoNLLDataFileFormat() throws Exception {
        CoNLLDataFileFormat ff = new CoNLLDataFileFormat();
        List<String[]> data = ff.readDataFile(new File(this.getClass().getResource("/test-datafile.txt").getFile()));
        
        assertEquals("Advanced", data.get(0)[0]);
        assertEquals("CC", data.get(4)[1]);
        assertEquals("B-PP", data.get(25)[2]);
        assertEquals(0, data.get(data.size()-1).length);
    }
}
