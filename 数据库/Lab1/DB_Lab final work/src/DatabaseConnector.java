import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String URL = "jdbc:mysql://localhost:3306/db_lab";
    private static final String USER = "root";
    private static final String PASSWORD = "Tonysun050127";

    public static Connection connect() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("数据库连接成功！");
            return conn;
        } catch (SQLException e) {
            System.out.println("数据库连接失败：" + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        connect();
    }
}
