package com.vtu.translate.data.model

/**
 * Lớp được sử dụng để xử lý các sự kiện chỉ nên được xử lý một lần.
 * Ví dụ: hiển thị thông báo, điều hướng, v.v.
 *
 * @param T Kiểu dữ liệu của nội dung sự kiện
 * @property content Nội dung của sự kiện
 */
data class Event<out T>(private val content: T) {
    
    /**
     * Đã xử lý sự kiện hay chưa
     */
    var hasBeenHandled = false
        private set
    
    /**
     * Trả về nội dung và đánh dấu sự kiện đã được xử lý.
     * Các lần gọi tiếp theo sẽ trả về null.
     *
     * @return Nội dung nếu chưa được xử lý, null nếu đã được xử lý
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
    
    /**
     * Trả về nội dung ngay cả khi đã được xử lý.
     * Chỉ sử dụng cho các trường hợp đặc biệt khi cần xem nội dung mà không đánh dấu đã xử lý.
     */
    fun peekContent(): T = content
}