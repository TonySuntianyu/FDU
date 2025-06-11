<template>
  <div class="shop-search-container">
    <div class="search-bar">
      <div class="search-input">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索商家名称、分类等" 
          @keyup.enter="searchShops"
          @focus="handleSearchFocus"
          @blur="handleSearchBlur"
          @input="handleSearchInput"
          @clear="handleClear"
          clearable>
          <template #suffix>
            <el-button :icon="Search" circle @click="searchShops"></el-button>
          </template>
        </el-input>
      </div>
      
      <!-- 搜索历史记录 -->
      <div v-if="showSearchHistory && searchHistory.length > 0" class="search-history">
        <div class="history-header">
          <span>搜索历史</span>
          <div class="history-actions">
            <el-button type="text" @click="toggleHistoryExpand">
              <el-icon><component :is="historyExpanded ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
              {{ historyExpanded ? '收起' : '展开' }}
            </el-button>
            <el-button type="text" @click="clearSearchHistory">
              <el-icon><Delete /></el-icon>
              清空
            </el-button>
          </div>
        </div>
        <div class="history-list" v-show="historyExpanded">
          <el-tag
            v-for="item in searchHistory"
            :key="item.id"
            class="history-item"
            @click="handleHistoryClick(item.keyword)"
            @mousedown.prevent
          >
            <el-icon><Clock /></el-icon>
            {{ item.keyword }}
          </el-tag>
        </div>
      </div>
      
      <!-- 相似关键词提示 -->
      <div v-if="showSimilarKeywords && similarKeywords.length > 0" class="similar-keywords">
        <div class="similar-header">
          <span>您是否想搜：</span>
        </div>
        <div class="similar-list">
          <el-tag
            v-for="keyword in similarKeywords"
            :key="keyword"
            class="similar-item"
            @click="useSimilarKeyword(keyword)"
            @mousedown.prevent
          >
            {{ keyword }}
          </el-tag>
        </div>
      </div>
      
      <!-- 加载中状态 -->
      <div v-if="isLoading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        加载中...
      </div>
    </div>

    <!-- 搜索结果统计 -->
    <div class="search-summary" v-if="!loading && searchQuery.trim() !== ''">
      <div class="result-count">
        找到 <strong>{{ total }}</strong> 条结果，共 <strong>{{ totalPages }}</strong> 页
      </div>
      <div class="active-filters">
        <span v-if="selectedRating">评分：{{ selectedRating }}分以上</span>
        <span v-if="selectedPrice">价格：{{ formatPriceRange(selectedPrice) }}</span>
        <span v-if="selectedAverageCost">人均：{{ formatPriceRange(selectedAverageCost) }}/人</span>
        <span v-if="sortBy !== 'default'">
          排序：{{ 
            sortBy === 'rating_desc' ? '评分最高' : 
            sortBy === 'average_cost_asc' ? '人均消费最低' : '综合排序' 
          }}
        </span>
      </div>
    </div>

    <!-- 筛选面板 -->
    <div class="filter-panel">
      <div class="filter-section">
        <div class="filter-title">商家评分</div>
        <div class="filter-options">
          <el-radio-group v-model="selectedRating" @change="applyFilter">
            <el-radio value="">全部</el-radio>
            <el-radio value="4.5">4.5分以上</el-radio>
            <el-radio value="4.0">4.0分以上</el-radio>
            <el-radio value="3.5">3.5分以上</el-radio>
          </el-radio-group>
        </div>
      </div>
      
      <div class="filter-section">
        <div class="filter-title">价格区间</div>
        <div class="filter-options">
          <el-radio-group v-model="selectedPrice" @change="applyFilter">
            <el-radio value="">全部</el-radio>
            <el-radio value="0-50">￥0-50</el-radio>
            <el-radio value="50-100">￥50-100</el-radio>
            <el-radio value="100-200">￥100-200</el-radio>
            <el-radio value="200+">￥200以上</el-radio>
          </el-radio-group>
        </div>
      </div>
      
      <div class="filter-section">
        <div class="filter-title">人均消费</div>
        <div class="filter-options">
          <el-radio-group v-model="selectedAverageCost" @change="applyFilter">
            <el-radio value="">全部</el-radio>
            <el-radio value="0-50">￥0-50/人</el-radio>
            <el-radio value="50-100">￥50-100/人</el-radio>
            <el-radio value="100-200">￥100-200/人</el-radio>
            <el-radio value="200+">￥200以上/人</el-radio>
          </el-radio-group>
        </div>
      </div>
      
      <div class="filter-section">
        <div class="filter-title">排序方式</div>
        <div class="filter-options">
          <el-radio-group v-model="sortBy" @change="applyFilter">
            <el-radio value="default">综合排序</el-radio>
            <el-radio value="rating_desc">评分最高</el-radio>
            <el-radio value="average_cost_asc">人均消费最低</el-radio>
          </el-radio-group>
        </div>
      </div>
      
      <div class="filter-actions">
        <div class="filter-actions-left">
          <div class="filter-title">每页显示</div>
          <el-select v-model="pageSize" @change="handlePageSizeChange" size="small" placeholder="每页显示">
            <el-option :value="6" label="6条/页"></el-option>
            <el-option :value="9" label="9条/页"></el-option>
            <el-option :value="12" label="12条/页"></el-option>
            <el-option :value="18" label="18条/页"></el-option>
          </el-select>
        </div>
        <div>
          <el-button type="primary" @click="resetFilters">重置筛选条件</el-button>
        </div>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div class="search-results">
      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="errorMessage" class="no-results error-message">
        <el-alert
          :title="errorMessage"
          type="error"
          show-icon
          :closable="false"
        />
        <el-button class="retry-button" type="primary" size="small" @click="resetAndSearch">
          重置筛选条件并重试
        </el-button>
      </div>
      <div v-else-if="total === 0" class="no-results">
        <el-empty 
          description=""
          :image-size="180">
          <template #description>
            <div class="empty-result-message">
              <h3>没有找到符合条件的商家</h3>
              <p class="empty-result-tips">可能原因：</p>
              <ul class="empty-result-tips-list">
                <li v-if="selectedPrice.value && selectedPrice.value.includes('+')">您选择的"价格区间 ￥{{ selectedPrice.value.replace('+', '以上') }}"目前没有匹配的商家</li>
                <li v-else-if="selectedPrice.value">您选择的"价格区间 ￥{{ selectedPrice.value }}"目前没有匹配的商家</li>
                <li v-if="selectedAverageCost.value && selectedAverageCost.value.includes('+')">您选择的"人均消费 ￥{{ selectedAverageCost.value.replace('+', '以上') }}"目前没有匹配的商家</li>
                <li v-else-if="selectedAverageCost.value">您选择的"人均消费 ￥{{ selectedAverageCost.value }}"目前没有匹配的商家</li>
                <li v-if="selectedRating.value">您选择的"{{ selectedRating.value }}分以上"的商家目前没有符合条件的</li>
                <li>尝试调整筛选条件或清空搜索关键词</li>
              </ul>
              <el-button type="primary" @click="resetFilters" class="empty-reset-btn">
                重置筛选条件
              </el-button>
            </div>
          </template>
        </el-empty>
      </div>
      <div v-else class="shop-list">
        <div 
          v-for="shopData in shops" 
          :key="shopData.shop ? shopData.shop.id : shopData.id" 
          class="shop-card" 
          @click="viewShopDetails(shopData)">
          <div class="shop-avatar">
            <img :src="getShopImage(shopData)" alt="店铺图片" />
          </div>
          <div class="shop-info">
            <h3 class="shop-name">{{ (shopData.shop ? shopData.shop.name : shopData.name) || '未命名商家' }}</h3>
            <div class="shop-rating">
              <el-rate 
                :model-value="(shopData.shop ? shopData.shop.rating : shopData.rating) || 0" 
                disabled 
                text-color="#ff9900" />
              <span>{{ (shopData.shop ? shopData.shop.rating : shopData.rating) || '暂无评分' }}</span>
            </div>
            <div class="shop-meta">
              <el-tag size="small" type="success" v-if="shopData.shop && shopData.shop.categoryName">{{ shopData.shop.categoryName }}</el-tag>
              <el-tag size="small" type="success" v-else-if="shopData.categoryName">{{ shopData.categoryName }}</el-tag>
              <el-tag size="small" type="warning">人均￥{{ (shopData.shop ? shopData.shop.averageCost : shopData.averageCost) || '未知' }}</el-tag>
              <el-tag size="small" type="info">￥{{ (shopData.shop ? shopData.shop.priceMin : shopData.priceMin) || '0' }}-￥{{ (shopData.shop ? shopData.shop.priceMax : shopData.priceMax) || '0' }}</el-tag>
            </div>
            <div class="shop-address"><i class="el-icon-location"></i> {{ (shopData.shop ? shopData.shop.address : shopData.address) || '暂无地址信息' }}</div>
            <div class="shop-hours"><i class="el-icon-time"></i> {{ (shopData.shop ? shopData.shop.businessHours : shopData.businessHours) || '暂无营业时间信息' }}</div>
            <div class="shop-description" v-if="shopData.shop && shopData.shop.description">{{ shopData.shop.description }}</div>
            <div class="shop-description" v-else-if="shopData.description">{{ shopData.description }}</div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 分页控件 -->
    <div class="pagination-container" v-if="!loading && !errorMessage">
      <div v-if="total === 0" class="pagination-empty-message">
        <div class="pagination-info">
          很抱歉，当前没有符合条件的商家数据
        </div>
      </div>
      <template v-else>
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @update:current-page="handlePageChange"
        ></el-pagination>
        <div class="pagination-info">
          共 {{ total }} 条记录，当前第 {{ currentPage }}/{{ totalPages }} 页
        </div>
      </template>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch, computed, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { Search, Delete, Clock, Loading, ArrowDown, ArrowUp } from '@element-plus/icons-vue';
