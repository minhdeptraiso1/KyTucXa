<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import BaseButton from '@/components/base/BaseButton.vue'
import BrandLogo from '@/components/brand/BrandLogo.vue'
import { useAuthStore } from '@/stores/auth'
import {
  facilityService,
  type FacilityOverviewStats,
  type PricePolicy,
  type PublicRoomSummary,
} from '@/services/facilityService'

const router = useRouter()
const auth = useAuthStore()
const DEFAULT_ROOM_IMAGE = '/images/ktx_room.jpg'
const publicRooms = ref<PublicRoomSummary[]>([])
const publicPolicies = ref<PricePolicy[]>([])
const homeDataLoading = ref(true)
const homeDataError = ref('')
const overview = ref<FacilityOverviewStats>({
  totalBuildings: 0,
  totalFloors: 0,
  totalRooms: 0,
  availableRooms: 0,
  fullRooms: 0,
  maintenanceRooms: 0,
  totalBeds: 0,
  availableBeds: 0,
  occupiedBeds: 0,
  maintenanceBeds: 0,
  occupancyRate: 0,
})

// ── SCROLL STATE & INTERACTIVITY ──
const scrollY = ref(0)
const scrollProgress = ref(0)
const isScrolled = computed(() => scrollY.value > 20)
const showBackToTop = computed(() => scrollY.value > 350)
const activeSection = ref('hero')

// Hero Parallax calculations
const heroBgTransform = computed(() => {
  const y = scrollY.value * 0.35
  const scale = 1 + scrollY.value * 0.00025
  return `translate3d(0, ${y}px, 0) scale(${Math.min(scale, 1.25)})`
})

const heroContentStyle = computed(() => {
  const fade = Math.max(0, 1 - scrollY.value / 650)
  const y = -scrollY.value * 0.16
  return {
    opacity: fade,
    transform: `translate3d(0, ${y}px, 0)`,
  }
})

// Circular progress ring parameters (r = 18, circumference = 2 * PI * 18 ≈ 113.1)
const ringCircumference = 2 * Math.PI * 18
const ringOffset = computed(() => {
  return ringCircumference - (scrollProgress.value / 100) * ringCircumference
})

function handleScroll() {
  const currentY = window.scrollY
  scrollY.value = currentY

  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  scrollProgress.value = docHeight > 0 ? Math.min(100, Math.max(0, (currentY / docHeight) * 100)) : 0

  // Scroll spy active section detection
  if (window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 60) {
    activeSection.value = 'contact'
  } else {
    const sections = ['hero', 'rooms', 'amenities', 'phases', 'contact']
    for (const id of sections) {
      const el = document.getElementById(id)
      if (el) {
        const rect = el.getBoundingClientRect()
        if (rect.top <= 220 && rect.bottom >= 120) {
          activeSection.value = id
        }
      }
    }
  }
}

function scrollToSection(id: string) {
  const el = document.getElementById(id)
  if (el) {
    const navOffset = 65
    const targetY = el.getBoundingClientRect().top + window.scrollY - navOffset
    window.scrollTo({ top: targetY, behavior: 'smooth' })
  }
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function goToRegister() {
  const token = localStorage.getItem('ktx.accessToken')
  if (token) {
    router.push(auth.homePath)
  } else {
    router.push('/register')
  }
}

// ── ANIMATED NUMERICAL COUNTERS (Scroll-Triggered) ──
const statsVisible = ref(false)
const counts = ref({
  places: 0,
  buildings: 0,
  satisfaction: 0,
  available: 0,
})
const statsInView = ref(false)

function startCounting() {
  if (statsVisible.value || homeDataLoading.value) return
  statsVisible.value = true

  const duration = 1600
  const startTime = performance.now()

  function step(now: number) {
    const elapsed = now - startTime
    const progress = Math.min(elapsed / duration, 1)
    // Ease out quad
    const ease = 1 - (1 - progress) * (1 - progress)

    counts.value.places = Math.floor(ease * overview.value.totalBeds)
    counts.value.buildings = Math.floor(ease * overview.value.totalBuildings)
    counts.value.satisfaction = Math.round(ease * overview.value.occupancyRate * 10) / 10
    counts.value.available = Math.floor(ease * overview.value.availableBeds)

    if (progress < 1) {
      requestAnimationFrame(step)
    } else {
      counts.value.places = overview.value.totalBeds
      counts.value.buildings = overview.value.totalBuildings
      counts.value.satisfaction = overview.value.occupancyRate
      counts.value.available = overview.value.availableBeds
    }
  }
  requestAnimationFrame(step)
}

// ── INTERSECTION OBSERVER FOR SCROLL REVEALS ──
let revealObserver: IntersectionObserver | undefined
let statsObserver: IntersectionObserver | undefined

function observeRevealElements() {
  document.querySelectorAll('.scroll-reveal').forEach((el) => {
    revealObserver?.observe(el)
  })
}

async function loadHomeData() {
  homeDataLoading.value = true
  homeDataError.value = ''
  try {
    const [roomResult, overviewResult, policyResult] = await Promise.all([
      facilityService.getPublicAvailableRooms(),
      facilityService.getPublicOverview(),
      facilityService.getPublicActivePricePolicies(),
    ])
    publicRooms.value = roomResult.data ?? []
    overview.value = overviewResult.data ?? overview.value
    publicPolicies.value = policyResult.data ?? []
  } catch {
    publicRooms.value = []
    publicPolicies.value = []
    homeDataError.value = 'Không thể tải dữ liệu phòng từ hệ thống. Vui lòng thử lại sau.'
  } finally {
    homeDataLoading.value = false
    if (statsInView.value) startCounting()
    await nextTick()
    observeRevealElements()
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  handleScroll()

  // Setup scroll reveals
  revealObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-revealed')
        }
      })
    },
    { threshold: 0.12, rootMargin: '0px 0px -40px 0px' },
  )

  observeRevealElements()

  // Setup stats counter observer
  const statsEl = document.getElementById('stats-strip')
  if (statsEl) {
    statsObserver = new IntersectionObserver(
      (entries) => {
        statsInView.value = Boolean(entries[0]?.isIntersecting)
        if (statsInView.value) {
          startCounting()
        }
      },
      { threshold: 0.2 },
    )
    statsObserver.observe(statsEl)
  }

  void loadHomeData()
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
  revealObserver?.disconnect()
  statsObserver?.disconnect()
})

const roomTypeMeta = {
  STANDARD_8: { name: 'Phòng tiêu chuẩn 8 người', badge: 'Tiêu chuẩn', badgeColor: '#10b981' },
  STANDARD_6: { name: 'Phòng tiêu chuẩn 6 người', badge: 'Tiêu chuẩn', badgeColor: '#0f9f92' },
  PREMIUM_4: { name: 'Phòng chất lượng cao 4 người', badge: 'Chất lượng cao', badgeColor: '#6366f1' },
} as const

