package org.deeplearning4j.spark.impl.common.repartition;

import org.apache.spark.HashPartitioner;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.deeplearning4j.spark.BaseSparkTest;
import org.deeplearning4j.spark.impl.common.repartition.HashingBalancedPartitioner.LinearCongruentialGenerator;
import org.junit.Test;
import scala.Tuple2;

import java.util.*;

import static org.junit.Assert.assertTrue;


/**
 * Created by huitseeker on 4/4/17.
 */
public class HashingBalancedPartitionerTest extends BaseSparkTest {

    @Test
    public void hashingBalancedPartitionerDoesBalance() {
        // partitionWeightsByClass = [[1.714, .429, .857], [0.9, 0.6, 1.5]]
        List<Double> reds = Arrays.asList(1.714D, 0.429D, .857D);
        List<Double> blues = Arrays.asList(0.9D, 0.6D, 1.5D);
        List<List<Double>> partitionWeights = Arrays.asList(reds, blues);

        HashingBalancedPartitioner hbp = new HashingBalancedPartitioner(partitionWeights);
        List<Tuple2<Integer, String>> l = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            l.add(new Tuple2<Integer, String>(0, "red"));
        }
        for (int i = 0; i < 3; i++) {
            l.add(new Tuple2<Integer, String>(0, "blue"));
        }
        for (int i = 0; i < 1; i++) {
            l.add(new Tuple2<Integer, String>(1, "red"));
        }
        for (int i = 0; i < 2; i++) {
            l.add(new Tuple2<Integer, String>(1, "blue"));
        }
        for (int i = 0; i < 2; i++) {
            l.add(new Tuple2<Integer, String>(2, "red"));
        }
        for (int i = 0; i < 5; i++) {
            l.add(new Tuple2<Integer, String>(2, "blue"));
        }
        // This should give exactly the sought distribution
        JavaPairRDD<Integer, String> rdd =
                        JavaPairRDD.fromJavaRDD(sc.parallelize(l)).partitionBy(new HashPartitioner(3));

        // Let's reproduce UIDs
        JavaPairRDD<Tuple2<Long, Integer>, String> indexedRDD = rdd.zipWithUniqueId().mapToPair(
                        new PairFunction<Tuple2<Tuple2<Integer, String>, Long>, Tuple2<Long, Integer>, String>() {
                            @Override
                            public Tuple2<Tuple2<Long, Integer>, String> call(
                                            Tuple2<Tuple2<Integer, String>, Long> payLoadNuid) {
                                Long uid = payLoadNuid._2();
                                String value = payLoadNuid._1()._2();
                                Integer elemClass = value.equals("red") ? 0 : 1;
                                return new Tuple2<Tuple2<Long, Integer>, String>(
                                                new Tuple2<Long, Integer>(uid, elemClass), value);
                            }
                        });

        List<Tuple2<Tuple2<Long, Integer>, String>> testList = indexedRDD.collect();

        int[][] colorCountsByPartition = new int[3][2];
        for (final Tuple2<Tuple2<Long, Integer>, String> val : testList) {
            System.out.println(val);
            Integer partition = hbp.getPartition(val._1());
            System.out.println(partition);

            if (val._2().equals("red"))
                colorCountsByPartition[partition][0] += 1;
            else
                colorCountsByPartition[partition][1] += 1;
        }

        for (int i = 0; i < 3; i++) {
            System.out.println(Arrays.toString(colorCountsByPartition[i]));
        }
        for (int i = 0; i < 3; i++) {
            // avg red per partition : 2.33
            assertTrue(colorCountsByPartition[i][0] >= 1 && colorCountsByPartition[i][0] < 4);
            // avg blue per partition : 3.33
            assertTrue(colorCountsByPartition[i][1] >= 2 && colorCountsByPartition[i][1] < 5);
        }

    }
}
