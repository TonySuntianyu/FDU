<template>
  <div v-if="show" class="coupon-modal-overlay">
    <div class="coupon-modal">
      <div class="coupon-modal-header">
        <h2>新人专享优惠券</h2>
        <span class="close-btn" @click="close">&times;</span>
      </div>
      <div class="coupon-modal-body">
        <p class="welcome-text">欢迎使用"小众点评"！新用户可免费领取一张优惠券</p>
        
        <div class="coupons-container">
          <div 
            v-for="(coupon, index) in coupons" 
            :key="index" 
            class="coupon-card"
            :class="{ 
              'selected': selectedCouponId === coupon.id,
              'disabled': coupon.totalQuantity <= 0 || coupon.disabled
            }"
            @click="!coupon.disabled && coupon.totalQuantity > 0 ? selectCoupon(coupon) : null"
          >
            <div class="coupon-header">
              <h3>{{ coupon.title }}</h3>
              <span class="coupon-value">
                <template v-if="coupon.type === '减固定金额'">
                  满{{ coupon.useThreshold }}元减{{ coupon.amount }}元
                </template>
                <template v-else-if="coupon.type === '减到固定金额'">
                  <template v-if="coupon.amount === 0">免单券</template>
                  <template v-else>减至{{ coupon.amount }}元</template>
                </template>
                <template v-else-if="coupon.type === '折扣券'">
                  {{ Math.round((coupon.amount)*10) }}折
                </template>
                <template v-else-if="coupon.type === '秒杀券'">
                  秒杀价{{ coupon.amount }}元
                </template>
                <template v-else-if="coupon.type === '免单券'">
                  最高免{{ coupon.maxDeduction }}元
                </template>
              </span>
            </div>
            <div class="coupon-body">
              <p v-if="coupon.description">{{ coupon.description }}</p>
              <p v-if="coupon.useThreshold">
                <span class="label">使用门槛：</span>
                <span>满{{ coupon.useThreshold }}元可用</span>
              </p>
              <p v-else><span class="label">使用门槛：</span><span>无门槛</span></p>
              <p v-if="coupon.maxDeduction">
                <span class="label">最高抵扣：</span>
                <span>{{ coupon.maxDeduction }}元</span>
              </p>
              <p v-if="coupon.applicableCategory">
                <span class="label">适用品类：</span>
                <span>{{ coupon.applicableCategory }}</span>
              </p>
              <p v-if="coupon.applicableShop">
                <span class="label">适用店铺：</span>
                <span>{{ coupon.applicableShop }}</span>
              </p>
              <p>
                <span class="label">有效期：</span>
                <span>领取后{{ coupon.validDays }}天内有效</span>
              </p>
              <p>
                <span class="label">剩余数量：</span>
                <span v-if="coupon.totalQuantity > 0">{{ coupon.totalQuantity }}张</span>
                <span v-else class="sold-out">已抢光</span>
              </p>
            </div>
            <div v-if="coupon.totalQuantity <= 0 || coupon.disabled" class="sold-out-overlay">
              <span>已抢光</span>
            </div>
          </div>
        </div>

        <div class="action-buttons">
          <button 
            class="receive-btn" 
            :disabled="!selectedCouponId || loading || (selectedCoupon && (selectedCoupon.totalQuantity <= 0 || selectedCoupon.disabled))" 
            @click="receiveCoupon"
          >
            {{ loading ? '领取中...' : '立即领取' }}
          </button>
          <button class="skip-btn" @click="skip">暂不领取</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import CouponService from '@/services/CouponService';
import { ElMessage } from 'element-plus';

