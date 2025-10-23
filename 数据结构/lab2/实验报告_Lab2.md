# 数据结构 Lab2 实验报告

**实验名称：** 栈的应用实验  
**实验时间：** 2025年秋季学期  
**实验目的：** 通过三个编程题目深入理解栈数据结构的应用，掌握单调栈、栈模拟等算法技巧

---

## 实验内容概述

本次实验包含三个编程题目，分别涉及：
1. **下一个更大元素（循环数组）** - 使用单调栈解决循环数组问题
2. **字符串解码** - 使用栈处理嵌套编码字符串
3. **魔法符文序列验证** - 使用栈模拟验证序列操作的可能性

---

## 题目一：下一个更大元素

### 问题描述
给定一个循环数组，返回每个元素的下一个更大元素。如果不存在更大的元素，返回-1。

### 算法设计
使用**单调栈**算法解决循环数组问题：

1. **核心思想**：维护一个单调递减的栈，栈中存储数组下标
2. **循环处理**：由于是循环数组，需要遍历两轮（2*n次）
3. **栈操作**：当当前元素大于栈顶元素时，栈顶元素找到了下一个更大元素

### 关键代码分析
```cpp
vector<int> nextGreaterElements(vector<int>& nums) {
    int n = nums.size();
    vector<int> result(n, -1);  // 初始化结果数组
    stack<int> st;  // 单调栈，存储数组下标
    
    // 遍历两轮处理循环数组
    for (int i = 0; i < 2 * n; i++) {
        int index = i % n;  // 获取实际数组下标
        
        // 维护单调栈
        while (!st.empty() && nums[st.top()] < nums[index]) {
            result[st.top()] = nums[index];
            st.pop();
        }
        
        // 只在第一轮遍历时将下标入栈
        if (i < n) {
            st.push(index);
        }
    }
    
    return result;
}
```

### 时间复杂度
- **时间复杂度**：O(n) - 每个元素最多入栈和出栈一次
- **空间复杂度**：O(n) - 栈的空间复杂度

### 测试结果
- 输入：`1 2 1` → 输出：`2 -1 2`
- 输入：`5 4 3 6 1` → 输出：`6 6 6 -1 5`

---

## 题目二：字符串解码

### 问题描述
解码经过编码的字符串，其中数字k后面紧跟着方括号[]，表示方括号内的字符串重复k次。编码可能嵌套。

### 算法设计
使用**双栈**结构处理嵌套编码：

1. **字符串栈**：存储外层字符串
2. **数字栈**：存储重复次数
3. **状态管理**：维护当前字符串和当前数字

### 关键代码分析
```cpp
string decodeString(string s) {
    stack<string> strStack;  // 存储字符串
    stack<int> numStack;     // 存储重复次数
    string currentStr = "";  // 当前正在构建的字符串
    int currentNum = 0;      // 当前正在解析的数字
    
    for (char c : s) {
        if (isdigit(c)) {
            // 累积数字
            currentNum = currentNum * 10 + (c - '0');
        } else if (c == '[') {
            // 遇到左括号，保存当前状态
            numStack.push(currentNum);
            strStack.push(currentStr);
            currentNum = 0;
            currentStr = "";
        } else if (c == ']') {
            // 遇到右括号，重复字符串
            int repeatTimes = numStack.top();
            numStack.pop();
            string prevStr = strStack.top();
            strStack.pop();
            
            string repeatedStr = "";
            for (int i = 0; i < repeatTimes; i++) {
                repeatedStr += currentStr;
            }
            currentStr = prevStr + repeatedStr;
        } else {
            // 字母直接添加
            currentStr += c;
        }
    }
    
    return currentStr;
}
```

### 时间复杂度
- **时间复杂度**：O(n) - 其中n是解码后字符串的长度
- **空间复杂度**：O(n) - 栈的空间复杂度

### 测试结果
- 输入：`3[a]2[bc]` → 输出：`aaabcbc`
- 输入：`3[a2[c]]` → 输出：`accaccacc`

---

## 题目三：魔法符文序列验证

### 问题描述
验证理想符文序列是否可能由输入符文序列通过栈操作得到。

### 算法设计
使用**栈模拟**算法：

1. **模拟过程**：模拟将输入序列元素依次入栈，然后从栈顶弹出元素
2. **匹配策略**：优先从栈顶获取目标元素，否则从输入序列获取新元素
3. **验证条件**：能够成功处理所有目标元素

### 关键代码分析
```cpp
bool canObtainSequence(vector<int>& input, vector<int>& target) {
    stack<int> st;  // 模拟水晶栈
    int inputIndex = 0;  // 输入序列的当前索引
    int targetIndex = 0;  // 目标序列的当前索引
    
    while (targetIndex < target.size()) {
        // 优先从栈顶获取目标元素
        if (!st.empty() && st.top() == target[targetIndex]) {
            st.pop();
            targetIndex++;
        }
        // 否则从输入序列获取新元素
        else if (inputIndex < input.size()) {
            st.push(input[inputIndex]);
            inputIndex++;
        }
        // 无法获取目标元素
        else {
            return false;
        }
    }
    
    return targetIndex == target.size();
}
```

### 时间复杂度
- **时间复杂度**：O(n) - 每个元素最多入栈和出栈一次
- **空间复杂度**：O(n) - 栈的空间复杂度

### 测试结果
- 输入1：`5\n1 2 3 4 5\n4 5 3 2 1` → 输出：`YES`
- 输入2：`5\n1 2 3 4 5\n3 5 4 1 2` → 输出：`NO`

---

## 实验总结

### 算法技巧总结

1. **单调栈**：解决"下一个更大元素"类问题的高效方法
2. **双栈结构**：处理嵌套结构问题的经典模式
3. **栈模拟**：验证序列操作可能性的重要方法

### 编程经验

1. **代码注释**：为关键算法步骤添加详细注释，提高代码可读性
2. **边界处理**：注意循环数组的边界条件和栈的空栈检查
3. **状态管理**：在复杂算法中合理管理多个状态变量

### 时间复杂度分析

所有三个算法的时间复杂度都是O(n)，空间复杂度也是O(n)，体现了栈数据结构在处理这些问题时的高效性。

### 实验收获

通过本次实验，深入理解了栈数据结构的多种应用场景：
- 单调栈在解决数组问题中的威力
- 栈在处理嵌套结构时的天然优势
- 栈模拟在验证操作序列时的有效性

这些算法技巧在实际编程和算法竞赛中都有广泛应用，为后续学习更复杂的数据结构和算法奠定了坚实基础。

---

**实验完成时间：** 2025年10月  
**实验环境：** C++  
**代码质量：** 已通过洛谷平台测试，所有测试用例均通过

