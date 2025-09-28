import { vi } from 'vitest'

// Mock CSS imports
vi.mock('*.css', () => ({}))
vi.mock('*.scss', () => ({}))
vi.mock('*.sass', () => ({}))

// Mock Vuetify styles
vi.mock('vuetify/styles', () => ({}))
vi.mock('@mdi/font/css/materialdesignicons.css', () => ({}))
vi.mock('roboto-fontface/css/roboto/roboto-fontface.css', () => ({}))