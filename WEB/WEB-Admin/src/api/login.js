import request from '@/utils/request'

export function login(username, password) {
  return request({
/*    url: '/user/login',
    method: 'post',
    data: {
      username,
      password
    }*/
    url:`/admin/user/login/${username}/${password}`,
    //请求方式
    method:"post"
  })
}

export function getInfo(token) {
  return request({
    url: `/admin/user/getInfo/${token}`,
    method: 'get',
    //params: { token }
  })
}

export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}
