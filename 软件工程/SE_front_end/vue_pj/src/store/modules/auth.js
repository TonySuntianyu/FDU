// 用于安全管理用户认证状态的Vuex模块
import { getCookie, setCookie, removeCookie, encodeValue, decodeValue } from '@/utils/cookieStorage';
import { notifyUserLogin, notifyUserLogout, notifyUserUpdate } from '@/utils/sessionState';

// 从cookie获取初始状态
const getInitialState = () => {
  try {
    // 从cookie获取会话信息
    const tokenCookie = getCookie('auth_token');
    const userCookie = getCookie('auth_user');
    const user = userCookie ? decodeValue(userCookie, true) : null;
    
    return {
      token: tokenCookie || null,
      user: user,
      isAuthenticated: !!tokenCookie
    };
  } catch (e) {
    console.error('从cookie获取认证状态失败:', e);
    return {
      token: null,
      user: null,
      isAuthenticated: false
    };
  }
};

export default {
  namespaced: true,
  
  // 使用函数返回初始状态
  state: getInitialState,
  
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token;
      state.isAuthenticated = !!token;
      // 安全存储token - 使用cookie而非localStorage
      if (token) {
        setCookie('auth_token', token);
      } else {
        removeCookie('auth_token');
      }
    },
    
    SET_USER(state, user) {
      state.user = user;
      // 安全存储用户信息 - 使用cookie而非localStorage
      if (user) {
        setCookie('auth_user', encodeValue(user));
      } else {
        removeCookie('auth_user');
      }
    },
    
    LOGOUT(state) {
      state.token = null;
      state.user = null;
      state.isAuthenticated = false;
      // 清除cookie
      removeCookie('auth_token');
      removeCookie('auth_user');
    },
  },
  
  actions: {
    // 初始化认证状态
    initAuth({ commit }) {
      const token = getCookie('auth_token');
      const userStr = getCookie('auth_user');
      
      if (token) {
        commit('SET_TOKEN', token);
      }
      
      if (userStr) {
        try {
          const user = decodeValue(userStr, true);
          commit('SET_USER', user);
        } catch (e) {
          console.error('解析用户信息失败:', e);
          // 如果解析失败，清除可能损坏的数据
          removeCookie('auth_user');
        }
      }
    },
    
    // 保存令牌到Vuex状态和cookie
    saveToken({ commit }, token) {
      commit('SET_TOKEN', token);
    },
    
    // 保存用户信息到Vuex状态和cookie
    saveUser({ commit, state }, user) {
      commit('SET_USER', user);
      
      // 发送用户登录通知
      if (user && !state.user) {
        notifyUserLogin(user);
      } else if (user) {
        notifyUserUpdate(user);
      }
    },
    
    // 登出动作
    logout({ commit }) {
      // 先发送登出通知
      notifyUserLogout();
      // 然后清除状态
      commit('LOGOUT');
    }
  },
  
  getters: {
    // 获取当前用户
    currentUser: state => state.user,
    
    // 判断是否已认证
    isAuthenticated: state => state.isAuthenticated,
    
    // 获取令牌，用于API请求
    authToken: state => state.token
  }
}; 