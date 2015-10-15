package Projet3A.Properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

public class ProjectProperties implements Serializable{
	String PathToFile;
	int model;
	Integer numClasses;
	Integer numTrees;
	String featureSubsetStrategy;
	String impurity;
	Integer maxDepth;
	Integer maxBins;
	Integer seed;
	String PathToModel;
	Integer numIteration;
	int numCols;



	public ProjectProperties(){
		Properties properties = new Properties();
		File file= new File("file.properties");
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
			properties.load(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.PathToFile =properties.getProperty("PathToFile");
		model = Integer.valueOf(properties.getProperty("model"));
		numClasses = Integer.valueOf(properties.getProperty("numClasses"));
		numIteration = Integer.valueOf(properties.getProperty("numIteration"));
		numTrees = Integer.valueOf(properties.getProperty("numClasses"));
		maxDepth = Integer.valueOf(properties.getProperty("maxDepth"));
		maxBins = Integer.valueOf(properties.getProperty("maxBins"));
		seed = Integer.valueOf(properties.getProperty("seed"));
		numCols = Integer.valueOf(properties.getProperty("numCols"));
		featureSubsetStrategy = properties.getProperty("featureSubsetStrategy");
		impurity = properties.getProperty("impurity");
		PathToModel = properties.getProperty("PathToModel");
	}
	
	public Integer getNumIteration() {
		return numIteration;
	}


	public void setNumIteration(Integer numIteration) {
		this.numIteration = numIteration;
	}

	public String getPathToModel() {
		return PathToModel;
	}
	
	public void setPathToModel(String pathToModel) {
		PathToModel = pathToModel;
	}

	public Integer getNumClasses() {
		return numClasses;
	}

	public void setNumClasses(Integer numClasses) {
		this.numClasses = numClasses;
	}

	public Integer getNumTrees() {
		return numTrees;
	}

	public void setNumTrees(Integer numTrees) {
		this.numTrees = numTrees;
	}

	public String getFeatureSubsetStrategy() {
		return featureSubsetStrategy;
	}

	public void setFeatureSubsetStrategy(String featureSubsetStrategy) {
		this.featureSubsetStrategy = featureSubsetStrategy;
	}

	public String getImpurity() {
		return impurity;
	}

	public void setImpurity(String impurity) {
		this.impurity = impurity;
	}

	public int getNumCols() {
		return numCols;
	}

	public void setNumCols(int numcols) {
		this.numCols = numcols;
	}

	public Integer getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(Integer maxDepth) {
		this.maxDepth = maxDepth;
	}

	public Integer getMaxBins() {
		return maxBins;
	}

	public void setMaxBins(Integer maxBins) {
		this.maxBins = maxBins;
	}

	public Integer getSeed() {
		return seed;
	}

	public void setSeed(Integer seed) {
		this.seed = seed;
	}

	public String getPathToFile() {
		return PathToFile;
	}

	public void setPathToFile(String pathToFile) {
		PathToFile = pathToFile;
	}

	public int getModel() {
		return model;
	}

	public void setModel(int model) {
		this.model = model;
	}
	
}
