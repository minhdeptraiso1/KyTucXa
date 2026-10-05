export type SessionClaims = {
  sub?: string
  username?: string
  fullName?: string
  role?: 'ADMIN' | 'STAFF' | 'USER'
  permissions?: string[]
  exp?: number
}

export function parseJwt(token: string | null): SessionClaims | null {
  if (!token) return null
  try {
    const payload = token.split('.')[1]
    if (!payload) return null
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const decoded = decodeURIComponent(
      atob(normalized)
        .split('')
        .map((character) => `%${character.charCodeAt(0).toString(16).padStart(2, '0')}`)
        .join(''),
    )
    return JSON.parse(decoded) as SessionClaims
  } catch {
    return null
  }
}

export function homePathForRole(role?: SessionClaims['role']) {
  return role === 'USER' ? '/portal' : '/dashboard'
}
