/**
 * 会话状态管理模块
 * 提供用户登录状态变化的事件通知机制
 */

// 事件类型定义
export const USER_EVENTS = {
  LOGIN: 'user-login',
  LOGOUT: 'user-logout',
  UPDATE: 'user-update'
};

// 存储事件监听器
const listeners = {};

/**
 * 注册事件监听器
 * @param {string} event 事件类型
 * @param {Function} callback 回调函数
 * @returns {Function} 返回取消注册的函数
 */
export function addEventListener(event, callback) {
  if (!listeners[event]) {
    listeners[event] = [];
  }
  listeners[event].push(callback);
  
  // 返回取消注册的函数
  return () => {
    removeEventListener(event, callback);
  };
}

/**
 * 移除事件监听器
 * @param {string} event 事件类型
 * @param {Function} callback 回调函数
 */
export function removeEventListener(event, callback) {
  if (!listeners[event]) return;
  
  const index = listeners[event].indexOf(callback);
  if (index !== -1) {
    listeners[event].splice(index, 1);
  }
}

/**
 * 触发事件
 * @param {string} event 事件类型
 * @param {any} data 事件数据
 */
export function dispatchEvent(event, data) {
  if (!listeners[event]) return;
  
  listeners[event].forEach(callback => {
    try {
      callback(data);
    } catch (error) {
      console.error(`会话事件处理错误 (${event}):`, error);
    }
  });
}

/**
 * 通知用户登录事件
 * @param {Object} userData 用户数据
 */
export function notifyUserLogin(userData) {
  dispatchEvent(USER_EVENTS.LOGIN, userData);
}

/**
 * 通知用户登出事件
 */
export function notifyUserLogout() {
  dispatchEvent(USER_EVENTS.LOGOUT);
}

/**
 * 通知用户数据更新事件
 * @param {Object} userData 更新后的用户数据
 */
export function notifyUserUpdate(userData) {
  dispatchEvent(USER_EVENTS.UPDATE, userData);
} 