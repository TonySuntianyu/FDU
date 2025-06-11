<template>
  <div class="container">
    <div class="form-card">
      <h2 class="form-title">用户注册</h2>

      <!-- 用户名输入框 -->
      <div class="form-group">
        <label class="form-label">用户名</label>
        <input type="text" v-model="username" placeholder="输入用户名" class="form-input" />
        <p v-if="usernameError" class="error-message">{{ usernameError }}</p>
      </div>

      <!-- 密码输入框 -->
      <div class="form-group">
        <label class="form-label">密码</label>
        <input type="password" v-model="password" placeholder="输入密码" class="form-input" @input="checkPasswordStrength" />
        <p class="strength-message" :class="passwordStrengthClass">{{ passwordStrength }}</p>
      </div>

      <!-- 确认密码输入框 -->
      <div class="form-group">
        <label class="form-label">确认密码</label>
        <input type="password" v-model="confirmPassword" placeholder="确认密码" class="form-input" />
      </div>

      <!-- 验证码输入框 -->
      <div class="form-group">
        <label class="form-label">验证码</label>
        <div class="captcha-container">
          <input type="text" v-model="captchaInput" placeholder="输入验证码" class="form-input captcha-input" />
          <img :src="captchaImage" alt="验证码" class="captcha-image" @click="refreshCaptcha" />
        </div>
        <p v-if="captchaError" class="error-message">{{ captchaError }}</p>
      </div>

      <!-- 注册按钮 -->
      <button @click="register" class="register-button">注册</button>

      <!-- 跳转到登录页面 -->
      <p class="login-link">
        已有账号？<router-link to="/login" class="link">去登录</router-link>
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
      confirmPassword: "",
      usernameError: "",
      passwordStrength: "",
      passwordStrengthClass: "default",
      captchaInput: "",
      captchaImage: "",
      captchaError: "",
      captchaId: "" // 存储验证码 ID，用于验证
    };
  },
  mounted() {
    this.getCaptcha();  // 页面加载时获取验证码
  },
  methods: {
    async register() {
      // 重置错误信息
      this.usernameError = "";
      this.captchaError = "";

      // 验证密码
      if (this.password !== this.confirmPassword) {
        alert("两次密码输入不一致！");
        return;
      }

      // 验证用户名
      if (this.username === "") {
        this.usernameError = "用户名不能为空！";
        return;
      }

      // 验证验证码
      try {
        await AuthService.verifyCaptcha(this.captchaInput, this.captchaId);  // 验证验证码
      } catch (error) {
        this.captchaError = "验证码不正确";
        return;
      }

      // RegisterPage.vue 中的注册逻辑
  try {
    await AuthService.register(
      this.username,
      this.password,
      this.captchaInput,
      this.captchaId
    );
    alert("注册成功，请登录！");
    this.$router.push("/login");
  } catch (error) {
    // 直接显示 error.message（已包含后端返回的 msg）
    alert(error.message); // 例如："注册失败，用户名已存在"
  }
  },

    checkPasswordStrength() {
      // 密码强度检查
      if (this.password.length < 6) {
        this.passwordStrength = "密码太短";
        this.passwordStrengthClass = "weak";
      } else if (/\d/.test(this.password) && /[a-zA-Z]/.test(this.password)) {
        this.passwordStrength = "密码强度：中";
        this.passwordStrengthClass = "medium";
      } else if (this.password.length >= 10) {
        this.passwordStrength = "密码强度：强";
        this.passwordStrengthClass = "strong";
      } else {
        this.passwordStrength = "密码强度：弱";
        this.passwordStrengthClass = "weak";
      }
    },

    // 获取验证码图片
    async getCaptcha() {
      try {
        const response = await AuthService.getCaptcha();  // 从后端获取验证码图片
        console.log('验证码接口返回数据:', response);

        // 确保从后端正确获取验证码数据，并更新 captchaImage 和 captchaId
        if (response ) {
          const { captchaImage, captchaId } = response;

          if (captchaImage && captchaId) {
            this.captchaImage = captchaImage;  // 设置 Base64 图片
            this.captchaId = captchaId;        // 设置 captchaId
          } else {
            console.error("验证码数据不完整:", response);
            alert("验证码加载失败，请刷新页面！");
          }
        } else {
          console.error("返回的数据格式不正确:", response);
          alert("验证码加载失败，请刷新页面！");
        }
      } catch (error) {
        console.error("获取验证码失败：", error);
        alert("验证码加载失败，请刷新页面！");
      }
    },

    // 刷新验证码
    refreshCaptcha() {
      this.getCaptcha();  // 刷新验证码图片
      this.captchaInput = "";  // 清空验证码输入框
      this.captchaError = "";  // 清空错误提示
    }
  }
};
</script>


<style scoped>
.container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
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
  color: #4b5563;
  text-align: left;
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

.error-message {
  color: #ef4444;
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.strength-message {
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.default {
  color: #6b7280;
}

.weak {
  color: #ef4444;
}

.medium {
  color: #f59e0b;
}

.strong {
  color: #10b981;
}

.register-button {
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

.register-button:hover {
  background-color: #2563eb;
}

.login-link {
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

.captcha-container {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border: 1px solid #d1d5db;
  border-radius: 0.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
