package Projet3A.projet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Vectors;

import Projet3A.Properties.ProjectProperties;

import org.apache.spark.mllib.linalg.Vector;

import scala.Tuple2;

public class Main {
	public static void main(String[] args) {
		/*
		 * initialisation
		 */
		Logger.getLogger("org").setLevel(Level.OFF);
		Logger.getLogger("akka").setLevel(Level.OFF);
		SparkConf conf = new SparkConf().setAppName("sentiments");
		conf.setMaster("local[2]");
		JavaSparkContext sc = new JavaSparkContext(conf);
		ProjectProperties prop = new ProjectProperties();
		/*
		 * récuperation des données
		 */
		JavaRDD<Tuple2<Double, Vector>> data = getData(sc, prop);
		JavaPairRDD<String, Tuple2<Vector, Double>> toModel = data
				.mapToPair(t-> new Tuple2<String, Tuple2<Vector, Double>>("id", new Tuple2<Vector, Double>(t._2, t._1)));
		/*
		 * filtrage pour rééquilibrer
		 */
		toModel = equilibrer(toModel);
		/*
		 * séparation en train/test
		 */
		double[] r = new double[] { 0.75, 0.25 };
		JavaRDD<Tuple2<String, Tuple2<Vector, Double>>>[] sets = toModel.map(t -> t).randomSplit(r);
		JavaPairRDD<String, Tuple2<Vector, Double>> trainset = sets[0].mapToPair(t -> t);
		/*
		 * creation du modèle
		 */
		System.out.println("training...");
		final Model model = new Model();
		try {
			model.train(trainset, prop.getModel(), sc, prop);
		} catch (Exception e) {
			e.printStackTrace();
		}
		model.save(sc, prop);
		/*
		 * test du modèle
		 */
		System.out.println("testing...");
		JavaRDD<Tuple2<Double, Double>> results = sets[1].map(t -> {
			double x = model.predict(t._2._1);
			return new Tuple2<Double, Double>(x, t._2._2);
		});
		stat(results);
	}

	public static JavaRDD<Tuple2<Double, Vector>> getData(JavaSparkContext sc, ProjectProperties prop) {
		JavaRDD<String> data = sc.textFile(prop.getPathToFile());
		JavaRDD<String[]> databis = data.map(t -> t.split(","));
		final int numcols = prop.getNumCols() + 1;
		JavaRDD<Tuple2<Double, Vector>> toreturn = databis.map(t -> {
			int n = Math.min(t.length - 1, numcols);
			double[] tab = new double[n];
			for (int i = 0; i < n; i++)
				tab[i] = Double.valueOf(t[i + 1]);
			return new Tuple2<Double, Vector>(Double.valueOf(t[0]) - 3, Vectors.dense(tab));
		});
		return toreturn;
	}

	public static void stat(JavaRDD<Tuple2<Double, Double>> res) {
		JavaRDD<Tuple2<Double, Double>> pos = res.filter(t -> t._2.equals(1d));
		JavaRDD<Tuple2<Double, Double>> neg = res.filter(t -> t._2.equals(0d));
		long totpos = pos.count();
		long totneg = neg.count();
		long tot = totpos + totneg;
		long totvp = pos.filter(t -> t._1.equals(t._2)).count();
		long totvn = neg.filter(t -> t._1.equals(t._2)).count();
		long totv = totvp + totvn;
		System.out.println("% de prediction : " + totv / (double) tot);
		System.out.println("% de vp (sensibilité): " + totvp / (double) totpos);
		System.out.println("% de vn (specificité): " + totvn / (double) totneg);
	}

	public static JavaPairRDD<String, Tuple2<Vector, Double>> equilibrer(
			JavaPairRDD<String, Tuple2<Vector, Double>> data) {
		JavaPairRDD<String, Tuple2<Vector, Double>> pos = data.filter(t -> t._2._2.equals(1d));
		JavaPairRDD<String, Tuple2<Vector, Double>> neg = data.filter(t -> t._2._2.equals(0d));
		long npos = pos.count();
		long nneg = neg.count();
		if (npos > nneg) {
			double[] r = new double[] { nneg / (double) (npos), 1 - nneg / (double) (npos) };
			return pos.map(t->t).randomSplit(r)[0].mapToPair(t->t).union(neg);
		} else {
			double[] r = new double[] { npos / (double) (nneg), 1 - npos / (double) (nneg) };
			return neg.map(t->t).randomSplit(r)[0].mapToPair(t->t).union(pos);
		}
	}
}