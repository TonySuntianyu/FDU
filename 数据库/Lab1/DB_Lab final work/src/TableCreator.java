import java.sql.Connection;
import java.sql.Statement;

public class TableCreator {
    public static void main(String[] args) {
        String[] createStatements = {
            "DROP TABLE IF EXISTS room" ,

            // 创建 student 表（保持不变）
            "CREATE TABLE IF NOT EXISTS student (" +
            "registno VARCHAR(20) PRIMARY KEY, " +
            "name VARCHAR(50), " +
            "kdno VARCHAR(10), " +
            "kcno VARCHAR(10), " +
            "ccno VARCHAR(10), " +
            "seat INT)",

            // 修正 room 表结构
            "CREATE TABLE IF NOT EXISTS room (" +
            "kdno VARCHAR(10), " +
            "kcno VARCHAR(10), " +
            "ccno VARCHAR(10), " +
            "kdname VARCHAR(100), " +
            "exptime DATETIME, " +  // 修改为 DATETIME 类型
            "papername VARCHAR(100))"
        };

        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement()) {
            
            for (String sql : createStatements) {
                stmt.executeUpdate(sql);
            }
            System.out.println("数据表创建成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}