package uva.cs.auditeur.cloud.cloudsvm;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Formatter;
import java.util.StringTokenizer;

public class svm_scale
{
	private String line = null;
	private double lower = -1.0;
	private double upper = 1.0;
	private double y_lower;
	private double y_upper;
	private boolean y_scaling = false;
	private double[] feature_max;
	private double[] feature_min;
	private double y_max = -Double.MAX_VALUE;
	private double y_min = Double.MAX_VALUE;
	private int max_index;
	private long num_nonzeros = 0;
	private long new_num_nonzeros = 0;
	private Formatter featureFormatter = new Formatter(new StringBuilder());
	private Formatter rangeFormatter;
	private DataOutputStream fp;
	private byte[] unscaledData;
	private static void exit_with_help()
	{
		System.out.print(
		 "Usage: svm-scale [options] data_filename\n"
		+"options:\n"
		+"-l lower : x scaling lower limit (default -1)\n"
		+"-u upper : x scaling upper limit (default +1)\n"
		+"-y y_lower y_upper : y scaling limits (default: no y scaling)\n"
		+"-s save_filename : save scaling parameters to save_filename\n"
		+"-r restore_filename : restore scaling parameters from restore_filename\n"
		);
		System.exit(1);
	}
	//modified no file name input
	private BufferedReader rewind(BufferedReader fp) throws IOException
	{
		fp.close();
		return Utility.byteArray2Reader(unscaledData);
	}

	private void output_target(double value)
	{
		if(y_scaling)
		{
			if(value == y_min)
				value = y_lower;
			else if(value == y_max)
				value = y_upper;
			else
				value = y_lower + (y_upper-y_lower) *
				(value-y_min) / (y_max-y_min);
		}
		featureFormatter.format("%.0f ", value);
//		System.out.format("%.0f ", value);
	}

	private void output(int index, double value)
	{
		/* skip single-valued attribute */
		if(feature_max[index] == feature_min[index])
			return;

		if(value == feature_min[index])
			value = lower;
		else if(value == feature_max[index])
			value = upper;
		else
			value = lower + (upper-lower) * 
				(value-feature_min[index])/
				(feature_max[index]-feature_min[index]);

		if(value != 0)
		{
			featureFormatter.format("%d:%f ", index, value);
//			System.out.format("%d:%f ", index, value);
			new_num_nonzeros++;
		}
	}

	private String readline(BufferedReader fp) throws IOException
	{
		line = fp.readLine();
		return line;
	}

	// modification to work in the cloud
	
