import csv
import os
from random import randint, choice
from typing import List

# -------------------------- 核心配置 --------------------------
ROOT_OUTPUT_DIR = "shanghai_test_cases/"
CASE_CONFIGS = {
    "case1_simple": {
        "demand_file": "demand.txt",
        "timestamps": ["1200"],
        "road_count_range": (30, 35),  # 道路数量
        "has_traffic_light": False,
        "desc": "简单Case：1个时间点，无红绿灯，多道路，核心区多路径可选"
    },
    "case2_medium": {
        "demand_file": "demand.txt",
        "timestamps": ["0800", "1400"],
        "road_count_range": (40, 45),  # 道路数量
        "has_traffic_light": False,
        "desc": "中等Case：2个时间点，无红绿灯，多道路，核心+边缘全覆盖"
    },
    "case3_complex": {
        "demand_file": "demand.txt",
        "timestamps": ["0800", "1400", "1830"],
        "road_count_range": (50, 55),
        "has_traffic_light": False,
        "desc": "复杂Case：3个时间点，无红绿灯，多道路，核心+边缘+远郊全连通"
    }
}

SHANGHAI_SPOTS = {
    "Yangpu_Edu": ["复旦大学（江湾校区）", "复旦大学（邯郸校区）", "五角场"],
    "Puxi_Core": ["人民广场", "外滩", "豫园·城隍庙", "静安寺", "上海博物馆", 
                 "田子坊", "上海南京路步行街", "中山公园"],
    "Pudong_Core": ["东方明珠", "上海科技馆", "上海世纪公园", "上海迪士尼乐园"],
    "Pudong_Suburb": ["上海野生动物园"],
    "Other_Suburb": ["上海东林寺"]
}

# 使用固定的道路间距映射，确保更真实的路径规划
SPOT_DISTANCE_REF = {
    ("复旦大学（江湾校区）", "复旦大学（邯郸校区）"): 5,
    ("复旦大学（邯郸校区）", "五角场"): 2,
    ("五角场", "中山公园"): 8,
    ("复旦大学（邯郸校区）", "人民广场"): 10,
    ("人民广场", "外滩"): 2,
    ("人民广场", "静安寺"): 3,
    ("人民广场", "上海博物馆"): 1,
    ("人民广场", "上海南京路步行街"): 1,
    ("外滩", "豫园·城隍庙"): 3,
    ("静安寺", "中山公园"): 4,
    ("田子坊", "豫园·城隍庙"): 2,
    ("田子坊", "外滩"): 3,
    ("上海南京路步行街", "外滩"): 1,
    ("外滩", "东方明珠"): 2,
    ("人民广场", "上海科技馆"): 6,
    ("人民广场", "上海世纪公园"): 7,
    ("五角场", "上海科技馆"): 12,
    ("东方明珠", "上海科技馆"): 3,
    ("上海科技馆", "上海世纪公园"): 2,
    ("上海世纪公园", "上海迪士尼乐园"): 15,
    ("上海迪士尼乐园", "上海野生动物园"): 10,
    ("人民广场", "上海东林寺"): 60,
    ("上海野生动物园", "上海东林寺"): 55,
    ("上海迪士尼乐园", "上海东林寺"): 50
}

TRAFFIC_CONFIG = {
    "0800": {"base_car": 18, "light_delay": 1.6},
    "1400": {"base_car": 6, "light_delay": 1.0},
    "1830": {"base_car": 20, "light_delay": 1.8},
    "1200": {"base_car": 8, "light_delay": 1.0}
}

LANE_OPTIONS = {
    "Core": [3, 4, 5],  # 增加更多车道选项
    "Suburb_Main": [2, 3, 4],
    "Suburb_Minor": [2, 3]
}

SPEED_LIMIT = {
    "Core": 60,
    "Suburb_Main": 80,
    "Suburb_Minor": 70
}


class RoadBaseData:
    """道路基础数据类（无红绿灯属性）"""
    def __init__(self, road_id: str, start: str, end: str, length: int, lanes: int, speed: int):
        self.road_id = road_id
        self.start = start
        self.end = end
        self.length = length  # 单位：米
        self.lanes = lanes
        self.speed = speed    # 单位：km/h


