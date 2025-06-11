import axios from 'axios';
import AuthService from './AuthService';

// 简单的用户信息服务，模仿评论区的实现
class UserService {
  /**
   * 根据用户ID获取用户名
   * @param {number} userId 用户ID
   * @returns {string} 用户名
   */
  getUsernameById(userId) {
    if (!userId) return '未知用户';
    
    // 获取当前登录用户
    const currentUser = AuthService.getUser();
    
    // 如果是当前登录用户，返回"我"或当前用户名
    if (currentUser && currentUser.id == userId) {
      return currentUser.username || '我';
    }
    
    // 直接返回格式化的用户名，与评论区实现保持一致
    return `用户${userId}`;
  }
}

export default new UserService(); 