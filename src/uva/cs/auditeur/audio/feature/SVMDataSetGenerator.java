package uva.cs.auditeur.audio.feature;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import uva.cs.auditeur.audio.Wave;


public class SVMDataSetGenerator {
	

	
	public static final String SPACE = " ";
	public static final String NEWLINE = "\n";
	
	
	
/**
 * Save DataSet with specified label to file system
 * @param label
 * @param windowFeatureList
 * @param fp
 * @return
 * @throws IOException
 */
	public static StringBuilder generateDataSet(String label, List<WindowFeature> windowFeatureList, DataOutputStream fp) throws IOException{
		
		StringBuilder sb = new StringBuilder();
		int totalInstances = 0;
		for(WindowFeature wf: windowFeatureList){
			sb.append(label+SPACE);
			fp.writeBytes(label+SPACE);
        	
			
    		int featureIndex = 1;	//start at 1
    		for(double[] stats : wf.windowFeature){	//set of statistics of each feature
    			for(double value: stats){
    				sb.append(featureIndex+":"+(float)value+SPACE);
    				fp.writeBytes(featureIndex+":"+(float)value+SPACE);
    				featureIndex++;
    			}
    			
    		}
    		sb.append(NEWLINE);
    		fp.writeBytes(NEWLINE);
    		totalInstances++;	
        }
		System.out.println(totalInstances);
		return sb;
		
	}
	
public static StringBuilder generateDataSet(String label, List<WindowFeature> windowFeatureList){
		
		StringBuilder sb = new StringBuilder();
		int totalInstances = 0;
		for(WindowFeature wf: windowFeatureList){
			sb.append(label+SPACE);
        	
			
    		int featureIndex = 1;	//start at 1
    		for(double[] stats : wf.windowFeature){	//set of statistics of each feature
    			for(double value: stats){
    				sb.append(featureIndex+":"+(float)value+SPACE);
    				featureIndex++;
    			}
    			
    		}
    		sb.append(NEWLINE);
    		totalInstances++;	
        }
		System.out.println(totalInstances);
		return sb;
		
	}
	
	
public static StringBuilder generateAudioFeature(List<WindowFeature> windowFeatureList){
		StringBuilder sb = new StringBuilder();
		for(WindowFeature wf: windowFeatureList){

    		int featureIndex = 1;	//start at 1
    		for(double[] stats : wf.windowFeature){	//set of statistics of each feature
    			for(double value: stats){
    				sb.append(featureIndex+":"+(float)value+SPACE);
    				featureIndex++;
    			}
    		}
    		sb.append(NEWLINE);
        }
		return sb;
}




public static StringBuilder generateAudioFeatureFromByteSignal(double[] inputSignal, int Fs){
	MFCCFeatureExtract mfccFeature = new MFCCFeatureExtract(inputSignal,Fs);
	List<WindowFeature> windowFeatureList = mfccFeature.getListOfWindowFeature();
	
	StringBuilder sb = new StringBuilder();
	for(WindowFeature wf: windowFeatureList){

		int featureIndex = 1;	//start at 1
		for(double[] stats : wf.windowFeature){	//set of statistics of each feature
			for(double value: stats){
				sb.append(featureIndex+":"+(float)value+SPACE);
				featureIndex++;
			}
		}
		sb.append(NEWLINE);
    }
	return sb;
}

public static StringBuilder generateAudioFeatureFromByteSignal(double[] inputSignal,int Tw, int Ts, int Fs, int Wl){
	MFCCFeatureExtract mfccFeature = new MFCCFeatureExtract(inputSignal, Tw, Ts, Fs, Wl);
	List<WindowFeature> windowFeatureList = mfccFeature.getListOfWindowFeature();
	
	StringBuilder sb = new StringBuilder();
	for(WindowFeature wf: windowFeatureList){

		int featureIndex = 1;	//start at 1
		for(double[] stats : wf.windowFeature){	//set of statistics of each feature
			for(double value: stats){
				sb.append(featureIndex+":"+(float)value+SPACE);
				featureIndex++;
			}
		}
		sb.append(NEWLINE);
    }
	return sb;
}
	
	public static List<WindowFeature> getWindowFeatureListFromFiles(List<String> audioFiles){
		  List<WindowFeature> windowFeatureList = new ArrayList<WindowFeature>();
			for(String file: audioFiles){
				Wave wave = new Wave(file);
				int Fs = wave.getWaveHeader().getSampleRate();
				double[] inputSignal = wave.getSampleAmplitudes();
				MFCCFeatureExtract mfccFeature = new MFCCFeatureExtract(inputSignal,Fs);
				windowFeatureList.addAll(mfccFeature.getListOfWindowFeature());
			}
		return windowFeatureList;
	}
	
	public static List<String> getListOfFilesInFolder(final File folder) {
		System.out.println("Reading Files:");
		List<String> fileList = new ArrayList<String>();
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	        	getListOfFilesInFolder(fileEntry);
	        } else {
	        	fileList.add(fileEntry.getParent()+"/"+fileEntry.getName());
	            System.out.println(fileEntry.getParent()+"/"+fileEntry.getName());
	        }
	    }
	    return fileList;
	}
	
	public static String generateWindowFeature(double[] inputSignal, int frameSize, int Fs){
		//need to know more about the right frame size; frame size = number of samples/frame which is in term of 2^n (padded with zeros if not exact)
		//only give one window feature
		// need to enforce that window size should be between 0 - 1 second
		//e.g. 1 second ~ 2^15 samples  
		//if the frame size is 2^10 then there would be 2^5 or 32 frames per window
		// and 0.5 second ~ 2^14 samples / 2^10 frame size => 2^4 frames per window
		MFCCFeatureExtract mfccFeature = new MFCCFeatureExtract(inputSignal,Fs);
		List<WindowFeature> windowFeatureList = mfccFeature.getListOfWindowFeature();
		
		StringBuilder sb = new StringBuilder();
		for(WindowFeature wf: windowFeatureList){

			int featureIndex = 1;	//start at 1
			for(double[] stats : wf.windowFeature){	//set of statistics of each feature
				for(double value: stats){
					sb.append(featureIndex+":"+(float)value+SPACE);
					featureIndex++;
				}
			}
			sb.append(NEWLINE);
	    }
		return sb.toString();
	}

}
