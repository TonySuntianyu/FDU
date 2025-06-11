<template>
  <div class="shop-detail-container">
    <div v-if="loading" class="loading">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="!shop" class="no-data">
      <h2>未找到商家信息</h2>
      <el-button type="primary" @click="goBack">返回搜索页</el-button>
    </div>

    <div v-else class="shop-detail">
      <!-- 返回导航 -->
      <div class="navigation">
        <el-button icon="el-icon-arrow-left" @click="goBack">返回搜索页</el-button>
      </div>

      <!-- 商家基本信息 -->
      <div class="shop-header">
        <div class="shop-title-area">
          <h1 class="shop-name">{{ shop.name }}</h1>
          <el-tag v-if="shop.category" size="medium" type="success">{{ shop.category.name || shop.categoryName }}</el-tag>
        </div>

        <div class="shop-rating">
          <el-rate v-model="shop.rating" disabled text-color="#ff9900" />
          <span class="rating-value">{{ shop.rating }}分</span>
        </div>

        <div class="price-info">
          <div class="price-item">
            <span class="price-label">价格区间</span>
            <span class="price-value">¥{{ shop.priceMin }} - ¥{{ shop.priceMax }}</span>
          </div>
          <div class="price-item">
            <span class="price-label">人均消费</span>
            <span class="price-value">¥{{ shop.averageCost }}</span>
          </div>
        </div>

        <div v-if="shop.description" class="shop-description">
          <h3>商家介绍</h3>
          <p>{{ shop.description }}</p>
        </div>
      </div>

      <!-- 商家联系信息 -->
      <div class="contact-info">
        <div class="info-item">
          <i class="el-icon-location"></i>
          <span>{{ shop.address || '暂无地址信息' }}</span>
        </div>
        <div class="info-item">
          <i class="el-icon-time"></i>
          <span>{{ shop.businessHours || '暂无营业时间信息' }}</span>
        </div>
        <div class="info-item">
          <i class="el-icon-phone"></i>
          <span>{{ shop.phone || '暂无联系电话' }}</span>
        </div>
      </div>

      <!-- 商家图片展示 -->
      <div class="shop-images-section">
        <h2>商家图片</h2>

        <div v-if="!images || images.length === 0" class="no-images">
          <el-empty description="暂无商家图片" />
        </div>

        <div v-else>
          <!-- 精选图片轮播 -->
          <div class="featured-images">
            <h3>精选展示</h3>
            <el-carousel :interval="4000" type="card" height="300px">
              <el-carousel-item v-for="(image, index) in images" :key="index">
                <div class="carousel-item">
                  <img :src="`http://localhost:8088${image.imageUrl}`" :alt="image.description || '商家图片'" class="carousel-image">
                  <div class="image-description" v-if="image.description">{{ image.description }}</div>
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>

          <!-- 按类别分组展示图片 -->
          <div class="image-categories">
            <!-- 店面环境图片 -->
            <div v-if="getImagesByType('门店外观').length > 0 || getImagesByType('店内环境').length > 0" class="image-category">
              <h3>店铺环境</h3>
              <div class="image-grid">
                <div
                  v-for="(image, index) in [...getImagesByType('门店外观'), ...getImagesByType('店内环境')]"
                  :key="'env-'+index"
                  class="grid-item"
                  @click="previewImage(image.imageUrl)"
                >
                  <img :src="`http://localhost:8088${image.imageUrl}`" :alt="image.description || '环境图片'">
                  <div class="image-tag">{{ image.description || '环境图片' }}</div>
                </div>
              </div>
            </div>

            <!-- 菜品图片 -->
            <div v-if="getImagesByType('招牌菜品').length > 0 || getImagesByType('特色美食').length > 0" class="image-category">
              <h3>特色菜品</h3>
              <div class="image-grid">
                <div
                  v-for="(image, index) in [...getImagesByType('招牌菜品'), ...getImagesByType('特色美食')]"
                  :key="'food-'+index"
                  class="grid-item"
                  @click="previewImage(image.imageUrl)"
                >
                <img :src="`http://localhost:8088${image.imageUrl}`" :alt="image.description || '菜品图片'">
                  <div class="image-tag">{{ image.description || '菜品图片' }}</div>
                </div>
              </div>
            </div>

            <!-- 其他图片 -->
            <div v-if="getImagesByType('其他').length > 0" class="image-category">
              <h3>其他图片</h3>
              <div class="image-grid">
                <div
                  v-for="(image, index) in getImagesByType('其他')"
                  :key="'other-'+index"
                  class="grid-item"
                  @click="previewImage(image.imageUrl)"
                >
                <img :src="`http://localhost:8088${image.imageUrl}`" :alt="image.description || '其他图片'">
                  <div class="image-tag">{{ image.description || '图片' }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 团购套餐展示 -->
      <div class="group-buy-packages">
        <h2>团购套餐</h2>
        
        <div v-if="loading || loadingPackages" class="loading-packages">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="!packages || packages.length === 0" class="no-packages">
          <el-empty description="暂无团购套餐" />
        </div>
        
        <div v-else class="package-list">
          <el-card v-for="pkg in packages" :key="pkg.id" class="package-item" @click="viewPackageDetail(pkg.id)">
            <div class="package-content">
              <div class="package-title">{{ pkg.title }}</div>
              <div class="package-description">{{ pkg.description }}</div>
              <div class="package-price-sales">
                <span class="package-price">¥{{ pkg.price }}</span>
                <span class="package-sales">已售 {{ pkg.sales }}</span>
              </div>
              <el-button type="primary" size="small" @click.stop="viewPackageDetail(pkg.id)">查看详情</el-button>
            </div>
          </el-card>
        </div>
      </div>
      
      <!-- 评论区域 -->
      <div class="shop-reviews">
        <h2>商户评论</h2>
        
        <div class="review-form">
          <h3>发表评论</h3>
          <el-form @submit.prevent="submitReview">
            <el-form-item>
              <el-input
                v-model="newReview.content"
                type="textarea"
                :rows="4"
                placeholder="请输入您的评论，至少15个字..."
                maxlength="1000"
                show-word-limit
              ></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitReview" :disabled="!isReviewValid">发表评论</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div v-if="loadingReviews" class="loading-reviews">
          <el-skeleton :rows="5" animated />
        </div>
        
        <div v-else-if="!reviews || reviews.length === 0" class="no-reviews">
          <el-empty description="暂无评论" />
          <!-- 评论状态提示 -->
          <div class="review-status" style="margin-top: 10px; padding: 10px; background-color: #f0f9eb; border: 1px solid #e1f3d8; color: #67c23a; border-radius: 4px; font-size: 12px;">
            <p>当前商户ID: {{ route.params.id }}</p>
            <p>评论加载状态: {{ loadingReviews ? '加载中' : '加载完成' }}</p>
            <p>评论数量: {{ reviews.length }}</p>
          </div>
        </div>
        
        <div v-else class="review-list">
          <!-- 评论状态提示 -->
          <div class="review-status" style="margin-bottom: 15px; padding: 10px; background-color: #f0f9eb; border: 1px solid #e1f3d8; color: #67c23a; border-radius: 4px; font-size: 12px;">
            <p>当前商户ID: {{ route.params.id }}</p>
            <p>评论加载状态: {{ loadingReviews ? '加载中' : '加载完成' }}</p>
            <p>评论数量: {{ reviews.length }}</p>
            <p v-if="reviews[0]">第一条评论ID: {{ reviews[0].id }}，用户: {{ getUsernameById(reviews[0].userId) }}</p>
          </div>
          
          <!-- 顶层评论列表 -->
          <template v-for="review in reviews" :key="review.id">
            <div class="review-item" v-if="review">
              <div class="review-header">
                <span class="user-id">用户: {{ getUsernameById(review.userId) }}</span>
                <span class="review-time">{{ formatTime(review.createTime) }}</span>
              </div>
              <div class="review-content">{{ review.content }}</div>
              
              <!-- 回复按钮 -->
              <div class="review-actions">
                <el-button type="text" @click="startReply(review.id)">回复</el-button>
              </div>
              
              <!-- 回复表单 -->
              <div v-if="replyingTo === review.id" class="reply-form">
                <el-input
                  v-model="newReply.content"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入您的回复，至少15个字..."
                  maxlength="1000"
                  show-word-limit
                ></el-input>
                <div class="reply-actions">
                  <el-button type="primary" size="small" @click="submitReply(review.id)" :disabled="!isReplyValid">提交回复</el-button>
                  <el-button size="small" @click="cancelReply">取消</el-button>
                </div>
              </div>
              
              <!-- 子回复(嵌套评论) -->
              <div v-if="review.replies && review.replies.length > 0" class="nested-replies">
                <template v-for="reply in review.replies" :key="reply.id">
                  <div v-if="reply" class="nested-reply-item">
                    <nested-reply 
                      :reply="reply" 
                      :merchant-id="Number(route.params.id)"
                      @reply-added="loadReviews"
                    />
                  </div>
                </template>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="showViewer"
      :url-list="[previewUrl]"
      @close="closeViewer"
    />
  </div>
</template>

<script>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElImageViewer, ElMessage } from 'element-plus';
import ShopService from '@/services/ShopService';
import GroupBuyService from '@/services/GroupBuyService';
import ReviewService from '@/services/ReviewService';
import NestedReply from '@/components/NestedReply.vue';
import AuthService from '@/services/AuthService';

