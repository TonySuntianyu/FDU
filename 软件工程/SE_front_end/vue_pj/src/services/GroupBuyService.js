import axios from 'axios';
import AuthService from './AuthService';
import ShopService from './ShopService';

const API_URL = 'http://localhost:8088/api';

// 添加请求拦截器
axios.interceptors.request.use(config => {
  console.log('发送请求:', {
    url: config.url,
    method: config.method,
    headers: config.headers,
    params: config.params
  });
  return config;
});

// 添加响应拦截器
axios.interceptors.response.use(response => {
  console.log('收到响应:', {
    status: response.status,
    data: response.data
  });
  
  // 如果响应包含dishItems，记录详细信息
  if (response.data && response.data.dishItems && Array.isArray(response.data.dishItems)) {
    console.log('响应中的菜品信息:');
    response.data.dishItems.forEach((item, index) => {
      console.log(`菜品 ${index+1}:`, {
        ...item,
        dishPrice_type: typeof item.dishPrice,
        dishDescription_type: typeof item.dishDescription
      });
    });
  }
  
  return response;
}, error => {
  console.error('请求错误:', {
    status: error.response?.status,
    data: error.response?.data,
    message: error.message
  });
  return Promise.reject(error);
});

class GroupBuyService {
  // Get packages by shop ID
  async getPackagesByShopId(shopId) {
    try {
      console.log('开始获取团购套餐，shopId:', shopId);
      const user = AuthService.getUser();
      console.log('当前用户信息:', user);
      
      if (!user || !user.token) {
        console.error('未获取到用户信息或token');
        throw new Error('未登录或token已过期');
      }
      
      const url = `${API_URL}/groupbuy/packages?shopId=${shopId}`;
      console.log('请求URL:', url);
      
      const response = await axios.get(url, {
        headers: { 'Authorization': `Bearer ${user.token}` }
      });
      console.log('团购套餐响应:', response.data);
      return response.data;
    } catch (error) {
      console.error('获取团购套餐失败:', error);
      throw error;
    }
  }

  // Get package detail by ID
  async getPackageDetail(packageId) {
    try {
      console.log('开始获取团购套餐详情，packageId:', packageId);
      const url = `${API_URL}/groupbuy/packages/${packageId}`;
      console.log('请求URL:', url);
      
      const response = await axios.get(url);
      console.log('团购套餐详情响应:', response.data);
      
      // 处理数据，确保dishItems中的dishPrice是数字，dishDescription是字符串
      if (response.data && response.data.dishItems && Array.isArray(response.data.dishItems)) {
        response.data.dishItems = response.data.dishItems.map(item => ({
          ...item,
          dishPrice: typeof item.dishPrice === 'number' ? item.dishPrice : parseFloat(item.dishPrice) || 0,
          dishDescription: item.dishDescription || ''
        }));
      }
      
      return response.data;
    } catch (error) {
      console.error('获取团购套餐详情失败:', error);
      throw error;
    }
  }

  // Create an order
  async createOrder(packageId, couponId = null) {
    try {
      const user = AuthService.getUser();
      if (!user || !user.id) {
        throw new Error('未登录或用户信息不完整');
      }
      
      const response = await axios.post(`${API_URL}/orders`, 
        { packageId, couponId }, 
        { headers: { 'userId': user.id } }
      );
      return response.data;
    } catch (error) {
      console.error('Error creating order:', error);
      throw error;
    }
  }

