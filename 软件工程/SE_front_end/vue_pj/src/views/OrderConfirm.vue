<!-- Order Confirmation Page -->
<template>
  <div class="order-confirm">
    <div class="navigation">
      <el-button icon="el-icon-arrow-left" @click="goBack">返回套餐详情</el-button>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="!packageDetail" class="no-data">
      <h2>未找到套餐信息</h2>
      <el-button type="primary" @click="goBack">返回</el-button>
    </div>

    <div v-else class="order-content">
      <h1>确认订单</h1>

      <div class="package-info">
        <h2>套餐信息</h2>
        <div class="package-card">
          <div class="package-title">{{ packageDetail.title }}</div>
          <div class="package-price">¥{{ packageDetail.price }}</div>
        </div>
      </div>

      <div class="coupon-section">
        <h2>选择优惠券</h2>
        <div v-if="loadingCoupons" class="loading-coupons">
          <el-skeleton :rows="3" animated />
        </div>
        <div v-else-if="!coupons || coupons.length === 0" class="no-coupons">
          <p>暂无可用优惠券</p>
        </div>
        <div v-else class="coupon-selection">
          <div class="coupon-selection-header">
            <span class="coupon-count">共{{ coupons.length }}张可用于当前套餐的优惠券</span>
            <span class="coupon-package-info">套餐: {{ packageDetail.title }}</span>
          </div>
          <el-radio-group v-model="selectedCouponId">
            <div 
              class="coupon-item no-coupon-option" 
              :class="{ 'coupon-selected': selectedCouponId === null }"
              @click="selectedCouponId = null"
            >
              <el-radio :label="null" class="coupon-radio">
                <div class="coupon-info no-coupon-info">
                  <div class="no-coupon-text">不使用优惠券</div>
                </div>
              </el-radio>
            </div>
            
            <div 
              v-for="coupon in coupons" 
              :key="coupon.id" 
              class="coupon-item"
              :class="{ 'coupon-selected': selectedCouponId === coupon.id }"
              @click="selectedCouponId = coupon.id"
            >
              <el-radio :label="coupon.id" class="coupon-radio">
                <div class="coupon-info">
                  <!-- 根据优惠券类型显示不同的内容 -->
                  <div class="coupon-amount" :class="{ 'discount-type': coupon.type === '折扣券' }">
                    <template v-if="coupon.type === '折扣券'">
                      {{ formatDiscount(coupon.amount) }}
                    </template>
                    <template v-else-if="coupon.type === '秒杀券'">
                      秒杀价{{ coupon.amount }}元
                    </template>
                    <template v-else-if="coupon.type === '免单券'">
                      最高免{{ coupon.maxDeduction }}元
                    </template>
                    <template v-else>
                      ¥{{ coupon.discountAmount }}
                    </template>
                  </div>
                  <div class="coupon-details">
                    <div class="coupon-name">{{ coupon.title || '优惠券' }}</div>
                    <div class="coupon-description" v-if="coupon.description">{{ coupon.description }}</div>
                    
                    <!-- 显示优惠券有效期信息 -->
                    <div class="coupon-validity-container">
                      <!-- 如果有计算出的过期日期 -->
                      <div class="coupon-validity" v-if="coupon.expirationDate">
                        有效期: {{ formatDate(coupon.expirationDate) }}
                      </div>
                      <!-- 没有过期日期但有有效期文本 -->
                      <div class="coupon-validity" v-else-if="coupon.validityText">
                        {{ coupon.validityText }}
                      </div>
                      <!-- 没有任何有效期信息 -->
                      <div class="coupon-validity" v-else>长期有效</div>
                      
                      <!-- 显示validDays信息作为标签 -->
                      <div v-if="coupon.validDays" class="validity-tag">
                        {{ coupon.validDays }}天
                      </div>
                    </div>
                  </div>
                </div>
              </el-radio>
            </div>
          </el-radio-group>
        </div>
      </div>

      <!-- 邀请码输入 -->
      <div class="invitation-section">
        <h2>邀请码</h2>
        <div class="invitation-input">
          <el-input 
            v-model="invitationCode" 
            placeholder="请输入邀请码（选填）" 
            clearable
            maxlength="8"
            :disabled="submitting"
          >
            <template #prefix>
              <i class="el-icon-medal"></i>
            </template>
          </el-input>
          <div class="invitation-tips">
            <p>首次下单填写邀请码，订单金额满10元即可成功被邀请</p>
            <p>每被成功邀请一次，您将获得额外优惠券</p>
          </div>
        </div>
      </div>

      <div class="order-summary">
        <div class="summary-row">
          <span>套餐原价：</span>
          <span>¥{{ packageDetail.price }}</span>
        </div>
        <div class="summary-row" v-if="selectedCoupon">
          <span>优惠券：</span>
          <span v-if="selectedCoupon.type === '折扣券'">
            {{ formatDiscount(selectedCoupon.amount) }}
            (优惠¥{{ calculateDiscountAmount(packageDetail.price, selectedCoupon).toFixed(2) }})
          </span>
          <span v-else-if="selectedCoupon.type === '秒杀券'">
            秒杀价{{ selectedCoupon.amount }}元
            (优惠¥{{ calculateDiscountAmount(packageDetail.price, selectedCoupon).toFixed(2) }})
          </span>
          <span v-else-if="selectedCoupon.type === '免单券'">
            最高免{{ selectedCoupon.maxDeduction }}元
            (优惠¥{{ calculateDiscountAmount(packageDetail.price, selectedCoupon).toFixed(2) }})
          </span>
          <span v-else>-¥{{ selectedCoupon.discountAmount }}</span>
        </div>
        <div class="summary-row total">
          <span>实付金额：</span>
          <span>¥{{ finalPrice }}</span>
        </div>
      </div>

      <div class="submit-section">
        <el-button type="primary" size="large" :loading="submitting" @click="submitOrder">确认下单</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import GroupBuyService from '@/services/GroupBuyService';
