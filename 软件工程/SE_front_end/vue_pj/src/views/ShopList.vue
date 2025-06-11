<template>
  <div class="shop-list-container">
    <div class="shop-list-header">
      <div class="header-left">
        <el-button 
          type="primary" 
          plain 
          icon="el-icon-arrow-left" 
          @click="goBack"
          class="back-button"
        >
          前往搜索
        </el-button>
      </div>
      <div class="header-center">
        <h1>美食商家列表</h1>
      </div>
      <div class="filter-controls">
        <el-select 
          v-model="pageSize" 
          placeholder="每页显示" 
          @change="handlePageSizeChange"
          popper-class="page-size-select"
        >
          <el-option :value="9" label="每页9条"></el-option>
          <el-option :value="12" label="每页12条"></el-option>
          <el-option :value="16" label="每页16条"></el-option>
          <el-option :value="24" label="每页24条"></el-option>
        </el-select>
      </div>
    </div>

    <!-- 加载状态显示 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 商家列表 -->
    <div v-else-if="shops.length > 0" class="shops-grid">
      <div 
        v-for="shopData in shops" 
        :key="getShopId(shopData)"
        class="shop-card"
        @click="viewShopDetails(getShopId(shopData))"
      >
        <div class="shop-image">
          <img :src="getShopImage(shopData)" alt="商家图片" @error="handleImageError">
          <div class="shop-category" v-if="getShopCategory(shopData)">
            {{ getShopCategory(shopData) }}
          </div>
        </div>
        <div class="shop-info">
          <h3 class="shop-name">{{ getShopName(shopData) }}</h3>
          <div class="shop-rating">
            <el-rate :model-value="getShopRating(shopData)" disabled text-color="#ff9900" />
            <span>{{ getShopRating(shopData) }}</span>
          </div>
          <div class="shop-price">
            <span>人均 ¥{{ getShopAverageCost(shopData) }}</span>
            <span class="separator">|</span>
            <span>¥{{ getShopPriceRange(shopData) }}</span>
          </div>
          <div class="shop-address">
            <i class="el-icon-location"></i>
            {{ getShopAddress(shopData) }}
          </div>
        </div>
      </div>
    </div>

    <!-- 无数据显示 -->
    <div v-else class="empty-state">
      <el-empty description="暂无商家数据"></el-empty>
    </div>

    <!-- 分页控件 -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @update:current-page="handleCurrentChange"
      ></el-pagination>
      <div class="pagination-info">
        共 {{ total }} 条记录，当前第 {{ currentPage }}/{{ Math.ceil(total/pageSize) }} 页
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import ShopService from '@/services/ShopService';

