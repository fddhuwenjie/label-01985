import { login as loginApi, logout as logoutApi } from '@/api/auth'

export default {
  namespaced: true,
  state: () => ({
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || '',
    realName: localStorage.getItem('realName') || '',
    roles: JSON.parse(localStorage.getItem('roles') || '[]')
  }),
  getters: {
    isAdmin: (state) => state.roles.includes('ROLE_ADMIN'),
    isTeacher: (state) => state.roles.includes('ROLE_TEACHER')
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    SET_USERNAME(state, username) {
      state.username = username
      localStorage.setItem('username', username)
    },
    SET_REAL_NAME(state, realName) {
      state.realName = realName
      localStorage.setItem('realName', realName)
    },
    SET_ROLES(state, roles) {
      state.roles = roles
      localStorage.setItem('roles', JSON.stringify(roles))
    },
    CLEAR_USER(state) {
      state.token = ''
      state.username = ''
      state.realName = ''
      state.roles = []
      localStorage.clear()
    }
  },
  actions: {
    async login({ commit }, { username, password }) {
      const res = await loginApi({ username, password })
      commit('SET_TOKEN', res.data.token)
      commit('SET_USERNAME', res.data.username)
      commit('SET_REAL_NAME', res.data.realName)
      commit('SET_ROLES', res.data.roles)
      return res
    },
    async logout({ commit }) {
      try {
        await logoutApi()
      } catch (e) {
        /* ignore */
      }
      commit('CLEAR_USER')
    }
  }
}
