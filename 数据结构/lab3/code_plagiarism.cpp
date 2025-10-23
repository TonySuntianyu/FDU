#include <iostream>
#include <string>
#include <vector>
#include <map>
using namespace std;

// 构建KMP算法的next数组
vector<int> buildNext(const string& pattern) {
    int m = pattern.length();
    vector<int> next(m, 0);
    
    int j = 0;
    for (int i = 1; i < m; i++) {
        while (j > 0 && pattern[i] != pattern[j]) {
            j = next[j - 1];
        }
        
        if (pattern[i] == pattern[j]) {
            j++;
        }
        
        next[i] = j;
    }
    
    return next;
}

// 使用KMP算法计算模式串在文本串中的出现次数
int kmpSearch(const string& text, const string& pattern) {
    int n = text.length();
    int m = pattern.length();
    
    if (m > n || m == 0) {
        return 0;
    }
    
    vector<int> next = buildNext(pattern);
    int count = 0;
    int j = 0;
    
    for (int i = 0; i < n; i++) {
        while (j > 0 && text[i] != pattern[j]) {
            j = next[j - 1];
        }
        
        if (text[i] == pattern[j]) {
            j++;
        }
        
        if (j == m) {
            count++;
            j = next[j - 1];
        }
    }
    
    return count;
}

// 检测代码片段T在代码S中的出现次数
// 通过结构模式匹配检测相似性
int countCodeMatches(const string& S, const string& T) {
    int n = S.length();
    int m = T.length();
    
    if (m > n || m == 0) {
        return 0;
    }
    
    int count = 0;
    
    // 尝试所有可能的起始位置
    for (int start = 0; start <= n - m; start++) {
        // 检查从start位置开始的长度为m的子串是否与T匹配
        map<char, char> varMap; // 变量映射
        map<char, char> nonVarMap; // 非变量符号映射
        
        bool match = true;
        
        for (int i = 0; i < m; i++) {
            char sChar = S[start + i];
            char tChar = T[i];
            
            if (sChar >= 'a' && sChar <= 'z' && tChar >= 'a' && tChar <= 'z') {
                // 都是变量
                if (varMap.find(sChar) == varMap.end()) {
                    varMap[sChar] = tChar;
                } else if (varMap[sChar] != tChar) {
                    match = false;
                    break;
                }
            } else if (sChar >= 'A' && sChar <= 'Z' && tChar >= 'A' && tChar <= 'Z') {
                // 都是非变量符号
                if (nonVarMap.find(sChar) == nonVarMap.end()) {
                    nonVarMap[sChar] = tChar;
                } else if (nonVarMap[sChar] != tChar) {
                    match = false;
                    break;
                }
            } else if (sChar == tChar) {
                // 其他字符直接比较
                continue;
            } else {
                match = false;
                break;
            }
        }
        
        if (match) {
            count++;
        }
    }
    
    return count;
}

int main() {
    string S, T;
    cin >> S >> T;
    
    // 使用代码匹配算法计算T在S中的出现次数
    int result = countCodeMatches(S, T);
    
    cout << result << endl;
    
    return 0;
}