function formatMoney(value: number) {
  return new Intl.NumberFormat('vi-VN').format(value)
}

function genderLabel(values: PublicRoomSummary['genderType'][]) {
  const unique = [...new Set(values)]
  if (unique.includes('MIXED') || unique.length > 1) return 'Phòng dành cho nam và nữ'
  return unique[0] === 'FEMALE' ? 'Khu phòng nữ' : 'Khu phòng nam'
}

function handleRoomImageError(event: Event) {
  const image = event.target as HTMLImageElement
  image.onerror = null
  image.src = DEFAULT_ROOM_IMAGE
}

const rooms = computed(() => Object.keys(roomTypeMeta).flatMap((roomType) => {
  const typedRooms = publicRooms.value.filter((room) => room.roomType === roomType)
  if (!typedRooms.length) return []
  const type = roomType as PublicRoomSummary['roomType']
  const meta = roomTypeMeta[type]
  const buildings = [...new Set(typedRooms.map((room) => room.buildingCode))]
  const prices = typedRooms.map((room) => room.pricePerMonth).filter((price): price is number => price != null)
  const policyPrice = publicPolicies.value.find((policy) => policy.roomType === type)?.pricePerMonth
  const areaValues = typedRooms.map((room) => room.areaSqm).filter((area): area is number => area != null)
  const description = typedRooms.find((room) => room.description?.trim())?.description?.trim()
  const image = typedRooms.find((room) => room.imageUrl?.trim())?.imageUrl || DEFAULT_ROOM_IMAGE
  return [{
    id: type,
    roomType: type,
    name: meta.name,
    capacity: `${typedRooms[0]?.capacity ?? 0} người / phòng`,
    price: prices.length ? Math.min(...prices) : policyPrice,
    image,
    features: [
      `${typedRooms.length} phòng đang còn chỗ`,
      `Tại tòa ${buildings.join(', ')}`,
      genderLabel(typedRooms.map((room) => room.genderType)),
      areaValues.length ? `Diện tích từ ${Math.min(...areaValues)} m²` : '',
      description || '',
    ].filter(Boolean),
    badge: meta.badge,
    badgeColor: meta.badgeColor,
    available: typedRooms.reduce((sum, room) => sum + room.availableBeds, 0),
  }]
}))

const amenities = [
  { index: '01', icon: 'gym', label: 'Rèn luyện thể chất', desc: 'Phòng gym hiện đại, bàn bóng bàn và khu thể thao ngoài trời phục vụ từ 05:00 đến 22:00.', meta: 'Hằng ngày' },
  { index: '02', icon: 'kitchen', label: 'Bếp và sinh hoạt chung', desc: 'Bếp từ an toàn, lò vi sóng, tủ lạnh và không gian bàn ăn chung theo từng tầng.', meta: 'Theo tầng' },
  { index: '03', icon: 'laundry', label: 'Giặt sấy tự phục vụ', desc: 'Máy giặt công nghiệp, máy sấy nhanh; đặt lịch và theo dõi trạng thái trực tuyến.', meta: 'Trực tuyến' },
  { index: '04', icon: 'security', label: 'An ninh số & Mạng Gigabit', desc: 'Hệ thống WiFi campus phủ sóng toàn diện, cổng từ ra vào khuôn viên và camera 24/7.', meta: '24/7' },
  { index: '05', icon: 'bike', label: 'Bãi xe thông minh có mái che', desc: 'Khu đỗ xe máy và xe đạp riêng biệt của sinh viên nội trú với hệ thống quẹt thẻ kiểm soát.', meta: 'Có giám sát' },
]

const roomStatusPhases = computed(() => [...publicRooms.value]
  .sort((left, right) => right.availableBeds - left.availableBeds)
  .slice(0, 6)
  .map((room) => ({
    id: room.id,
    roomNo: `${room.buildingCode}${room.floorNumber}-${room.roomNumber}`,
    building: `${room.buildingName} · Tầng ${room.floorNumber}`,
    roomType: roomTypeMeta[room.roomType].name,
    available: room.availableBeds,
    capacity: room.capacity,
    gender: genderLabel([room.genderType]),
    price: room.pricePerMonth,
  })))
</script>

