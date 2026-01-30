/**
 * 表格列配置工具
 * 减少重复的表格列定义代码
 */

import { h } from 'vue'
import { NButton, NSpace, NIcon, NPopconfirm, NTag, NAvatar } from 'naive-ui'
import { CreateOutline, TrashOutline } from '@vicons/ionicons5'
import { formatDateTime, getUserInitial } from './common'
import { USER_STATUS, ARTICLE_STATUS } from '@/config/constants'

/**
 * 创建ID列
 * @param {number} width - 列宽，默认 80
 * @returns {Object} 列配置
 */
export const createIdColumn = (width = 80) => ({
    title: 'ID',
    key: 'id',
    width
})

/**
 * 创建头像列
 * @param {number} width - 列宽，默认 80
 * @returns {Object} 列配置
 */
export const createAvatarColumn = (width = 80) => ({
    title: '头像',
    key: 'avatar',
    width,
    render: (row) => {
        if (row.avatar) {
            return h(NAvatar, { src: row.avatar, round: true })
        }
        return h(NAvatar, { round: true }, { default: () => getUserInitial(row.username) })
    }
})

/**
 * 创建文本列
 * @param {string} title - 列标题
 * @param {string} key - 数据键名
 * @param {Object} options - 配置选项
 * @param {number} options.width - 列宽
 * @param {boolean} options.ellipsis - 是否显示省略号
 * @param {Function} options.render - 自定义渲染函数
 * @returns {Object} 列配置
 */
export const createTextColumn = (title, key, options = {}) => {
    const column = {
        title,
        key
    }

    if (options.width) {
        column.width = options.width
    }

    if (options.ellipsis) {
        column.ellipsis = { tooltip: true }
    }

    if (options.render) {
        column.render = options.render
    }

    return column
}

/**
 * 创建标签列
 * @param {string} title - 列标题
 * @param {string} key - 数据键名
 * @param {Object} options - 配置选项
 * @param {number} options.width - 列宽
 * @param {Function} options.getType - 获取标签类型的函数
 * @param {Function} options.getText - 获取标签文本的函数
 * @returns {Object} 列配置
 */
export const createTagColumn = (title, key, options = {}) => ({
    title,
    key,
    width: options.width || 120,
    render: (row) => {
        const type = options.getType ? options.getType(row) : 'default'
        const text = options.getText ? options.getText(row) : row[key]
        return h(NTag, { type }, { default: () => text })
    }
})

/**
 * 创建用户状态列
 * @param {number} width - 列宽，默认 100
 * @returns {Object} 列配置
 */
export const createUserStatusColumn = (width = 100) => ({
    title: '状态',
    key: 'status',
    width,
    render: (row) =>
        h(
            NTag,
            { type: row.status === USER_STATUS.ENABLED ? 'success' : 'default' },
            { default: () => (row.status === USER_STATUS.ENABLED ? '启用' : '禁用') }
        )
})

/**
 * 创建文章状态列
 * @param {number} width - 列宽，默认 100
 * @returns {Object} 列配置
 */
export const createArticleStatusColumn = (width = 100) => ({
    title: '状态',
    key: 'isDraft',
    width,
    render: (row) =>
        h(
            NTag,
            { type: row.isDraft === ARTICLE_STATUS.PUBLISHED ? 'success' : 'warning' },
            { default: () => (row.isDraft === ARTICLE_STATUS.PUBLISHED ? '已发布' : '草稿') }
        )
})

/**
 * 创建角色列
 * @param {number} width - 列宽，默认 100
 * @returns {Object} 列配置
 */
export const createRoleColumn = (width = 100) => ({
    title: '角色',
    key: 'roleKey',
    width,
    render: (row) =>
        h(
            NTag,
            { type: row.roleKey === 'admin' ? 'error' : 'info' },
            { default: () => (row.roleKey === 'admin' ? '管理员' : '普通用户') }
        )
})

/**
 * 创建时间列
 * @param {string} title - 列标题，默认 '创建时间'
 * @param {string} key - 数据键名，默认 'createTime'
 * @param {number} width - 列宽，默认 180
 * @returns {Object} 列配置
 */
export const createTimeColumn = (title = '创建时间', key = 'createTime', width = 180) => ({
    title,
    key,
    width,
    render: (row) => formatDateTime(row[key])
})

/**
 * 创建操作列
 * @param {Object} options - 配置选项
 * @param {Function} options.onEdit - 编辑回调函数
 * @param {Function} options.onDelete - 删除回调函数
 * @param {string} options.deleteConfirmText - 删除确认文本
 * @param {number} options.width - 列宽，默认 180
 * @param {boolean} options.showEdit - 是否显示编辑按钮，默认 true
 * @param {boolean} options.showDelete - 是否显示删除按钮，默认 true
 * @param {Array} options.extraButtons - 额外的按钮配置
 * @returns {Object} 列配置
 */