import InvitationService from '@/services/InvitationService';

export default {
  name: 'OrderConfirm',
  setup() {
    const route = useRoute();
    const router = useRouter();
    
    const packageDetail = ref(null);
    const coupons = ref([]);
    const selectedCouponId = ref(null);
    const loading = ref(true);
    const loadingCoupons = ref(true);
    const submitting = ref(false);
    const invitationCode = ref('');

    // 加载套餐详情
    const loadPackageDetail = async () => {
      const packageId = route.params.id;
      if (!packageId) {
        loading.value = false;
        return;
      }

      try {
        loading.value = true;
        const response = await GroupBuyService.getPackageDetail(packageId);
        packageDetail.value = response;
      } catch (error) {
        console.error('获取套餐详情失败:', error);
        ElMessage.error('获取套餐详情失败');
      } finally {
        loading.value = false;
      }
    };

    // 加载用户可用优惠券
    const loadCoupons = async () => {
      try {
        loadingCoupons.value = true;
        // 确保已经加载了套餐详情才调用获取优惠券接口
        if (!packageDetail.value || !packageDetail.value.id) {
          console.log('套餐信息未加载完成，无法筛选优惠券');
          coupons.value = [];
          loadingCoupons.value = false;
          return;
        }
        
        // 传递packageId参数，用于筛选适用的优惠券
        const response = await GroupBuyService.getUserCoupons(packageDetail.value.id);
        
        console.log('前端接收到的优惠券数据:', response);
        
        // 确保优惠券数据格式正确
        if (response && Array.isArray(response)) {
          coupons.value = response.map(coupon => {
            // 计算真实有效期 - 处理validDays的情况
            let finalExpirationDate = coupon.expirationDate;
            let validityText = '';
            
            // 如果没有过期时间但有validDays
            if (!finalExpirationDate && coupon.validDays) {
              // 从当前时间计算有效期
              const now = new Date();
              const expirationDate = new Date(now);
              expirationDate.setDate(now.getDate() + coupon.validDays);
              finalExpirationDate = expirationDate.toISOString();
              validityText = `${coupon.validDays}天有效期`;
              
              console.log(`优惠券${coupon.id}基于当前时间和validDays=${coupon.validDays}计算的有效期: ${finalExpirationDate}`);
            } 
            // 如果有receivedAt和validDays
            else if (coupon.receivedAt && coupon.validDays) {
              try {
                const receivedDate = new Date(coupon.receivedAt);
                if (!isNaN(receivedDate.getTime())) {
                  const calculatedDate = new Date(receivedDate);
                  calculatedDate.setDate(receivedDate.getDate() + coupon.validDays);
                  finalExpirationDate = calculatedDate.toISOString();
                  validityText = `领取后${coupon.validDays}天有效期`;
                  
                  console.log(`优惠券${coupon.id}基于领取时间和validDays=${coupon.validDays}计算的有效期: ${finalExpirationDate}`);
                }
              } catch (err) {
                console.error('计算动态有效期出错:', err);
              }
            }
            // 有过期时间，没有validDays
            else if (finalExpirationDate) {
              validityText = '固定到期日';
            }
            // 既没有过期时间也没有validDays
            else {
              validityText = '长期有效';
            }
            
            return {
              ...coupon,
              // 确保折扣金额有效
              discountAmount: coupon.discountAmount || coupon.amount || 0,
              // 确保标题存在
              title: coupon.title || '优惠券',
              // 确保描述字段
              description: coupon.description || '',
              // 使用计算后的最终有效期
              expirationDate: finalExpirationDate,
              // 有效期文本说明
              validityText: coupon.validityText || validityText
            };
          });
          
          console.log('处理后的优惠券数据:', coupons.value);
        } else {
          coupons.value = [];
        }
        
        // 默认选择减免金额最高的优惠券
        if (coupons.value.length > 0) {
          const bestCoupon = coupons.value.reduce((prev, current) => 
            (prev.discountAmount > current.discountAmount) ? prev : current
          );
          selectedCouponId.value = bestCoupon.id;
        }
      } catch (error) {
        console.error('获取优惠券失败:', error);
        coupons.value = [];
      } finally {
        loadingCoupons.value = false;
      }
    };

    // 返回上一页
    const goBack = () => {
      router.go(-1);
    };

    // 提交订单
    const submitOrder = async () => {
      if (!packageDetail.value || !packageDetail.value.id) {
        ElMessage.error('套餐信息不完整，无法下单');
        return;
      }

      try {
        submitting.value = true;
        console.log('正在提交订单，套餐ID:', packageDetail.value.id, '优惠券ID:', selectedCouponId.value);
        
        let response;
        
        // 如果有邀请码，则使用带邀请码的API
        if (invitationCode.value) {
          console.log('使用邀请码下单:', invitationCode.value);
          response = await InvitationService.createOrderWithInvitation(
            packageDetail.value.id,
            invitationCode.value,
            selectedCouponId.value
          );
        } else {
          // 使用普通下单API
          response = await GroupBuyService.createOrder(
            packageDetail.value.id, 
            selectedCouponId.value
          );
        }
        
        console.log('下单成功，响应数据:', response);
        
        // 下单成功，跳转到券码详情页
        if (response) {
          ElMessage.success('下单成功');
          // 使用订单ID进行跳转
          const orderId = response.orderId;
          if (orderId) {
            router.push({ 
              name: 'VoucherDetail', 
              params: { id: orderId } 
            });
          } else {
            console.error('响应中缺少orderId:', response);
            ElMessage.error('下单成功，但无法获取订单ID');
          }
        } else {
          ElMessage.error('下单失败，请重试');
        }
      } catch (error) {
        console.error('下单失败:', error);
        ElMessage.error('下单失败: ' + (error.response?.data?.message || error.message || '请重试'));
      } finally {
        submitting.value = false;
      }
    };

    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return '长期有效';
      
      try {
        console.log('正在格式化日期:', dateString, typeof dateString);
        const date = new Date(dateString);
        
        // Check if date is valid
        if (isNaN(date.getTime())) {
          console.error('无效的日期格式:', dateString);
          return '长期有效';
        }
        
        const now = new Date();
        const diffTime = date.getTime() - now.getTime();
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        
        // 如果超过30天，显示具体日期
        if (diffDays > 30) {
          return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
        }
        // 如果不到30天，显示剩余天数
        else if (diffDays > 0) {
          return `剩余${diffDays}天`;
        }
        // 已过期
        else {
          return '已过期';
        }
      } catch (error) {
        console.error('日期格式化错误:', error);
        return '长期有效';
      }
    };

    // 格式化折扣率的显示
    const formatDiscount = (discount) => {
      if (!discount) return '不打折';
      
      // 直接使用discount值计算折扣
      const discountRate = parseFloat(discount);
      
      // 计算折扣百分比，例如0.9对应9折
      let percentage = Math.round(discountRate * 10);
      
      // 确保是个位数，如果是10则显示为"不打折"
      if (percentage === 10) {
        return '不打折';
      } else if (percentage === 0) {
        return '免单券';
      }
      
      return `${percentage}折`;
    };

    // 计算属性：已选优惠券
    const selectedCoupon = computed(() => {
      if (!selectedCouponId.value) return null;
      return coupons.value.find(coupon => coupon.id === selectedCouponId.value);
    });

    // 计算属性：最终价格
    const finalPrice = computed(() => {
      if (!packageDetail.value) return 0;
      let price = packageDetail.value.price;

      if (selectedCoupon.value) {
        const couponType = selectedCoupon.value.type;
        const amount = parseFloat(selectedCoupon.value.amount) || 0;
        const maxDeduction = parseFloat(selectedCoupon.value.maxDeduction) || 0;

        if (couponType === '折扣券') {
          // 折扣券：原价 * amount
          price = price * amount;
        } else if (couponType === '减到固定金额') {
          // 减到固定金额：直接等于amount
          price = amount;
        } else if (couponType === '减固定金额') {
          // 减固定金额：原价 - amount
          price = price - amount;
        } else if (couponType === '秒杀券') {
          // 秒杀券：如果原价大于maxDeduction，则减maxDeduction
          // 如果原价小于maxDeduction，则减到amount
          if (price > maxDeduction) {
            price = price - maxDeduction;
          } else {
            price = amount;
          }
        } else if (couponType === '免单券') {
          // 免单券：如果原价大于maxDeduction，则减maxDeduction
          // 如果原价小于maxDeduction，则全额减免
          if (price > maxDeduction) {
            price = price - maxDeduction;
          } else {
            price = 0;
          }
        }
      }
      return price.toFixed(2);
    });

    // 计算折扣金额
    const calculateDiscountAmount = (originalPrice, coupon) => {
      if (!originalPrice || !coupon) return 0;
      
      const amount = parseFloat(coupon.amount) || 0;
      const maxDeduction = parseFloat(coupon.maxDeduction) || 0;
      
      if (coupon.type === '折扣券') {
        // 折扣金额 = 原价 * (1 - 折扣率)
        return originalPrice * (1 - amount);
      } else if (coupon.type === '秒杀券') {
        // 秒杀券：如果原价大于maxDeduction，则减maxDeduction
        // 如果原价小于maxDeduction，则减到amount
        if (originalPrice > maxDeduction) {
          return maxDeduction;
        } else {
          return originalPrice - amount;
        }
      } else if (coupon.type === '免单券') {
        // 免单券：如果原价大于maxDeduction，则减maxDeduction
        // 如果原价小于maxDeduction，则全额减免
        if (originalPrice > maxDeduction) {
          return maxDeduction;
        } else {
          return originalPrice;
        }
      } else {
        // 减额券直接返回折扣金额
        return parseFloat(coupon.discountAmount) || 0;
      }
    };

    onMounted(async () => {
      await loadPackageDetail();
      loadCoupons();
    });

    return {
      packageDetail,
      coupons,
      selectedCouponId,
      loading,
      loadingCoupons,
      submitting,
      invitationCode,
      goBack,
      submitOrder,
      formatDate,
      formatDiscount,
      selectedCoupon,
      finalPrice,
      calculateDiscountAmount
    };
  }
}
</script>

