/**
 * 格式化时间为北京时间显示
 * 后端返回的时间已经是 Asia/Shanghai 时区
 * @param {string} dateStr 时间字符串
 * @returns {string} 格式化后的北京时间
 */
export function formatBeijingTime(dateStr) {
  if (!dateStr) return '--'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  return date.toLocaleString('zh-CN', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

/**
 * 手机号校验正则
 */
export const phonePattern = /^1[3-9]\d{9}$/

/**
 * 手机号校验器（允许为空）
 */
export function phoneValidator(rule, value, callback) {
  if (value && !phonePattern.test(value)) {
    callback(new Error('请输入正确的11位手机号'))
  } else {
    callback()
  }
}