	public void setData(byte[] unscaledData){
		this.unscaledData = unscaledData;
	}
	
//	don't need this yet
//	private void setRange(byte[] range){
//		this.rangeRestore = range;
//	}
	
	
	public void cloud_run(String []argv) throws IOException{
		int i,index;
		// read from byte array instead
		BufferedReader fp = Utility.byteArray2Reader(unscaledData);
		BufferedReader fp_restore = null;
		String save_filename = null;
		String restore_filename = null;
		String data_filename = null;


		for(i=0;i<argv.length;i++)
		{
			if (argv[i].charAt(0) != '-')	break;
			++i;
			switch(argv[i-1].charAt(1))
			{
				case 'l': lower = Double.parseDouble(argv[i]);	break;
				case 'u': upper = Double.parseDouble(argv[i]);	break;
				case 'y':
					  y_lower = Double.parseDouble(argv[i]);
					  ++i;
					  y_upper = Double.parseDouble(argv[i]);
					  y_scaling = true;
					  break;
				default:
					  System.err.println("unknown option");
					  exit_with_help();
			}
		}

		if(!(upper > lower) || (y_scaling && !(y_upper > y_lower)))
		{
			System.err.println("inconsistent lower/upper specification");
			System.exit(1);
		}

//		if(argv.length != i+1)
//			exit_with_help();
//
//		data_filename = argv[i];

		/* assumption: min index of attributes is 1 */
		/* pass 1: find out max index of attributes */
		max_index = 0;


		while (readline(fp) != null)
		{
			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");
			st.nextToken();
			while(st.hasMoreTokens())
			{
				index = Integer.parseInt(st.nextToken());
				max_index = Math.max(max_index, index);
				st.nextToken();
				num_nonzeros++;
			}
		}

		try {
			feature_max = new double[(max_index+1)];
			feature_min = new double[(max_index+1)];
		} catch(OutOfMemoryError e) {
			System.err.println("can't allocate enough memory");
			System.exit(1);
		}

		for(i=0;i<=max_index;i++)
		{
			feature_max[i] = -Double.MAX_VALUE;
			feature_min[i] = Double.MAX_VALUE;
		}

		fp = rewind(fp);

		/* pass 2: find out min/max value */
		while(readline(fp) != null)
		{
			int next_index = 1;
			double target;
			double value;

			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");
			target = Double.parseDouble(st.nextToken());
			y_max = Math.max(y_max, target);
			y_min = Math.min(y_min, target);

			while (st.hasMoreTokens())
			{
				index = Integer.parseInt(st.nextToken());
				value = Double.parseDouble(st.nextToken());

				for (i = next_index; i<index; i++)
				{
					feature_max[i] = Math.max(feature_max[i], 0);
					feature_min[i] = Math.min(feature_min[i], 0);
				}

				feature_max[index] = Math.max(feature_max[index], value);
				feature_min[index] = Math.min(feature_min[index], value);
				next_index = index + 1;
			}

			for(i=next_index;i<=max_index;i++)
			{
				feature_max[i] = Math.max(feature_max[i], 0);
				feature_min[i] = Math.min(feature_min[i], 0);
			}
		}

		fp = rewind(fp);

		/* pass 2.5: save feature_min/feature_max */
			rangeFormatter = new Formatter(new StringBuilder());
			rangeFormatter.format("x\n");
			rangeFormatter.format("%f %f\n", lower, upper);
			for(i=1;i<=max_index;i++)
			{
				if(feature_min[i] != feature_max[i]) 
					rangeFormatter.format("%d %f %f\n", i, feature_min[i], feature_max[i]);
//					System.out.format("%d %f %f\n", i, feature_min[i], feature_max[i]);
			}


		/* pass 3: scale */
		while(readline(fp) != null)
		{
			int next_index = 1;
			double target;
			double value;

			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");
			target = Double.parseDouble(st.nextToken());
			output_target(target);
			while(st.hasMoreElements())
			{
				index = Integer.parseInt(st.nextToken());
				value = Double.parseDouble(st.nextToken());
				for (i = next_index; i<index; i++)
					output(i, 0);
				output(index, value);
				next_index = index + 1;
			}

			for(i=next_index;i<= max_index;i++)
				output(i, 0);
			featureFormatter.format("\n");
//			System.out.format("\n");
			
		}
		if (new_num_nonzeros > num_nonzeros)
			System.err.print(
			 "WARNING: original #nonzeros " + num_nonzeros+"\n"
			+"         new      #nonzeros " + new_num_nonzeros+"\n"
			+"Use -l 0 if many original feature values are zeros\n");

		fp.close();
	}
	
	//scaled feature dataset for svm model training
	public byte[] get_scaled_features(){
		byte[] scaled_data = new byte[0];
		scaled_data = featureFormatter.toString().getBytes();
		
		return scaled_data;
	}
	//scaled feature dataset for svm model training
	public String get_scaled_features_string(){
			return featureFormatter.toString();
	}
	
	//range file to use with scaling test dataset
	public byte[] get_range()
	{
		byte[] range_data = new byte[0];
		range_data = rangeFormatter.toString().getBytes();
		
		return range_data;
	}
	
	private static void main(String argv[]) throws IOException
	{
		String[] args = new String[]{"-l","-1","-u","1"};
		svm_scale s = new svm_scale();
		//need to take out the part that read file locally out and see if the byte array actually go through
		//need also to get the byte array of the dataset
		BufferedReader fp=null;
		String data_filename = "training1_samples"; 
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
		s.setData(result.toString().getBytes());
		
		s.cloud_run(args);
//		System.out.println("");
		
//      s.fp = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("scaled_dataset")));
//      s.fp.write(s.get_scaled_features());
//      s.fp.close();
//      
//      s.fp = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("scaling_range")));
//      s.fp.write(s.get_range());
//      s.fp.close();
      
      
	}
}
