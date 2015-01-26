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
		pairCounts = new HashMap<HashSet<String>,Integer>();
		tripleCounts = new HashMap<HashSet<String>,Integer>();
		threshold = 100;
		firstPass();
		//System.out.println("1st Candidates: " + singleCounts.size());
		pruneSingles();
		//System.out.println("1st Official: " + singleCounts.size());
		secondPass();
		//System.out.println("2nd Candidates: " + pairCounts.size());
		prunePairs();
		//System.out.println("2nd Official: " + pairCounts.size());
		thirdPass();
		//System.out.println("3rd Candidates: " + tripleCounts.size());
		pruneTriples();
		//System.out.println("3rd Official: " + tripleCounts.size());
		System.out.println("Finished");
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
	//All used in the second step of the algorithm
	private static HashSet<HashSet<String>> makePairs(String value){
	   String[] ar = value.split("\\s+");
	   HashSet<HashSet<String>> basketPairs = new HashSet<HashSet<String>>();
	   int size = ar.length;
	   for(int i = 0; i < size; i++){
		   String item1 = ar[i];
		   for(int j = 0; j < size; j++){
			   if(i!=j){
				   String item2 = ar[j];
				   //Both items are frequent
				   if(singleCounts.containsKey(item1) && singleCounts.containsKey(item2)){
					   HashSet<String> pair = new HashSet<String>(2);
					   pair.add(item1);
					   pair.add(item2);
					   basketPairs.add(pair);
				   }
			   }
		   }
	   }
	   return basketPairs;
	}
	private static void countPairs(String value){
		HashSet<HashSet<String>> basketPairs = makePairs(value);
		for(HashSet<String> pair: basketPairs){
			if(pairCounts.containsKey(pair)){
				pairCounts.put(pair, pairCounts.get(pair)+1);
			}else{
				pairCounts.put(pair,1);
			}
		}
   	}
	private static void prunePairs(){
		Iterator<Map.Entry<HashSet<String>,Integer>> iter = pairCounts.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry<HashSet<String>,Integer> entry = iter.next();
		    if(entry.getValue() < threshold){
		        iter.remove();
		    }
		}
	}
	private static void secondPass() throws IOException{
		// Construct BufferedReader from FileReader
		File fi = new File(inputFile);
		BufferedReader br = new BufferedReader(new FileReader(fi));
		String line = null;
		while ((line = br.readLine()) != null) {
			countPairs(line);
		}
		br.close();
	}
	//All used in the second step of the algorithm
	//All used in the third step of the algorithm
	private static HashSet<HashSet<String>> makeTriples(String value){
		HashSet<HashSet<String>> basketTriples = new HashSet<HashSet<String>>();
		HashSet<HashSet<String>> basketPairs = makePairs(value);
		for(HashSet<String> pair1: basketPairs){
			for(HashSet<String> pair2: basketPairs){
				if(!pair2.equals(pair1) && pairCounts.containsKey(pair1) && pairCounts.containsKey(pair2)){
					HashSet<String> triple = new HashSet<String>(4);
					triple.addAll(pair1);
					triple.addAll(pair2);
					//A very crude way of making sure the pairs have at least 1 element in common
					if(triple.size() == 3){
						basketTriples.add(triple);
					}
				}
			}
		}
		return basketTriples;
	}
	private static void countTriples(String value){
		HashSet<HashSet<String>> basketTriples = makeTriples(value);
		for(HashSet<String> triple: basketTriples){
			if(tripleCounts.containsKey(triple)){
				tripleCounts.put(triple, tripleCounts.get(triple)+1);
			}else{
				tripleCounts.put(triple,1);
			}
		}
   	}
	private static void pruneTriples(){
		Iterator<Map.Entry<HashSet<String>,Integer>> iter = tripleCounts.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry<HashSet<String>,Integer> entry = iter.next();
		    if(entry.getValue() < threshold){
		        iter.remove();
		    }
		}
	}
	private static void thirdPass() throws IOException{
		// Construct BufferedReader from FileReader
		File fi = new File(inputFile);
		BufferedReader br = new BufferedReader(new FileReader(fi));
		String line = null;
		while ((line = br.readLine()) != null) {
			countTriples(line);
		}
		br.close();
	}
	//All used in the third step of the algorithm
	
	//Rules for itemsets of 2
	class PairRule{
		double confidence;
		HashSet<String> left;
		HashSet<String> right;
		
		public PairRule(HashSet<String> left, HashSet<String> right){
			this.left = left;
			this.right = right;
			computeConfidence();
		}
		
		private void computeConfidence(){
			HashSet<String> pair = new HashSet<String>(2);
			pair.addAll(left);
			pair.addAll(right);
			int countPair = pairCounts.get(pair);
			int leftCount = singleCounts.get(left.toArray()[0]);
			this.confidence = (double)countPair/(double)leftCount;
		}
		
		@Override
		public String toString(){
			return null;
		}
	}
	
	//Rules for itemsets of 3
	class TripleRule{
		double confidence;
		HashSet<String> left;
		HashSet<String> right;
		
		public TripleRule(HashSet<String> left, HashSet<String> right){
			this.left = left;
			this.right = right;
			computeConfidence();
		}
		
		private void computeConfidence(){
			HashSet<String> triple = new HashSet<String>(3);
			triple.addAll(left);
			triple.addAll(right);
			int countPair = pairCounts.get(left);
			int countTriple = tripleCounts.get(triple);
			this.confidence = (double)countTriple/(double)countPair;
		}
		
		@Override
		public String toString(){
			return null;
		}
	}
	
	//This is just the model for reading the file
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