<template>
  <div class="home-root">
    <!-- ── SCROLL PROGRESS BAR (Glowing top edge indicator) ── -->
    <div
      class="scroll-progress-bar"
      :style="{ width: `${scrollProgress}%` }"
      aria-hidden="true"
    />

    <!-- ── FLOATING BACK-TO-TOP BUTTON WITH PROGRESS RING ── -->
    <BaseButton
      variant="ghost"
      size="sm"
      class="back-to-top-btn"
      :class="{ 'back-to-top-btn--visible': showBackToTop }"
      aria-label="Cuộn lên đầu trang"
      @click="scrollToTop"
    >
      <svg class="back-to-top-btn__ring" width="46" height="46" viewBox="0 0 46 46">
        <circle
          cx="23"
          cy="23"
          r="18"
          stroke="rgba(255, 255, 255, 0.15)"
          stroke-width="3"
          fill="none"
        />
        <circle
          cx="23"
          cy="23"
          r="18"
          stroke="#2dd4bf"
          stroke-width="3"
          fill="none"
          stroke-linecap="round"
          :stroke-dasharray="ringCircumference"
          :stroke-dashoffset="ringOffset"
        />
      </svg>
      <svg
        class="back-to-top-btn__icon"
        viewBox="0 0 24 24"
        width="18"
        height="18"
        fill="none"
        stroke="currentColor"
        stroke-width="2.5"
      >
        <path stroke-linecap="round" stroke-linejoin="round" d="M5 10l7-7m0 0l7 7m-7-7v18" />
      </svg>
    </BaseButton>

    <!-- ── DYNAMIC STICKY NAV BAR WITH SCROLL SPY ── -->
    <nav
      class="home-nav"
      :class="{ 'home-nav--scrolled': isScrolled }"
      aria-label="Điều hướng chính"
    >
      <BrandLogo tone="inverse" compact />

      <!-- Center navigation links with scroll spy -->
      <ul class="home-nav__links" role="menubar">
        <li role="none">
          <BaseButton
            variant="ghost"
            size="sm"
            class="home-nav__link-btn"
            :class="{ 'home-nav__link-btn--active': activeSection === 'hero' }"
            @click="scrollToSection('hero')"
          >
            Trang chủ
          </BaseButton>
        </li>
        <li role="none">
          <BaseButton
            variant="ghost"
            size="sm"
            class="home-nav__link-btn"
            :class="{ 'home-nav__link-btn--active': activeSection === 'rooms' }"
            @click="scrollToSection('rooms')"
          >
            Loại phòng
          </BaseButton>
        </li>
        <li role="none">
          <BaseButton
            variant="ghost"
            size="sm"
            class="home-nav__link-btn"
            :class="{ 'home-nav__link-btn--active': activeSection === 'amenities' }"
            @click="scrollToSection('amenities')"
          >
            Tiện nghi
          </BaseButton>
        </li>
        <li role="none">
          <BaseButton
            variant="ghost"
            size="sm"
            class="home-nav__link-btn"
            :class="{ 'home-nav__link-btn--active': activeSection === 'phases' }"
            @click="scrollToSection('phases')"
          >
            Tình trạng phòng
          </BaseButton>
        </li>
        <li role="none">
          <BaseButton
            variant="ghost"
            size="sm"
            class="home-nav__link-btn"
            :class="{ 'home-nav__link-btn--active': activeSection === 'contact' }"
            @click="scrollToSection('contact')"
          >
            Liên hệ
          </BaseButton>
        </li>
      </ul>

      <div class="home-nav__actions">
        <template v-if="auth.isAuthenticated">
          <span class="hidden max-w-52 truncate text-sm font-semibold text-white/90 lg:inline">
            Xin chào, {{ auth.displayName }}
          </span>
          <BaseButton size="sm" @click="router.push(auth.homePath)">
            Vào hệ thống
          </BaseButton>
        </template>
        <template v-else>
          <BaseButton variant="tertiary" size="sm" class="home-nav__login" @click="router.push('/login')">
            Đăng nhập
          </BaseButton>
          <BaseButton size="sm" @click="goToRegister">
            Đăng ký thuê phòng
          </BaseButton>
        </template>
      </div>
    </nav>

    <!-- ── HERO SECTION WITH PARALLAX & DEPTH ── -->
    <section id="hero" class="home-hero" aria-label="Giới thiệu ký túc xá">
      <div class="home-hero__bg">
        <img
          src="/images/ktx_hero.jpg"
          alt="Ký túc xá Trường Đại học Công Nghệ"
          class="home-hero__img"
          :style="{ transform: heroBgTransform }"
        />
        <div class="home-hero__overlay" />
      </div>

      <div class="home-hero__content" :style="heroContentStyle">
        <span class="home-eyebrow animate-fade-in">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
            <path d="M12 2L1 21h22L12 2zm0 3.84L19.46 19H4.54L12 5.84zM11 11h2v4h-2zm0 6h2v2h-2z" />
          </svg>
          KÝ TÚC XÁ TRƯỜNG ĐẠI HỌC CÔNG NGHỆ (VNU - UET)
        </span>

        <h1 class="home-hero__title">
          Ngôi nhà thứ hai<br />
          <span class="home-hero__highlight">dành cho sinh viên</span>
        </h1>

        <p class="home-hero__sub">
          Không gian sống an toàn, hiện đại, tiện nghi ngay cạnh khuôn viên giảng đường tại<br />
          <strong>144 Xuân Thủy, Dịch Vọng Hậu, Cầu Giấy, Hà Nội</strong>
        </p>

        <div class="home-hero__ctas">
          <BaseButton size="lg" @click="goToRegister">
            {{ auth.isAuthenticated ? 'Vào hồ sơ nội trú của tôi' : 'Đăng ký thuê phòng ngay' }}
          </BaseButton>
          <BaseButton variant="secondary" size="lg" class="home-btn home-btn--outline" @click="scrollToSection('rooms')">
            Xem các loại phòng
          </BaseButton>
        </div>

        <p class="home-hero__trust">
          Đăng ký trực tuyến · Hợp đồng số · Thanh toán minh bạch — Vận hành trên một nền tảng
        </p>
      </div>
    </section>

    <!-- ── ANIMATED STATS COUNTER STRIP (Scroll-triggered) ── -->
    <div id="stats-strip" class="home-stats-strip">
      <div class="home-stats-strip__inner">
        <div class="stat-card scroll-reveal">
          <span class="stat-card__number">{{ counts.places.toLocaleString('vi-VN') }}</span>
          <span class="stat-card__label">Tổng số giường trong hệ thống</span>
        </div>
        <div class="stat-card scroll-reveal delay-100">
          <span class="stat-card__number">{{ counts.buildings }}</span>
          <span class="stat-card__label">Tòa nhà KTX đang quản lý</span>
        </div>
        <div class="stat-card scroll-reveal delay-200">
          <span class="stat-card__number">{{ counts.satisfaction }}%</span>
          <span class="stat-card__label">Tỷ lệ giường đang được sử dụng</span>
        </div>
        <div class="stat-card scroll-reveal delay-300">
          <span class="stat-card__number">{{ counts.available.toLocaleString('vi-VN') }}</span>
          <span class="stat-card__label">Giường đang sẵn sàng đăng ký</span>
        </div>
      </div>
    </div>

    <!-- ── ADDRESS INFO STRIP ── -->
    <div class="home-address-strip">
      <div class="home-address-strip__inner">
        <span class="address-item">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" />
            <circle cx="12" cy="9" r="2.5" />
          </svg>
          144 Xuân Thủy, Dịch Vọng Hậu, Cầu Giấy, Hà Nội
        </span>
        <span class="address-item">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z" />
          </svg>
          Hotline: <strong>024 3754 7777</strong>
        </span>
        <span class="address-item">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <polyline points="12 6 12 12 16 14" />
          </svg>
          Văn phòng: 7h30 – 17h00 (Thứ 2 – Thứ 6)
        </span>
      </div>
    </div>

    <!-- ── ROOM TYPES SECTION (Scroll Reveal) ── -->
    <section id="rooms" class="home-section">
      <div class="home-section__header scroll-reveal">
        <span class="home-section__eyebrow">CÁC LOẠI PHÒNG Ở NỘI TRÚ</span>
        <h2 class="home-section__title">Chọn không gian sống phù hợp</h2>
        <p class="home-section__sub">
          Đa dạng loại phòng từ tiêu chuẩn đến chất lượng cao, trang bị đầy đủ tiện nghi, an toàn tuyệt đối cho sinh viên học tập.
        </p>
      </div>

      <p v-if="homeDataLoading" class="home-data-state">Đang tải dữ liệu phòng từ hệ thống...</p>
      <p v-else-if="homeDataError" class="home-data-state home-data-state--error">{{ homeDataError }}</p>
      <p v-else-if="!rooms.length" class="home-data-state">Hiện chưa có phòng còn chỗ để đăng ký.</p>
      <div v-else class="home-rooms">
        <article
          v-for="(room, index) in rooms"
          :key="room.id"
          class="home-room-card scroll-reveal"
          :class="`delay-${(index + 1) * 150}`"
        >
          <div class="home-room-card__img-wrap">
            <img :src="room.image" :alt="room.name" class="home-room-card__img" @error="handleRoomImageError" />
            <span class="home-room-card__badge" :style="{ background: room.badgeColor }">
              {{ room.badge }}
            </span>
            <span class="home-room-card__avail">
              <span class="avail-dot" /> Còn {{ room.available }} chỗ
            </span>
          </div>

          <div class="home-room-card__body">
            <div class="home-room-card__top">
              <h3 class="home-room-card__name">{{ room.name }}</h3>
              <span class="home-room-card__capacity">{{ room.capacity }}</span>
            </div>

            <div class="home-room-card__price">
              <template v-if="room.price != null">₫{{ formatMoney(room.price) }}<span>/tháng/sinh viên</span></template>
              <template v-else><span>Liên hệ Ban quản lý để cập nhật giá</span></template>
            </div>

            <ul class="home-room-card__features">
              <li v-for="f in room.features" :key="f">
                <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                </svg>
                <span>{{ f }}</span>
              </li>
            </ul>

            <BaseButton block class="home-room-card__action" @click="goToRegister">
              Đăng ký phòng này
            </BaseButton>
          </div>
        </article>
      </div>
    </section>

    <!-- ── AMENITIES SECTION (Scroll Reveal) ── -->
    <section id="amenities" class="home-section home-section--alt" aria-label="Tiện nghi ký túc xá">
      <div class="home-section__header scroll-reveal">
        <span class="home-section__eyebrow">TIỆN NGHI & CƠ SỞ VẬT CHẤT</span>
        <h2 class="home-section__title">Đầy đủ tiện ích phục vụ sinh viên</h2>
        <p class="home-section__sub">Môi trường sống hiện đại, an toàn và gắn kết, giúp sinh viên yên tâm tập trung học tập và nghiên cứu.</p>
      </div>

      <div class="home-amenities-showcase">
        <figure class="home-amenities-visual scroll-reveal">
          <img
            src="/images/ktx_amenities.jpg"
            alt="Không gian tiện ích và sinh hoạt chung tại ký túc xá"
            class="home-amenities-img"
          />
        </figure>

        <div class="home-amenities-list">
          <article
            v-for="(a, index) in amenities"
            :key="a.index"
            class="home-amenity-row scroll-reveal"
            :class="`delay-${index * 100}`"
          >
            <span class="home-amenity-row__index">{{ a.index }}</span>
            <div class="home-amenity-row__content">
              <h3>{{ a.label }}</h3>
              <p>{{ a.desc }}</p>
            </div>
            <span class="home-amenity-row__meta">{{ a.meta }}</span>
          </article>
        </div>
      </div>
    </section>

    <!-- ── ROOM STATUS TRACKING (Scroll Reveal) ── -->
    <section id="phases" class="home-section" aria-label="Theo dõi tình trạng phòng">
      <div class="home-section__header scroll-reveal">
        <span class="home-section__eyebrow">THEO DÕI TÌNH TRẠNG PHÒNG TRỐNG</span>
        <h2 class="home-section__title">Phòng đang còn chỗ — Đăng ký sớm</h2>
        <p class="home-section__sub">
          Danh sách được cập nhật trực tiếp từ tình trạng phòng và giường hiện tại trong hệ thống.
        </p>
      </div>

      <p v-if="homeDataLoading" class="home-data-state">Đang tải tình trạng phòng...</p>
      <p v-else-if="homeDataError" class="home-data-state home-data-state--error">{{ homeDataError }}</p>
      <p v-else-if="!roomStatusPhases.length" class="home-data-state">Hiện chưa có phòng khả dụng.</p>
      <div v-else class="home-phase-grid">
        <div
          v-for="(r, index) in roomStatusPhases"
          :key="r.id"
          class="home-phase-card home-phase-card--ok scroll-reveal"
          :class="`delay-${index * 120}`"
        >
          <div class="home-phase-card__header">
            <div>
              <span class="home-phase-card__room">{{ r.roomNo }}</span>
              <span class="home-phase-card__building">{{ r.building }}</span>
            </div>
            <span class="home-phase-card__pulse-badge">
              Sẵn sàng
            </span>
          </div>

          <div class="home-phase-card__body">
            <div class="home-phase-card__detail">
              <span>Loại phòng:</span>
              <strong>{{ r.roomType }}</strong>
            </div>
            <div class="home-phase-card__detail">
              <span>Chỗ còn lại:</span>
              <strong>{{ r.available }} / {{ r.capacity }} giường</strong>
            </div>
            <div class="home-phase-card__detail">
              <span>Giá niêm yết:</span>
              <strong>{{ r.price != null ? `${formatMoney(r.price)} đ/tháng` : 'Chưa cập nhật' }}</strong>
            </div>
          </div>

          <BaseButton block variant="secondary" @click="goToRegister">
            Đăng ký giữ chỗ phòng này
          </BaseButton>
        </div>
      </div>
    </section>

    <!-- ── CONTACT & SUPPORT SECTION (Scroll Reveal) ── -->
    <section id="contact" class="home-section home-section--alt" aria-label="Thông tin liên hệ Ban Quản lý">
      <div class="home-section__header scroll-reveal">
        <span class="home-section__eyebrow">LIÊN HỆ &amp; HỖ TRỢ</span>
        <h2 class="home-section__title">Ban Quản lý Ký túc xá UET</h2>
        <p class="home-section__sub">
          Đội ngũ cán bộ quản lý và bộ phận hỗ trợ kỹ thuật luôn sẵn sàng tiếp nhận thông tin, giải đáp thắc mắc và đồng hành cùng sinh viên.
        </p>
      </div>

      <div class="home-contact-grid">
        <!-- Card 1: Văn phòng -->
        <div class="home-contact-card scroll-reveal">
          <div class="home-contact-card__icon-box">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" />
              <circle cx="12" cy="9" r="2.5" />
            </svg>
          </div>
          <h3 class="home-contact-card__heading">Văn phòng Ban Quản lý</h3>
          <p class="home-contact-card__text">
            Phòng 102, Tòa nhà KTX - Khu KTX Mễ Trì / Cầu Giấy, 144 Xuân Thủy, Cầu Giấy, Hà Nội
          </p>
          <div class="home-contact-card__details">
            <div class="contact-detail-row">
              <span class="detail-label">Thứ 2 – Thứ 6:</span>
              <span class="detail-val">07:30 – 17:00</span>
            </div>
            <div class="contact-detail-row">
              <span class="detail-label">Thứ 7:</span>
              <span class="detail-val">08:00 – 11:30</span>
            </div>
            <div class="contact-detail-row">
              <span class="detail-label">Chủ nhật & Ngày lễ:</span>
              <span class="detail-val text-muted">Trực bảo vệ 24/7</span>
            </div>
          </div>
        </div>

        <!-- Card 2: Đường dây nóng -->
        <div class="home-contact-card scroll-reveal delay-100">
          <div class="home-contact-card__icon-box">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z" />
            </svg>
          </div>
          <h3 class="home-contact-card__heading">Đường dây nóng 24/7</h3>
          <p class="home-contact-card__text">
            Hỗ trợ tư vấn thuê phòng, thủ tục hồ sơ và xử lý kịp thời các sự cố kỹ thuật nội trú.
          </p>
          <div class="home-contact-card__details">
            <div class="contact-detail-row">
              <span class="detail-label">Tổng đài tư vấn:</span>
              <a href="tel:02437547777" class="detail-link">024 3754 7777</a>
            </div>
            <div class="contact-detail-row">
              <span class="detail-label">Trực ban bảo vệ 24/7:</span>
              <a href="tel:02437548888" class="detail-link">024 3754 8888</a>
            </div>
            <div class="contact-detail-row">
              <span class="detail-label">Kỹ thuật & Điện nước:</span>
              <a href="tel:0988123456" class="detail-link">0988 123 456</a>
            </div>
          </div>
        </div>

        <!-- Card 3: Kênh trực tuyến -->
        <div class="home-contact-card scroll-reveal delay-200">
          <div class="home-contact-card__icon-box">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="2" y="4" width="20" height="16" rx="2" />
              <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7" />
            </svg>
          </div>
          <h3 class="home-contact-card__heading">Kênh thông tin trực tuyến</h3>
          <p class="home-contact-card__text">
            Tiếp nhận đăng ký nội trú, phản hồi ý kiến sinh viên và giải quyết các thủ tục số.
          </p>
          <div class="home-contact-card__details">
            <div class="contact-detail-row">
              <span class="detail-label">Hộp thư hỗ trợ:</span>
              <a href="mailto:ktx@vnu.edu.vn" class="detail-link">ktx@vnu.edu.vn</a>
            </div>
            <div class="contact-detail-row">
              <span class="detail-label">Ban Quản lý:</span>
              <a href="mailto:bqlktx@uet.vnu.edu.vn" class="detail-link">bqlktx@uet.vnu.edu.vn</a>
            </div>
            <div class="contact-detail-row">
              <span class="detail-label">Cổng thông tin KTX:</span>
              <span class="detail-val">ktx.uet.vnu.edu.vn</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ── CTA SECTION (Scroll Reveal) ── -->
    <section id="cta" class="home-cta">
      <div class="home-cta__inner scroll-reveal">
        <h2 class="home-cta__title">
          {{ auth.isAuthenticated ? `Chào mừng trở lại, ${auth.displayName}` : 'Sẵn sàng trải nghiệm cuộc sống KTX?' }}
        </h2>
        <p class="home-cta__sub">
          {{ auth.isAuthenticated
            ? 'Tiếp tục theo dõi hồ sơ đăng ký, giường nội trú và hợp đồng của bạn ngay hôm nay.'
            : 'Hồ sơ xét duyệt trực tuyến nhanh chóng, minh bạch. Đội ngũ quản lý luôn sẵn sàng đồng hành cùng sinh viên.'
          }}
        </p>
        <div class="home-cta__btns">
          <BaseButton size="lg" @click="goToRegister">
            {{ auth.isAuthenticated ? 'Vào hồ sơ nội trú' : 'Bắt đầu đăng ký ngay' }}
          </BaseButton>
          <BaseButton
            v-if="!auth.isAuthenticated"
            size="lg"
            variant="secondary"
            class="home-cta__secondary"
            @click="router.push('/login')"
          >
            Đăng nhập tài khoản
          </BaseButton>
        </div>
      </div>
    </section>

    <!-- ── FOOTER ── -->
    <footer class="home-footer">
      <div class="home-footer__inner">
        <BrandLogo tone="inverse" />
        <p class="home-footer__addr">
          144 Xuân Thủy, Dịch Vọng Hậu, Cầu Giấy, Hà Nội &nbsp;|&nbsp;
          Hotline: 024 3754 7777 &nbsp;|&nbsp;
          Email: ktx@vnu.edu.vn
        </p>
        <p class="home-footer__copy">
          © 2026 Trường Đại học Công Nghệ - Đại học Quốc gia Hà Nội. Hệ thống quản lý Ký túc xá số hóa.
        </p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* ── ROOT ── */
