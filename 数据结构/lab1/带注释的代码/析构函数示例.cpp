/**
 * 析构函数示例程序
 * 演示析构函数的作用和重要性
 */

#include <iostream>
using namespace std;

class SimpleClass {
private:
    int* data;  // 动态分配的整数指针
    
public:
    // 构造函数：分配内存
    SimpleClass(int value) {
        cout << "构造函数被调用，分配内存" << endl;
        data = new int(value);  // 动态分配内存
    }
    
    // 析构函数：释放内存
    ~SimpleClass() {
        cout << "析构函数被调用，释放内存" << endl;
        delete data;  // 释放动态分配的内存
        data = nullptr;  // 将指针设为空，避免悬空指针
    }
    
    // 获取数据
    int getValue() {
        return *data;
    }
};

void demonstrateDestructor() {
    cout << "=== 进入函数作用域 ===" << endl;
    
    // 创建对象，构造函数被调用
    SimpleClass obj(42);
    cout << "对象的值: " << obj.getValue() << endl;
    
    cout << "=== 即将离开函数作用域 ===" << endl;
    // 当函数结束时，obj对象超出作用域
    // 析构函数会自动被调用
}

int main() {
    cout << "程序开始" << endl;
    
    // 调用函数演示析构函数
    demonstrateDestructor();
    
    cout << "函数调用结束，析构函数已自动执行" << endl;
    cout << "程序结束" << endl;
    
    return 0;
}

/**
 * 运行结果：
 * 程序开始
 * === 进入函数作用域 ===
 * 构造函数被调用，分配内存
 * 对象的值: 42
 * === 即将离开函数作用域 ===
 * 析构函数被调用，释放内存
 * 函数调用结束，析构函数已自动执行
 * 程序结束
 * 
 * 关键点：
 * 1. 构造函数在创建对象时调用
 * 2. 析构函数在对象销毁时自动调用
 * 3. 即使我们没有显式调用析构函数，它也会自动执行
 */
