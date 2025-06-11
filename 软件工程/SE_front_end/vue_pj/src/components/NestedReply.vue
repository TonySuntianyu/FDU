<template>
  <div class="nested-reply">
    <div class="reply-header">
      <span class="user-id">用户: {{ getUsernameById(reply.userId) }}</span>
      <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
    </div>
    <div class="reply-content">{{ reply.content }}</div>
    
    <!-- 回复按钮 -->
    <div class="reply-actions">
      <el-button type="text" @click="startReply">回复</el-button>
    </div>
    
    <!-- 回复表单 -->
    <div v-if="isReplying" class="nested-reply-form">
      <el-input
        v-model="newReplyContent"
        type="textarea"
        :rows="3"
        placeholder="请输入您的回复，至少15个字..."
        maxlength="1000"
        show-word-limit
      ></el-input>
      <div class="nested-reply-actions">
        <el-button type="primary" size="small" @click="submitReply" :disabled="!isValid">提交回复</el-button>
        <el-button size="small" @click="cancelReply">取消</el-button>
      </div>
    </div>
    
    <!-- 嵌套回复 -->
    <div v-if="reply.replies && reply.replies.length > 0" class="nested-replies">
      <div v-for="childReply in reply.replies" :key="childReply.id" class="nested-reply-item">
        <nested-reply 
          :reply="childReply" 
          :merchant-id="merchantId"
          @reply-added="$emit('reply-added')"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import ReviewService from '../services/ReviewService';
import AuthService from '../services/AuthService';

export default {
  name: 'NestedReply',
  props: {
    reply: {
      type: Object,
      required: true
    },
    merchantId: {
      type: Number,
      required: true
    }
  },
  emits: ['reply-added'],
  setup(props, { emit }) {
    const isReplying = ref(false);
    const newReplyContent = ref('');
    
    // 计算属性，验证回复内容是否有效
    const isValid = computed(() => {
      return newReplyContent.value && newReplyContent.value.length >= 15;
    });
    
    // 格式化时间
    const formatTime = (timestamp) => {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return date.toLocaleString();
    };
    
    // 根据用户ID获取用户名
    const getUsernameById = (userId) => {
      if (!userId) return '未知用户';
      
      // 获取当前登录用户
      const currentUser = AuthService.getUser();
      
      // 如果是当前登录用户，显示"我"
      if (currentUser && currentUser.id === userId) {
        return currentUser.username || '我';
      }
      
      // 这里应该调用获取用户信息的API，但为简化处理，直接返回用户名
      return `用户${userId}`;
    };
    
    // 开始回复
    const startReply = () => {
      isReplying.value = true;
    };
    
    // 提交回复
    const submitReply = async () => {
      if (!isValid.value) {
        ElMessage.error('回复内容至少需要15个字');
        return;
      }
      
      try {
        console.log('【嵌套回复】开始提交回复，回复目标ID:', props.reply.id);
        console.log('【嵌套回复】商户ID:', props.merchantId);
        console.log('【嵌套回复】回复内容:', newReplyContent.value);
        
        // 确保商户ID是数字类型
        const numericMerchantId = Number(props.merchantId);
        if (isNaN(numericMerchantId)) {
          console.error('【嵌套回复】商户ID不是有效数字:', props.merchantId);
          ElMessage.error('无效的商户ID');
          return;
        }
        
        const response = await ReviewService.createReview({
          merchantId: numericMerchantId,
          content: newReplyContent.value,
          parentId: props.reply.id
        });
        
        console.log('【嵌套回复】提交成功，响应:', response);
        
        if (response) {
          ElMessage.success('回复发布成功');
          newReplyContent.value = '';
          isReplying.value = false;
          
          // 发送事件通知父组件重新加载数据
          console.log('【嵌套回复】触发父组件reload事件');
          emit('reply-added');
        }
      } catch (error) {
        console.error('【嵌套回复】提交失败:', error);
        ElMessage.error('发布回复失败: ' + (error.message || '未知错误'));
      }
    };
    
    // 取消回复
    const cancelReply = () => {
      isReplying.value = false;
      newReplyContent.value = '';
    };
    
    return {
      isReplying,
      newReplyContent,
      isValid,
      formatTime,
      startReply,
      submitReply,
      cancelReply,
      getUsernameById
    };
  }
};
</script>

<style scoped>
.nested-reply {
  padding: 10px;
  margin-top: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  border-left: 3px solid #e0e0e0;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.user-id {
  font-size: 14px;
  color: #606266;
}

.reply-time {
  font-size: 12px;
  color: #999;
}

.reply-content {
  margin-bottom: 10px;
  font-size: 14px;
}

.reply-actions {
  margin-top: 5px;
}

.nested-reply-form {
  margin-top: 10px;
}

.nested-reply-actions {
  margin-top: 10px;
  text-align: right;
}

.nested-replies {
  margin-left: 20px;
  margin-top: 10px;
}

.nested-reply-item {
  margin-bottom: 10px;
}
</style> 