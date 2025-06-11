import axios from 'axios';

const API_URL = "http://localhost:8088";  // 确保与后端端口一致

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
        console.log('发送请求:', config.url, config.params || config.data);
        return config;
    },
    error => {
        console.error('请求错误:', error);
        return Promise.reject(error);
    }
);

// 响应拦截器
axiosInstance.interceptors.response.use(
    response => {
        console.log('接收响应:', response.config.url, response.status);
        
        // 检查编码问题
        if (response.data && typeof response.data === 'object') {
            // 递归检查并修复对象中的字符串值
            const fixEncoding = (obj) => {
                if (!obj || typeof obj !== 'object') return obj;
                
                Object.keys(obj).forEach(key => {
                    if (typeof obj[key] === 'string') {
                        // 尝试修复乱码字符
                        try {
                            // 记录原始值用于比较
                            const original = obj[key];
                            
                            // 检查是否包含控制字符或未识别字符
                            if (/[\u0000-\u001F\u007F-\u009F\uFFFD]/.test(original)) {
                                console.warn(`发现可能的编码问题: "${key}" = "${original}"`);
                                // 此处无法真正修复，仅记录问题
                            }
                        } catch (e) {
                            console.error('尝试修复编码时出错:', e);
                        }
                    } else if (obj[key] && typeof obj[key] === 'object') {
                        // 递归处理嵌套对象
                        fixEncoding(obj[key]);
                    }
                });
                
                return obj;
            };
            
            // 应用编码修复
            response.data = fixEncoding(response.data);
        }
        
        return response;
    },
    error => {
        console.error('响应错误:', error);
        return Promise.reject(error);
    }
);