export const createActionColumn = (options = {}) => {
    const {
        onEdit,
        onDelete,
        deleteConfirmText = '确定要删除吗？',
        width = 180,
        showEdit = true,
        showDelete = true,
        extraButtons = []
    } = options

    return {
        title: '操作',
        key: 'actions',
        width,
        fixed: 'right',
        render: (row) => {
            const buttons = []

            // 编辑按钮
            if (showEdit && onEdit) {
                buttons.push(
                    h(
                        NButton,
                        {
                            size: 'small',
                            type: 'primary',
                            onClick: () => onEdit(row)
                        },
                        {
                            icon: () => h(NIcon, null, { default: () => h(CreateOutline) }),
                            default: () => '编辑'
                        }
                    )
                )
            }

            // 额外按钮
            extraButtons.forEach(buttonConfig => {
                buttons.push(
                    h(
                        NButton,
                        {
                            size: 'small',
                            type: buttonConfig.type || 'default',
                            onClick: () => buttonConfig.onClick(row)
                        },
                        {
                            icon: buttonConfig.icon ? () => h(NIcon, null, { default: () => h(buttonConfig.icon) }) : undefined,
                            default: () => buttonConfig.text
                        }
                    )
                )
            })

            // 删除按钮
            if (showDelete && onDelete) {
                buttons.push(
                    h(
                        NPopconfirm,
                        {
                            onPositiveClick: () => onDelete(row)
                        },
                        {
                            trigger: () =>
                                h(
                                    NButton,
                                    { size: 'small', type: 'error' },
                                    {
                                        icon: () => h(NIcon, null, { default: () => h(TrashOutline) }),
                                        default: () => '删除'
                                    }
                                ),
                            default: () => deleteConfirmText
                        }
                    )
                )
            }

            return h(NSpace, null, { default: () => buttons })
        }
    }
}

/**
 * 创建用户表格列配置
 * @param {Object} options - 配置选项
 * @param {boolean} options.showId - 是否显示ID列，默认 true
 * @param {boolean} options.showRole - 是否显示角色列，默认 false
 * @param {boolean} options.showIntro - 是否显示个人简介列，默认 false
 * @param {Function} options.onEdit - 编辑回调
 * @param {Function} options.onDelete - 删除回调
 * @param {string} options.deleteConfirmText - 删除确认文本
 * @returns {Array} 列配置数组
 */
export const createUserColumns = (options = {}) => {
    const {
        showId = true,
        showRole = false,
        showIntro = false,
        onEdit,
        onDelete,
        deleteConfirmText = '确定要删除这个用户吗？'
    } = options

    const columns = []

    // ID列
    if (showId) {
        columns.push(createIdColumn())
    }

    // 角色列
    if (showRole) {
        columns.push(createRoleColumn())
    }

    // 头像列
    columns.push(createAvatarColumn())

    // 基本信息列
    columns.push(
        createTextColumn('用户名', 'username', { width: 150 }),
        createTextColumn('昵称', 'nickname', { width: 150 }),
        createTextColumn('邮箱', 'email', { ellipsis: true })
    )

    // 个人简介列
    if (showIntro) {
        columns.push(
            createTextColumn('个人简介', 'intro', {
                ellipsis: true,
                render: (row) => row.intro || '-'
            })
        )
    }

    // 状态和时间列
    columns.push(
        createUserStatusColumn(),
        createTimeColumn()
    )

    // 操作列
    if (onEdit || onDelete) {
        columns.push(
            createActionColumn({
                onEdit,
                onDelete,
                deleteConfirmText
            })
        )
    }

    return columns
}

/**
 * 创建文章表格列配置
 * @param {Object} options - 配置选项
 * @returns {Array} 列配置数组
 */
export const createArticleColumns = (options = {}) => {
    const { onEdit, onDelete } = options

    return [
        createIdColumn(),
        createTextColumn('标题', 'title', { ellipsis: true }),
        createTagColumn('分类', 'categoryName', {
            width: 120,
            getType: () => 'success',
            getText: (row) => row.categoryName || '-'
        }),
        createTextColumn('作者', 'authorName', { width: 120 }),
        createTextColumn('浏览量', 'viewCount', { width: 100 }),
        createTextColumn('点赞数', 'likeCount', { width: 100 }),
        createArticleStatusColumn(),
        createTimeColumn(),
        createActionColumn({
            onEdit,
            onDelete,
            deleteConfirmText: '确定要删除这篇文章吗？'
        })
    ]
}

export default {
    createIdColumn,
    createAvatarColumn,
    createTextColumn,
    createTagColumn,
    createUserStatusColumn,
    createArticleStatusColumn,
    createRoleColumn,
    createTimeColumn,
    createActionColumn,
    createUserColumns,
    createArticleColumns
}