export default {
  name: 'ShopList',
  
  setup() {
    const router = useRouter();
    
    // 状态定义
    const shops = ref([]);
    const loading = ref(true);
    const currentPage = ref(1);
    const pageSize = ref(9);
    const total = ref(0);
    const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1);
    
    // 组件卸载标志
    const isUnmounted = ref(false);
    
    // 不同类型商家对应的图片
    const categoryImages = {
      '火锅': 'https://fuss10.elemecdn.com/a/3f/3302e58f9a181d2509f3dc0fa68b0jpeg.jpeg',
      '奶茶': 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
      '烧烤': 'https://shadow.elemecdn.com/app/element/hamburger.9cf7b091-55e9-11e9-a976-7f4d0b07eef6.png',
      '西餐': 'https://fuss10.elemecdn.com/1/34/19aa98b1fcb2781c4fba33d850549jpeg.jpeg',
      '中餐': 'https://fuss10.elemecdn.com/0/6f/e35ff375812e6b0020b6b4e8f9583jpeg.jpeg',
      '快餐': 'https://fuss10.elemecdn.com/9/bb/e27858e973f5d7d3904835f46abbdjpeg.jpeg',
      '甜品': 'https://fuss10.elemecdn.com/d/e6/c4d93a3805b3ce3f323f7974e6f78jpeg.jpeg',
      '小吃': 'https://fuss10.elemecdn.com/3/28/bbf893f792f03a54408b3b7a7ebf0jpeg.jpeg',
      // 默认图片
      'default': 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg'
    };
    
    // 加载商家列表
    const loadShopList = async () => {
      if (isUnmounted.value) return; // 如果组件已卸载，中止操作
      
      loading.value = true;
      try {
        console.log('开始请求商家列表数据，参数:', { pageCurrent: currentPage.value, pageSize: pageSize.value });
        
        // 主动获取所有商家，然后在前端进行分页处理
        const allShopsResponse = await ShopService.getShopList(1, 100); // 获取较大数量的商家
        
        // 检查组件是否已卸载
        if (isUnmounted.value) return;
        
        console.log('获取商家列表响应:', allShopsResponse);
        
        if (allShopsResponse.code === 1 && allShopsResponse.data) {
          let allShops = [];
          
          // 处理不同类型的响应数据结构
          if (Array.isArray(allShopsResponse.data)) {
            allShops = allShopsResponse.data;
          } else if (allShopsResponse.data.records) {
            allShops = allShopsResponse.data.records;
          } else if (typeof allShopsResponse.data === 'object') {
            // 尝试从返回对象中找到数组
            const arrayFields = Object.entries(allShopsResponse.data)
              .find(([_, value]) => Array.isArray(value) && value.length > 0);
            
            if (arrayFields) {
              allShops = arrayFields[1];
            } else {
              allShops = [allShopsResponse.data]; // 单个对象
            }
          }
          
          // 再次检查组件是否已卸载
          if (isUnmounted.value) return;
          
          console.log(`获取到总共 ${allShops.length} 条商家数据`);
          
          // 设置总记录数
          total.value = allShops.length;
          
          // 前端分页处理
          const startIndex = (currentPage.value - 1) * pageSize.value;
          const endIndex = startIndex + pageSize.value;
          shops.value = allShops.slice(startIndex, endIndex);
          
          console.log(`当前页 ${currentPage.value}，每页 ${pageSize.value} 条，显示第 ${startIndex+1} 到 ${Math.min(endIndex, total.value)} 条，共 ${total.value} 条`);
          
          // 检查是否需要调整当前页码
          const maxPage = Math.ceil(total.value / pageSize.value);
          if (currentPage.value > maxPage && maxPage > 0) {
            console.log(`当前页码 ${currentPage.value} 超出最大页数 ${maxPage}，自动调整为最后一页`);
            currentPage.value = maxPage;
            // 重新计算显示范围
            const newStartIndex = (currentPage.value - 1) * pageSize.value;
            const newEndIndex = newStartIndex + pageSize.value;
            shops.value = allShops.slice(newStartIndex, newEndIndex);
          }
        } else {
          // 检查组件是否已卸载
          if (isUnmounted.value) return;
          
          console.error('获取商家列表失败:', allShopsResponse.msg || '未知错误');
          shops.value = [];
          total.value = 0;
        }
      } catch (error) {
        // 检查组件是否已卸载
        if (isUnmounted.value) return;
        
        console.error('获取商家列表异常:', error);
        if (error.response) {
          console.error('服务器响应错误:', error.response.status, error.response.data);
        }
        shops.value = [];
        total.value = 0;
      } finally {
        // 检查组件是否已卸载
        if (!isUnmounted.value) {
          loading.value = false;
        }
      }
    };
    
    // 检查响应格式是否符合预期
    const checkResponseFormat = (response) => {
      if (!response) {
        console.error('响应为空');
        return;
      }
      
      if (typeof response !== 'object') {
        console.error(`响应不是对象，而是 ${typeof response}:`, response);
        return;
      }
      
      if (response.code !== 1) {
        console.error(`响应code不为1，而是 ${response.code}，消息:`, response.msg);
        return;
      }
      
      if (!response.data) {
        console.error('响应data为空');
        return;
      }
      
      if (!response.data.records) {
        console.error('响应data中没有records字段:', response.data);
        return;
      }
      
      if (!Array.isArray(response.data.records)) {
        console.error(`响应data.records不是数组，而是 ${typeof response.data.records}:`, response.data.records);
        return;
      }
      
      console.log(`响应格式正确，共有${response.data.records.length}条记录`);
    };
    
    // 页码变化处理
    const handleCurrentChange = (page) => {
      if (isUnmounted.value) return;
      
      console.log('页码变化，新页码:', page);
      currentPage.value = page;
      loadShopList();
      // 滚动到页面顶部
      window.scrollTo(0, 0);
    };
    
    // 每页数量变化处理
    const handlePageSizeChange = () => {
      if (isUnmounted.value) return;
      
      console.log('每页显示数量变化为:', pageSize.value);
      currentPage.value = 1; // 重置到第一页
      loadShopList();
    };
    
    // 查看商家详情
    const viewShopDetails = (shopId) => {
      if (isUnmounted.value) return;
      
      try {
        if (!shopId) {
          console.warn('无效的商家ID');
          return;
        }
        router.push({ name: 'ShopDetail', params: { id: shopId } });
      } catch (error) {
        console.error('导航到商家详情页失败:', error);
      }
    };
    
    // 获取商家ID，兼容不同数据结构
    const getShopId = (shopData) => {
      if (!shopData) return 0;
      return shopData.shop ? shopData.shop.id : shopData.id;
    };
    
    // 获取商家名称，兼容不同数据结构
    const getShopName = (shopData) => {
      if (!shopData) return '未命名商家';
      return shopData.shop ? shopData.shop.name : shopData.name || '未命名商家';
    };
    
    // 获取商家分类，兼容不同数据结构
    const getShopCategory = (shopData) => {
      if (!shopData) return null;
      if (shopData.shop) {
        return shopData.shop.categoryName || 
               (shopData.shop.category ? shopData.shop.category.name : '未分类');
      }
      return shopData.categoryName || 
             (shopData.category ? shopData.category.name : '未分类');
    };
    
    // 获取商家评分，兼容不同数据结构
    const getShopRating = (shopData) => {
      if (!shopData) return 0;
      if (shopData.shop) {
        return parseFloat(shopData.shop.rating) || 0;
      }
      return parseFloat(shopData.rating) || 0;
    };
    
    // 获取商家人均消费，兼容不同数据结构
    const getShopAverageCost = (shopData) => {
      if (!shopData) return '暂无';
      if (shopData.shop) {
        return shopData.shop.averageCost || '暂无';
      }
      return shopData.averageCost || '暂无';
    };
    
    // 获取商家价格区间，兼容不同数据结构
    const getShopPriceRange = (shopData) => {
      if (!shopData) return '0-0';
      if (shopData.shop) {
        return `${shopData.shop.priceMin || 0}-${shopData.shop.priceMax || 0}`;
      }
      return `${shopData.priceMin || 0}-${shopData.priceMax || 0}`;
    };
    
    // 获取商家地址，兼容不同数据结构
    const getShopAddress = (shopData) => {
      if (!shopData) return '暂无地址信息';
      if (shopData.shop) {
        return shopData.shop.address || '暂无地址信息';
      }
      return shopData.address || '暂无地址信息';
    };
    
    // 获取商家图片
    // ShopList.vue 中的 getShopImage 方法
    const getShopImage = (shopData) => {
      console.log('进入getShopImage方法，shopData:', shopData); // 打印整个shopData
      console.log('shopData是否包含images:', shopData.images);
      console.log('shopData.shop是否存在:', shopData.shop);
      console.log('shopData.shop是否包含images:', shopData.shop?.images);

      // 根据实际数据结构获取图片
      const images = shopData.images;

      console.log('获取到的images数组:', images);
      console.log('images是否为数组且非空:', Array.isArray(images) && images.length > 0);

      if (Array.isArray(images) && images.length > 0 && images[0].imageUrl) {
        console.log('使用后端图片:', `http://localhost:8088${images[0].imageUrl}`);
        return `http://localhost:8088${images[0].imageUrl}`;
      }

      // 根据分类返回默认图片
      const categoryName = getShopCategory(shopData);
      console.log('使用默认图片，分类:', categoryName);
      return categoryImages[categoryName] || categoryImages.default;
    };
    
    // 处理图片加载错误
    const handleImageError = (e) => {
      console.warn('图片加载失败，使用默认图片');
      e.target.src = categoryImages.default; // 使用默认图片
    };
    
    // 返回搜索页面
    const goBack = () => {
      router.push('/shop/search');
    };
    
    // 生命周期钩子
    onMounted(() => {
      loadShopList();
    });
    
    // 组件卸载钩子
    onUnmounted(() => {
      isUnmounted.value = true;
    });
    
    return {
      shops,
      loading,
      currentPage,
      pageSize,
      total,
      totalPages,
      handleCurrentChange,
      handlePageSizeChange,
      viewShopDetails,
      getShopImage,
      handleImageError,
      getShopId,
      getShopName,
      getShopCategory,
      getShopRating,
      getShopAverageCost,
      getShopPriceRange,
      getShopAddress,
      isUnmounted,
      goBack
    };
  }
};
</script>

