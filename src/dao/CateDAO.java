package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import vo.News;

public class CateDAO {
	public static final String URL = "jdbc:mysql://138.68.62.57:3306/webtext?serverTimezone=GMT&useSSL=false&useUnicode=true&characterEncoding=UTF8";
	public static final String DRIVER = "com.mysql.cj.jdbc.Driver";
	public static final String UNAME = "root";
	public static final String PASSWORD = "MyNewPass4!";
	
    public Connection getConn() throws ClassNotFoundException, SQLException{
        Class.forName(DRIVER);
        Connection conn = DriverManager.getConnection(URL,UNAME,PASSWORD);        
        return conn ;                                                            
    }
    
    //判断训练集是否为空
    public Boolean isEmpty() throws SQLException, ClassNotFoundException {
    	String sql = "select * from News";
    	Connection conn = getConn();
    	PreparedStatement ps = conn.prepareStatement(sql);
    	ResultSet rs = ps.executeQuery();
    	Boolean result=rs.next();
    	return result;
    }
    
    public List<String> getTextByType(String type) throws ClassNotFoundException, SQLException{
    	List<String> result=new ArrayList<String>();
    	String sql = "select content from News where type=?";
    	Connection conn = getConn();
    	PreparedStatement ps = conn.prepareStatement(sql);
    	ps.setString(1, type);
    	ResultSet rs = ps.executeQuery();
    	while(rs.next()) {
    		result.add(rs.getString("content"));
    	}
    	rs.close();
    	ps.close();
    	conn.close();
    	return result;
    }
    
    public void addCategory(News news) throws ClassNotFoundException, SQLException{
    	String sql = "insert into news (type,title,url,content,date,source) values (?,?,?,?,?,?)";
    	Connection conn = getConn();
    	PreparedStatement ps = conn.prepareStatement(sql);
    	ps.setString(1, news.getType());
    	ps.setString(2, news.getTitle());
    	ps.setString(3, news.getUrl());
    	ps.setString(4, news.getContent());
    	ps.setString(5, news.getDate());
    	ps.setString(6, news.getSource());
    	
    	ps.execute();
    	ps.close();
    	conn.close();
    }
}
