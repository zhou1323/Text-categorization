import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.huaban.analysis.jieba.SegToken;

public class WordUtil {
	
	private List<String> stopWords;
	public WordUtil(String path) {
		this.stopWords = new ArrayList<>();
		File file = new File(path);
		String line;
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(file));
			while((line=bReader.readLine())!=null) {
				this.stopWords.add(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public List<String> getStopWords() {
		return stopWords;
	}
	public void setStopWords(List<String> stopWords) {
		this.stopWords = stopWords;
	}
	
	public List<String> filter(List<SegToken> list){
		List<String> resultList = new ArrayList<>();
		for(SegToken s:list) {
			if(!this.stopWords.contains(s.word)) {
				resultList.add(s.word);
			}
		}
		return resultList;
	}
	
	public HashMap<String, Integer> countWordFry(List<String> wordList){
		HashMap<String, Integer> map = new HashMap<>();
		for(String s:wordList) {
			map.put(s, Collections.frequency(wordList, s));
		}
		return map;
	}
}
