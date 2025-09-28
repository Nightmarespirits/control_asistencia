import { ref } from 'vue'

export function useSoundEffects() {
  const audioContext = ref<AudioContext | null>(null)

  // Initialize audio context
  const initAudioContext = () => {
    if (!audioContext.value) {
      audioContext.value = new (window.AudioContext || (window as any).webkitAudioContext)()
    }
  }

  // Create a simple beep sound
  const createBeep = (frequency: number, duration: number, type: OscillatorType = 'sine') => {
    return new Promise<void>((resolve) => {
      if (!audioContext.value) {
        initAudioContext()
      }

      if (!audioContext.value) {
        resolve()
        return
      }

      const oscillator = audioContext.value.createOscillator()
      const gainNode = audioContext.value.createGain()

      oscillator.connect(gainNode)
      gainNode.connect(audioContext.value.destination)

      oscillator.frequency.value = frequency
      oscillator.type = type

      gainNode.gain.setValueAtTime(0.3, audioContext.value.currentTime)
      gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.value.currentTime + duration)

      oscillator.start(audioContext.value.currentTime)
      oscillator.stop(audioContext.value.currentTime + duration)

      oscillator.onended = () => resolve()
    })
  }

  const playSuccessSound = async () => {
    try {
      // Play a pleasant success sound (two ascending tones)
      await createBeep(800, 0.1)
      await createBeep(1000, 0.2)
    } catch (error) {
      console.warn('Could not play success sound:', error)
    }
  }

  const playErrorSound = async () => {
    try {
      // Play a lower error sound
      await createBeep(300, 0.3, 'square')
    } catch (error) {
      console.warn('Could not play error sound:', error)
    }
  }

  return {
    playSuccessSound,
    playErrorSound,
    initAudioContext
  }
}