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

import classification.Term;
import classification.Category;
import classification.WordUtil;
import dao.TextDAO;

public class Pretreatment {
	/**
	 * 分词，英文的分词相比中文的分词要简单很多，这里使用的分隔符为除单词、数字外的任意字符串 如果使用中文，则可以使用中科院的一套分词系统，分词效果还算不错
	 * 
	 * @param originalString
	 * @return
	 * @return
	 */
	public HashMap<String, Integer> textParse(String originalString) {
		JiebaSegmenter segmenter = new JiebaSegmenter();
		WordUtil filter = new WordUtil("E:\\data\\stopWords.txt");
		List<String> wordList = filter.filter(segmenter.process(originalString, SegMode.INDEX));
		HashMap<String, Integer> results = filter.countWordFry(wordList);
		return results;
	}

	public List<Category> initDataSet() {
		List<Category> categories=new ArrayList<Category>();
		Pretreatment pt=new Pretreatment();
		try {
			//总新闻篇数
			int total=0;
			for (int i = 0; i < utils.Properties.newsType.length; i++) {
				String type=utils.Properties.newsType[i];
				String jsonString=pt.getDatafromFile(type);
				JSONArray news=new JSONObject(jsonString).getJSONArray(type);
				total+=news.length();
			}
			System.out.println("The total num is "+total);
			//封装每类
			for (int i = 0; i < utils.Properties.newsType.length; i++) {
				Category category=new Category();
				//总词数
				int termNum=0;
				HashMap<String, Integer> terms=new HashMap<String,Integer>();
				
				String type=utils.Properties.newsType[i];
				String jsonString=pt.getDatafromFile(type);
				JSONArray news=new JSONObject(jsonString).getJSONArray(type);
				double possibility=(double)news.length()/total;
				
				System.out.println(i+ " possibility is "+possibility);
				//遍历文章
				for (int j=0;j<news.length();j++) {
					JSONObject artical=news.getJSONObject(j);
					
					//遍历词
					JSONArray termsArray=artical.getJSONArray("terms");
					for(int k=0;k<termsArray.length();k++) {
						JSONObject term=termsArray.getJSONObject(k);
						String termName=term.getString("term");
						int count=term.getInt("count");
						termNum+=count;
						if(terms.containsKey(termName)) {
							terms.put(termName, terms.get(termName) + count);
						}
						else {
							terms.put(termName, count);
						}
					}
				}
				category.setPossibility(possibility);
				category.setTerms(terms);
				category.setTermNum(termNum);
				category.setType(type);
				System.out.println(category.getType()+" has initialized!");
				categories.add(category);
			}
			return categories;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	public void saveCategoriesToFile(List<Category> categories) {
		JSONArray array=new JSONArray();
		for (Category category : categories) {
			JSONObject temp=new JSONObject(category);
			array.put(temp);
		}
		String json=array.toString();
		saveDataToFile("vec", json);
	}
	
	public void process2() {
		List<Category> categories=initDataSet();
		saveCategoriesToFile(categories);
	}
	/**
	 * 将所有文本向量化
	 * 
	 * @param text
	 */
	public HashMap<String, Integer> text2vec(List<String> texts) {
		HashMap<String, Integer> result = new HashMap<String, Integer>();

		for (String text : texts) {
			HashMap<String, Integer> vecForArtical = textParse(text);
			for (String term : vecForArtical.keySet()) {
				if (result.containsKey(term)) {
					result.put(term, result.get(term) + vecForArtical.get(term));
				} else {
					result.put(term, vecForArtical.get(term));
				}
			}
		}
		return result;
	}

	public JSONObject vec2JSON(HashMap<String, Integer> vecs) {
		JSONObject termsObject = new JSONObject();
		int totalNum = 0;
		JSONArray termArray = new JSONArray();
		for (String term : vecs.keySet()) {
			JSONObject node = new JSONObject();
			node.put("term", term);
			node.put("count", vecs.get(term));
			totalNum += vecs.get(term);
			termArray.put(node);
		}
		termsObject.put("totalNum", totalNum);
		termsObject.put("detail", termArray);
		return termsObject;
	}

	public void process() {
		TextDAO textDAO = new TextDAO();
		JSONObject results = new JSONObject();
		try {
			for (int i = 0; i < utils.Properties.newsType.length; i++) {
				String type = utils.Properties.newsType[i];
				List<String> news = textDAO.getTextByType(type);
				JSONObject terms = vec2JSON(text2vec(news));

				JSONObject temp = new JSONObject();
				temp.put("possibility", textDAO.getTextPossibilityByType(type));
				temp.put("terms", terms);
				results.put(type, temp);

				System.out.println(type + " has finished!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String jsonString = results.toString();
		saveDataToFile("terms", jsonString);
	}

	private void saveDataToFile(String fileName, String data) {
		BufferedWriter writer = null;
		File file = new File("E:\\data\\" + fileName + ".json");
		// 如果文件不存在，则新建一个
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 写入
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8"));
			writer.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("文件写入成功！");
	}

	
	public String getDatafromFile(String fileName) {
		String Path = "E:\\data\\" + fileName + ".json";
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
	
	public JSONObject getTermsByType(String type) {
		String temp = getDatafromFile("terms");
		JSONObject json = new JSONObject(temp);
		JSONObject result = (JSONObject) json.get(type);
		return result;
	}
}