import ShopService from '@/services/ShopService';
import AuthService from '@/services/AuthService';
import { useStore } from 'vuex';

export default {
  name: 'ShopSearch',
  
  components: {
    Search,
    Delete,
    Clock,
    Loading,
    ArrowDown,
    ArrowUp
  },
  
  setup() {
    const router = useRouter();
    const store = useStore();
    
    // 搜索相关
    const searchQuery = ref('');
    const shops = ref([]);
    const loading = ref(false);
    const showSearchHistory = ref(false);
    const searchHistory = ref([]);
    const isLoading = ref(false);
    const historyExpanded = ref(true); // 默认展开搜索历史
    const defaultImage = 'https://shadow.elemecdn.com/app/element/hamburger.9cf7b091-55e9-11e9-a976-7f4d0b07eef6.png';
    
    // 相似关键词相关
    const showSimilarKeywords = ref(false);
    const similarKeywords = ref([]);
    
    // 缓存所有商家数据，避免频繁请求
    const allShopsCache = ref([]);
    
    // 存储原始缓存数据
    const originalShopsCache = ref([]);
    
    // 筛选相关
    const selectedRatings = ref([]);
    const selectedPrices = ref([]);
    const selectedAverageCosts = ref([]);
    const selectedRating = ref('');
    const selectedPrice = ref('');
    const selectedAverageCost = ref('');
    const sortBy = ref('default');
    
    // 分页相关
    const currentPage = ref(1);
    const pageSize = ref(6);
    const total = ref(0);
    const totalPages = computed(() => {
      const pages = Math.ceil(total.value / pageSize.value);
      return pages > 0 ? pages : 1;
    });
    
    // 组件卸载标志
    const isUnmounted = ref(false);
    
    // 用户信息 - 使用计算属性从Vuex中获取
    const user = computed(() => store.getters['auth/currentUser']);
    const userId = computed(() => {
      const currentUser = user.value;
      if (!currentUser) {
        return null;
      }
      
      // 如果用户对象没有id属性，尝试获取完整的用户信息
      if (!currentUser.id) {
        // 使用AuthService获取完整用户信息
        const fullUserInfo = AuthService.getUser();
        if (fullUserInfo && fullUserInfo.id) {
          return fullUserInfo.id;
        }
        return 1;
      }
      
      // 确保userId是数字类型
      const idValue = Number(currentUser.id);
      
      return idValue;
    });
    
    // 在setup中添加错误消息状态
    const errorMessage = ref('');
    
    // 监听搜索框内容变化
    watch(searchQuery, (newValue) => {
      if (!newValue || newValue.trim() === '') {
        if (userId.value && document.activeElement === document.querySelector('.search-input .el-input__inner')) {
          showSearchHistory.value = true;
          loadSearchHistory();
        }
      } else {
        showSimilarKeywords.value = false;
      }
    });
    
    // 页面加载时获取所有商家和搜索历史
    onMounted(() => {
      loadShops();
      loadSearchHistory();
    });
    
    // 组件卸载钩子
    onUnmounted(() => {
      isUnmounted.value = true;
      console.log('ShopSearch组件已卸载');
    });
    
    // 加载商家列表
    const loadShops = async () => {
      if (isUnmounted.value) return;
      
      loading.value = true;
      try {
        // // 如果已有全部商家数据缓存，直接使用缓存数据
        // if (originalShopsCache.value.length > 0) {
        //   allShopsCache.value = [...originalShopsCache.value];
        //   total.value = allShopsCache.value.length;
        //   updatePagedShops();
        //   loading.value = false;
        //   return;
        // }
        
        // 获取较大数量的商家数据
        const response = await ShopService.getShops(1, 100);
        
        if (isUnmounted.value) return;
        
        if (response.code === 1 && response.data) {
          // 处理不同类型的响应数据结构
          if (Array.isArray(response.data)) {
            originalShopsCache.value = response.data;
            allShopsCache.value = [...originalShopsCache.value];
          } else if (response.data.records) {
            originalShopsCache.value = response.data.records;
            allShopsCache.value = [...originalShopsCache.value];
          } else if (typeof response.data === 'object') {
            // 尝试从返回对象中找到数组
            const arrayFields = Object.entries(response.data)
              .find(([_, value]) => Array.isArray(value) && value.length > 0);
            
            if (arrayFields) {
              originalShopsCache.value = arrayFields[1];
              allShopsCache.value = [...originalShopsCache.value];
            } else {
              originalShopsCache.value = [response.data]; // 单个对象
              allShopsCache.value = [...originalShopsCache.value];
            }
          }
          
          // 设置总数据并进行前端分页
          if (allShopsCache.value.length > 0) {
            total.value = allShopsCache.value.length;
            
            // 前端分页
            updatePagedShops();
          } else {
            shops.value = [];
            total.value = 0;
          }
        } else {
          shops.value = [];
          total.value = 0;
        }
      } catch (error) {
        if (isUnmounted.value) return;
        
        shops.value = [];
        total.value = 0;
      } finally {
        if (!isUnmounted.value) {
          loading.value = false;
        }
      }
    };
    
    // 根据当前页码和每页数量更新显示的商家数据
    const updatePagedShops = () => {
      if (allShopsCache.value.length === 0) return;
      
      console.log('更新分页数据 - 当前页:', currentPage.value, '每页显示:', pageSize.value, '总条数:', total.value);
      
      const startIndex = (currentPage.value - 1) * pageSize.value;
      const endIndex = startIndex + pageSize.value;
      
      // 确保startIndex在有效范围内
      if (startIndex >= allShopsCache.value.length) {
        // 如果超出范围，调整到最后一页
        const maxPage = Math.max(1, Math.ceil(allShopsCache.value.length / pageSize.value));
        console.log('页码超出范围，调整为最后一页:', maxPage);
        currentPage.value = maxPage;
        // 重新计算索引
        const newStartIndex = (currentPage.value - 1) * pageSize.value;
        shops.value = allShopsCache.value.slice(newStartIndex, newStartIndex + pageSize.value);
      } else {
        shops.value = allShopsCache.value.slice(startIndex, endIndex);
        console.log('当前页显示商家数:', shops.value.length, '(', startIndex, '-', Math.min(endIndex, allShopsCache.value.length)-1, ')');
      }
    };
    
    // 加载搜索历史
    const loadSearchHistory = async () => {
      try {
        // 使用计算属性获取用户ID
        const currentUserId = userId.value;
        
        if (!currentUserId) {
          showSearchHistory.value = false;
          return;
        }

        isLoading.value = true;
        
        const response = await ShopService.getSearchHistory(currentUserId);

        if (response.code === 1 && Array.isArray(response.data)) {
          searchHistory.value = response.data
            .sort((a, b) => new Date(b.searchTime) - new Date(a.searchTime))
            .slice(0, 10);
          showSearchHistory.value = searchHistory.value.length > 0;
        } else {
          searchHistory.value = [];
          showSearchHistory.value = false;
        }
      } catch (error) {
        searchHistory.value = [];
        showSearchHistory.value = false;
      } finally {
        isLoading.value = false;
      }
    };
    
    // 搜索商家
    const searchShops = async () => {
      if (isUnmounted.value) return; // 如果组件已卸载，中止操作
      
      errorMessage.value = ''; // 清空之前的错误
      
      if (!searchQuery.value.trim()) {
        // 重置筛选条件（可选：根据需求决定是否保留筛选，这里假设清空筛选）
        resetFilters(); // 调用重置筛选函数
        loadShops();
        return;
      }
      
      loading.value = true;
      showSearchHistory.value = false;
      
      try {
        // 基础搜索参数 - 确保每次创建新对象，避免参数残留
        const queryParams = {
          name: searchQuery.value,
          pageSize: 100, // 获取更多数据，在前端分页
          pageCurrent: 1, // 从第一页开始获取
          sortBy: sortBy.value
        };
        
        // 只有在用户已登录时才添加userId
        if (userId.value) {
          // 确保userId是数值类型
          const userIdValue = userId.value;
          
          // 尝试将userId转换为数值类型
          try {
            queryParams.userId = Number(userIdValue);
          } catch (e) {
            // 如果转换失败，使用原始值
            queryParams.userId = userIdValue;
          }
        }
        
        // 添加评分筛选
        if (selectedRating.value) {
          queryParams.minRating = Number(selectedRating.value);
        }
        
        // 调用API进行搜索
        const response = await ShopService.searchShops(queryParams);
        
        // 检查组件是否已卸载
        if (isUnmounted.value) return;
        
        if (response.code === 0) {
          // 处理API返回的错误
          errorMessage.value = response.msg || '搜索时出现错误，请稍后重试';
          allShopsCache.value = [];
          shops.value = [];
          total.value = 0;
        } else if (response && response.code === 1) {
          // 重置当前页为第一页
          currentPage.value = 1;
          
          // 解析API返回的商家数据
          if (response.data && Array.isArray(response.data)) {
            // 直接返回数组的情况
            allShopsCache.value = response.data;
            originalShopsCache.value = [...response.data]; // 保存原始数据
            total.value = response.data.length;
          } else if (response.data && response.data.records) {
            // 返回包含records字段的分页对象
            allShopsCache.value = response.data.records;
            originalShopsCache.value = [...response.data.records]; // 保存原始数据
            total.value = response.data.total || response.data.records.length;
          } else if (response.data) {
            // 其他情况，尝试适配
            allShopsCache.value = response.data || [];
            originalShopsCache.value = [...allShopsCache.value]; // 保存原始数据
            total.value = allShopsCache.value.length;
          } else {
            allShopsCache.value = [];
            shops.value = [];
            total.value = 0;
          }
          
          // 搜索成功后，应用当前的筛选条件
          if (allShopsCache.value.length > 0 && (selectedPrice.value || selectedAverageCost.value || selectedRating.value)) {
            console.log('搜索成功，应用当前筛选条件');
            // 使用前端筛选逻辑筛选搜索结果
            const filteredData = filterCachedShops();
            allShopsCache.value = filteredData;
            total.value = filteredData.length;
          } else {
            // 没有筛选条件，保存原始结果总数
            total.value = allShopsCache.value.length;
            console.log('搜索结果总数:', total.value);
          }
          
          // 更新显示的分页数据
          if (allShopsCache.value.length > 0) {
            updatePagedShops();
          } else {
            shops.value = [];
          }
        } else {
          errorMessage.value = '获取数据格式错误，请联系管理员';
          allShopsCache.value = [];
          shops.value = [];
          total.value = 0;
        }
      } catch (error) {
        // 检查组件是否已卸载
        if (isUnmounted.value) return;
        
        errorMessage.value = '搜索时发生意外错误，请稍后再试';
        allShopsCache.value = [];
        shops.value = [];
        total.value = 0;
      } finally {
        // 检查组件是否已卸载
        if (!isUnmounted.value) {
          loading.value = false;
          
          // 搜索后滚动到页面顶部
          window.scrollTo(0, 0);
        }
      }
    };
    
    // 页码变化处理
    const handlePageChange = (page) => {
      if (isUnmounted.value) return;
      
      console.log('页码变化，新页码:', page, '总页数:', Math.ceil(total.value / pageSize.value));
      currentPage.value = page;
      
      // 更新页面显示的商家数据
      updatePagedShops();
      
      // 滚动到页面顶部
      window.scrollTo(0, 0);
    };
    
    // 使用历史记录项
    const useHistoryItem = (keyword) => {
      searchQuery.value = keyword;
      currentPage.value = 1; // 重置到第一页
      searchShops();
    };
    
    // 清空搜索历史
    const clearSearchHistory = async () => {
      if (isUnmounted.value) return;
      
      const currentUserId = userId.value;
      if (!currentUserId) {
        console.warn('清空搜索历史失败: 无法获取用户ID');
        return;
      }
      
      try {
        console.log('清空搜索历史，用户ID:', currentUserId);
        // 调用后端API清空搜索历史
        const response = await ShopService.clearSearchHistory(currentUserId);
        if (response.code === 1) {
          searchHistory.value = [];
          showSearchHistory.value = false;
          console.log('搜索历史清空成功');
        } else {
          console.warn('清空搜索历史失败:', response.msg);
        }
      } catch (error) {
        console.error('清空搜索历史出错:', error);
      }
    };
    
    // 根据筛选条件对缓存数据进行过滤
    const filterCachedShops = () => {
      if (isUnmounted.value) return [];
      
      // 如果没有缓存数据，返回空数组
      if (originalShopsCache.value.length === 0) {
        console.warn('没有原始缓存数据可供筛选');
        return [];
      }
      
      console.log('开始筛选，原始数据大小:', originalShopsCache.value.length);
      
      // 对缓存数据进行深拷贝，避免影响原始数据
      let filteredShops = [...originalShopsCache.value];
      
      // 调试信息：记录筛选前样本数据
      if (filteredShops.length > 0) {
        const sample = filteredShops[0];
        console.log('筛选前样本数据:', {
          结构: sample.shop ? '嵌套shop对象' : '直接shop对象',
          数据: sample.shop || sample
        });
      }
      
      // 应用评分筛选
      if (selectedRating.value) {
        const minRating = Number(selectedRating.value);
        if (!isNaN(minRating)) {
          const beforeCount = filteredShops.length;
          filteredShops = filteredShops.filter(shop => {
            // 处理两种可能的数据结构
            const shopObj = shop.shop || shop;
            const rating = Number(shopObj.rating);
            const result = !isNaN(rating) && rating >= minRating;
            return result;
          });
          console.log(`评分筛选 ${minRating}+ 后: ${beforeCount} -> ${filteredShops.length}`);
        }
      }
      
      // 应用价格筛选
      if (selectedPrice.value) {
        const beforeCount = filteredShops.length;
        // 检查是否是"200+"这种格式（表示200以上）
        if (selectedPrice.value.includes('+')) {
          const minPrice = Number(selectedPrice.value.replace('+', ''));
          filteredShops = filteredShops.filter(shop => {
            // 处理两种可能的数据结构
            const shopObj = shop.shop || shop;
            const shopMinPrice = Number(shopObj.priceMin) || 0;
            const shopMaxPrice = Number(shopObj.priceMax) || 0;
            
            if (isNaN(shopMaxPrice)) {
              console.warn('无效的商家最高价:', shopObj);
              return false;
            }
            
            // 对于"200+"筛选，要求商家价格区间与200以上有交集
            const result = shopMaxPrice >= minPrice;
            return result;
          });
        } else if (selectedPrice.value.includes('-')) {
          // 普通范围筛选 - 使用交集判断
          const [min, max] = selectedPrice.value.split('-');
          const minPrice = Number(min);
          const maxPrice = Number(max);
          
          if (isNaN(minPrice) || isNaN(maxPrice)) {
            console.error('无效的价格区间:', selectedPrice.value);
          } else {
            filteredShops = filteredShops.filter(shop => {
              // 处理两种可能的数据结构
              const shopObj = shop.shop || shop;
              const shopMinPrice = Number(shopObj.priceMin) || 0;
              const shopMaxPrice = Number(shopObj.priceMax) || 0;
              
              if (isNaN(shopMinPrice) || isNaN(shopMaxPrice)) {
                console.warn('无效的商家价格:', shopObj);
                return false;
              }
              
              // 价格区间交集判断：
              // 只有当商家最高价小于筛选最低价，或者商家最低价大于筛选最高价时，才没有交集
              // 取反后就是有交集的条件
              const result = !(shopMaxPrice < minPrice || shopMinPrice > maxPrice);
              return result;
            });
          }
        }
        console.log(`价格筛选 ${selectedPrice.value} 后: ${beforeCount} -> ${filteredShops.length}`);
      }
      
      // 应用人均消费筛选
      if (selectedAverageCost.value) {
        const beforeCount = filteredShops.length;
        // 检查是否是"200+"这种格式（表示200以上的人均消费）
        if (selectedAverageCost.value.includes('+')) {
          const minCost = Number(selectedAverageCost.value.replace('+', ''));
          filteredShops = filteredShops.filter(shop => {
            // 处理两种可能的数据结构
            const shopObj = shop.shop || shop;
            const averageCost = Number(shopObj.averageCost) || 0;
            
            // 对于"200+"筛选，要求人均消费>=200
            return averageCost >= minCost;
          });
        } else if (selectedAverageCost.value.includes('-')) {
          // 普通范围筛选 - 对于人均消费，直接判断是否在范围内
          const [min, max] = selectedAverageCost.value.split('-');
          const minCost = Number(min);
          const maxCost = Number(max);
          
          filteredShops = filteredShops.filter(shop => {
            // 处理两种可能的数据结构
            const shopObj = shop.shop || shop;
            const averageCost = Number(shopObj.averageCost) || 0;
            
            // 人均消费在区间内
            return averageCost >= minCost && averageCost <= maxCost;
          });
        }
        console.log(`人均消费筛选 ${selectedAverageCost.value} 后: ${beforeCount} -> ${filteredShops.length}`);
      }
      
      // 应用排序
      if (sortBy.value !== 'default') {
        if (sortBy.value === 'rating_desc') {
          filteredShops.sort((a, b) => {
            const shopA = a.shop || a;
            const shopB = b.shop || b;
            const ratingA = Number(shopA.rating) || 0;
            const ratingB = Number(shopB.rating) || 0;
            return ratingB - ratingA;
          });
        } else if (sortBy.value === 'average_cost_asc') {
          filteredShops.sort((a, b) => {
            const shopA = a.shop || a;
            const shopB = b.shop || b;
            const costA = Number(shopA.averageCost) || 0;
            const costB = Number(shopB.averageCost) || 0;
            return costA - costB;
          });
        }
        console.log(`应用排序 ${sortBy.value}`);
      }
      
      return filteredShops;
    };
    
    // 应用筛选
    const applyFilter = () => {
      if (isUnmounted.value) return;
      
      // 重置页码到第一页
      currentPage.value = 1;
      
      console.log('应用筛选:', {
        searchQuery: searchQuery.value,
        rating: selectedRating.value,
        price: selectedPrice.value,
        averageCost: selectedAverageCost.value
      });
      
      if (originalShopsCache.value.length > 0) {
        console.log('使用前端筛选，基于原始缓存数据，缓存大小:', originalShopsCache.value.length);
        
        // 复制原始数据，然后应用筛选
        allShopsCache.value = [...originalShopsCache.value];
        
        // 如果有筛选条件，则应用筛选
        if (selectedRating.value || selectedPrice.value || selectedAverageCost.value || sortBy.value !== 'default') {
          // 过滤缓存数据
          const filteredData = filterCachedShops();
          console.log('筛选后数据大小:', filteredData.length);
          
          // 更新数据
          allShopsCache.value = filteredData;
        }
        
        // 设置总数为筛选后的实际总数，而不是当前页显示的数量
        total.value = allShopsCache.value.length;
        console.log('筛选后总数:', total.value, '总页数:', Math.ceil(total.value / pageSize.value));
        
        // 更新当前页显示的数据
        updatePagedShops();
      } else if (searchQuery.value.trim()) {
        // 如果有搜索关键词但没有缓存数据，重新执行搜索
        console.log('无缓存数据，重新执行搜索');
        searchShops();
      } else {
        console.warn('既没有搜索关键词也没有缓存数据，无法应用筛选');
      }
    };
    
    // 查看商家详情
    const viewShopDetails = (shopData) => {
      if (isUnmounted.value) return;
      
      try {
        if (!shopData) {
          console.error('商家数据为空');
          return;
        }
        
        console.log('查看商家详情:', shopData);
        
        let shopId;
        // 处理两种可能的数据结构
        if (shopData.shop && shopData.shop.id) {
          // 第一种数据结构：{shop: {...}, images: [...]}
          shopId = shopData.shop.id;
        } else if (shopData.id) {
          // 第二种数据结构：直接是商家对象
          shopId = shopData.id;
        } else {
          console.error('无法获取商家ID');
          return;
        }
        
        router.push({ name: 'ShopDetail', params: { id: shopId } });
      } catch (error) {
        console.error('查看商家详情出错:', error);
      }
    };
    
    // 重置筛选条件
    const resetFilters = () => {
      if (isUnmounted.value) return;
      
      selectedRating.value = '';
      selectedPrice.value = '';
      selectedAverageCost.value = '';
      sortBy.value = 'default';
      currentPage.value = 1;
      
      // 恢复原始数据
      if (originalShopsCache.value.length > 0) {
        console.log('重置筛选条件，恢复原始数据');
        allShopsCache.value = [...originalShopsCache.value];
        total.value = allShopsCache.value.length;
        updatePagedShops();
      } else {
        searchShops();
      }
    };
    
    // 重置筛选条件并重新搜索
    const resetAndSearch = () => {
      if (isUnmounted.value) return;
      
      errorMessage.value = '';
      searchQuery.value = '';
      selectedRating.value = '';
      selectedPrice.value = '';
      selectedAverageCost.value = '';
      sortBy.value = 'default';
      currentPage.value = 1;
      loadShops();
    };
    
    // 获取商家图片
    const getShopImage = (shopData) => {
      try {
        if (!shopData) {
          return defaultImage;
        }
        
        // 获取正确的shop对象和图片数组
        let shop, images;
        
        // 检查数据结构 - 有两种可能的情况
        if (shopData.shop && shopData.images) {
          // 第一种数据结构：{shop: {...}, images: [...]}
          // 这是后端page和search接口新的统一结构
          shop = shopData.shop;
          images = shopData.images;
        } else if (shopData.id) {
          // 第二种数据结构：直接是商家对象
          // 这可能是旧版API或者前端缓存数据
          shop = shopData;
          images = [];
        } else {
          return defaultImage;
        }
        
        // 处理图片数据
        if (Array.isArray(images) && images.length > 0) {
          const imageData = images[0]; // 使用第一张图片
          
          if (imageData && imageData.imageUrl) {
            let imageUrl = imageData.imageUrl;
            
            // 统一处理图片URL
            if (imageUrl.startsWith('http')) {
              // 如果是完整的URL，直接使用
              return imageUrl;
            } else {
              // 如果是相对路径，添加后端服务器地址
              // 移除开头的斜杠（如果有）
              imageUrl = imageUrl.replace(/^\/+/, '');
              // 添加后端服务器地址
              imageUrl = `http://localhost:8088/${imageUrl}`;
              return imageUrl;
            }
          }
        }
        
        // 如果没有图片，返回基于分类的默认图片
        const categoryName = shop.categoryName || '未分类';
        
        const categoryImages = {
          '火锅': 'https://fuss10.elemecdn.com/a/3f/3302e58f9a181d2509f3dc0fa68b0jpeg.jpeg',
          '奶茶': 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg',
          '烧烤': 'https://shadow.elemecdn.com/app/element/hamburger.9cf7b091-55e9-11e9-a976-7f4d0b07eef6.png',
          '西餐': 'https://fuss10.elemecdn.com/1/34/19aa98b1fcb2781c4fba33d850549jpeg.jpeg',
          '中餐': 'https://fuss10.elemecdn.com/0/6f/e35ff375812e6b0020b6b4e8f9583jpeg.jpeg',
          '快餐': 'https://fuss10.elemecdn.com/9/bb/e27858e973f5d7d3904835f46abbdjpeg.jpeg',
          '甜品': 'https://fuss10.elemecdn.com/d/e6/c4d93a3805b3ce3f323f7974e6f78jpeg.jpeg',
          '小吃': 'https://fuss10.elemecdn.com/3/28/bbf893f792f03a54408b3b7a7ebf0jpeg.jpeg',
          '海鲜': 'https://fuss10.elemecdn.com/0/6f/e35ff375812e6b0020b6b4e8f9583jpeg.jpeg',
          '自助餐': 'https://fuss10.elemecdn.com/9/bb/e27858e973f5d7d3904835f46abbdjpeg.jpeg',
          '未分类': 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg'
        };
        
        return categoryImages[categoryName] || categoryImages['未分类'];
      } catch (error) {
        return defaultImage;
      }
    };
    
    // 格式化价格区间显示
    const formatPriceRange = (range) => {
      if (!range) return '';
      
      const [min, max] = range.split('-');
      if (max === '+') {
        return `￥${min}以上`;
      }
      return `￥${min}-${max}`;
    };
    
    // 处理每页显示条数变化
    const handlePageSizeChange = () => {
      if (isUnmounted.value) return;
      
      // 记录当前显示的第一条记录的索引
      const currentStartIndex = (currentPage.value - 1) * pageSize.value;
      console.log('每页条数变化 -', pageSize.value, '当前起始索引:', currentStartIndex);
      
      // 根据新的每页条数计算新的页码
      currentPage.value = Math.floor(currentStartIndex / pageSize.value) + 1;
      console.log('调整为新页码:', currentPage.value);
      
      // 更新分页数据
      updatePagedShops();
    };
    
    // 在 setup 函数中添加以下内容
    const handleSearchFocus = async () => {
      if (userId.value) {
        showSearchHistory.value = true;
        await loadSearchHistory();
      }
    };
    
    // 添加搜索框失去焦点的处理函数
    const handleSearchBlur = () => {
      // 使用setTimeout延迟，以便能够点击历史记录
      setTimeout(() => {
        showSearchHistory.value = false;
        showSimilarKeywords.value = false;
      }, 300);
    };
    
    // 修改清空搜索框的处理
    const handleClear = async () => {
      searchQuery.value = '';
      if (userId.value && document.activeElement === document.querySelector('.search-input .el-input__inner')) {
        showSearchHistory.value = true;
        await loadSearchHistory();
      }
    };
    
    // 修改搜索历史点击处理方法
    const handleHistoryClick = (keyword) => {
      searchQuery.value = keyword;
      showSearchHistory.value = false;
      searchShops();
    };
    
    // 监听搜索框输入，获取相似关键词
    const handleSearchInput = async (value) => {
      if (value && value.trim().length >= 2) {
        // 获取相似关键词
        await getSimilarKeywords(value);
      } else {
        showSimilarKeywords.value = false;
        similarKeywords.value = [];
      }
    };

    // 获取相似关键词
    const getSimilarKeywords = async (keyword) => {
      try {
        if (!keyword || keyword.trim().length < 2) return;
        
        const response = await ShopService.getSimilarKeywords(keyword.trim());
        
        if (response && response.code === 1 && response.data && response.data.length > 0) {
          similarKeywords.value = response.data;
          showSimilarKeywords.value = true;
        } else {
          similarKeywords.value = [];
          showSimilarKeywords.value = false;
        }
      } catch (error) {
        similarKeywords.value = [];
        showSimilarKeywords.value = false;
      }
    };

    // 使用相似关键词进行搜索
    const useSimilarKeyword = (keyword) => {
      searchQuery.value = keyword;
      showSimilarKeywords.value = false;
      searchShops();
    };
    
    // 收起/展开搜索历史
    const toggleHistoryExpand = () => {
      historyExpanded.value = !historyExpanded.value;
      
      // 如果是收起操作，设置延时后完全隐藏搜索历史框
      if (!historyExpanded.value) {
        setTimeout(() => {
          showSearchHistory.value = false;
        }, 300);
      }
    };
    
    return {
      Search,
      Delete,
      Clock,
      Loading,
      ArrowDown,
      ArrowUp,
      searchQuery,
      shops,
      loading,
      errorMessage,
      searchHistory,
      showSearchHistory,
      historyExpanded,
      isLoading,
      selectedRating,
      selectedPrice,
      selectedAverageCost,
      sortBy,
      searchShops,
      useHistoryItem,
      clearSearchHistory,
      toggleHistoryExpand,
      applyFilter,
      viewShopDetails,
      resetFilters,
      resetAndSearch,
      getShopImage,
      formatPriceRange,
      defaultImage,
      currentPage,
      pageSize,
      total,
      totalPages,
      isUnmounted,
      handlePageChange,
      handlePageSizeChange,
      handleSearchFocus,
      handleSearchBlur,
      handleClear,
      handleHistoryClick,
      // 相似关键词相关
      showSimilarKeywords,
      similarKeywords,
      handleSearchInput,
      getSimilarKeywords,
      useSimilarKeyword
    };
  }
};
</script>

