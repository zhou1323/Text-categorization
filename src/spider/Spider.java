package spider;

import java.io.IOException;
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

import vo.News;

public class Spider {
	public static void crawl() {
		
		String baseurl = "http://www.chinanews.com/scroll-news";
		String[] types = {"gn","gj","sh","mil","ga","tw","hr","cj","fortune","estate","auto","ny","it","wh","yl","ty","jk"};
		String startDate = "2018 04 18";
		List<String> datestr = utils.Util.generateDate(startDate);
		String url = null;
		for(String type:types) {
			String newstype = utils.Util.getType(type);
			for(String date:datestr) {
				url = baseurl +"/"+type+"/"+date+"/news.shtml";
				try {
					Document doc = Jsoup.connect(url).get();
					String newsdate = doc.select("div.dd_time").text().split(" ")[0];
					Elements elements = doc.select("div.dd_bt");
					for(Element e: elements) {
						String newsurl = e.select("a").attr("href");
						int newsid = utils.Util.getIDFromUrl(newsurl);
						String newsTitle = e.select("a").text();
						Document contentdoc = Jsoup.connect(newsurl).get();
						String sourceStr = contentdoc.select("div.left-t").text();
						if(sourceStr.contains("来源")) {
							News news = new News();
							news.setUrl(newsurl);
							news.setTitle(newsTitle);
							news.setId(newsid);
							news.setType(newstype);
							news.setDate(newsdate);
							String source = sourceStr.substring(sourceStr.lastIndexOf("：")+1, sourceStr.lastIndexOf(" "));
							news.setSource(source);
							Elements ps = contentdoc.select("div.left_zw p");
							StringBuilder builder = new StringBuilder();
							for(Element p:ps) {
								builder.append(p.text());
							}
							String content = builder.toString();
							news.setContent(content);
							System.out.println("crawling  "+ newsurl);
							NewsWriter.writeToFiles(news);
						}				
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}
}