.home-root {
  min-height: 100vh;
  font-family: var(--font-sans, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif);
  color: var(--color-ink, #0f172a);
  background: var(--color-bg, #f8fafc);
  position: relative;
  overflow-x: hidden;
}

/* ── SCROLL PROGRESS BAR ── */
.scroll-progress-bar {
  position: fixed;
  top: 0;
  left: 0;
  height: 3.5px;
  background: linear-gradient(90deg, #0d9488 0%, #2dd4bf 50%, #818cf8 100%);
  z-index: 999;
  box-shadow: 0 0 12px rgba(45, 212, 191, 0.7);
  transition: width 0.08s linear;
  pointer-events: none;
}

/* ── FLOATING BACK-TO-TOP BUTTON ── */
.back-to-top-btn {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  width: 48px;
  height: 48px;
  border-radius: 9999px;
  background: rgba(15, 23, 42, 0.88);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.5), 0 0 15px rgba(45, 212, 191, 0.2);
  cursor: pointer;
  z-index: 150;
  opacity: 0;
  pointer-events: none;
  transform: translateY(16px) scale(0.9);
  transition: opacity 0.3s cubic-bezier(0.16, 1, 0.3, 1), transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.back-to-top-btn--visible {
  opacity: 1;
  pointer-events: auto;
  transform: translateY(0) scale(1);
}

.back-to-top-btn:hover {
  background: rgba(15, 23, 42, 0.98);
  box-shadow: 0 15px 30px -5px rgba(0, 0, 0, 0.6), 0 0 22px rgba(45, 212, 191, 0.4);
  transform: translateY(-3px) scale(1.05);
}

.back-to-top-btn__ring {
  position: absolute;
  top: 1px;
  left: 1px;
  transform: rotate(-90deg);
  pointer-events: none;
}

.back-to-top-btn__icon {
  position: relative;
  z-index: 1;
  color: #2dd4bf;
  transition: transform 0.2s ease;
}

.back-to-top-btn:hover .back-to-top-btn__icon {
  transform: translateY(-2px);
}

/* ── NAV BAR ── */
.home-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 clamp(1.5rem, 5vw, 4rem);
  height: 4.25rem;
  background: rgba(7, 13, 23, 0.65);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.35s ease;
}

.home-nav--scrolled {
  background: rgba(7, 13, 23, 0.92);
  backdrop-filter: blur(20px);
  height: 3.75rem;
  border-bottom: 1px solid rgba(45, 212, 191, 0.15);
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.5);
}

.home-nav__links {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  list-style: none;
  margin: 0;
  padding: 0;
}

.home-nav__link-btn {
  background: transparent;
  border: none;
  color: #cbd5e1;
  font-size: 0.88rem;
  font-weight: 500;
  padding: 0.4rem 0.85rem;
  border-radius: 9999px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.home-nav__link-btn:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.08);
}

