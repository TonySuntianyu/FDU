#include <iostream>
#include <vector>
#include <string>
#include <queue>
using namespace std;

// 二叉树节点结构体
struct TreeNode {
    int val;
    TreeNode* left;
    TreeNode* right;
    
    TreeNode(int x) : val(x), left(nullptr), right(nullptr) {}
};

// 根据层序遍历序列构建二叉树
TreeNode* buildTree(vector<string>& levelOrder) {
    if (levelOrder.empty() || levelOrder[0] == "null") {
        return nullptr;
    }
    
    TreeNode* root = new TreeNode(stoi(levelOrder[0]));
    queue<TreeNode*> q;
    q.push(root);
    
    int i = 1;
    while (!q.empty() && i < levelOrder.size()) {
        TreeNode* node = q.front();
        q.pop();
        
        // 处理左子节点
        if (i < levelOrder.size()) {
            if (levelOrder[i] != "null") {
                node->left = new TreeNode(stoi(levelOrder[i]));
                q.push(node->left);
            }
            i++;
        }
        
        // 处理右子节点
        if (i < levelOrder.size()) {
            if (levelOrder[i] != "null") {
                node->right = new TreeNode(stoi(levelOrder[i]));
                q.push(node->right);
            }
            i++;
        }
    }
    
    return root;
}

// 前序遍历：根 -> 左 -> 右
void preorderTraversal(TreeNode* root, vector<int>& result) {
    if (root == nullptr) return;
    
    result.push_back(root->val);
    preorderTraversal(root->left, result);
    preorderTraversal(root->right, result);
}

// 中序遍历：左 -> 根 -> 右
void inorderTraversal(TreeNode* root, vector<int>& result) {
    if (root == nullptr) return;
    
    inorderTraversal(root->left, result);
    result.push_back(root->val);
    inorderTraversal(root->right, result);
}

// 后序遍历：左 -> 右 -> 根
void postorderTraversal(TreeNode* root, vector<int>& result) {
    if (root == nullptr) return;
    
    postorderTraversal(root->left, result);
    postorderTraversal(root->right, result);
    result.push_back(root->val);
}

// 打印遍历结果
void printResult(const vector<int>& result) {
    for (int i = 0; i < result.size(); i++) {
        if (i > 0) cout << " ";
        cout << result[i];
    }
    cout << endl;
}

int main() {
    string input;
    getline(cin, input);
    
    // 解析输入字符串
    vector<string> levelOrder;
    string token;
    for (char c : input) {
        if (c == ' ') {
            if (!token.empty()) {
                levelOrder.push_back(token);
                token.clear();
            }
        } else {
            token += c;
        }
    }
    if (!token.empty()) {
        levelOrder.push_back(token);
    }
    
    // 构建二叉树
    TreeNode* root = buildTree(levelOrder);
    
    // 前序遍历
    vector<int> preorder;
    preorderTraversal(root, preorder);
    printResult(preorder);
    
    // 中序遍历
    vector<int> inorder;
    inorderTraversal(root, inorder);
    printResult(inorder);
    
    // 后序遍历
    vector<int> postorder;
    postorderTraversal(root, postorder);
    printResult(postorder);
    
    return 0;
}
