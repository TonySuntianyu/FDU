import axios from 'axios';
import AuthService from './AuthService';

const API_URL = 'http://localhost:8088/api';

class InvitationService {
  /**
   * 获取用户邀请信息（包含邀请码、邀请记录和奖励记录）
   * @returns {Promise<Object>} 用户邀请信息
   */
  async getInvitationInfo() {
    try {
      const user = AuthService.getUser();
      if (!user || !user.id) {
        throw new Error('未登录或用户信息不完整');
      }
      
      const response = await axios.get(`${API_URL}/invitation/info`, {
        headers: { 'userId': user.id }
      });
      
      if (!response.data) {
        throw new Error('服务器返回数据为空');
      }
      
      return response.data;
    } catch (error) {
      console.error('获取邀请信息失败:', error);
      if (error.response) {
        // 服务器返回了错误响应
        throw new Error(`服务器错误: ${error.response.status} - ${error.response.data?.message || '未知错误'}`);
      } else if (error.request) {
        // 请求已发送但没有收到响应
        throw new Error('无法连接到服务器，请检查网络连接');
      } else {
        // 请求配置出错
        throw new Error(error.message || '获取邀请信息失败');
      }
    }
  }

  /**
   * 使用邀请码创建订单
   * @param {number} packageId 套餐ID
   * @param {string} invitationCode 邀请码
   * @param {number} [couponId] 优惠券ID（可选）
   * @returns {Promise<Object>} 订单信息
   */
  async createOrderWithInvitation(packageId, invitationCode, couponId = null) {
    try {
      const user = AuthService.getUser();
      if (!user || !user.id) {
        throw new Error('未登录或用户信息不完整');
      }
      
      // 构建请求数据
      const requestData = {
        packageId: packageId,
        invitationCode: invitationCode
      };
      
      // 如果提供了优惠券ID，添加到请求中
      if (couponId) {
        requestData.couponId = couponId;
      }
      
      // 发送创建订单请求
      const response = await axios.post(
        `${API_URL}/orders/with-invitation`, 
        requestData, 
        { headers: { 'userId': user.id } }
      );
      
      return response.data;
    } catch (error) {
      console.error('使用邀请码创建订单失败:', error);
      throw error;
    }
  }
}

export default new InvitationService(); 