export default {
  name: 'ShopDetail',
  components: {
    ElImageViewer,
    NestedReply: NestedReply
  },

  setup() {
    const route = useRoute();
    const router = useRouter();

    const shop = ref(null);
    const images = ref([]);
    const packages = ref([]);
    const loading = ref(true);
    const loadingPackages = ref(true);
    const showViewer = ref(false);
    const previewUrl = ref('');
    
    // 评论相关
    const reviews = ref([]);
    const loadingReviews = ref(true);
    const newReview = ref({ content: '' });
    const newReply = ref({ content: '' });
    const replyingTo = ref(null);
    
    // 计算属性，验证评论是否有效
    const isReviewValid = computed(() => {
      return newReview.value.content && newReview.value.content.length >= 15;
    });
    
    // 计算属性，验证回复是否有效
    const isReplyValid = computed(() => {
      return newReply.value.content && newReply.value.content.length >= 15;
    });
    
    // 格式化时间
    const formatTime = (timestamp) => {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return date.toLocaleString();
    };

    // 获取商家详情
    const loadShopDetails = async () => {
      const shopId = route.params.id;
      if (!shopId) {
        loading.value = false;
        return;
      }

      try {
        const response = await ShopService.getShopDetails(shopId);
        console.log('商家详情响应:', response);

        // 直接处理服务器返回的数据
        if (response && response.shop) {
          shop.value = response.shop;
          images.value = response.images || [];
          console.log('商家图片:', images.value);
        } else {
          console.error('商家详情数据格式不符合预期:', response);
        }
      } catch (error) {
        console.error('获取商家详情失败:', error);
      } finally {
        loading.value = false;
      }
    };

    // 返回搜索页
    const goBack = () => {
      router.push({ name: 'ShopSearch' });
    };

    // 按类型获取图片
    const getImagesByType = (type) => {
      if (!images.value || images.value.length === 0) return [];

      const typeMap = {
        '门店外观': ['门店外观', '店铺外观', '门面'],
        '店内环境': ['店内环境', '店内', '环境'],
        '招牌菜品': ['招牌菜品', '招牌菜', '特色菜'],
        '特色美食': ['特色美食', '菜品', '美食'],
        '其他': []
      };

      if (type === '其他') {
        // 返回不属于任何已知类别的图片
        const allKnownTypes = [].concat(...Object.values(typeMap).filter(arr => arr.length > 0));
        return images.value.filter(img =>
          !img.description ||
          !allKnownTypes.some(knownType =>
            img.description.includes(knownType)
          )
        );
      }

      // 根据类型返回图片
      return images.value.filter(img =>
        img.description &&
        typeMap[type].some(keyword => img.description.includes(keyword))
      );
    };

    // 预览图片
    const previewImage = (url) => {
      previewUrl.value = `http://localhost:8088${url}`;
      showViewer.value = true;
    };

    // 关闭预览
    const closeViewer = () => {
      showViewer.value = false;
    };

    // 获取团购套餐
    const loadPackages = async () => {
      if (!shop.value || !shop.value.id) return;
      
      loadingPackages.value = true;
      try {
        const response = await GroupBuyService.getPackagesByShopId(shop.value.id);
        console.log('团购套餐响应:', response);
        packages.value = response || [];
      } catch (error) {
        console.error('获取团购套餐失败:', error);
      } finally {
        loadingPackages.value = false;
      }
    };

    // 查看套餐详情
    const viewPackageDetail = (packageId) => {
      router.push({ name: 'GroupBuyDetail', params: { id: packageId } });
    };
    
    // 获取评论列表
    const loadReviews = async () => {
      const shopId = route.params.id;
      if (!shopId) return;
      
      loadingReviews.value = true;
      
      try {
        console.log('【评论加载】开始加载商户ID=', shopId, '的评论');
        
        // 尝试使用标准API获取评论
        const reviewsData = await ReviewService.getReviewsByMerchant(shopId);
        
        // 检查是否成功获取到评论
        if (Array.isArray(reviewsData) && reviewsData.length > 0) {
          console.log(`【评论加载】成功通过标准API获取到${reviewsData.length}条评论`);
          reviews.value = reviewsData;
        } else {
          console.log('【评论加载】标准API未获取到评论，尝试使用测试API');
          
          // 如果标准API没有返回评论，尝试使用测试API
          try {
            const testReviewsData = await ReviewService.getAllReviewsByMerchant(shopId);
            if (Array.isArray(testReviewsData) && testReviewsData.length > 0) {
              console.log(`【评论加载】成功通过测试API获取到${testReviewsData.length}条评论`);
              reviews.value = testReviewsData;
            } else {
              console.log('【评论加载】测试API也未获取到评论');
              reviews.value = [];
            }
          } catch (testError) {
            console.error('【评论加载】测试API调用失败:', testError);
            reviews.value = [];
          }
        }
      } catch (error) {
        console.error('【评论加载】获取评论列表失败:', error);
        ElMessage.error('获取评论失败: ' + (error.message || '未知错误'));
        
        // 失败后尝试使用测试API
        try {
          console.log('【评论加载】尝试使用备用测试API');
          const testReviewsData = await ReviewService.getAllReviewsByMerchant(shopId);
          if (Array.isArray(testReviewsData)) {
            console.log(`【评论加载】备用API获取到${testReviewsData.length}条评论`);
            reviews.value = testReviewsData;
          } else {
            reviews.value = [];
          }
        } catch (testError) {
          console.error('【评论加载】备用API也失败:', testError);
          reviews.value = [];
        }
      } finally {
        loadingReviews.value = false;
        console.log(`【评论加载】评论加载完成，共${reviews.value.length}条评论`);
      }
    };
    
    // 提交评论
    const submitReview = async () => {
      if (!isReviewValid.value) {
        ElMessage.error('评论内容至少需要15个字');
        return;
      }
      
      try {
        console.log('【评论提交】开始提交新评论');
        console.log('【评论提交】商户ID:', route.params.id);
        
        const response = await ReviewService.createReview({
          merchantId: Number(route.params.id),
          content: newReview.value.content
        });
        
        console.log('【评论提交】提交成功，响应:', response);
        
        if (response) {
          ElMessage.success('评论发布成功');
          newReview.value.content = '';
          
          // 使用改进后的loadReviews方法重新加载评论
          console.log('【评论提交】评论发布成功，重新加载评论');
          await loadReviews();
        }
      } catch (error) {
        console.error('【评论提交】提交失败:', error);
        ElMessage.error('发布评论失败: ' + (error.message || '未知错误'));
      }
    };
    
    // 开始回复
    const startReply = (reviewId) => {
      replyingTo.value = reviewId;
      newReply.value.content = '';
    };
    
    // 提交回复
    const submitReply = async (reviewId) => {
      if (!isReplyValid.value) {
        ElMessage.error('回复内容至少需要15个字');
        return;
      }
      
      try {
        console.log('【回复提交】开始提交回复，回复评论ID:', reviewId);
        
        const response = await ReviewService.createReview({
          merchantId: Number(route.params.id),
          content: newReply.value.content,
          parentId: reviewId
        });
        
        console.log('【回复提交】提交成功，响应:', response);
        
        if (response) {
          ElMessage.success('回复发布成功');
          newReply.value.content = '';
          replyingTo.value = null;
          
          // 使用改进后的loadReviews方法重新加载评论
          console.log('【回复提交】回复发布成功，重新加载评论');
          await loadReviews();
        }
      } catch (error) {
        console.error('【回复提交】提交失败:', error);
        ElMessage.error('发布回复失败: ' + (error.message || '未知错误'));
      }
    };
    
    // 取消回复
    const cancelReply = () => {
      replyingTo.value = null;
      newReply.value.content = '';
    };

    // 根据用户ID获取用户名
    const getUsernameById = (userId) => {
      if (!userId) return '未知用户';
      
      // 获取当前登录用户
      const currentUser = AuthService.getUser();
      
      // 如果是当前登录用户，显示"我"
      if (currentUser && currentUser.id === userId) {
        return currentUser.username || '我';
      }
      
      // 这里应该调用获取用户信息的API，但为简化处理，直接返回用户名
      // 在实际项目中，可以维护一个用户信息缓存或调用后端API获取用户名
      return `用户${userId}`;
    };

    // 页面加载时获取数据
    onMounted(async () => {
      console.log('【组件生命周期】组件挂载开始');
      
      // 1. 加载商家详情
      await loadShopDetails();
      console.log('【数据加载】商家详情加载完成');
      
      // 2. 加载团购套餐
      await loadPackages();
      console.log('【数据加载】团购套餐加载完成');
      
      // 3. 手动加载评论 - 不使用loadReviews方法，直接实现
      console.log('【评论加载】开始手动加载评论数据');
      try {
        // 获取商户ID
        const shopId = route.params.id;
        if (!shopId) {
          console.error('【评论加载】错误: 缺少商户ID，无法加载评论');
          return;
        }
        console.log(`【评论加载】当前商户ID: ${shopId}`);
        
        // 设置加载状态
        loadingReviews.value = true;
        
        // 直接从ReviewService获取数据
        console.log('【API调用】调用ReviewService.getReviewsByMerchant');
        const reviewsData = await ReviewService.getReviewsByMerchant(shopId);
        console.log('【API调用】评论API调用完成，检查返回数据');
        
        // 检查并处理数据
        if (Array.isArray(reviewsData)) {
          console.log(`【评论处理】收到${reviewsData.length}条评论数据`);
          // 为页面数据赋值
          reviews.value = reviewsData;
          console.log('【评论处理】数据已赋值给reviews变量');
        } else {
          console.error('【评论处理】错误: API返回的不是数组:', reviewsData);
          reviews.value = [];
        }
      } catch (error) {
        console.error('【评论加载】加载评论出错:', error);
        reviews.value = [];
      } finally {
        // 更新加载状态
        loadingReviews.value = false;
        console.log(`【评论加载】评论加载完成，共${reviews.value.length}条评论`);
      }
      
      console.log('【组件生命周期】组件挂载完成');
    });

    // 监听店铺ID变化，重新加载数据
    watch(() => route.params.id, async (newId, oldId) => {
      if (newId && newId !== oldId) {
        console.log(`【路由变化】商户ID从 ${oldId} 变为 ${newId}`);
        
        // 显示加载状态
        loading.value = true;
        
        // 1. 加载商家详情
        await loadShopDetails();
        console.log('【数据加载】商家详情已更新');
        
        // 2. 加载团购套餐
        await loadPackages();
        console.log('【数据加载】团购套餐已更新');
        
        // 3. 手动加载评论 - 不使用loadReviews方法
        console.log('【评论加载】开始手动加载评论数据');
        try {
          // 设置加载状态
          loadingReviews.value = true;
          
          // 直接从ReviewService获取数据
          console.log('【API调用】调用ReviewService.getReviewsByMerchant');
          const reviewsData = await ReviewService.getReviewsByMerchant(newId);
          console.log('【API调用】评论API调用完成');
          
          // 检查并处理数据
          if (Array.isArray(reviewsData)) {
            console.log(`【评论处理】收到${reviewsData.length}条评论数据`);
            // 为页面数据赋值
            reviews.value = reviewsData;
          } else {
            console.error('【评论处理】错误: API返回的不是数组:', reviewsData);
            reviews.value = [];
          }
        } catch (error) {
          console.error('【评论加载】加载评论出错:', error);
          reviews.value = [];
        } finally {
          // 更新加载状态
          loadingReviews.value = false;
          loading.value = false;
          console.log(`【评论加载】评论加载完成，共${reviews.value.length}条评论`);
        }
      }
    });

    return {
      shop,
      images,
      packages,
      loading,
      loadingPackages,
      showViewer,
      previewUrl,
      reviews,
      loadingReviews,
      newReview,
      newReply,
      replyingTo,
      isReviewValid,
      isReplyValid,
      route,
      goBack,
      getImagesByType,
      previewImage,
      closeViewer,
      viewPackageDetail,
      loadReviews,
      submitReview,
      startReply,
      submitReply,
      cancelReply,
      formatTime,
      getUsernameById
    };
  }
};
</script>

