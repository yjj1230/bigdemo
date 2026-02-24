import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const role = ref(localStorage.getItem('role') || 'USER')

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  const setRole = (newRole) => {
    role.value = newRole
    localStorage.setItem('role', newRole)
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    role.value = 'USER'
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('role')
  }

  return {
    token,
    userInfo,
    role,
    setToken,
    setUserInfo,
    setRole,
    logout
  }
})
