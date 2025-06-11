import java.io.*;
import java.util.*;

public class Lab1 {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();
        String fileName = "grade.txt";

        // 使用 try-with-resources 语句自动关闭资源
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+"); // 使用正则表达式处理多个空格
                if (parts.length == 2) {
                    try {
                        int score = Integer.parseInt(parts[1]);
                        students.add(new Student(parts[0], score));
                    } catch (NumberFormatException e) {
                        System.err.println("无效的成绩格式: " + parts[1]);
                    }
                } else {
                    System.err.println("无效的行格式: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("文件读取失败: " + e.getMessage());
            return;
        }

        // 计算平均成绩
        double average = students.stream()
                .mapToInt(Student::getScore)
                .average()
                .orElse(0.0);
        System.out.println("平均成绩: " + average);

        // 按成绩从高到低排序
        students.sort(Comparator.comparingInt(Student::getScore).reversed());

        // 输出学生姓名
        students.forEach(student -> System.out.println(student.getName()));
    }
}

class Student {
    private String name;
    private int score;

    Student(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }
}
