package Projet3A.projet;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.linalg.Vector;

import scala.Tuple2;

public class Main {
	public static void main(String[] args) {
		Logger.getLogger("org").setLevel(Level.OFF);
		Logger.getLogger("akka").setLevel(Level.OFF);
		SparkConf conf = new SparkConf().setAppName("sentiments");
			conf.setMaster("local[2]");
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaRDD<Tuple2<Double, Vector>> data = getData(sc);
		JavaPairRDD<String,Tuple2<Vector,Double>> toModel = data.mapToPair(new PairFunction<Tuple2<Double, Vector>, String, Tuple2<Vector, Double>>() {
			public Tuple2<String, Tuple2<Vector, Double>> call(Tuple2<Double, Vector> t) throws Exception {
				return new Tuple2<String,Tuple2<Vector,Double>>("id",new Tuple2<Vector,Double>(t._2,t._1));
			}
		});
		long nt = toModel.count();
		long n = toModel.filter(new Function<Tuple2<String, Tuple2<Vector, Double>>, Boolean>() {
			public Boolean call(Tuple2<String, Tuple2<Vector, Double>> t) throws Exception {
				return t._2._2.equals(0d);
			}
		}).count();
		System.out.println(n);
		System.out.println(nt-n);
		System.out.println(n/(double)nt+" % de pixels malades");
		double[] r = new double[]{0.75,0.25};
		JavaRDD<Tuple2<String,Tuple2<Vector,Double>>>[] sets = toModel.map(new Function<Tuple2<String, Tuple2<Vector, Double>>, Tuple2<String, Tuple2<Vector, Double>>>() {
					public Tuple2<String, Tuple2<Vector, Double>> call(Tuple2<String, Tuple2<Vector, Double>> t) throws Exception {
						return t;
					}
				}).randomSplit(r);
//		System.out.println(sets[0].count());
//		System.out.println(sets[1].count());
		JavaPairRDD<String,Tuple2<Vector,Double>> trainset = sets[0].mapToPair(new PairFunction<Tuple2<String, Tuple2<Vector, Double>>, String, Tuple2<Vector, Double>>() {
			public Tuple2<String, Tuple2<Vector, Double>> call(Tuple2<String, Tuple2<Vector, Double>> t)
					throws Exception {
				return t;
			}
		});
		final Model model = new Model(); 
		try {
			model.train(trainset, 3, sc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JavaRDD<Tuple2<Double,Double>> results = sets[1].map(new Function<Tuple2<String, Tuple2<Vector, Double>>, Tuple2<Double, Double>>() {
			public Tuple2<Double, Double> call(Tuple2<String, Tuple2<Vector, Double>> t) throws Exception {
				return new Tuple2<Double,Double>(model.predict(t._2._1),t._2._2);
			}
		});
		stat(results);
	}
	
	public static JavaRDD<Tuple2<Double, Vector>> getData(JavaSparkContext sc){
		JavaRDD<String> data = sc.textFile("C:/Users/admin/Documents/Projet3A - Polarimetrie de Mueller/PixTup.csv");
		JavaRDD<String[]> databis = data.map(new Function<String, String[]>() {
			public String[] call(String t) throws Exception {
				return t.split(",");
			}
		});
		JavaRDD<Tuple2<Double,Vector>> toreturn = databis.map(new Function<String[], Tuple2<Double, Vector>>() {
			public Tuple2<Double, Vector> call(String[] t) throws Exception {
					double[] tab=new double[t.length-1];
					for (int i=0;i<t.length-1;i++) tab[i]=Double.valueOf(t[i+1]);
					return new Tuple2<Double,Vector>(Double.valueOf(t[0])-3,Vectors.dense(tab));
			}
		});
		return toreturn;
	}
	
	public static void stat(JavaRDD<Tuple2<Double,Double>> res){
		JavaRDD<Tuple2<Double,Double>> pos = res.filter(new Function<Tuple2<Double, Double>, Boolean>() {
			public Boolean call(Tuple2<Double, Double> t) throws Exception {
				return t._2.equals(1d);
			}
		});
		JavaRDD<Tuple2<Double,Double>> neg = res.filter(new Function<Tuple2<Double, Double>, Boolean>() {
			public Boolean call(Tuple2<Double, Double> t) throws Exception {
				return t._2.equals(0d);
			}
		});
		long totpos = pos.count();
		long totneg = neg.count();
		long tot = totpos+totneg;
		long totvp = pos.filter(new Function<Tuple2<Double, Double>, Boolean>() {
			public Boolean call(Tuple2<Double, Double> t) throws Exception {
				return t._1.equals(t._2);
			}
		}).count();
		long totvn = neg.filter(new Function<Tuple2<Double, Double>, Boolean>() {
			public Boolean call(Tuple2<Double, Double> t) throws Exception {
				return t._1.equals(t._2);
			}
		}).count();
		long totv = totvp+totvn;
		System.out.println("% de prediction : "+totv/(double)tot);
		System.out.println("% de vp (sensibilité): "+totvp/(double)totpos);
		System.out.println("% de vn (specificité): "+totvn/(double)totneg);
		
	}
}
