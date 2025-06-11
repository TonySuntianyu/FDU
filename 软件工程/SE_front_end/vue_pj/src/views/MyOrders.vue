<!-- My Orders Page -->
<template>
  <div class="my-orders">
    <h1>我的团购订单</h1>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="!orders || orders.length === 0" class="no-data">
      <el-empty description="暂无团购订单" />
      <el-button type="primary" @click="goToSearch">去逛逛</el-button>
    </div>

    <div v-else class="orders-list">
      <el-card 
        v-for="order in orders" 
        :key="order.id" 
        class="order-item"
        @click="viewOrderDetail(order.id)"
      >
        <div class="order-content">
          <div class="order-header">
            <div class="order-title">{{ order.packageTitle }}</div>
            <div class="order-status" :class="getStatusClass(order.status)">
              {{ getStatusText(order.status) }}
            </div>
          </div>
          
          <div class="order-details">
            <div class="order-shop">门店：{{ order.shopName || '暂无门店信息' }}</div>
            <div class="order-time">下单时间：{{ formatDateTime(order.createdAt) }}</div>
            <div class="order-price">实付金额：<span>¥{{ order.orderPrice }}</span></div>
          </div>
          
          <div class="order-actions">
            <el-button type="primary" size="small" @click.stop="viewOrderDetail(order.id)">查看券码</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import GroupBuyService from '@/services/GroupBuyService';

export default {
  name: 'MyOrders',
  setup() {
    const router = useRouter();
    const orders = ref([]);
    const loading = ref(true);

    // 加载用户订单列表
    const loadOrders = async () => {
      try {
        loading.value = true;
        const response = await GroupBuyService.getUserOrders();
        orders.value = response || [];
        console.log('用户订单:', orders.value);
      } catch (error) {
        console.error('获取订单失败:', error);
        ElMessage.error('获取订单列表失败');
      } finally {
        loading.value = false;
      }
    };

    // 跳转到订单详情页
    const viewOrderDetail = (orderId) => {
      router.push({ 
        name: 'VoucherDetail', 
        params: { id: orderId } 
      });
    };

    // 跳转到商家搜索页
    const goToSearch = () => {
      router.push({ name: 'ShopSearch' });
    };

    // 获取订单状态文本
    const getStatusText = (status) => {
      const statusMap = {
        0: '已取消',
        1: '已购买',
        2: '已使用'
      };
      return statusMap[status] || '未知状态';
    };

    // 获取订单状态样式类
    const getStatusClass = (status) => {
      const classMap = {
        0: 'cancelled',
        1: 'purchased',
        2: 'used'
      };
      return classMap[status] || '';
    };

    // 格式化日期时间
    const formatDateTime = (dateString) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${date.getMinutes()}`;
    };

    onMounted(() => {
      loadOrders();
    });

    return {
      orders,
      loading,
      viewOrderDetail,
      goToSearch,
      getStatusText,
      getStatusClass,
      formatDateTime
    };
  }
}
</script>

<style scoped>
.my-orders {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  font-size: 24px;
  color: #333;
  margin-bottom: 30px;
  text-align: center;
}

.loading, .no-data {
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.no-data button {
  margin-top: 20px;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-item {
  cursor: pointer;
  transition: transform 0.2s;
}

.order-item:hover {
  transform: translateY(-3px);
}

.order-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.order-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.order-status {
  font-size: 14px;
  padding: 2px 10px;
  border-radius: 4px;
  color: #fff;
}

.order-status.cancelled {
  background-color: #909399;
}

.order-status.purchased {
  background-color: #67c23a;
}

.order-status.used {
  background-color: #409EFF;
}

.order-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.order-price {
  font-weight: bold;
}

.order-price span {
  color: #f56c6c;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 5px;
}
</style> 