"""
问题2：优化无人机FY1的策略参数

使用模拟退火算法找到最优的飞行方向、速度、投放时间和起爆延迟参数，
使得烟幕干扰弹对导弹M1的有效遮蔽时间最大化。
"""

import numpy as np
from scipy.optimize import basinhopping, minimize
import matplotlib.pyplot as plt
from matplotlib.ticker import MaxNLocator
from problem1 import calculate_smoke_obscuration

# 设置中文字体支持
plt.rcParams['font.sans-serif'] = ['SimHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

# 目标函数（返回负的有效时间，因为我们要最小化）
def objective(params):
    theta, s, t_drop, t_delay = params
    u = np.cos(theta/10)
    v = np.sin(theta/10)
    dir_vec = [u, v, 0]
    
    effective_time = calculate_smoke_obscuration(
        drone_direction=dir_vec,
        drone_speed=s,
        drop_time=t_drop/10,
        blast_delay=t_delay/10,
        visualize=False,
        verbose=False
    )
    
    return -effective_time  # 返回负值以便最小化

# 自定义回调函数用于记录优化过程
class OptimizationTracker:
    def __init__(self):
        self.history = []  # 记录所有函数值
        self.params_history = []  # 记录所有参数
        self.best_history = []  # 记录最佳函数值
        self.best_params_history = []  # 记录最佳参数
        self.best_value = float('inf')
        self.best_params = None
    
    def __call__(self, x, f, accepted):
        self.history.append(f)
        self.params_history.append(x.copy())
        
        if f < self.best_value:
            self.best_value = f
            self.best_params = x.copy()
        
        self.best_history.append(self.best_value)
        self.best_params_history.append(self.best_params.copy())
        
        if len(self.history) % 10 == 0:
            print(f"Iteration {len(self.history)}: Current value = {-f:.4f}, Best value = {-self.best_value:.4f}")

def optimize_smoke_strategy():
    """优化烟幕干扰弹策略"""
    
    # 变量边界
    bounds = [(np.pi*9, np.pi*11), (70, 140), (0, 50), (0.1, 50)]
    
    # 初始参数
    initial_params = np.array([np.pi*10, 120, 5, 35])
    
    # 创建跟踪器
    tracker = OptimizationTracker()
    
    # 模拟退火优化
    print("开始模拟退火优化...")
    minimizer_kwargs = {
        "method": "L-BFGS-B", 
        "bounds": bounds,
        "options": {"maxiter": 100}
    }
    
    result_sa = basinhopping(
        objective, 
        initial_params, 
        niter=200, 
        minimizer_kwargs=minimizer_kwargs, 
        stepsize=0.5,
        accept_test=None,
        callback=tracker
    )
    
    # 提取最佳参数
    best_params_sa = result_sa.x
    best_value_sa = -result_sa.fun  # 转换为正的有效时间
    
    print("\n模拟退火优化结果:")
    print(f"最佳方向角度 (theta): {best_params_sa[0]:.4f} rad")
    print(f"最佳速度: {best_params_sa[1]:.2f} m/s")
    print(f"最佳投放时间: {best_params_sa[2]:.2f} s")
    print(f"最佳起爆延迟: {best_params_sa[3]:.2f} s")
    print(f"最大有效遮蔽时间: {best_value_sa:.4f} s")
    
    # 绘制收敛曲线
    plot_optimization_results(tracker, initial_params, best_params_sa, best_value_sa, bounds)
    
    return best_params_sa, best_value_sa, tracker

def plot_optimization_results(tracker, initial_params, best_params, best_value, bounds):
    """绘制优化结果图表"""
    
    plt.figure(figsize=(15, 5))
    
    # 1. 损失函数曲线
    plt.subplot(1, 3, 1)
    iterations = range(1, len(tracker.history) + 1)
    plt.plot(iterations, [-x for x in tracker.history], 'b-', alpha=0.7, label='当前值')
    plt.plot(iterations, [-x for x in tracker.best_history], 'r-', linewidth=2, label='最佳值')
    plt.xlabel('迭代次数')
    plt.ylabel('有效遮蔽时间 (s)')
    plt.title('优化过程 - 损失函数曲线')
    plt.legend()
    plt.grid(True, alpha=0.3)
    plt.gca().xaxis.set_major_locator(MaxNLocator(integer=True))
    
    # 2. 参数变化曲线（归一化显示）
    plt.subplot(1, 3, 2)
    
    # 使用tracker中的参数历史记录
    theta_norm = [(p[0] - bounds[0][0]) / (bounds[0][1] - bounds[0][0]) for p in tracker.params_history]
    speed_norm = [(p[1] - bounds[1][0]) / (bounds[1][1] - bounds[1][0]) for p in tracker.params_history]
    drop_norm = [(p[2] - bounds[2][0]) / (bounds[2][1] - bounds[2][0]) for p in tracker.params_history]
    delay_norm = [(p[3] - bounds[3][0]) / (bounds[3][1] - bounds[3][0]) for p in tracker.params_history]
    
    plt.plot(iterations, theta_norm, label='方向角度', alpha=0.7)
    plt.plot(iterations, speed_norm, label='速度', alpha=0.7)
    plt.plot(iterations, drop_norm, label='投放时间', alpha=0.7)
    plt.plot(iterations, delay_norm, label='起爆延迟', alpha=0.7)
    
    plt.xlabel('迭代次数')
    plt.ylabel('归一化参数值')
    plt.title('参数优化过程')
    plt.legend()
    plt.grid(True, alpha=0.3)
    plt.gca().xaxis.set_major_locator(MaxNLocator(integer=True))
    
    # 3. 最终结果对比
    plt.subplot(1, 3, 3)
    initial_value = -objective(initial_params)
    plt.bar(['初始参数', '优化后参数'], [initial_value, best_value], alpha=0.7)
    plt.ylabel('有效遮蔽时间 (s)')
    plt.title('优化前后对比')
    plt.grid(True, alpha=0.3)
    
    # 在柱状图上添加数值标签
    for i, v in enumerate([initial_value, best_value]):
        plt.text(i, v + 0.1, f'{v:.2f}s', ha='center', va='bottom')
    
    plt.tight_layout()
    plt.show()

def main():
    print("=== 问题2：优化无人机FY1的策略参数 ===\n")
    
    # 运行优化
    best_params, best_value, tracker = optimize_smoke_strategy()
    
    # 输出详细的最佳参数信息
    print("\n详细最佳参数信息:")
    print(f"方向向量: [{np.cos(best_params[0]/10):.4f}, {np.sin(best_params[0]/10):.4f}, 0]")
    print(f"速度: {best_params[1]:.2f} m/s (范围: 70-140 m/s)")
    print(f"投放时间: {best_params[2]/10:.2f} s (范围: 0-5 s)")
    print(f"起爆延迟: {best_params[3]/10:.2f} s (范围: 0.1-5 s)")
    
    # 计算初始参数的效果
    initial_params = np.array([np.pi*10, 120, 5, 35])
    initial_value = -objective(initial_params)
    print(f"优化提升: {(best_value - initial_value):.2f} s ({((best_value - initial_value)/initial_value*100):.1f}%)")
    
    # 输出最佳参数对应的具体值
    print("\n最佳策略:")
    print(f"无人机飞行方向: 角度 {best_params[0]:.4f} rad (约 {np.degrees(best_params[0]):.2f}°)")
    print(f"无人机飞行速度: {best_params[1]:.2f} m/s")
    print(f"烟幕干扰弹投放点: 在任务开始后 {best_params[2]/10:.2f} s 投放")
    print(f"烟幕干扰弹起爆点: 投放后 {best_params[3]/10:.2f} s 起爆")
    print(f"预计有效遮蔽时间: {best_value:.2f} s")
    
    # 验证最佳策略
    print("\n验证最佳策略...")
    u = np.cos(best_params[0]/10)
    v = np.sin(best_params[0]/10)
    verification_time = calculate_smoke_obscuration(
        drone_direction=[u, v, 0],
        drone_speed=best_params[1],
        drop_time=best_params[2]/10,
        blast_delay=best_params[3]/10,
        visualize=True,
        verbose=True
    )
    
    print(f"\n验证结果：有效遮蔽时间 = {verification_time:.3f} s")


if __name__ == "__main__":
    main()
