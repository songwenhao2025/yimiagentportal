// =====================================================
// 壹米AI Agent门户 - 配置文件
// =====================================================

/**
 * API 配置
 */
export const apiConfig = {
  baseUrl: (import.meta.env.VITE_API_BASE_URL as string) || '',
  timeout: parseInt((import.meta.env.VITE_API_TIMEOUT as string) || '30000'),
}

/**
 * 日志配置
 */
export const logConfig = {
  level: (import.meta.env.VITE_LOG_LEVEL as string) || 'info',
}

/**
 * 应用配置
 */
export const appConfig = {
  version: (import.meta.env.VITE_APP_VERSION as string) || '1.0.0',
  env: (import.meta.env.VITE_APP_ENV as string) || 'development',
  isProduction: import.meta.env.VITE_APP_ENV === 'production',
}

/**
 * 统一导出所有配置
 */
export const config = {
  api: apiConfig,
  log: logConfig,
  app: appConfig,
}

export default config