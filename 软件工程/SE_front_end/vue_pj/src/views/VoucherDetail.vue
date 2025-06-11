<!-- Voucher Detail Page -->
<template>
  <div class="voucher-detail">
    <div class="navigation">
      <el-button icon="el-icon-arrow-left" @click="goToOrders">查看我的订单</el-button>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="!voucher" class="no-data">
      <h2>未找到券码信息</h2>
      <el-button type="primary" @click="goToOrders">查看我的订单</el-button>
    </div>

    <div v-else class="voucher-content">
      <h1>团购券码</h1>
      
      <div class="voucher-card">
        <div class="package-info">
          <h2>{{ voucher.packageTitle }}</h2>
          <div class="shop-info">适用门店：{{ voucher.shopName || '暂无门店信息' }}</div>
          <div class="price-info">价值：¥{{ voucher.orderPrice }}</div>
          <div class="order-time">购买时间：{{ formatDateTime(voucher.createdTime) }}</div>
        </div>

        <div class="code-info">
          <h3>券码</h3>
          <div class="code-display">{{ formatCode(voucher.code) }}</div>
        </div>

        <div class="qr-code">
          <h3>二维码</h3>
          <div class="qr-image">
            <img v-if="voucher.qrCodeUrl" :src="voucher.qrCodeUrl" alt="券码二维码" />
            <div v-else class="no-qr">二维码加载失败</div>
          </div>
          <p class="usage-tip">出示二维码给商家扫描即可使用</p>
        </div>

        <div class="status-bar" :class="{ 'used': voucher.status === 1 }">
          {{ voucher.status === 1 ? '已使用' : '未使用' }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import GroupBuyService from '@/services/GroupBuyService';

export default {
  name: 'VoucherDetail',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const voucher = ref(null);
    const loading = ref(true);

    // 加载券码详情
    const loadVoucherDetail = async () => {
      const orderId = route.params.id;
      if (!orderId) {
        loading.value = false;
        return;
      }

      try {
        loading.value = true;
        const response = await GroupBuyService.getOrderDetail(orderId);
        voucher.value = response;
        console.log('券码详情:', voucher.value);
      } catch (error) {
        console.error('获取券码详情失败:', error);
        ElMessage.error('获取券码详情失败');
      } finally {
        loading.value = false;
      }
    };

    // 跳转到我的订单页
    const goToOrders = () => {
      router.push({ name: 'MyOrders' });
    };

    // 格式化日期时间
    const formatDateTime = (dateString) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}`;
    };

    // 格式化券码，每4位添加一个空格
    const formatCode = (code) => {
      if (!code) return '';
      return code.replace(/(.{4})/g, '$1 ').trim();
    };

    onMounted(() => {
      loadVoucherDetail();
    });

    return {
      voucher,
      loading,
      goToOrders,
      formatDateTime,
      formatCode
    };
  }
}
</script>

<style scoped>
.voucher-detail {
  max-width: 600px;
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

.voucher-content {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.voucher-content h1 {
  margin-top: 0;
  margin-bottom: 30px;
  font-size: 24px;
  color: #333;
  text-align: center;
}

.voucher-card {
  position: relative;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
}

.package-info {
  padding: 20px;
  background-color: #f9f9f9;
  border-bottom: 1px dashed #ddd;
}

.package-info h2 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 20px;
  color: #333;
}

.shop-info, .price-info, .order-time {
  margin-bottom: 10px;
  font-size: 14px;
  color: #666;
}

.price-info {
  font-weight: bold;
  color: #f56c6c;
}

.code-info {
  padding: 20px;
  text-align: center;
  border-bottom: 1px dashed #ddd;
}

.code-info h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  color: #333;
}

.code-display {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  letter-spacing: 2px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.qr-code {
  padding: 20px;
  text-align: center;
}

.qr-code h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  color: #333;
}

.qr-image {
  width: 200px;
  height: 200px;
  margin: 0 auto 15px;
  padding: 10px;
  background-color: #fff;
  border: 1px solid #eee;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qr-image img {
  max-width: 100%;
  max-height: 100%;
}

.no-qr {
  color: #999;
  font-size: 14px;
}

.usage-tip {
  font-size: 14px;
  color: #666;
}

.status-bar {
  position: absolute;
  top: 20px;
  right: 20px;
  padding: 5px 15px;
  background-color: #67c23a;
  color: #fff;
  border-radius: 20px;
  font-size: 14px;
}

.status-bar.used {
  background-color: #909399;
}
</style> 