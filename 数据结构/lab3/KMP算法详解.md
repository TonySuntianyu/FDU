# KMP算法详解

## 1. 算法背景

KMP算法（Knuth-Morris-Pratt算法）是一种高效的字符串匹配算法，由Donald Knuth、Vaughan Pratt和James Morris在1977年共同提出。该算法解决了传统暴力匹配算法中重复比较的问题。

## 2. 问题描述

给定一个文本串T（长度为n）和一个模式串P（长度为m），找出模式串P在文本串T中的所有出现位置。

### 传统暴力匹配的问题

```cpp
// 暴力匹配算法
int bruteForce(const string& text, const string& pattern) {
    int n = text.length();
    int m = pattern.length();
    
    for (int i = 0; i <= n - m; i++) {
        int j = 0;
        while (j < m && text[i + j] == pattern[j]) {
            j++;
        }
        if (j == m) {
            return i; // 找到匹配
        }
    }
    return -1; // 未找到
}
```

**问题**：当匹配失败时，文本串指针i只向前移动1位，模式串指针j重置为0，导致大量重复比较。

## 3. KMP算法核心思想

KMP算法的核心思想是：**利用已匹配的信息，避免重复比较**。

### 关键观察

当模式串P在位置j处匹配失败时，我们已经知道：
- P[0...j-1]与T[i...i+j-1]完全匹配
- 可以利用P[0...j-1]的内部结构信息，决定下一个比较位置

## 4. Next数组（部分匹配表）

### 4.1 定义

Next数组是KMP算法的核心，它记录了模式串中每个位置的最长公共前后缀长度。

**定义**：next[i] = 模式串P[0...i]的最长公共前后缀长度

### 4.2 前后缀概念

对于字符串"ababa"：
- 前缀：a, ab, aba, abab
- 后缀：a, ba, aba, baba
- 公共前后缀：a, aba
- 最长公共前后缀：aba（长度为3）

### 4.3 Next数组构建算法

```cpp
vector<int> buildNext(const string& pattern) {
    int m = pattern.length();
    vector<int> next(m, 0);
    
    int j = 0; // 指向前缀末尾
    for (int i = 1; i < m; i++) { // i指向后缀末尾
        // 不匹配时，回退到前一个匹配位置
        while (j > 0 && pattern[i] != pattern[j]) {
            j = next[j - 1];
        }
        
        // 匹配时，j向前移动
        if (pattern[i] == pattern[j]) {
            j++;
        }
        
        next[i] = j;
    }
    
    return next;
}
```

### 4.4 构建过程示例

以模式串"ababa"为例：

```
模式串: a b a b a
索引:   0 1 2 3 4

步骤1: i=1, j=0
- pattern[1]='b', pattern[0]='a' 不匹配
- j=0, next[1]=0

步骤2: i=2, j=0  
- pattern[2]='a', pattern[0]='a' 匹配
- j=1, next[2]=1

步骤3: i=3, j=1
- pattern[3]='b', pattern[1]='b' 匹配  
- j=2, next[3]=2

步骤4: i=4, j=2
- pattern[4]='a', pattern[2]='a' 匹配
- j=3, next[4]=3

最终next数组: [0, 0, 1, 2, 3]
```

## 5. KMP匹配算法

### 5.1 算法实现

```cpp
int kmpSearch(const string& text, const string& pattern) {
    int n = text.length();
    int m = pattern.length();
    
    if (m == 0) return 0;
    if (m > n) return -1;
    
    vector<int> next = buildNext(pattern);
    int j = 0; // 模式串指针
    
    for (int i = 0; i < n; i++) { // 文本串指针
        // 不匹配时，利用next数组回退
        while (j > 0 && text[i] != pattern[j]) {
            j = next[j - 1];
        }
        
        // 匹配时，j向前移动
        if (text[i] == pattern[j]) {
            j++;
        }
        
        // 完全匹配
        if (j == m) {
            return i - m + 1; // 返回匹配位置
        }
    }
    
    return -1; // 未找到
}
```

