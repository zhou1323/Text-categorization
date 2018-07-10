package classification;
import java.util.List;

import javax.management.loading.PrivateClassLoader;

public class Term {
	private String term;
	private int count;
	private double tdidf;
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public double getTdidf() {
		return tdidf;
	}
	public void setTdidf(double tdidf) {
		this.tdidf = tdidf;
	}
	
	
}
