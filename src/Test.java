import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;

class Test {

	@org.junit.jupiter.api.Test
	void test() {
		String paragraph = "你好2018夏天";
		JiebaSegmenter segmenter = new JiebaSegmenter();
		WordUtil filter = new WordUtil("E:\\Test\\Test\\stopWords.txt");
		List<String> list = filter.filter(segmenter.process(paragraph, SegMode.INDEX));
		HashMap<String, Integer> map = filter.countWordFry(list);
		
		System.out.println(map);
	}

}
