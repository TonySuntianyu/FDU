#include <iostream>
#include <string>
#include <stack>
using namespace std;

/**
 * 题目2：字符串解码
 * 使用栈来处理嵌套的编码字符串解码问题
 */
string decodeString(string s) {
    stack<string> strStack;  // 存储字符串
    stack<int> numStack;     // 存储重复次数
    string currentStr = "";  // 当前正在构建的字符串
    int currentNum = 0;      // 当前正在解析的数字
    
    for (char c : s) {
        if (isdigit(c)) {
            // 如果是数字，累积到currentNum中
            currentNum = currentNum * 10 + (c - '0');
        } else if (c == '[') {
            // 遇到左括号，将当前数字和字符串分别入栈
            numStack.push(currentNum);
            strStack.push(currentStr);
            // 重置当前状态
            currentNum = 0;
            currentStr = "";
        } else if (c == ']') {
            // 遇到右括号，需要重复当前字符串
            int repeatTimes = numStack.top();
            numStack.pop();
            string prevStr = strStack.top();
            strStack.pop();
            
            // 将当前字符串重复repeatTimes次
            string repeatedStr = "";
            for (int i = 0; i < repeatTimes; i++) {
                repeatedStr += currentStr;
            }
            
            // 与之前的字符串合并
            currentStr = prevStr + repeatedStr;
        } else {
            // 如果是字母，直接添加到当前字符串
            currentStr += c;
        }
    }
    
    return currentStr;
}

int main() {
    string s;
    getline(cin, s);  // 读取一行输入
    
    string result = decodeString(s);
    cout << result << endl;
    
    return 0;
}
