#include <iostream>
#include <vector>
#include <stack>
using namespace std;

/**
 * 题目3：魔法符文序列验证
 * 使用栈模拟来验证目标序列是否可以通过输入序列的栈操作得到
 */
bool canObtainSequence(vector<int>& input, vector<int>& target) {
    stack<int> st;  // 模拟水晶栈
    int inputIndex = 0;  // 输入序列的当前索引
    int targetIndex = 0;  // 目标序列的当前索引
    
    // 遍历目标序列，尝试通过栈操作得到每个元素
    while (targetIndex < target.size()) {
        // 如果栈不为空且栈顶元素等于当前目标元素，直接弹出
        if (!st.empty() && st.top() == target[targetIndex]) {
            st.pop();
            targetIndex++;
        }
        // 如果输入序列还有元素，尝试压入栈中
        else if (inputIndex < input.size()) {
            st.push(input[inputIndex]);
            inputIndex++;
        }
        // 如果既不能从栈顶得到目标元素，也没有更多输入元素，则无法得到目标序列
        else {
            return false;
        }
    }
    
    // 如果成功处理了所有目标元素，返回true
    return targetIndex == target.size();
}

int main() {
    int n;
    cin >> n;
    
    vector<int> input(n);
    vector<int> target(n);
    
    // 读取输入序列
    for (int i = 0; i < n; i++) {
        cin >> input[i];
    }
    
    // 读取目标序列
    for (int i = 0; i < n; i++) {
        cin >> target[i];
    }
    
    // 验证是否可以得到目标序列
    if (canObtainSequence(input, target)) {
        cout << "YES" << endl;
    } else {
        cout << "NO" << endl;
    }
    
    return 0;
}
