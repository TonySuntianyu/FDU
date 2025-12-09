#include <iostream>
#include <vector>
#include <climits>
using namespace std;

// 复制Problem2的代码用于测试
vector<vector<int>> floydWarshall(vector<vector<int>>& graph, int n) {
    vector<vector<int>> dist(n, vector<int>(n));
    
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            if (i == j) {
                dist[i][j] = 0;
            } else if (graph[i][j] != 0) {
                dist[i][j] = graph[i][j];
            } else {
                dist[i][j] = INT_MAX;
            }
        }
    }
    
    for (int k = 0; k < n; k++) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (dist[i][k] != INT_MAX && 
                    dist[k][j] != INT_MAX &&
                    dist[i][k] + dist[k][j] < dist[i][j]) {
                    dist[i][j] = dist[i][k] + dist[k][j];
                }
            }
        }
    }
    
    return dist;
}

int findOptimalFireStation(vector<vector<int>>& graph, int n) {
    vector<vector<int>> dist = floydWarshall(graph, n);
    
    int minEccentricity = INT_MAX;
    int optimalNode = -1;
    
    for (int i = 0; i < n; i++) {
        int eccentricity = 0;
        bool reachable = true;
        
        for (int j = 0; j < n; j++) {
            if (i == j) {
                continue;
            }
            if (dist[i][j] == INT_MAX) {
                reachable = false;
                break;
            }
            if (dist[i][j] > eccentricity) {
                eccentricity = dist[i][j];
            }
        }
        
        if (!reachable) {
            continue;
        }
        
        if (eccentricity < minEccentricity) {
            minEccentricity = eccentricity;
            optimalNode = i;
        }
    }
    
    return optimalNode;
}

int main() {
    cout << "=== 测试用例1：连通图（原样例1）===" << endl;
    vector<vector<int>> graph1 = {
        {0, 1, 3},
        {1, 0, 2},
        {3, 2, 0}
    };
    int result1 = findOptimalFireStation(graph1, 3);
    cout << "结果: " << result1 << " (期望: 1)" << endl;
    
    cout << "\n=== 测试用例2：连通图（原样例2）===" << endl;
    vector<vector<int>> graph2 = {
        {0, 2, 0, 5},
        {2, 0, 3, 0},
        {0, 3, 0, 1},
        {5, 0, 1, 0}
    };
    int result2 = findOptimalFireStation(graph2, 4);
    cout << "结果: " << result2 << " (期望: 2)" << endl;
    
    cout << "\n=== 测试用例3：非连通图（两个分离的连通分量）===" << endl;
    vector<vector<int>> graph3 = {
        {0, 1, 0, 0},
        {1, 0, 0, 0},
        {0, 0, 0, 1},
        {0, 0, 1, 0}
    };
    int result3 = findOptimalFireStation(graph3, 4);
    cout << "结果: " << result3 << " (期望: -1)" << endl;
    
    cout << "\n=== 测试用例4：非连通图（孤立节点）===" << endl;
    vector<vector<int>> graph4 = {
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0}
    };
    int result4 = findOptimalFireStation(graph4, 3);
    cout << "结果: " << result4 << " (期望: -1)" << endl;
    
    cout << "\n=== 测试用例5：非连通图（一个连通分量+孤立节点）===" << endl;
    vector<vector<int>> graph5 = {
        {0, 1, 0},
        {1, 0, 0},
        {0, 0, 0}
    };
    int result5 = findOptimalFireStation(graph5, 3);
    cout << "结果: " << result5 << " (期望: -1)" << endl;
    
    return 0;
}

