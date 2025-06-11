import { createStore } from 'vuex';
import auth from './modules/auth';

// 创建Vuex store
const store = createStore({
  modules: {
    auth
  },
  // 全局状态
  state: {
    // 放置共享状态
  },
  // 全局同步变更状态的方法
  mutations: {
    // 放置变更状态的方法
  },
  // 全局异步操作
  actions: {
    // 放置异步操作方法
  },
  // 全局计算属性
  getters: {
    // 放置计算属性
  }
});

export default store; 