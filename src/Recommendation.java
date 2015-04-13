import java.io.*;
import java.util.*;

public class Recommendation {
	public static HashMap<String,Integer> singleCounts;
	public static HashMap<HashSet<String>,Integer> pairCounts;
	public static HashMap<HashSet<String>,Integer> tripleCounts;
	public static Recommendation.RuleArray pairRules;
	public static Recommendation.RuleArray tripleRules;
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
		pairRules = new RuleArray();
		tripleRules = new RuleArray();
		threshold = 100;
		System.out.println("First Pass");
		firstPass();
		pruneSingles();
		System.out.println("Second Pass");
		secondPass();
		prunePairs();
		System.out.println("Third Pass");
		thirdPass();
		pruneTriples();
		System.out.println("Computing pair rules");
		computePairRules();
		pairRules.printAr();
		System.out.println("Computing triple rules");
		computeTripleRules();
		tripleRules.printAr();
		System.out.println("Finished");
	}
	
	//All used in the first step of the algorithm
	private static void countSingles(String line){
		String[] ar = line.split("\\s+");
		int size = ar.length;
		HashSet<String> dups = new HashSet<String>(size);
		for(int i = 0; i < size; i++){
			String key = ar[i];
			if(!dups.contains(key)){
				dups.add(key);
				if(singleCounts.containsKey(key)){
					singleCounts.put(key,singleCounts.get(key)+1);
				}else{
					singleCounts.put(key,1);
				}
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
	//Used to compute the pair rules
	private static void computePairRules(){
		Iterator<Map.Entry<HashSet<String>,Integer>> iter = pairCounts.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry<HashSet<String>,Integer> entry = iter.next();
		    String[] pair = new String[2];
		    pair = entry.getKey().toArray(pair);
		    HashSet<String> a = new HashSet<String>(1);
		    HashSet<String> b = new HashSet<String>(1);
		    a.add(pair[0]);
		    b.add(pair[1]);
		    PairRule rule1 = new PairRule(a,b);
		    PairRule rule2 = new PairRule(b,a);
		    pairRules.addRule(rule1);
		    pairRules.addRule(rule2);
		}
	}
	//Used to compute the triple rules
	private static void computeTripleRules(){
		Iterator<Map.Entry<HashSet<String>,Integer>> iter = tripleCounts.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry<HashSet<String>,Integer> entry = iter.next();
		    HashSet<String> triple = entry.getKey();
		    for(String key1: triple){
		    	for(String key2: triple){
		    		if(!key1.equals(key2)){
		    			for(String key3: triple){
		    				if(!key2.equals(key3) && !key1.equals(key3)){
		    					HashSet<String> left = new HashSet<String>(2);
		    					left.add(key1);
		    					left.add(key2);
		    					HashSet<String> right = new HashSet<String>(1);
		    					right.add(key3);
		    					TripleRule rm = new TripleRule(left,right);
		    					tripleRules.addRule(rm);
		    				}
		    			}
		    		}
		    	}
		    }
		    
		    String[] pair = new String[2];
		    pair = entry.getKey().toArray(pair);
		    HashSet<String> a = new HashSet<String>(1);
		    HashSet<String> b = new HashSet<String>(1);
		    a.add(pair[0]);
		    b.add(pair[1]);
		    PairRule rule1 = new PairRule(a,b);
		    PairRule rule2 = new PairRule(b,a);
		    pairRules.addRule(rule1);
		    pairRules.addRule(rule2);
		}
	}
	
	

	public static class RuleArray{
		ArrayList<Rule> topRules;
		int maxRules = 5;
		public RuleArray(){
			topRules = new ArrayList<Rule>(5);
		}
		
		public void addRule(Rule rule){
			if(topRules.contains(rule)) return;
			if(topRules.size() < maxRules){
				topRules.add(rule);
			}else{
				int index = -1;
				double minConf = Integer.MAX_VALUE;
				for(int i = 0; i < maxRules; i++){
					Rule obj = topRules.get(i);
					if(obj.getConfidence() < minConf){
						minConf = obj.getConfidence();
						index = i;
					}
					if(obj.getConfidence() == minConf){
						Rule obj2 = topRules.get(index);
						if(obj.toString().compareTo(obj2.toString()) > 0){
							minConf = obj.getConfidence();
							index = i;
						}
					}					
				}
				if(index != -1 && minConf < rule.getConfidence()){
					topRules.set(index,rule);
				}
				if(index != -1 && minConf == rule.getConfidence()){
					Rule obj = topRules.get(index);
					if(obj.toString().compareTo(rule.toString()) > 0){
						topRules.set(index,rule);
					}
				}
			}
		}
	
		public void printAr(){
			for(int i = 0; i < 5; i++){
				Rule rule = topRules.get(i);
				System.out.println(rule.toString());
			}
		}
	}
	public static interface Rule{
		public double getConfidence();
		public String toString();
	}
	//Rules for itemsets of 2
	public static class PairRule implements Rule{
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
			String[] leftStr = new String[1];
			leftStr = left.toArray(leftStr);
			int leftCount = singleCounts.get(leftStr[0]);
			this.confidence = (double)countPair/(double)leftCount;
		}
		
		public double getConfidence(){
			return confidence;
		}
		
		@Override
		public String toString(){
			return left.toString() + " => " + right.toString() + " Confidence: " + confidence;
		}
		
		@Override
		public boolean equals(Object obj){
			if (!(obj instanceof PairRule))
	            return false;
	        if (obj == this)
	            return true;
	        PairRule pr = (PairRule) obj;
	        if(pr.left.equals(this.left) && pr.right.equals(this.right) && this.confidence == pr.confidence){
	        	return true;
	        }else{
	        	return false;
	        }
		}
	}
	//Rules for itemsets of 3
	public static class TripleRule implements Rule{
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
		
		public double getConfidence(){
			return confidence;
		}
		
		@Override
		public String toString(){
			return left.toString() + " => " + right.toString() + " Confidence: " + confidence;
		}
	
		public boolean equals(Object obj){
			if (!(obj instanceof TripleRule))
	            return false;
	        if (obj == this)
	            return true;
	        TripleRule pr = (TripleRule) obj;
	        if(pr.left.equals(this.left) && pr.right.equals(this.right) && this.confidence == pr.confidence){
	        	return true;
	        }else{
	        	return false;
	        }
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
