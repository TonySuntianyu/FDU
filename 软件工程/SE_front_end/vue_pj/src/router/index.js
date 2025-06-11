import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/Home.vue';
import LoginPage from '../views/LoginPage.vue';
import RegisterPage from '../views/RegisterPage.vue';
import ShopSearch from '../views/ShopSearch.vue';
import ShopDetail from '../views/ShopDetail.vue';
import ShopList from '../views/ShopList.vue';
import GroupBuyDetail from '../views/GroupBuyDetail.vue';
import OrderConfirm from '../views/OrderConfirm.vue';
import VoucherDetail from '../views/VoucherDetail.vue';
import MyOrders from '../views/MyOrders.vue';
import AuthService from '../services/AuthService';
import CouponWallet from '../views/CouponWallet.vue';
import InvitationPage from '../views/InvitationPage.vue';

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/login', name: 'Login', component: LoginPage },
  { path: '/register', name: 'Register', component: RegisterPage },
  { path: '/shop/search', name: 'ShopSearch', component: ShopSearch, meta: { requiresAuth: true } },
  { path: '/shop/detail/:id', name: 'ShopDetail', component: ShopDetail, meta: { requiresAuth: true } },
  { path: '/shop/list', name: 'ShopList', component: ShopList, meta: { requiresAuth: true } },
  { path: '/groupbuy/:id', name: 'GroupBuyDetail', component: GroupBuyDetail, meta: { requiresAuth: true } },
  { path: '/order/confirm/:id', name: 'OrderConfirm', component: OrderConfirm, meta: { requiresAuth: true } },
  { path: '/voucher/:id', name: 'VoucherDetail', component: VoucherDetail, meta: { requiresAuth: true } },
  { path: '/orders', name: 'MyOrders', component: MyOrders, meta: { requiresAuth: true } },
  { path: '/coupons', name: 'CouponWallet', component: CouponWallet, meta: { requiresAuth: true } },
  { path: '/invitation', name: 'InvitationPage', component: InvitationPage, meta: { requiresAuth: true } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 检查路由是否需要身份验证
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  
  // 当前用户是否已登录 - 检查两种方法
  // 1. 从Vuex store获取登录状态
  const storeAuth = window.$store && window.$store.getters['auth/isAuthenticated'];
  // 2. 从AuthService获取（兼容旧方式）
  const serviceAuth = !!AuthService.getUser();
  
  const isLoggedIn = storeAuth || serviceAuth;
  console.log('路由守卫 - 需要认证:', requiresAuth, '是否已登录:', isLoggedIn);
  
  // 如果需要身份验证且用户未登录，则重定向到登录页面
  if (requiresAuth && !isLoggedIn) {
    console.log('未登录，重定向到登录页面');
    next({ name: 'Login' });
  } else {
    console.log('允许访问:', to.path);
    next(); // 其他情况正常导航
  }
});

export default router;