<style scoped>
.order-confirm {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.navigation {
  margin-bottom: 20px;
}

.loading, .no-data {
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.order-content {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.order-content h1 {
  margin-top: 0;
  margin-bottom: 30px;
  font-size: 24px;
  color: #333;
  text-align: center;
}

.package-info, .coupon-section, .order-summary {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.package-info h2, .coupon-section h2 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #333;
}

.package-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 6px;
}

.package-title {
  font-size: 16px;
  font-weight: bold;
}

.package-price {
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
}

.coupon-selection {
  margin-top: 20px;
}

/* Common styles for both no-coupon and coupon items */
.coupon-item {
  margin-bottom: 15px;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background-color: #fff;
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
  display: flex;
  align-items: center;
  height: 76px; /* Fixed height for all coupon items */
  box-sizing: border-box;
}

.coupon-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.coupon-selected {
  border-color: #f56c6c;
  background-color: #fff7f7;
}

.coupon-selected::after {
  content: "";
  position: absolute;
  top: -1px;
  bottom: -1px;
  left: -1px;
  width: 4px;
  background-color: #f56c6c;
  border-top-left-radius: 6px;
  border-bottom-left-radius: 6px;
}

/* No coupon option specific styles */
.no-coupon-option {
  background-color: #f5f7fa;
}

.no-coupon-info {
  justify-content: flex-start;
  width: 100%;
  display: flex;
  align-items: center;
  padding-left: 72px; /* Align with coupon content */
}

.no-coupon-text {
  font-size: 15px;
  font-weight: bold;
  color: #606266;
}

/* Coupon radio styles */
.coupon-radio {
  width: 100%;
  height: 100%;
  margin: 0;
  display: flex;
  align-items: center;
}

.coupon-radio :deep(.el-radio__label) {
  padding-left: 8px;
  width: 100%;
  display: flex;
  align-items: center;
  height: 100%;
}

.coupon-radio :deep(.el-radio__input) {
  align-self: center;
  flex-shrink: 0;
}

/* Coupon info styles */
.coupon-info {
  display: flex;
  align-items: center;
  width: 100%;
  height: 100%;
}

.coupon-amount {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
  margin-right: 20px;
  min-width: 80px;
  text-align: center;
}

.coupon-details {
  display: flex;
  flex-direction: column;
  flex: 1;
  justify-content: center;
  min-height: 46px; /* Ensure minimum height for consistency */
}

.coupon-name {
  font-size: 15px;
  font-weight: bold;
  margin-bottom: 4px;
  line-height: 1.2;
}

.coupon-description {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
  line-height: 1.2;
}

.coupon-validity {
  font-size: 12px;
  color: #999;
  line-height: 1.2;
}

.coupon-validity-container {
  display: flex;
  align-items: center;
  margin-top: 2px;
}

.validity-tag {
  background-color: #ff9800;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  margin-left: 8px;
  font-weight: bold;
}

.loading-coupons, .no-coupons {
  padding: 20px;
  text-align: center;
  color: #999;
}

.order-summary {
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 6px;
  border-bottom: none;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.summary-row.total {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px dashed #ddd;
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.submit-section {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.submit-section button {
  width: 200px;
  padding: 12px 0;
  font-size: 16px;
}

.discount-type {
  background-color: #ff9800;
  color: white;
  font-weight: bold;
  border-radius: 4px;
  padding: 4px 8px;
}

.coupon-selection-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.coupon-count {
  font-size: 14px;
  color: #666;
}

.coupon-package-info {
  font-size: 14px;
  color: #999;
}

/* 邀请码部分样式 */
.invitation-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.invitation-section h2 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #333;
}

.invitation-input {
  margin-top: 15px;
}

.invitation-tips {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.invitation-tips p {
  margin: 5px 0;
}
</style> 