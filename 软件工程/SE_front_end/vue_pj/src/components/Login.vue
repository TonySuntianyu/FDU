<template>
  <div class="login-container">
    <h2>用户登录</h2>
    <form @submit.prevent="login">
      <label>用户名:</label>
      <input type="text" v-model="username" required />

      <label>密码:</label>
      <input type="password" v-model="password" required />

      <button type="submit">登录</button>
      <span v-if="loginError" class="error">{{ loginError }}</span>
    </form>
  </div>
</template>

<script>
import AuthService from "@/services/AuthService";

export default {
  data() {
    return {
      username: "",
      password: "",
      loginError: "",
    };
  },
  methods: {
    async login() {
      this.loginError = ""; // 清除之前的错误信息
      console.log('登录表单提交');
      
      try {
        // 登录前显示加载状态
        const loadingMessage = "正在登录...";
        console.log(loadingMessage);
        
        // 调用登录服务
        const response = await AuthService.login(this.username, this.password);
        console.log('登录响应:', response);
        
        if (response && response.success) {
          // 如果响应中包含token，保存它
          if (response.token) {
            console.log('保存token到store');
            this.$store.dispatch('auth/saveToken', response.token);
            // 同时保存用户名
            this.$store.dispatch('auth/saveUser', { username: this.username });
          }
          
          // 登录成功后跳转到首页
          console.log('登录成功，跳转到首页');
          this.$router.push("/");
        } else {
          // 显示登录失败的消息
          this.loginError = response.message || "登录失败";
          console.error('登录失败:', this.loginError);
        }
      } catch (error) {
        console.error('登录异常:', error);
        this.loginError = error.message || "登录失败，请检查用户名和密码";
      }
    },
  },
};
</script>

<style scoped>
.login-container {
  max-width: 400px;
  margin: auto;
  padding: 20px;
}
.error {
  color: red;
}
</style>
