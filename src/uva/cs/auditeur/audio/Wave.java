package uva.cs.auditeur.audio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;



/**
 * Read WAVE headers and data from wave input stream
 * 
 * @author Jacquet Wong
 */
public class Wave implements Serializable{

        private static final long serialVersionUID = 1L;
        private WaveHeader waveHeader;
        private byte[] data;    // little endian
        
       

        /**
         * Constructor
         * 
         */
        public Wave() {
                this.waveHeader=new WaveHeader();
                this.data=new byte[0];
        }
        
        public Wave(String filename) {
    		try {
    			InputStream inputStream = new FileInputStream(filename);
    			initWaveWithInputStream(inputStream);
    			inputStream.close();
    		} catch (FileNotFoundException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
        
        private void initWaveWithInputStream(InputStream inputStream) {
    		// reads the first 44 bytes for header
    		waveHeader = new WaveHeader(inputStream);
    		
    		if (waveHeader.isValid()) {
    			// load data
    			try {
    				data = new byte[inputStream.available()];
    				inputStream.read(data);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    			// end load data
    		} else {
    			System.err.println("Invalid Wave Header");
    		}
    	}

        


        /**
         * Constructor
         * 
         * @param WaveHeader
         *            waveHeader
         * @param byte[]
         *            data
         */
        public Wave(WaveHeader waveHeader, byte[] data) {
                this.waveHeader = waveHeader;
                this.data = data;
        }
        
      
        public WaveHeader getWaveHeader() {
                return waveHeader;
        }
        
      
        /**
         * Get the wave data in bytes
         * 
         * @return wave data
         */
        public byte[] getBytes() {
                return data;
        }

        /**
         * Data byte size of the wave excluding header size
         * 
         * @return byte size of the wave
         */
        public int size() {
                return data.length;
        }
        
        /**
         * Length of the wave in second
         * 
         * @return length in second
         */
        public float length() {
                float second = (float) waveHeader.getSubChunk2Size() / waveHeader.getByteRate();
                return second;
        }

        /**
         * Timestamp of the wave length
         * 
         * @return timestamp
         */
        public String timestamp() {
                float totalSeconds = this.length();
                float second = totalSeconds % 60;
                int minute = (int) totalSeconds / 60 % 60;
                int hour = (int) (totalSeconds / 3600);

                StringBuffer sb = new StringBuffer();
                if (hour > 0) {
                        sb.append(hour + ":");
                }
                if (minute > 0) {
                        sb.append(minute + ":");
                }
                sb.append(second);

                return sb.toString();
        }

        /**
         * Get the amplitudes of the wave samples (depends on the header)
         * 
         * @return amplitudes array (signed 16-bit)
         */
        
        
        public double[] getSampleAmplitudes(){
            int bytePerSample = waveHeader.getBitsPerSample() / 8;
            int numSamples = data.length / bytePerSample;
            double[] amplitudes = new double[numSamples];
            
            int pointer = 0;
            for (int i = 0; i < numSamples; i++) {
                    short amplitude = 0;
                    for (int byteNumber = 0; byteNumber < bytePerSample; byteNumber++) {
                            // little endian
                            amplitude |= (short) ((data[pointer++] & 0xFF) << (byteNumber * 8));
                    }
                    amplitudes[i] = amplitude;
            }
            
            return amplitudes;
    }
        
        
        public String toString(){
                StringBuffer sb=new StringBuffer(waveHeader.toString());
                sb.append("\n");
                sb.append("length: " + timestamp());
                return sb.toString();
        }
        
//        public static void main(String[] args) {
//        	 String path = "/Users/Karn/Documents/uva/MFCCTesting/whatFood.wav";
//        	 System.out.println("hello world");
//        	 Wave wav = new Wave(path);
//        	 System.out.println(wav);
//        	 
//        	 
//        	 
//    	}
        
        
        
        

}
