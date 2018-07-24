package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
	public static String getType(String type) {
		String result = null;
		switch (type) {
		case "gn":
			result = "国内";
			break;
		case "gj":
			result = "国际";
			break;
		case "sh":
			result = "社会";
			break;
		case "mil":
			result = "军事";
			break;
		case "ga":
			result = "港澳";
			break;
		case "tw":
			result = "台湾";
			break;
		case "hr":
			result = "华人";
			break;
		case "cj":
			result = "财经";
			break;
		case "fortune":
			result = "金融";
			break;
		case "estate":
			result = "房产";
			break;
		case "auto":
			result = "汽车";
			break;
		case "ny":
			result = "能源";
			break;
		case "it":
			result = "IT";
			break;
		case "wh":
			result = "文化";
			break;
		case "yl":
			result = "娱乐";
			break;
		case "ty":
			result = "体育";
			break;
		case "jk":
			result = "健康";
			break;
		default:
			break;
		}
		return result;
	}
	
	public static int getIDFromUrl(String url) {
		String result = url.substring(url.lastIndexOf("/")+1, url.lastIndexOf("."));
		String parn = "[^0-9]";
		Pattern pattern = Pattern.compile(parn);
		Matcher m = pattern.matcher(result);
		return Integer.parseInt(m.replaceAll("").trim());
	}
	public static List<String> generateDate(String date) {
		List<String> list = new ArrayList<>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MM dd");
		Date begin;
		try {
			begin = dateFormat.parse(date);
			Calendar temp = Calendar.getInstance();
			temp.setTime(begin);
			while(begin.getTime()<new Date().getTime()) {
				list.add(dateToStr(temp.getTime()));
				temp.add(Calendar.DAY_OF_YEAR, 1);
				begin = temp.getTime();
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
	}
	/**
	 * 把date转化为字符串
	 * @param date
	 * @return
	 */
	public static String dateToStr(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		String year = sdf.format(date);
		sdf.applyPattern("MM");
		String month = sdf.format(date);
		sdf.applyPattern("dd");
		String day = sdf.format(date);
		return year+"/"+month+day;
	}
}
