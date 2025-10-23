#include <iostream>
#include <unordered_map>
using namespace std;

// 链表节点类
class ListNode {
public:
    unsigned long long data;        // 数据域，存储正整数
    ListNode* next;       // 指针域，指向下一个节点
    
    // 构造函数
    ListNode(unsigned long long val) : data(val), next(nullptr) {}
    
    // 析构函数
    ~ListNode() {}
};

// 单链表类
class LinkedList {
private:
    ListNode* head;  // 头节点指针
    unordered_map<unsigned long long, ListNode*> nodeMap;  // 用于快速查找节点的映射表
    
public:
    // 构造函数：初始化链表，只有一个值为1的节点
    LinkedList() {
        head = new ListNode(1);
        nodeMap[1] = head;
    }
    
    // 析构函数：释放所有节点内存
    ~LinkedList() {
        ListNode* current = head;
        while (current != nullptr) {
            ListNode* next = current->next;
            delete current;
            current = next;
        }
    }
    
    // 1. 返回头节点
    ListNode* getHead() {
        return head;
    }
    
    // 2. 返回下一个节点
    ListNode* getNext(ListNode* node) {
        if (node == nullptr) return nullptr;
        return node->next;
    }
    
    // 3. 插入节点：在指定值为x的节点后插入新节点y
    void insertNode(unsigned long long x, unsigned long long y) {
        if (nodeMap.find(x) == nodeMap.end()) {
            return;  // 节点x不存在，忽略操作
        }
        
        ListNode* targetNode = nodeMap[x];
        ListNode* newNode = new ListNode(y);
        
        // 在目标节点后插入新节点
        newNode->next = targetNode->next;
        targetNode->next = newNode;
        
        // 更新映射表
        nodeMap[y] = newNode;
    }
    
    // 4. 删除节点：删除值为x的节点
    void deleteNode(unsigned long long x) {
        if (nodeMap.find(x) == nodeMap.end()) {
            return;  // 节点x不存在，忽略操作
        }
        
        // 如果要删除的是头节点
        if (head->data == x) {
            return;  // 根据题目说明，初始节点永远不被删除
        }
        
        // 找到要删除节点的前一个节点
        ListNode* prev = nullptr;
        ListNode* current = head;
        
        while (current != nullptr && current->data != x) {
            prev = current;
            current = current->next;
        }
        
        if (current != nullptr) {
            // 从链表中移除节点
            if (prev != nullptr) {
                prev->next = current->next;
            }
            
            // 从映射表中移除
            nodeMap.erase(x);
            
            // 释放内存
            delete current;
        }
    }
    
    // 5. 修改节点：将指定值为x的节点的数据修改为y
    void modifyNode(unsigned long long x, unsigned long long y) {
        if (nodeMap.find(x) == nodeMap.end()) {
            return;  // 节点x不存在，忽略操作
        }
        
        ListNode* targetNode = nodeMap[x];
        targetNode->data = y;
        
        // 更新映射表
        nodeMap.erase(x);
        nodeMap[y] = targetNode;
    }
    
    // 6. 查询节点对应数值：给定某个值x，输出它的后继节点的值
    unsigned long long queryNext(unsigned long long x) {
        if (nodeMap.find(x) == nodeMap.end()) {
            return 0;  // 节点x不存在，返回0
        }
        
        ListNode* targetNode = nodeMap[x];
        if (targetNode->next == nullptr) {
            return 0;  // 没有后继节点，返回0
        }
        
        return targetNode->next->data;
    }
    
    // 7. 顺序打印全部节点
    void printAll() {
        ListNode* current = head;
        bool first = true;
        
        while (current != nullptr) {
            if (!first) {
                cout << " ";
            }
            cout << current->data;
            first = false;
            current = current->next;
        }
        cout << endl;
    }
};

int main() {
    LinkedList list;
    int q;
    cin >> q;
    
    for (int i = 0; i < q; i++) {
        int operation;
        cin >> operation;
        
        if (operation == 1) {
            // 插入操作：1 x y
            unsigned long long x, y;
            cin >> x >> y;
            list.insertNode(x, y);
        }
        else if (operation == 2) {
            // 查询操作：2 x
            unsigned long long x;
            cin >> x;
            cout << list.queryNext(x) << endl;
        }
        else if (operation == 3) {
            // 删除操作：3 x
            unsigned long long x;
            cin >> x;
            list.deleteNode(x);
        }
        else if (operation == 4) {
            // 修改操作：4 x y
            unsigned long long x, y;
            cin >> x >> y;
            list.modifyNode(x, y);
        }
        else if (operation == 5) {
            // 打印操作：5
            list.printAll();
        }
    }
    
    return 0;
}
