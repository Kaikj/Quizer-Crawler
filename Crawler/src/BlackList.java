public class BlackList {
	// the dictionary for children sensitive/"awkward" words that may appear in News.(Self defined)
	//String[] is used instead of Hash such that 
	private final static String[] BLACKLIST = { "bomb","body","behead","drug", "dope","die","death","gay","genital", "harrass","homosexual",
			"kidnap", "kill","kick", "loanshark", "molest", "murder","mutilate","punch","porn", "rape", "rob",
			"sex", "steal","terror" };
	
	public static boolean isBlacklisted(String word){
		boolean result = false;
		for(String s: BLACKLIST){
			if(word.toLowerCase().contains(s)){
				result = true;
			}
		}
		return result;
	}
}
