import java.io.*;
import java.util.*;

public class Recommendation {
	public static HashMap<String,Integer> singleCounts;
	public static HashMap<HashSet<String>,Integer> pairCounts;
	public static HashMap<HashSet<String>,Integer> tripleCounts;
	public static String inputFile;
	public static String outputFile;
	public static int threshold;
	
	public static void main(String[] args) throws Exception {
		//Set the files
		inputFile = args[0];
		outputFile = args[1];
		//Set the class variable
		singleCounts = new HashMap<String,Integer>();
		threshold = 100;
		firstPass();
		pruneSingles();
	}
	
	//All used in the first step of the algorithm
	private static void countSingles(String line){
		String[] ar = line.split("\\s+");
		int size = ar.length;
		for(int i = 0; i < size; i++){
			String key = ar[i];
			if(singleCounts.containsKey(key)){
				singleCounts.put(key,singleCounts.get(key)+1);
			}else{
				singleCounts.put(key,1);
			}
		}
	}

	private static void pruneSingles(){
		Iterator<Map.Entry<String,Integer>> iter = singleCounts.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry<String,Integer> entry = iter.next();
		    if(entry.getValue() < threshold){
		        iter.remove();
		    }
		}
	}

	private static void firstPass() throws IOException{
		// Construct BufferedReader from FileReader
		File fi = new File(inputFile);
		BufferedReader br = new BufferedReader(new FileReader(fi));
		String line = null;
		while ((line = br.readLine()) != null) {
			countSingles(line);
		}
		br.close();
	}
	//All used in the first step of the algorithm
	
	
	private static void makePairs(String value){
	   String[] ar = value.split("\\s+");
	   int size = ar.length;
	   if(size < 2) return;
	   for(int i = 0; i < size; i++){
		   String item1 = ar[i];
		   for(int j = 0; j < size; j++){
			   if(i != j){
				   String item2 = ar[j];
				   //Both items are frequent
				   if(singleCounts.containsKey(item1) && singleCounts.containsKey(item2)){
					   HashSet<String> pair = new HashSet<String>(2);
					   if(pairCounts.containsKey(pair)){
			            	pairCounts.put(pair, pairCounts.get(pair)+1);
			            }else{
			            	pairCounts.put(pair,1);
			            }
				   }
			   }
		   }
	   }
   }
	
	private static void readFile() throws IOException{
		// Construct BufferedReader from FileReader
		File fi = new File(inputFile);
		BufferedReader br = new BufferedReader(new FileReader(fi));
		String line = null;
		while ((line = br.readLine()) != null) {
			System.out.println(line);
		}
		br.close();
	}
}
