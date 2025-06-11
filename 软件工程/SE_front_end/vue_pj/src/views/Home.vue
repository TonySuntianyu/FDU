<!--
  这是一个Vue组件的模板部分，用于定义组件的HTML结构。
  该组件为应用的主页，提供登录和注册的入口。
-->
<template>
  <!--
    外层容器，使用类名 "centered-container"。
    该容器的作用是将内部内容垂直和水平居中显示。
  -->
  <div class="centered-container" >
    <!--
      登录框容器，使用类名 "login-box"。
      包含欢迎标题和登录、注册链接。
    -->
    <div v-if="!isLoggedIn" class="login-box">
      <!-- 显示欢迎信息，告知用户欢迎来到 "软旦餐厅" -->
      <h1>欢迎来到"软旦餐厅"</h1>
      <!-- 段落元素，用于包裹登录和注册链接 -->
      <p>
        <!--
          路由链接组件，使用类名 "custom-link"。
          点击该链接将导航到登录页面，路径为 "/login"。
        -->
        <router-link to="/login" class="button">登录</router-link>
        <!--
          路由链接组件，使用类名 "custom-link"。
          点击该链接将导航到注册页面，路径为 "/register"。
        -->
        <router-link to="/register" class="button">注册</router-link>
        
      </p>
    </div>
    
    <!-- 登录后显示 -->
    <div v-else class="home-menu">
      <h1>软旦餐厅</h1>
      <p class="welcome-text">欢迎回来，{{ currentUser?.username || '用户' }}</p>
      
      <div class="menu-grid">
        <router-link to="/shop/list" class="menu-item">
          <div class="menu-icon">
            <i class="el-icon-s-shop"></i>
          </div>
          <div class="menu-title">餐厅列表</div>
        </router-link>
        
        <router-link to="/shop/search" class="menu-item">
          <div class="menu-icon">
            <i class="el-icon-search"></i>
          </div>
          <div class="menu-title">搜索餐厅</div>
        </router-link>
        
        <router-link to="/orders" class="menu-item">
          <div class="menu-icon">
            <i class="el-icon-s-order"></i>
          </div>
          <div class="menu-title">我的订单</div>
        </router-link>
        
        <router-link to="/coupons" class="menu-item">
          <div class="menu-icon">
            <i class="el-icon-s-ticket"></i>
          </div>
          <div class="menu-title">优惠券</div>
        </router-link>
        
        <router-link to="/invitation" class="menu-item">
          <div class="menu-icon">
            <i class="el-icon-present"></i>
          </div>
          <div class="menu-title">邀请有礼</div>
        </router-link>
      </div>
      
      <div class="logout-section">
        <button @click="handleLogout" class="logout-button">退出登录</button>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import AuthService from '@/services/AuthService';

// 导出一个默认的Vue组件对象
export default {
  // 组件的名称，用于在其他地方引用和调试
  name: 'HomePage',
  setup() {
    const router = useRouter();
    
    // 计算用户是否已登录
    const isLoggedIn = computed(() => {
      return !!AuthService.getUser();
    });
    
    // 获取当前登录用户信息
    const currentUser = computed(() => {
      return AuthService.getUser();
    });
    
    // 处理登出逻辑
    const handleLogout = () => {
      AuthService.logout();
      router.push('/login');
    };
    
    return {
      isLoggedIn,
      currentUser,
      handleLogout
    };
  }
}
</script>

<style>
/*
  全局样式，对 body 和 html 元素进行样式设置。
  去除默认的外边距和内边距，使元素充满整个页面。
  设置高度和宽度为 100%，确保页面占满整个可视区域。
*/
body, html {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
}

/*
  对 body 元素设置背景样式。
  使用背景图片 "/Home_BG.jpg" 作为页面的背景。
  background-size: cover 使背景图片覆盖整个页面。
  background-position: center 使背景图片居中显示。
  background-repeat: no-repeat 防止背景图片重复显示。
  background-attachment: fixed 使背景图片固定，不随页面滚动而滚动。
*/

body {
  background-image: url('/Home_BG.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;

}

/*
  定义类名为 "centered-container" 的样式。
  使用 flex 布局，将内部元素垂直和水平居中显示。
  min-height: 100vh 确保容器至少占满整个视口的高度。
*/
.centered-container { 
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  min-height: 100vh;
}

/*
  定义类名为 "login-box" 的样式。
  设置边框为 2px 宽的灰色边框，边框圆角为 20px。
  内边距为 40px，使内容与边框之间有一定的间距。
  文本居中显示。
  背景颜色为半透明的浅灰色，增加视觉效果。
  添加阴影效果，使登录框有立体感。
  宽度为 350px，固定登录框的宽度。
*/
.login-box {
  border: 2px solid #ccc;
  border-radius: 20px;
  padding: 40px;
  text-align: center;
  background-color: rgba(240, 240, 240, 0.9); /* 半透明背景 */
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  width: 350px;
}

/*
  定义类名为 "custom-link" 的样式。
  设置字体大小为 24px。
  文本颜色为 #1c2561。
  去除下划线，使链接看起来更简洁。
*/
.custom-link {
  font-size: 24px;
  color: #1c2561;
  text-decoration: none;
}

/*
  定义类名为 "custom-link" 的链接在鼠标悬停时的样式。
  当鼠标悬停在链接上时，显示下划线，提示用户该元素可点击。
*/
.custom-link:hover {
  text-decoration: underline;
}

/* 按钮样式 */
.button {
  display: inline-block;
  padding: 10px 20px;
  margin: 10px 5px;
  font-size: 18px;
  color: #fff;
  background-color: #409eff;
  border-radius: 5px;
  text-decoration: none;
  transition: background-color 0.3s;
}

.button:hover {
  background-color: #66b1ff;
}

/* 登录后的样式 */
.home-menu {
  border-radius: 20px;
  padding: 40px;
  text-align: center;
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  width: 80%;
  max-width: 800px;
}

.home-menu h1 {
  color: #409eff;
  margin-bottom: 10px;
}

.welcome-text {
  font-size: 18px;
  color: #666;
  margin-bottom: 30px;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background-color: #f2f6fc;
  border-radius: 10px;
  text-decoration: none;
  color: #333;
  transition: all 0.3s;
}

.menu-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  background-color: #ecf5ff;
}

.menu-icon {
  font-size: 36px;
  color: #409eff;
  margin-bottom: 10px;
}

.menu-title {
  font-size: 16px;
  font-weight: bold;
}

.logout-section {
  margin-top: 20px;
}

.logout-button {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.logout-button:hover {
  background-color: #f78989;
}
</style>