### 5.2 匹配过程示例

文本串T: "abababab"
模式串P: "ababa"
Next数组: [0, 0, 1, 2, 3]

```
步骤1: i=0, j=0, T[0]='a', P[0]='a' ✓
步骤2: i=1, j=1, T[1]='b', P[1]='b' ✓  
步骤3: i=2, j=2, T[2]='a', P[2]='a' ✓
步骤4: i=3, j=3, T[3]='b', P[3]='b' ✓
步骤5: i=4, j=4, T[4]='a', P[4]='a' ✓
完全匹配！返回位置0

继续搜索重叠匹配：
步骤6: i=5, j=4, T[5]='b', P[4]='a' ✗
- j = next[4-1] = next[3] = 2
- T[5]='b', P[2]='a' ✗  
- j = next[2-1] = next[1] = 0
- T[5]='b', P[0]='a' ✗
- j=0, i=6

步骤7: i=6, j=0, T[6]='a', P[0]='a' ✓
...继续匹配
```

## 6. 算法复杂度分析

### 6.1 时间复杂度

- **构建Next数组**：O(m)
- **匹配过程**：O(n)
- **总时间复杂度**：O(n + m)

### 6.2 空间复杂度

- **Next数组存储**：O(m)
- **总空间复杂度**：O(m)

### 6.3 与暴力算法对比

| 算法 | 时间复杂度 | 空间复杂度 | 优势 |
|------|------------|------------|------|
| 暴力匹配 | O(n×m) | O(1) | 简单直观 |
| KMP算法 | O(n+m) | O(m) | 高效，避免重复比较 |

## 7. 算法优势

### 7.1 避免重复比较

KMP算法通过Next数组，在匹配失败时能够跳过已经确定不可能匹配的位置。

### 7.2 线性时间复杂度

在最坏情况下，KMP算法仍能保持O(n+m)的时间复杂度。

### 7.3 支持重叠匹配

通过调整匹配成功后的处理方式，可以轻松实现重叠匹配。

## 8. 实际应用

### 8.1 字符串搜索

```cpp
// 查找所有匹配位置
vector<int> findAllMatches(const string& text, const string& pattern) {
    vector<int> positions;
    int n = text.length();
    int m = pattern.length();
    
    if (m == 0) return positions;
    
    vector<int> next = buildNext(pattern);
    int j = 0;
    
    for (int i = 0; i < n; i++) {
        while (j > 0 && text[i] != pattern[j]) {
            j = next[j - 1];
        }
        
        if (text[i] == pattern[j]) {
            j++;
        }
        
        if (j == m) {
            positions.push_back(i - m + 1);
            j = next[j - 1]; // 支持重叠匹配
        }
    }
    
    return positions;
}
```

### 8.2 文本编辑器

- 查找和替换功能
- 语法高亮
- 代码搜索

### 8.3 生物信息学

- DNA序列匹配
- 蛋白质序列分析

## 9. 算法扩展

### 9.1 多模式匹配

KMP算法可以扩展为AC自动机，用于多模式串匹配。

### 9.2 字符串周期

利用Next数组可以高效计算字符串的周期。

```cpp
// 计算字符串周期
int getPeriod(const string& s) {
    vector<int> next = buildNext(s);
    int n = s.length();
    int period = n - next[n - 1];
    return (n % period == 0) ? period : n;
}
```

## 10. 总结

KMP算法是字符串匹配领域的重要算法，其核心思想是通过预处理模式串，构建Next数组来避免重复比较。该算法具有以下特点：

1. **高效性**：时间复杂度O(n+m)
2. **实用性**：广泛应用于文本处理、生物信息学等领域
3. **可扩展性**：可以扩展为更复杂的字符串匹配算法
4. **教学价值**：是学习算法设计的经典案例

通过深入理解KMP算法，不仅能够解决字符串匹配问题，更重要的是培养了利用已有信息优化算法效率的思维方法。
