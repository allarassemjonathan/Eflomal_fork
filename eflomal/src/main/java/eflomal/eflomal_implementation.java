package eflomal;

import java.util.ArrayList;
import java.util.Random;
import java.util.Map;
import java.util.TreeMap;


public class eflomal_implementation {
private String filename = "piglatin_v2.zip";
public TreeMap<Integer,ArrayList<Integer>> links;
public TreeMap<Pair, Float> counts;
private SentencePairReader reader;

public void initialize_() {
	
}
public eflomal_implementation() {
	
/**
 *  * There is one links array per src-tgt sentence pair
 * 
 * Each links array is of the same length as the target sentence
 * 
 * Let i be a sentence pair index in the translation corpus
 * Let j be the index of a word in the target sentence of that pair
 * links[i][j] is the index of the word in the source sentece of that pair
 *             which is responsible for the presence of word j
 * 
 *   Initializing links
     * for every pair
     * 	grab the target 
     * 		create a list
     * 		for every word in the target 
     * 			generate a number between 1 
     * 			and the length of source sentence
     * 
 */
	Random rand = new Random();
	reader = new SentencePairReader(filename);
	this.links = new TreeMap<Integer, ArrayList<Integer>>();
	
	//cash source and target tokens separately for later use
	ArrayList<ArrayList<Integer>> targets_tokens = new ArrayList<>();
	ArrayList<ArrayList<Integer>> sources_tokens = new ArrayList<>();
	
	// this will take care of the index of the pair
	int pair_index = 0;

	while(reader.hasNext()) {
		
		// get the target sentence in the pair
		SentencePair pair = reader.next();
		Sentence target = pair.getSecond();
		Sentence source = pair.getFirst();
		
		//cash the target/source tokens for later. we will use the index_pair
		//to iterate through it
		targets_tokens.add(target.getTokens());
		sources_tokens.add(source.getTokens());
		
		// this holds the index of the tokens responsible for the target word
		ArrayList<Integer> source_ids = new ArrayList<>();
		
		for (int i=0; i< target.getTokens().size(); i++) {
			// the num will refer to an index in the list of source words
			// but responsible for a target word
			int num = rand.nextInt(pair.getSource().getTokens().size());
			
			// add that random source index into the list of source indexe
			// responsible for target indexes
			source_ids.add(num);
		}
		// links add the source tokens
		this.links.put(pair_index, source_ids);
		pair_index++;
	}
	
	System.out.print(this.links);
	
	/**Initializing counts
	 * There is one entry for every non-zero mapping from a source word to its 
	 * consequential target word. All missing entries are assumed to be 0.
	 * 
	 * An entry of 57 at index Pair(66, 42) means that the word represented by  
	 * the word token 66 is attributed to the word represented by the word token 
	 * 42 a total of 57 times accross the corpus.
	 * counts = new Map<Pair<target, source>, counts>()
	 * counts = new Map<Pair<uint32, uint32>, uint32>()
     */
	
	 //targets_tokens.size() should be  equal to pair_index
	 this.counts  = new TreeMap<Pair, Float>();
	 for (int i = 0;i< pair_index ; i++) {
		 // get the source of index of tokens explaining targets for the pair i
		 ArrayList<Integer> indexes_source = links.get(i);
		 
		 // get the entire source sentence with the tokens
		 ArrayList<Integer> temp_source_tokens = sources_tokens.get(i);
		 
		 // initialize the array that will hold all the tokens explaining target
		 // words instead of their indices in the actual source pair (temp_source_tokens)
		 ArrayList<Integer> explaining_source_tokens = new ArrayList<>();
		 
		 // for each guess in the indexes of source tokens in links find the actual source token
		 for (int k = 0; k < indexes_source.size(); k++) {
			 // using the source ids, index a token in the source sentence to explain target token k
			 explaining_source_tokens.add(temp_source_tokens.get(indexes_source.get(k)));
		 }
		 
		 // get the target set of tokens
		 ArrayList<Integer> explained_target_tokens = targets_tokens.get(i);
		 
		 // update the counts using the explaining tokens and explained tokens
		 for(int k = 0; k < explaining_source_tokens.size(); k++) {
			 //create a key pair (target_token, source_token) since there is a match (k-th elements match)
			 Pair key = new Pair(explained_target_tokens.get(k), explaining_source_tokens.get(k));
			 if (counts.containsKey(key)) {
				 counts.put(key,1+counts.get(key));
			 }
			 else {
				 counts.put(key, (float)1);
			 }
		 } 
	 }
	 //System.out.print(counts);
}

public static void main(String []args) {
	System.out.print("testing");
	eflomal_implementation e = new eflomal_implementation();

	System.out.println("links " + e.links);
	System.out.println("links " + e.counts);
	
}

}
