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
