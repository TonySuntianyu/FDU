import java.sql.*;
public class DatabaseExample {
    public static void main(String[] args) {
        try {
            // 加载JDBC驱动程序
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/myaql_db", "root", "Tonysun050127");
            
            // 创建Statement对象
            Statement stmt = connection.createStatement();
            
            // 执行查询
            ResultSet rs = stmt.executeQuery("SELECT * FROM my_table");
            
            // 处理结果
            while (rs.next()) {
                System.out.println(rs.getString("column_name"));
            }
            
            // 关闭连接
            rs.close();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