.home-nav__link-btn--active {
  color: #2dd4bf;
  background: rgba(45, 212, 191, 0.12);
  font-weight: 600;
}

.home-nav__actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.home-nav__login {
  color: #e2e8f0;
}

@media (max-width: 900px) {
  .home-nav__links {
    display: none;
  }
}

/* ── HERO ── */
.home-hero {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  overflow: hidden;
  padding-top: 5rem;
  padding-bottom: 4rem;
}

.home-hero__bg {
  position: absolute;
  inset: -5% 0 0 0;
  height: 120%;
  pointer-events: none;
  overflow: hidden;
}

.home-hero__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center 30%;
  will-change: transform;
}

.home-hero__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(7, 13, 23, 0.92) 0%,
    rgba(7, 13, 23, 0.72) 48%,
    rgba(7, 13, 23, 0.88) 100%
  );
}

.home-hero__content {
  position: relative;
  z-index: 2;
  max-width: 72rem;
  margin: 0 auto;
  padding: 2rem clamp(1.5rem, 5vw, 4rem);
  color: #ffffff;
  will-change: transform, opacity;
}

.home-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: #2dd4bf;
  font: 700 0.75rem "DM Mono", monospace;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  margin-bottom: 1.25rem;
  padding: 0.35rem 0.85rem;
  border-radius: 9999px;
  background: rgba(45, 212, 191, 0.12);
  border: 1px solid rgba(45, 212, 191, 0.25);
  backdrop-filter: blur(8px);
}

