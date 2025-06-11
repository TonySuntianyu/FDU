好的，我们详细展开这个实验的教程，确保你可以顺利完成。

---

# **Lab1: 数据库应用接口实验教程（使用 VSCode + MySQL + Java）**

## **一、实验目标**
- 掌握如何使用 Java 通过 JDBC 连接 MySQL 数据库。
- 学会读取 CSV 文件并将数据导入 MySQL 数据库。
- 了解如何动态生成 SQL 语句，使程序可以适应不同的表结构。
- 处理外部数据的完整性和一致性问题。

---

## **二、实验环境**
- **操作系统**：Windows / macOS / Linux
- **数据库**：MySQL（建议安装 MySQL 8.0）
- **开发工具**：VSCode
- **编程语言**：Java（使用 JDK 17 或更高）
- **数据库访问库**：JDBC（使用 MySQL 官方驱动）
- **数据格式**：CSV（Comma-Separated Values）

---

## **三、实验步骤**
### **1. 安装和配置开发环境**
#### **1.1 安装 MySQL**
如果你还没有安装 MySQL，可以按照以下步骤进行：
- **Windows 用户**：
  1. 下载 MySQL Installer：[MySQL 官网](https://dev.mysql.com/downloads/)
  2. 选择 **MySQL Community Server**，按照安装向导进行安装。
  3. 设置 root 用户密码，并记住这个密码。
  4. 使用 MySQL Workbench 或命令行工具 `mysql -u root -p` 连接数据库。

- **macOS 用户**：
  ```sh
  brew install mysql
  brew services start mysql
  mysql -u root -p
  ```

#### **1.2 安装 VSCode**
如果你还没有安装 VSCode，可以到 [VSCode 官网](https://code.visualstudio.com/) 下载并安装。

#### **1.3 安装 JDK**
- **Windows** / **Mac**：下载 [JDK](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html) 并安装。
- 安装完成后，运行以下命令检查是否安装成功：
  ```sh
  java -version
  javac -version
  ```

#### **1.4 安装 MySQL JDBC 驱动**
- 下载 [MySQL JDBC 驱动](https://dev.mysql.com/downloads/connector/j/) 并解压。
- 将 `mysql-connector-java-*.jar` 复制到项目的 `lib` 目录下。

#### **1.5 在 VSCode 中配置 Java 开发环境**
1. 在 VSCode 中安装以下扩展：
   - **Java Extension Pack**（官方 Java 扩展包）
   - **Language Support for Java(TM) by Red Hat**
2. 打开 VSCode，创建一个新的 Java 项目：
   ```sh
   mkdir Lab1-Database
   cd Lab1-Database
   code .
   ```

---

### **2. 编写数据库初始化程序**
#### **2.1 创建数据库**
在 MySQL 中创建数据库 `lab1_db`：
```sql
CREATE DATABASE lab1_db;
USE lab1_db;
```

#### **2.2 编写 Java 代码连接 MySQL**
在 `src` 目录下创建 `DatabaseConnector.java`：
```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String URL = "jdbc:mysql://localhost:3306/lab1_db";
    private static final String USER = "root";  // 你的 MySQL 用户名
    private static final String PASSWORD = "yourpassword";  // 你的 MySQL 密码

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
```
**运行代码**：
```sh
javac DatabaseConnector.java
java DatabaseConnector
```
如果输出 `"数据库连接成功！"`，说明 MySQL 连接成功。

---

### **3. 读取 CSV 文件**
#### **3.1 准备 CSV 数据**
在项目目录下创建 `data.csv`：
```csv
id,name,age,email
1,张三,25,zhangsan@example.com
2,李四,30,lisi@example.com
3,王五,22,wangwu@example.com
```

#### **3.2 编写 Java 代码读取 CSV**
在 `src` 目录下创建 `CSVReader.java`：
```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CSVReader {
    public static void main(String[] args) {
        String filePath = "data.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                System.out.println("ID: " + values[0] + ", Name: " + values[1] + ", Age: " + values[2] + ", Email: " + values[3]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```
**运行代码**：
```sh
javac CSVReader.java
java CSVReader
```
如果正确输出 CSV 内容，说明数据读取成功。

---

### **4. 创建数据库表结构**
创建 `schema.sql` 文件：
```sql
CREATE TABLE users (
    id INT PRIMARY KEY,
    name VARCHAR(50),
    age INT,
    email VARCHAR(100)
);
```
在 `src` 目录下创建 `TableCreator.java`：
```java
import java.sql.Connection;
import java.sql.Statement;

public class TableCreator {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement()) {
            
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                         "id INT PRIMARY KEY, " +
                         "name VARCHAR(50), " +
                         "age INT, " +
                         "email VARCHAR(100))";
            
            stmt.executeUpdate(sql);
            System.out.println("数据表创建成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
**运行代码**：
```sh
javac TableCreator.java
java TableCreator
```

---

### **5. 将数据插入数据库**
创建 `DataInserter.java`：
```java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.io.BufferedReader;
import java.io.FileReader;

public class DataInserter {
    public static void main(String[] args) {
        String filePath = "data.csv";
        String sql = "INSERT INTO users (id, name, age, email) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnector.connect();
             BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            
            String line;
            br.readLine(); // 跳过标题行
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, Integer.parseInt(values[0]));
                    pstmt.setString(2, values[1]);
                    pstmt.setInt(3, Integer.parseInt(values[2]));
                    pstmt.setString(4, values[3]);
                    pstmt.executeUpdate();
                }
            }
            System.out.println("数据插入成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
**运行代码**：
```sh
javac DataInserter.java
java DataInserter
```

---

## **总结**
你已经成功完成：
1. **数据库连接**
2. **读取 CSV 数据**
3. **创建数据库表**
4. **插入数据**
5. **查询数据**

如果有问题，欢迎继续提问！ 🚀