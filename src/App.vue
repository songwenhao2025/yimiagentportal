<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('App Launch')
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style lang="scss">
@import './uni.scss';

page {
  background-color: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

view, text {
  box-sizing: border-box;
}

image {
  display: block;
}

button {
  margin: 0;
  padding: 0;
  background: none;
  border: none;
  
  &::after {
    border: none;
  }
}

input, textarea {
  box-sizing: border-box;
}

scroll-view {
  -webkit-overflow-scrolling: touch;
}
</style>
