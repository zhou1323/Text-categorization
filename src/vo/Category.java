package vo;

import java.util.HashMap;

public class Category {
	//类别
	private String type;
	//出现此类的概率
	private double possibility;
	//总词数
	private int termNum;
	//词的具体情况
	private HashMap<String, Integer> terms;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public double getPossibility() {
		return possibility;
	}
	public void setPossibility(double possibility) {
		this.possibility = possibility;
	}
	public int getTermNum() {
		return termNum;
	}
	public void setTermNum(int termNum) {
		this.termNum = termNum;
	}
	public HashMap<String, Integer> getTerms() {
		return terms;
	}
	public void setTerms(HashMap<String, Integer> terms) {
		this.terms = terms;
	}
	@Override
	public String toString() {
		return "Category [type=" + type + ", possibility=" + possibility + ", termNum=" + termNum + ", terms=" + terms
				+ "]";
	}
}
