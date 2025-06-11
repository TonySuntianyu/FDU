import axios from 'axios';
import { userSession } from '@/utils/cookieStorage';
import store from '@/store'; // 确保在main.js中正确初始化了store

const API_URL = "http://localhost:8088";  // 修改端口和路径，确保后端端口正确

export default {
    // 获取验证码图片
    async getCaptcha() {
        try {
            const response = await axios.get(`${API_URL}/captcha/generate`);
            const { captchaImage, captchaId } = response.data;
            return { captchaImage, captchaId };
        } catch (error) {
            console.error('Error fetching captcha:', error);
            throw new Error('Failed to load captcha');
        }
    },

    // 验证验证码
    async verifyCaptcha(captchaText, captchaId) {
        try {
            const response = await axios.post(`${API_URL}/captcha/verify`, null, {
                params: { captchaId, captchaText }
            });
            return response.data;
        } catch (error) {
            console.error('Captcha verification error:', error.response?.data?.message || error.message);
            throw new Error('Captcha verification failed');
        }
    },

    //****// 登录接口，传递验证码
    async login(username, password, captchaId, captchaText) {
        try {
            const response = await axios.post(`${API_URL}/user/login`, {
                username,
                password,
                captchaId,
                captchaText
            });

            // 校验状态码和响应数据
            if (response.status === 200 && response.data && response.data.data) {
                const data = response.data.data;
                if (data.startsWith("Bearer ")) {
                    const token = data.substring(7); // 提取 Token
                    
                    // 从token解析用户信息
                    let userId = null;
                    try {
                        const tokenParts = token.split('.');
                        if (tokenParts.length === 3) {
                            const payload = JSON.parse(atob(tokenParts[1]));
                            userId = payload.userId || payload.id || payload.sub;
                        }
                    } catch (e) {
                        console.error('解析token payload失败:', e);
                    }
                    
                    // 构建用户信息对象
                    const userData = { 
                        token, 
                        username,
                        id: userId
                    };
                    
                    // 使用Vuex保存用户状态
                    if (store && store.dispatch) {
                        store.dispatch('auth/saveToken', token);
                        store.dispatch('auth/saveUser', userData);
                    } else {
                        // 兼容性处理：如果Vuex未就绪，使用cookie存储
                        userSession.setUsername(username);
                        if (userId) userSession.setUserId(userId);
                        userSession.setSessionInfo({ token, username, id: userId });
                    }
                    
                    return userData;
                } else {
                    throw new Error('登录失败，返回的 Token 格式不正确');
                }
            } else {
                throw new Error(`登录失败，状态码：${response.status}`);
            }
        } catch (error) {
            console.error('Login error:', error.response?.data?.message || error.message);
            throw error; // 保留原始错误信息
        }
    },

    //****// 获取存储的用户信息（如 token）
    getUser() {
        // 优先从Vuex获取
        if (store && store.getters && store.getters['auth/currentUser']) {
            return store.getters['auth/currentUser'];
        }
        
        // 如果Vuex未就绪，尝试从cookie获取
        const sessionInfo = userSession.getSessionInfo();
        if (sessionInfo) {
            return sessionInfo;
        }
        
        // 兼容旧版：尝试从localStorage获取（为了平滑过渡）
        const userString = localStorage.getItem('user');
        if (!userString) return null;

        try {
            const userData = JSON.parse(userString);
            // 如果有token，尝试从token中解析用户ID和用户名
            if (userData.token) {
                try {
                    const tokenParts = userData.token.split('.');
                    if (tokenParts.length === 3) {
                        const payload = JSON.parse(atob(tokenParts[1]));
                        console.log('解析的token payload:', payload);
                        
                        // 从token中获取用户信息
                        const user = {
                            ...userData,
                            id: payload.id || payload.userId || payload.sub || 1,
                            username: payload.username || payload.name || userData.username || '用户'
                        };
                        
                        // 迁移到新的存储机制
                        if (store && store.dispatch) {
                            store.dispatch('auth/saveUser', user);
                        } else {
                            userSession.setSessionInfo(user);
                        }
                        
                        return user;
                    }
                } catch (e) {
                    console.error('解析token失败:', e);
                }
            }
            
            // 如果没有完成安全迁移，兼容性返回原始数据
            return userData;
        } catch (e) {
            console.error('解析用户数据失败:', e);
            return null;
        }
    },

    // 注销方法，清除所有会话数据
    logout() {
        // 使用Vuex清除状态
        if (store && store.dispatch) {
            store.dispatch('auth/logout');
        }
        
        // 兼容性清除
        userSession.clearSession();
        localStorage.removeItem('user');
    },

    // 注册接口，传递验证码
    async register(username, password, captchaText, captchaId) {
        try {
            const payload = {
                user: { username, password },
                captchaId,
                captchaText
            };
    
            // 发送 POST 请求
            const response = await axios.post(`${API_URL}/user/register`, payload);
    
            // 直接根据后端的 code 判断是否成功
            if (response.data.code === 200) {
                return response.data;
            } else {
                // 如果 code 不是 200，抛出后端返回的 msg
                throw new Error(response.data.msg || '注册失败');
            }
        } catch (error) {
            // 如果是 HTTP 400 错误，提取后端的 msg
            if (error.response && error.response.data) {
                throw new Error(error.response.data.msg || '注册失败');
            } else {
                throw new Error(error.message || '注册失败，请重试！');
            }
        }
    }
}
