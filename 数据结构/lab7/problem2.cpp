#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

// 边的结构体
struct Edge {
    int u, v;      // 边的两个端点
    long long w;   // 边的权重

    // 重载小于运算符，用于排序
    bool operator<(const Edge& other) const {
        return w < other.w;
    }
};

// 并查集数据结构
class UnionFind {
private:
    vector<int> parent;  // 父节点数组
    vector<int> rank;    // 秩数组（用于路径压缩优化）

public:
    // 初始化并查集，大小为n
    UnionFind(int n) {
        parent.resize(n + 1);
        rank.resize(n + 1, 0);
        for (int i = 1; i <= n; i++) {
            parent[i] = i;  // 初始时每个节点的父节点是自己
        }
    }

    // 查找根节点，带路径压缩
    int find(int x) {
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
};

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int n, m;
    cin >> n >> m;

    vector<Edge> edges(m);

    // 读入所有边
    for (int i = 0; i < m; i++) {
        cin >> edges[i].u >> edges[i].v >> edges[i].w;
    }

    // 按边权从小到大排序（Kruskal算法的核心步骤）
    sort(edges.begin(), edges.end());

    // 初始化并查集
    UnionFind uf(n);

    // Kruskal算法：贪心地选择边权最小的边
    long long mstWeight = 0;  // 最小生成树的边权和
    int edgeCount = 0;        // 已选择的边数

    for (const Edge& e : edges) {
        // 如果边的两个端点不在同一连通分量中，则加入MST
        if (!uf.same(e.u, e.v)) {
            uf.unite(e.u, e.v);
            mstWeight += e.w;
            edgeCount++;
            
            // 最小生成树有n-1条边，如果已经选够，可以提前退出
            if (edgeCount == n - 1) {
                break;
            }
        }
    }

    // 输出最小生成树的边权和
    cout << mstWeight << "\n";

    return 0;
}

