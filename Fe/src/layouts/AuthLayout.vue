<script setup lang="ts">
import BaseThemeToggle from '@/components/base/BaseThemeToggle.vue'
import BrandLogo from '@/components/brand/BrandLogo.vue'
</script>

<template>
  <main class="auth-layout">
    <div class="auth-layout__visual">
      <div class="auth-layout__bg">
        <img
          src="/images/ktx_hero.jpg"
          alt="Ký túc xá Trường Đại học Công Nghệ"
          class="auth-layout__bg-img"
        />
        <div class="auth-layout__visual-overlay" />
      </div>

      <div class="auth-layout__brand">
        <BrandLogo tone="inverse" />
      </div>

      <div class="auth-layout__copy">
        <span class="eyebrow">
          NỀN TẢNG QUẢN LÝ NỘI TRÚ
        </span>
        <h1>
          Vận hành KTX trên một <span class="highlight-text">hạ tầng số</span> thống nhất.
        </h1>
        <p>
          Phòng ở, hợp đồng và hóa đơn được kết nối trong một quy trình rõ ràng cho sinh viên và ban quản lý.
        </p>

        <div class="auth-layout__capabilities" aria-label="Năng lực hệ thống">
          <div><span>01</span><strong>Quản lý phòng theo thời gian thực</strong></div>
          <div><span>02</span><strong>Hợp đồng và chuyển phòng trực tuyến</strong></div>
          <div><span>03</span><strong>Theo dõi hóa đơn minh bạch</strong></div>
        </div>
      </div>
    </div>

    <section class="auth-layout__panel">
      <div class="auth-layout__top-actions">
        <BaseThemeToggle />
      </div>

      <slot />
    </section>
  </main>
</template>
