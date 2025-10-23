#include <iostream>
#include <vector>
using namespace std;

const int MOD = 1e9 + 7;

int main() {
    int n, p, m;
    cin >> n >> p >> m;
    
    // 读取矩阵A (n x p)
    vector<vector<long long>> A(n, vector<long long>(p));
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < p; j++) {
            cin >> A[i][j];
        }
    }
    
    // 读取矩阵B (p x m)
    vector<vector<long long>> B(p, vector<long long>(m));
    for (int i = 0; i < p; i++) {
        for (int j = 0; j < m; j++) {
            cin >> B[i][j];
        }
    }
    
    // 计算矩阵乘法 C = A * B
    vector<vector<long long>> C(n, vector<long long>(m, 0));
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            for (int k = 0; k < p; k++) {
                // 累加 A[i][k] * B[k][j]
                C[i][j] = (C[i][j] + (A[i][k] * B[k][j]) % MOD) % MOD;
            }
        }
    }
    
    // 输出结果，确保非负数
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            // 确保结果为非负数
            cout << (C[i][j] + MOD) % MOD;
            if (j < m - 1) cout << " ";
        }
        cout << endl;
    }
    
    return 0;
}
