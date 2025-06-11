<template>
  <div class="invitation-page">
    <div class="page-header">
      <h1>我的邀请</h1>
      <p>邀请好友使用您的专属邀请码下单，即可获得奖励</p>
    </div>
    
    <div class="page-content">
      <invitation-info />
      
      <div class="invitation-explain">
        <el-card class="explain-card">
          <template #header>
            <div class="explain-header">
              <h2>邀请规则</h2>
            </div>
          </template>
          
          <div class="explain-content">
            <div class="rule-item">
              <div class="rule-title">
                <i class="el-icon-medal-1"></i>
                <span>如何获得邀请码?</span>
              </div>
              <div class="rule-description">
                <p>所有注册用户均可获得专属邀请码，邀请码在用户注册后自动生成。</p>
              </div>
            </div>
            
            <div class="rule-item">
              <div class="rule-title">
                <i class="el-icon-shopping-cart-2"></i>
                <span>如何成功邀请好友?</span>
              </div>
              <div class="rule-description">
                <p>好友在下单时填写您的邀请码，且订单金额满10元，即视为成功邀请。</p>
                <p>同一用户只能被邀请一次，不能重复被邀请。</p>
                <p>邀请人不能使用自己的邀请码。</p>
              </div>
            </div>
            
            <div class="rule-item">
              <div class="rule-title">
                <i class="el-icon-present"></i>
                <span>邀请奖励是什么?</span>
              </div>
              <div class="rule-description">
                <p>每成功邀请2位好友，您将获得无门槛20元优惠券一张，有效期7天。</p>
                <p>邀请奖励将在符合条件后自动发放到您的账户中。</p>
              </div>
            </div>
            
            <div class="rule-item">
              <div class="rule-title">
                <i class="el-icon-time"></i>
                <span>邀请码有效期</span>
              </div>
              <div class="rule-description">
                <p>邀请码长期有效，无使用次数限制。</p>
                <p>平台有权对邀请规则进行调整，具体以最新公告为准。</p>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
import InvitationInfo from '@/components/InvitationInfo.vue';

export default {
  name: 'InvitationPage',
  components: {
    InvitationInfo
  }
};
</script>

<style scoped>
.invitation-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px 0;
  background: linear-gradient(135deg, #409EFF 0%, #007fff 100%);
  color: white;
  border-radius: 8px;
}

.page-header h1 {
  font-size: 28px;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 16px;
  opacity: 0.9;
}

.page-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.invitation-explain {
  margin-bottom: 30px;
}

.explain-card {
  border-radius: 8px;
}

.explain-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.explain-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.explain-content {
  padding: 10px 0;
}

.rule-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #ebeef5;
}

.rule-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.rule-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.rule-title i {
  margin-right: 10px;
  font-size: 20px;
  color: #409EFF;
}

.rule-description {
  padding-left: 30px;
  color: #606266;
  line-height: 1.6;
}

.rule-description p {
  margin: 5px 0;
  font-size: 14px;
}

@media (max-width: 768px) {
  .page-content {
    flex-direction: column;
  }
  
  .invitation-info, .invitation-explain {
    width: 100%;
  }
}
</style> 