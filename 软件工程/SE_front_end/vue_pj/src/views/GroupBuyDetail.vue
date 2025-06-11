<!-- Group Buy Detail Page -->
<template>
  <div class="group-buy-detail">
    <div class="navigation">
      <el-button icon="el-icon-arrow-left" @click="goBack">返回商家页</el-button>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="!packageDetail" class="no-data">
      <h2>未找到套餐信息</h2>
      <el-button type="primary" @click="goBack">返回商家页</el-button>
    </div>

    <div v-else class="package-detail">
      <div class="package-header">
        <h1 class="package-title">{{ packageDetail.title }}</h1>
        <div class="package-meta">
          <div class="package-price">¥{{ packageDetail.price }}</div>
          <div class="package-sales">已售 {{ packageDetail.sales }}</div>
        </div>
      </div>

      <div class="package-description-section">
        <h2>套餐详情</h2>
        <p>{{ packageDetail.description }}</p>
      </div>

      <div class="package-dishes-section" v-if="packageDetail.dishItems && packageDetail.dishItems.length > 0">
        <h2>套餐内容</h2>
        <el-table :data="packageDetail.dishItems" stripe style="width: 100%">
          <el-table-column prop="dishName" label="菜品名称" width="180" />
          <el-table-column label="单价" width="100">
            <template #default="scope">
              <span>¥{{ scope.row.dishPrice }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column label="描述">
            <template #default="scope">
              <span>{{ scope.row.dishDescription }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="purchase-section">
        <el-button type="primary" size="large" @click="purchasePackage">立即购买</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import GroupBuyService from '@/services/GroupBuyService';

export default {
  name: 'GroupBuyDetail',
  setup() {
    const route = useRoute();
    const router = useRouter();
    const packageDetail = ref(null);
    const loading = ref(true);

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
        console.log('套餐详情:', packageDetail.value);
        
        // 添加更详细的日志记录
        if (packageDetail.value && packageDetail.value.dishItems) {
          console.log('套餐中的菜品数量:', packageDetail.value.dishItems.length);
          packageDetail.value.dishItems.forEach((item, index) => {
            console.log(`菜品 ${index+1}:`, item);
            console.log(`菜品 ${index+1} 单价:`, item.dishPrice, typeof item.dishPrice);
            console.log(`菜品 ${index+1} 描述:`, item.dishDescription, typeof item.dishDescription);
          });
        } else {
          console.log('没有菜品数据或数据格式不正确:', packageDetail.value);
        }
      } catch (error) {
        console.error('获取套餐详情失败:', error);
      } finally {
        loading.value = false;
      }
    };

    // 返回商家页
    const goBack = () => {
      router.go(-1); // 返回上一页
    };

    // 购买套餐
    const purchasePackage = () => {
      if (packageDetail.value && packageDetail.value.id) {
        router.push({ 
          name: 'OrderConfirm', 
          params: { id: packageDetail.value.id } 
        });
      }
    };

    onMounted(() => {
      loadPackageDetail();
    });

    return {
      packageDetail,
      loading,
      goBack,
      purchasePackage
    };
  }
}
</script>

<style scoped>
.group-buy-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.navigation {
  margin-bottom: 20px;
}

.loading, .no-data {
  min-height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.package-detail {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.package-header {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.package-title {
  font-size: 28px;
  color: #333;
  margin: 0;
}

.package-meta {
  display: flex;
  align-items: center;
  gap: 20px;
}

.package-price {
  font-size: 28px;
  color: #f56c6c;
  font-weight: bold;
}

.package-sales {
  font-size: 16px;
  color: #999;
}

.package-description-section, .package-dishes-section {
  margin-bottom: 30px;
}

.purchase-section {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.purchase-section button {
  padding: 12px 40px;
  font-size: 18px;
}
</style>