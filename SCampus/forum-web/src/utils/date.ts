/**
 * 日期处理工具函数
 * 基于 dayjs 封装常用的日期格式化方法
 */
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

// 配置 dayjs
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/**
 * 格式化日期
 * @param date 日期
 * @param format 格式
 */
export function formatDate(date: string | Date, format: string = 'YYYY-MM-DD'): string {
  return dayjs(date).format(format)
}

/**
 * 格式化日期时间
 * @param date 日期
 */
export function formatDateTime(date: string | Date): string {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

/**
 * 格式化为相对时间
 * @param date 日期
 */
export function formatRelativeTime(date: string | Date): string {
  const now = dayjs()
  const target = dayjs(date)
  const diff = now.diff(target, 'second')

  // 小于1分钟
  if (diff < 60) {
    return '刚刚'
  }

  // 小于1小时
  if (diff < 3600) {
    return `${Math.floor(diff / 60)}分钟前`
  }

  // 小于24小时
  if (diff < 86400) {
    return `${Math.floor(diff / 3600)}小时前`
  }

  // 小于7天
  if (diff < 604800) {
    return `${Math.floor(diff / 86400)}天前`
  }

  // 大于7天显示具体日期
  return formatDate(date)
}

/**
 * 获取当前时间
 * @param format 格式
 */
export function getCurrentTime(format: string = 'YYYY-MM-DD HH:mm:ss'): string {
  return dayjs().format(format)
}

/**
 * 判断是否是今天
 * @param date 日期
 */
export function isToday(date: string | Date): boolean {
  return dayjs(date).isSame(dayjs(), 'day')
}

/**
 * 判断是否是昨天
 * @param date 日期
 */
export function isYesterday(date: string | Date): boolean {
  return dayjs(date).isSame(dayjs().subtract(1, 'day'), 'day')
}

/**
 * 获取年龄
 * @param birthDate 出生日期
 */
export function getAge(birthDate: string | Date): number {
  return dayjs().diff(dayjs(birthDate), 'year')
}

/**
 * 获取两个日期之间的天数差
 * @param startDate 开始日期
 * @param endDate 结束日期
 */
export function getDaysDiff(startDate: string | Date, endDate: string | Date): number {
  return dayjs(endDate).diff(dayjs(startDate), 'day')
}
