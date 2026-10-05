<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import * as THREE from 'three'

const canvas = ref<HTMLCanvasElement | null>(null)
let renderer: THREE.WebGLRenderer | undefined
let frame = 0
let resizeObserver: ResizeObserver | undefined
let scene: THREE.Scene | undefined
let targetX = 0
let targetY = 0

function onPointerMove(event: PointerEvent) {
  targetX = (event.clientX / window.innerWidth - 0.5) * 0.35
  targetY = (event.clientY / window.innerHeight - 0.5) * 0.18
}

function disposeObject(object: THREE.Object3D) {
  object.traverse((child) => {
    const renderable = child as THREE.Mesh
    if (renderable.geometry) renderable.geometry.dispose()
    const materials = Array.isArray(renderable.material) ? renderable.material : [renderable.material]
    materials.filter(Boolean).forEach((material) => material.dispose())
  })
}

onMounted(() => {
  if (!canvas.value || !window.WebGLRenderingContext) return

  try {
    const host = canvas.value.parentElement
    scene = new THREE.Scene()
    const camera = new THREE.PerspectiveCamera(38, 1, 0.1, 100)
    camera.position.set(5.8, 5.1, 7.8)
    camera.lookAt(0, 0.5, 0)

    renderer = new THREE.WebGLRenderer({
      canvas: canvas.value,
      alpha: true,
      antialias: true,
      powerPreference: 'high-performance',
    })
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
    renderer.outputColorSpace = THREE.SRGBColorSpace

    const campus = new THREE.Group()
    campus.position.set(0.7, -0.35, 0)
    campus.rotation.y = -0.2
    scene.add(campus)

    const platform = new THREE.Mesh(
      new THREE.BoxGeometry(6.8, 0.12, 5.4),
      new THREE.MeshStandardMaterial({ color: 0x0b2230, roughness: 0.88, metalness: 0.18 }),
    )
    campus.add(platform)

    const grid = new THREE.GridHelper(6.3, 12, 0x2dd4bf, 0x164e63)
    grid.position.y = 0.08
    grid.scale.z = 0.78
    const gridMaterials = Array.isArray(grid.material) ? grid.material : [grid.material]
    gridMaterials.forEach((material) => {
      material.transparent = true
      material.opacity = 0.28
    })
    campus.add(grid)

    const buildings = [
      { x: -2.2, z: -1.45, w: 0.85, d: 0.9, h: 1.25 },
      { x: -0.85, z: -1.45, w: 1.2, d: 0.9, h: 2.15 },
      { x: 0.8, z: -1.45, w: 1.15, d: 0.9, h: 1.55 },
      { x: 2.2, z: -1.45, w: 0.8, d: 0.9, h: 2.5 },
      { x: -1.75, z: 0.35, w: 1.15, d: 1.15, h: 2.75 },
      { x: 0, z: 0.4, w: 1.45, d: 1.25, h: 1.65 },
      { x: 1.9, z: 0.35, w: 1.1, d: 1.15, h: 2.1 },
      { x: -0.7, z: 1.75, w: 1.05, d: 0.72, h: 1.25 },
      { x: 1.05, z: 1.75, w: 1.25, d: 0.72, h: 1.75 },
    ]

    buildings.forEach((building, index) => {
      const geometry = new THREE.BoxGeometry(building.w, building.h, building.d)
      const material = new THREE.MeshStandardMaterial({
        color: index % 3 === 0 ? 0x0f766e : 0x123b4a,
        emissive: index % 3 === 0 ? 0x063f3b : 0x071a22,
        emissiveIntensity: 0.55,
        metalness: 0.25,
        roughness: 0.58,
      })
      const mesh = new THREE.Mesh(geometry, material)
      mesh.position.set(building.x, building.h / 2 + 0.13, building.z)
      campus.add(mesh)

      const edges = new THREE.LineSegments(
        new THREE.EdgesGeometry(geometry),
        new THREE.LineBasicMaterial({ color: 0x5eead4, transparent: true, opacity: 0.42 }),
      )
      edges.position.copy(mesh.position)
      campus.add(edges)
    })

    const routes = [
      [-2.7, 0, 2.7, 0],
      [-1.35, -2.25, -1.35, 2.25],
      [1.35, -2.25, 1.35, 2.25],
    ]
    routes.forEach(([x1, z1, x2, z2]) => {
      const geometry = new THREE.BufferGeometry().setFromPoints([
        new THREE.Vector3(x1, 0.17, z1),
        new THREE.Vector3(x2, 0.17, z2),
      ])
      campus.add(new THREE.Line(
        geometry,
        new THREE.LineBasicMaterial({ color: 0x818cf8, transparent: true, opacity: 0.65 }),
      ))
    })

    const nodeMaterial = new THREE.MeshBasicMaterial({ color: 0x5eead4 })
    ;[[-2.7, 0], [2.7, 0], [-1.35, -2.25], [-1.35, 2.25], [1.35, -2.25], [1.35, 2.25]].forEach(([x, z]) => {
      const node = new THREE.Mesh(new THREE.BoxGeometry(0.13, 0.13, 0.13), nodeMaterial.clone())
      node.position.set(x, 0.22, z)
      campus.add(node)
    })

    scene.add(new THREE.AmbientLight(0xdbeafe, 1.35))
    const keyLight = new THREE.DirectionalLight(0x5eead4, 3.8)
    keyLight.position.set(4, 7, 5)
    scene.add(keyLight)
    const accentLight = new THREE.PointLight(0x818cf8, 6, 13)
    accentLight.position.set(-4, 2, 2)
    scene.add(accentLight)

    const resize = () => {
      if (!host || !renderer) return
      const width = host.clientWidth || 1
      const height = host.clientHeight || 1
      camera.aspect = width / height
      camera.updateProjectionMatrix()
      renderer.setSize(width, height, false)
    }

    resizeObserver = new ResizeObserver(resize)
    if (host) resizeObserver.observe(host)
    resize()

    const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    if (!reducedMotion) window.addEventListener('pointermove', onPointerMove, { passive: true })

    const render = () => {
      if (!reducedMotion) {
        campus.rotation.y += (targetX - campus.rotation.y) * 0.035
        campus.rotation.x += (-targetY - campus.rotation.x) * 0.035
      }
      renderer?.render(scene!, camera)
      frame = requestAnimationFrame(render)
    }
    render()
  } catch {
    renderer = undefined
  }
})

onBeforeUnmount(() => {
  cancelAnimationFrame(frame)
  window.removeEventListener('pointermove', onPointerMove)
  resizeObserver?.disconnect()
  if (scene) disposeObject(scene)
  renderer?.dispose()
})
</script>

<template>
  <canvas ref="canvas" class="three-scene" aria-hidden="true" />
</template>
