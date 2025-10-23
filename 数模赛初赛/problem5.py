"""
问题5：多无人机与多导弹的匹配分配

使用匈牙利算法进行最优匹配，确保每个导弹分配一个最合适的无人机进行烟幕干扰。
分配原则基于无人机到导弹飞行直线的垂直距离。
"""

import numpy as np
from scipy.optimize import linear_sum_assignment
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

# 设置中文字体支持
plt.rcParams['font.sans-serif'] = ['SimHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

# 定义导弹位置和飞行方向（指向原点）
missiles = {
    'M1': np.array([20000, 0, 2000]),
    'M2': np.array([19000, 600, 2100]),
    'M3': np.array([18000, -600, 1900])
}

drones = {
    'FY1': np.array([17800, 0, 1800]),
    'FY2': np.array([12000, 1400, 1400]),
    'FY3': np.array([6000, -3000, 700]),
    'FY4': np.array([11000, 2000, 1800]),
    'FY5': np.array([13000, -2000, 1300])
}

# 原点（假目标）和真目标
origin = np.array([0, 0, 0])
real_target = np.array([0, 200, 0])

def distance_to_line(point, line_point, direction):
    """
    计算点到直线的距离
    point: 无人机位置
    line_point: 导弹位置（直线上一点）
    direction: 直线方向向量（指向原点）
    """
    # 计算向量
    vec_to_point = point - line_point
    # 点到直线的距离公式：|(AP) × u| / |u|
    cross_product = np.linalg.norm(np.cross(vec_to_point, direction))
    distance = cross_product / np.linalg.norm(direction)
    return distance

def visualize_battlefield(assignment_result):
    """可视化战场态势和分配结果"""
    fig = plt.figure(figsize=(16, 10))
    ax = fig.add_subplot(111, projection='3d')
    
    # 绘制目标
    ax.scatter(*origin, color='red', s=300, marker='x', label='假目标（原点）')
    ax.scatter(*real_target, color='blue', s=300, marker='o', label='真目标')
    
    # 绘制导弹和分配的连线
    missile_names = list(missiles.keys())
    drone_names = list(drones.keys())
    
    colors = ['red', 'green', 'purple']  # 为每个导弹分配不同颜色
    
    for i, (m_idx, d_idx) in enumerate(assignment_result):
        missile_name = missile_names[m_idx]
        drone_name = drone_names[d_idx]
        missile_pos = missiles[missile_name]
        drone_pos = drones[drone_name]
        
        # 绘制导弹
        ax.scatter(*missile_pos, color=colors[i], s=200, marker='^', 
                  label=f'{missile_name}')
        
        # 绘制导弹飞行轨迹
        missile_traj = np.array([missile_pos + t * (origin - missile_pos) 
                                for t in np.linspace(0, 1, 50)])
        ax.plot(missile_traj[:, 0], missile_traj[:, 1], missile_traj[:, 2], 
                color=colors[i], linestyle='--', alpha=0.5)
        
        # 绘制分配连线
        ax.plot([missile_pos[0], drone_pos[0]], 
                [missile_pos[1], drone_pos[1]], 
                [missile_pos[2], drone_pos[2]], 
                color=colors[i], linewidth=2, alpha=0.7,
                label=f'{missile_name} → {drone_name}')
    
    # 绘制所有无人机
    for name, pos in drones.items():
        ax.scatter(*pos, color='green', s=100, marker='s')
        ax.text(pos[0], pos[1], pos[2] + 100, name, fontsize=10)
    
    # 设置标签和标题
    ax.set_xlabel('X (m)')
    ax.set_ylabel('Y (m)')
    ax.set_zlabel('Z (m)')
    ax.set_title('导弹-无人机最优分配方案\n（基于垂直距离最小化）')
    ax.legend(loc='upper right', bbox_to_anchor=(1.15, 1))
    ax.grid(True, alpha=0.3)
    
    # 设置视角
    ax.view_init(elev=25, azim=-60)
    
    plt.tight_layout()
    plt.show()

def analyze_assignment_quality(cost_matrix, row_ind, col_ind):
    """分析分配质量"""
    missile_names = list(missiles.keys())
    drone_names = list(drones.keys())
    
    # 计算总成本
    total_cost = sum(cost_matrix[r, c] for r, c in zip(row_ind, col_ind))
    
    # 计算平均成本
    avg_cost = total_cost / len(row_ind)
    
    # 找出最大和最小距离
    distances = [cost_matrix[r, c] for r, c in zip(row_ind, col_ind)]
    max_dist = max(distances)
    min_dist = min(distances)
    
    print("\n=== 分配质量分析 ===")
    print(f"总垂直距离: {total_cost:.2f} m")
    print(f"平均垂直距离: {avg_cost:.2f} m")
    print(f"最大垂直距离: {max_dist:.2f} m ({missile_names[row_ind[distances.index(max_dist)]]} - {drone_names[col_ind[distances.index(max_dist)]]})")
    print(f"最小垂直距离: {min_dist:.2f} m ({missile_names[row_ind[distances.index(min_dist)]]} - {drone_names[col_ind[distances.index(min_dist)]]})")
    
    # 计算其他可能方案的比较
    print("\n=== 与其他方案比较 ===")
    
    # 最近距离分配（贪心算法）
    greedy_total = 0
    used_drones = set()
    greedy_assignments = []
    
    for i in range(len(missile_names)):
        min_dist_idx = -1
        min_dist_val = float('inf')
        
        for j in range(len(drone_names)):
            if j not in used_drones and cost_matrix[i, j] < min_dist_val:
                min_dist_val = cost_matrix[i, j]
                min_dist_idx = j
        
        if min_dist_idx != -1:
            used_drones.add(min_dist_idx)
            greedy_total += min_dist_val
            greedy_assignments.append((i, min_dist_idx))
    
    print(f"贪心算法总距离: {greedy_total:.2f} m")
    print(f"优化提升: {((greedy_total - total_cost) / greedy_total * 100):.1f}%")

def main():
    print("=== 问题5：多无人机与多导弹的匹配分配 ===\n")
    
    # 创建成本矩阵（行：导弹，列：无人机）
    cost_matrix = np.zeros((3, 5))
    
    missile_names = list(missiles.keys())
    drone_names = list(drones.keys())
    
    print("计算所有无人机到各导弹飞行直线的垂直距离...")
    
    for i, m in enumerate(missile_names):
        missile_pos = missiles[m]
        # 导弹飞行方向向量（指向原点）
        direction_vector = origin - missile_pos
        
        for j, d in enumerate(drone_names):
            drone_pos = drones[d]
            # 计算无人机到导弹飞行直线的距离
            dist = distance_to_line(drone_pos, missile_pos, direction_vector)
            cost_matrix[i, j] = dist
    
    # 使用匈牙利算法找到最小总成本的分配
    print("\n使用匈牙利算法求解最优分配...")
    row_ind, col_ind = linear_sum_assignment(cost_matrix)
    
    # 输出分配结果
    print("\n=== 最优分配结果 ===")
    assignment_pairs = []
    for i in range(len(row_ind)):
        m = missile_names[row_ind[i]]
        d = drone_names[col_ind[i]]
        dist = cost_matrix[row_ind[i], col_ind[i]]
        print(f"{m} 分配给 {d}，垂直距离为 {dist:.2f} 米")
        assignment_pairs.append((row_ind[i], col_ind[i]))
    
    # 输出所有距离矩阵
    print("\n=== 完整距离矩阵 ===")
    print("（行：导弹，列：无人机，单位：米）")
    
    # 打印表头
    header = "      " + "".join([f"{d:>10}" for d in drone_names])
    print(header)
    print("-" * (6 + 10 * len(drone_names)))
    
    # 打印矩阵内容
    for i, m in enumerate(missile_names):
        row = f"{m:>6}" + "".join([f"{cost_matrix[i, j]:>10.2f}" for j in range(5)])
        print(row)
    
    # 输出每个导弹的飞行方向向量
    print("\n=== 导弹飞行参数 ===")
    for m in missile_names:
        direction = origin - missiles[m]
        print(f"{m}:")
        print(f"  位置: {missiles[m]}")
        print(f"  飞行方向: {direction}")
        print(f"  到假目标距离: {np.linalg.norm(direction):.2f} m")
    
    # 分析分配质量
    analyze_assignment_quality(cost_matrix, row_ind, col_ind)
    
    # 可视化结果
    print("\n生成可视化图表...")
    visualize_battlefield(assignment_pairs)
    
    # 生成分配矩阵热力图
    plt.figure(figsize=(10, 6))
    
    # 创建一个标记矩阵，标记被选中的分配
    assignment_matrix = np.zeros_like(cost_matrix)
    for r, c in zip(row_ind, col_ind):
        assignment_matrix[r, c] = 1
    
    # 绘制热力图
    im = plt.imshow(cost_matrix, cmap='YlOrRd', aspect='auto')
    plt.colorbar(im, label='垂直距离 (m)')
    
    # 添加分配标记
    for r, c in zip(row_ind, col_ind):
        plt.plot(c, r, 'b*', markersize=20)
        plt.text(c, r, f'{cost_matrix[r, c]:.0f}', 
                ha='center', va='center', color='blue', fontweight='bold')
    
    # 设置刻度标签
    plt.xticks(range(len(drone_names)), drone_names)
    plt.yticks(range(len(missile_names)), missile_names)
    
    # 添加网格线
    for i in range(len(missile_names) + 1):
        plt.axhline(i - 0.5, color='black', linewidth=0.5)
    for i in range(len(drone_names) + 1):
        plt.axvline(i - 0.5, color='black', linewidth=0.5)
    
    plt.xlabel('无人机')
    plt.ylabel('导弹')
    plt.title('导弹-无人机距离矩阵与最优分配\n（蓝色星号表示最优分配）')
    plt.tight_layout()
    plt.show()
    
    print("\n分配方案总结：")
    for i in range(len(row_ind)):
        m = missile_names[row_ind[i]]
        d = drone_names[col_ind[i]]
        print(f"  • {m} → {d}")
    
    print(f"\n最优方案总垂直距离: {sum(cost_matrix[r, c] for r, c in zip(row_ind, col_ind)):.2f} 米")


if __name__ == "__main__":
    main()