export default {
    /**
     * 获取商家列表
     * @param {number} pageCurrent - 当前页码
     * @param {number} pageSize - 每页条数
     * @returns {Promise<Object>} 商家列表数据
     */
    async getShops(pageCurrent, pageSize) {
        try {
            console.log(`正在请求分页列表API: ${API_URL}/shop/page, 参数:`, { pageCurrent, pageSize });
            const response = await axiosInstance.get(`/shop/page`, {
                params: { pageCurrent, pageSize }
            });
            
            // 检查响应结构
            if (response.data && response.data.code === 1) {
                console.log('请求成功, 商家数量:', 
                    response.data.data ? 
                    (Array.isArray(response.data.data) ? response.data.data.length : '数据不是数组') : 
                    '没有数据');
                
                // 检查返回的商家数据结构
                if (Array.isArray(response.data.data) && response.data.data.length > 0) {
                    const sample = response.data.data[0];
                    console.log('商家数据样本结构:', 
                        sample.shop && sample.images ? '含shop和images字段' : 
                        (sample.id ? '直接是商家对象' : '未知结构'));
                }
            } else {
                console.warn('API返回异常状态:', response.data);
            }
            
            return response.data;
        } catch (error) {
            console.error('获取商家列表失败:', error);
            throw error;
        }
    },

    /**
     * 搜索商家
     * @param {Object} queryParams - 搜索参数对象
     * @returns {Promise<Object>} 搜索结果
     */
    async searchShops(queryParams) {
        try {
            // 清理参数，移除无效值
            const cleanParams = { ...queryParams };
            Object.keys(cleanParams).forEach(key => {
                const value = cleanParams[key];
                // 移除null、undefined、NaN值
                if (value === null || value === undefined || (typeof value === 'number' && isNaN(value))) {
                    delete cleanParams[key];
                }
            });
            
            // 添加默认分页参数
            const params = {
                ...cleanParams,
                pageSize: cleanParams.pageSize || 10,
                pageCurrent: cleanParams.pageCurrent || 1
            };
            
            // 处理用户ID - 确保转换为数字类型
            if (params.userId) {
                // 记录原始值用于调试
                const userIdType = typeof params.userId;
                const userIdValue = params.userId;
                
                // 确保userId是数字类型
                params.userId = Number(params.userId);
                
                console.log(`发送搜索请求，userId转换: ${userIdType}类型(${userIdValue}) => number类型(${params.userId})`);
            } else {
                console.warn('发送搜索请求，未包含userId参数，搜索历史将不会被记录');
                
                // 尝试从Vuex获取用户ID
                if (window && window.$store) {
                    const user = window.$store.getters['auth/currentUser'];
                    if (user && user.id) {
                        params.userId = Number(user.id);
                        console.log(`从Vuex自动获取用户ID: ${params.userId}`);
                    }
                }
            }
            
            console.log('发送搜索请求，最终参数:', params); 
            const response = await axiosInstance.get(`/shop/search`, { params });
            console.log('搜索响应:', response.data);
            
            if (response.data.code === 1 && (!response.data.data || response.data.data.length === 0)) {
                console.log('未找到符合条件的商家，尝试检查数据库中是否有相关数据');
            }
            
            return response.data;
        } catch (error) {
            console.error('搜索商家失败:', error);
            // 提供更详细的错误信息
            if (error.response) {
                // 服务器返回了错误状态码
                console.error(`服务器返回错误: ${error.response.status} - ${error.response.data.message || '未知错误'}`);
                return { code: 0, msg: `搜索失败: ${error.response.data.message || '服务器错误'}`, data: [] };
            } else if (error.request) {
                // 请求发送了但没有收到响应
                console.error('没有收到服务器响应，请检查网络连接');
                return { code: 0, msg: '网络错误，请检查连接', data: [] };
            } else {
                // 请求设置时发生错误
                console.error(`请求错误: ${error.message}`);
                return { code: 0, msg: `请求错误: ${error.message}`, data: [] };
            }
        }
    },

    /**
     * 获取用户的搜索历史
     * @param {number} userId - 用户ID
     * @returns {Promise<Object>} 搜索历史数据
     */
    async getSearchHistory(userId) {
        try {
            // 确保userId是数字类型
            let userIdParam = userId;
            
            // 如果未提供userId，尝试从Vuex获取
            if (!userIdParam && window && window.$store) {
                const user = window.$store.getters['auth/currentUser'];
                if (user && user.id) {
                    userIdParam = user.id;
                    console.log('从Vuex获取用户ID:', userIdParam);
                }
            }
            
            // 确保转换为数字类型
            if (userIdParam) {
                userIdParam = Number(userIdParam);
            }
            
            if (!userIdParam) {
                console.error('获取搜索历史失败: 无法获取用户ID');
                return { code: 0, msg: '未登录或无法获取用户ID', data: [] };
            }
            
            console.log('获取搜索历史，用户ID:', userIdParam);
            const response = await axiosInstance.get(`/shop/search/history`, {
                params: { userId: userIdParam }
            });
            console.log('搜索历史响应:', response.data);
            return response.data;
        } catch (error) {
            console.error('获取搜索历史失败:', error);
            
            // 提供更详细的错误信息，但不抛出异常
            if (error.response) {
                // 服务器返回了错误状态码
                console.error(`服务器返回错误: ${error.response.status}`);
                return { 
                    code: 0, 
                    msg: `获取搜索历史失败: ${error.response.data?.message || error.response.data?.msg || '服务器错误'}`, 
                    data: [] 
                };
            } else if (error.request) {
                // 请求发送了但没有收到响应
                console.error('没有收到服务器响应，请检查网络连接或后端服务是否运行');
                return { code: 0, msg: '网络错误，请检查连接', data: [] };
            } else {
                // 请求设置时发生错误
                console.error(`请求错误: ${error.message}`);
                return { code: 0, msg: `请求错误: ${error.message}`, data: [] };
            }
        }
    },

    /**
     * 获取商家详情
     * @param {number} shopId - 商家ID
     * @returns {Promise<Object>} 商家详情数据
     */
    async getShopDetails(shopId) {
        try {
            // 这里调用后端接口获取商家详情，包含图片信息
            const response = await axiosInstance.get(`/shop/${shopId}/detail`);
            return response.data;
        } catch (error) {
            console.error('获取商家详情失败:', error);
            throw error;
        }
    },

    /**
     * 清空用户所有搜索历史记录
     * @param {number} userId - 用户ID
     * @returns {Promise<Object>} 操作结果
     */
    async clearSearchHistory(userId) {
        try {
            // 确保userId是数字类型
            let userIdParam = userId;
            
            // 如果未提供userId，尝试从Vuex获取
            if (!userIdParam && window && window.$store) {
                const user = window.$store.getters['auth/currentUser'];
                if (user && user.id) {
                    userIdParam = user.id;
                    console.log('从Vuex获取用户ID:', userIdParam);
                }
            }
            
            // 确保转换为数字类型
            if (userIdParam) {
                userIdParam = Number(userIdParam);
            }
            
            if (!userIdParam) {
                console.error('清空搜索历史失败: 无法获取用户ID');
                return { code: 0, msg: '未登录或无法获取用户ID', data: null };
            }
            
            console.log(`正在清空用户的搜索历史记录: userId=${userIdParam}`);
            const response = await axiosInstance.delete(`/shop/search/history/clear`, {
                params: { userId: userIdParam }
            });

            if (response.data.code === 1) {
                console.log('清空成功');
            } else {
                console.warn('清空失败:', response.data.msg);
            }

            return response.data;
        } catch (error) {
            console.error('清空搜索历史失败:', error);

            if (error.response) {
                return { code: 0, msg: `清空失败: ${error.response.data.msg || '服务器错误'}`, data: null };
            } else {
                return { code: 0, msg: '网络错误，请检查连接', data: null };
            }
        }
    },

    /**
     * 获取相似关键词
     * @param {string} keyword - 原始关键词
     * @returns {Promise<Object>} 相似关键词数据
     */
    async getSimilarKeywords(keyword) {
        try {
            if (!keyword || keyword.trim() === '') {
                return { code: 1, data: [] };
            }
            
            console.log('获取相似关键词，原始关键词:', keyword);
            const response = await axiosInstance.get(`/shop/search/similar`, {
                params: { keyword: keyword.trim() }
            });
            console.log('相似关键词响应:', response.data);
            return response.data;
        } catch (error) {
            console.error('获取相似关键词失败:', error);
            
            // 提供更详细的错误信息，但不抛出异常
            if (error.response) {
                console.error(`服务器返回错误: ${error.response.status}`);
                return { 
                    code: 0, 
                    msg: `获取相似关键词失败: ${error.response.data?.message || error.response.data?.msg || '服务器错误'}`, 
                    data: [] 
                };
            } else if (error.request) {
                console.error('没有收到服务器响应，请检查网络连接');
                return { code: 0, msg: '网络错误，请检查连接', data: [] };
            } else {
                console.error(`请求错误: ${error.message}`);
                return { code: 0, msg: `请求错误: ${error.message}`, data: [] };
            }
        }
    },

    /**
     * 获取商家列表
     * @param {number} [pageCurrent=1] - 当前页码
     * @param {number} [pageSize=20] - 每页数量，默认改为20条
     * @returns {Promise<Object>} 商家列表及分页信息
     */
    async getShopList(pageCurrent = 1, pageSize = 20) {
        try {
            console.log(`正在请求商家列表API: ${API_URL}/shop/list 参数:`, { pageCurrent, pageSize });
            
            // 确保参数是数字
            const numPageCurrent = parseInt(pageCurrent) || 1;
            const numPageSize = parseInt(pageSize) || 20;
            
            console.log(`转换后的参数: pageCurrent=${numPageCurrent}, pageSize=${numPageSize}`);
            
            // 添加请求超时重试逻辑
            let retries = 0;
            const maxRetries = 2;
            
            while (retries <= maxRetries) {
                try {
                    const response = await axiosInstance.get(`/shop/list`, {
                        params: { 
                            pageCurrent: numPageCurrent, 
                            pageSize: numPageSize
                        },
                        timeout: 10000 // 10秒超时
                    });
                    
                    console.log('商家列表API响应状态:', response.status);
                    console.log('商家列表API响应数据内容:', response.data ? '数据不为空' : '数据为空');
                    
                    if (response.data && response.data.code === 1) {
                        // 检查数据内容
                        if (Array.isArray(response.data.data)) {
                            console.log(`成功获取到${response.data.data.length}条商家数据`);
                            
                            if (response.data.data.length === 0) {
                                console.warn('返回的商家数组为空，可能是数据库中没有数据，或查询条件有误');
                            }
                        } else if (response.data.data) {
                            console.log('响应包含data对象但不是数组');
                        } else {
                            console.warn('响应code=1但没有包含data数据');
                        }
                        
                        return response.data;
                    } else {
                        console.warn('API响应状态异常:', response.data);
                        return { code: 0, msg: '响应数据格式错误', data: [] };
                    }
                } catch (error) {
                    retries++;
                    if (retries > maxRetries) throw error;
                    console.warn(`请求商家列表失败，正在进行第${retries}次重试...`);
                    await new Promise(resolve => setTimeout(resolve, 1000)); // 等待1秒再重试
                }
            }
        } catch (error) {
            console.error('获取商家列表失败:', error);
            
            // 提供更详细的错误信息
            if (error.response) {
                // 服务器返回了错误状态码
                console.error(`服务器返回错误: ${error.response.status}`);
                console.error('错误响应数据:', error.response.data);
                return { code: 0, msg: `获取商家列表失败: ${error.response.data.message || '服务器错误'}`, data: [] };
            } else if (error.request) {
                // 请求发送了但没有收到响应
                console.error('没有收到服务器响应，请检查网络连接或后端服务是否运行');
                return { code: 0, msg: '网络错误，请检查连接或后端服务', data: [] };
            } else {
                // 请求设置时发生错误
                console.error(`请求错误: ${error.message}`);
                return { code: 0, msg: `请求错误: ${error.message}`, data: [] };
            }
        }
    },
    
    /**
     * 获取首页推荐商家
     * @param {number} [count=4] - 获取的商家数量
     * @returns {Promise<Object>} 商家列表
     */
    async getRecommendedShops(count = 4) {
        try {
            return this.getShopList(1, count);
        } catch (error) {
            console.error('获取推荐商家失败:', error);
            throw error;
        }
    },

    /**
     * 获取商户评论列表
     * @param {number} shopId - 商户ID
     * @returns {Promise<Array>} 评论列表
     */
    async getReviews(shopId) {
        try {
            if (!shopId) {
                console.error('获取评论失败: 商户ID不能为空');
                return [];
            }
            
            console.log('获取评论，商户ID:', shopId);
            const response = await axiosInstance.get(`/api/reviews/${shopId}`);
            console.log('评论响应:', response.data);
            return response.data;
        } catch (error) {
            console.error('获取评论失败:', error);
            
            // 提供更详细的错误信息，但不抛出异常
            if (error.response) {
                console.error(`服务器返回错误: ${error.response.status}`);
                return [];
            } else if (error.request) {
                console.error('没有收到服务器响应，请检查网络连接');
                return [];
            } else {
                console.error(`请求错误: ${error.message}`);
                return [];
            }
        }
    },
    
    /**
     * 添加评论
     * @param {number} shopId - 商户ID
     * @param {string} content - 评论内容
     * @returns {Promise<Object>} 添加的评论
     */
    async addReview(shopId, content) {
        try {
            if (!shopId) {
                throw new Error('商户ID不能为空');
            }
            
            if (!content || content.length < 15) {
                throw new Error('评论内容需至少15个字');
            }
            
            // 获取当前登录用户ID
            let userId;
            if (window && window.$store) {
                const user = window.$store.getters['auth/currentUser'];
                if (user && user.id) {
                    userId = user.id;
                }
            }
            
            if (!userId) {
                throw new Error('请先登录');
            }
            
            // 构建请求参数
            const params = new URLSearchParams();
            params.append('merchantId', shopId);
            params.append('content', content);
            
            const response = await axiosInstance.post('/api/reviews', params, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'userId': userId
                }
            });
            
            return { review: response.data };
        } catch (error) {
            console.error('添加评论失败:', error);
            throw error;
        }
    },
    
    /**
     * 添加回复
     * @param {number} shopId - 商户ID
     * @param {number} parentId - 父评论ID
     * @param {string} content - 回复内容
     * @returns {Promise<Object>} 添加的回复
     */
    async addReply(shopId, parentId, content) {
        try {
            if (!shopId) {
                throw new Error('商户ID不能为空');
            }
            
            if (!parentId) {
                throw new Error('父评论ID不能为空');
            }
            
            if (!content || content.length < 15) {
                throw new Error('回复内容需至少15个字');
            }
            
            // 获取当前登录用户ID
            let userId;
            if (window && window.$store) {
                const user = window.$store.getters['auth/currentUser'];
                if (user && user.id) {
                    userId = user.id;
                }
            }
            
            if (!userId) {
                throw new Error('请先登录');
            }
            
            // 构建请求参数
            const params = new URLSearchParams();
            params.append('merchantId', shopId);
            params.append('content', content);
            params.append('parentId', parentId);
            
            const response = await axiosInstance.post('/api/reviews', params, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'userId': userId
                }
            });
            
            return { reply: response.data };
        } catch (error) {
            console.error('添加回复失败:', error);
            throw error;
        }
    }
} 