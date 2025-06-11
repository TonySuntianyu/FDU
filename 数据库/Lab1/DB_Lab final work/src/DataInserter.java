import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DataInserter {
    // 配置项
    private static final String TARGET_TABLE = "room"; // 可改为 student
    private static final String CSV_FILE = "room.csv"; // 可改为 student.csv
    
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnector.connect();
             BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            
            // 清空目标表
            truncateTable(conn, TARGET_TABLE);
            
            // 读取标题行确定字段数
            String[] headers = br.readLine().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            String sql = buildInsertSQL(TARGET_TABLE, headers.length);
            
            // 准备日期格式化器
            SimpleDateFormat[] dateFormats = {
                new SimpleDateFormat("yyyy-MM-dd H:mm"),
                new SimpleDateFormat("yyyy-MM-dd HH:mm")
            };

            Set<String> uniqueKeys = new HashSet<>();
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] values = parseCSVLine(line, headers.length);
                    
                    // 处理主键去重（根据表结构调整）
                    String uniqueKey = TARGET_TABLE.equals("room") ? 
                        values[0] + values[1] + values[2] : // kdno+kcno+ccno
                        values[0]; // registno
                    if (!uniqueKeys.add(uniqueKey)) {
                        System.out.println("跳过重复记录: " + uniqueKey);
                        continue;
                    }
                    
                    // 绑定参数
                    for (int i = 0; i < headers.length; i++) {
                        setPreparedStatementValue(pstmt, i+1, headers[i], values[i], dateFormats);
                    }
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            System.out.println("数据插入成功！插入表: " + TARGET_TABLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String[] parseCSVLine(String line, int expectedLength) {
        String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].replaceAll("^\"|\"$", "").trim();
            if (values[i].isEmpty()) values[i] = null;
        }
        return values.length == expectedLength ? values : new String[expectedLength];
    }

    private static void setPreparedStatementValue(PreparedStatement pstmt, int index, 
            String columnName, String value, SimpleDateFormat[] dateFormats) throws SQLException {
        if (value == null) {
            pstmt.setNull(index, Types.VARCHAR);
            return;
        }

        try {
            if ("exptime".equalsIgnoreCase(columnName)) {
                Date date = null;
                for (SimpleDateFormat format : dateFormats) {
                    try {
                        date = format.parse(value);
                        break;
                    } catch (Exception e) {}
                }
                if (date != null) {
                    pstmt.setTimestamp(index, new Timestamp(date.getTime()));
                } else {
                    pstmt.setNull(index, Types.TIMESTAMP);
                }
            } else {
                pstmt.setString(index, value);
            }
        } catch (Exception e) {
            System.err.println("字段处理错误 [" + columnName + "]: " + value);
            pstmt.setNull(index, Types.VARCHAR);
        }
    }

    private static String buildInsertSQL(String tableName, int paramCount) {
        return "INSERT INTO " + tableName + " VALUES (" + 
            String.join(",", java.util.Collections.nCopies(paramCount, "?")) + ")";
    }

    private static void truncateTable(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("TRUNCATE TABLE " + tableName);
            System.out.println("已清空表: " + tableName);
        }
    }
}