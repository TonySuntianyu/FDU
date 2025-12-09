#include "TrafficCalculator.h"
#include <cmath>
#include <limits>

double TrafficCalculator::calculateTrafficCoefficient(int vehicles, int lanes, 
                                                       int length, int speedLimit) {
    if (lanes == 0 || length == 0 || speedLimit == 0) {
        return 1.0; // 默认值，避免除零
    }
    
    // 车辆密度 = 车辆数 / 车道数
    double vehicleDensity = static_cast<double>(vehicles) / lanes;
    
    // 道路容量因子 = 1 / 道路长度（米转千米）
    double capacityFactor = 1.0 / (length / 1000.0);
    
    // 限速因子 = 1 / 限速（限速越低，影响越大）
    double speedFactor = 1.0 / speedLimit;
    
    // 堵车系数 = 车辆密度 * 容量因子 * 限速因子
    // 这个值越大，表示道路越拥堵
    double coefficient = vehicleDensity * capacityFactor * speedFactor;
    
    return coefficient;
}

double TrafficCalculator::calculateWeight(int vehicles, int lanes, 
                                          int length, int speedLimit) {
    if (speedLimit == 0 || length == 0) {
        return std::numeric_limits<double>::max();
    }
    
    // 基础通行时间（小时）= 道路长度（千米）/ 限速（km/h）
    double baseTime = (length / 1000.0) / speedLimit;
    
    // 计算堵车系数
    double trafficCoeff = calculateTrafficCoefficient(vehicles, lanes, length, speedLimit);
    
    // 权重 = 基础时间 * (1 + 堵车系数)
    // 堵车系数越大，权重越大，表示通过该道路的成本越高
    double weight = baseTime * (1.0 + trafficCoeff);
    
    return weight;
}

double TrafficCalculator::calculateSimpleWeight(int vehicles, int lanes, 
                                                int length, int speedLimit) {
    // 简化版本：直接使用题目给出的公式思路
    // 堵车系数 = 现有车辆数 / 车道数 / 道路长度 * 道路限速
    // 但这里我们反过来：权重应该与堵车系数成正比
    
    if (lanes == 0 || length == 0 || speedLimit == 0) {
        return std::numeric_limits<double>::max();
    }
    
    // 基础时间
    double baseTime = (length / 1000.0) / speedLimit;
    
    // 车辆密度影响
    double densityImpact = static_cast<double>(vehicles) / lanes;
    
    // 归一化：除以道路长度（千米）
    double normalizedDensity = densityImpact / (length / 1000.0);
    
    // 权重 = 基础时间 * (1 + 归一化密度)
    double weight = baseTime * (1.0 + normalizedDensity * 0.1); // 0.1是调节因子
    
    return weight;
}