export default {
  name: 'NewUserCouponModal',
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      coupons: [],
      selectedCouponId: null,
      selectedCoupon: null,
      loading: false,
      error: null
    };
  },
  watch: {
    show(newVal) {
      if (newVal) {
        this.fetchCoupons();
        // 重置选择状态
        this.selectedCouponId = null;
        this.selectedCoupon = null;
      }
    }
  },
  methods: {
    async fetchCoupons() {
      try {
        this.loading = true;
        const coupons = await CouponService.getNewUserCoupons();
        console.log('获取到的优惠券数据:', coupons);
        
        if (Array.isArray(coupons) && coupons.length > 0) {
          this.coupons = coupons;
        } else {
          console.warn('获取的优惠券数据为空或无效');
          this.coupons = [];
        }
      } catch (error) {
        this.error = error.message || '获取优惠券失败';
        console.error('获取优惠券失败:', error);
      } finally {
        this.loading = false;
      }
    },
    selectCoupon(coupon) {
      if (!coupon || !coupon.id) {
        console.error('无效的优惠券数据:', coupon);
        return;
      }
      
      this.selectedCouponId = coupon.id;
      this.selectedCoupon = coupon;
      console.log('已选择优惠券:', coupon.title, 'ID:', coupon.id);
    },
    async receiveCoupon() {
      if (!this.selectedCouponId || !this.selectedCoupon || this.loading) {
        return;
      }
      
      try {
        this.loading = true;
        console.log('开始领取优惠券，ID:', this.selectedCouponId);
        
        const result = await CouponService.receiveNewUserCoupon(this.selectedCouponId);
        console.log('领取优惠券结果:', result);
        
        // 领取成功，发送事件通知父组件
        this.$emit('received', this.selectedCouponId);
        
        // 使用Element Plus的ElMessage组件显示成功消息
        ElMessage({
          message: '恭喜您，优惠券领取成功！',
          type: 'success'
        });
        
        this.close();
      } catch (error) {
        // 直接使用增强错误对象中的消息
        let errorMsg = error.message || '领取优惠券失败';
        
        // 检查优惠券数量是否已经为0
        if (this.selectedCoupon && this.selectedCoupon.totalQuantity <= 0) {
          errorMsg = '优惠券已被抢光';
        }
        
        // 记录错误信息，优先显示友好错误消息
        this.error = errorMsg;
        console.error('领取优惠券失败:', error);
        
        // 使用Element Plus的ElMessage组件显示错误消息
        ElMessage({
          message: errorMsg,
          type: 'warning' // 使用警告图标而不是错误图标，更友好
        });
        
        // 如果错误是因为优惠券已发放完，则从列表中移除或标记为不可用
        if (errorMsg.includes('已发放完') || errorMsg.includes('已被抢光')) {
          // 更新当前选中的优惠券状态
          if (this.selectedCoupon) {
            this.selectedCoupon.totalQuantity = 0;
            this.selectedCoupon.disabled = true;
          }
          
          // 刷新优惠券列表
          this.fetchCoupons();
        }
      } finally {
        this.loading = false;
      }
    },
    skip() {
      this.$emit('skipped');
      this.close();
    },
    close() {
      this.$emit('close');
    }
  }
};
</script>

<style scoped>
.coupon-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.coupon-modal {
  background-color: white;
  border-radius: 8px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.coupon-modal-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.coupon-modal-header h2 {
  margin: 0;
  color: #ff5722;
  font-size: 24px;
}

.close-btn {
  font-size: 28px;
  cursor: pointer;
  color: #999;
}

.close-btn:hover {
  color: #333;
}

.coupon-modal-body {
  padding: 20px;
}

.welcome-text {
  text-align: center;
  font-size: 16px;
  margin-bottom: 20px;
  color: #666;
}

.coupons-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  justify-content: center;
}

.coupon-card {
  width: 100%;
  max-width: 300px;
  border: 2px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.coupon-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  border-color: #ff9800;
}

.coupon-card.selected {
  border-color: #ff5722;
  box-shadow: 0 0 10px rgba(255, 87, 34, 0.3);
}

.coupon-card.selected::after {
  content: '✓';
  position: absolute;
  top: 10px;
  right: 10px;
  width: 20px;
  height: 20px;
  background-color: #ff5722;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.coupon-header {
  background-color: #f5f5f5;
  padding: 15px;
  border-bottom: 1px dashed #ddd;
}

.coupon-header h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.coupon-value {
  display: block;
  font-size: 18px;
  font-weight: bold;
  color: #ff5722;
}

.coupon-body {
  padding: 15px;
}

.coupon-body p {
  margin: 8px 0;
  font-size: 14px;
  color: #666;
}

.label {
  color: #999;
  margin-right: 5px;
}

.action-buttons {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 15px;
}

.receive-btn, .skip-btn {
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.receive-btn {
  background-color: #ff5722;
  color: white;
  border: none;
}

.receive-btn:hover {
  background-color: #e64a19;
}

.receive-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.skip-btn {
  background-color: transparent;
  color: #666;
  border: 1px solid #ddd;
}

.skip-btn:hover {
  background-color: #f5f5f5;
}

.coupon-card.disabled {
  opacity: 0.6;
  cursor: not-allowed;
  border-color: #ddd;
}

.sold-out {
  color: #ff0000;
  font-weight: bold;
}

.sold-out-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  font-size: 24px;
  font-weight: bold;
  transform: rotate(-15deg);
}

.sold-out-overlay span {
  background-color: #ff5722;
  padding: 5px 20px;
  border-radius: 5px;
}
</style> 