  // Get user's orders
  async getUserOrders() {
    try {
      const user = AuthService.getUser();
      if (!user || !user.id) {
        throw new Error('未登录或用户信息不完整');
      }
      
      const response = await axios.get(`${API_URL}/orders`, {
        headers: { 'userId': user.id }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching user orders:', error);
      throw error;
    }
  }

  // Get order details with voucher
  async getOrderDetail(orderId) {
    try {
      const user = AuthService.getUser();
      if (!user || !user.id) {
        throw new Error('未登录或用户信息不完整');
      }
      
      const response = await axios.get(`${API_URL}/orders/${orderId}`, {
        headers: { 'userId': user.id }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching order detail:', error);
      throw error;
    }
  }

  // Get user available coupons
  async getUserCoupons(packageId = null) {
    try {
      const user = AuthService.getUser();
      if (!user || !user.id) {
        throw new Error('未登录或用户信息不完整');
      }
      
      // 如果提供了packageId，先获取套餐信息以便进行优惠券筛选
      let packageDetail = null;
      let shopName = null;
      let shopCategory = null;
      if (packageId) {
        try {
          packageDetail = await this.getPackageDetail(packageId);
          console.log('获取到套餐信息:', packageDetail);
          
          // 如果有shopId，获取店铺信息以获取店铺名称
          if (packageDetail && packageDetail.shopId) {
            try {
              const shopResponse = await ShopService.getShopDetails(packageDetail.shopId);
              if (shopResponse && shopResponse.shop) {
                shopName = shopResponse.shop.name;
                shopCategory = shopResponse.shop.categoryName;
                console.log('获取到店铺信息:', { 
                  name: shopName, 
                  category: shopCategory,
                  shopId: packageDetail.shopId 
                });
              } else {
                console.warn('获取店铺响应中未包含店铺信息:', shopResponse);
              }
            } catch (shopErr) {
              console.error('获取店铺信息失败:', shopErr);
            }
          } else {
            console.warn('套餐详情中缺少shopId:', packageDetail);
          }
        } catch (err) {
          console.error('获取套餐信息失败:', err);
        }
      }
      
      const response = await axios.get(`${API_URL}/coupons/user`, {
        headers: { 'userId': user.id }
      });
      
      console.log('优惠券原始数据:', response.data);
      
      // 增加调试信息，输出所有优惠券的有效期信息
      if (response.data && Array.isArray(response.data)) {
        response.data.forEach((coupon, index) => {
          console.log(`优惠券${index + 1} ID:${coupon.id}, 标题:${coupon.title}`);
          console.log(`  类型: ${coupon.type}, 金额/折扣率: ${coupon.amount}`);
          console.log(`  适用品类: ${coupon.applicableCategory}, 适用店铺: ${coupon.applicableShop}`);
          console.log(`  expirationDate: ${coupon.expirationDate} (${typeof coupon.expirationDate})`);
          console.log(`  validDays: ${coupon.validDays} (${typeof coupon.validDays})`);
          console.log(`  receivedAt: ${coupon.receivedAt}`);
          console.log(`  userCouponId: ${coupon.userCouponId}`);
        });
      }
      
      // 确保优惠券数据格式正确
      if (response.data && Array.isArray(response.data)) {
        let coupons = response.data.map(coupon => {
          // 处理过期时间
          let expirationDate = coupon.expirationDate;
          let calculatedExpirationDate = null;
          
          // 输出原始过期时间信息
          console.log(`处理优惠券 ID:${coupon.id}, 原始过期时间:${expirationDate}, validDays:${coupon.validDays}`);
          
          // 如果有validDays处理
          if (coupon.validDays) {
            // 如果有receivedAt信息，使用receivedAt计算
            if (coupon.receivedAt) {
              try {
                const receivedDate = new Date(coupon.receivedAt);
                if (!isNaN(receivedDate.getTime())) {
                  calculatedExpirationDate = new Date(receivedDate);
                  calculatedExpirationDate.setDate(receivedDate.getDate() + coupon.validDays);
                  console.log(`  根据validDays计算的过期时间: ${calculatedExpirationDate.toISOString()}`);
                  
                  // 使用计算出的过期时间替代原始过期时间
                  expirationDate = calculatedExpirationDate.toISOString();
                }
              } catch (err) {
                console.error('计算validDays过期时间出错:', err);
              }
            } 
            // 如果没有receivedAt但有validDays，以当前时间为基准创建一个过期时间
            else {
              // 如果没有领取时间，就假设从当前时间开始计算validDays天的有效期
              const now = new Date();
              calculatedExpirationDate = new Date(now);
              calculatedExpirationDate.setDate(now.getDate() + coupon.validDays);
              console.log(`  无领取时间，使用当前时间计算，过期时间: ${calculatedExpirationDate.toISOString()}`);
              
              // 使用计算出的过期时间
              expirationDate = calculatedExpirationDate.toISOString();
            }
          }
          
          // 处理日期格式
          if (expirationDate && !calculatedExpirationDate) {
            // 如果是数字（时间戳）
            if (typeof expirationDate === 'number') {
              expirationDate = new Date(expirationDate);
              console.log(`  数字时间戳转换: ${expirationDate.toISOString()}`);
            }
            // 如果是字符串但可能不是标准格式
            else if (typeof expirationDate === 'string') {
              // 尝试直接解析
              let date = new Date(expirationDate);
              
              // 如果解析失败，尝试其他格式
              if (isNaN(date.getTime())) {
                console.log(`  标准解析失败，尝试其他格式: ${expirationDate}`);
                // 尝试yyyy-MM-dd或yyyy/MM/dd格式
                const parts = expirationDate.split(/[-\/]/);
                if (parts.length === 3) {
                  date = new Date(
                    parseInt(parts[0]), 
                    parseInt(parts[1]) - 1, 
                    parseInt(parts[2])
                  );
                  console.log(`  使用分隔符解析: ${date.toISOString()}`);
                }
              } else {
                console.log(`  标准格式解析成功: ${date.toISOString()}`);
              }
              
              if (!isNaN(date.getTime())) {
                expirationDate = date;
              } else {
                console.warn('无法解析日期:', expirationDate);
                expirationDate = null;
              }
            }
          }
          
          // 处理折扣金额
          let discountAmount = 0;
          let discountType = coupon.type || '';
          
          if (discountType === '折扣券') {
            // 折扣券的amount存储的是折扣率的减去部分，如9折券存储的是0.1
            // 在前端显示时，仅用于展示参考值，实际计算会在前端finalPrice中处理
            if (typeof coupon.amount === 'number') {
              // 标记为折扣券，金额用于显示，不直接参与计算
              discountAmount = coupon.amount;
            } else if (typeof coupon.amount === 'string') {
              discountAmount = parseFloat(coupon.amount) || 0;
            }
          } else {
            // 其他类型的优惠券，discountAmount就是直接减去的金额
            discountAmount = typeof coupon.discountAmount === 'number' ? 
              coupon.discountAmount : 
              (typeof coupon.amount === 'number' ? coupon.amount : parseFloat(coupon.amount || coupon.discountAmount) || 0);
          }
          
          // 检查优惠券是否适用于当前套餐
          let isApplicable = true;
          
          // 如果提供了具体的套餐信息，检查优惠券的适用范围
          if (packageDetail) {
            console.log(`  检查优惠券ID:${coupon.id}是否适用于套餐ID:${packageDetail.id}`);
            console.log(`  优惠券信息: 品类=${coupon.applicableCategory || '无限制'}, 店铺=${coupon.applicableShop || '无限制'}`);
            console.log(`  店铺信息: 品类=${shopCategory || '未知'}, 店铺名=${shopName || '未知'}`);
            
            // 检查适用的品类
            if (coupon.applicableCategory && shopCategory) {
              if (coupon.applicableCategory !== shopCategory) {
                console.log(`  优惠券不适用：优惠券适用品类"${coupon.applicableCategory}"，店铺品类"${shopCategory}"`);
                isApplicable = false;
              } else {
                console.log(`  优惠券适用品类匹配成功: "${coupon.applicableCategory}" = "${shopCategory}"`);
              }
            } else if (!coupon.applicableCategory) {
              // 如果优惠券没有指定适用品类，则可用于任何品类
              console.log(`  优惠券未指定适用品类，可用于任何品类`);
            }
            
            // 检查适用的店铺
            if (coupon.applicableShop && shopName) {
              if (coupon.applicableShop !== shopName) {
                console.log(`  优惠券不适用：优惠券适用店铺"${coupon.applicableShop}"，店铺名称"${shopName}"`);
                isApplicable = false;
              } else {
                console.log(`  优惠券适用店铺匹配成功: "${coupon.applicableShop}" = "${shopName}"`);
              }
            }
          }
          
          const result = {
            ...coupon,
            // 设置折扣金额
            discountAmount: discountAmount,
            // 确保标题存在
            title: coupon.title || '优惠券',
            // 确保描述字段存在
            description: coupon.description || '',
            // 格式化后的过期时间
            expirationDate: expirationDate instanceof Date ? expirationDate.toISOString() : expirationDate,
            // 增加receivedAt和validDays，以便前端计算动态有效期
            receivedAt: coupon.receivedAt,
            validDays: coupon.validDays,
            // 计算后的优惠券有效期文本，用于前端显示
            validityText: coupon.validDays ? `${coupon.validDays}天有效期` : (expirationDate ? '固定过期日期' : '长期有效'),
            // 添加一个字段表示优惠券的生效状态
            isActive: true,
            // 优惠券是否适用于当前套餐
            isApplicable: isApplicable
          };
          
          console.log(`  处理后的结果:`, {
            id: result.id,
            title: result.title,
            type: result.type,
            amount: result.amount,
            discountAmount: result.discountAmount,
            expirationDate: result.expirationDate,
            validityText: result.validityText,
            isApplicable: result.isApplicable
          });
          
          return result;
        });
        
        // 如果有套餐ID，只返回适用于该套餐的优惠券
        if (packageId) {
          const applicableCoupons = coupons.filter(coupon => coupon.isApplicable);
          console.log(`共${coupons.length}张优惠券，适用于套餐ID:${packageId}的有${applicableCoupons.length}张`);
          return applicableCoupons;
        }
        
        return coupons;
      }
      
      return response.data;
    } catch (error) {
      console.error('Error fetching user coupons:', error);
      throw error;
    }
  }
}

export default new GroupBuyService(); 