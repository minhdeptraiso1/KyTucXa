import { onBeforeUnmount } from 'vue'
import { gsap } from 'gsap'

export function useGsap() {
  const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  const context = gsap.context(() => undefined)

  onBeforeUnmount(() => context.revert())

  function reveal(target: gsap.TweenTarget) {
    if (reducedMotion) return
    context.add(() => {
      gsap.fromTo(target, { opacity: 0, y: 14 }, { opacity: 1, y: 0, duration: 0.55, ease: 'power2.out' })
    })
  }

  return { context, reducedMotion, reveal }
}
