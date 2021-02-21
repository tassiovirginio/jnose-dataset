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

package org.deeplearning4j.spark.impl.common;

import org.apache.spark.api.java.function.Function2;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Adds 2 ndarrays
 * @author Adam Gibson
 */
public class Add implements Function2<INDArray, INDArray, INDArray> {
    @Override
    public INDArray call(INDArray v1, INDArray v2) throws Exception {
        INDArray res = v1.addi(v2);

        Nd4j.getExecutioner().commit();

        return res;
    }
}
