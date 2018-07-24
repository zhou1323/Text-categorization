package textProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;
import com.mysql.cj.xdevapi.JsonArray;

import classification.WordUtil;
import dao.TextDAO;
import vo.Category;
import vo.Term;

public class Pretreatment {
	//分词
	public HashMap<String, Integer> textParse(String originalString) {
		JiebaSegmenter segmenter = new JiebaSegmenter();
		WordUtil filter = new WordUtil(utils.Properties.stopWordsPos);
		List<String> wordList = filter.filter(segmenter.process(originalString, SegMode.INDEX));
		HashMap<String, Integer> results = filter.countWordFry(wordList);
		return results;
	}

	//读取文本
	public String getDatafromFile(String fileName) {
		String Path = utils.Properties.dataSetPos+fileName+".json";
		
		BufferedReader reader = null;
		String laststr = "";
		try {
			FileInputStream fileInputStream = new FileInputStream(Path);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, Charset.forName("UTF-8"));
			reader = new BufferedReader(inputStreamReader);
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				laststr += tempString;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return laststr;
	}
}
