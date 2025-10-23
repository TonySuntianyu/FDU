#include <iostream>
#include <vector>
#include <stack>
using namespace std;

/**
 * 题目1：下一个更大元素（循环数组）
 * 使用单调栈解决循环数组中每个元素的下一个更大元素问题
 */
vector<int> nextGreaterElements(vector<int>& nums) {
    int n = nums.size();
    vector<int> result(n, -1);  // 初始化结果数组，默认值为-1
    stack<int> st;  // 单调栈，存储数组下标
    
    // 由于是循环数组，需要遍历两轮
    for (int i = 0; i < 2 * n; i++) {
        int index = i % n;  // 获取实际数组下标
        
        // 维护单调栈：如果当前元素大于栈顶元素，则栈顶元素找到了下一个更大元素
        while (!st.empty() && nums[st.top()] < nums[index]) {
            result[st.top()] = nums[index];
            st.pop();
        }
        
        // 只在第一轮遍历时将下标入栈，避免重复处理
        if (i < n) {
            st.push(index);
        }
    }
    
    return result;
}

int main() {
    vector<int> nums;
    int num;
    
    // 读取输入
    while (cin >> num) {
        nums.push_back(num);
    }
    
    // 计算下一个更大元素
    vector<int> result = nextGreaterElements(nums);
    
    // 输出结果
    for (int i = 0; i < result.size(); i++) {
        if (i > 0) cout << " ";
        cout << result[i];
    }
    cout << endl;
    
    return 0;
}
