import Vue from 'vue'
import Router from 'vue-router'

// in development-env not use lazy-loading, because lazy-loading too many pages will cause webpack hot update too slow. so only in production use lazy-loading;
// detail: https://panjiachen.github.io/vue-element-admin-site/#/lazy-loading

Vue.use(Router)

/* Layout */
import Layout from '../views/layout/Layout'

/**
* hidden: true                   if `hidden:true` will not show in the sidebar(default is false)
* alwaysShow: true               if set true, will always show the root menu, whatever its child routes length
*                                if not set alwaysShow, only more than one route under the children
*                                it will becomes nested mode, otherwise not show the root menu
* redirect: noredirect           if `redirect:noredirect` will no redirect in the breadcrumb
* name:'router-name'             the name is used by <keep-alive> (must set!!!)
* meta : {
    title: 'title'               the name show in submenu and breadcrumb (recommend set)
    icon: 'svg-name'             the icon show in the sidebar,
  }
**/
export const constantRouterMap = [
  { path: '/login', component: () => import('@/views/login/index'), hidden: true },
  { path: '/404', component: () => import('@/views/404'), hidden: true },

  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    name: '管理员信息',
    hidden: true,
    children: [{
      path: 'dashboard',
      component: () => import('@/views/dashboard/index'),
    }]
  },
  //医院设置路由
  {
    path: '/hospSet',
    component: Layout,
    redirect: '/hospSet/list',
    name: '医院设置管理',
    meta: { title: '医院设置管理', icon: 'example' },
    children: [
      {
        path: 'list',
        name: '医院设置列表',
        component: () => import('@/views/hospset/list'),
        meta: { title: '医院设置列表', icon: 'table' }
      },
      {
        path: 'add',
        name: '医院设置添加',
        component: () => import('@/views/hospset/add'),
        meta: { title: '医院设置添加', icon: 'tree' }
      },
      {
        path: 'hosp/list',
        name: '医院列表',
        component: () => import('@/views/hosp/hosplist'),
        meta: { title: '医院列表', icon: 'tree' }
      },
      //隐藏路由
      {
        path: 'edit/:id',
        name: '医院设置添加',
        component: () => import('@/views/hospset/add'),
        meta: { title: '编辑', icon: 'tree' },
        hidden: true
      },
      {
        path: 'show/:id',
        name: '医院详情',
        component: () => import('@/views/hosp/show'),
        meta: { title: '查看', icon: 'tree' },
        hidden: true
      },
      //排班
      {
        path: 'schedule/:hoscode',
        name: '医院详情',
        component: () => import('@/views/hosp/schedule'),
        meta: { title: '查看', icon: 'tree' },
        hidden: true
      }
    ]
  },
  //数据字典路由
  {
    path: '/cmn',
    component: Layout,
    redirect: '/cmn/list',
    name: '数据管理',
    meta: { title: '数据管理', icon: 'example' },
    alwaysShow:true,
    children: [
      {
        path: 'list',
        name: '数据字典',
        component: () => import('@/views/dict/dictList'),
        meta: { title: '数据字典', icon: 'table' }
      }
    ]
  },
  {
    path:'/user',
    component:Layout,
    redirect:'/user/userInfo/list',
    name:'userInfo',
    meta:{ title:'用户管理', icon:'table'},
    alwaysShow:true,
    children:[
      {
        path:'userInfo/list',
        name:'用户列表',
        component:()=>import('@/views/user/userInfo/list'),
        meta:{title:'用户列表', icon:'table'}
      },
      {
        path: 'userInfo/show/:id',
        name: '用户查看',
        component: () =>import('@/views/user/userInfo/show'),
        meta: { title: '用户查看' },
        hidden: true
      },
      {
        path: 'userInfo/authList',
        name: '认证审批列表',
        component: () =>import('@/views/user/userInfo/authList'),
        meta: { title: '认证审批列表', icon: 'table' }
      }
    ]
  },
  //数据字典路由
  {
    path: '/order',
    component: Layout,
    redirect: '/order/list',
    name: '订单管理',
    meta: { title: '订单管理', icon: 'example' },
    alwaysShow:true,
    children: [
      {
        path: 'list',
        name: '订单列表',
        component: () => import('@/views/order/orderList'),
        meta: { title: '订单列表', icon: 'table' }
      },
      {
        path: 'orderShow/:id',
        name: '订单详情',
        component: () => import('@/views/order/show'),
        meta: { title: '详情', icon: 'tree' },
        hidden: true
      },
    ]
  },
  //统计管理
  {
    path: '/statistics',
    component: Layout,
    redirect: '/statistics/index',
    name: '统计管理',
    meta: { title: '统计管理', icon: 'example' },
    alwaysShow:true,
    children: [
      {
        path: 'index',
        name: '预约统计',
        component: () => import('@/views/statistics/index'),
        meta: { title: '预约统计', icon: 'table' }
      }
    ]
  },
  {
    path: 'external-link',
    component: Layout,
    children: [
      {
        path: 'https://panjiachen.github.io/vue-element-admin-site/#/',
        meta: { title: 'External Link', icon: 'link' }
      }
    ]
  },

  { path: '*', redirect: '/404', hidden: true }
]

export default new Router({
  // mode: 'history', //后端支持可开
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRouterMap
})