<style scoped>
body {
  overflow-x: hidden;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.shop-search-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
  min-height: 100vh;
}

.search-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 15px 0;
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  position: relative;
}

.search-input :deep(.el-input__wrapper) {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 25px;
  padding: 0 20px;
}

.search-input :deep(.el-input__inner) {
  height: 45px;
  font-size: 16px;
}

.search-history {
  position: absolute;
  top: calc(100% + 5px);
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 800px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  padding: 15px;
  margin-top: 0;
  z-index: 101;
  transition: all 0.3s ease;
  overflow: hidden;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
  transition: all 0.3s ease;
}

.history-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.history-actions :deep(.el-button) {
  padding: 4px 8px;
  display: flex;
  align-items: center;
  transition: all 0.2s ease;
}

.history-actions :deep(.el-button):hover {
  background-color: #f0f2f500;
  border-radius: 4px;
}

.history-actions :deep(.el-icon) {
  margin-right: 4px;
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  transition: max-height 0.3s ease;
  overflow: hidden;
}

.history-item {
  padding: 6px 12px;
  background-color: #f5f7fa;
  border-radius: 16px;
  cursor: pointer;
  font-size: 13px;
  color: #606266;
  transition: all 0.3s ease;
}

.history-item:hover {
  background-color: #ecf5ff;
  color: #409EFF;
}

