package com.vtu.translate.data.util

import kotlinx.coroutines.delay

/**
 * Lớp tiện ích để xử lý các tác vụ theo batch với khả năng kiểm soát tốc độ
 * để tránh vượt quá giới hạn tốc độ API.
 *
 * @param T Kiểu dữ liệu của các item cần xử lý
 * @param R Kiểu dữ liệu kết quả sau khi xử lý
 */
class BatchProcessor<T, R> {
    
    /**
     * Xử lý danh sách các item theo batch với khả năng kiểm soát tốc độ
     *
     * @param items Danh sách các item cần xử lý
     * @param batchSize Kích thước của mỗi batch
     * @param delayBetweenItemsMs Thời gian delay giữa các item trong cùng batch (milliseconds)
     * @param delayBetweenBatchesMs Thời gian delay giữa các batch (milliseconds)
     * @param shouldContinue Hàm kiểm tra xem có nên tiếp tục xử lý không
     * @param onBatchStart Callback khi bắt đầu xử lý một batch mới
     * @param onBatchComplete Callback khi hoàn thành xử lý một batch
     * @param onItemStart Callback khi bắt đầu xử lý một item
     * @param onItemComplete Callback khi hoàn thành xử lý một item
     * @param processor Hàm xử lý mỗi item
     * @return Danh sách kết quả sau khi xử lý
     */
    suspend fun processBatch(
        items: List<T>,
        batchSize: Int = 5,
        delayBetweenItemsMs: Long = 200,
        delayBetweenBatchesMs: Long = 1000,
        shouldContinue: () -> Boolean = { true },
        onBatchStart: (Int, Int) -> Unit = { _, _ -> },
        onBatchComplete: (Int, Int) -> Unit = { _, _ -> },
        onItemStart: (T, Int) -> Unit = { _, _ -> },
        onItemComplete: (T, R?, Int, Boolean) -> Unit = { _, _, _, _ -> },
        processor: suspend (T) -> Result<R>
    ): List<Pair<T, Result<R>>> {
        val results = mutableListOf<Pair<T, Result<R>>>()
        
        // Xử lý theo batch
        for (batchIndex in items.indices step batchSize) {
            if (!shouldContinue()) break
            
            val batchEndIndex = minOf(batchIndex + batchSize, items.size)
            val currentBatch = items.subList(batchIndex, batchEndIndex)
            
            onBatchStart(batchIndex / batchSize + 1, (items.size + batchSize - 1) / batchSize)
            
            // Xử lý từng item trong batch
            for ((itemIndex, item) in currentBatch.withIndex()) {
                if (!shouldContinue()) break
                
                val globalIndex = batchIndex + itemIndex
                onItemStart(item, globalIndex)
                
                val result = try {
                    processor(item)
                } catch (e: Exception) {
                    Result.failure(e)
                }
                
                results.add(item to result)
                onItemComplete(item, result.getOrNull(), globalIndex, result.isSuccess)
                
                // Delay giữa các item trong cùng batch
                if (itemIndex < currentBatch.size - 1 && shouldContinue()) {
                    delay(delayBetweenItemsMs)
                }
            }
            
            onBatchComplete(batchIndex / batchSize + 1, (items.size + batchSize - 1) / batchSize)
            
            // Delay giữa các batch
            if (batchEndIndex < items.size && shouldContinue()) {
                delay(delayBetweenBatchesMs)
            }
        }
        
        return results
    }
    
    /**
     * Xử lý lại các item thất bại với số lần thử lại và thời gian chờ tăng dần
     *
     * @param failedItems Danh sách các item thất bại cần xử lý lại
     * @param maxRetries Số lần thử lại tối đa cho mỗi item
     * @param initialDelayMs Thời gian delay ban đầu trước khi thử lại (milliseconds)
     * @param backoffFactor Hệ số tăng thời gian delay giữa các lần thử lại
     * @param onRetry Callback khi thử lại một item
     * @param processor Hàm xử lý mỗi item
     * @return Danh sách kết quả sau khi xử lý lại
     */
    suspend fun retryFailedItems(
        failedItems: List<Pair<T, Result<R>>>,
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        backoffFactor: Double = 2.0,
        onRetry: (T, Int, Long) -> Unit = { _, _, _ -> },
        processor: suspend (T) -> Result<R>
    ): List<Pair<T, Result<R>>> {
        val results = mutableListOf<Pair<T, Result<R>>>()
        
        for ((item, _) in failedItems) {
            var currentDelay = initialDelayMs
            var result: Result<R>? = null
            
            // Thử lại với số lần và thời gian chờ tăng dần
            for (retryCount in 1..maxRetries) {
                delay(currentDelay)
                onRetry(item, retryCount, currentDelay)
                
                result = try {
                    processor(item)
                } catch (e: Exception) {
                    Result.failure(e)
                }
                
                if (result?.isSuccess == true) break
                
                // Tăng thời gian chờ theo hệ số
                currentDelay = (currentDelay * backoffFactor).toLong()
            }
            
            results.add(item to (result ?: Result.failure(Exception("Đã vượt quá số lần thử lại"))))
        }
        
        return results
    }
}