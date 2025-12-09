# PowerShell 扩展 OAuth2 连接超时问题解决方案

## 问题描述
PowerShell 扩展在尝试通过 Google OAuth2 登录时出现连接超时错误：
```
Failed to login. Message: Failed to exchange authorization code for tokens: 
request to https://oauth2.googleapis.com/token failed, reason: connect ETIMEDOUT 142.250.107.95:443
```

## 已应用的解决方案

已在 `.vscode/settings.json` 中添加了以下配置：

1. **禁用遥测**：减少不必要的网络请求
   - `"telemetry.telemetryLevel": "off"`

2. **PowerShell 扩展基础配置**：
   - `"powershell.enableProfileLoading": true`
   - `"powershell.scriptAnalysis.enable": true`
   - `"powershell.integratedConsole.showOnStartup": false`
   - `"powershell.developer.editorServicesLogLevel": "Normal"`

## 其他可选解决方案

### 方案 1：配置代理（如果使用代理服务器）

如果您的网络需要通过代理访问外网，请在 `.vscode/settings.json` 中添加：

```json
"http.proxy": "http://your-proxy-server:port",
"http.proxyStrictSSL": false
```

### 方案 2：检查网络连接

1. **测试 Google 服务连接**：
   ```powershell
   Test-NetConnection -ComputerName oauth2.googleapis.com -Port 443
   ```

2. **检查 DNS 解析**：
   ```powershell
   Resolve-DnsName oauth2.googleapis.com
   ```

### 方案 3：使用系统代理设置

VS Code 默认会使用系统代理设置。确保 Windows 系统代理配置正确：
- 打开"设置" → "网络和 Internet" → "代理"
- 检查代理服务器设置

### 方案 4：禁用 PowerShell 扩展的在线功能

如果不需要 PowerShell 扩展的云端功能，可以：
1. 打开 VS Code 扩展面板
2. 找到 PowerShell 扩展
3. 点击设置图标
4. 禁用相关在线功能

### 方案 5：重新安装 PowerShell 扩展

如果问题持续存在：
1. 卸载 PowerShell 扩展
2. 重启 VS Code
3. 重新安装 PowerShell 扩展

## 重要说明

⚠️ **这个错误不会影响您的项目代码运行**

- 您的 `run.ps1` 脚本可以正常使用
- C++ 程序编译和运行不受影响
- 这只是 PowerShell 扩展的认证功能问题

## 验证修复

重启 VS Code 后，检查是否还有相同的错误提示。如果问题仍然存在，可以尝试上述其他方案。