.filter-panel {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.filter-section {
  margin-bottom: 0;
}

.filter-section:last-child {
  margin-bottom: 0;
}

.filter-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-options :deep(.el-radio) {
  margin-right: 0;
}

.filter-options :deep(.el-radio__label) {
  padding: 6px 12px;
  border-radius: 16px;
  background-color: #f5f7fa;
  transition: all 0.3s ease;
}

.filter-options :deep(.el-radio__input.is-checked + .el-radio__label) {
  background-color: #ecf5ff;
  color: #409EFF;
}

.filter-actions {
  grid-column: 1 / -1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.filter-actions-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.filter-actions-left .filter-title {
  margin-bottom: 0;
}

.filter-actions-left :deep(.el-select) {
  width: 120px;
}

.shop-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  padding: 10px 0;
}

.shop-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.shop-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.shop-avatar {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.shop-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.shop-card:hover .shop-avatar img {
  transform: scale(1.05);
}

.shop-info {
  padding: 15px;
}

.shop-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.shop-rating {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.shop-rating span {
  margin-left: 8px;
  color: #ff9900;
  font-weight: 500;
}

.shop-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.shop-meta :deep(.el-tag) {
  border-radius: 12px;
  padding: 4px 10px;
}

.shop-address, .shop-hours {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.shop-address i, .shop-hours i {
  margin-right: 6px;
  font-size: 14px;
}

.shop-description {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  margin-top: 8px;
}

.search-summary {
  background-color: #fff;
  padding: 15px 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.result-count {
  font-size: 14px;
  color: #606266;
}

.result-count strong {
  color: #409EFF;
  font-weight: 600;
}

.active-filters {
  display: flex;
  gap: 8px;
}

.active-filters span {
  background-color: #ecf5ff;
  color: #409EFF;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 13px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 30px;
  padding: 20px 0;
}

.pagination-empty-message {
  background-color: #fff;
  padding: 40px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.pagination-info {
  margin-left: 15px;
  font-size: 13px;
  color: #606266;
}

.loading, .no-results {
  background-color: #fff;
  padding: 40px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.no-results h3 {
  color: #303133;
  font-size: 18px;
  margin-bottom: 10px;
}

.no-results p {
  color: #909399;
  font-size: 14px;
}

.error-message {
  background-color: #fff;
  padding: 20px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.retry-button {
  margin-top: 15px;
}

.similar-keywords {
  position: absolute;
  width: 100%;
  max-width: 800px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  padding: 15px;
  margin-top: 10px;
  z-index: 100;
}

.similar-header {
  display: flex;
  justify-content: space-between;
  padding-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.similar-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.similar-item {
  cursor: pointer;
  transition: all 0.3s ease;
}

.similar-item:hover {
  background-color: #409EFF;
  color: white;
}

.history-header span {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.empty-result-message {
  text-align: center;
  padding: 15px;
}

.empty-result-message h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 15px;
}

.empty-result-tips {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.empty-result-tips-list {
  list-style-type: none;
  padding: 0;
  margin: 0 0 20px 0;
  text-align: center;
}

.empty-result-tips-list li {
  color: #909399;
  font-size: 14px;
  line-height: 1.8;
}

.empty-reset-btn {
  margin-top: 10px;
}

.no-results {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 30px;
  text-align: center;
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media screen and (max-width: 768px) {
  .shop-search-container {
    padding: 10px;
  }
  
  .search-bar {
    padding: 10px 0;
  }
  
  .filter-panel {
    padding: 15px;
  }
  
  .shop-list {
    grid-template-columns: 1fr;
  }
  
  .shop-avatar {
    height: 160px;
  }
}
</style> 