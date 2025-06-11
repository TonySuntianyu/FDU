<template>
  <div class="container">
    <div class="form-card">
      <h2 class="form-title">用户登录</h2>

      <div class="form-group">
        <label class="form-label">用户名</label>
        <input type="text" v-model="username" placeholder="输入用户名" class="form-input" />
      </div>

      <div class="form-group">
        <label class="form-label">密码</label>
        <input type="password" v-model="password" placeholder="输入密码" class="form-input" />
      </div>

      <button @click="login" class="login-button">登录</button>

      <p class="register-link">
        还没有账号？<router-link to="/register" class="link">去注册</router-link>
      </p>
    </div>
  </div>
</template>

<script>
import AuthService from "../services/AuthService";

export default {
  data() {
    return {
      username: "",
      password: "",
    };
  },
  methods: {
    async login() {
      try {
        await AuthService.login(this.username, this.password);
        this.$router.push("/");
      } catch (error) {
        alert("登录失败，请检查用户名和密码！");
      }
    },
  },
};
</script>

<style scoped>
.container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: #f3f4f6;
}

.form-card {
  background-color: white;
  padding: 2rem;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  border-radius: 0.5rem;
  width: 24rem;
}

.form-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #1f2937;
  text-align: center;
}

.form-group {
  margin-top: 1rem;
}

.form-label {
  display: block;
  text-align: left;
  color: #4b5563;
}

.form-input {
  width: 100%;
  padding: 0.5rem 1rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  margin-top: 0.25rem;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.25);
}

.login-button {
  width: 100%;
  margin-top: 1.5rem;
  background-color: #3b82f6;
  color: white;
  padding: 0.5rem 0;
  border: none;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-button:hover {
  background-color: #2563eb;
}

.register-link {
  text-align: center;
  color: #4b5563;
  margin-top: 1rem;
  font-size: 0.875rem;
}

.link {
  color: #3b82f6;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}
</style>