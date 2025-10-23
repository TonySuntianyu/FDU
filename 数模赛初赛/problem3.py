"""
问题3：多弹协同优化策略

无人机携带3个烟幕干扰弹，采用混合优化策略（遗传算法+模拟退火）
找到最优的投放策略，使总的有效遮蔽时间最大化。
"""

import numpy as np
from scipy.optimize import basinhopping, differential_evolution
import matplotlib.pyplot as plt
from matplotlib.ticker import MaxNLocator
from problem1 import calculate_smoke_obscuration

# 设置中文字体支持
plt.rcParams['font.sans-serif'] = ['SimHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

def calculate_multiple_smoke_obscuration(drone_direction, drone_speed, drop_times, blast_delays, 
                                        verbose=False):
    """
    计算多个烟幕干扰弹对导弹M1的总有效遮蔽时间
    """
    total_time = 0
    individual_times = []
    
    for i, (drop_time, blast_delay) in enumerate(zip(drop_times, blast_delays)):
        eff_time = calculate_smoke_obscuration(
            drone_direction=drone_direction,
            drone_speed=drone_speed,
            drop_time=drop_time,
            blast_delay=blast_delay,
            visualize=False,
            verbose=verbose and i == 0
        )
        individual_times.append(eff_time)
        total_time += eff_time
    
    if verbose:
        print(f"\n=== 多弹效果分析 ===")
        for i, (drop, delay, time) in enumerate(zip(drop_times, blast_delays, individual_times)):
            print(f"弹 {i+1}: 投放={drop:.2f}s, 延迟={delay:.2f}s, 有效={time:.3f}s")
        print(f"总有效遮蔽时间: {total_time:.3f}s")
    
    return total_time

def check_time_constraints(drop_times, min_interval=1.0):
    """检查投放时间间隔约束"""
    if len(drop_times) <= 1:
        return True
    sorted_times = np.sort(drop_times)
    intervals = np.diff(sorted_times)
    return np.all(intervals >= min_interval)

def objective_multiple_bombs(params, num_bombs=3):
    """多弹目标函数"""
    theta = params[0]
    s = params[1]
    
    # 提取投放时间和起爆延迟
    drop_times = [params[2 + 2*i] for i in range(num_bombs)]
    blast_delays = [params[3 + 2*i] for i in range(num_bombs)]
    
    # 检查时间间隔约束
    if not check_time_constraints(drop_times):
        # 基于违反程度计算惩罚值
        sorted_times = np.sort(drop_times)
        intervals = np.diff(sorted_times)
        violation = max(0, 1.0 - np.min(intervals)) if len(intervals) > 0 else 1.0
        penalty = 50.0 + 50.0 * violation  # 惩罚与违反程度成正比
        return penalty
    
    u = np.cos(theta)
    v = np.sin(theta)
    dir_vec = [u, v, 0]
    
    try:
        total_time = calculate_multiple_smoke_obscuration(
            drone_direction=dir_vec,
            drone_speed=s,
            drop_times=drop_times,
            blast_delays=blast_delays,
            verbose=False
        )
        
        return -total_time
        
    except Exception as e:
        print(f"计算错误: {e}")
        return 100.0

class AdvancedOptimizationTracker:
    def __init__(self, num_bombs=3):
        self.num_bombs = num_bombs
        self.history = []
        self.params_history = []
        self.best_history = []
        self.best_value = float('inf')
        self.best_params = None
        self.valid_solutions = 0
    
    def __call__(self, x, f, accepted):
        self.history.append(f)
        self.params_history.append(x.copy())
        
        is_valid = f < 50  # 有效解判断
        
        if is_valid:
            self.valid_solutions += 1
            
        if f < self.best_value:
            self.best_value = f
            self.best_params = x.copy()
        
        self.best_history.append(self.best_value)
        
        if len(self.history) % 5 == 0:
            theta = x[0]
            s = x[1]
            drop_times = [x[2 + 2*i] for i in range(self.num_bombs)]
            
            if is_valid:
                status = "有效"
                time_info = f"Current = {-f:.3f}s, Best = {-self.best_value:.3f}s"
            else:
                status = "无效(违反约束)"
                time_info = f"惩罚值 = {f:.1f}"
                
            print(f"Iteration {len(self.history)}: {status}, {time_info}")
            print(f"  Theta: {np.degrees(theta):.1f}°, Speed: {s:.1f} m/s")
            print(f"  投放时间: {[f'{t:.2f}' for t in drop_times]}s")
            
            # 每20次迭代显示详细统计
            if len(self.history) % 20 == 0:
                valid_ratio = self.valid_solutions / len(self.history) * 100
                print(f"  有效解比例: {valid_ratio:.1f}%")

def adaptive_step_size(x, iteration, max_iterations):
    """自适应步长调整"""
    base_step = 0.3
    # 随着迭代进行，逐渐减小步长
    decay_factor = 1.0 - (iteration / max_iterations) * 0.8
    return base_step * decay_factor

def custom_take_step(bounds, max_iterations):
    """自定义步长生成函数"""
    def take_step(x):
        iteration = len(take_step.counter) if hasattr(take_step, 'counter') else 0
        step_size = adaptive_step_size(x, iteration, max_iterations)
        
        # 为不同参数类型设置不同的扰动策略
        new_x = x.copy()
        
        # 角度参数 - 较小扰动
        new_x[0] += np.random.normal(0, step_size * 0.1)
        
        # 速度参数 - 中等扰动
        new_x[1] += np.random.normal(0, step_size * 0.5)
        
        # 投放时间参数 - 相关扰动（保持间隔）
        for i in range(2, len(x), 2):
            # 对投放时间进行相关扰动
            base_perturb = np.random.normal(0, step_size * 0.3)
            new_x[i] += base_perturb
            
            # 对起爆延迟进行较小扰动
            new_x[i+1] += np.random.normal(0, step_size * 0.2)
        
        # 确保参数在边界内
        for i in range(len(new_x)):
            new_x[i] = max(bounds[i][0], min(bounds[i][1], new_x[i]))
        
        # 记录迭代次数
        if not hasattr(take_step, 'counter'):
            take_step.counter = []
        take_step.counter.append(1)
        
        return new_x
    return take_step

def hybrid_optimization(objective_func, initial_params, bounds, num_bombs=3, max_iterations=100):
    """混合优化策略：遗传算法 + 模拟退火"""
    
    print("第一阶段：使用遗传算法进行全局搜索...")
    
    # 遗传算法进行粗搜索
    ga_result = differential_evolution(
        objective_func,
        bounds,
        strategy='best1bin',
        maxiter=20,
        popsize=15,
        tol=1e-4,
        mutation=(0.5, 1),
        recombination=0.7,
        disp=True
    )
    
    print(f"遗传算法结果: {-ga_result.fun:.3f}s")
    
    # 使用遗传算法结果作为模拟退火的初始点
    sa_initial = ga_result.x
    
    print("\n第二阶段：使用改进的模拟退火进行精细优化...")
    
    # 创建改进的跟踪器
    tracker = AdvancedOptimizationTracker(num_bombs=num_bombs)
    
    # 自定义步长函数
    take_step = custom_take_step(bounds, max_iterations)
    
    # 改进的模拟退火
    minimizer_kwargs = {
        "method": "L-BFGS-B", 
        "bounds": bounds,
        "options": {"maxiter": 15, "ftol": 1e-4}
    }
    
    result_sa = basinhopping(
        objective_func,
        sa_initial, 
        niter=max_iterations,
        minimizer_kwargs=minimizer_kwargs, 
        take_step=take_step,
        T=2.0,
        stepsize=0.3,
        callback=tracker,
        niter_success=15,
        interval=10
    )
    
    return result_sa, tracker

def plot_optimization_results(tracker):
    """绘制优化过程图表"""
    
    plt.figure(figsize=(15, 5))
    
    # 1. 收敛曲线
    valid_mask = np.array(tracker.history) < 50
    valid_indices = np.where(valid_mask)[0]
    if len(valid_indices) > 0:
        plt.subplot(1, 3, 1)
        plt.plot(valid_indices + 1, -np.array(tracker.history)[valid_indices], 'b-', alpha=0.7, label='当前值')
        plt.plot(valid_indices + 1, -np.array(tracker.best_history)[valid_indices], 'r-', linewidth=2, label='最佳值')
        plt.xlabel('迭代次数')
        plt.ylabel('有效遮蔽时间 (s)')
        plt.title('优化收敛曲线')
        plt.legend()
        plt.grid(True)
    
    # 2. 参数变化
    plt.subplot(1, 3, 2)
    theta_vals = [np.degrees(p[0]) for p in tracker.params_history]
    speed_vals = [p[1] for p in tracker.params_history]
    plt.plot(theta_vals, 'g-', alpha=0.7, label='角度 (°)')
    plt.plot(speed_vals, 'b-', alpha=0.7, label='速度 (m/s)')
    plt.xlabel('迭代次数')
    plt.ylabel('参数值')
    plt.title('主要参数变化')
    plt.legend()
    plt.grid(True)
    
    # 3. 有效解比例
    plt.subplot(1, 3, 3)
    window_size = 10
    valid_ratios = []
    for i in range(len(tracker.history)):
        start = max(0, i - window_size + 1)
        window = tracker.history[start:i+1]
        valid_count = sum(1 for val in window if val < 50)
        valid_ratios.append(valid_count / len(window) * 100)
    
    plt.plot(valid_ratios, 'm-', linewidth=2)
    plt.xlabel('迭代次数')
    plt.ylabel('有效解比例 (%)')
    plt.title('有效解比例（滑动窗口）')
    plt.grid(True)
    plt.ylim(0, 100)
    
    plt.tight_layout()
    plt.show()

def main():
    print("=== 问题3：多弹协同优化策略 ===\n")
    
    # 设置3弹优化
    num_bombs = 3
    
    # 变量边界
    bounds = [
        (3.0, 3.3),      # theta (172°-189°)
        (70, 140),       # speed
    ]
    
    # 为每个弹添加投放时间和起爆延迟边界
    for i in range(num_bombs):
        bounds.append((i * 1.0, i * 1.0 + 5.0))  # 投放时间，确保有重叠空间
        bounds.append((1, 8))                    # blast_delay
    
    # 基于单弹最优解设置初始参数
    single_opt_theta = 3.124
    single_opt_speed = 121.32
    
    # 初始参数 - 确保时间间隔
    initial_params = np.array([
        single_opt_theta,
        single_opt_speed,
        1.0, 3.0,    # 弹1
        3.0, 4.0,    # 弹2（与弹1间隔2秒）
        5.0, 5.0     # 弹3（与弹2间隔2秒）
    ])
    
    print("开始混合优化...")
    result, tracker = hybrid_optimization(
        lambda x: objective_multiple_bombs(x, num_bombs),
        initial_params,
        bounds,
        num_bombs=num_bombs,
        max_iterations=80
    )
    
    # 提取最佳参数
    if tracker.best_value < 50:  # 有效解
        best_params = tracker.best_params
        best_time = -tracker.best_value
        print(f"\n优化成功！最佳时间: {best_time:.3f}s")
    else:
        best_params = initial_params
        best_time = -objective_multiple_bombs(initial_params, num_bombs)
        print("\n优化未找到更好解，使用初始参数")
    
    theta = best_params[0]
    speed = best_params[1]
    drop_times = [best_params[2 + 2*i] for i in range(num_bombs)]
    blast_delays = [best_params[3 + 2*i] for i in range(num_bombs)]
    
    print(f"\n=== 最终结果 ===")
    print(f"最佳方向角度: {np.degrees(theta):.1f}°")
    print(f"最佳速度: {speed:.2f} m/s")
    for i in range(num_bombs):
        print(f"弹 {i+1}: 投放={drop_times[i]:.3f}s, 延迟={blast_delays[i]:.3f}s")
    
    # 验证时间间隔
    sorted_times = np.sort(drop_times)
    intervals = np.diff(sorted_times)
    print(f"\n时间间隔: {intervals}")
    print(f"时间间隔检查:")
    for i, interval in enumerate(intervals):
        status = "✓" if interval >= 1.0 else "✗"
        print(f"弹 {i+1} 和弹 {i+2} 间隔: {interval:.3f}s {status}")
    
    # 验证效果
    print("\n验证最终策略效果:")
    final_time = calculate_multiple_smoke_obscuration(
        drone_direction=[np.cos(theta), np.sin(theta), 0],
        drone_speed=speed,
        drop_times=drop_times,
        blast_delays=blast_delays,
        verbose=True
    )
    
    # 计算单弹最优时间用于对比
    single_bomb_time = calculate_smoke_obscuration(
        drone_direction=[np.cos(single_opt_theta), np.sin(single_opt_theta), 0],
        drone_speed=single_opt_speed,
        drop_time=0.37,  # 单弹最优投放时间
        blast_delay=3.48,  # 单弹最优起爆延迟
        verbose=False
    )
    
    print(f"\n=== 性能对比 ===")
    print(f"单弹最优时间: {single_bomb_time:.3f}s")
    print(f"3弹优化时间: {final_time:.3f}s")
    print(f"3弹相比单弹提升: {final_time - single_bomb_time:.3f}s "
          f"({(final_time - single_bomb_time)/single_bomb_time*100:.1f}%)")
    
    # 绘制优化过程
    plot_optimization_results(tracker)
    
    # 保存结果到Excel
    save_problem3_results_to_excel(theta, speed, drop_times, blast_delays, final_time)
    
    # 输出最终策略
    print("\n=== 最终3弹投放策略 ===")
    print(f"无人机飞行方向: {np.degrees(theta):.1f}°")
    print(f"无人机飞行速度: {speed:.2f} m/s")
    print(f"方向向量: [{np.cos(theta):.4f}, {np.sin(theta):.4f}, 0.000]")
    
    for i in range(num_bombs):
        blast_time = drop_times[i] + blast_delays[i]
        print(f"\n弹 {i+1}:")
        print(f"  投放时间: 任务开始后 {drop_times[i]:.3f} s")
        print(f"  起爆延迟: {blast_delays[i]:.3f} s")
        print(f"  预计起爆时间: {blast_time:.3f} s")
    
    print(f"\n预计总有效遮蔽时间: {final_time:.3f} s")
    print(f"相比单弹策略提升: {final_time - single_bomb_time:.3f} s "
          f"({(final_time - single_bomb_time)/single_bomb_time*100:.1f}%)")

def save_problem3_results_to_excel(theta, speed, drop_times, blast_delays, total_time, filename='result1.xlsx'):
    """将Problem3结果保存到Excel文件"""
    import pandas as pd
    
    # 无人机FY1位置
    FY1_start = np.array([17800, 0, 1800])
    direction = np.array([np.cos(theta), np.sin(theta), 0])
    v_drone = direction * speed
    
    # 准备数据
    results_data = []
    for i in range(len(drop_times)):
        # 计算投放位置和起爆位置
        drop_pos = FY1_start + v_drone * drop_times[i]
        blast_pos = drop_pos + v_drone * blast_delays[i] + np.array([0, 0, -0.5 * 9.8 * blast_delays[i]**2])
        
        results_data.append({
            '烟幕弹编号': f'弹{i+1}',
            '无人机编号': 'FY1',
            '飞行方向(度)': np.degrees(theta),
            '飞行速度(m/s)': speed,
            '投放时间(s)': drop_times[i],
            '起爆延迟(s)': blast_delays[i],
            '投放位置X(m)': drop_pos[0],
            '投放位置Y(m)': drop_pos[1],
            '投放位置Z(m)': drop_pos[2],
            '起爆位置X(m)': blast_pos[0],
            '起爆位置Y(m)': blast_pos[1],
            '起爆位置Z(m)': blast_pos[2],
            '预计有效时间(s)': total_time / 3  # 平均分配
        })
    
    # 创建DataFrame并保存
    df = pd.DataFrame(results_data)
    df.to_excel(filename, index=False)
    
    # 添加汇总信息
    with pd.ExcelWriter(filename, mode='a', engine='openpyxl') as writer:
        summary_df = pd.DataFrame({
            '指标': ['总有效遮蔽时间', '烟幕弹数量', '无人机编号', '目标导弹'],
            '数值': [f'{total_time:.3f}s', '3枚', 'FY1', 'M1']
        })
        summary_df.to_excel(writer, sheet_name='汇总', index=False)
    
    print(f"\n结果已保存到 {filename}")


if __name__ == "__main__":
    main()