# 路径连通性检查函数（保留原逻辑，确保连通）
def is_route_connected(start: str, end: str, base_roads: List[RoadBaseData], case_name: str) -> bool:
    visited = set()
    queue = [start]
    visited.add(start)

    while queue:
        current_spot = queue.pop(0)
        if current_spot == end:
            return True
        
        for road in base_roads:
            # 复用道路方向逻辑，确保一致性
            if case_name == "case1_simple":
                road_dir = "双向" if randint(0, 10) > 2 else "单向"
            elif case_name == "case3_complex":
                road_dir = "单向" if randint(0, 10) > 3 else "双向"
            else:
                road_dir = "单向" if randint(0, 10) > 5 else "双向"
            
            # 单向道路：start→end
            if road_dir == "单向" and road.start == current_spot and road.end not in visited:
                visited.add(road.end)
                queue.append(road.end)
            # 双向道路：双向通行
            elif road_dir == "双向":
                if road.start == current_spot and road.end not in visited:
                    visited.add(road.end)
                    queue.append(road.end)
                if road.end == current_spot and road.start not in visited:
                    visited.add(road.start)
                    queue.append(road.start)
    
    return False


def init_root_dir() -> None:
    """初始化根输出文件夹及3个Case子文件夹"""
    if not os.path.exists(ROOT_OUTPUT_DIR):
        os.makedirs(ROOT_OUTPUT_DIR)
        print(f"已创建根输出文件夹：{ROOT_OUTPUT_DIR}")
    for case_name in CASE_CONFIGS.keys():
        case_dir = os.path.join(ROOT_OUTPUT_DIR, case_name)
        if not os.path.exists(case_dir):
            os.makedirs(case_dir)
            print(f"  - 已创建{case_name}子文件夹")


def get_spot_area(spot: str) -> str:
    """获取景点所属地理区域"""
    for area, spots in SHANGHAI_SPOTS.items():
        if spot in spots:
            return area
    return "Suburb_Minor"


def get_realistic_distance(start: str, end: str, case_name: str) -> int:
    """基于Case复杂度生成符合现实比例的道路长度"""
    if (start, end) in SPOT_DISTANCE_REF:
        km = SPOT_DISTANCE_REF[(start, end)]
    elif (end, start) in SPOT_DISTANCE_REF:
        km = SPOT_DISTANCE_REF[(end, start)]
    else:
        start_area = get_spot_area(start)
        end_area = get_spot_area(end)
        if case_name == "case1_simple":
            km = randint(3, 10)
        elif case_name == "case3_complex":
            km = randint(45, 60) if (start_area == "Other_Suburb" or end_area == "Other_Suburb") else randint(3, 20)
        else:
            km = randint(3, 15)
    meter = km * 1000
    return int(meter * (0.9 + randint(0, 20) / 100))


def get_road_config(start_area: str, end_area: str, case_name: str) -> tuple:
    """基于Case复杂度和区域获取道路配置（车道数、限速）"""
    if start_area in ["Puxi_Core", "Yangpu_Edu", "Pudong_Core"] and end_area in ["Puxi_Core", "Yangpu_Edu", "Pudong_Core"]:
        if case_name == "case1_simple":
            lanes = 3 if randint(0, 10) > 2 else 4
        elif case_name == "case3_complex":
            lanes = 4 if randint(0, 10) > 4 else 3
        else:
            lanes = choice(LANE_OPTIONS["Core"])
        speed = SPEED_LIMIT["Core"]
    elif (start_area in ["Puxi_Core", "Yangpu_Edu", "Pudong_Core"]) and (end_area in ["Pudong_Suburb"]):
        lanes = choice(LANE_OPTIONS["Suburb_Main"])
        speed = SPEED_LIMIT["Suburb_Main"]
    else:
        lanes = choice(LANE_OPTIONS["Suburb_Minor"])
        speed = SPEED_LIMIT["Suburb_Minor"]
    return lanes, speed


def generate_all_spots(case_name: str) -> List[str]:
    """基于Case复杂度筛选景点"""
    all_spots = []
    for area, spots in SHANGHAI_SPOTS.items():
        if case_name == "case1_simple" and area == "Other_Suburb":
            continue
        all_spots.extend(spots)
    return all_spots