.home-hero__title {
  font: 800 clamp(2.8rem, 6.2vw, 5.6rem) var(--font-display);
  line-height: 1.06;
  letter-spacing: -0.04em;
  margin: 0 0 1.35rem;
  color: #ffffff;
}

.home-hero__highlight {
  background: linear-gradient(135deg, #2dd4bf 0%, #38bdf8 50%, #818cf8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.home-hero__sub {
  color: #cbd5e1;
  font-size: clamp(1rem, 1.5vw, 1.18rem);
  line-height: 1.7;
  margin: 0 0 2rem;
  max-width: 48rem;
}

.home-hero__sub strong {
  color: #f1f5f9;
}

.home-hero__ctas {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  margin-bottom: 1.5rem;
}

.home-btn--outline {
  background: rgba(255, 255, 255, 0.05);
  border: 1.5px solid rgba(255, 255, 255, 0.3);
  color: #ffffff;
  align-items: center;
  border-radius: var(--radius-md, 0.5rem);
  display: inline-flex;
  font: 600 0.95rem var(--font-display);
  min-height: 3.25rem;
  padding: 0 1.5rem;
  cursor: pointer;
  backdrop-filter: blur(8px);
  transition: all 0.2s ease;
}

.home-btn--outline:hover {
  background: rgba(45, 212, 191, 0.15);
  border-color: #2dd4bf;
  color: #2dd4bf;
  transform: translateY(-2px);
}

.home-hero__trust {
  border-left: 2.5px solid #2dd4bf;
  color: #94a3b8;
  font: 500 0.84rem "DM Mono", monospace;
  letter-spacing: 0.02em;
  margin: 0 0 2rem;
  padding: 0.25rem 0 0.25rem 0.85rem;
}

/* ── STATS STRIP (LIVE COUNTER) ── */
.home-stats-strip {
  background: #09121f;
  border-top: 1px solid rgba(45, 212, 191, 0.2);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  padding: 2.5rem clamp(1.5rem, 5vw, 4rem);
}

.home-stats-strip__inner {
  max-width: 72rem;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(13rem, 1fr));
  gap: 2rem;
}

.stat-card {
  display: flex;
  flex-direction: column;
  border-left: 2px solid rgba(45, 212, 191, 0.4);
  padding-left: 1.25rem;
}

