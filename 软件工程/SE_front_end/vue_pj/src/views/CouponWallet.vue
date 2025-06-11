<template>
  <div class="coupon-wallet-container">
    <div class="page-header">
      <h1>我的卡包</h1>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-refresh" @click="handleRefresh">刷新</el-button>
        <el-button type="success" @click="$router.push('/shop/list')">去逛逛</el-button>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated style="width: 100%" />
    </div>
    <div v-else-if="coupons.length === 0" class="empty-state">
      <el-empty description="暂无优惠券，快去领取吧！">
        <template #default>
          <el-button type="primary" @click="$router.push('/shop/list')">去领券</el-button>
        </template>
      </el-empty>
    </div>
    <div v-else class="tabs-container">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="未使用" name="unused">
          <div v-if="loading" class="loading">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else-if="unusedCoupons.length === 0" class="empty-state">
            <el-empty description="暂无未使用的优惠券" />
          </div>
          <div v-else class="coupons-list">
            <div v-for="coupon in unusedCoupons" :key="coupon.id" class="coupon-card">
              <div class="coupon-left">
                <div class="coupon-value">
                  <template v-if="coupon.type === '减固定金额'">
                    <span class="symbol">¥</span>
                    <span class="amount">{{ coupon.amount }}</span>
                  </template>
                  <template v-else-if="coupon.type === '减到固定金额'">
                    <template v-if="coupon.amount === 0">
                      <span class="free-text">免单</span>
                    </template>
                    <template v-else>
                      <span class="symbol">¥</span>
                      <span class="amount">{{ coupon.amount }}</span>
                    </template>
                  </template>
                  <template v-else-if="coupon.type === '折扣券'">
                    <span class="amount">{{ Math.round(coupon.amount*10) }}</span>
                    <span class="symbol">折</span>
                  </template>
                </div>
                <div class="coupon-threshold" v-if="coupon.useThreshold">
                  满{{ coupon.useThreshold }}元可用
                </div>
                <div class="coupon-threshold" v-else>无门槛</div>
                <div v-if="coupon.isNewUserCoupon" class="new-user-tag">新人券</div>
              </div>
              <div class="coupon-right">
                <h3 class="coupon-title">{{ coupon.title }}</h3>
                <p class="coupon-description" v-if="coupon.description">{{ coupon.description }}</p>
                <div class="coupon-details">
                  <p v-if="coupon.applicableCategory">适用品类：{{ coupon.applicableCategory }}</p>
                  <p v-if="coupon.applicableShop">适用店铺：{{ coupon.applicableShop }}</p>
                  <p v-if="coupon.maxDeduction">最高抵扣{{ coupon.maxDeduction }}元</p>
                  <p class="expiry-date">
                    <span>有效期至：</span>
                    <span v-if="coupon.expirationDate">{{ formatDate(coupon.expirationDate) }}</span>
                    <span v-else-if="coupon.receivedAt && coupon.validDays">
                      {{ formatDate(new Date(coupon.receivedAt).getTime() + coupon.validDays * 24 * 60 * 60 * 1000) }}
                    </span>
                    <span v-else>无限期</span>
                  </p>
                </div>
                <el-button type="primary" size="small" @click="$router.push('/shop/list')">去使用</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="已使用" name="used">
          <div v-if="loading" class="loading">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else-if="usedCoupons.length === 0" class="empty-state">
            <el-empty description="暂无已使用的优惠券" />
          </div>
          <div v-else class="coupons-list">
            <div v-for="coupon in usedCoupons" :key="coupon.id" class="coupon-card used">
              <!-- 与未使用券相同的结构，但添加了已使用样式 -->
              <div class="coupon-left">
                <div class="coupon-value">
                  <template v-if="coupon.type === '减固定金额'">
                    <span class="symbol">¥</span>
                    <span class="amount">{{ coupon.amount }}</span>
                  </template>
                  <template v-else-if="coupon.type === '减到固定金额'">
                    <template v-if="coupon.amount === 0">
                      <span class="free-text">免单</span>
                    </template>
                    <template v-else>
                      <span class="symbol">¥</span>
                      <span class="amount">{{ coupon.amount }}</span>
                    </template>
                  </template>
                  <template v-else-if="coupon.type === '折扣券'">
                    <span class="amount">{{ Math.round(coupon.amount*10) }}</span>
                    <span class="symbol">折</span>
                  </template>
                </div>
                <div class="coupon-threshold" v-if="coupon.useThreshold">
                  满{{ coupon.useThreshold }}元可用
                </div>
                <div class="coupon-threshold" v-else>无门槛</div>
                <div class="status-label">已使用</div>
              </div>
              <div class="coupon-right">
                <h3 class="coupon-title">{{ coupon.title }}</h3>
                <p class="coupon-description" v-if="coupon.description">{{ coupon.description }}</p>
                <div class="coupon-details">
                  <p v-if="coupon.applicableCategory">适用品类：{{ coupon.applicableCategory }}</p>
                  <p v-if="coupon.applicableShop">适用店铺：{{ coupon.applicableShop }}</p>
                  <p v-if="coupon.maxDeduction">最高抵扣{{ coupon.maxDeduction }}元</p>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="已过期" name="expired">
          <div v-if="loading" class="loading">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else-if="expiredCoupons.length === 0" class="empty-state">
            <el-empty description="暂无已过期的优惠券" />
          </div>
          <div v-else class="coupons-list">
            <div v-for="coupon in expiredCoupons" :key="coupon.id" class="coupon-card expired">
              <!-- 与未使用券相同的结构，但添加了已过期样式 -->
              <div class="coupon-left">
                <div class="coupon-value">
                  <template v-if="coupon.type === '减固定金额'">
                    <span class="symbol">¥</span>
                    <span class="amount">{{ coupon.amount }}</span>
                  </template>
                  <template v-else-if="coupon.type === '减到固定金额'">
                    <template v-if="coupon.amount === 0">
                      <span class="free-text">免单</span>
                    </template>
                    <template v-else>
                      <span class="symbol">¥</span>
                      <span class="amount">{{ coupon.amount }}</span>
                    </template>
                  </template>
                  <template v-else-if="coupon.type === '折扣券'">
                    <span class="amount">{{ Math.round(coupon.amount*10) }}</span>
                    <span class="symbol">折</span>
                  </template>
                </div>
                <div class="coupon-threshold" v-if="coupon.useThreshold">
                  满{{ coupon.useThreshold }}元可用
                </div>
                <div class="coupon-threshold" v-else>无门槛</div>
                <div class="status-label">已过期</div>
              </div>
              <div class="coupon-right">
                <h3 class="coupon-title">{{ coupon.title }}</h3>
                <p class="coupon-description" v-if="coupon.description">{{ coupon.description }}</p>
                <div class="coupon-details">
                  <p v-if="coupon.applicableCategory">适用品类：{{ coupon.applicableCategory }}</p>
                  <p v-if="coupon.applicableShop">适用店铺：{{ coupon.applicableShop }}</p>
                  <p v-if="coupon.maxDeduction">最高抵扣{{ coupon.maxDeduction }}元</p>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import CouponService from '@/services/CouponService';
