#include <iostream>
#include <string>
#include <vector>
using namespace std;

// 构建KMP算法的next数组
vector<int> buildNext(const string& pattern) {
    int m = pattern.length();
    vector<int> next(m, 0);
    
    int j = 0; // 模式串指针
    for (int i = 1; i < m; i++) {
        // 当字符不匹配时，回退到next[j-1]位置
        while (j > 0 && pattern[i] != pattern[j]) {
            j = next[j - 1];
        }
        
        // 如果字符匹配，j向前移动
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
    
    // 如果模式串长度大于文本串，直接返回0
    if (m > n) {
        return 0;
    }
    
    // 如果模式串为空，返回0
    if (m == 0) {
        return 0;
    }
    
    vector<int> next = buildNext(pattern);
    int count = 0;
    int j = 0; // 模式串指针
    
    for (int i = 0; i < n; i++) {
        // 当字符不匹配时，回退到next[j-1]位置
        while (j > 0 && text[i] != pattern[j]) {
            j = next[j - 1];
        }
        
        // 如果字符匹配，j向前移动
        if (text[i] == pattern[j]) {
            j++;
        }
        
        // 如果完全匹配，计数并继续搜索（允许重叠）
        if (j == m) {
            count++;
            // 回退到next[j-1]位置继续搜索
            j = next[j - 1];
        }
    }
    
    return count;
}

int main() {
    string A, B;
    cin >> A >> B;
    
    int result = kmpSearch(A, B);
    cout << result << endl;
    
    return 0;
}
