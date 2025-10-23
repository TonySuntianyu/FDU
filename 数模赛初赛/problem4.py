"""
问题4：无人机FY2的策略参数搜索

使用遗传算法搜索无人机FY2针对导弹M2的有效策略参数。
由于初始位置改变，需要重新调整计算函数。
"""

import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import tqdm
import random

# 设置中文字体支持
plt.rcParams['font.sans-serif'] = ['SimHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

def calculate_smoke_obscuration_fy2(drone_direction, drone_speed, drop_time, blast_delay, 
                                   visualize=False, verbose=False):
    """
    计算烟幕干扰弹对导弹M2的有效遮蔽时间
    针对无人机FY2和导弹M2的位置进行计算
    """
    
    # 定义常量
    g = 9.8
    v_missile = 300
    v_smoke_sink = 3
    effective_radius = 10
    effective_duration = 20

    # 目标位置
    fake_target = np.array([0, 0, 0])
    real_target = np.array([0, 200, 0])

    # 导弹M2初始位置
    M2_start = np.array([19000, 600, 2100])

    # 无人机FY2初始位置
    FY2_start = np.array([12000, 1400, 1400])
    
    # 标准化无人机方向向量
    drone_direction = np.array(drone_direction)
    if np.linalg.norm(drone_direction) > 0:
        drone_direction = drone_direction / np.linalg.norm(drone_direction)
    
    # 无人机速度向量
    v_drone_vector = drone_speed * drone_direction
    
    if verbose:
        print(f"无人机速度向量: {v_drone_vector}")

    # 时间参数
    t_drop = drop_time
    t_blast_delay = blast_delay
    t_blast = t_drop + t_blast_delay

    # 计算位置
    drop_position = FY2_start + v_drone_vector * t_drop
    vertical_drop = 0.5 * g * t_blast_delay**2
    blast_position = np.array([drop_position[0] + v_drone_vector[0] * t_blast_delay, 
                              drop_position[1] + v_drone_vector[1] * t_blast_delay, 
                              drop_position[2] - vertical_drop])

    if verbose:
        print(f"投放点位置: {drop_position}")
        print(f"起爆点位置: {blast_position}")

    # 导弹飞行方向（指向假目标）
    missile_direction = fake_target - M2_start
    missile_direction = missile_direction / np.linalg.norm(missile_direction)
    v_missile_vector = v_missile * missile_direction
    
    if verbose:
        print(f"导弹速度向量: {v_missile_vector}")
        print(f"导弹初始位置: {M2_start}")

    # 计算导弹到达假目标时间
    def time_to_target(position, velocity, target):
        t_x = (target[0] - position[0]) / velocity[0]
        return t_x

    t_missile_to_target = time_to_target(M2_start, v_missile_vector, fake_target)
    
    if verbose:
        print(f"导弹到达假目标时间: {t_missile_to_target:.2f} s")

    # 计算导弹在时间t的位置
    def missile_position(t):
        return M2_start + v_missile_vector * t

    # 计算烟幕云团在时间t的位置（从起爆开始）
    def smoke_position(t_smoke):
        return np.array([blast_position[0], blast_position[1], blast_position[2] - v_smoke_sink * t_smoke])

    # 计算角度条件：判断烟幕是否在导弹和真目标之间
    def is_smoke_between(missile_pos, smoke_pos, target_pos):
        missile_to_smoke = smoke_pos - missile_pos
        missile_to_target = target_pos - missile_pos
        
        dot_product = np.dot(missile_to_smoke, missile_to_target)
        cos_angle = dot_product / (np.linalg.norm(missile_to_smoke) * np.linalg.norm(missile_to_target))
        
        return cos_angle > 0

    # 计算点到直线距离
    def distance_point_to_line(point, line_point, line_direction):
        ap = point - line_point
        projection = np.dot(ap, line_direction) / np.linalg.norm(line_direction)
        foot_point = line_point + projection * line_direction
        return np.linalg.norm(point - foot_point)

    # 判断导弹是否经过云团
    def is_missile_through_smoke(missile_pos, smoke_pos, missile_prev_pos, smoke_prev_pos):
        radius = effective_radius
        
        missile_move = missile_pos - missile_prev_pos
        missile_move_length = np.linalg.norm(missile_move)
        
        if missile_move_length == 0:
            return False
        
        smoke_move = smoke_pos - smoke_prev_pos
        relative_move = missile_move - smoke_move
        relative_move_length = np.linalg.norm(relative_move)
        
        if relative_move_length == 0:
            return np.linalg.norm(missile_pos - smoke_pos) <= radius
        
        relative_direction = relative_move / relative_move_length
        smoke_to_missile = missile_prev_pos - smoke_prev_pos
        projection = np.dot(smoke_to_missile, relative_direction)
        perpendicular_dist = np.linalg.norm(smoke_to_missile - projection * relative_direction)
        
        if perpendicular_dist <= radius and 0 <= projection <= relative_move_length:
            return True
        
        if np.linalg.norm(missile_prev_pos - smoke_prev_pos) <= radius:
            return True
        if np.linalg.norm(missile_pos - smoke_pos) <= radius:
            return True
        
        return False

    # 计算有效遮蔽时间
    start_time = 0
    end_time = min(effective_duration, t_missile_to_target - t_blast)
    
    if end_time <= 0:
        return 0.0
    
    time_step = 0.01
    total_effective_time = 0.0
    current_time = start_time
    
    # 存储上一时刻的位置用于判断穿越
    prev_missile_pos = missile_position(t_blast)
    prev_smoke_pos = smoke_position(0)
    
    while current_time <= end_time:
        t_missile = t_blast + current_time
        if t_missile > t_missile_to_target:
            break
            
        pos_missile = missile_position(t_missile)
        pos_smoke = smoke_position(current_time)
        
        # 计算距离
        missile_to_target_direction = real_target - pos_missile
        missile_to_target_direction = missile_to_target_direction / np.linalg.norm(missile_to_target_direction)
        distance = distance_point_to_line(pos_smoke, pos_missile, missile_to_target_direction)
        
        # 检查角度条件
        is_between = is_smoke_between(pos_missile, pos_smoke, real_target)
        
        # 检查导弹是否穿过烟幕云团
        is_through = is_missile_through_smoke(pos_missile, pos_smoke, prev_missile_pos, prev_smoke_pos)
        
        # 检查导弹是否在云团内部
        in_smoke = np.linalg.norm(pos_missile - pos_smoke) <= effective_radius
        
        if (distance <= effective_radius and is_between) or is_through or in_smoke:
            total_effective_time += time_step
        
        # 更新上一时刻位置
        prev_missile_pos = pos_missile
        prev_smoke_pos = pos_smoke
        
        current_time += time_step
    
    if verbose:
        print(f"\n=== 计算结果 ===")
        print(f"烟幕弹投放时间: {t_drop:.1f} s")
        print(f"烟幕弹起爆时间: {t_blast:.1f} s")
        print(f"起爆点位置: ({blast_position[0]:.1f}, {blast_position[1]:.1f}, {blast_position[2]:.1f})")
        print(f"导弹到达假目标时间: {t_missile_to_target:.2f} s")
        print(f"有效遮蔽时长: {total_effective_time:.3f} 秒")

    if visualize:
        # 可视化
        fig = plt.figure(figsize=(15, 10))
        ax = fig.add_subplot(111, projection='3d')

        # 绘制轨迹
        missile_times = np.linspace(0, t_missile_to_target, 100)
        missile_traj = np.array([missile_position(t) for t in missile_times])
        ax.plot(missile_traj[:, 0], missile_traj[:, 1], missile_traj[:, 2], 'r-', label='导弹M2轨迹', linewidth=2)

        # 绘制烟幕下沉轨迹
        smoke_times = np.linspace(0, min(effective_duration, t_missile_to_target - t_blast), 50)
        smoke_traj = np.array([smoke_position(t) for t in smoke_times])
        ax.plot(smoke_traj[:, 0], smoke_traj[:, 1], smoke_traj[:, 2], 'b-', label='烟幕下沉轨迹', linewidth=2)

        # 标记关键点
        ax.scatter(*fake_target, color='red', s=200, label='假目标', marker='x')
        ax.scatter(*real_target, color='blue', s=200, label='真目标', marker='o')
        ax.scatter(*M2_start, color='orange', s=100, label='导弹M2起点')
        ax.scatter(*FY2_start, color='green', s=100, label='无人机FY2起点')
        ax.scatter(*blast_position, color='purple', s=100, label='起爆点')

        # 设置视角和标签
        ax.view_init(elev=20, azim=45)
        ax.set_xlabel('X (m)')
        ax.set_ylabel('Y (m)')
        ax.set_zlabel('Z (m)')
        ax.set_title(f'烟幕干扰弹对导弹M2的遮蔽效果分析\n有效遮蔽时间: {total_effective_time:.3f}秒')
        ax.legend()
        ax.grid(True)

        plt.tight_layout()
        plt.show()

    return total_effective_time


# 自定义遗传算法实现
class GeneticAlgorithm:
    def __init__(self, pop_size=100, crossover_rate=0.7, mutation_rate=0.2, generations=50):
        self.pop_size = pop_size
        self.crossover_rate = crossover_rate
        self.mutation_rate = mutation_rate
        self.generations = generations
        
        # 参数范围
        self.param_ranges = [
            (0, 63),    # i的范围 (角度参数)
            (80, 120),  # j的范围 (速度)
            (0, 30),    # q的范围 (投放时间)
            (0, 15)     # s的范围 (起爆延迟)
        ]
    
    def create_individual(self):
        # 创建一个个体（一组参数）
        individual = []
        for param_range in self.param_ranges:
            individual.append(random.randint(param_range[0], param_range[1] - 1))
        return individual
    
    def create_population(self):
        # 创建初始种群
        return [self.create_individual() for _ in range(self.pop_size)]
    
    def evaluate(self, individual):
        # 评估个体的适应度
        i, j, q, s = individual
        result = calculate_smoke_obscuration_fy2(
            drone_direction=[np.cos(i/10), np.sin(i/10), 0],
            drone_speed=j,
            drop_time=q,
            blast_delay=s,
            visualize=False,
            verbose=False
        )
        return result  # 返回适应度值
    
    def select(self, population, fitnesses):
        # 锦标赛选择
        selected = []
        for _ in range(self.pop_size):
            # 随机选择3个个体进行竞争
            candidates = random.sample(list(zip(population, fitnesses)), 3)
            # 选择适应度最高的
            winner = max(candidates, key=lambda x: x[1])[0]
            selected.append(winner)
        return selected
    
    def crossover(self, parent1, parent2):
        # 单点交叉
        if random.random() < self.crossover_rate:
            point = random.randint(1, len(parent1) - 1)
            child1 = parent1[:point] + parent2[point:]
            child2 = parent2[:point] + parent1[point:]
            return child1, child2
        return parent1, parent2
    
    def mutate(self, individual):
        # 均匀变异
        mutated = individual.copy()
        for i in range(len(mutated)):
            if random.random() < self.mutation_rate:
                low, high = self.param_ranges[i]
                mutated[i] = random.randint(low, high - 1)
        return mutated
    
    def run(self):
        # 创建初始种群
        population = self.create_population()
        
        # 存储所有满足条件的解
        solutions = []
        
        print("开始遗传算法优化...")
        for gen in tqdm.tqdm(range(self.generations)):
            # 评估种群中所有个体
            fitnesses = [self.evaluate(ind) for ind in population]
            
            # 记录满足条件的解
            for i, fit in enumerate(fitnesses):
                if fit != 0:
                    solutions.append((population[i], fit))
            
            # 选择
            selected = self.select(population, fitnesses)
            
            # 交叉和变异
            next_population = []
            for i in range(0, self.pop_size, 2):
                parent1 = selected[i]
                parent2 = selected[i+1] if i+1 < self.pop_size else selected[0]
                
                child1, child2 = self.crossover(parent1, parent2)
                child1 = self.mutate(child1)
                child2 = self.mutate(child2)
                
                next_population.extend([child1, child2])
            
            # 确保种群大小不变
            population = next_population[:self.pop_size]
        
        # 去除重复的解
        unique_solutions = []
        seen = set()
        for sol, fit in solutions:
            key = tuple(sol)
            if key not in seen:
                seen.add(key)
                unique_solutions.append((sol, fit))
        
        return unique_solutions

def main():
    print("=== 问题4：无人机FY2的策略参数搜索 ===\n")
    
    # 首先测试一个示例参数
    print("测试示例参数...")
    test_time = calculate_smoke_obscuration_fy2(
        drone_direction=[-0.5, -0.5, 0],
        drone_speed=100,
        drop_time=20,
        blast_delay=6,
        visualize=True,
        verbose=True
    )
    print(f"\n示例参数的有效遮蔽时间: {test_time:.3f}s")
    
    # 创建遗传算法实例并运行
    print("\n开始使用遗传算法搜索有效参数...")
    ga = GeneticAlgorithm(pop_size=100, generations=50)
    solutions = ga.run()
    
    # 输出结果
    if solutions:
        print(f"\n找到了 {len(solutions)} 个满足条件的解:")
        
        # 按照适应度排序
        solutions.sort(key=lambda x: x[1], reverse=True)
        
        # 显示前10个最好的解
        print("\n前10个最佳解:")
        for i, (sol, fit) in enumerate(solutions[:10], 1):
            i_val, j_val, q_val, s_val = sol
            angle_deg = np.degrees(i_val/10)
            print(f"解 {i}: 角度={angle_deg:.1f}°, 速度={j_val}m/s, "
                  f"投放时间={q_val}s, 起爆延迟={s_val}s, "
                  f"有效时间={fit:.3f}s")
        
        # 可视化最佳解
        if solutions:
            best_sol, best_fit = solutions[0]
            i_val, j_val, q_val, s_val = best_sol
            
            print(f"\n最佳解详细信息:")
            print(f"方向角度: {np.degrees(i_val/10):.1f}°")
            print(f"方向向量: [{np.cos(i_val/10):.4f}, {np.sin(i_val/10):.4f}, 0]")
            print(f"飞行速度: {j_val} m/s")
            print(f"投放时间: {q_val} s")
            print(f"起爆延迟: {s_val} s")
            print(f"有效遮蔽时间: {best_fit:.3f} s")
            
            print("\n验证最佳解...")
            verify_time = calculate_smoke_obscuration_fy2(
                drone_direction=[np.cos(i_val/10), np.sin(i_val/10), 0],
                drone_speed=j_val,
                drop_time=q_val,
                blast_delay=s_val,
                visualize=True,
                verbose=True
            )
            print(f"\n验证结果：有效遮蔽时间 = {verify_time:.3f} s")
    else:
        print("\n未找到满足条件的解")
    
    # 绘制解的分布
    if solutions:
        plt.figure(figsize=(10, 6))
        
        # 提取各参数
        angles = [np.degrees(sol[0][0]/10) for sol in solutions]
        speeds = [sol[0][1] for sol in solutions]
        times = [sol[1] for sol in solutions]
        
        plt.scatter(angles, speeds, c=times, cmap='viridis', s=50, alpha=0.6)
        plt.colorbar(label='有效遮蔽时间 (s)')
        plt.xlabel('飞行方向角度 (°)')
        plt.ylabel('飞行速度 (m/s)')
        plt.title('无人机FY2有效策略参数分布')
        plt.grid(True, alpha=0.3)
        plt.tight_layout()
        plt.show()


if __name__ == "__main__":
    main()
