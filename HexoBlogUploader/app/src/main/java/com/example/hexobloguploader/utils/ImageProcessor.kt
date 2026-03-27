package com.example.hexobloguploader.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 图片处理工具类
 * 用于处理图片的保存、路径生成等操作
 */
object ImageProcessor {
    
    private const val TAG = "ImageProcessor"
    
    /**
     * 保存图片到博客目录
     * @param context 上下文
     * @param imageUri 图片URI
     * @param imageName 图片名称（可选）
     * @return 保存后的图片相对路径，失败返回null
     */
    fun saveImageToBlog(
        context: Context,
        imageUri: Uri,
        imageName: String? = null
    ): String? {
        return try {
            // 获取博客存储管理器
            val storageManager = com.example.hexobloguploader.storage.BlogStorageManager(context)
            
            // 确保图片目录存在
            if (!storageManager.imagesDir.exists()) {
                storageManager.imagesDir.mkdirs()
            }
            
            // 生成图片文件名
            val fileName = generateImageFileName(imageName)
            
            // 目标文件
            val targetFile = File(storageManager.imagesDir, fileName)
            
            // 复制图片
            val success = copyImageToFile(context, imageUri, targetFile)
            
            if (success) {
                // 返回相对路径（相对于博客根目录）
                "/images/$fileName"
            } else {
                null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "保存图片失败: ${e.message}", e)
            null
        }
    }
    
    /**
     * 生成图片文件名
     * 格式：YYYYMMDD_HHMMSS_随机数.扩展名
     */
    private fun generateImageFileName(originalName: String? = null): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val random = (1000..9999).random()
        
        // 如果有原始文件名，使用原始扩展名
        val extension = if (originalName != null && originalName.contains(".")) {
            originalName.substringAfterLast(".", "jpg")
        } else {
            "jpg"
        }
        
        return "${timestamp}_${random}.${extension.lowercase()}"
    }
    
    /**
     * 复制图片到文件
     */
    private fun copyImageToFile(context: Context, sourceUri: Uri, targetFile: File): Boolean {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            if (inputStream == null) {
                Log.e(TAG, "无法打开输入流")
                return false
            }
            
            val outputStream = FileOutputStream(targetFile)
            
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            
            outputStream.close()
            inputStream.close()
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "复制图片失败: ${e.message}", e)
            false
        }
    }
    
    /**
     * 生成Markdown图片语法
     * @param imagePath 图片路径（相对路径）
     * @param altText 图片描述
     * @return Markdown图片语法
     */
    fun generateMarkdownImageSyntax(imagePath: String, altText: String = ""): String {
        return if (altText.isNotEmpty()) {
            "![$altText]($imagePath)"
        } else {
            "![]($imagePath)"
        }
    }
    
    /**
     * 检查图片路径是否有效
     */
    fun isValidImagePath(path: String): Boolean {
        return path.startsWith("/images/") && 
               (path.endsWith(".jpg") || 
                path.endsWith(".jpeg") || 
                path.endsWith(".png") || 
                path.endsWith(".gif") || 
                path.endsWith(".webp") ||
                path.endsWith(".bmp"))
    }
    
    /**
     * 从URI获取文件名
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        return try {
            var fileName: String? = null
            
            // 尝试从URI中获取文件名
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(
                    uri, 
                    arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), 
                    null, 
                    null, 
                    null
                )
                
                cursor?.use {
                    if (it.moveToFirst()) {
                        val index = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                        if (index != -1) {
                            fileName = it.getString(index)
                        }
                    }
                }
            }
            
            // 如果无法从content URI获取，尝试从路径获取
            if (fileName.isNullOrEmpty()) {
                val path = uri.path
                if (path != null) {
                    fileName = path.substringAfterLast("/")
                }
            }
            
            fileName
        } catch (e: Exception) {
            Log.e(TAG, "获取文件名失败: ${e.message}", e)
            null
        }
    }
    
    /**
     * 获取图片文件大小
     */
    fun getImageFileSize(context: Context, imagePath: String): Long {
        return try {
            val storageManager = com.example.hexobloguploader.storage.BlogStorageManager(context)
            val imageFile = File(storageManager.imagesDir, imagePath.removePrefix("/images/"))
            
            if (imageFile.exists()) {
                imageFile.length()
            } else {
                0L
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取图片大小失败: ${e.message}", e)
            0L
        }
    }
    
    /**
     * 格式化文件大小
     */
    fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }
    
    /**
     * 清理临时图片文件
     */
    fun cleanupTempImages(context: Context, olderThanDays: Int = 7): Boolean {
        return try {
            val storageManager = com.example.hexobloguploader.storage.BlogStorageManager(context)
            val imagesDir = storageManager.imagesDir
            
            if (!imagesDir.exists() || !imagesDir.isDirectory) {
                return true
            }
            
            val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
            var deletedCount = 0
            
            imagesDir.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }
            
            Log.d(TAG, "清理了 $deletedCount 个临时图片文件")
            true
        } catch (e: Exception) {
            Log.e(TAG, "清理临时图片失败: ${e.message}", e)
            false
        }
    }
    
    /**
     * 检查存储权限
     * 注意：实际权限检查应该在Activity/Fragment中进行
     */
    fun checkStoragePermissions(context: Context): Boolean {
        // 这里只是占位符，实际权限检查需要运行时权限请求
        // 在Android 10+上，需要使用MediaStore API而不是直接文件访问
        return true
    }
    
    /**
     * 获取支持的图片格式
     */
    fun getSupportedImageFormats(): List<String> {
        return listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
    }
    
    /**
     * 验证图片格式
     */
    fun validateImageFormat(fileName: String): Boolean {
        val extension = fileName.substringAfterLast(".", "").lowercase()
        return getSupportedImageFormats().contains(extension)
    }
}