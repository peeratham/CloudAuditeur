package uva.cs.auditeur.cloud;

import java.util.Arrays;

import uva.cs.auditeur.classifier.svm.SVMDataSetGenerator;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;


public class WavFeature {
	private byte[] data;
	private BlobKey blobKey;
	private Wave wav;
	
	public WavFeature(BlobKey blobKey){
		this.blobKey = blobKey;
		this.data = Utility.readBlob(this.blobKey);
		this.wav = getWavFromBlob(data);
		
//        System.out.println("successfully create wav from blob");
	}
	

	public byte[] getData() {
		return this.data;
	}
	
	public Wave getWav() {
		return this.wav;
	}
	

//	public static byte[] readBlob(BlobKey blobKey){
//		  
//		 BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
//		 BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
//		  
//		 if( blobInfo == null )
//		  return null;
//		  
//		 if( blobInfo.getSize() > Integer.MAX_VALUE )
//		  throw new RuntimeException("This method can only process blobs up to " + Integer.MAX_VALUE + " bytes");
//		  
//		 int blobSize = (int)blobInfo.getSize();
//		 int chunks = (int)Math.ceil(((double)blobSize / BlobstoreService.MAX_BLOB_FETCH_SIZE));
//		 int totalBytesRead = 0;
//		 int startPointer = 0;
//		 int endPointer;
//		 byte[] blobBytes = new byte[blobSize];
//		  
//		 for( int i = 0; i < chunks; i++ ){
//
//			 endPointer = Math.min(blobSize - 1, startPointer + BlobstoreService.MAX_BLOB_FETCH_SIZE - 1);
//
//			 byte[] bytes = blobstoreService.fetchData(blobKey, startPointer, endPointer);
//
//			 for( int j = 0; j < bytes.length; j++ )
//				 blobBytes[j + totalBytesRead] = bytes[j];
//
//			 startPointer = endPointer + 1;
//			 totalBytesRead += bytes.length;
//		 }
////		 System.out.println("byte length: "+blobBytes.length);
//		 return blobBytes;
//		}
	
	private Wave getWavFromBlob(byte[] wavBlob){
		byte[] hdBytes = Arrays.copyOfRange(wavBlob, 0, 45);
		WaveHeader whd = new WaveHeader(hdBytes);
		byte[] data = Arrays.copyOfRange(wavBlob, 45,wavBlob.length);
		return new Wave(whd, data);
	}
	
	//do something with the byte data of .wav file
	public float getAvgAmp(){
		float result = 0;
       double[] amplitudes = wav.getSampleAmplitudes();
       int total_amp = 0;
       for(int i = 0; i < amplitudes.length; i++){
       	total_amp += amplitudes[i];
       }
    	result = (float)total_amp/amplitudes.length;
       return result;
	}
	
	public float getDuration(){
		return this.wav.length();
	}
	
	public int getSampleRate(){
		return this.getWav().getWaveHeader().getSampleRate();
	}
	
	public StringBuilder getAudioFeature(){
		int Fs = getSampleRate();
		double[] inputSignal = wav.getSampleAmplitudes();
		StringBuilder sb = SVMDataSetGenerator.generateAudioFeatureFromByteSignal(inputSignal, Fs);
		return sb;
	}
	
	
	
	
}