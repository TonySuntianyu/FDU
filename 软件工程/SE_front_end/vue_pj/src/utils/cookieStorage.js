/**
 * Cookie存储工具类
 * 用于替代localStorage存储敏感数据，解决安全问题
 */

// 设置cookie
export function setCookie(name, value, expiresInHours = 24) {
  const date = new Date();
  date.setTime(date.getTime() + expiresInHours * 60 * 60 * 1000);
  const expires = `expires=${date.toUTCString()}`;
  // 设置httpOnly和secure属性，增强安全性
  // 注意：httpOnly只能由服务器设置，前端设置无效，这里仅作为注释说明
  document.cookie = `${name}=${value};${expires};path=/;SameSite=Strict`;
}

// 获取cookie
export function getCookie(name) {
  const cookieName = `${name}=`;
  const cookies = document.cookie.split(';');
  for (let i = 0; i < cookies.length; i++) {
    let cookie = cookies[i].trim();
    if (cookie.indexOf(cookieName) === 0) {
      return cookie.substring(cookieName.length, cookie.length);
    }
  }
  return null;
}

// 删除cookie
export function removeCookie(name) {
  document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/;SameSite=Strict`;
}

// 编码值以安全存储（可用于复杂对象存储）
export function encodeValue(value) {
  try {
    return typeof value === 'object' 
      ? encodeURIComponent(JSON.stringify(value))
      : encodeURIComponent(String(value));
  } catch (e) {
    console.error('编码值时出错:', e);
    return '';
  }
}

// 解码值 
export function decodeValue(value, isObject = false) {
  try {
    const decoded = decodeURIComponent(value);
    return isObject ? JSON.parse(decoded) : decoded;
  } catch (e) {
    console.error('解码值时出错:', e);
    return isObject ? null : '';
  }
}

// 用于存储用户会话信息的封装方法
export const userSession = {
  // 存储用户ID
  setUserId(userId) {
    setCookie('user_id', userId);
  },
  
  // 获取用户ID
  getUserId() {
    return getCookie('user_id');
  },
  
  // 存储用户名
  setUsername(username) {
    setCookie('username', username);
  },
  
  // 获取用户名
  getUsername() {
    return getCookie('username');
  },
  
  // 存储非敏感会话信息
  setSessionInfo(info) {
    setCookie('session_info', encodeValue(info));
  },
  
  // 获取会话信息
  getSessionInfo() {
    const info = getCookie('session_info');
    return info ? decodeValue(info, true) : null;
  },
  
  // 清除所有用户会话相关cookie
  clearSession() {
    removeCookie('user_id');
    removeCookie('username');
    removeCookie('session_info');
  }
}; 