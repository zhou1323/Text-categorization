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
import spider.Spider;
import textProcess.Pretreatment;
import vo.Category;
import vo.News;

public class Classification {
	// 各类新闻中每个词出现的概率
	private List<HashMap<String, Double>> pVecs = new ArrayList<HashMap<String, Double>>();
	// 各类新闻出现的概率
	private double[] ratios = new double[utils.Properties.newsType.length];

	// 初始化测试集
	public HashMap<String, Integer> initTestSet(String file, boolean isFile) {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		Pretreatment pt = new Pretreatment();
		
		try {
			HashMap<String, Integer> temp = new HashMap<String, Integer>();
			if (isFile) {
				BufferedReader br = null;
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("GBK")));
				StringBuilder sb1 = new StringBuilder();
				String string = null;
				while ((string = br.readLine()) != null) {
					sb1.append(string);
				}
				temp = pt.textParse(sb1.toString());
				
				br.close();
			} else
				temp = pt.textParse(file);
			
			// 筛选tdidf较大的词
			List<String> words = pickWordsByTdidf(temp);
			for (String word : words) {
				result.put(word, temp.get(word));
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	// 选出tdidf较大的词
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

		for (int i = 0; i < (list.size() > 10 ? 10 : list.size()); i++) {
			HashMap.Entry<String, Double> mapping = list.get(i);
			// System.out.println(mapping.getKey());
			result.add(mapping.getKey());
		}
		return result;
	}

	// 初始化数据集
	public List<Category> initDataSet() {
		List<Category> categories = new ArrayList<Category>();
		Pretreatment pt = new Pretreatment();
		try {
			// 总新闻篇数
			int total = 0;
			for (int i = 0; i < utils.Properties.newsType.length; i++) {
				String type = utils.Properties.newsType[i];
				// 读取训练集结果
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

					int totalTerm = artical.getInt("total");
					termNum += totalTerm;

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

	// 训练数据集,计算各类新闻中每个词出现的概率
	public void trainNB(List<Category> categories) {
		Pretreatment pt = new Pretreatment();

		// 记录每个类别下每个词的出现次数
		// 记录每个类别下一共出现了多少词,为防止分母为0，所以在此默认值为2
		// 平滑
		for (int i = 0; i < utils.Properties.newsType.length; i++) {
			HashMap<String, Double> termPos = new HashMap<String, Double>();
			Category category = categories.get(i);
			ratios[i] = category.getPossibility();
			double denom = category.getTermNum() + category.getTerms().size() / 10;

			HashMap<String, Integer> terms = category.getTerms();
			for (String termName : terms.keySet()) {
				double possibility = Math.log((double) (terms.get(termName) + 0.1) / denom);
				termPos.put(termName, possibility);
			}

			termPos.put("TheGhostWord", Math.log((double) 0.1 / denom));
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
			possibilities[i] = calProbabilityByClass(pVecs.get(i), terms) + Math.log(ratios[i]);
		}

		int location = 0;
		double max = possibilities[0];

		for (int i = 0; i < possibilities.length; i++) {
			if (possibilities[i] > max) {
				max = possibilities[i];
				location = i;
			}
		}
		return location;
	}

	// 取对数后需要乘出现次数
	private double calProbabilityByClass(HashMap<String, Double> vec, HashMap<String, Integer> article) {
		double sum = 0.0;

		// 测试集为标准
		for (String term : article.keySet()) {
			if (vec.containsKey(term)) {
				sum += vec.get(term) * article.get(term);
			} else {
				sum += vec.get("TheGhostWord") * article.get(term);

			}
		}
		return sum;
	}

	//训练
	public void train() {
		List<Category> dataSet = initDataSet();
		trainNB(dataSet);
	}
	// 测试
	public String testingNB(String file,boolean isFile) {
		HashMap<String, Integer> testSet = initTestSet(file,isFile);
		int type=classifyNB(testSet);
		String realType =utils.Properties.newsType[type];
		return realType;
		// 测试集向量化
		//HashMap<String, Integer> testSet = initTestSet(
		//		utils.Properties.testSetPos + String.valueOf(i) + ".txt",0);
		//int type = classifyNB(testSet);
		//System.out.println(i + " is " + utils.Properties.newsType[type]);
	}


//	public static void main(String[] args) {
//		Classification bayesian = new Classification();
//		bayesian.testingNB();
//	}
}
