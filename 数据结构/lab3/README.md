# Lab3 数据结构实验

本实验包含三个题目的C++实现：

## 题目1：矩阵乘法 (matrix_multiplication.cpp)

**功能**：计算两个矩阵的乘积并对结果取模
- 输入：两个矩阵A(n×p)和B(p×m)
- 输出：矩阵C = A × B，每个元素模10^9+7
- 特点：使用vector处理动态数组，确保模运算结果为非负数

## 题目2：子串数量 (kmp_substring.cpp)

**功能**：使用KMP算法计算字符串B在字符串A中的出现次数
- 算法：KMP字符串匹配算法
- 特点：支持重叠匹配，时间复杂度O(n+m)
- 实现：包含next数组构建和模式匹配两个核心函数

## 题目3：编程作业 (code_plagiarism.cpp)

**功能**：代码雷同检测，将问题归约为KMP算法
- 核心思想：将代码标准化后使用KMP算法匹配
- 标准化规则：
  - 小写字母(a-z)表示变量，按出现顺序映射为a,b,c...
  - 大写字母(A-Z)表示非变量符号，按出现顺序映射为A,B,C...
- 归约：将代码雷同检测问题转换为字符串匹配问题

## 编译和运行

```bash
# 编译
g++ -o matrix_multiplication matrix_multiplication.cpp
g++ -o kmp_substring kmp_substring.cpp
g++ -o code_plagiarism code_plagiarism.cpp

# 运行
./matrix_multiplication
./kmp_substring
./code_plagiarism
```

## 注意事项

1. 所有代码都包含详细的中文注释
2. 矩阵乘法确保模运算结果为非负数
3. KMP算法支持重叠匹配
4. 代码雷同检测通过标准化将问题归约为KMP算法
