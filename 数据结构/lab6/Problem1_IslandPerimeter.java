import java.util.Scanner;

/**
 * 题目1：岛屿的周长总和
 * 
 * 算法思路：
 * 对于每个陆地格子（值为1），检查其上下左右四个方向：
 * - 如果相邻位置是海洋（值为0）或超出边界，则该边计入周长
 * - 遍历所有陆地格子，累加周长即可
 * 
 * 时间复杂度：O(m*n)，其中m和n是网格的行数和列数
 * 空间复杂度：O(m*n)，用于存储网格
 */
public class Problem1_IslandPerimeter {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // 读取网格大小
        int m = scanner.nextInt();
        int n = scanner.nextInt();
        
        // 读取网格数据
        int[][] grid = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                grid[i][j] = scanner.nextInt();
            }
        }
        
        // 计算周长总和
        int perimeter = calculatePerimeter(grid, m, n);
        System.out.println(perimeter);
        
        scanner.close();
    }
    
    /**
     * 计算所有岛屿的周长总和
     * 
     * @param grid 网格数组，1表示陆地，0表示海洋
     * @param m 网格行数
     * @param n 网格列数
     * @return 所有岛屿的周长总和
     */
    private static int calculatePerimeter(int[][] grid, int m, int n) {
        int totalPerimeter = 0;
        
        // 定义四个方向：上、下、左、右
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        
        // 遍历网格中的每个格子
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                // 如果当前格子是陆地
                if (grid[i][j] == 1) {
                    // 检查四个方向
                    for (int k = 0; k < 4; k++) {
                        int newX = i + dx[k];
                        int newY = j + dy[k];
                        
                        // 如果相邻位置超出边界或是海洋，则该边计入周长
                        if (newX < 0 || newX >= m || newY < 0 || newY >= n || grid[newX][newY] == 0) {
                            totalPerimeter++;
                        }
                    }
                }
            }
        }
        
        return totalPerimeter;
    }
}

