import { ref, watch, onMounted } from 'vue'

export type Theme = 'light' | 'dark'

const currentTheme = ref<Theme>('dark')

export function useTheme() {
  const isDark = ref(currentTheme.value === 'dark')

  const applyTheme = (theme: Theme) => {
    currentTheme.value = theme
    isDark.value = theme === 'dark'
    document.documentElement.setAttribute('data-theme', theme)
    if (theme === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
    try {
      localStorage.setItem('ktx.theme', theme)
    } catch {
      // localStorage may fail in restricted sandboxes
    }
  }

  const toggleTheme = () => {
    const nextTheme: Theme = currentTheme.value === 'dark' ? 'light' : 'dark'
    applyTheme(nextTheme)
  }

  const initTheme = () => {
    try {
      const saved = localStorage.getItem('ktx.theme') as Theme | null
      if (saved === 'light' || saved === 'dark') {
        applyTheme(saved)
        return
      }
    } catch {
      // fallback
    }

    // Default to dark for a high-tech modern aesthetic
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    applyTheme(prefersDark ? 'dark' : 'light')
  }

  return {
    theme: currentTheme,
    isDark,
    toggleTheme,
    applyTheme,
    initTheme,
  }
}
