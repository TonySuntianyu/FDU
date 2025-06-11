import axios from 'axios';
import AuthService from './AuthService';

const API_URL = 'http://localhost:8088/api';

class CouponService {
  // 获取新人优惠券列表
  async getNewUserCoupons() {
    try {
      const user = AuthService.getUser();
      console.log('尝试获取新人优惠券，当前用户信息:', user);
      
      if (!user || !user.id) {
        throw new Error('未登录或用户信息不完整');
      }
      
      // 使用后端API获取新人优惠券
      const response = await axios.get(`${API_URL}/coupons/new-user-coupons`, {
        headers: { 'userId': user.id }
      });
      
      console.log('获取新人优惠券成功:', response.data);
      
      // 处理优惠券数据，确保包含ID字段
      if (Array.isArray(response.data)) {
        console.log('===========优惠券数据处理开始===========');
        console.log('原始优惠券数据:', JSON.stringify(response.data, null, 2));
        
        const processedData = response.data.map((coupon, index) => {
          console.log(`处理第${index + 1}个优惠券:`, coupon.title || '未命名优惠券');
          console.log(`- 原始ID值: ${coupon.id}, 类型: ${typeof coupon.id}`);
          
          // 如果优惠券没有id字段，则使用后端传来的其他唯一标识，或者使用索引作为ID
          if (!coupon.id) {
            console.log(`- ID为空，尝试寻找替代字段...`);
            // 检查其他可能的ID字段名（常见的ID字段命名变化）
            if (coupon.couponId) {
              console.log(`- 找到couponId字段: ${coupon.couponId}`);
              coupon.id = coupon.couponId;
            } else if (coupon._id) {
              console.log(`- 找到_id字段: ${coupon._id}`);
              coupon.id = coupon._id;
            } else {
              // 使用索引+1作为ID（避免0作为ID）
              console.log(`- 未找到任何ID字段，使用索引作为ID: ${index + 1}`);
              coupon.id = index + 1;
            }
          }
          
          console.log(`- 最终使用的ID值: ${coupon.id}, 类型: ${typeof coupon.id}`);
          return coupon;
        });
        
        console.log('处理后的优惠券数据:');
        processedData.forEach((coupon, index) => {
          console.log(`[${index + 1}] ID: ${coupon.id}, 标题: ${coupon.title || '未命名'}`);
        });
        console.log('===========优惠券数据处理完成===========');
        
        return processedData;
      }
      
      return response.data;
    } catch (error) {
      console.error('获取新人优惠券失败:', error);
      // 返回空数组作为默认值，避免前端崩溃
      return [];
    }
  }

  // 领取新人优惠券
  async receiveNewUserCoupon(couponId) {
    try {
      const user = AuthService.getUser();
      if (!user || !user.id) {
        throw new Error('未登录或用户信息不完整');
      }
      
      console.log('===========开始领取优惠券===========');
      console.log(`用户ID: ${user.id}, 优惠券ID: ${couponId}, ID类型: ${typeof couponId}`);
      
      // 使用CouponController中的/receive-new-user-coupon端点
      const response = await axios({
        method: 'post',
        url: `${API_URL}/coupons/receive-new-user-coupon`,
        headers: { 'userId': user.id },
        params: { couponId: couponId }  // 作为URL参数传递
      });
      
      console.log('领取优惠券响应数据:', JSON.stringify(response.data, null, 2));
      console.log('===========领取优惠券完成===========');
      return response.data;
    } catch (error) {
      console.error('===========领取优惠券失败===========');
      console.error(`错误信息: ${error.message}`);
      
      // 优化错误处理：从错误响应中提取有效信息
      let errorMessage = '领取优惠券失败';
      
      if (error.response) {
        console.error(`状态码: ${error.response.status}`);
        console.error(`响应数据: ${JSON.stringify(error.response.data, null, 2)}`);
        
        // 处理业务错误 (400状态码)
        if (error.response.status === 400 && error.response.data) {
          if (error.response.data.message) {
            // 提取业务异常信息作为错误消息
            errorMessage = error.response.data.message;
          }
        } 
        // 如果是服务器错误 (500状态码)，检查是否包含优惠券已发放完的信息
        else if (error.response.status === 500) {
          // 如果错误消息中包含"优惠券已发放完"，则显示该信息
          if (error.response.data && 
              error.response.data.message && 
              error.response.data.message.includes('优惠券已发放完')) {
            errorMessage = '优惠券已发放完';
          } else {
            errorMessage = '服务器错误，请稍后再试';
          }
        }
      }
      
      // 创建一个增强的错误对象
      const enhancedError = new Error(errorMessage);
      enhancedError.originalError = error;
      enhancedError.status = error.response ? error.response.status : null;
      enhancedError.data = error.response ? error.response.data : null;
      
      throw enhancedError;
    }
  }

  // 获取用户卡包中的所有优惠券
  async getCouponsInWallet() {
    try {
      const user = AuthService.getUser();
      if (!user || !user.id) {
        throw new Error('未登录或用户信息不完整');
      }
      
      // 使用CouponController中的/wallet端点
      const response = await axios.get(`${API_URL}/coupons/wallet`, {
        headers: { 'userId': user.id }
      });
      
      console.log('获取卡包优惠券成功:', response.data);
      return response.data;
    } catch (error) {
      console.error('获取用户卡包优惠券失败:', error);
      // 返回空数组作为默认值，避免前端崩溃
      return [];
    }
  }

  // 获取用户可用的优惠券
  async getUserAvailableCoupons() {
    try {
      const user = AuthService.getUser();
      if (!user || !user.id) {
        throw new Error('未登录或用户信息不完整');
      }
      
      // 使用CouponController中的/user端点
      const response = await axios.get(`${API_URL}/coupons/user`, {
        headers: { 'userId': user.id }
      });
      
      console.log('获取用户可用优惠券成功:', response.data);
      return response.data;
    } catch (error) {
      console.error('获取用户可用优惠券失败:', error);
      return []; // 返回空数组作为默认值，避免前端崩溃
    }
  }

  // 检查用户是否为新用户
  async isNewUser() {
    try {
      const user = AuthService.getUser();
      console.log('检查用户是否为新用户，当前用户:', user);
      
      if (!user || !user.id) {
        console.log('用户未登录，视为非新用户');
        return false; // 未登录用户视为非新用户
      }
      
      // 使用后端API检查是否为新用户
      const response = await axios.get(`${API_URL}/coupons/is-new-user`, {
        headers: { 'userId': user.id }
      });
      
      console.log('用户是否为新用户:', response.data);
      return response.data;
    } catch (error) {
      console.error('检查用户是否为新用户失败:', error);
      return false; // 默认为非新用户，避免错误领取优惠券
    }
  }
  
  // 检查用户是否已领取过新人券
  async hasReceivedNewUserCoupon() {
    try {
      const user = AuthService.getUser();
      console.log('检查用户是否已领取新人券，当前用户:', user);
      
      if (!user || !user.id) {
        console.log('用户未登录，视为已领取新人券');
        return true; // 未登录用户视为已领取过新人券
      }
      
      // 使用后端API检查是否已领取新人券
      const response = await axios.get(`${API_URL}/coupons/has-received-new-user-coupon`, {
        headers: { 'userId': user.id }
      });
      
      console.log('用户是否已领取新人券:', response.data);
      return response.data;
    } catch (error) {
      console.error('检查用户是否已领取新人券失败:', error);
      return true; // 默认为已领取，避免重复领取优惠券
    }
  }
}

export default new CouponService(); 