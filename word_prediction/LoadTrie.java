import java.util.*;

public class LoadTrie {
	public static void main(String[] args) { 

		Trie trie = new Trie();
		String[] words = {"the", "be", "and"};

		for (String word : words) {
			trie.load(word);
		}
	}
}