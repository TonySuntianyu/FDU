# BF算法与广义表详解

## 目录
1. [BF算法（Brute Force）](#bf算法brute-force)
2. [KMP算法（Knuth-Morris-Pratt）](#kmp算法knuth-morris-pratt)
3. [广义表（Generalized List）](#广义表generalized-list)
4. [BF算法在广义表中的应用](#bf算法在广义表中的应用)
5. [广义表的存储结构](#广义表的存储结构)
6. [广义表的操作实现](#广义表的操作实现)
7. [算法复杂度分析](#算法复杂度分析)
8. [实际应用案例](#实际应用案例)
9. [完整实现示例](#完整实现示例)

---

## BF算法（Brute Force）

### 1.1 基本概念

BF算法，也称为暴力匹配算法或朴素字符串匹配算法，是一种简单直接的字符串匹配方法。

**算法思想**：
- 从主串的第一个字符开始，逐个字符与模式串进行比较
- 如果匹配失败，主串指针回退到下一个位置，模式串指针重置为0
- 重复上述过程直到找到匹配或遍历完整个主串

### 1.2 算法特点

| 特点 | 描述 |
|------|------|
| **时间复杂度** | O(m×n)，其中m为主串长度，n为模式串长度 |
| **空间复杂度** | O(1)，只需要常数个额外变量 |
| **优点** | 实现简单，易于理解，不需要预处理 |
| **缺点** | 效率较低，存在大量重复比较 |

### 1.3 BF算法实现

```cpp
#include <iostream>
#include <string>
#include <vector>
using namespace std;

class BFAlgorithm {
public:
    // 基本BF算法实现
    static int bfSearch(const string& text, const string& pattern) {
        int textLen = text.length();
        int patternLen = pattern.length();
        
        if (patternLen == 0) return 0;
        if (textLen < patternLen) return -1;
        
        for (int i = 0; i <= textLen - patternLen; i++) {
            int j = 0;
            while (j < patternLen && text[i + j] == pattern[j]) {
                j++;
            }
            if (j == patternLen) {
                return i;  // 找到匹配，返回起始位置
            }
        }
        
        return -1;  // 未找到匹配
    }
    
    // 查找所有匹配位置
    static vector<int> bfSearchAll(const string& text, const string& pattern) {
        vector<int> positions;
        int textLen = text.length();
        int patternLen = pattern.length();
        
        if (patternLen == 0) return positions;
        if (textLen < patternLen) return positions;
        
        for (int i = 0; i <= textLen - patternLen; i++) {
            int j = 0;
            while (j < patternLen && text[i + j] == pattern[j]) {
                j++;
            }
            if (j == patternLen) {
                positions.push_back(i);
            }
        }
        
        return positions;
    }
    
    // 统计匹配次数
    static int countMatches(const string& text, const string& pattern) {
        int count = 0;
        int textLen = text.length();
        int patternLen = pattern.length();
        
        if (patternLen == 0) return 0;
        if (textLen < patternLen) return 0;
        
        for (int i = 0; i <= textLen - patternLen; i++) {
            int j = 0;
            while (j < patternLen && text[i + j] == pattern[j]) {
                j++;
            }
            if (j == patternLen) {
                count++;
            }
        }
        
        return count;
    }
    
    // 显示匹配过程
    static void showMatchingProcess(const string& text, const string& pattern) {
        cout << "文本: " << text << endl;
        cout << "模式: " << pattern << endl;
        cout << "匹配过程:" << endl;
        
        int textLen = text.length();
        int patternLen = pattern.length();
        
        for (int i = 0; i <= textLen - patternLen; i++) {
            cout << "位置 " << i << ": ";
            
            // 显示当前比较位置
            for (int k = 0; k < i; k++) {
                cout << " ";
            }
            cout << pattern << endl;
            
            int j = 0;
            while (j < patternLen && text[i + j] == pattern[j]) {
                j++;
            }
            
            if (j == patternLen) {
                cout << "匹配成功！位置: " << i << endl;
                return;
            } else {
                cout << "匹配失败，继续下一个位置" << endl;
            }
        }
        
        cout << "未找到匹配" << endl;
    }
};
```

### 1.4 BF算法优化

```cpp
class OptimizedBF {
public:
    // 优化版本：减少不必要的比较
    static int optimizedBF(const string& text, const string& pattern) {
        int textLen = text.length();
        int patternLen = pattern.length();
        
        if (patternLen == 0) return 0;
        if (textLen < patternLen) return -1;
        
        int i = 0, j = 0;
        
        while (i < textLen && j < patternLen) {
            if (text[i] == pattern[j]) {
                i++;
                j++;
            } else {
                i = i - j + 1;  // 主串指针回退
                j = 0;           // 模式串指针重置
            }
        }
        
        if (j == patternLen) {
            return i - j;  // 返回匹配位置
        }
        
        return -1;
    }
    
    // 带统计信息的BF算法
    static int bfWithStats(const string& text, const string& pattern, int& comparisons) {
        comparisons = 0;
        int textLen = text.length();
        int patternLen = pattern.length();
        
        if (patternLen == 0) return 0;
        if (textLen < patternLen) return -1;
        
        for (int i = 0; i <= textLen - patternLen; i++) {
            int j = 0;
            while (j < patternLen) {
                comparisons++;
                if (text[i + j] == pattern[j]) {
                    j++;
                } else {
                    break;
                }
            }
            if (j == patternLen) {
                return i;
            }
        }
        
        return -1;
    }
};
```

---

## KMP算法（Knuth-Morris-Pratt）

### 2.1 基本概念

KMP算法是由Donald Knuth、James Morris和Vaughan Pratt在1977年共同提出的字符串匹配算法。KMP算法通过预处理模式串，避免BF算法中的重复比较，显著提高了匹配效率。

**算法思想**：
- 利用模式串自身的结构信息，避免主串指针的回退
- 通过预处理生成next数组，记录模式串中每个位置的最长公共前后缀长度
- 当匹配失败时，模式串指针根据next数组回退到合适位置

### 2.2 算法特点

| 特点 | 描述 |
|------|------|
| **时间复杂度** | O(m+n)，其中m为主串长度，n为模式串长度 |
| **空间复杂度** | O(n)，需要存储next数组 |
| **优点** | 效率高，避免重复比较，主串指针不回退 |
| **缺点** | 需要预处理，实现相对复杂 |

### 2.3 核心概念：部分匹配表（PMT）

**部分匹配表（Partial Match Table）**：记录模式串中每个位置的最长公共前后缀长度。

**前后缀定义**：
- 前缀：从字符串开头开始的连续子串
- 后缀：以字符串结尾的连续子串
- 公共前后缀：既是前缀又是后缀的子串

**示例**：模式串 "ababaca"
```
位置: 0 1 2 3 4 5 6
字符: a b a b a c a
PMT:  0 0 1 2 3 0 1
```

### 2.4 KMP算法实现

```cpp
class KMPAlgorithm {
private:
    // 计算next数组（优化版本）
    static vector<int> computeNext(const string& pattern) {
        int m = pattern.length();
        vector<int> next(m, 0);
        
        int j = 0;  // 前缀指针
        for (int i = 1; i < m; i++) {
            // 不匹配时，j回退到next[j-1]
            while (j > 0 && pattern[i] != pattern[j]) {
                j = next[j - 1];
            }
            
            // 匹配时，j前进
            if (pattern[i] == pattern[j]) {
                j++;
            }
            
            next[i] = j;
        }
        
        return next;
    }
    
    // 计算PMT数组（原始版本）
    static vector<int> computePMT(const string& pattern) {
        int m = pattern.length();
        vector<int> pmt(m, 0);
        
        for (int i = 1; i < m; i++) {
            int j = pmt[i - 1];
            
            while (j > 0 && pattern[i] != pattern[j]) {
                j = pmt[j - 1];
            }
            
            if (pattern[i] == pattern[j]) {
                j++;
            }
            
            pmt[i] = j;
        }
        
        return pmt;
    }
    
public:
    // 基本KMP算法实现
    static int kmpSearch(const string& text, const string& pattern) {
        int n = text.length();
        int m = pattern.length();
        
        if (m == 0) return 0;
        if (n < m) return -1;
        
        vector<int> next = computeNext(pattern);
        
        int i = 0;  // 主串指针
        int j = 0;  // 模式串指针
        
        while (i < n) {
            if (text[i] == pattern[j]) {
                i++;
                j++;
                
                if (j == m) {
                    return i - j;  // 找到匹配
                }
            } else {
                if (j > 0) {
                    j = next[j - 1];  // 根据next数组回退
                } else {
                    i++;  // 主串指针前进
                }
            }
        }
        
        return -1;  // 未找到匹配
    }
    
    // 查找所有匹配位置
    static vector<int> kmpSearchAll(const string& text, const string& pattern) {
        vector<int> positions;
        int n = text.length();
        int m = pattern.length();
        
        if (m == 0) return positions;
        if (n < m) return positions;
        
        vector<int> next = computeNext(pattern);
        
        int i = 0;  // 主串指针
        int j = 0;  // 模式串指针
        
        while (i < n) {
            if (text[i] == pattern[j]) {
                i++;
                j++;
                
                if (j == m) {
                    positions.push_back(i - j);
                    j = next[j - 1];  // 继续寻找下一个匹配
                }
            } else {
                if (j > 0) {
                    j = next[j - 1];
                } else {
                    i++;
                }
            }
        }
        
        return positions;
    }
    
    // 统计匹配次数
    static int countMatches(const string& text, const string& pattern) {
        int count = 0;
        int n = text.length();
        int m = pattern.length();
        
        if (m == 0) return 0;
        if (n < m) return 0;
        
        vector<int> next = computeNext(pattern);
        
        int i = 0;  // 主串指针
        int j = 0;  // 模式串指针
        
        while (i < n) {
            if (text[i] == pattern[j]) {
                i++;
                j++;
                
                if (j == m) {
                    count++;
                    j = next[j - 1];  // 继续寻找下一个匹配
                }
            } else {
                if (j > 0) {
                    j = next[j - 1];
                } else {
                    i++;
                }
            }
        }
        
        return count;
    }
    
    // 显示匹配过程
    static void showMatchingProcess(const string& text, const string& pattern) {
        cout << "文本: " << text << endl;
        cout << "模式: " << pattern << endl;
        
        vector<int> next = computeNext(pattern);
        cout << "Next数组: ";
        for (int i = 0; i < next.size(); i++) {
            cout << next[i] << " ";
        }
        cout << endl;
        
        cout << "匹配过程:" << endl;
        
        int n = text.length();
        int m = pattern.length();
        int i = 0, j = 0;
        int step = 0;
        
        while (i < n) {
            step++;
            cout << "步骤 " << step << ": ";
            
            // 显示当前比较位置
            for (int k = 0; k < i - j; k++) {
                cout << " ";
            }
            for (int k = 0; k < m; k++) {
                if (k < j) {
                    cout << "✓";  // 已匹配
                } else {
                    cout << pattern[k];
                }
            }
            cout << endl;
            
            if (text[i] == pattern[j]) {
                cout << "  字符匹配: " << text[i] << " == " << pattern[j] << endl;
                i++;
                j++;
                
                if (j == m) {
                    cout << "匹配成功！位置: " << i - j << endl;
                    return;
                }
            } else {
                cout << "  字符不匹配: " << text[i] << " != " << pattern[j] << endl;
                if (j > 0) {
                    cout << "  根据next数组回退: " << j << " -> " << next[j - 1] << endl;
                    j = next[j - 1];
                } else {
                    cout << "  主串指针前进" << endl;
                    i++;
                }
            }
        }
        
        cout << "未找到匹配" << endl;
    }
    
    // 计算next数组的详细过程
    static void showNextComputation(const string& pattern) {
        cout << "计算模式串 \"" << pattern << "\" 的next数组:" << endl;
        
        int m = pattern.length();
        vector<int> next(m, 0);
        
        cout << "位置: ";
        for (int i = 0; i < m; i++) {
            cout << i << " ";
        }
        cout << endl;
        
        cout << "字符: ";
        for (char c : pattern) {
            cout << c << " ";
        }
        cout << endl;
        
        int j = 0;
        for (int i = 1; i < m; i++) {
            cout << "i=" << i << ", j=" << j << ": ";
            
            while (j > 0 && pattern[i] != pattern[j]) {
                cout << "不匹配，j回退到next[" << j-1 << "]=" << next[j-1] << " ";
                j = next[j - 1];
            }
            
            if (pattern[i] == pattern[j]) {
                cout << "匹配，j++ ";
                j++;
            } else {
                cout << "不匹配，j保持0 ";
            }
            
            next[i] = j;
            cout << "-> next[" << i << "]=" << j << endl;
        }
        
        cout << "最终next数组: ";
        for (int i = 0; i < m; i++) {
            cout << next[i] << " ";
        }
        cout << endl;
    }
};
```

### 2.5 KMP算法优化

```cpp
class OptimizedKMP {
public:
    // 优化的next数组计算
    static vector<int> computeOptimizedNext(const string& pattern) {
        int m = pattern.length();
        vector<int> next(m, -1);  // 初始化为-1
        
        int j = -1;  // 前缀指针
        for (int i = 0; i < m - 1; i++) {
            while (j >= 0 && pattern[i] != pattern[j]) {
                j = next[j];
            }
            j++;
            next[i + 1] = j;
        }
        
        return next;
    }
    
    // 使用优化next数组的KMP算法
    static int optimizedKmpSearch(const string& text, const string& pattern) {
        int n = text.length();
        int m = pattern.length();
        
        if (m == 0) return 0;
        if (n < m) return -1;
        
        vector<int> next = computeOptimizedNext(pattern);
        
        int i = 0;  // 主串指针
        int j = 0;  // 模式串指针
        
        while (i < n && j < m) {
            if (j == -1 || text[i] == pattern[j]) {
                i++;
                j++;
            } else {
                j = next[j];
            }
        }
        
        if (j == m) {
            return i - j;
        }
        
        return -1;
    }
    
    // 带统计信息的KMP算法
    static int kmpWithStats(const string& text, const string& pattern, int& comparisons) {
        comparisons = 0;
        int n = text.length();
        int m = pattern.length();
        
        if (m == 0) return 0;
        if (n < m) return -1;
        
        vector<int> next = computeNext(pattern);
        
        int i = 0;
        int j = 0;
        
        while (i < n) {
            comparisons++;
            if (text[i] == pattern[j]) {
                i++;
                j++;
                
                if (j == m) {
                    return i - j;
                }
            } else {
                if (j > 0) {
                    j = next[j - 1];
                } else {
                    i++;
                }
            }
        }
        
        return -1;
    }
};
```

### 2.6 KMP算法在广义表中的应用

```cpp
class KMPInGeneralizedList {
public:
    // 在广义表中使用KMP算法查找模式
    static bool findPatternInList(GeneralizedList* list, const string& pattern) {
        if (list == nullptr || list->isEmpty()) {
            return false;
        }
        
        // 将广义表转换为字符串
        string listStr = list->toString();
        
        // 使用KMP算法查找
        return KMPAlgorithm::kmpSearch(listStr, pattern) != -1;
    }
    
    // 在广义表中查找所有匹配的模式
    static vector<string> findAllPatterns(GeneralizedList* list, const string& pattern) {
        vector<string> results;
        if (list == nullptr || list->isEmpty()) {
            return results;
        }
        
        // 递归查找所有原子
        findAllPatternsHelper(list, pattern, results);
        return results;
    }
    
    // 在广义表中替换模式
    static GeneralizedList* replacePatternInList(GeneralizedList* list, 
                                               const string& oldPattern, 
                                               const string& newPattern) {
        if (list == nullptr) return nullptr;
        
        GeneralizedList* result = new GeneralizedList();
        
        for (int i = 0; i < list->getLength(); i++) {
            Element* element = list->get(i);
            Element* newElement = nullptr;
            
            if (element->isAtom()) {
                Atom* atom = static_cast<Atom*>(element);
                string value = atom->getValue();
                
                // 使用KMP算法进行替换
                string replaced = replacePattern(value, oldPattern, newPattern);
                newElement = new Atom(replaced);
            } else if (element->isList()) {
                GeneralizedList* subList = static_cast<GeneralizedList*>(element);
                newElement = replacePatternInList(subList, oldPattern, newPattern);
            }
            
            if (newElement != nullptr) {
                result->insertTail(newElement);
            }
        }
        
        return result;
    }
    
private:
    static void findAllPatternsHelper(GeneralizedList* list, const string& pattern, 
                                     vector<string>& results) {
        if (list == nullptr || list->isEmpty()) {
            return;
        }
        
        for (int i = 0; i < list->getLength(); i++) {
            Element* element = list->get(i);
            
            if (element->isAtom()) {
                Atom* atom = static_cast<Atom*>(element);
                string value = atom->getValue();
                
                // 使用KMP算法查找模式
                if (KMPAlgorithm::kmpSearch(value, pattern) != -1) {
                    results.push_back(value);
                }
            } else if (element->isList()) {
                GeneralizedList* subList = static_cast<GeneralizedList*>(element);
                findAllPatternsHelper(subList, pattern, results);
            }
        }
    }
    
    static string replacePattern(const string& text, const string& oldPattern, 
                                const string& newPattern) {
        vector<int> positions = KMPAlgorithm::kmpSearchAll(text, oldPattern);
        
        if (positions.empty()) {
            return text;
        }
        
        string result = text;
        int offset = 0;
        
        for (int pos : positions) {
            result.replace(pos + offset, oldPattern.length(), newPattern);
            offset += newPattern.length() - oldPattern.length();
        }
        
        return result;
    }
};
```

### 2.7 KMP算法与BF算法对比

```cpp
class AlgorithmComparison {
public:
    // 性能对比测试
    static void comparePerformance(const string& text, const string& pattern) {
        cout << "=== 算法性能对比 ===" << endl;
        cout << "文本长度: " << text.length() << endl;
        cout << "模式长度: " << pattern.length() << endl;
        
        // BF算法测试
        auto start = chrono::high_resolution_clock::now();
        int bfResult = BFAlgorithm::bfSearch(text, pattern);
        auto end = chrono::high_resolution_clock::now();
        auto bfDuration = chrono::duration_cast<chrono::microseconds>(end - start);
        
        cout << "BF算法结果: " << bfResult << ", 耗时: " << bfDuration.count() << " 微秒" << endl;
        
        // KMP算法测试
        start = chrono::high_resolution_clock::now();
        int kmpResult = KMPAlgorithm::kmpSearch(text, pattern);
        end = chrono::high_resolution_clock::now();
        auto kmpDuration = chrono::duration_cast<chrono::microseconds>(end - start);
        
        cout << "KMP算法结果: " << kmpResult << ", 耗时: " << kmpDuration.count() << " 微秒" << endl;
        
        // 比较结果
        if (bfResult == kmpResult) {
            cout << "✓ 两种算法结果一致" << endl;
        } else {
            cout << "✗ 算法结果不一致" << endl;
        }
        
        // 性能提升
        if (bfDuration.count() > 0) {
            double speedup = (double)bfDuration.count() / kmpDuration.count();
            cout << "KMP算法性能提升: " << speedup << " 倍" << endl;
        }
    }
    
    // 比较次数统计
    static void compareComparisons(const string& text, const string& pattern) {
        cout << "\n=== 比较次数统计 ===" << endl;
        
        int bfComparisons = 0;
        BFAlgorithm::bfSearch(text, pattern);
        // 注意：这里需要修改BF算法以支持比较次数统计
        
        int kmpComparisons = 0;
        KMPAlgorithm::kmpSearch(text, pattern);
        // 注意：这里需要修改KMP算法以支持比较次数统计
        
        cout << "BF算法比较次数: " << bfComparisons << endl;
        cout << "KMP算法比较次数: " << kmpComparisons << endl;
        
        if (bfComparisons > 0) {
            double reduction = (double)(bfComparisons - kmpComparisons) / bfComparisons * 100;
            cout << "比较次数减少: " << reduction << "%" << endl;
        }
    }
};
```

---

## 广义表（Generalized List）

### 2.1 基本概念

广义表是线性表的推广，也称为列表（List）。广义表中的元素可以是原子（不可再分的基本元素），也可以是广义表（子表）。

**定义**：
- 广义表是n（n≥0）个元素a₁, a₂, ..., aₙ的有限序列
- 其中aᵢ可以是原子或广义表
- 当n=0时，称为空表

### 2.2 广义表的特点

| 特点 | 描述 |
|------|------|
| **层次性** | 广义表具有层次结构，可以嵌套 |
| **共享性** | 广义表可以被其他广义表共享 |
| **递归性** | 广义表可以是自己的子表 |
| **长度** | 最外层包含的元素个数 |
| **深度** | 嵌套的最大层数 |

### 2.3 广义表示例

```
L1 = ()                    // 空表
L2 = (a)                   // 长度为1，深度为1
L3 = (a, b, c)             // 长度为3，深度为1
L4 = (a, (b, c))           // 长度为2，深度为2
L5 = ((a, b), (c, d))      // 长度为2，深度为2
L6 = (a, (b, (c, d)))      // 长度为2，深度为3
L7 = (a, L3)               // 长度为2，深度为2（共享L3）
L8 = (L8)                  // 长度为1，深度为∞（递归）
```

### 2.4 广义表的抽象数据类型

```cpp
ADT GeneralizedList {
    数据对象：D = {a1, a2, ..., an}
    数据关系：R = {<ai-1, ai> | ai-1, ai ∈ D, i = 2, 3, ..., n}
    
    基本操作：
    InitGList(&L)          // 初始化广义表
    DestroyGList(&L)       // 销毁广义表
    CreateGList(&L, S)     // 根据字符串S创建广义表
    CopyGList(&T, L)       // 复制广义表
    GListLength(L)         // 求广义表长度
    GListDepth(L)          // 求广义表深度
    GListEmpty(L)          // 判断广义表是否为空
    GetHead(L)             // 取表头
    GetTail(L)             // 取表尾
    InsertFirst(&L, e)      // 在表头插入元素
    DeleteFirst(&L, &e)    // 删除表头元素
    TraverseGList(L, visit) // 遍历广义表
} ADT GeneralizedList
```

---

## BF算法在广义表中的应用

### 3.1 广义表字符串解析

BF算法在广义表的字符串解析中发挥重要作用，用于识别括号、逗号等分隔符。

```cpp
class GeneralizedListParser {
private:
    string input;
    int pos;
    
public:
    GeneralizedListParser(const string& str) : input(str), pos(0) {}
    
    // 跳过空白字符
    void skipWhitespace() {
        while (pos < input.length() && isspace(input[pos])) {
            pos++;
        }
    }
    
    // 解析原子
    string parseAtom() {
        skipWhitespace();
        string atom = "";
        
        while (pos < input.length() && 
               input[pos] != '(' && 
               input[pos] != ')' && 
               input[pos] != ',' &&
               !isspace(input[pos])) {
            atom += input[pos];
            pos++;
        }
        
        return atom;
    }
    
    // 解析广义表
    GeneralizedList* parseList() {
        skipWhitespace();
        
        if (pos >= input.length() || input[pos] != '(') {
            return nullptr;
        }
        
        pos++; // 跳过 '('
        skipWhitespace();
        
        // 检查是否为空表
        if (pos < input.length() && input[pos] == ')') {
            pos++; // 跳过 ')'
            return new GeneralizedList(); // 空表
        }
        
        GeneralizedList* list = new GeneralizedList();
        
        while (pos < input.length()) {
            skipWhitespace();
            
            if (pos >= input.length()) break;
            
            if (input[pos] == ')') {
                pos++; // 跳过 ')'
                break;
            }
            
            if (input[pos] == '(') {
                // 子表
                GeneralizedList* subList = parseList();
                if (subList != nullptr) {
                    list->insertTail(subList);
                }
            } else {
                // 原子
                string atom = parseAtom();
                if (!atom.empty()) {
                    list->insertTail(new Atom(atom));
                }
            }
            
            skipWhitespace();
            
            if (pos < input.length() && input[pos] == ',') {
                pos++; // 跳过 ','
            }
        }
        
        return list;
    }
    
    // 使用BF算法查找特定模式
    bool findPattern(const string& pattern) {
        return BFAlgorithm::bfSearch(input, pattern) != -1;
    }
    
    // 统计特定字符出现次数
    int countChar(char ch) {
        int count = 0;
        for (char c : input) {
            if (c == ch) count++;
        }
        return count;
    }
};
```

### 3.2 广义表模式匹配

```cpp
class GeneralizedListMatcher {
public:
    // 在广义表中查找特定原子
    static bool findAtom(GeneralizedList* list, const string& atom) {
        if (list == nullptr || list->isEmpty()) {
            return false;
        }
        
        for (int i = 0; i < list->getLength(); i++) {
            Element* element = list->get(i);
            
            if (element->isAtom()) {
                Atom* atomElement = static_cast<Atom*>(element);
                if (atomElement->getValue() == atom) {
                    return true;
                }
            } else if (element->isList()) {
                GeneralizedList* subList = static_cast<GeneralizedList*>(element);
                if (findAtom(subList, atom)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // 查找所有匹配的原子
    static vector<string> findAllAtoms(GeneralizedList* list, const string& pattern) {
        vector<string> results;
        findAllAtomsHelper(list, pattern, results);
        return results;
    }
    
private:
    static void findAllAtomsHelper(GeneralizedList* list, const string& pattern, 
                                   vector<string>& results) {
        if (list == nullptr || list->isEmpty()) {
            return;
        }
        
        for (int i = 0; i < list->getLength(); i++) {
            Element* element = list->get(i);
            
            if (element->isAtom()) {
                Atom* atomElement = static_cast<Atom*>(element);
                string value = atomElement->getValue();
                
                // 使用BF算法进行模式匹配
                if (BFAlgorithm::bfSearch(value, pattern) != -1) {
                    results.push_back(value);
                }
            } else if (element->isList()) {
                GeneralizedList* subList = static_cast<GeneralizedList*>(element);
                findAllAtomsHelper(subList, pattern, results);
            }
        }
    }
};
```

---

## 广义表的存储结构

### 4.1 链式存储结构

```cpp
// 广义表元素基类
class Element {
public:
    virtual ~Element() {}
    virtual bool isAtom() const = 0;
    virtual bool isList() const = 0;
    virtual string toString() const = 0;
};

// 原子类
class Atom : public Element {
private:
    string value;
    
public:
    Atom(const string& val) : value(val) {}
    
    bool isAtom() const override { return true; }
    bool isList() const override { return false; }
    
    string getValue() const { return value; }
    string toString() const override { return value; }
};

// 广义表类
class GeneralizedList : public Element {
private:
    struct Node {
        Element* data;
        Node* next;
        
        Node(Element* elem) : data(elem), next(nullptr) {}
    };
    
    Node* head;
    int length;
    
public:
    GeneralizedList() : head(nullptr), length(0) {}
    
    ~GeneralizedList() {
        clear();
    }
    
    // 拷贝构造函数
    GeneralizedList(const GeneralizedList& other) {
        head = nullptr;
        length = 0;
        copyFrom(other);
    }
    
    // 赋值操作符
    GeneralizedList& operator=(const GeneralizedList& other) {
        if (this != &other) {
            clear();
            copyFrom(other);
        }
        return *this;
    }
    
    bool isAtom() const override { return false; }
    bool isList() const override { return true; }
    
    string toString() const override {
        if (isEmpty()) return "()";
        
        string result = "(";
        Node* current = head;
        while (current != nullptr) {
            result += current->data->toString();
            if (current->next != nullptr) {
                result += ", ";
            }
            current = current->next;
        }
        result += ")";
        return result;
    }
    
    // 基本操作
    bool isEmpty() const {
        return head == nullptr;
    }
    
    int getLength() const {
        return length;
    }
    
    Element* get(int index) const {
        if (index < 0 || index >= length) {
            return nullptr;
        }
        
        Node* current = head;
        for (int i = 0; i < index; i++) {
            current = current->next;
        }
        return current->data;
    }
    
    void insertTail(Element* element) {
        Node* newNode = new Node(element);
        
        if (head == nullptr) {
            head = newNode;
        } else {
            Node* current = head;
            while (current->next != nullptr) {
                current = current->next;
            }
            current->next = newNode;
        }
        length++;
    }
    
    void insertHead(Element* element) {
        Node* newNode = new Node(element);
        newNode->next = head;
        head = newNode;
        length++;
    }
    
    Element* removeHead() {
        if (head == nullptr) {
            return nullptr;
        }
        
        Node* toDelete = head;
        Element* data = head->data;
        head = head->next;
        delete toDelete;
        length--;
        return data;
    }
    
    void clear() {
        while (head != nullptr) {
            Node* toDelete = head;
            head = head->next;
            delete toDelete->data;
            delete toDelete;
        }
        length = 0;
    }
    
    // 计算深度
    int getDepth() const {
        if (isEmpty()) return 1;
        
        int maxDepth = 0;
        Node* current = head;
        
        while (current != nullptr) {
            if (current->data->isList()) {
                GeneralizedList* subList = static_cast<GeneralizedList*>(current->data);
                int subDepth = subList->getDepth();
                maxDepth = max(maxDepth, subDepth);
            }
            current = current->next;
        }
        
        return maxDepth + 1;
    }
    
    // 获取表头
    Element* getHead() const {
        if (isEmpty()) return nullptr;
        return head->data;
    }
    
    // 获取表尾
    GeneralizedList* getTail() const {
        if (isEmpty() || length == 1) {
            return new GeneralizedList(); // 空表
        }
        
        GeneralizedList* tail = new GeneralizedList();
        Node* current = head->next;
        
        while (current != nullptr) {
            tail->insertTail(current->data);
            current = current->next;
        }
        
        return tail;
    }
    
private:
    void copyFrom(const GeneralizedList& other) {
        Node* current = other.head;
        while (current != nullptr) {
            Element* newElement = nullptr;
            
            if (current->data->isAtom()) {
                Atom* atom = static_cast<Atom*>(current->data);
                newElement = new Atom(atom->getValue());
            } else if (current->data->isList()) {
                GeneralizedList* subList = static_cast<GeneralizedList*>(current->data);
                newElement = new GeneralizedList(*subList);
            }
            
            if (newElement != nullptr) {
                insertTail(newElement);
            }
            
            current = current->next;
        }
    }
};
```

### 4.2 数组存储结构

```cpp
class ArrayBasedGeneralizedList {
private:
    struct ArrayNode {
        int tag;        // 0: 原子, 1: 子表
        union {
            string atom;
            int listIndex;
        } data;
        int next;
    };
    
    ArrayNode* nodes;
    int capacity;
    int freeHead;
    int listHead;
    int length;
    
public:
    ArrayBasedGeneralizedList(int cap = 1000) : capacity(cap), freeHead(0), 
                                                listHead(-1), length(0) {
        nodes = new ArrayNode[capacity];
        
        // 初始化空闲链表
        for (int i = 0; i < capacity - 1; i++) {
            nodes[i].next = i + 1;
        }
        nodes[capacity - 1].next = -1;
    }
    
    ~ArrayBasedGeneralizedList() {
        delete[] nodes;
    }
    
    // 分配节点
    int allocateNode() {
        if (freeHead == -1) return -1;
        
        int index = freeHead;
        freeHead = nodes[freeHead].next;
        return index;
    }
    
    // 释放节点
    void freeNode(int index) {
        nodes[index].next = freeHead;
        freeHead = index;
    }
    
    // 从字符串创建广义表
    void createFromString(const string& str) {
        GeneralizedListParser parser(str);
        GeneralizedList* list = parser.parseList();
        if (list != nullptr) {
            convertFromList(list);
        }
    }
    
    // 转换为字符串
    string toString() const {
        if (listHead == -1) return "()";
        
        string result = "(";
        int current = listHead;
        
        while (current != -1) {
            if (nodes[current].tag == 0) {
                result += nodes[current].data.atom;
            } else {
                // 子表
                result += "[" + to_string(nodes[current].data.listIndex) + "]";
            }
            
            if (nodes[current].next != -1) {
                result += ", ";
            }
            current = nodes[current].next;
        }
        
        result += ")";
        return result;
    }
    
private:
    void convertFromList(GeneralizedList* list) {
        // 实现从链式结构到数组结构的转换
        // 这里需要递归处理子表
    }
};
```

---

## 广义表的操作实现

### 5.1 基本操作实现

```cpp
class GeneralizedListOperations {
public:
    // 复制广义表
    static GeneralizedList* copyList(const GeneralizedList* original) {
        if (original == nullptr) return nullptr;
        
        GeneralizedList* copy = new GeneralizedList();
        
        for (int i = 0; i < original->getLength(); i++) {
            Element* element = original->get(i);
            Element* newElement = nullptr;
            
            if (element->isAtom()) {
                Atom* atom = static_cast<Atom*>(element);
                newElement = new Atom(atom->getValue());
            } else if (element->isList()) {
                GeneralizedList* subList = static_cast<GeneralizedList*>(element);
                newElement = copyList(subList);
            }
            
            if (newElement != nullptr) {
                copy->insertTail(newElement);
            }
        }
        
        return copy;
    }
    
    // 判断两个广义表是否相等
    static bool isEqual(const GeneralizedList* list1, const GeneralizedList* list2) {
        if (list1 == nullptr && list2 == nullptr) return true;
        if (list1 == nullptr || list2 == nullptr) return false;
        if (list1->getLength() != list2->getLength()) return false;
        
        for (int i = 0; i < list1->getLength(); i++) {
            Element* elem1 = list1->get(i);
            Element* elem2 = list2->get(i);
            
            if (elem1->isAtom() && elem2->isAtom()) {
                Atom* atom1 = static_cast<Atom*>(elem1);
                Atom* atom2 = static_cast<Atom*>(elem2);
                if (atom1->getValue() != atom2->getValue()) {
                    return false;
                }
            } else if (elem1->isList() && elem2->isList()) {
                GeneralizedList* subList1 = static_cast<GeneralizedList*>(elem1);
                GeneralizedList* subList2 = static_cast<GeneralizedList*>(elem2);
                if (!isEqual(subList1, subList2)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        
        return true;
    }
    
    // 计算广义表深度
    static int calculateDepth(const GeneralizedList* list) {
        if (list == nullptr || list->isEmpty()) return 1;
        
        int maxDepth = 0;
        for (int i = 0; i < list->getLength(); i++) {
            Element* element = list->get(i);
            if (element->isList()) {
                GeneralizedList* subList = static_cast<GeneralizedList*>(element);
                int subDepth = calculateDepth(subList);
                maxDepth = max(maxDepth, subDepth);
            }
        }
        
        return maxDepth + 1;
    }
    
    // 计算广义表长度
    static int calculateLength(const GeneralizedList* list) {
        if (list == nullptr) return 0;
        return list->getLength();
    }
    
    // 获取表头
    static Element* getHead(const GeneralizedList* list) {
        if (list == nullptr || list->isEmpty()) return nullptr;
        return list->get(0);
    }
    
    // 获取表尾
    static GeneralizedList* getTail(const GeneralizedList* list) {
        if (list == nullptr || list->isEmpty()) {
            return new GeneralizedList();
        }
        
        GeneralizedList* tail = new GeneralizedList();
        for (int i = 1; i < list->getLength(); i++) {
            Element* element = list->get(i);
            Element* newElement = nullptr;
            
            if (element->isAtom()) {
                Atom* atom = static_cast<Atom*>(element);
                newElement = new Atom(atom->getValue());
            } else if (element->isList()) {
                GeneralizedList* subList = static_cast<GeneralizedList*>(element);
                newElement = copyList(subList);
            }
            
            if (newElement != nullptr) {
                tail->insertTail(newElement);
            }
        }
        
        return tail;
    }
};
```

### 5.2 高级操作实现

```cpp
class AdvancedGeneralizedListOperations {
public:
    // 遍历广义表
    static void traverse(const GeneralizedList* list, function<void(const string&)> visit) {
        if (list == nullptr) return;
        
        for (int i = 0; i < list->getLength(); i++) {
            Element* element = list->get(i);
            
            if (element->isAtom()) {
                Atom* atom = static_cast<Atom*>(element);
                visit(atom->getValue());
            } else if (element->isList()) {
                GeneralizedList* subList = static_cast<GeneralizedList*>(element);
                traverse(subList, visit);
            }
        }
    }
    
    // 查找原子
    static bool findAtom(const GeneralizedList* list, const string& atom) {
        if (list == nullptr) return false;
        
        for (int i = 0; i < list->getLength(); i++) {
            Element* element = list->get(i);
            
            if (element->isAtom()) {
                Atom* atomElement = static_cast<Atom*>(element);
                if (atomElement->getValue() == atom) {
                    return true;
                }
            } else if (element->isList()) {
                GeneralizedList* subList = static_cast<GeneralizedList*>(element);
                if (findAtom(subList, atom)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // 替换原子
    static GeneralizedList* replaceAtom(const GeneralizedList* list, 
                                       const string& oldAtom, 
                                       const string& newAtom) {
        if (list == nullptr) return nullptr;
        
        GeneralizedList* result = new GeneralizedList();
        
        for (int i = 0; i < list->getLength(); i++) {
            Element* element = list->get(i);
            Element* newElement = nullptr;
            
            if (element->isAtom()) {
                Atom* atom = static_cast<Atom*>(element);
                if (atom->getValue() == oldAtom) {
                    newElement = new Atom(newAtom);
                } else {
                    newElement = new Atom(atom->getValue());
                }
            } else if (element->isList()) {
                GeneralizedList* subList = static_cast<GeneralizedList*>(element);
                newElement = replaceAtom(subList, oldAtom, newAtom);
            }
            
            if (newElement != nullptr) {
                result->insertTail(newElement);
            }
        }
        
        return result;
    }
    
    // 扁平化广义表
    static vector<string> flatten(const GeneralizedList* list) {
        vector<string> result;
        flattenHelper(list, result);
        return result;
    }
    
private:
    static void flattenHelper(const GeneralizedList* list, vector<string>& result) {
        if (list == nullptr) return;
        
        for (int i = 0; i < list->getLength(); i++) {
            Element* element = list->get(i);
            
            if (element->isAtom()) {
                Atom* atom = static_cast<Atom*>(element);
                result.push_back(atom->getValue());
            } else if (element->isList()) {
                GeneralizedList* subList = static_cast<GeneralizedList*>(element);
                flattenHelper(subList, result);
            }
        }
    }
};
```

---

## 算法复杂度分析

### 6.1 BF算法复杂度

| 操作 | 时间复杂度 | 空间复杂度 | 说明 |
|------|------------|------------|------|
| **基本匹配** | O(m×n) | O(1) | m为主串长度，n为模式串长度 |
| **查找所有匹配** | O(m×n) | O(k) | k为匹配次数 |
| **统计匹配次数** | O(m×n) | O(1) | 需要遍历整个主串 |

### 6.2 KMP算法复杂度

| 操作 | 时间复杂度 | 空间复杂度 | 说明 |
|------|------------|------------|------|
| **预处理next数组** | O(n) | O(n) | n为模式串长度 |
| **基本匹配** | O(m+n) | O(n) | m为主串长度，n为模式串长度 |
| **查找所有匹配** | O(m+n) | O(n+k) | k为匹配次数 |
| **统计匹配次数** | O(m+n) | O(n) | 需要遍历整个主串 |

### 6.3 广义表操作复杂度

| 操作 | 时间复杂度 | 空间复杂度 | 说明 |
|------|------------|------------|------|
| **创建广义表** | O(n) | O(n) | n为元素个数 |
| **复制广义表** | O(n) | O(n) | 需要递归复制所有元素 |
| **计算深度** | O(n) | O(d) | d为最大深度 |
| **查找原子** | O(n) | O(d) | 需要遍历所有元素 |
| **替换原子** | O(n) | O(n) | 需要创建新的广义表 |

### 6.4 性能对比

| 算法 | 预处理时间 | 匹配时间 | 空间复杂度 | 适用场景 |
|------|------------|----------|------------|----------|
| **BF算法** | O(1) | O(m×n) | O(1) | 简单匹配，模式串较短 |
| **KMP算法** | O(n) | O(m+n) | O(n) | 模式串较长，重复字符多 |
| **BM算法** | O(n) | O(m/n) | O(n) | 模式串较长，字符集较大 |

---

## 实际应用案例

### 7.1 文本编辑器中的查找功能

```cpp
class TextEditor {
private:
    string content;
    
public:
    TextEditor(const string& text) : content(text) {}
    
    // 使用BF算法查找文本
    int findTextBF(const string& pattern) {
        return BFAlgorithm::bfSearch(content, pattern);
    }
    
    // 使用KMP算法查找文本
    int findTextKMP(const string& pattern) {
        return KMPAlgorithm::kmpSearch(content, pattern);
    }
    
    // 查找所有匹配（使用KMP算法）
    vector<int> findAllText(const string& pattern) {
        return KMPAlgorithm::kmpSearchAll(content, pattern);
    }
    
    // 替换文本（使用KMP算法）
    string replaceText(const string& pattern, const string& replacement) {
        vector<int> positions = KMPAlgorithm::kmpSearchAll(content, pattern);
        
        string result = content;
        int offset = 0;
        
        for (int pos : positions) {
            result.replace(pos + offset, pattern.length(), replacement);
            offset += replacement.length() - pattern.length();
        }
        
        return result;
    }
    
    // 统计出现次数（使用KMP算法）
    int countOccurrences(const string& pattern) {
        return KMPAlgorithm::countMatches(content, pattern);
    }
    
    // 性能对比测试
    void compareAlgorithms(const string& pattern) {
        cout << "=== 算法性能对比 ===" << endl;
        
        // BF算法测试
        auto start = chrono::high_resolution_clock::now();
        int bfResult = findTextBF(pattern);
        auto end = chrono::high_resolution_clock::now();
        auto bfDuration = chrono::duration_cast<chrono::microseconds>(end - start);
        
        // KMP算法测试
        start = chrono::high_resolution_clock::now();
        int kmpResult = findTextKMP(pattern);
        end = chrono::high_resolution_clock::now();
        auto kmpDuration = chrono::duration_cast<chrono::microseconds>(end - start);
        
        cout << "BF算法结果: " << bfResult << ", 耗时: " << bfDuration.count() << " 微秒" << endl;
        cout << "KMP算法结果: " << kmpResult << ", 耗时: " << kmpDuration.count() << " 微秒" << endl;
        
        if (bfDuration.count() > 0) {
            double speedup = (double)bfDuration.count() / kmpDuration.count();
            cout << "KMP算法性能提升: " << speedup << " 倍" << endl;
        }
    }
};
```

### 7.2 表达式解析器

```cpp
class ExpressionParser {
private:
    GeneralizedList* expression;
    
public:
    ExpressionParser(const string& expr) {
        GeneralizedListParser parser(expr);
        expression = parser.parseList();
    }
    
    ~ExpressionParser() {
        delete expression;
    }
    
    // 计算表达式值
    double evaluate() {
        return evaluateHelper(expression);
    }
    
    // 显示表达式结构
    void showStructure() {
        if (expression != nullptr) {
            cout << "表达式结构: " << expression->toString() << endl;
        }
    }
    
private:
    double evaluateHelper(GeneralizedList* list) {
        if (list == nullptr || list->isEmpty()) {
            return 0.0;
        }
        
        if (list->getLength() == 1) {
            Element* element = list->get(0);
            if (element->isAtom()) {
                Atom* atom = static_cast<Atom*>(element);
                return stod(atom->getValue());
            }
        }
        
        // 处理运算符和操作数
        Element* op = list->get(0);
        if (op->isAtom()) {
            Atom* operatorAtom = static_cast<Atom*>(op);
            string opStr = operatorAtom->getValue();
            
            if (opStr == "+") {
                return evaluateHelper(static_cast<GeneralizedList*>(list->get(1))) +
                       evaluateHelper(static_cast<GeneralizedList*>(list->get(2)));
            } else if (opStr == "-") {
                return evaluateHelper(static_cast<GeneralizedList*>(list->get(1))) -
                       evaluateHelper(static_cast<GeneralizedList*>(list->get(2)));
            } else if (opStr == "*") {
                return evaluateHelper(static_cast<GeneralizedList*>(list->get(1))) *
                       evaluateHelper(static_cast<GeneralizedList*>(list->get(2)));
            } else if (opStr == "/") {
                return evaluateHelper(static_cast<GeneralizedList*>(list->get(1))) /
                       evaluateHelper(static_cast<GeneralizedList*>(list->get(2)));
            }
        }
        
        return 0.0;
    }
};
```

### 7.3 配置文件解析器

```cpp
class ConfigParser {
private:
    map<string, string> config;
    
public:
    void parseConfig(const string& configText) {
        vector<string> lines = splitLines(configText);
        
        for (const string& line : lines) {
            if (line.empty() || line[0] == '#') continue;
            
            int equalPos = BFAlgorithm::bfSearch(line, "=");
            if (equalPos != -1) {
                string key = line.substr(0, equalPos);
                string value = line.substr(equalPos + 1);
                
                // 去除空白字符
                key = trim(key);
                value = trim(value);
                
                config[key] = value;
            }
        }
    }
    
    string getValue(const string& key) {
        auto it = config.find(key);
        return (it != config.end()) ? it->second : "";
    }
    
    void setValue(const string& key, const string& value) {
        config[key] = value;
    }
    
    void printConfig() {
        for (const auto& pair : config) {
            cout << pair.first << " = " << pair.second << endl;
        }
    }
    
private:
    vector<string> splitLines(const string& text) {
        vector<string> lines;
        string current = "";
        
        for (char c : text) {
            if (c == '\n') {
                lines.push_back(current);
                current = "";
            } else {
                current += c;
            }
        }
        
        if (!current.empty()) {
            lines.push_back(current);
        }
        
        return lines;
    }
    
    string trim(const string& str) {
        int start = 0;
        int end = str.length() - 1;
        
        while (start <= end && isspace(str[start])) start++;
        while (end >= start && isspace(str[end])) end--;
        
        return str.substr(start, end - start + 1);
    }
};
```

---

## 完整实现示例

### 8.1 综合应用示例

```cpp
#include <iostream>
#include <string>
#include <vector>
#include <map>
#include <functional>
using namespace std;

int main() {
    cout << "=== BF算法与广义表综合应用示例 ===" << endl;
    
    // 1. BF算法示例
    cout << "\n1. BF算法示例:" << endl;
    string text = "Hello World, Hello C++, Hello Data Structures";
    string pattern = "Hello";
    
    cout << "文本: " << text << endl;
    cout << "模式: " << pattern << endl;
    
    int firstMatch = BFAlgorithm::bfSearch(text, pattern);
    cout << "首次匹配位置: " << firstMatch << endl;
    
    vector<int> allMatches = BFAlgorithm::bfSearchAll(text, pattern);
    cout << "所有匹配位置: ";
    for (int pos : allMatches) {
        cout << pos << " ";
    }
    cout << endl;
    
    int matchCount = BFAlgorithm::countMatches(text, pattern);
    cout << "匹配次数: " << matchCount << endl;
    
    // 2. KMP算法示例
    cout << "\n2. KMP算法示例:" << endl;
    cout << "文本: " << text << endl;
    cout << "模式: " << pattern << endl;
    
    int kmpFirstMatch = KMPAlgorithm::kmpSearch(text, pattern);
    cout << "KMP首次匹配位置: " << kmpFirstMatch << endl;
    
    vector<int> kmpAllMatches = KMPAlgorithm::kmpSearchAll(text, pattern);
    cout << "KMP所有匹配位置: ";
    for (int pos : kmpAllMatches) {
        cout << pos << " ";
    }
    cout << endl;
    
    int kmpMatchCount = KMPAlgorithm::countMatches(text, pattern);
    cout << "KMP匹配次数: " << kmpMatchCount << endl;
    
    // 显示KMP算法的next数组计算过程
    cout << "\nKMP算法next数组计算过程:" << endl;
    KMPAlgorithm::showNextComputation(pattern);
    
    // 显示KMP算法匹配过程
    cout << "\nKMP算法匹配过程:" << endl;
    KMPAlgorithm::showMatchingProcess(text, pattern);
    
    // 3. 广义表示例
    cout << "\n3. 广义表示例:" << endl;
    string listStr = "(a, (b, c), (d, (e, f)))";
    cout << "广义表字符串: " << listStr << endl;
    
    GeneralizedListParser parser(listStr);
    GeneralizedList* list = parser.parseList();
    
    if (list != nullptr) {
        cout << "解析结果: " << list->toString() << endl;
        cout << "长度: " << list->getLength() << endl;
        cout << "深度: " << list->getDepth() << endl;
        
        // 查找原子
        bool found = GeneralizedListMatcher::findAtom(list, "c");
        cout << "是否包含原子 'c': " << (found ? "是" : "否") << endl;
        
        // 遍历所有原子
        cout << "所有原子: ";
        vector<string> atoms = GeneralizedListMatcher::findAllAtoms(list, "");
        for (const string& atom : atoms) {
            cout << atom << " ";
        }
        cout << endl;
    }
    
    // 4. 文本编辑器示例
    cout << "\n4. 文本编辑器示例:" << endl;
    TextEditor editor("This is a test string for testing BF algorithm");
    
    int findPos = editor.findText("test");
    cout << "查找 'test' 位置: " << findPos << endl;
    
    vector<int> allPos = editor.findAllText("test");
    cout << "所有 'test' 位置: ";
    for (int pos : allPos) {
        cout << pos << " ";
    }
    cout << endl;
    
    string replaced = editor.replaceText("test", "demo");
    cout << "替换后: " << replaced << endl;
    
    // 5. 表达式解析示例
    cout << "\n5. 表达式解析示例:" << endl;
    string exprStr = "(+, 3, (*, 4, 5))";  // 3 + 4 * 5
    cout << "表达式: " << exprStr << endl;
    
    ExpressionParser exprParser(exprStr);
    exprParser.showStructure();
    
    // 6. 配置文件解析示例
    cout << "\n6. 配置文件解析示例:" << endl;
    string configText = 
        "# 配置文件\n"
        "database.host = localhost\n"
        "database.port = 3306\n"
        "database.name = testdb\n"
        "app.debug = true\n";
    
    ConfigParser configParser;
    configParser.parseConfig(configText);
    configParser.printConfig();
    
    cout << "\n获取配置值:" << endl;
    cout << "database.host = " << configParser.getValue("database.host") << endl;
    cout << "database.port = " << configParser.getValue("database.port") << endl;
    
    return 0;
}
```

### 8.2 性能测试示例

```cpp
class PerformanceTest {
public:
    static void testBFPerformance() {
        cout << "=== BF算法性能测试 ===" << endl;
        
        // 生成测试数据
        string text = generateRandomText(10000);
        string pattern = "abc";
        
        cout << "文本长度: " << text.length() << endl;
        cout << "模式长度: " << pattern.length() << endl;
        
        // 测试基本BF算法
        auto start = chrono::high_resolution_clock::now();
        int result = BFAlgorithm::bfSearch(text, pattern);
        auto end = chrono::high_resolution_clock::now();
        
        auto duration = chrono::duration_cast<chrono::microseconds>(end - start);
        cout << "BF算法耗时: " << duration.count() << " 微秒" << endl;
        cout << "匹配结果: " << result << endl;
        
        // 测试优化BF算法
        start = chrono::high_resolution_clock::now();
        int optimizedResult = OptimizedBF::optimizedBF(text, pattern);
        end = chrono::high_resolution_clock::now();
        
        duration = chrono::duration_cast<chrono::microseconds>(end - start);
        cout << "优化BF算法耗时: " << duration.count() << " 微秒" << endl;
        cout << "匹配结果: " << optimizedResult << endl;
    }
    
    static void testKMPPerformance() {
        cout << "\n=== KMP算法性能测试 ===" << endl;
        
        // 生成测试数据
        string text = generateRandomText(10000);
        string pattern = "abc";
        
        cout << "文本长度: " << text.length() << endl;
        cout << "模式长度: " << pattern.length() << endl;
        
        // 测试KMP算法
        auto start = chrono::high_resolution_clock::now();
        int result = KMPAlgorithm::kmpSearch(text, pattern);
        auto end = chrono::high_resolution_clock::now();
        
        auto duration = chrono::duration_cast<chrono::microseconds>(end - start);
        cout << "KMP算法耗时: " << duration.count() << " 微秒" << endl;
        cout << "匹配结果: " << result << endl;
        
        // 测试优化KMP算法
        start = chrono::high_resolution_clock::now();
        int optimizedResult = OptimizedKMP::optimizedKmpSearch(text, pattern);
        end = chrono::high_resolution_clock::now();
        
        duration = chrono::duration_cast<chrono::microseconds>(end - start);
        cout << "优化KMP算法耗时: " << duration.count() << " 微秒" << endl;
        cout << "匹配结果: " << optimizedResult << endl;
    }
    
    static void compareBFAndKMP() {
        cout << "\n=== BF算法与KMP算法性能对比 ===" << endl;
        
        // 生成测试数据
        string text = generateRandomText(50000);
        string pattern = "abcdef";
        
        cout << "文本长度: " << text.length() << endl;
        cout << "模式长度: " << pattern.length() << endl;
        
        // BF算法测试
        auto start = chrono::high_resolution_clock::now();
        int bfResult = BFAlgorithm::bfSearch(text, pattern);
        auto end = chrono::high_resolution_clock::now();
        auto bfDuration = chrono::duration_cast<chrono::microseconds>(end - start);
        
        // KMP算法测试
        start = chrono::high_resolution_clock::now();
        int kmpResult = KMPAlgorithm::kmpSearch(text, pattern);
        end = chrono::high_resolution_clock::now();
        auto kmpDuration = chrono::duration_cast<chrono::microseconds>(end - start);
        
        cout << "BF算法结果: " << bfResult << ", 耗时: " << bfDuration.count() << " 微秒" << endl;
        cout << "KMP算法结果: " << kmpResult << ", 耗时: " << kmpDuration.count() << " 微秒" << endl;
        
        if (bfDuration.count() > 0) {
            double speedup = (double)bfDuration.count() / kmpDuration.count();
            cout << "KMP算法性能提升: " << speedup << " 倍" << endl;
        }
        
        // 比较次数统计
        int bfComparisons = 0;
        BFAlgorithm::bfSearch(text, pattern);
        
        int kmpComparisons = 0;
        KMPAlgorithm::kmpSearch(text, pattern);
        
        cout << "BF算法比较次数: " << bfComparisons << endl;
        cout << "KMP算法比较次数: " << kmpComparisons << endl;
        
        if (bfComparisons > 0) {
            double reduction = (double)(bfComparisons - kmpComparisons) / bfComparisons * 100;
            cout << "比较次数减少: " << reduction << "%" << endl;
        }
    }
    
    static void testGeneralizedListPerformance() {
        cout << "\n=== 广义表性能测试 ===" << endl;
        
        // 生成测试数据
        string listStr = generateNestedList(5);
        cout << "测试广义表: " << listStr << endl;
        
        GeneralizedListParser parser(listStr);
        GeneralizedList* list = parser.parseList();
        
        if (list != nullptr) {
            // 测试深度计算
            auto start = chrono::high_resolution_clock::now();
            int depth = list->getDepth();
            auto end = chrono::high_resolution_clock::now();
            
            auto duration = chrono::duration_cast<chrono::microseconds>(end - start);
            cout << "深度计算耗时: " << duration.count() << " 微秒" << endl;
            cout << "计算深度: " << depth << endl;
            
            // 测试复制操作
            start = chrono::high_resolution_clock::now();
            GeneralizedList* copy = GeneralizedListOperations::copyList(list);
            end = chrono::high_resolution_clock::now();
            
            duration = chrono::duration_cast<chrono::microseconds>(end - start);
            cout << "复制操作耗时: " << duration.count() << " 微秒" << endl;
            
            delete copy;
        }
        
        delete list;
    }
    
private:
    static string generateRandomText(int length) {
        string chars = "abcdefghijklmnopqrstuvwxyz ";
        string result = "";
        
        for (int i = 0; i < length; i++) {
            result += chars[rand() % chars.length()];
        }
        
        return result;
    }
    
    static string generateNestedList(int depth) {
        if (depth == 0) {
            return "a";
        }
        
        string result = "(";
        for (int i = 0; i < 3; i++) {
            if (i > 0) result += ", ";
            result += generateNestedList(depth - 1);
        }
        result += ")";
        
        return result;
    }
};
```

---

## 总结

### BF算法与广义表的重要性

1. **BF算法**：
   - 简单直观的字符串匹配算法
   - 理解其他高级算法的基础
   - 在特定场景下仍然有用

2. **广义表**：
   - 线性表的推广，支持嵌套结构
   - 在表达式解析、数据结构表示中重要
   - 递归数据结构的典型代表

### 学习要点

1. **掌握BF算法原理**：理解暴力匹配的思想
2. **熟悉广义表结构**：理解递归和嵌套的概念
3. **学会性能分析**：比较不同算法的复杂度
4. **了解实际应用**：在文本处理、表达式解析中的应用

### 进阶学习

1. **KMP算法**：更高效的字符串匹配算法
2. **BM算法**：基于后缀的匹配算法
3. **正则表达式**：更强大的模式匹配
4. **语法分析**：基于广义表的编译器技术

BF算法与广义表是计算机科学中的基础概念，掌握它们对于理解更高级的算法和数据结构具有重要意义。
