package classification;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;

import dao.TextDAO;
import textProcess.Pretreatment;

class Test {

	@org.junit.jupiter.api.Test
	void test() {
		//调用binarySearch()方法前要先调用sort方法对数组进行排序，否则得出的返回值不定，这时二分搜索算法决定的。
		
		Pretreatment test=new Pretreatment();
		test.process2();
	}

}
