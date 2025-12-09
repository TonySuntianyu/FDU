/*
 * 题目：奶牛的家谱Ⅱ
 * 问题描述：给定前序遍历和中序遍历，以及两个节点名称，找出它们的最近公共祖先（LCA）
 * 
 * 算法思路：
 * 1. 根据前序遍历和中序遍历重建二叉树
 * 2. 构建父节点映射，方便向上查找
 * 3. 从两个节点向上遍历到根节点，找到第一个公共祖先
 */

#include <iostream>
#include <string>
#include <map>
#include <set>
using namespace std;

// 二叉树节点结构
struct TreeNode {
    char val;
    TreeNode* left;
    TreeNode* right;
    TreeNode* parent;
    
    TreeNode(char x) : val(x), left(nullptr), right(nullptr), parent(nullptr) {}
};

// 根据前序遍历和中序遍历构建二叉树
TreeNode* buildTree(string preorder, string inorder, TreeNode* parent) {
    // 如果为空，返回nullptr
    if (preorder.empty() || inorder.empty()) {
        return nullptr;
    }
    
    // 前序遍历的第一个元素是根节点
    char rootVal = preorder[0];
    TreeNode* root = new TreeNode(rootVal);
    root->parent = parent;
    
    // 在中序遍历中找到根节点的位置
    int rootIndex = inorder.find(rootVal);
    
    // 左子树的中序遍历
    string leftInorder = inorder.substr(0, rootIndex);
    // 右子树的中序遍历
    string rightInorder = inorder.substr(rootIndex + 1);
    
    // 左子树的前序遍历
    string leftPreorder = preorder.substr(1, leftInorder.length());
    // 右子树的前序遍历
    string rightPreorder = preorder.substr(1 + leftInorder.length());
    
    // 递归构建左右子树
    root->left = buildTree(leftPreorder, leftInorder, root);
    root->right = buildTree(rightPreorder, rightInorder, root);
    
    return root;
}

// 在树中查找值为val的节点
TreeNode* findNode(TreeNode* root, char val) {
    if (root == nullptr) {
        return nullptr;
    }
    
    if (root->val == val) {
        return root;
    }
    
    // 在左子树中查找
    TreeNode* left = findNode(root->left, val);
    if (left != nullptr) {
        return left;
    }
    
    // 在右子树中查找
    TreeNode* right = findNode(root->right, val);
    return right;
}

// 查找两个节点的最近公共祖先
char findLCA(TreeNode* root, char node1, char node2) {
    // 查找两个节点
    TreeNode* n1 = findNode(root, node1);
    TreeNode* n2 = findNode(root, node2);
    
    // 如果任一节点不存在，返回特殊标记
    if (n1 == nullptr || n2 == nullptr) {
        return '\0';
    }
    
    // 从node1向上遍历到根节点，记录所有祖先节点
    set<TreeNode*> ancestors;
    TreeNode* curr = n1;
    while (curr != nullptr) {
        ancestors.insert(curr);
        curr = curr->parent;
    }
    
    // 从node2向上遍历，找到第一个在ancestors中的节点，即为LCA
    curr = n2;
    while (curr != nullptr) {
        if (ancestors.find(curr) != ancestors.end()) {
            return curr->val;
        }
        curr = curr->parent;
    }
    
    // 理论上不应该到达这里
    return '\0';
}

// 释放二叉树内存
void deleteTree(TreeNode* root) {
    if (root == nullptr) {
        return;
    }
    deleteTree(root->left);
    deleteTree(root->right);
    delete root;
}

int main() {
    string preorder, inorder;
    char node1, node2;
    
    // 读取前序遍历和中序遍历
    cin >> preorder >> inorder;
    // 读取要查询的两个节点
    cin >> node1 >> node2;
    
    // 构建二叉树
    TreeNode* root = buildTree(preorder, inorder, nullptr);
    
    // 查找最近公共祖先
    char lca = findLCA(root, node1, node2);
    
    // 输出结果
    if (lca == '\0') {
        cout << "NA" << endl;
    } else {
        cout << lca << endl;
    }
    
    // 释放内存
    deleteTree(root);
    
    return 0;
}

