/**
 * 单链表操作程序
 * 
 * 功能描述：
 * 实现一个支持多种操作的单链表，包括插入、删除、修改、查询和打印等操作
 * 使用哈希表优化节点查找，提高操作效率
 * 
 * 支持的操作：
 * 1. 插入节点：在指定值为x的节点后插入新节点y
 * 2. 查询节点：查询指定值为x的节点的后继节点值
 * 3. 删除节点：删除指定值为x的节点（初始节点永远不被删除）
 * 4. 修改节点：将指定值为x的节点的数据修改为y
 * 5. 打印链表：按顺序输出所有节点的值
 * 
 * 时间复杂度：
 * - 插入、删除、修改、查询：O(1)（平均情况）
 * - 打印：O(n)
 * 
 * 空间复杂度：O(n)
 */

#include <iostream>
#include <unordered_map>  // 用于实现哈希表，优化节点查找
using namespace std;

/**
 * 链表节点类
 * 存储链表中的数据和指针信息
 */
class ListNode {
public:
    unsigned long long data;        // 数据域：存储正整数
    ListNode* next;                 // 指针域：指向下一个节点
    
    /**
     * 构造函数
     * @param val 节点存储的数值
     */
    ListNode(unsigned long long val) : data(val), next(nullptr) {}
    
    /**
     * 析构函数
     * 节点内存由链表类统一管理
     */
    ~ListNode() {}
};

/**
 * 单链表类
 * 实现各种链表操作的核心数据结构
 */
class LinkedList {
private:
    ListNode* head;  // 头节点指针：指向链表的第一个节点
    unordered_map<unsigned long long, ListNode*> nodeMap;  // 哈希表：用于快速查找节点
    
public:
    /**
     * 构造函数：初始化链表
     * 创建一个只包含值为1的节点的链表
     */
    LinkedList() {
        head = new ListNode(1);           // 创建初始节点
        nodeMap[1] = head;                // 将节点添加到哈希表中
    }
    
    /**
     * 析构函数：释放所有节点内存
     * 防止内存泄漏
     */
    ~LinkedList() {
        ListNode* current = head;
        while (current != nullptr) {
            ListNode* next = current->next;  // 保存下一个节点的指针
            delete current;                  // 删除当前节点
            current = next;                  // 移动到下一个节点
        }
    }
    
    /**
     * 操作1：返回头节点
     * @return 头节点指针
     */
    ListNode* getHead() {
        return head;
    }
    
    /**
     * 操作2：返回指定节点的下一个节点
     * @param node 当前节点指针
     * @return 下一个节点指针，如果当前节点为空则返回nullptr
     */
    ListNode* getNext(ListNode* node) {
        if (node == nullptr) return nullptr;
        return node->next;
    }
    
    /**
     * 操作3：插入节点
     * 在指定值为x的节点后插入新节点y
     * @param x 目标节点的值
     * @param y 新节点的值
     */
    void insertNode(unsigned long long x, unsigned long long y) {
        // 检查目标节点是否存在
        if (nodeMap.find(x) == nodeMap.end()) {
            return;  // 节点x不存在，忽略操作
        }
        
        // 获取目标节点指针
        ListNode* targetNode = nodeMap[x];
        
        // 创建新节点
        ListNode* newNode = new ListNode(y);
        
        // 在目标节点后插入新节点
        // 新节点的next指向目标节点的next
        newNode->next = targetNode->next;
        // 目标节点的next指向新节点
        targetNode->next = newNode;
        
        // 更新哈希表：将新节点添加到映射中
        nodeMap[y] = newNode;
    }
    
    /**
     * 操作4：删除节点
     * 删除指定值为x的节点
     * @param x 要删除的节点的值
     */
    void deleteNode(unsigned long long x) {
        // 检查目标节点是否存在
        if (nodeMap.find(x) == nodeMap.end()) {
            return;  // 节点x不存在，忽略操作
        }
        
        // 如果要删除的是头节点，根据题目要求不删除
        if (head->data == x) {
            return;  // 初始节点永远不被删除
        }
        
        // 找到要删除节点的前一个节点
        ListNode* prev = nullptr;      // 前一个节点指针
        ListNode* current = head;      // 当前节点指针
        
        // 遍历链表找到目标节点及其前驱
        while (current != nullptr && current->data != x) {
            prev = current;
            current = current->next;
        }
        
        // 如果找到目标节点，执行删除操作
        if (current != nullptr) {
            // 从链表中移除节点
            if (prev != nullptr) {
                prev->next = current->next;  // 前一个节点指向后一个节点
            }
            
            // 从哈希表中移除该节点的映射
            nodeMap.erase(x);
            
            // 释放节点内存
            delete current;
        }
    }
    
