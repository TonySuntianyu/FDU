import axios from 'axios';
import AuthService from './AuthService';

const API_URL = "http://localhost:8088";

// 配置axios实例
const axiosInstance = axios.create({
    baseURL: API_URL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json;charset=UTF-8',
        'Accept': 'application/json'
    }
});

// 请求拦截器
axiosInstance.interceptors.request.use(
    config => {
        // 添加用户认证信息
        const user = AuthService.getUser();
        if (config.headers) {
            // 确保所有请求都带有userId，未登录时使用游客ID 1
            config.headers['userId'] = user && user.id ? user.id : '1';
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// 响应拦截器
axiosInstance.interceptors.response.use(
    response => {
        return response;
    },
    error => {
        return Promise.reject(error);
    }
);

export default {
    /**
     * 创建评论或回复
     * 对应 ReviewController.java 中的 createReview 方法
     * @param {Object} reviewData - 评论数据
     * @param {number} reviewData.merchantId - 商户ID
     * @param {string} reviewData.content - 评论内容
     * @param {number} [reviewData.parentId] - 父评论ID (回复评论时需要)
     * @returns {Promise<Object>} 创建的评论数据
     */
    async createReview(reviewData) {
        try {
            // 参数检查
            if (!reviewData.merchantId) {
                throw new Error('商户ID不能为空');
            }
            if (!reviewData.content || reviewData.content.length < 15) {
                throw new Error('评论内容需至少15字');
            }

            console.log(`[API调用] 开始提交评论，商户ID=${reviewData.merchantId}`);
            console.log(`[API调用] 评论内容: ${reviewData.content.substring(0, 30)}...`);
            if (reviewData.parentId) {
                console.log(`[API调用] 回复评论ID: ${reviewData.parentId}`);
            }

            // 获取当前登录用户ID
            const user = AuthService.getUser();
            if (!user || !user.id) {
                throw new Error('请先登录后再发表评论');
            }
            
            console.log(`[API调用] 用户ID: ${user.id}`);

            // 构建请求参数 - 与后端 ReviewController 参数一致
            const params = new URLSearchParams();
            params.append('merchantId', reviewData.merchantId);
            params.append('content', reviewData.content);
            if (reviewData.parentId) {
                params.append('parentId', reviewData.parentId);
            }
            
            console.log(`[API调用] 请求参数: `, Object.fromEntries(params.entries()));

            // 发送请求 - 对应 @PostMapping 和 参数注解
            const headers = {
                'Content-Type': 'application/x-www-form-urlencoded',
                'userId': user.id
            };
            console.log(`[API调用] 请求头: `, headers);
            
            console.log(`[API调用] 发送POST请求到: ${API_URL}/api/reviews`);
            const response = await axiosInstance.post('/api/reviews', params, { headers });
            
            console.log(`[API调用] 响应状态: ${response.status}`);
            console.log(`[API调用] 响应数据: `, response.data);

            return response.data;
        } catch (error) {
            console.error(`[API调用] 错误: 创建评论失败:`, error);
            if (error.response) {
                console.error(`[API调用] 错误状态码: ${error.response.status}`);
                console.error(`[API调用] 错误响应: `, error.response.data);
            } else if (error.request) {
                console.error(`[API调用] 未收到响应`);
            } else {
                console.error(`[API调用] 请求配置错误: ${error.message}`);
            }
            throw error;
        }
    },

    /**
     * 获取商户的评论列表
     * 对应 ReviewController.java 中的 getReviewsByMerchant 方法
     * @param {number} merchantId - 商户ID
     * @returns {Promise<Array>} 评论列表
     */
    async getReviewsByMerchant(merchantId) {
        try {
            if (!merchantId) {
                throw new Error('商户ID不能为空');
            }

            console.log(`[API调用] 开始获取商户ID=${merchantId}的评论列表`);
            console.log(`[API调用] 商户ID类型: ${typeof merchantId}`);

            // 确保merchantId是数字
            const numericMerchantId = Number(merchantId);
            if (isNaN(numericMerchantId)) {
                console.error(`[API调用] 商户ID无法转换为数字: ${merchantId}`);
                throw new Error('商户ID必须是数字');
            }
            
            console.log(`[API调用] 转换后的商户ID: ${numericMerchantId} (${typeof numericMerchantId})`);

            // 获取当前登录用户
            const user = AuthService.getUser();
            
            // 确保请求包含userId头
            const headers = {
                // 若用户未登录，使用游客ID (1) 进行评论查看
                'userId': user && user.id ? user.id : '1'
            };
            
            console.log(`[API调用] 请求头:`, headers);
            
            // API路径对应 @GetMapping("/{merchantId}")
            const apiUrl = `/api/reviews/${numericMerchantId}`;
            console.log(`[API调用] 请求URL: ${API_URL}${apiUrl}`);
            
            const response = await axiosInstance.get(apiUrl, { headers });
            
            console.log(`[API调用] 响应状态: ${response.status}`);
            console.log(`[API调用] 响应headers:`, response.headers);
            console.log(`[API调用] 响应数据类型:`, typeof response.data);
            console.log(`[API调用] 响应是否为数组:`, Array.isArray(response.data));
            
            // 添加完整的响应数据日志，便于调试
            console.log(`[API调用] 完整响应数据:`, JSON.stringify(response.data));
            
            if (response.data !== null && response.data !== undefined) {
                console.log(`[API调用] 响应数据预览:`, JSON.stringify(response.data).slice(0, 100) + "...");
            }
            
            // 确保结果是数组
            let reviewsData = [];
            if (Array.isArray(response.data)) {
                reviewsData = response.data;
                console.log(`[API调用] 获取到${reviewsData.length}条评论`);
                
                if (reviewsData.length > 0) {
                    const firstReview = reviewsData[0];
                    console.log(`[API调用] 第一条评论ID: ${firstReview.id}, 用户ID: ${firstReview.userId}`);
                    console.log(`[API调用] 第一条评论内容: ${firstReview.content?.substring(0, 30)}...`);
                    console.log(`[API调用] 第一条评论是否有回复: ${firstReview.replies ? '是' : '否'}`);
                }
            } else {
                console.warn(`[API调用] 警告: 响应数据不是数组，而是 ${typeof response.data}`);
                // 尝试解析数据
                if (typeof response.data === 'object' && response.data !== null) {
                    if (Array.isArray(response.data.reviews)) {
                        reviewsData = response.data.reviews;
                        console.log(`[API调用] 从嵌套结构中提取评论数组，包含${reviewsData.length}条评论`);
                    }
                }
            }
                
            // 确保每个评论对象都有replies属性
            reviewsData.forEach(review => {
                if (!review.replies) {
                    review.replies = [];
                }
            });
                
            console.log(`[API调用] 返回处理后的评论数据，共${reviewsData.length}条评论`);
            return reviewsData;
        } catch (error) {
            console.error(`[API调用] 错误: 获取评论列表失败:`, error);
            if (error.response) {
                console.error(`[API调用] 错误状态码: ${error.response.status}`);
                console.error(`[API调用] 错误响应: `, error.response.data);
            } else if (error.request) {
                console.error(`[API调用] 未收到响应`);
            } else {
                console.error(`[API调用] 请求配置错误: ${error.message}`);
            }
            throw error;
        }
    },
    
    /**
     * 测试接口：获取指定商户的所有评论（不过滤parentId）
     * 用于调试问题
     * @param {number} merchantId - 商户ID
     * @returns {Promise<Array>} 评论列表
     */
    async getAllReviewsByMerchant(merchantId) {
        try {
            if (!merchantId) {
                throw new Error('商户ID不能为空');
            }

            console.log(`[API调用][测试] 开始获取商户ID=${merchantId}的所有评论`);
            
            // 确保merchantId是数字
            const numericMerchantId = Number(merchantId);

            // 获取当前登录用户
            const user = AuthService.getUser();
            
            // 确保请求包含userId头
            const headers = {
                'userId': user && user.id ? user.id : '1'
            };
            
            // 调用测试接口
            const apiUrl = `/api/reviews/test/${numericMerchantId}`;
            console.log(`[API调用][测试] 请求URL: ${API_URL}${apiUrl}`);
            
            const response = await axiosInstance.get(apiUrl, { headers });
            
            console.log(`[API调用][测试] 响应状态: ${response.status}`);
            console.log(`[API调用][测试] 响应数据类型:`, typeof response.data);
            
            if (Array.isArray(response.data)) {
                console.log(`[API调用][测试] 获取到${response.data.length}条评论`);
                return response.data;
            } else {
                console.warn(`[API调用][测试] 警告: 响应数据不是数组`);
                return [];
            }
        } catch (error) {
            console.error(`[API调用][测试] 错误: 获取评论列表失败:`, error);
            throw error;
        }
    }
}; 