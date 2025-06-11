import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CSVReader {
    public static void main(String[] args) {
        String filePath = "student.csv"; // 可替换为 room.csv
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // 读取标题行
            String header = br.readLine();
            System.out.println("Header: " + header);

            while ((line = br.readLine()) != null) {
                // 处理带引号的CSV字段
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                
                // 移除字段两侧的引号
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].replaceAll("^\"|\"$", "");
                }

                // 动态打印所有列
                StringBuilder output = new StringBuilder();
                for (String value : values) {
                    output.append(value).append(" | ");
                }
                System.out.println(output.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}