    /**
     * 操作5：修改节点
     * 将指定值为x的节点的数据修改为y
     * @param x 要修改的节点的原值
     * @param y 修改后的新值
     */
    void modifyNode(unsigned long long x, unsigned long long y) {
        // 检查目标节点是否存在
        if (nodeMap.find(x) == nodeMap.end()) {
            return;  // 节点x不存在，忽略操作
        }
        
        // 获取目标节点指针
        ListNode* targetNode = nodeMap[x];
        
        // 修改节点数据
        targetNode->data = y;
        
        // 更新哈希表：
        // 1. 删除原值的映射
        nodeMap.erase(x);
        // 2. 添加新值的映射
        nodeMap[y] = targetNode;
    }
    
    /**
     * 操作6：查询节点对应数值
     * 给定某个值x，输出它的后继节点的值
     * @param x 查询节点的值
     * @return 后继节点的值，如果不存在则返回0
     */
    unsigned long long queryNext(unsigned long long x) {
        // 检查目标节点是否存在
        if (nodeMap.find(x) == nodeMap.end()) {
            return 0;  // 节点x不存在，返回0
        }
        
        // 获取目标节点指针
        ListNode* targetNode = nodeMap[x];
        
        // 检查是否有后继节点
        if (targetNode->next == nullptr) {
            return 0;  // 没有后继节点，返回0
        }
        
        // 返回后继节点的值
        return targetNode->next->data;
    }
    
    /**
     * 操作7：顺序打印全部节点
     * 按链表顺序输出所有节点的值
     */
    void printAll() {
        ListNode* current = head;  // 从头节点开始遍历
        bool first = true;         // 标记是否为第一个节点（用于控制空格输出）
        
        // 遍历整个链表
        while (current != nullptr) {
            // 如果不是第一个节点，输出空格分隔符
            if (!first) {
                cout << " ";
            }
            
            // 输出当前节点的值
            cout << current->data;
            first = false;  // 标记已输出过节点
            
            // 移动到下一个节点
            current = current->next;
        }
        
        // 输出换行符
        cout << endl;
    }
};

/**
 * 主函数：程序入口
 * 处理用户输入并执行相应的链表操作
 */
int main() {
    LinkedList list;  // 创建链表实例
    int q;           // 操作次数
    
    // 读取操作次数
    cin >> q;
    
    // 处理每个操作
    for (int i = 0; i < q; i++) {
        int operation;  // 操作类型
        cin >> operation;
        
        if (operation == 1) {
            // 插入操作：1 x y
            // 在值为x的节点后插入值为y的新节点
            unsigned long long x, y;
            cin >> x >> y;
            list.insertNode(x, y);
        }
        else if (operation == 2) {
            // 查询操作：2 x
            // 查询值为x的节点的后继节点值
            unsigned long long x;
            cin >> x;
            cout << list.queryNext(x) << endl;
        }
        else if (operation == 3) {
            // 删除操作：3 x
            // 删除值为x的节点
            unsigned long long x;
            cin >> x;
            list.deleteNode(x);
        }
        else if (operation == 4) {
            // 修改操作：4 x y
            // 将值为x的节点修改为y
            unsigned long long x, y;
            cin >> x >> y;
            list.modifyNode(x, y);
        }
        else if (operation == 5) {
            // 打印操作：5
            // 按顺序输出所有节点的值
            list.printAll();
        }
    }
    
    return 0;
}

/**
 * 算法分析：
 * 
 * 时间复杂度分析：
 * - 插入操作：O(1) - 哈希表查找O(1)，插入操作O(1)
 * - 删除操作：O(n) - 需要遍历找到前驱节点，但哈希表查找O(1)
 * - 修改操作：O(1) - 哈希表查找O(1)，修改操作O(1)
 * - 查询操作：O(1) - 哈希表查找O(1)
 * - 打印操作：O(n) - 需要遍历整个链表
 * 
 * 空间复杂度：O(n)
 * - 链表存储：O(n)
 * - 哈希表存储：O(n)
 * - 总空间复杂度：O(n)
 * 
 * 优化建议：
 * 1. 可以使用双向链表优化删除操作，时间复杂度可降至O(1)
 * 2. 对于删除操作，可以在节点中存储前驱指针，避免遍历查找
 * 3. 可以考虑使用智能指针管理内存，提高代码安全性
 * 4. 对于大数据量，可以考虑使用内存池减少内存分配开销
 * 
 * 数据结构选择说明：
 * - 使用unordered_map而不是map的原因：查找操作更频繁，unordered_map的O(1)查找比map的O(log n)更优
 * - 使用单链表而不是双向链表的原因：题目要求相对简单，单链表足以满足需求且节省空间
 */
