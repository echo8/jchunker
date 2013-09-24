package com.echo8.jchunker.featvec;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HashMapFeatDictionaryTest {

    @Test
    public void testHashMapFeatDictionary() {
        HashMapFeatDictionary fd = new HashMapFeatDictionary();
        
        assertEquals(0, fd.getFeatIndex("feature1", true));
        assertEquals(-1, fd.getFeatIndex("feature2", false));
    }
}
