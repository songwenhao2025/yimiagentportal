// =====================================================
// 壹米AI Agent门户 - 配置文件
// =====================================================

/**
 * API 配置
 */
export const apiConfig = {
  baseUrl: process.env.VUE_APP_API_BASE_URL || '',
  timeout: parseInt(process.env.VUE_APP_API_TIMEOUT || '30000'),
}

/**
 * 数据库配置
 */
export const dbConfig = {
  host: process.env.VUE_APP_DB_HOST || 'localhost',
  port: parseInt(process.env.VUE_APP_DB_PORT || '3306'),
  database: process.env.VUE_APP_DB_NAME || 'yimi_ai_portal',
  username: process.env.VUE_APP_DB_USER || 'root',
  password: process.env.VUE_APP_DB_PASSWORD || '',
}

/**
 * 日志配置
 */
export const logConfig = {
  level: process.env.VUE_APP_LOG_LEVEL || 'info',
}

/**
 * 应用配置
 */
export const appConfig = {
  version: process.env.VUE_APP_VERSION || '1.0.0',
  env: process.env.VUE_APP_ENV || 'development',
  isProduction: process.env.VUE_APP_ENV === 'production',
}

/**
 * 统一导出所有配置
 */
export const config = {
  api: apiConfig,
  db: dbConfig,
  log: logConfig,
  app: appConfig,
}

export default config