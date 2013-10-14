package uva.cs.auditeur.cloud.cloudsvm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GridSearch {
	double[] C;
	double[] gamma;
	double bestGamma;
	double bestC;
	double bestAccuracy;
	
	public static double[] range_f(double begin, double end, double step){
		// like range, but works on non-integer too
		// code adapted from gridSearch
		List<Double> seq = new ArrayList<Double>();
		while(true){
			if(step > 0 && begin > end)
				break;
			if(step < 0 && begin < end)
				break;
			seq.add(begin);
			begin = begin + step;
		}
		//return as double[]
		double[] result = new double[seq.size()];
		for(int i = 0; i < seq.size(); i++){
			result[i] = seq.get(i).doubleValue();
		}
		return result;
	}
	
	public void gridRange(double c_begin, double c_end, double c_step, double g_begin, double g_end, double g_step){
		C = range_f(c_begin,c_end,c_step);
        System.out.print(Arrays.toString(C));
        gamma = range_f(g_begin,g_end,g_step);
        System.out.print(Arrays.toString(gamma));
        
        //looping through
        for(int c = 0; c < C.length; c++){
        	C[c] = Math.pow(2,C[c]);
        }
        for(int g = 0; g < gamma.length; g++){
        	gamma[g] = Math.pow(2,gamma[g]);
        } 
	}
	
	public void search(svm_train t){
			// search for C and gamma
			double bestC=0;
			double bestGamma=0;
			double bestAccuracy = 0.0;
			for (int i = 0; i < C.length; i++) {
				for (int j = 0; j < gamma.length; j++) {
					System.out.println(C[i]);
					System.out.println(gamma[j]);
					
					double avg_accuracy = t.do_cross_validation(C[i], gamma[j]);
					if(avg_accuracy > bestAccuracy){
						bestAccuracy = avg_accuracy;
						bestC = C[i];
						bestGamma = gamma[j];
					}
				}// gamma
			}// C
			this.bestC = bestC;
			this.bestGamma = bestGamma;
			this.bestAccuracy = bestAccuracy;
			
		System.out.println("best accuracy:"+bestAccuracy);
		System.out.println("best C:"+bestC);
		System.out.println("best gamma:"+bestGamma);
	}
	
	public double getBestC(){
		return bestC;
	}
	public double getBestGamma(){
		return bestGamma;
	}
	public double getBestAccuracy(){
		return bestAccuracy;
	}
	
	
	public static void main(String[] args) throws IOException {
		double c_begin = -5;
    	double c_end = 15;
    	double c_step = 3;
    	
    	double g_begin = 3;
    	double g_end = -15;
    	double g_step = -3;
    	
    	//should create a training paramete object///
		
//    	args = new String[]{"-v","5","-c","2.0","-g","0.015625","-q","scaled_dataset"};
//    	args = new String[]{"-q","scaled_dataset"};
    	args = new String[]{"-v","5","-q"};
		svm_train t = new svm_train();
		//modify input format as byte[]
				BufferedReader fp=null;
				String data_filename = "scaled_dataset"; 
				try {
				fp = new BufferedReader(new FileReader(data_filename));
			} catch (Exception e) {
				System.err.println("can't open file " + data_filename);
				System.exit(1);
			}
				StringBuilder result = new StringBuilder();
				String ln;
				while((ln = fp.readLine())!=null){
					result.append(ln+"\n");
				}
				t.setData(result.toString().getBytes());
				t.initialize(args);
		///////
//		now need to pass best params to svm_train without using args		
				
		//////

		GridSearch grid = new GridSearch(); 
		grid.gridRange(c_begin,c_end,c_step,g_begin,g_end,g_step);
		
		grid.search(t);
		
		
		t.setParam(grid.getBestC(), grid.getBestGamma());
		byte[] model_bytes = t.cloud_run();
		
//		for writing to a file system
//		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("model")));
//	    dos.write(model_bytes);
//	    dos.close();
		

		
	}

}