def generate_demand_file(case_name: str, start: str, end: str) -> None:
    """生成对应Case的客户需求TXT"""
    case_config = CASE_CONFIGS[case_name]
    case_dir = os.path.join(ROOT_OUTPUT_DIR, case_name)
    demand_path = os.path.join(case_dir, case_config["demand_file"])
    with open(demand_path, "w", encoding="utf-8") as f:
        f.write(f"起点：{start}\n")
        f.write(f"终点：{end}\n")
        f.write(f"说明：{case_config['desc']}\n")
    print(f"✅ {case_name} - 生成需求文件：{os.path.basename(demand_path)}")


def generate_base_roads(case_name: str, spots: List[str]) -> List[RoadBaseData]:
    """生成道路基础数据（优化：新增边缘景点道路优先生成逻辑）"""
    case_config = CASE_CONFIGS[case_name]
    base_roads = []
    road_set = set()
    road_id = 1
    road_count = randint(*case_config["road_count_range"])
    has_long_road = False

    # -------------------------- 优先生成边缘景点之间的道路（30%） --------------------------
    # 定义核心景点（高频重复区）和边缘景点（易被忽略区）
    core_spots = ["人民广场", "外滩", "东方明珠", "五角场", "静安寺", "上海南京路步行街"]
    edge_spots = [s for s in spots if s not in core_spots]  # 边缘景点：如科技馆、迪士尼、野生动物园等
    edge_road_target = int(road_count * 0.3)  # 边缘道路占比30%，避免核心区过度重复

    # 生成边缘景点之间的直接道路（如“上海科技馆→上海迪士尼”“迪士尼→野生动物园”）
    while len(base_roads) < edge_road_target and len(edge_spots) >= 2:
        start = choice(edge_spots)
        end = choice(edge_spots)
        if start == end or f"{start}-{end}" in road_set:
            continue
        
        # 生成边缘道路属性
        start_area = get_spot_area(start)
        end_area = get_spot_area(end)
        length = get_realistic_distance(start, end, case_name)
        lanes, speed = get_road_config(start_area, end_area, case_name)
        
        base_roads.append(RoadBaseData(
            road_id=f"SH{road_id:02d}",
            start=start,
            end=end,
            length=length,
            lanes=lanes,
            speed=speed
        ))
        road_set.add(f"{start}-{end}")
        road_id += 1

    # -------------------------- 生成剩余道路（核心+边缘混合，70%） --------------------------
    while len(base_roads) < road_count:
        start = choice(spots)
        end = choice(spots)
        if start == end or f"{start}-{end}" in road_set:
            continue
        
        # 复杂Case：确保包含至少1条远郊长路径（原逻辑保留）
        if case_name == "case3_complex" and not has_long_road:
            long_spot = "上海东林寺"
            if (start == long_spot or end == long_spot):
                has_long_road = True
        
        # 生成道路属性
        start_area = get_spot_area(start)
        end_area = get_spot_area(end)
        length = get_realistic_distance(start, end, case_name)
        lanes, speed = get_road_config(start_area, end_area, case_name)
        
        base_roads.append(RoadBaseData(
            road_id=f"SH{road_id:02d}",
            start=start,
            end=end,
            length=length,
            lanes=lanes,
            speed=speed
        ))
        road_set.add(f"{start}-{end}")
        road_id += 1

    # -------------------------- 防孤立逻辑（确保所有景点有连接） --------------------------
    connected_spots = set()
    for road in base_roads:
        connected_spots.add(road.start)
        connected_spots.add(road.end)
    missing_spots = [spot for spot in spots if spot not in connected_spots]
    for spot in missing_spots:
        core_spot = choice(["人民广场", "外滩", "东方明珠", "五角场"])
        road_key = f"{spot}-{core_spot}"
        if road_key not in road_set:
            length = get_realistic_distance(spot, core_spot, case_name)
            start_area = get_spot_area(spot)
            end_area = get_spot_area(core_spot)
            lanes, speed = get_road_config(start_area, end_area, case_name)
            base_roads.append(RoadBaseData(
                road_id=f"SH{road_id:02d}",
                start=spot,
                end=core_spot,
                length=length,
                lanes=lanes,
                speed=speed
            ))
            road_set.add(road_key)
            road_id += 1

    print(f"✅ {case_name} - 生成{len(base_roads)}条道路（含{edge_road_target}条边缘景点道路）")
    return base_roads


