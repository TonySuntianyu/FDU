import java.util.*;

/**
 * 两数之和问题解决方案
 * 给定一个整数目标值 target 和一个整数数组 nums，
 * 找出和为目标值 target 的两个整数，并返回它们的数组下标
 */
public class TwoSum {
    
    /**
     * 使用哈希表解决两数之和问题
     * 时间复杂度：O(n)
     * 空间复杂度：O(n)
     * 
     * @param nums 整数数组
     * @param target 目标值
     * @return 两个数的下标数组
     */
    public static int[] twoSum(int[] nums, int target) {
        // 使用HashMap存储数组元素和对应的下标
        Map<Integer, Integer> map = new HashMap<>();
        
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            
            // 如果哈希表中存在补数，说明找到了答案
            if (map.containsKey(complement)) {
                return new int[]{map.get(complement), i};
            }
            
            // 将当前元素和下标存入哈希表
            map.put(nums[i], i);
        }
        
        // 根据题目保证，每种输入只会对应一个答案
        // 如果没找到，返回空数组（理论上不会到达这里）
        return new int[]{};
    }
    
    /**
     * 主函数，处理输入输出
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            // 读取目标值
            int target = scanner.nextInt();
            
            // 读取数组元素
            List<Integer> numsList = new ArrayList<>();
            while (scanner.hasNextInt()) {
                numsList.add(scanner.nextInt());
            }
            
            // 转换为数组
            int[] nums = new int[numsList.size()];
            for (int i = 0; i < numsList.size(); i++) {
                nums[i] = numsList.get(i);
            }
            
            // 调用两数之和函数
            int[] result = twoSum(nums, target);
            
            // 输出结果
            System.out.println(result[0] + " " + result[1]);
        }
    }
}
