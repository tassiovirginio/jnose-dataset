/*-
 *
 *  * Copyright 2015 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package org.deeplearning4j.models;

import com.google.common.primitives.Doubles;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.learning.impl.elements.RandomUtils;
import org.deeplearning4j.models.embeddings.loader.VectorsConfiguration;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.InMemoryLookupCache;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.UimaSentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author jeffreytang
 * @author raver119@gmail.com
 */
public class WordVectorSerializerTest {

    private File textFile, binaryFile, textFile2;
    String pathToWriteto;

    private Logger logger = LoggerFactory.getLogger(WordVectorSerializerTest.class);

    @Before
    public void before() throws Exception {
        if (textFile == null) {
            textFile = new ClassPathResource("word2vecserialization/google_news_30.txt").getFile();
        }
        if (binaryFile == null) {
            binaryFile = new ClassPathResource("word2vecserialization/google_news_30.bin.gz").getFile(".gz");
        }
        pathToWriteto = new ClassPathResource("word2vecserialization/testing_word2vec_serialization.txt").getFile()
                        .getAbsolutePath();
        FileUtils.deleteDirectory(new File("word2vec-index"));
    }

    @Test
    public void testLoaderText() throws IOException {
        WordVectors vec = WordVectorSerializer.loadGoogleModel(textFile, false);
        assertEquals(vec.vocab().numWords(), 30);
        assertTrue(vec.vocab().hasToken("Morgan_Freeman"));
        assertTrue(vec.vocab().hasToken("JA_Montalbano"));
    }


    @Test
    public void testVocabPeristence() throws Exception {
        // we build vocab save it, and confirm equality

    }

}
