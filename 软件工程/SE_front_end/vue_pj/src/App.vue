<template>
  <div class="app-container" :class="{'home-page': currentPath === '/'}">
    <header class="app-header" v-if="currentPath !== '/'">
      <div class="logo">
        <router-link to="/">软旦餐厅</router-link>
      </div>
      <UserStatus :current-path="currentPath" />
    </header>
    <main class="app-content" :class="{'no-header': currentPath === '/'}">
      <router-view></router-view>
    </main>
    
    <!-- New User Coupon Modal -->
    <NewUserCouponModal
      :show="showNewUserCouponModal"
      @close="showNewUserCouponModal = false"
      @received="handleCouponReceived"
      @skipped="handleCouponSkipped"
    />
  </div>
</template>

<script>
import UserStatus from '@/components/UserStatus.vue';
import NewUserCouponModal from '@/components/NewUserCouponModal.vue';
import { useRoute, useRouter } from 'vue-router';
import { computed, ref, onMounted } from 'vue';
import CouponService from '@/services/CouponService';

export default {
  name: 'App',
  components: {
    UserStatus,
    NewUserCouponModal
  },
  setup() {
    // 使用ref初始化currentPath，避免直接依赖$route
    const currentPath = ref('/');
    // 新人券弹窗显示状态
    const showNewUserCouponModal = ref(false);
    
    // 处理优惠券领取成功
    const handleCouponReceived = (couponId) => {
      console.log('用户成功领取优惠券:', couponId);
      showNewUserCouponModal.value = false;
      // 这里可以显示一个成功提示或者其他操作
    };
    
    // 处理用户跳过领取新人券
    const handleCouponSkipped = () => {
      console.log('用户跳过领取新人券');
      showNewUserCouponModal.value = false;
    };
    
    // 创建一个更新用户状态的方法
    const updateUserStatus = () => {
      console.log('触发用户状态更新');
      // 通过事件总线或其他机制通知UserStatus组件更新
      const event = new CustomEvent('update-user-status');
      window.dispatchEvent(event);
      
      // 用户状态更新后，检查是否需要显示新人券弹窗
      setTimeout(() => checkAutoShowNewUserCoupon(), 1000);
    };
    
    // 将方法挂载到window对象上，以便其他组件调用
    window.$updateUserStatus = updateUserStatus;
    
    // 检查是否需要自动显示新人券弹窗
    const checkAutoShowNewUserCoupon = async () => {
      console.log('开始检查是否需要自动显示新人券弹窗');
      try {
        const isNewUser = await CouponService.isNewUser();
        const hasReceivedCoupon = await CouponService.hasReceivedNewUserCoupon();
        
        console.log('自动检查新人券状态:', {isNewUser, hasReceivedCoupon});
        
        // 如果是新用户且未领取过新人券，自动显示弹窗
        if (isNewUser && !hasReceivedCoupon) {
          console.log('满足条件，显示新人券弹窗');
          showNewUserCouponModal.value = true;
        } else {
          console.log('不满足条件，不显示新人券弹窗');
        }
      } catch (error) {
        console.error('检查新人券状态失败:', error);
        // 错误时不显示弹窗
        showNewUserCouponModal.value = false;
      }
    };
    
    // 在onMounted生命周期钩子中安全地获取路由信息
    onMounted(() => {
      console.log('App组件挂载完成');
      try {
        const router = useRouter();
        if (router) {
          // 监听路由变化
          router.afterEach((to) => {
            currentPath.value = to.path;
            
            console.log('路由变化，当前路径:', to.path);
            // 如果用户已登录，检查是否需要显示新人券弹窗
            if (to.path !== '/' && to.path !== '/login' && to.path !== '/register') {
              console.log('非登录/注册/首页路由，检查新人券状态');
              checkAutoShowNewUserCoupon();
            }
          });
          
          // 获取当前路径
          const route = useRoute();
          if (route && route.path) {
            currentPath.value = route.path;
            console.log('初始路径:', route.path);
          }
        }
        
        // 初始化完成后，检查一次新人券状态
        setTimeout(() => checkAutoShowNewUserCoupon(), 2000);
      } catch (error) {
        console.error('路由初始化错误:', error);
      }
    });
    
    return { 
      currentPath,
      showNewUserCouponModal,
      handleCouponReceived,
      handleCouponSkipped
    };
  }
};
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  background-color: #f5f7fa;
  padding-top: 50px; /* 默认为导航栏预留空间 */
}

/* 首页时不需要为导航栏预留空间 */
.home-page + .app-content {
  padding-top: 0 !important;
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 50px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: fixed;  /* 固定在页面顶部 */
  top: 0;           /* 顶部对齐 */
  left: 0;          /* 左边对齐 */
  right: 0;         /* 右边对齐 */
  z-index: 10;      /* 确保在其他元素之上 */
  box-sizing: border-box; /* 确保padding不会影响整体宽度 */
}

/* 除了首页外的页面，需要为固定的header预留空间 */
.app-container:not(.home-page) body {
  padding-top: 50px; /* 与header高度一致 */
  height: calc(100vh - 50px); /* 可选：根据需求决定是否设置 */
}

.logo {
  font-size: 24px;
  font-weight: bold;
}

.logo a {
  color: #409EFF;
  text-decoration: none;
}

.app-content {
  flex: 1;
}

.no-header {
  padding-top: 0; /* 首页不需要为导航栏预留空间 */
}
</style>
