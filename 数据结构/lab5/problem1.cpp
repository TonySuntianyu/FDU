/*
 * 题目：奶牛的家谱Ⅰ
 * 问题描述：给定中序遍历和前序遍历，输出后序遍历
 * 
 * 算法思路：
 * 1. 使用递归方法根据前序遍历和中序遍历重建二叉树
 * 2. 前序遍历的第一个元素是根节点
 * 3. 在中序遍历中找到根节点的位置，左边是左子树，右边是右子树
 * 4. 递归构建左右子树
 * 5. 后序遍历：先遍历左子树，再遍历右子树，最后访问根节点
 */

#include <iostream>
#include <string>
using namespace std;

// 根据前序遍历和中序遍历，构建后序遍历结果
void buildPostOrder(string preorder, string inorder, string& postorder) {
    // 如果为空，直接返回
    if (preorder.empty() || inorder.empty()) {
        return;
    }
    
    // 前序遍历的第一个元素是根节点
    char root = preorder[0];
    
    // 在中序遍历中找到根节点的位置
    int rootIndex = inorder.find(root);
    
    // 左子树的中序遍历
    string leftInorder = inorder.substr(0, rootIndex);
    // 右子树的中序遍历
    string rightInorder = inorder.substr(rootIndex + 1);
    
    // 左子树的前序遍历（从第二个字符开始，长度为左子树节点数）
    string leftPreorder = preorder.substr(1, leftInorder.length());
    // 右子树的前序遍历（左子树之后的剩余部分）
    string rightPreorder = preorder.substr(1 + leftInorder.length());
    
    // 递归构建左子树的后序遍历
    buildPostOrder(leftPreorder, leftInorder, postorder);
    // 递归构建右子树的后序遍历
    buildPostOrder(rightPreorder, rightInorder, postorder);
    // 最后添加根节点（后序遍历：左-右-根）
    postorder += root;
}

int main() {
    string inorder, preorder;
    
    // 读取中序遍历和前序遍历
    cin >> inorder >> preorder;
    
    string postorder = "";
    // 构建后序遍历
    buildPostOrder(preorder, inorder, postorder);
    
    // 输出后序遍历结果
    cout << postorder << endl;
    
    return 0;
}

