package org.deeplearning4j.datasets.iterator;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.MultiDataSetIteratorAdapter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by susaneraly on 6/8/17.
 */
public class EarlyTerminationMultiDataSetIteratorTest {

    @Test
    public void testNextNum() throws IOException {
        int terminateAfter = 1;

        MultiDataSetIterator iter =
                        new MultiDataSetIteratorAdapter(new MnistDataSetIterator(minibatchSize, numExamples));
        EarlyTerminationMultiDataSetIterator earlyEndIter =
                        new EarlyTerminationMultiDataSetIterator(iter, terminateAfter);

        earlyEndIter.next(10);
        assertEquals(false, earlyEndIter.hasNext());

        earlyEndIter.reset();
        assertEquals(true, earlyEndIter.hasNext());
    }

    @Test
    public void testCallstoNextNotAllowed() throws IOException {
        int terminateAfter = 1;

        MultiDataSetIterator iter =
                        new MultiDataSetIteratorAdapter(new MnistDataSetIterator(minibatchSize, numExamples));
        EarlyTerminationMultiDataSetIterator earlyEndIter =
                        new EarlyTerminationMultiDataSetIterator(iter, terminateAfter);

        earlyEndIter.next(10);
        iter.reset();
        exception.expect(RuntimeException.class);
        earlyEndIter.next(10);
    }

}
