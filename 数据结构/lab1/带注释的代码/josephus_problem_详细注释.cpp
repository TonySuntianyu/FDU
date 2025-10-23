/**
 * 约瑟夫问题求解程序
 * 
 * 问题描述：
 * 有n个人围成一圈，从第1个人开始报数，数到第k个人就将其淘汰出局，
 * 然后从下一个人重新开始报数，直到最后只剩下一个人。
 * 求最后剩下的人的编号。
 * 
 * 算法思路：
 * 使用循环单链表模拟约瑟夫问题，通过删除节点来模拟淘汰过程
 * 
 * 时间复杂度：O(n*k)
 * 空间复杂度：O(n)
 */

#include <iostream>
using namespace std;

/**
 * 循环链表节点类
 * 用于存储约瑟夫问题中每个人的编号信息
 */
class CircularListNode {
public:
    int data;                    // 数据域：存储人员编号（1到n）
    CircularListNode* next;      // 指针域：指向下一个节点，形成循环链表
    
    /**
     * 构造函数
     * @param val 人员编号
     */
    CircularListNode(int val) : data(val), next(nullptr) {}
    
    /**
     * 析构函数
     * 节点内存由链表类统一管理，这里不需要特殊处理
     */
    ~CircularListNode() {}
};

/**
 * 循环单链表类
 * 用于实现约瑟夫问题的核心数据结构
 */
class CircularLinkedList {
private:
    CircularListNode* head;      // 头节点指针：指向链表的第一个节点
    CircularListNode* current;   // 当前节点指针：用于遍历和操作链表
    int size;                   // 链表大小：记录当前剩余的人数
    
public:
    /**
     * 构造函数：创建包含n个节点的循环链表
     * @param n 总人数
     */
    CircularLinkedList(int n) : head(nullptr), current(nullptr), size(n) {
        // 如果人数小于等于0，直接返回
        if (n <= 0) return;
        
        // 创建第一个节点（编号为1）
        head = new CircularListNode(1);
        current = head;
        
        // 创建其余n-1个节点（编号为2到n）
        for (int i = 2; i <= n; i++) {
            CircularListNode* newNode = new CircularListNode(i);
            current->next = newNode;  // 将新节点连接到当前节点后面
            current = newNode;        // 移动当前指针到新节点
        }
        
        // 将最后一个节点连接到头节点，形成循环链表
        current->next = head;
        current = head;  // 重置当前指针到头节点，准备开始游戏
    }
    
    /**
     * 析构函数：释放所有节点内存
     * 防止内存泄漏
     */
    ~CircularLinkedList() {
        if (head == nullptr) return;
        
        // 从第二个节点开始删除（因为要保留头节点到最后）
        CircularListNode* temp = head->next;
        while (temp != head) {
            CircularListNode* next = temp->next;
            delete temp;
            temp = next;
        }
        // 最后删除头节点
        delete head;
    }
    
    /**
     * 检查是否只剩一个节点
     * @return true如果只剩一个人，false否则
     */
    bool isLastOne() {
        return size == 1;
    }
    
    /**
     * 获取当前节点数据（人员编号）
     * @return 当前人员的编号
     */
    int getCurrentData() {
        return current->data;
    }
    
    /**
     * 移动到下一个节点
     * 用于模拟报数过程
     */
    void moveNext() {
        current = current->next;
    }
    
    /**
     * 删除当前节点，并移动到下一个节点
     * 这是约瑟夫问题的核心操作：淘汰当前人员
     */
    void deleteCurrent() {
        // 如果只剩一个人或没有人，不需要删除
        if (size <= 1) return;
        
        // 找到当前节点的前一个节点
        // 由于是循环链表，需要遍历找到前驱节点
        CircularListNode* prev = current;
        while (prev->next != current) {
            prev = prev->next;
        }
        
        // 如果要删除的是头节点，需要更新头节点指针
        if (current == head) {
            head = current->next;
        }
        
        // 从链表中移除当前节点
        // 将前一个节点的next指针指向当前节点的下一个节点
        prev->next = current->next;
        
        // 释放当前节点内存
        CircularListNode* toDelete = current;
        current = current->next;  // 移动到下一个节点，继续游戏
        
        delete toDelete;
        size--;  // 人数减1
    }
    
    /**
     * 约瑟夫问题求解：返回最后剩下的人的编号
     * @param k 报数的间隔（每数到第k个人就淘汰）
     * @return 最后剩下的人的编号
     */
    int solveJosephus(int k) {
        // 从第1个人开始报数
        current = head;
        
        // 循环直到只剩一个人
        while (!isLastOne()) {
            // 数k-1个人（因为要删除第k个人）
            // 例如：如果k=3，当前是第1个人，需要数到第3个人
            // 所以需要移动2次：1->2->3，然后删除第3个人
            for (int i = 1; i < k; i++) {
                moveNext();
            }
            
            // 删除当前节点（第k个人被淘汰）
            deleteCurrent();
        }
        
        // 返回最后剩下的人的编号
        return getCurrentData();
    }
};

/**
 * 主函数：程序入口
 * 读取输入参数并求解约瑟夫问题
 */
int main() {
    int n, k;  // n：总人数，k：报数间隔
    
    // 读取输入：n个人，每数到第k个人就淘汰
    cin >> n >> k;
    
    // 创建循环链表，模拟n个人围成一圈
    CircularLinkedList circle(n);
    
    // 求解约瑟夫问题，得到最后剩下的人的编号
    int winner = circle.solveJosephus(k);
    
    // 输出获胜者编号
    cout << winner << endl;
    
    return 0;
}

/**
 * 算法分析：
 * 
 * 时间复杂度：O(n*k)
 * - 外层循环最多执行n-1次（淘汰n-1个人）
 * - 内层循环每次执行k-1次（数k-1个人）
 * - 删除操作的时间复杂度为O(n)（需要找到前驱节点）
 * - 总时间复杂度：O(n*k)
 * 
 * 空间复杂度：O(n)
 * - 需要存储n个节点，每个节点占用常数空间
 * - 总空间复杂度：O(n)
 * 
 * 优化思路：
 * 1. 可以使用数学公式直接计算结果，时间复杂度为O(n)
 * 2. 可以使用双向循环链表，删除操作的时间复杂度可以优化到O(1)
 * 3. 对于大数据量，可以考虑使用数组模拟，减少内存分配开销
 */