.stat-card__number {
  font: 800 clamp(2.2rem, 3.5vw, 3.2rem) var(--font-display);
  background: linear-gradient(135deg, #ffffff 0%, #2dd4bf 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  line-height: 1.1;
  margin-bottom: 0.35rem;
}

.stat-card__label {
  color: #94a3b8;
  font-size: 0.88rem;
  font-weight: 500;
}

/* ── ADDRESS STRIP ── */
.home-address-strip {
  background: var(--color-primary, #0d9488);
  padding: 0.95rem clamp(1.5rem, 5vw, 4rem);
  box-shadow: 0 4px 15px rgba(13, 148, 136, 0.2);
}

.home-address-strip__inner {
  max-width: 72rem;
  margin: 0 auto;
  display: flex;
  flex-wrap: wrap;
  gap: 1.8rem;
  align-items: center;
  justify-content: space-between;
  color: #ffffff;
  font-size: 0.88rem;
  font-weight: 500;
}

.address-item {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

/* ── SCROLL REVEAL UTILITIES ── */
.scroll-reveal {
  opacity: 0;
  transform: translateY(32px);
  transition: opacity 0.8s cubic-bezier(0.16, 1, 0.3, 1), transform 0.8s cubic-bezier(0.16, 1, 0.3, 1);
  will-change: opacity, transform;
}

.scroll-reveal.is-revealed {
  opacity: 1;
  transform: translateY(0);
}

.delay-100 { transition-delay: 100ms; }
.delay-120 { transition-delay: 120ms; }
.delay-150 { transition-delay: 150ms; }
.delay-200 { transition-delay: 200ms; }
.delay-300 { transition-delay: 300ms; }

/* ── SECTIONS ── */
.home-section {
  padding: 6rem clamp(1.5rem, 5vw, 4rem);
  max-width: 74rem;
  margin: 0 auto;
  scroll-margin-top: 4.5rem;
}

.home-section--alt {
  max-width: 100%;
  background: var(--color-surface-hover, #f1f5f9);
  border-top: 1px solid var(--color-border, #e2e8f0);
  border-bottom: 1px solid var(--color-border, #e2e8f0);
}

[data-theme="dark"] .home-section--alt {
  background: #0b1320;
  border-color: #1e293b;
}

.home-section__header {
  text-align: center;
  margin-bottom: 3.5rem;
}

.home-section__eyebrow {
  display: inline-block;
  color: var(--color-primary, #0d9488);
  font: 700 0.72rem "DM Mono", monospace;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  margin-bottom: 0.75rem;
  padding: 0.25rem 0.75rem;
  background: var(--color-primary-soft, #f0fdfa);
  border-radius: 999px;
}

.home-section__title {
  font: 800 clamp(1.9rem, 3.2vw, 3rem) var(--font-display);
  letter-spacing: -0.04em;
  margin: 0.5rem 0 0.75rem;
  color: var(--color-ink, #0f172a);
}

.home-section__sub {
  color: var(--color-muted, #64748b);
  font-size: 1.05rem;
  line-height: 1.65;
  max-width: 44rem;
  margin: 0 auto;
}

/* ── ROOMS ── */
.home-data-state {
  max-width: 52rem;
  margin: 0 auto;
  padding: 2rem;
  border: 1px dashed var(--color-border, #e2e8f0);
  border-radius: 1rem;
  background: var(--color-surface, #ffffff);
  color: var(--color-muted, #64748b);
  text-align: center;
}

.home-data-state--error {
  border-color: rgba(239, 68, 68, 0.35);
  color: #dc2626;
}

.home-rooms {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(22rem, 1fr));
  gap: 2.5rem;
  max-width: 72rem;
  margin: 0 auto;
}

.home-room-card {
  background: var(--color-surface, #ffffff);
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: 1.5rem;
  overflow: hidden;
  box-shadow: 0 10px 30px -5px rgba(0, 0, 0, 0.06);
  transition: transform 0.35s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.35s cubic-bezier(0.16, 1, 0.3, 1), border-color 0.35s ease;
}

.home-room-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 25px 50px -12px rgba(13, 148, 136, 0.15), 0 0 20px rgba(45, 212, 191, 0.1);
  border-color: rgba(45, 212, 191, 0.4);
}

.home-room-card__img-wrap {
  position: relative;
  height: 16rem;
  overflow: hidden;
}

.home-room-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.home-room-card:hover .home-room-card__img {
  transform: scale(1.06);
}

.home-room-card__badge {
  position: absolute;
  top: 1.25rem;
  left: 1.25rem;
  color: #ffffff;
  font: 700 0.72rem "DM Mono", monospace;
  letter-spacing: 0.05em;
  padding: 0.4rem 0.85rem;
  border-radius: 999px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.home-room-card__avail {
  position: absolute;
  top: 1.25rem;
  right: 1.25rem;
  background: rgba(7, 13, 23, 0.78);
  backdrop-filter: blur(8px);
  color: #2dd4bf;
  font: 700 0.75rem "DM Mono", monospace;
  padding: 0.4rem 0.85rem;
  border-radius: 999px;
  border: 1px solid rgba(45, 212, 191, 0.35);
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.avail-dot {
  width: 6px;
  height: 6px;
  border-radius: 9999px;
  background: #2dd4bf;
  box-shadow: 0 0 8px #2dd4bf;
  animation: pulseDot 2s infinite;
}

@keyframes pulseDot {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.4); opacity: 0.6; }
}

.home-room-card__body {
  padding: 1.75rem;
}

.home-room-card__top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 0.5rem;
  margin-bottom: 0.6rem;
}

.home-room-card__name {
  font: 700 1.25rem var(--font-display);
  color: var(--color-ink, #0f172a);
  margin: 0;
}

.home-room-card__capacity {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--color-primary, #0d9488);
  background: var(--color-primary-soft, #f0fdfa);
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  white-space: nowrap;
}

.home-room-card__price {
  font: 800 1.75rem var(--font-display);
  color: var(--color-primary, #0d9488);
  letter-spacing: -0.03em;
  margin-bottom: 1.25rem;
}

.home-room-card__price span {
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--color-muted, #64748b);
}

.home-room-card__features {
  list-style: none;
  padding: 0;
  margin: 0 0 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.home-room-card__features li {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  font-size: 0.9rem;
  color: var(--color-ink-subtle, #334155);
}

.home-room-card__features svg {
  color: var(--color-primary, #0d9488);
  flex-shrink: 0;
}

.home-room-card__action {
  margin-top: 1.25rem;
}

/* ── AMENITIES ── */
.home-amenities-showcase {
  align-items: stretch;
  display: grid;
  gap: clamp(2rem, 5vw, 4.5rem);
  grid-template-columns: minmax(0, 1.05fr) minmax(20rem, 0.95fr);
  max-width: 74rem;
  margin: 0 auto;
}

.home-amenities-visual {
  border-radius: 1.5rem;
  margin: 0;
  overflow: hidden;
  position: relative;
  box-shadow: 0 15px 35px -5px rgba(0, 0, 0, 0.1);
}

.home-amenities-img {
  display: block;
  height: 100%;
  min-height: 32rem;
  object-fit: cover;
  width: 100%;
  transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.home-amenities-visual:hover .home-amenities-img {
  transform: scale(1.04);
}

.home-amenities-list {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.home-amenity-row {
  align-items: start;
  border-bottom: 1px solid var(--color-border, #e2e8f0);
  display: grid;
  gap: 1.25rem;
  grid-template-columns: 2.5rem 1fr auto;
  padding: 1.4rem 0;
  transition: transform 0.2s ease, background-color 0.2s ease;
}

.home-amenity-row:hover {
  transform: translateX(6px);
}

.home-amenity-row__index {
  color: var(--color-primary, #0d9488);
  font: 800 0.85rem "DM Mono", monospace;
  padding-top: 0.2rem;
}

.home-amenity-row__content h3 {
  font: 700 1.1rem var(--font-display);
  margin: 0 0 0.35rem;
  color: var(--color-ink, #0f172a);
}

.home-amenity-row__content p {
  color: var(--color-muted, #64748b);
  font-size: 0.88rem;
  line-height: 1.6;
  margin: 0;
}

.home-amenity-row__meta {
  color: var(--color-primary, #0d9488);
  font: 600 0.72rem "DM Mono", monospace;
  background: var(--color-primary-soft, #f0fdfa);
  padding: 0.25rem 0.65rem;
  border-radius: 999px;
  white-space: nowrap;
}

/* ── PHASE / ROOM STATUS ── */
.home-phase-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(20rem, 1fr));
  gap: 2rem;
  max-width: 72rem;
  margin: 0 auto;
}

.home-phase-card {
  border-radius: 1.25rem;
  border: 1.5px solid var(--color-border, #e2e8f0);
  padding: 1.75rem;
  background: var(--color-surface, #ffffff);
  position: relative;
  transition: transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
}

.home-phase-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 40px -10px rgba(0, 0, 0, 0.1);
}

.home-phase-card--soon {
  border-color: rgba(244, 63, 94, 0.4);
  background: linear-gradient(180deg, rgba(244, 63, 94, 0.05) 0%, transparent 100%);
}

.home-phase-card--warning {
  border-color: rgba(245, 158, 11, 0.4);
  background: linear-gradient(180deg, rgba(245, 158, 11, 0.05) 0%, transparent 100%);
}

.home-phase-card--ok {
  border-color: rgba(13, 148, 136, 0.35);
  background: linear-gradient(180deg, rgba(13, 148, 136, 0.05) 0%, transparent 100%);
}

.home-phase-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.25rem;
}

.home-phase-card__room {
  font: 800 1.25rem "DM Mono", monospace;
  color: var(--color-primary, #0d9488);
  display: block;
}

.home-phase-card__building {
  font-size: 0.8rem;
  color: var(--color-muted, #64748b);
}

.home-phase-card__pulse-badge {
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
  padding: 0.3rem 0.75rem;
  border-radius: 999px;
  background: var(--color-surface-hover, #f1f5f9);
  color: var(--color-ink, #0f172a);
}

.home-phase-card--soon .home-phase-card__pulse-badge {
  background: rgba(244, 63, 94, 0.15);
  color: #e11d48;
}

.home-phase-card--warning .home-phase-card__pulse-badge {
  background: rgba(245, 158, 11, 0.15);
  color: #d97706;
}

.home-phase-card--ok .home-phase-card__pulse-badge {
  background: rgba(13, 148, 136, 0.15);
  color: #0d9488;
}

.home-phase-card__body {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.home-phase-card__detail {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 0.9rem;
  color: var(--color-ink-subtle, #334155);
}

.home-phase-card__detail span {
  color: var(--color-muted, #64748b);
}

/* ── CTA SECTION ── */
.home-cta {
  background: linear-gradient(135deg, #070e1b 0%, #0d2138 60%, #081627 100%);
  padding: 6.5rem clamp(1.5rem, 5vw, 4rem);
  position: relative;
  overflow: hidden;
}

.home-cta::before {
  content: "";
  position: absolute;
  top: -50%;
  left: 50%;
  width: 600px;
  height: 600px;
  transform: translateX(-50%);
  background: radial-gradient(circle, rgba(45, 212, 191, 0.15) 0%, transparent 70%);
  filter: blur(60px);
  pointer-events: none;
}

.home-cta__inner {
  max-width: 50rem;
  margin: 0 auto;
  text-align: center;
  position: relative;
  z-index: 1;
}

.home-cta__title {
  font: 800 clamp(2.2rem, 4.5vw, 3.6rem) var(--font-display);
  color: #ffffff;
  letter-spacing: -0.04em;
  margin: 0 0 1.25rem;
}

.home-cta__sub {
  color: #94a3b8;
  font-size: 1.1rem;
  line-height: 1.7;
  margin: 0 0 2.5rem;
}

.home-cta__btns {
  display: flex;
  gap: 1.25rem;
  justify-content: center;
  flex-wrap: wrap;
}

.home-cta__secondary {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.25);
  color: #ffffff;
}

.home-cta__secondary:hover {
  background: rgba(255, 255, 255, 0.16);
  border-color: rgba(255, 255, 255, 0.4);
}

/* ── FOOTER ── */
.home-footer {
  background: #050a12;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  padding: 3rem clamp(1.5rem, 5vw, 4rem);
}

.home-footer__inner {
  max-width: 72rem;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  text-align: center;
}

.home-footer__addr {
  font-size: 0.88rem;
  color: #64748b;
  margin: 0;
  line-height: 1.7;
}

.home-footer__copy {
  font-size: 0.8rem;
  color: #475569;
  margin: 0;
}

/* ── CONTACT GRID & CARDS ── */
.home-contact-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(20rem, 1fr));
  gap: 2rem;
  max-width: 72rem;
  margin: 0 auto;
}

.home-contact-card {
  background: var(--color-surface, #ffffff);
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: 1.25rem;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.3s cubic-bezier(0.16, 1, 0.3, 1), border-color 0.3s ease;
}

.home-contact-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 16px 36px -8px rgba(13, 148, 136, 0.12), 0 0 20px rgba(45, 212, 191, 0.08);
  border-color: rgba(45, 212, 191, 0.5);
}

.home-contact-card__icon-box {
  width: 3.25rem;
  height: 3.25rem;
  border-radius: 1rem;
  background: var(--color-primary-soft, #f0fdfa);
  color: var(--color-primary, #0d9488);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.25rem;
}

[data-theme="dark"] .home-contact-card__icon-box {
  background: rgba(45, 212, 191, 0.15);
  color: #2dd4bf;
}

.home-contact-card__heading {
  font: 700 1.25rem var(--font-display);
  color: var(--color-ink, #0f172a);
  margin: 0 0 0.5rem;
}

.home-contact-card__text {
  color: var(--color-muted, #64748b);
  font-size: 0.92rem;
  line-height: 1.6;
  margin: 0 0 1.25rem;
}

.home-contact-card__details {
  margin-top: auto;
  padding-top: 1.25rem;
  border-top: 1px solid var(--color-border, #e2e8f0);
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.contact-detail-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 0.75rem;
  font-size: 0.88rem;
}

.detail-label {
  color: var(--color-muted, #64748b);
  font-weight: 500;
}

.detail-val {
  color: var(--color-ink, #0f172a);
  font-weight: 600;
  text-align: right;
}

.detail-val.text-muted {
  color: var(--color-muted, #64748b);
  font-weight: 500;
}

.detail-link {
  color: var(--color-primary, #0d9488);
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s ease;
}

.detail-link:hover {
  text-decoration: underline;
  color: #0f766e;
}

[data-theme="dark"] .home-contact-card {
  background: #0f172a;
  border-color: #1e293b;
}

[data-theme="dark"] .home-contact-card__heading {
  color: #f1f5f9;
}

[data-theme="dark"] .home-contact-card__text {
  color: #94a3b8;
}

[data-theme="dark"] .home-contact-card__details {
  border-color: #1e293b;
}

[data-theme="dark"] .detail-val {
  color: #f1f5f9;
}

[data-theme="dark"] .detail-link {
  color: #2dd4bf;
}

/* ── RESPONSIVE ── */
@media (max-width: 768px) {
  .home-hero__ctas {
    flex-direction: column;
  }
  .home-hero__chips {
    flex-direction: column;
    align-items: flex-start;
  }
  .home-address-strip__inner {
    flex-direction: column;
    gap: 0.85rem;
    align-items: flex-start;
  }
  .home-cta__btns {
    flex-direction: column;
    align-items: stretch;
  }
  .home-amenities-showcase {
    grid-template-columns: 1fr;
  }
  .home-amenities-img {
    min-height: 20rem;
  }
  .home-amenity-row {
    grid-template-columns: 2rem 1fr;
  }
  .home-amenity-row__meta {
    grid-column: 2;
  }
}
</style>
