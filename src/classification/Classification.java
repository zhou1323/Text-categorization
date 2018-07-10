package classification;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.sql.PseudoColumnUsage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.lang.model.element.VariableElement;

import org.json.JSONArray;
import org.json.JSONObject;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;

import dao.CateDAO;
import dao.TextDAO;
import textProcess.Pretreatment;

public class Classification {
	// 各类新闻中每个词出现的概率
	private List<HashMap<String, Double>> pVecs = new ArrayList<HashMap<String, Double>>();
	// 各类新闻出现的概率
	private double[] ratios = new double[utils.Properties.newsType.length];

	public HashMap<String, Integer> initTestSet(String file) {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		Pretreatment pt = new Pretreatment();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream("E:\\军事\\"+file+".txt"),Charset.forName("GBK")));
			StringBuilder sb1 = new StringBuilder();
			String string = null;
			while ((string = br.readLine()) != null) {
				sb1.append(string);
			}
			HashMap<String, Integer> temp = pt.textParse(sb1.toString());
			// 筛选tdidf较大的词
			//List<String> words = pickWordsByTdidf(temp);
			//for (String word : words) {
			//	result.put(word, temp.get(word));
			//}
			return temp;
			//return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public List<String> pickWordsByTdidf(HashMap<String, Integer> terms) {
		List<String> result = new ArrayList<String>();
		Pretreatment pt = new Pretreatment();
		String baseJson = pt.getDatafromFile("wordBase");
		JSONObject base = new JSONObject(baseJson);

		int totalNum = 0;
		for (Integer count : terms.values()) {
			totalNum += count;
		}

		HashMap<String, Double> temp = new HashMap<String, Double>();

		for (String term : terms.keySet()) {
			if (base.has(term)) {
				double td = (double) terms.get(term) / totalNum;
				double tdidf = td * base.getJSONObject(term).getDouble("idfvalue");
				temp.put(term, tdidf);
			}
		}

		// 将map.entrySet()转换成list
		List<HashMap.Entry<String, Double>> list = new ArrayList<HashMap.Entry<String, Double>>(temp.entrySet());
		Collections.sort(list, new Comparator<HashMap.Entry<String, Double>>() {
			// 降序排序
			@Override
			public int compare(HashMap.Entry<String, Double> o1, HashMap.Entry<String, Double> o2) {
				// return o1.getValue().compareTo(o2.getValue());
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		for (int i = 0; i < (list.size() > 30 ? 30 : list.size()); i++) {
			HashMap.Entry<String, Double> mapping = list.get(i);
			System.out.println(mapping.getKey());
			result.add(mapping.getKey());
		}
		return result;
	}

	/**
	 * 初始化数据集
	 * 
	 * @return
	 */
	public List<Category> initDataSet() {
		List<Category> categories = new ArrayList<Category>();
		Pretreatment pt = new Pretreatment();
		try {
			// 总新闻篇数
			int total = 0;
			for (int i = 0; i < utils.Properties.newsType.length; i++) {
				String type = utils.Properties.newsType[i];
				String jsonString = pt.getDatafromFile(type);
				JSONArray news = new JSONObject(jsonString).getJSONArray(type);
				total += news.length();
			}
			// 封装每类
			for (int i = 0; i < utils.Properties.newsType.length; i++) {
				Category category = new Category();
				// 总词数
				int termNum = 0;
				HashMap<String, Integer> terms = new HashMap<String, Integer>();

				String type = utils.Properties.newsType[i];
				String jsonString = pt.getDatafromFile(type);
				JSONArray news = new JSONObject(jsonString).getJSONArray(type);
				double possibility = (double) news.length() / total;

				// 遍历文章
				for (int j = 0; j < news.length(); j++) {
					JSONObject artical = news.getJSONObject(j);
					
					int totalTerm=artical.getInt("total");
					termNum+=totalTerm;
					
					// 遍历词
					JSONArray termsArray = artical.getJSONArray("terms");
					for (int k = 0; k < termsArray.length(); k++) {
						JSONObject term = termsArray.getJSONObject(k);
						String termName = term.getString("term");
						int count = term.getInt("count");
						if (terms.containsKey(termName)) {
							terms.put(termName, terms.get(termName) + count);
						} else {
							terms.put(termName, count);
						}
					}
				}
				category.setPossibility(possibility);
				category.setTerms(terms);
				category.setTermNum(termNum);
				category.setType(type);
				System.out.println(category.getType() + " has initialized!");
				categories.add(category);
			}
			return categories;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public void trainNB(List<Category> categories) {
		Pretreatment pt = new Pretreatment();

		// 记录每个类别下每个词的出现次数
		// List<List<Integer>> pNums=new
		// ArrayList<List<Integer>>(Arrays.asList(null,null,null,null,null,null,null,null,null,null));
		// // 记录每个类别下一共出现了多少词,为防止分母为0，所以在此默认值为2
		// double[] pDenoms= new double[10];
		for (int i = 0; i < utils.Properties.newsType.length; i++) {
			HashMap<String, Double> termPos = new HashMap<String, Double>();
			Category category = categories.get(i);
			ratios[i] = category.getPossibility();
			int denom = category.getTermNum()+2;

			HashMap<String, Integer> terms = category.getTerms();
			for (String termName : terms.keySet()) {
				double possibility = Math.log((double) (terms.get(termName)+1) / denom);
				//System.out.println(termName+" "+possibility);
				termPos.put(termName, possibility);
			}
			
			termPos.put("TheGhostWord",(double)1/denom);
			System.out.println("Ghost word is:"+(double)1/denom);
			pVecs.add(termPos);
		}
	}

	/**
	 * 比较不同类别 p(w0,w1,w2...wn | ci)*p(ci) 的大小 <br>
	 * p(w0,w1,w2...wn | ci) = p(w0|ci)*p(w1|ci)*p(w2|ci)... <br>
	 * 由于防止下溢，对中间计算值都取了对数，因此上述公式化为log(p(w0,w1,w2...wn | ci)) + log(p(ci)),即
	 * 化为多个式子相加得到结果
	 * 
	 * @param email
	 * @return 返回概率最大值
	 */
	public int classifyNB(HashMap<String, Integer> terms) {
		double[] possibilities = new double[utils.Properties.newsType.length];
		for (int i = 0; i < pVecs.size(); i++) {
			System.out.println("For "+utils.Properties.newsType[i]);
			possibilities[i] = calProbabilityByClass(pVecs.get(i), terms) + Math.log(ratios[i]);
		}

		int location = 0;
		double max = possibilities[0];

		for (int i = 0; i < possibilities.length; i++) {
			System.out.println(utils.Properties.newsType[i] + " " + possibilities[i]);
			if (possibilities[i] > max) {
				max = possibilities[i];
				location = i;
			}
		}
		return location;
	}

	// *出现次数
	private double calProbabilityByClass(HashMap<String, Double> vec, HashMap<String, Integer> artical) {
		double sum = 0.0;
		for (String term : artical.keySet()) {
			if (vec.containsKey(term)) {
				//System.out.println(term+"'s possibility "+vec.get(term)+" and the num is "+artical.get(term));
				sum += vec.get(term) * artical.get(term);
			}
			else {
				sum += vec.get("TheGhostWord") * artical.get(term);
			}
		}
		return sum;
	}

	public void testingNB() {
		List<Category> dataSet = initDataSet();
		// 训练样本
		trainNB(dataSet);
				
		int correctNum=0;
		for(int i=11;i<=11;i++) {
			HashMap<String, Integer> testSet = initTestSet(String.valueOf(i));
			int type = classifyNB(testSet);
			if(utils.Properties.newsType[type].equals("军事")) {
				correctNum+=1;
			}
		}
		
		System.out.println("Correct Num is "+correctNum);
	}

	public static void main(String[] args) {
		Classification bayesian = new Classification();
		bayesian.testingNB();
	}
}
