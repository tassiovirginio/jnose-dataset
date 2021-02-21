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

package org.deeplearning4j.datasets.datavec;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.datavec.api.records.Record;
import org.datavec.api.records.metadata.RecordMetaData;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.collection.CollectionRecordReader;
import org.datavec.api.records.reader.impl.collection.CollectionSequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.NumberedFileInputSplit;
import org.datavec.api.writable.DoubleWritable;
import org.datavec.api.writable.IntWritable;
import org.datavec.api.writable.NDArrayWritable;
import org.datavec.api.writable.Writable;
import org.deeplearning4j.datasets.datavec.exception.ZeroLengthSequenceException;
import org.deeplearning4j.datasets.datavec.tools.SpecialImageRecordReader;
import org.deeplearning4j.datasets.iterator.AsyncDataSetIterator;
import org.junit.Ignore;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;
import static org.nd4j.linalg.indexing.NDArrayIndex.all;
import static org.nd4j.linalg.indexing.NDArrayIndex.point;

/**
 * Created by agibsonccc on 3/6/15.
 */
@Slf4j
public class RecordReaderDataSetiteratorTest {

    @Test
    public void testRecordReaderMetaData() throws Exception {

        RecordReader csv = new CSVRecordReader();
        csv.initialize(new FileSplit(new ClassPathResource("iris.txt").getTempFileFromArchive()));

        int batchSize = 10;
        int labelIdx = 4;
        int numClasses = 3;

        RecordReaderDataSetIterator rrdsi = new RecordReaderDataSetIterator(csv, batchSize, labelIdx, numClasses);
        rrdsi.setCollectMetaData(true);

        while (rrdsi.hasNext()) {
            DataSet ds = rrdsi.next();
            List<RecordMetaData> meta = ds.getExampleMetaData(RecordMetaData.class);
            int i = 0;
            for (RecordMetaData m : meta) {
                Record r = csv.loadFromMetaData(m);
                INDArray row = ds.getFeatureMatrix().getRow(i);
                System.out.println(m.getLocation() + "\t" + r.getRecord() + "\t" + row);

                for (int j = 0; j < 4; j++) {
                    double exp = r.getRecord().get(j).toDouble();
                    double act = row.getDouble(j);
                    assertEquals("Failed on idx: " + j, exp, act, 1e-6);
                }
                i++;
            }
            System.out.println();

            DataSet fromMeta = rrdsi.loadFromMetaData(meta);
            assertEquals(ds, fromMeta);
        }
    }
}
