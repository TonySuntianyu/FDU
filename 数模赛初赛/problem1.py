"""
问题1：计算烟幕干扰弹对导弹M1的有效遮蔽时间

该程序分析无人机FY1投放烟幕干扰弹后，对导弹M1的遮蔽效果。
主要计算烟幕云团能够有效遮蔽导弹视线的时间长度。
"""

import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

# 设置中文字体支持
plt.rcParams['font.sans-serif'] = ['SimHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

def calculate_smoke_obscuration(drone_direction, drone_speed, drop_time, blast_delay, 
                               visualize=False, verbose=False):
    """
    计算烟幕干扰弹对导弹M1的有效遮蔽时间
    
    参数:
    drone_direction: 无人机飞行方向向量 (3D向量)
    drone_speed: 无人机飞行速度 (m/s)
    drop_time: 受领任务后投放时间 (s)
    blast_delay: 投放后到起爆的时间间隔 (s)
    visualize: 是否可视化结果
    verbose: 是否输出详细信息
    
    返回:
    effective_time: 有效遮蔽时间 (s)
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

    # 导弹M1初始位置
    M1_start = np.array([20000, 0, 2000])

    # 无人机FY1初始位置
    FY1_start = np.array([17800, 0, 1800])
    
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
    drop_position = FY1_start + v_drone_vector * t_drop
    vertical_drop = 0.5 * g * t_blast_delay**2
    blast_position = np.array([drop_position[0] + v_drone_vector[0] * t_blast_delay, 
                              drop_position[1] + v_drone_vector[1] * t_blast_delay, 
                              drop_position[2] - vertical_drop])

    if verbose:
        print(f"投放点位置: {drop_position}")
        print(f"起爆点位置: {blast_position}")

    # 导弹飞行方向（指向假目标）
    missile_direction = fake_target - M1_start
    missile_direction = missile_direction / np.linalg.norm(missile_direction)
    v_missile_vector = v_missile * missile_direction
    
    if verbose:
        print(f"导弹速度向量: {v_missile_vector}")
        print(f"导弹初始位置: {M1_start}")

    # 计算导弹到达假目标时间
    def time_to_target(position, velocity, target):
        t_x = (target[0] - position[0]) / velocity[0]
        return t_x

    t_missile_to_target = time_to_target(M1_start, v_missile_vector, fake_target)
    
    if verbose:
        print(f"导弹到达假目标时间: {t_missile_to_target:.2f} s")

    # 计算导弹在时间t的位置
    def missile_position(t):
        return M1_start + v_missile_vector * t

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

        # 详细分析关键时间点
        print(f"\n=== 关键时间点详细分析 ===")
        for t_check in [0, 1, 2, 3, 4, 5]:
            if t_check <= min(effective_duration, t_missile_to_target - t_blast):
                t_missile_check = t_blast + t_check
                pos_missile = missile_position(t_missile_check)
                pos_smoke = smoke_position(t_check)
                
                missile_to_target_direction = real_target - pos_missile
                missile_to_target_direction = missile_to_target_direction / np.linalg.norm(missile_to_target_direction)
                distance = distance_point_to_line(pos_smoke, pos_missile, missile_to_target_direction)
                
                is_between = is_smoke_between(pos_missile, pos_smoke, real_target)
                direct_distance = np.linalg.norm(pos_missile - pos_smoke)
                
                missile_to_smoke = pos_smoke - pos_missile
                missile_to_target = real_target - pos_missile
                dot_product = np.dot(missile_to_smoke, missile_to_target)
                angle_deg = np.degrees(np.arccos(dot_product / (np.linalg.norm(missile_to_smoke) * np.linalg.norm(missile_to_target))))
                
                print(f"起爆后 {t_check}s:")
                print(f"  导弹位置: {pos_missile}")
                print(f"  云团位置: {pos_smoke}")
                print(f"  到视线距离: {distance:.2f}m")
                print(f"  直接距离: {direct_distance:.2f}m")
                print(f"  烟幕在导弹目标之间: {'是' if is_between else '否'}")
                print(f"  导弹-烟幕-目标角度: {angle_deg:.1f}°")
                print(f"  是否在云团内: {'是' if direct_distance <= effective_radius else '否'}")
                print(f"  是否有效遮蔽: {'是' if (distance <= effective_radius and is_between) or direct_distance <= effective_radius else '否'}")

    if visualize:
        # 可视化
        fig = plt.figure(figsize=(15, 10))
        ax = fig.add_subplot(111, projection='3d')

        # 绘制轨迹
        missile_times = np.linspace(0, t_missile_to_target, 100)
        missile_traj = np.array([missile_position(t) for t in missile_times])
        ax.plot(missile_traj[:, 0], missile_traj[:, 1], missile_traj[:, 2], 'r-', label='导弹轨迹', linewidth=2)

        # 绘制烟幕下沉轨迹
        smoke_times = np.linspace(0, min(effective_duration, t_missile_to_target - t_blast), 50)
        smoke_traj = np.array([smoke_position(t) for t in smoke_times])
        ax.plot(smoke_traj[:, 0], smoke_traj[:, 1], smoke_traj[:, 2], 'b-', label='烟幕下沉轨迹', linewidth=2)

        # 标记关键点
        ax.scatter(*fake_target, color='red', s=200, label='假目标', marker='x')
        ax.scatter(*real_target, color='blue', s=200, label='真目标', marker='o')
        ax.scatter(*M1_start, color='orange', s=100, label='导弹起点')
        ax.scatter(*blast_position, color='green', s=100, label='起爆点')

        # 在关键时间点绘制烟幕云团
        for t in [0, 10, 20]:
            if t < len(smoke_times):
                smoke_pos = smoke_position(t)
                # 绘制球体表示烟幕云团
                u = np.linspace(0, 2 * np.pi, 20)
                v = np.linspace(0, np.pi, 20)
                x = smoke_pos[0] + effective_radius * np.outer(np.cos(u), np.sin(v))
                y = smoke_pos[1] + effective_radius * np.outer(np.sin(u), np.sin(v))
                z = smoke_pos[2] + effective_radius * np.outer(np.ones(np.size(u)), np.cos(v))
                ax.plot_surface(x, y, z, color='cyan', alpha=0.2)

        # 设置视角和标签，并对烟幕区域进行局部放大
        if len(smoke_traj) > 0:
            x_center = smoke_traj[:, 0].mean()
            y_center = smoke_traj[:, 1].mean()
            z_min, z_max = np.min(smoke_traj[:, 2]), np.max(smoke_traj[:, 2])
            
            margin_xy = 120  # 水平方向边距
            margin_z = 80   # 垂直方向边距
            
            ax.set_xlim(x_center - margin_xy, x_center + margin_xy)
            ax.set_ylim(y_center - margin_xy, y_center + margin_xy)
            ax.set_zlim(z_min - margin_z, z_max + margin_z)
            ax.set_title('烟幕干扰弹遮蔽效果 (烟幕区域放大)')
        else:
            ax.set_title('烟幕干扰弹对导弹M1的遮蔽效果分析（包含穿越检测）')
            
        ax.view_init(elev=20, azim=45)
        ax.set_xlabel('X (m)')
        ax.set_ylabel('Y (m)')
        ax.set_zlabel('Z (m)')
        ax.legend()
        ax.grid(True)

        plt.tight_layout()
        plt.show()

    return total_effective_time


def main():
    print("=== 问题1：计算烟幕干扰弹对导弹M1的有效遮蔽时间 ===\n")
    
    # 使用给定的参数
    effective_time = calculate_smoke_obscuration(
        drone_direction=[-1, 0, 0],  # 朝向假目标
        drone_speed=120,
        drop_time=1.5,
        blast_delay=3.6,
        visualize=True,
        verbose=True
    )
    
    print(f"\n最终结果：有效遮蔽时间为 {effective_time:.3f} 秒")


if __name__ == "__main__":
    main()
