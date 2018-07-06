import java.util.List;

public class Artical {
	private List<String> wordList;
	private String type;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getWordList() {
		return wordList;
	}
	public void setWordList(List<String> wordList) {
		this.wordList = wordList;
	}
	@Override
	public String toString() {
		return "Artical [wordList=" + wordList + ", type=" + type + "]";
	}
}