import { ElMessage } from 'element-plus';

export default {
  name: 'CouponWallet',
  setup() {
    const coupons = ref([]);
    const loading = ref(true);
    const activeTab = ref('unused');
    
    // 根据状态筛选优惠券
    const unusedCoupons = computed(() => {
      console.log('计算未使用优惠券，当前数据:', coupons.value);
      return coupons.value.filter(coupon => coupon.status === 0 || coupon.status === undefined);
    });
    const usedCoupons = computed(() => coupons.value.filter(coupon => coupon.status === 1));
    const expiredCoupons = computed(() => coupons.value.filter(coupon => coupon.status === 2 || checkCouponExpiration(coupon)));
    
    // 格式化日期
    const formatDate = (dateStr) => {
      try {
        const date = new Date(dateStr);
        if (isNaN(date.getTime())) {
          console.error('无效的日期格式:', dateStr);
          return '无效日期';
        }
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
      } catch (error) {
        console.error('日期格式化错误:', error);
        return '无效日期';
      }
    };
    
    // 获取用户卡包中的优惠券
    const fetchCoupons = async () => {
      loading.value = true;
      try {
        const data = await CouponService.getCouponsInWallet();
        console.log('获取到的优惠券数据:', data);
        
        // 确保返回的是数组
        if (Array.isArray(data)) {
          coupons.value = data;
        } else {
          console.error('获取到的优惠券数据不是数组类型:', data);
          coupons.value = [];
        }
        
        // 如果获取成功但数据为空，显示提示
        if (coupons.value.length === 0) {
          ElMessage.info('您的卡包中还没有优惠券，快去领取吧！');
        }
      } catch (error) {
        console.error('获取优惠券失败:', error);
        // 显示错误通知
        ElMessage.error('获取优惠券数据失败，请稍后再试');
        coupons.value = [];
      } finally {
        loading.value = false;
      }
    };
    
    // 检查优惠券是否已过期
    const checkCouponExpiration = (coupon) => {
      if (!coupon) return false;
      
      // 已设置状态为过期
      if (coupon.status === 2) return true;
      
      const now = new Date();
      
      // 检查固定过期时间
      if (coupon.expirationDate) {
        return new Date(coupon.expirationDate) < now;
      }
      
      // 检查基于领取时间的有效期
      if (coupon.receivedAt && coupon.validDays) {
        const expirationDate = new Date(coupon.receivedAt);
        expirationDate.setDate(expirationDate.getDate() + coupon.validDays);
        return expirationDate < now;
      }
      
      return false;
    };
    
    // 刷新按钮点击事件
    const handleRefresh = () => {
      console.log('刷新优惠券数据');
      fetchCoupons();
    };
    
    // 页面加载后获取优惠券数据
    onMounted(() => {
      console.log('CouponWallet组件挂载完成，准备获取数据');
      fetchCoupons();
    });
    
    return {
      coupons,
      loading,
      activeTab,
      unusedCoupons,
      usedCoupons,
      expiredCoupons,
      formatDate,
      handleRefresh,
      checkCouponExpiration
    };
  }
};
</script>

