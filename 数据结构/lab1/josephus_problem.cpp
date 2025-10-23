#include <iostream>
using namespace std;

// 循环链表节点类
class CircularListNode {
public:
    int data;                    // 数据域，存储人员编号
    CircularListNode* next;      // 指针域，指向下一个节点
    
    // 构造函数
    CircularListNode(int val) : data(val), next(nullptr) {}
    
    // 析构函数
    ~CircularListNode() {}
};

// 循环单链表类
class CircularLinkedList {
private:
    CircularListNode* head;      // 头节点指针
    CircularListNode* current;   // 当前节点指针
    int size;                   // 链表大小
    
public:
    // 构造函数：创建包含n个节点的循环链表
    CircularLinkedList(int n) : head(nullptr), current(nullptr), size(n) {
        if (n <= 0) return;
        
        // 创建第一个节点
        head = new CircularListNode(1);
        current = head;
        
        // 创建其余节点
        for (int i = 2; i <= n; i++) {
            CircularListNode* newNode = new CircularListNode(i);
            current->next = newNode;
            current = newNode;
        }
        
        // 将最后一个节点连接到头节点，形成循环
        current->next = head;
        current = head;  // 重置当前指针到头节点
    }
    
    // 析构函数：释放所有节点内存
    ~CircularLinkedList() {
        if (head == nullptr) return;
        
        CircularListNode* temp = head->next;
        while (temp != head) {
            CircularListNode* next = temp->next;
            delete temp;
            temp = next;
        }
        delete head;
    }
    
    // 检查是否只剩一个节点
    bool isLastOne() {
        return size == 1;
    }
    
    // 获取当前节点数据
    int getCurrentData() {
        return current->data;
    }
    
    // 移动到下一个节点
    void moveNext() {
        current = current->next;
    }
    
    // 删除当前节点，并移动到下一个节点
    void deleteCurrent() {
        if (size <= 1) return;
        
        // 找到当前节点的前一个节点
        CircularListNode* prev = current;
        while (prev->next != current) {
            prev = prev->next;
        }
        
        // 如果要删除的是头节点，更新头节点
        if (current == head) {
            head = current->next;
        }
        
        // 从链表中移除当前节点
        prev->next = current->next;
        
        // 释放当前节点内存
        CircularListNode* toDelete = current;
        current = current->next;  // 移动到下一个节点
        
        delete toDelete;
        size--;
    }
    
    // 约瑟夫问题求解：返回最后剩下的人的编号
    int solveJosephus(int k) {
        // 从第1个人开始
        current = head;
        
        while (!isLastOne()) {
            // 数k-1个人（因为要删除第k个人）
            for (int i = 1; i < k; i++) {
                moveNext();
            }
            
            // 删除当前节点（第k个人）
            deleteCurrent();
        }
        
        // 返回最后剩下的人的编号
        return getCurrentData();
    }
};

int main() {
    int n, k;
    cin >> n >> k;
    
    // 创建循环链表
    CircularLinkedList circle(n);
    
    // 求解约瑟夫问题
    int winner = circle.solveJosephus(k);
    
    // 输出获胜者编号
    cout << winner << endl;
    
    return 0;
}
