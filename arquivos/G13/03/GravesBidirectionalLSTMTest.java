package org.deeplearning4j.nn.layers.recurrent;

import junit.framework.TestCase;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.CacheMode;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.distribution.UniformDistribution;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.params.GravesBidirectionalLSTMParamInitializer;
import org.deeplearning4j.nn.params.GravesLSTMParamInitializer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.junit.Ignore;
import org.junit.Test;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.activations.impl.ActivationSigmoid;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.learning.config.NoOp;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;


public class GravesBidirectionalLSTMTest {

    @Test
    public void testGateActivationFnsSanityCheck() {
        for (String gateAfn : new String[] {"sigmoid", "hardsigmoid"}) {

            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
                            .seed(12345).list()
                            .layer(0, new org.deeplearning4j.nn.conf.layers.GravesBidirectionalLSTM.Builder()
                                            .gateActivationFunction(gateAfn).activation(Activation.TANH).nIn(2).nOut(2)
                                            .build())
                            .layer(1, new org.deeplearning4j.nn.conf.layers.RnnOutputLayer.Builder()
                                            .lossFunction(LossFunctions.LossFunction.MSE).nIn(2).nOut(2)
                                            .activation(Activation.TANH).build())
                            .build();

            MultiLayerNetwork net = new MultiLayerNetwork(conf);
            net.init();

            assertEquals(gateAfn, ((org.deeplearning4j.nn.conf.layers.GravesBidirectionalLSTM) net.getLayer(0).conf()
                            .getLayer()).getGateActivationFn().toString());

            INDArray in = Nd4j.rand(new int[] {3, 2, 5});
            INDArray labels = Nd4j.rand(new int[] {3, 2, 5});

            net.fit(in, labels);
        }
    }
}
