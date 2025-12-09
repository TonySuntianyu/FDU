#ifndef TRAFFIC_CALCULATOR_H
#define TRAFFIC_CALCULATOR_H

// 堵车系数计算器
class TrafficCalculator {
public:
    // 计算堵车系数
    // 公式：堵车系数 = (现有车辆数 / 车道数) / 道路长度 * (1 / 道路限速)
    // 这个公式考虑了车辆密度、道路容量和限速的影响
    // 车辆数越多、车道数越少、道路越短、限速越低，堵车系数越大
    static double calculateTrafficCoefficient(int vehicles, int lanes, 
                                               int length, int speedLimit);
    
    // 将堵车系数转换为边的权重
    // 权重 = 基础时间 + 堵车影响
    // 基础时间 = 道路长度 / 限速（转换为小时）
    static double calculateWeight(int vehicles, int lanes, int length, 
                                  int speedLimit);
    
    // 简化版本：直接使用堵车系数作为权重的一部分
    static double calculateSimpleWeight(int vehicles, int lanes, int length, 
                                        int speedLimit);
};

#endif // TRAFFIC_CALCULATOR_H