<style scoped>
.shop-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.shop-list-header {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  position: relative;
}

.header-left {
  flex: 0 0 auto;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.shop-list-header h1 {
  margin: 0;
  font-size: 32px;
  color: #303133;
  font-weight: 700;
  background: linear-gradient(45deg, #409EFF, #468ae4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  letter-spacing: 2px;
  position: relative;
  padding-bottom: 10px;
}

.shop-list-header h1::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(45deg, #409EFF, #67C23A);
  border-radius: 2px;
}

.filter-controls {
  flex: 0 0 auto;
}

.back-button {
  margin-right: 20px;
}

.loading-state, .empty-state {
  padding: 50px 0;
  text-align: center;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.shops-grid {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
  justify-items: center;
}

.shop-card {
  width: 100%;
  max-width: 320px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  background-color: #fff;
  border: 1px solid #ebeef5;
}

.shop-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.shop-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.shop-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.shop-card:hover .shop-image img {
  transform: scale(1.08);
}

.shop-category {
  position: absolute;
  top: 12px;
  right: 12px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  backdrop-filter: blur(4px);
}

.shop-info {
  padding: 20px;
}

.shop-name {
  margin: 0 0 12px;
  font-size: 18px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 600;
}

.shop-rating {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.shop-rating span {
  margin-left: 8px;
  color: #ff9900;
  font-weight: 600;
}

.shop-price {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.separator {
  margin: 0 8px;
  color: #dcdfe6;
}

.shop-address {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
}

.shop-address i {
  margin-right: 6px;
  font-size: 16px;
}

.pagination-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 30px;
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.pagination-info {
  margin-top: 15px;
  color: #909399;
  font-size: 14px;
}

:deep(.page-size-select) {
  margin-top: 10px !important;
}

:deep(.el-select-dropdown) {
  margin-top: 10px !important;
}
</style> 