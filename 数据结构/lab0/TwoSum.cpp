#include <iostream>
#include <vector>
#include <unordered_map>
using namespace std;

vector<int> twoSum(vector<int>& nums, int target) {
    // 使用unordered_map存储数组元素和对应的下标
    unordered_map<int, int> map;
    
    for (int i = 0; i < nums.size(); i++) {
        int complement = target - nums[i];
        
        // 如果哈希表中存在补数，说明找到了答案
        if (map.find(complement) != map.end()) {
            return {map[complement], i};
        }
        
        // 将当前元素和下标存入哈希表
        map[nums[i]] = i;
    }
    
    // 根据题目保证，每种输入只会对应一个答案
    // 如果没找到，返回空数组（理论上不会到达这里）
    return {};
}

/**
 * 主函数，处理输入输出
 */
int main() {
    int target;
    cin >> target;
    
    vector<int> nums;
    int num;
    
    // 读取数组元素
    while (cin >> num) {
        nums.push_back(num);
    }
    
    // 调用两数之和函数
    vector<int> result = twoSum(nums, target);
    
    // 输出结果
    cout << result[0] << " " << result[1] << endl;
    
    return 0;
}
