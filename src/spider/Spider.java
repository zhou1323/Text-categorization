package spider;

import java.io.Console;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import classification.Classification;
import dao.CateDAO;
import vo.News;

public class Spider {
	public static void crawl() {
		String baseurl = "http://www.chinanews.com/scroll-news";
		String[] types = { "gn", "gj", "sh", "mil", "ga", "tw", "hr", "cj", "fortune", "estate", "auto", "ny", "it",
				"wh", "yl", "ty", "jk" };
		String startDate = "2018 07 22";
		List<String> datestr = utils.Util.generateDate(startDate);
		String url = null;
		
		//分类器
		Classification classification=new Classification();
		classification.train();
		
		//DAO
		CateDAO cDao=new CateDAO();
		for (String type : types) {
			String newstype = utils.Util.getType(type);
			for (String date : datestr) {
				url = baseurl + "/" + type + "/" + date + "/news.shtml";
				try {
					Document doc = Jsoup.connect(url).get();
					String newsdate = doc.select("div.dd_time").text().split(" ")[0];
					Elements elements = doc.select("div.dd_bt");
					for (Element e : elements) {
						String newsurl = e.select("a").attr("href");
						int newsid = utils.Util.getIDFromUrl(newsurl);
						String newsTitle = e.select("a").text();
						Document contentdoc = Jsoup.connect(newsurl).get();
						String sourceStr = contentdoc.select("div.left-t").text();
						if (sourceStr.contains("来源")) {
							News news = new News();
							news.setUrl(newsurl);
							news.setTitle(newsTitle);
							news.setId(newsid);
							news.setDate(newsdate);
							String source = sourceStr.substring(sourceStr.lastIndexOf("：") + 1,
									sourceStr.lastIndexOf(" "));
							news.setSource(source);
							Elements ps = contentdoc.select("div.left_zw p");
							StringBuilder builder = new StringBuilder();
							for (Element p : ps) {
								builder.append(p.text());
							}
							String content = builder.toString();
							news.setContent(content);
							String newsType=classification.testingNB(content,false);
							news.setType(newsType);
							cDao.addCategory(news);
							System.out.println("Add successfully!");
						}
					}
				} catch (IOException | ClassNotFoundException | SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static void main(String args[]) {
		crawl();
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MM dd");
//		try {
//			System.out.println(dateFormat.parse("2018 04 15"));
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		for(String s:generateDate("2018 04 15")) {
//			System.out.println(s);
//		}
//		String s = "http://www.chinanews.com/gn/2018/04-20/8495700.shtml";
//		System.out.println(getIDFromUrl(s));
	}
}
