package org.deeplearning4j.optimize.solvers.accumulation;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import static org.junit.Assert.assertTrue;

/**
 * Tests for memory-related stuff in gradients accumulator
 *
 * @author raver119@gmail.com
 */
@Slf4j
public class EncodedGradientsAccumulatorTest {

    /**
     * This test ensures, that memory amount assigned to buffer is enough for any number of updates
     * @throws Exception
     */
    @Test
    public void testStore1() throws Exception {
        int numParams = 100000;

        int workers[] = new int[] {2, 4, 8};

        EncodingHandler handler = new EncodingHandler(1e-3);


        for (int numWorkers : workers) {
            int bufferSize = EncodedGradientsAccumulator.getOptimalBufferSize(numParams, numWorkers, 2);
            log.info("Workers: {}; Buffer size: {} bytes", numWorkers, bufferSize);
            EncodedGradientsAccumulator accumulator =
                            new EncodedGradientsAccumulator(numWorkers, handler, bufferSize, 2, null);

            for (int e = 10; e < numParams / 10; e++) {
                INDArray encoded = handler.encodeUpdates(getGradients(numParams, e, 2e-3));
                accumulator.receiveUpdate(encoded);

                // just purge updates, like they were consumed
                for (int i = 0; i < accumulator.messages.size(); i++) {
                    accumulator.messages.get(i).clear();
                }
            }
        }
    }

}
