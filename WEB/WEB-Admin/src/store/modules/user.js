import { login, logout, getInfo } from '@/api/login'
import { getToken, setToken, removeToken } from '@/utils/auth'
import {count} from "echarts/lib/component/dataZoom/history";

const user = {
  state: {
    token: getToken(),
    name: '',
    avatar: '',
    roles: [],
    email: ''
  }
  ,
  mutations: {
    SET_TOKEN: (state, token) => {
      state.token = token
    },
    SET_NAME: (state, name) => {
      state.name = name
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_ROLES: (state, roles) => {
      state.roles = roles
    },
    SET_EMAIL: (state, email) => {
      state.email = email
    }
  },

  actions: {
    // 登录
/*    Login({ commit }, userInfo) {
      //const data={'token':"admin"}
      setToken(data.token)
      commit('SET_TOKEN', data.token)
      const username = userInfo.username.trim()
      return new Promise((resolve, reject) => {
        login(username, userInfo.password).then(response => {

          const data = response.data

          setToken(data.token)
          commit('SET_TOKEN', data.token)
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },*/
    Login({ commit }, userInfo) {
      const { username, password } = userInfo
      return new Promise((resolve, reject) => {

        login(username,password).then(response => {//ESLINT简写方法，response为后台的ajax数据
          commit('SET_TOKEN', response.data.token)
          setToken(response.data.token)//接受后台数据中的token，否则就抛出异常
          //console.log(response.data.token)
          resolve()

        }).catch(error => {
          reject(error)
          console.log('k')
          console.log(error)
        })
      })
    },

    // 获取用户信息
    GetInfo({ commit, state }) {
/*      console.log(data)
      const datalist = {"roles":"admin","name":"admin","avatar":"https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif"}
      if (datalist.roles && datalist.roles.length > 0) { // 验证返回的roles是否是一个非空数组
        commit('SET_ROLES', datalist.roles)
      } else {
        reject('getInfo: roles must be a non-null array !')
      }
      commit('SET_NAME', datalist.name)
      /!*commit('SET_ROLES', this.data.roles)
      commit("SET_EMAIL",this.data.email)*!/
      commit('SET_AVATAR', "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")*/
        return new Promise((resolve, reject) => {
        getInfo(state.token).then(response => {
          const data = response.data
          if (data.roles && data.roles.length > 0) { // 验证返回的roles是否是一个非空数组
            commit('SET_ROLES', data.roles)
          } else {
            reject('getInfo: roles must be a non-null array !')
          }
          commit("SET_EMAIL",data.email)
          commit('SET_NAME', data.name)
          commit('SET_AVATAR', data.avatar)
          //commit('SET_AVATAR', "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")
          resolve(response)
        }).catch(error => {
          reject(error)
        })
      })
      commit('SET_NAME', data.name)
      //commit('SET_AVATAR', "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")
    },

    // 登出
    LogOut({ commit, state }) {
      commit('SET_TOKEN', '')
      commit('SET_ROLES', [])
      removeToken()
      // return new Promise((resolve, reject) => {
      //   logout(state.token).then(() => {
      //     commit('SET_TOKEN', '')
      //     commit('SET_ROLES', [])
      //     removeToken()
      //     resolve()
      //   }).catch(error => {
      //     reject(error)
      //   })
      // })
    },

    // 前端 登出
    FedLogOut({ commit }) {
      //return new Promise(resolve => {
        commit('SET_TOKEN', '')
        removeToken()
        resolve()
     // })
    }
  }
}

export default user
