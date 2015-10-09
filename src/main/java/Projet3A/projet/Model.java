package Projet3A.projet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.classification.SVMWithSGD;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.DecisionTree;
import org.apache.spark.mllib.tree.RandomForest;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.apache.spark.mllib.tree.model.RandomForestModel;
import org.apache.spark.rdd.RDD;

import Projet3A.Properties.ProjectProperties;
/*import com.thales.spark.neuralnet.activationFunctions.SigmoidActivation;
import com.thales.spark.neuralnet.algorithms.SparkNeuralNetwork;
import com.thales.spark.neuralnet.learner.SGDLearner;
import com.thales.spark.neuralnet.lossFunctions.CrossEntropyLoss;
import com.thales.spark.neuralnet.models.NeuralNetworkModel;
*/
import scala.Tuple2;
import scala.Tuple3;
//import scala.actors.threadpool.Arrays;

public class Model implements Serializable {

	private static final long serialVersionUID = 1L;
	int modeltype;
	NaiveBayesModel NBmodel;
	SVMModel SVMmodel;
	DecisionTreeModel decisionTreeModel;
	RandomForestModel forest;
	LogisticRegressionModel logReg;
//	NeuralNetworkModel neural;
	
	public Model (){
		modeltype=-1;
	}
	
	public void setModel(NaiveBayesModel m){
		NBmodel=m;
		modeltype = 0;
	}
	public void setModel(SVMModel m){
		SVMmodel=m;
		modeltype = 1;
	}
	public void setModel(DecisionTreeModel m){
		decisionTreeModel=m;
		modeltype = 2;
	}
	public void setModel(RandomForestModel m){
		forest=m;
		modeltype = 3;
	}
	public void setModelType(int i){
		modeltype=i;
	}
	public int getModelType(){
		return modeltype;
	}
	public void train(JavaPairRDD<String, Tuple2<Vector, Double>> trainSet,int model,JavaSparkContext sc,ProjectProperties props) throws Exception{
		JavaRDD<LabeledPoint> labeledTweets = trainSet.mapToPair(new PairFunction<Tuple2<String, Tuple2<Vector, Double>>, Vector, Double>() {
			public Tuple2<Vector, Double> call(Tuple2<String, Tuple2<Vector, Double>> t) throws Exception {
				return t._2();
			}
		}).map(new Function<Tuple2<Vector, Double>, LabeledPoint>() {
			public LabeledPoint call(Tuple2<Vector, Double> point) throws Exception {
				return new LabeledPoint(point._2(), point._1());
			}
		}
		);
		train(labeledTweets,model,sc,props);
	}
	/*
	 * train the model (creation of the model)
	 */
	public void train(JavaRDD<LabeledPoint> trainSet,int model,JavaSparkContext sc,ProjectProperties props) throws Exception{
		if (trainSet.isEmpty())
			throw new Exception("pas assez de donnÃ©es, dico trop restrictif");
		if (model==0){
			modeltype=0;
			System.out.println("NaiveBayes model");
			NBmodel=NaiveBayes.train(trainSet.rdd());
		}
		if (model==1||model==11){
			modeltype=1;
			System.out.println("SVM model");
			SVMmodel=SVMWithSGD.train(trainSet.rdd(),100);
			if (model==11){
				modeltype=11;
				SVMmodel.clearThreshold();
			}
		}
		if (model==2){
			modeltype=2;
			System.out.println("Decision tree");
			Integer numClasses = props.getNumClasses();
			Map<Integer, Integer> categoricalFeaturesInfo = new HashMap<Integer, Integer>();
			String impurity = props.getImpurity();
			Integer maxDepth = props.getMaxDepth();
			Integer maxBins = props.getMaxBins();
			decisionTreeModel=DecisionTree.trainClassifier(trainSet, numClasses,
					  categoricalFeaturesInfo, impurity, maxDepth, maxBins);
			
		}
		if (model==3){
			modeltype=3;
			System.out.println("Random Forest");
			HashMap<Integer, Integer> categoricalFeaturesInfo = new HashMap<Integer, Integer>();
			Integer numClasses = props.getNumClasses();
			Integer numTrees = props.getNumTrees();
			String featureSubsetStrategy = props.getFeatureSubsetStrategy();
			
			String impurity = props.getImpurity();
			Integer maxDepth = props.getMaxDepth();
			Integer maxBins = props.getMaxBins();
			Integer seed = props.getSeed();

			forest = RandomForest.trainClassifier(trainSet, numClasses,
			  categoricalFeaturesInfo, numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins,
			  seed);
		}
		if (model==4){
			modeltype=4;
			System.out.println("logistic regression");
			logReg=LogisticRegressionWithSGD.train(trainSet.rdd(), 100);
		}
		/*if (model==5){
			modeltype=5;
			System.out.println("neural network");
			int dim = trainSet.first().features().size();
			int[] t = new int[]{dim,1};
//			trainSet.saveAsObjectFile("trainSet 200000");
//			JavaRDD<LabeledPoint> train = trainSet.map(s->{
//				double[] ta = s.features().toArray();
//				for (int i = 0;i<ta.length;i++){
//					ta[i]=ta[i]/10d;
//				}
//				return new LabeledPoint(s.label(),Vectors.dense(ta));
//			});
//			List<LabeledPoint> set = train.collect();
			/*neural = new NeuralNetworkModel(t, new SigmoidActivation(), new SigmoidActivation(), new SGDLearner(0.1), new CrossEntropyLoss());
			for (int i=0;i<50;i++)
				neural =new SparkNeuralNetwork().learn_neural_from_labeledPoint(train.rdd(),neural , 50, true);
			neural = SparkNeuralNetwork.learn_neural_for_classification_labeledPoint(t, trainSet.rdd(), 20, null);
		}*/
	}
	/*
	 * predict the result
	 */
	public double predict(Vector v){
		
		if (modeltype==0){
			return NBmodel.predict(v);
		}
		if (modeltype==1||modeltype==11){
			return SVMmodel.predict(v);
		}
		if (modeltype==2){
			return decisionTreeModel.predict(v);
		}
		if (modeltype==3){
			return forest.predict(v);
		}
		if (modeltype==4){
			return logReg.predict(v);
		}
		/*if (modeltype==5){
			double[] ta = v.toArray();
//			for (int i = 0;i<ta.length;i++){
//				ta[i]=ta[i]/10d;
//			}
			double[] res = neural.predict(ta);
//			System.out.println(res[res.length-1]);
			return res[res.length-1];
		}*/
		return -1d;
	}
	
