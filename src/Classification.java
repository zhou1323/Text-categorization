import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.lang.model.element.VariableElement;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;

import dao.CateDAO;
import dao.TextDAO;
public class Classification {
	//各类新闻中每个词出现的概率
	private List<List<Double>> pVecs = new ArrayList<List<Double>>();
	//各类新闻出现的概率
	private double[] ratios=new double[utils.Properties.newsType.length];
	

	public List<Artical> initTestSet(){
		List<Artical> testSet=new ArrayList<Artical>();
		BufferedReader br=null;
		try {
				br = new BufferedReader(new InputStreamReader(
						new FileInputStream("E:\\Test\\Test\\59.txt")));
				StringBuilder sb1 = new StringBuilder();
				String string = null;while ((string = br.readLine()) != null) {
					sb1.append(string);
				}
				Artical testArtical = new Artical();
				testArtical.setWordList(textParse(sb1.toString()));
				
				testSet.add(testArtical);
			
			return testSet;
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
	/**
	 * 初始化数据集
	 * 
	 * @return
	 */
	public List<Artical> initDataSet() {
		List<Artical> dataSet = new ArrayList<Artical>();
		TextDAO textDAO=new TextDAO();
		try {
			for(int i=0;i<utils.Properties.newsType.length;i++) {
				List<String> news=textDAO.getTextByType(utils.Properties.newsType[i]);
				for (String string : news) {
					Artical artical=new Artical();
					artical.setWordList(textParse(string));
					artical.setType(utils.Properties.newsType[i]);
					dataSet.add(artical);
				}
			}
			return dataSet;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	/**
	 * 分词，英文的分词相比中文的分词要简单很多，这里使用的分隔符为除单词、数字外的任意字符串
	 * 如果使用中文，则可以使用中科院的一套分词系统，分词效果还算不错
	 * 
	 * @param originalString
	 * @return
	 * @return
	 */
	private List<String> textParse(String originalString) {
		JiebaSegmenter segmenter = new JiebaSegmenter();
		WordUtil filter = new WordUtil("E:\\Test\\Test\\stopWords.txt");
		List<String> wordList = filter.filter(segmenter.process(originalString, SegMode.INDEX));
		
		return wordList;
	}

	/**
	 * 构建单词集，此长度等于向量长度
	 * 
	 * @return
	 */
	public Set<String> createVocabList(List<Artical> dataSet) {
		Set<String> set = new LinkedHashSet<String>();
		for (Artical email : dataSet) {
			for (String string : email.getWordList()) {
				set.add(string);
			}
		}
		return set;
	}

	/**
	 * 将邮件转换为向量
	 * 
	 * @param vocabSet
	 * @param inputSet
	 * @return
	 */
	public List<Integer> setOfWords2Vec(Set<String> vocabSet, Artical email) {
		List<Integer> returnVec = new ArrayList<Integer>();
		for (String word : vocabSet) {
			returnVec.add(calWordFreq(word, email));
		}
		return returnVec;
	}

	/**
	 * 计算一个词在某个集合中的出现次数
	 * 
	 * @return
	 */
	private int calWordFreq(String word, Artical email) {
		int num = 0;
		for (String string : email.getWordList()) {
			if (string.equals(word)) {
				++num;
			}
		}
		return num;
	}

	public void trainNB(Set<String> vocabSet, List<Artical> dataSet) {
		// 训练文本的数量
		int numTrainDocs = dataSet.size();
		// 训练集中各类型新闻的概率
		int[] newsNum=calSpamNum(dataSet);
		for(int i=0;i<newsNum.length;i++) {
			ratios[i]=(double) newsNum[i]/numTrainDocs;
		}

		// 记录每个类别下每个词的出现次数
		List<List<Integer>> pNums=new ArrayList<List<Integer>>(Arrays.asList(null,null,null,null,null,null,null,null,null,null));
		// 记录每个类别下一共出现了多少词,为防止分母为0，所以在此默认值为2
		double[] pDenoms= new double[10];
		for (Artical email : dataSet) {
			List<Integer> list = setOfWords2Vec(vocabSet, email);
			
			int postion=Arrays.binarySearch(utils.Properties.newsType,email.getType());
			pNums.set(postion, vecAddVec(pNums.get(postion), list));
			pDenoms[postion] += calTotalWordNum(list);
			
		}
		for(int i=0;i<pNums.size();i++) {
			List<Double> temp=calWordRatio(pNums.get(i), pDenoms[i]);
			pVecs.add(temp);
		}
	}

	/**
	 * 两个向量相加
	 * 
	 * @param vec1
	 * @param vec2
	 * @return
	 */
	private List<Integer> vecAddVec(List<Integer> vec1,
			List<Integer> vec2) {
		if (vec1 == null) {
			return vec2;
		}
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < vec1.size(); i++) {
			list.add(vec1.get(i) + vec2.get(i));
		}
		return list;
	}
	
	/**
	 * 计算各类型新闻的数量
	 * 
	 * @param dataSet
	 * @return
	 */
	private int[] calSpamNum(List<Artical> dataSet) {
		int[] results= {0,0,0,0,0,0,0,0,0,0};
		
		for (Artical email : dataSet) {
			
			int position=Arrays.binarySearch(utils.Properties.newsType, email.getType());
			
			results[position]+=1;
		}
		return results;
	}
	
	/**
	 * 统计出现的所有单词数
	 * @param list
	 * @return
	 */
	private int calTotalWordNum(List<Integer> list) {
		int num = 0;
		for (Integer integer : list) {
			num += integer;
		}
		return num;
	}
	
	/**
	 * 计算每个单词在该类别下的出现概率，为防止分子为0，导致朴素贝叶斯公式为0，设置分子的默认值为1
	 * @param list
	 * @param wordNum
	 * @return
	 */
	private List<Double> calWordRatio(List<Integer> list, double wordNum) {
		List<Double> vec = new ArrayList<Double>();
		for (Integer i : list) {
			vec.add(Math.log((double)(i+1) / wordNum));
		}
		return vec;
	}
	
	/**
	 * 比较不同类别 p(w0,w1,w2...wn | ci)*p(ci) 的大小   <br>
	 *  p(w0,w1,w2...wn | ci) = p(w0|ci)*p(w1|ci)*p(w2|ci)... <br>
	 *  由于防止下溢，对中间计算值都取了对数，因此上述公式化为log(p(w0,w1,w2...wn | ci)) + log(p(ci)),即
	 *  化为多个式子相加得到结果
	 *  
	 * @param email
	 * @return 返回概率最大值 
	 */
	public int classifyNB(List<Integer> emailVec) {
		double[] possibilities=new double[pVecs.size()];
		for(int i=0;i<pVecs.size();i++) {
			possibilities[i] = calProbabilityByClass(pVecs.get(i), emailVec) + Math.log(ratios[i]);
		}
		Arrays.sort(possibilities);
		int pos=Arrays.binarySearch(possibilities, Arrays.stream(possibilities).max().getAsDouble());
//		for(int i=0;i<possibilities.length;i++) {
//			for(int j=0;j<possibilities.length-i-1;j++) {
//				if(possibilities[j]<possibilities[j+1]) {
//					double temp=possibilities[j];
//					possibilities[j]=possibilities[j+1];
//					possibilities[j+1]=temp;
//				}
//			}
//		}
		
		return pos;
	}
	
	private double calProbabilityByClass(List<Double> vec,List<Integer> emailVec) {
		double sum = 0.0;
		for (int i = 0; i < vec.size(); i++) {
			sum += (vec.get(i) * emailVec.get(i));
		}
		return sum;
	}
	
	public void testingNB() {
		List<Artical> dataSet = initDataSet();
		List<Artical> testSet = initTestSet();
//		List<Artical> testSet = new ArrayList<Artical>();
		//随机取前10作为测试样本
//		for (int i = 0; i < 10; i++) {
//			Random random = new Random();
//			int n = random.nextInt(50-i);
//			testSet.add(dataSet.get(n));
//			//从训练样本中删除这10条测试样本
//			dataSet.remove(n);
//		}
		Set<String> vocabSet = createVocabList(dataSet);
		//训练样本
		trainNB(vocabSet, dataSet);
		
		int errorCount = 0;
		for (Artical email : testSet) {
//			if (classifyNB(setOfWords2Vec(vocabSet, email)) != email.getType()) {
//				++errorCount;
//			}
			int temp=classifyNB(setOfWords2Vec(vocabSet, email));
			System.out.println(utils.Properties.newsType[1]);
			
		}
//		System.out.println("the error rate is: " + (double) errorCount / testSet.size());
//		return (double) errorCount / testSet.size();
	}

	public static void main(String[] args) {
		//调用binarySearch()方法前要先调用sort方法对数组进行排序，否则得出的返回值不定，这时二分搜索算法决定的。
		Arrays.sort(utils.Properties.newsType);
		
		Classification bayesian = new Classification();
		bayesian.testingNB();
//		double d = 0;
//		for (int i = 0; i < 50; i++) {
//			d +=bayesian.testingNB();
//		}
//		System.out.println("total error rate is: " + d / 50);
	}
}
