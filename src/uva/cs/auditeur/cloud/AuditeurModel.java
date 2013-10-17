package uva.cs.auditeur.cloud;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AuditeurModel {
	private String modelKey;
	private String rangeKey;
	private List<String> lookfors;
	private List<String> withins;
	private double accuracy;
	
	
	public AuditeurModel() {
		// TODO Auto-generated constructor stub
	}
	public void setModelKey(String blobKeyStr){
		this.modelKey = blobKeyStr;
	}
	public void setRangeKey(String blobKeyStr){
		this.rangeKey = blobKeyStr;
	}
	public void setLookFors(ArrayList<String> lookforTags){
		this.lookfors = lookforTags;
	}
	public void setWithins(ArrayList<String> withinTags){
		this.withins = withinTags;
	}
	
	public void setAccuracy(double accuracy){
		this.accuracy = accuracy;
	}
	
	public static void main(String[] args) {
//		Gson gson = new Gson();
//		Gson gson = new GsonBuilder().setPrettyPrinting().create();
//		String json = gson.toJson(model);
//		System.out.println(json);
	}

}
