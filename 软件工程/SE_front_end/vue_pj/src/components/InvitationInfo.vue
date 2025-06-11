<template>
  <div class="invitation-info-container">
    <el-card class="invitation-card">
      <template #header>
        <div class="invitation-header">
          <h2>我的邀请码</h2>
        </div>
      </template>
      
      <div v-if="loading" class="loading">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="error" class="error-state">
        <el-empty description="获取邀请信息失败" :image-size="100">
          <template #description>
            <p>{{ error }}</p>
          </template>
          <el-button type="primary" @click="loadInvitationInfo">重试</el-button>
        </el-empty>
      </div>
      
      <div v-else class="invitation-content">
        <!-- 邀请码展示 -->
        <div class="code-section">
          <h3>您的专属邀请码</h3>
          <div class="code-display">
            <span class="invitation-code">{{ invitationInfo.invitationCode }}</span>
            <el-button 
              type="primary" 
              size="small" 
              icon="el-icon-copy-document"
              @click="copyCode"
            >
              复制
            </el-button>
          </div>
          <div class="invitation-tips">
            <p>邀请好友使用您的邀请码下单，好友首单满10元即可成功邀请</p>
            <p>每成功邀请2位好友，您将获得无门槛20元优惠券奖励</p>
          </div>
        </div>
        
        <!-- 邀请记录 -->
        <div class="records-section">
          <h3>邀请记录 ({{ invitationInfo.invitationRecords?.length || 0 }})</h3>
          
          <div v-if="!invitationInfo.invitationRecords || invitationInfo.invitationRecords.length === 0" class="no-records">
            <el-empty description="暂无邀请记录" :image-size="80" />
          </div>
          
          <el-table
            v-else
            :data="invitationInfo.invitationRecords"
            style="width: 100%"
          >
            <el-table-column
              prop="inviteeId"
              label="被邀请人"
              width="120"
            >
              <template #default="scope">
                <span class="username-display">{{ getUsernameById(scope.row.inviteeId) }}</span>
              </template>
            </el-table-column>
            
            <el-table-column
              prop="orderTime"
              label="下单时间"
              width="160"
            >
              <template #default="scope">
                {{ formatDate(scope.row.orderTime) }}
              </template>
            </el-table-column>
            
            <el-table-column
              prop="orderAmount"
              label="订单金额"
              width="120"
            >
              <template #default="scope">
                <span class="amount">¥{{ scope.row.orderAmount }}</span>
              </template>
            </el-table-column>
            
            <el-table-column
              prop="isValid"
              label="状态"
            >
              <template #default="scope">
                <el-tag :type="scope.row.isValid ? 'success' : 'danger'">
                  {{ scope.row.isValid ? '有效' : '无效' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 奖励记录 -->
        <div class="rewards-section">
          <h3>奖励记录 ({{ invitationInfo.invitationRewards?.length || 0 }})</h3>
          
          <div v-if="!invitationInfo.invitationRewards || invitationInfo.invitationRewards.length === 0" class="no-rewards">
            <el-empty description="暂无奖励记录" :image-size="80" />
          </div>
          
          <el-table
            v-else
            :data="invitationInfo.invitationRewards"
            style="width: 100%"
          >
            <el-table-column
              prop="invitationCount"
              label="邀请人数"
              width="100"
            >
              <template #default="scope">
                <span>{{ scope.row.invitationCount }}人</span>
              </template>
            </el-table-column>
            
            <el-table-column
              prop="rewardType"
              label="奖励类型"
              width="120"
            >
              <template #default="scope">
                <span>{{ scope.row.rewardType }}</span>
              </template>
            </el-table-column>
            
            <el-table-column
              prop="rewardAmount"
              label="奖励金额"
              width="120"
            >
              <template #default="scope">
                <span class="amount">¥{{ scope.row.rewardAmount }}</span>
              </template>
            </el-table-column>
            
            <el-table-column
              prop="createTime"
              label="获得时间"
            >
              <template #default="scope">
                {{ formatDate(scope.row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import InvitationService from '@/services/InvitationService';
import AuthService from '@/services/AuthService';

export default {
  name: 'InvitationInfo',
  setup() {
    const invitationInfo = ref({
      invitationCode: '',
      invitationRecords: [],
      invitationRewards: []
    });
    const loading = ref(true);
    const error = ref('');
    
    // 加载邀请信息
    const loadInvitationInfo = async () => {
      loading.value = true;
      error.value = '';
      
      try {
        const response = await InvitationService.getInvitationInfo();
        invitationInfo.value = response;
      } catch (err) {
        console.error('获取邀请信息失败:', err);
        error.value = err.message || '获取邀请信息失败，请稍后重试';
      } finally {
        loading.value = false;
      }
    };
    
    // 复制邀请码
    const copyCode = () => {
      const code = invitationInfo.value.invitationCode;
      if (!code) {
        ElMessage.warning('邀请码不存在');
        return;
      }
      
      // 使用 Clipboard API 复制
      navigator.clipboard.writeText(code)
        .then(() => {
          ElMessage.success('邀请码已复制到剪贴板');
        })
        .catch(err => {
          console.error('复制失败:', err);
          ElMessage.error('复制失败，请手动复制');
        });
    };
    
    // 格式化日期
    const formatDate = (dateStr) => {
      if (!dateStr) return '';
      
      try {
        const date = new Date(dateStr);
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        });
      } catch (err) {
        console.error('日期格式化错误:', err);
        return dateStr;
      }
    };
    
    // 根据用户ID获取用户名 - 直接模仿评论区的实现
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
    
    onMounted(() => {
      loadInvitationInfo();
    });
    
    return {
      invitationInfo,
      loading,
      error,
      loadInvitationInfo,
      copyCode,
      formatDate,
      getUsernameById
    };
  }
};
</script>

<style scoped>
.invitation-info-container {
  margin: 20px auto;
  max-width: 800px;
}

.invitation-card {
  border-radius: 8px;
  margin-bottom: 20px;
}

.invitation-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.invitation-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.loading, .error-state {
  padding: 30px 0;
  text-align: center;
}

.code-section, .records-section, .rewards-section {
  margin-bottom: 30px;
}

.code-section h3, .records-section h3, .rewards-section h3 {
  font-size: 18px;
  margin-bottom: 15px;
  font-weight: 600;
  color: #303133;
}

.code-display {
  display: flex;
  align-items: center;
  margin: 15px 0;
}

.invitation-code {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  letter-spacing: 2px;
  margin-right: 15px;
  background-color: #ecf5ff;
  padding: 8px 15px;
  border-radius: 4px;
}

.invitation-tips {
  font-size: 14px;
  color: #909399;
  line-height: 1.6;
  margin: 15px 0;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.invitation-tips p {
  margin: 5px 0;
}

.no-records, .no-rewards {
  padding: 20px 0;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
}

.username-display {
  color: #409EFF;
  font-weight: bold;
  background-color: #ecf5ff;
  padding: 2px 8px;
  border-radius: 4px;
  display: inline-block;
}
</style> 