<style scoped>
.coupon-wallet-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.page-header h1 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.loading-container {
  padding: 40px 20px;
}

.tabs-container {
  margin-top: 20px;
}

.loading, .empty-state {
  padding: 40px 0;
  text-align: center;
}

.coupons-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 10px 0;
}

.coupon-card {
  display: flex;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
}

.coupon-card::after {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  left: 100px;
  border-left: 1px dashed #ddd;
  z-index: 1;
}

.coupon-card::before {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  left: 100px;
  width: 20px;
  background-color: #fff;
  z-index: 0;
}

.coupon-card.used, .coupon-card.expired {
  opacity: 0.7;
  filter: grayscale(0.5);
}

.coupon-left {
  width: 100px;
  background-color: #ff5722;
  color: white;
  padding: 20px 10px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  position: relative;
}

.coupon-value {
  margin-bottom: 8px;
}

.symbol {
  font-size: 18px;
  vertical-align: text-top;
}

.amount {
  font-size: 32px;
  font-weight: bold;
}

.free-text {
  font-size: 24px;
  font-weight: bold;
}

.coupon-threshold {
  font-size: 12px;
  white-space: nowrap;
}

.status-label, .new-user-tag {
  position: absolute;
  padding: 4px 8px;
  font-size: 12px;
  border-bottom-right-radius: 8px;
}

.status-label {
  top: 0;
  left: 0;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
}

.new-user-tag {
  top: 0;
  right: 0;
  background-color: #ff9800;
  color: white;
  transform: translateX(100%);
}

.coupon-right {
  flex: 1;
  padding: 15px;
  background-color: white;
  display: flex;
  flex-direction: column;
}

.coupon-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #303133;
}

.coupon-description {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.coupon-details {
  flex: 1;
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}

.coupon-details p {
  margin: 5px 0;
}

.expiry-date {
  color: #f56c6c;
}

@media screen and (max-width: 768px) {
  .coupon-card {
    flex-direction: column;
  }
  
  .coupon-left {
    width: 100%;
    height: 80px;
    flex-direction: row;
    justify-content: space-around;
  }
  
  .coupon-card::after, .coupon-card::before {
    display: none;
  }
  
  .coupon-value {
    margin-bottom: 0;
    margin-right: 10px;
  }
}
</style> 