<style scoped>
.shop-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

.navigation {
  margin-bottom: 20px;
}

.loading, .no-data {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.shop-header {
  margin-bottom: 30px;
  padding: 25px;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.shop-title-area {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.shop-name {
  font-size: 28px;
  font-weight: bold;
  margin: 0;
  margin-right: 15px;
}

.shop-rating {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.rating-value {
  margin-left: 8px;
  color: #ff9900;
  font-weight: bold;
  font-size: 18px;
}

.price-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
}

.price-item {
  display: flex;
  flex-direction: column;
}

.price-label {
  color: #606266;
  font-size: 14px;
  margin-bottom: 5px;
}

.price-value {
  font-weight: bold;
  color: #f56c6c;
  font-size: 18px;
}

.shop-description {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.shop-description h3 {
  font-size: 16px;
  margin-bottom: 10px;
  color: #303133;
}

.shop-description p {
  color: #606266;
  line-height: 1.6;
  white-space: pre-line;
}

.contact-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 30px;
  padding: 20px;
  border-radius: 8px;
  background-color: #f8f9fa;
}

.info-item {
  display: flex;
  align-items: center;
  margin-right: 20px;
}

.info-item i {
  margin-right: 8px;
  font-size: 20px;
  color: #409EFF;
}

.shop-images-section {
  margin-bottom: 40px;
}

.shop-images-section h2 {
  font-size: 22px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409EFF;
}

.shop-images-section h3 {
  font-size: 18px;
  margin: 15px 0;
  color: #303133;
}

.featured-images {
  margin-bottom: 30px;
}

.carousel-item {
  position: relative;
  height: 100%;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.image-description {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.6);
  color: white;
  padding: 8px 15px;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
}

.image-categories {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.image-category {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.grid-item {
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
  height: 200px;
}

.grid-item:hover {
  transform: scale(1.03);
}

.grid-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-tag {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.6);
  color: white;
  padding: 5px 10px;
  font-size: 12px;
  text-align: center;
}

.no-images {
  padding: 40px;
  background-color: #f8f9fa;
  border-radius: 8px;
  text-align: center;
}

.group-buy-packages {
  margin-top: 30px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.package-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.package-item {
  cursor: pointer;
  transition: transform 0.3s;
}

.package-item:hover {
  transform: translateY(-5px);
}

.package-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.package-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.package-description {
  font-size: 14px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.package-price-sales {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 10px 0;
}

.package-price {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
}

.package-sales {
  font-size: 14px;
  color: #999;
}

.loading-packages, .no-packages {
  padding: 30px;
  text-align: center;
}

.shop-reviews {
  margin-top: 30px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.shop-reviews h2 {
  font-size: 22px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409EFF;
  color: #333;
}

.review-form {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #e6e6e6;
}

.review-form h3 {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
}

.review-list {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  margin-bottom: 25px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border-left: 4px solid #409EFF;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.user-id {
  font-size: 14px;
  color: #606266;
  font-weight: bold;
}

.review-time {
  font-size: 12px;
  color: #999;
}

.review-content {
  margin: 15px 0;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 15px;
}

.review-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.reply-form {
  margin-top: 15px;
  padding: 15px;
  background-color: #f0f2f5;
  border-radius: 8px;
  border: 1px solid #e6e6e6;
}

.reply-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.nested-replies {
  margin-top: 15px;
  margin-left: 20px;
  padding: 10px;
  border-left: 2px solid #ddd;
  background-color: rgba(240, 240, 240, 0.5);
  border-radius: 0 8px 8px 0;
}

.nested-reply-item {
  margin-bottom: 10px;
}

.loading-reviews, .no-reviews {
  padding: 30px;
  text-align: center;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px dashed #ddd;
}
</style>