	public void save(JavaSparkContext sc,ProjectProperties props){
		if (modeltype==2){
			decisionTreeModel.save(sc.sc(), props.getPathToModel());
		}
		if (modeltype==3){
			forest.save(sc.sc(), props.getPathToModel());
		}
	}
	/*public JavaPairRDD<String, Tuple3<Double, Double, Double>> predict(JavaPairRDD<String, Tuple2<Vector, Double>> VectorizesTweets,double pond){
//		List<Tuple2<String, Tuple2<Vector, Double>>> temp = VectorizesTweets.collect();
//		VectorizesTweets.saveAsObjectFile("testSet 200000");
		JavaPairRDD<String, Tuple3<Double, Double, Double>> data = VectorizesTweets.mapToPair(t -> {
				Vector vector = t._2()._1();
				double pred = predict(vector);
				Tuple3<Double, Double, Double> tuple = new Tuple3<Double, Double, Double>(
						pred * (1 + pond), (double) t._2()._2()
								* (1 + pond), (1 + pond));
				return new Tuple2<String, Tuple3<Double, Double, Double>>(t
						._1(), tuple);
			});
		if (modeltype==11){
			JavaRDD<Tuple2<Double,Integer>> temp = data.map(new Function<Tuple2<String, Tuple3<Double, Double, Double>>, Tuple2<Double, Integer>>() {
				public Tuple2<Double, Integer> call(Tuple2<String, Tuple3<Double, Double, Double>> t) throws Exception {
					return new Tuple2<Double,Integer>(t._2()._1()*t._2()._1(),1);
				}
			}
					);
			Tuple2<Double,Integer> tuple = temp.reduce(new Function2<Tuple2<Double, Integer>, Tuple2<Double, Integer>, Tuple2<Double, Integer>>() {
				public Tuple2<Double, Integer> call(Tuple2<Double, Integer> t1, Tuple2<Double, Integer> t2)
						throws Exception {
					return new Tuple2<Double,Integer>(t1._1()+t2._1(),t1._2()+t2._2());
				}
			});
			double moy = Math.sqrt(tuple._1()/(double)tuple._2());
			
			data = data.mapToPair(new PairFunction<Tuple2<String, Tuple3<Double, Double, Double>>, String, Tuple3<Double, Double, Double>>() {
				public Tuple2<String, Tuple3<Double, Double, Double>> call(
						Tuple2<String, Tuple3<Double, Double, Double>> t) throws Exception {
					return new Tuple2<String, Tuple3<Double, Double, Double>>(t._1(),new Tuple3<Double, Double, Double>(Math.max(0,Math.min(t._2()._1()/(4*moy)+0.5,1)),t._2()._2(),t._2()._3()));
				}
			}
					);
		}
//		JavaRDD<Tuple2<Object, Object>> d = data.map(t->{
//			return new Tuple2<Object, Object>(t._2()._1(),t._2()._2());
//		});
//		BinaryClassificationMetrics metric = new BinaryClassificationMetrics(d.rdd());
//		RDD<Tuple2<Object, Object>> roc = metric.roc();
//		roc.saveAsTextFile("roc");
		return data;
	}
		*/	
}

