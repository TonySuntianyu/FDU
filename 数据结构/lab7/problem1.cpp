#include <iostream>
#include <vector>
#include <unordered_map>
#include <algorithm>
using namespace std;

// 并查集数据结构
class UnionFind {
private:
    unordered_map<int, int> parent;  // 父节点映射
    unordered_map<int, int> rank;    // 秩（用于路径压缩优化）

public:
    // 查找根节点，带路径压缩
    int find(int x) {
        if (parent.find(x) == parent.end()) {
            parent[x] = x;
            rank[x] = 0;
            return x;
        }
        if (parent[x] != x) {
            parent[x] = find(parent[x]);  // 路径压缩
        }
        return parent[x];
    }

    // 合并两个集合
    void unite(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        
        if (rootX == rootY) return;  // 已经在同一集合中
        
        // 按秩合并
        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX;
        } else {
            parent[rootY] = rootX;
            rank[rootX]++;
        }
    }

    // 判断两个元素是否在同一集合中
    bool same(int x, int y) {
        return find(x) == find(y);
    }

    // 清空并查集（用于处理多个测试用例）
    void clear() {
        parent.clear();
        rank.clear();
    }
};

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int t;
    cin >> t;

    while (t--) {
        int n;
        cin >> n;

        UnionFind uf;
        vector<pair<int, int>> inequalities;  // 存储不等约束条件

        // 先处理所有相等约束（e=1），将它们合并到同一集合
        for (int i = 0; i < n; i++) {
            int x, y, e;
            cin >> x >> y >> e;

            if (e == 1) {
                // 相等约束：合并到同一集合
                uf.unite(x, y);
            } else {
                // 不等约束：先记录下来，稍后检查
                inequalities.push_back({x, y});
            }
        }

        // 检查所有不等约束是否与相等约束冲突
        bool valid = true;
        for (auto& p : inequalities) {
            int x = p.first, y = p.second;
            // 如果两个应该不等的变量在同一个集合中，则冲突
            if (uf.same(x, y)) {
                valid = false;
                break;
            }
        }

        // 输出结果
        cout << (valid ? "YES" : "NO") << "\n";
    }

    return 0;
}