def generate_map_csv(case_name: str, timestamp: str, base_roads: List[RoadBaseData]) -> None:
    """生成对应Case、对应时间戳的地图CSV（无红绿灯）"""
    case_dir = os.path.join(ROOT_OUTPUT_DIR, case_name)
    csv_path = os.path.join(case_dir, f"map_{timestamp}.csv")
    traffic = TRAFFIC_CONFIG[timestamp]
    base_car = traffic["base_car"]

    with open(csv_path, "w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=[
            "道路ID", "起始地点", "目标地点", "道路方向", "道路长度(米)",
            "道路限速(km/h)", "车道数", "现有车辆数"
        ])
        writer.writeheader()

        for road in base_roads:
            # 计算现有车辆数
            lane_compensation = {2: 0, 3: 2, 4: 4}.get(road.lanes, 0)
            car_count = base_car + randint(-4, 4) + lane_compensation
            car_count = max(1, min(car_count, road.lanes * 8))

            # 确定道路方向（原逻辑保留）
            if case_name == "case1_simple":
                road_direction = "双向" if randint(0, 10) > 2 else "单向"
            elif case_name == "case3_complex":
                road_direction = "单向" if randint(0, 10) > 3 else "双向"
            else:
                road_direction = "单向" if randint(0, 10) > 5 else "双向"

            # 写入道路数据
            writer.writerow({
                "道路ID": road.road_id,
                "起始地点": road.start,
                "目标地点": road.end,
                "道路方向": road_direction,
                "道路长度(米)": road.length,
                "道路限速(km/h)": road.speed,
                "车道数": road.lanes,
                "现有车辆数": car_count
            })

    print(f"✅ {case_name} - 生成时间戳[{timestamp}]的地图CSV")


def generate_case(case_name: str) -> None:
    """生成单个Case的完整测试样例（保留连通性校验）"""
    print(f"\n===== 开始生成 {case_name} =====")
    
    # 1. 获取当前Case的景点列表
    spots = generate_all_spots(case_name)
    print(f"📌 {case_name} - 包含景点数：{len(spots)}")
    
    # 2. 先生成道路数据（优化后：含更多边缘道路）
    base_roads = generate_base_roads(case_name, spots)
    
    # 3. 选择连通的起点终点（保留校验逻辑）
    start_spot = choice(spots)
    end_spot = choice(spots)
    max_attempts = 200
    attempts = 0
    while (start_spot == end_spot or not is_route_connected(start_spot, end_spot, base_roads, case_name)) and attempts < max_attempts:
        start_spot = choice(spots)
        end_spot = choice(spots)
        attempts += 1
    
    if attempts >= max_attempts:
        raise Exception(f"❌ {case_name} 尝试{max_attempts}次仍未找到连通的起点终点，请检查道路数量或方向配置")
    
    print(f"📌 {case_name} - 需求：{start_spot} → {end_spot}（尝试{attempts+1}次找到连通路径）")
    
    # 4. 生成需求文件
    generate_demand_file(case_name, start_spot, end_spot)
    
    # 5. 生成所有时间戳的地图CSV
    for timestamp in CASE_CONFIGS[case_name]["timestamps"]:
        generate_map_csv(case_name, timestamp, base_roads)
    
    print(f"===== {case_name} 生成完成 =====")


def main():
    """主函数：生成所有3组Case"""
    init_root_dir()
    for case_name in CASE_CONFIGS.keys():
        generate_case(case_name)
    print(f"\n🎉 所有测试样例生成完成！")
    print(f"📂 样例根目录：{os.path.abspath(ROOT_OUTPUT_DIR)}")
    print(f"📝 包含 {len(CASE_CONFIGS)} 组Case，每组道路更丰富且确保起点到终点连通")


if __name__ == "__main__":
    main()
