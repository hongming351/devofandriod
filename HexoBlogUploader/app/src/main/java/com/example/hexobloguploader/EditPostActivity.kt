package com.example.hexobloguploader

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hexobloguploader.databinding.ActivityEditPostBinding
import com.example.hexobloguploader.model.Post
import com.example.hexobloguploader.storage.BlogStorageManager
import com.example.hexobloguploader.utils.Debouncer
import io.noties.markwon.Markwon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Markdown 编辑器 Activity
 * 用于创建和编辑 Hexo 博客文章
 */
class EditPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditPostBinding
    private lateinit var storageManager: BlogStorageManager
    private lateinit var markwon: Markwon
    private lateinit var previewDebouncer: Debouncer
    
    private var currentPost: Post? = null
    private var isNewPost: Boolean = false
    
    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        storageManager = BlogStorageManager(this)
        markwon = Markwon.create(this)
        previewDebouncer = Debouncer.create(300) // 减少防抖延迟到300ms
        
        setupToolbar()
        loadPostData()
        setupEditTextListener()
        setupButtons()
        updatePreview()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun loadPostData() {
        // 获取传递的 Post 数据
        val post = intent.getSerializableExtra("POST_DATA") as? Post
        
        if (post != null) {
            // 编辑现有文章
            currentPost = post
            isNewPost = false
            
            supportActionBar?.title = "编辑文章"
            binding.editTextTitle.setText(post.title)
            // 显示正文内容（不包含 Front-matter）
            binding.editTextContent.setText(post.bodyContent)
            
            // 显示标签和分类
            binding.editTextTags.setText(post.tags.joinToString(", "))
            binding.editTextCategories.setText(post.categories.joinToString(", "))
            
        } else {
            // 创建新文章
            isNewPost = true
            supportActionBar?.title = "新建文章"
            
            // 设置默认标题
            binding.editTextTitle.setText("新博客文章")
            
            // 生成默认内容（只生成正文，Front-matter 会在保存时自动生成）
            val defaultContent = generateDefaultContent()
            binding.editTextContent.setText(defaultContent)
        }
    }
    
    private fun generateDefaultContent(): String {
        return """# 新博客文章

开始写作吧！

## 功能说明

1. 支持 Markdown 语法
2. 实时预览
3. 自动保存
4. Hexo Front-matter 支持

## 写作提示

- 使用 # 表示标题
- 使用 * 或 - 表示列表
- 使用 **粗体** 和 *斜体*
- 使用 ``` 表示代码块

**祝你写作愉快！**
"""
    }
    
    private fun setupEditTextListener() {
        // 内容变化时使用防抖器更新预览
        binding.editTextContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 使用防抖器延迟更新预览，避免频繁渲染
                previewDebouncer.debounce {
                    updatePreview()
                }
            }
        })
        
        // 标题变化时更新工具栏
        binding.editTextTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val title = s?.toString()?.trim()
                if (!title.isNullOrEmpty()) {
                    supportActionBar?.title = title
                }
            }
        })
    }
    
    private fun setupButtons() {
        binding.buttonSave.setOnClickListener {
            savePost()
        }
        
        binding.buttonPreview.setOnClickListener {
            togglePreviewMode()
        }
        
        binding.buttonDelete.setOnClickListener {
            deletePost()
        }
        
        // Markdown 工具栏按钮
        binding.buttonInsertImage.setOnClickListener {
            insertImage()
        }
        
        binding.buttonInsertLink.setOnClickListener {
            insertLink()
        }
        
        binding.buttonInsertCode.setOnClickListener {
            insertCodeBlock()
        }
        
        binding.buttonInsertBold.setOnClickListener {
            insertBold()
        }
        
        binding.buttonInsertItalic.setOnClickListener {
            insertItalic()
        }
        
        // 新建文章时不显示删除按钮
        binding.buttonDelete.visibility = if (isNewPost) android.view.View.GONE else android.view.View.VISIBLE
    }
    
    private fun updatePreview() {
        val content = binding.editTextContent.text.toString()
        
        // 使用 Markwon 渲染 Markdown
        markwon.setMarkdown(binding.textPreview, content)
        
        // 更新字符统计
        val charCount = content.length
        val wordCount = content.split("\\s+".toRegex()).count { it.isNotEmpty() }
        binding.textStats.text = "字符: $charCount | 单词: $wordCount"
    }
    
    private fun togglePreviewMode() {
        if (binding.scrollViewPreview.visibility == android.view.View.VISIBLE) {
            // 切换到编辑模式
            binding.scrollViewPreview.visibility = android.view.View.GONE
            binding.scrollViewEdit.visibility = android.view.View.VISIBLE
            binding.buttonPreview.text = "预览"
        } else {
            // 切换到预览模式
            binding.scrollViewEdit.visibility = android.view.View.GONE
            binding.scrollViewPreview.visibility = android.view.View.VISIBLE
            binding.buttonPreview.text = "编辑"
            updatePreview()
        }
    }
    
    private fun savePost() {
        val title = binding.editTextTitle.text.toString().trim()
        val bodyContent = binding.editTextContent.text.toString().trim()
        val tagsText = binding.editTextTags.text.toString().trim()
        val categoriesText = binding.editTextCategories.text.toString().trim()
        
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入文章标题", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (bodyContent.isEmpty()) {
            Toast.makeText(this, "请输入文章内容", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 解析标签和分类
        val tags = tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val categories = categoriesText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        
        val post = if (isNewPost) {
            // 创建新文章
            Post.createNew(
                title = title,
                bodyContent = bodyContent,
                categories = categories,
                tags = tags
            )
        } else {
            // 更新现有文章
            currentPost?.let { existingPost ->
                Post.updatePost(
                    post = existingPost,
                    title = title,
                    bodyContent = bodyContent,
                    categories = categories,
                    tags = tags
                )
            }
        }
        
        if (post != null) {
            val success = storageManager.savePost(post)
            
            if (success) {
                Toast.makeText(this, "文章保存成功", Toast.LENGTH_SHORT).show()
                
                // 检查是否自动提交到 Git
                if (shouldAutoCommit()) {
                    autoCommitToGit(post.title)
                }
                
                // 返回结果
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "文章保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 自动提交到 Git
     */
    private fun autoCommitToGit(postTitle: String) {
        // 获取 Git 设置管理器
        val gitSettingsManager = com.example.hexobloguploader.git.GitSettingsManager(this)
        
        // 检查 Git 设置是否完整
        if (!gitSettingsManager.isGitSetupComplete()) {
            Log.w("EditPostActivity", "Git 设置不完整，跳过自动提交")
            return
        }
        
        // 检查是否启用自动提交
        if (!gitSettingsManager.isAutoCommitEnabled()) {
            Log.d("EditPostActivity", "自动提交已禁用，跳过自动提交")
            return
        }
        
        val gitToken = gitSettingsManager.getGitHubToken()
        
        if (gitToken.isNotEmpty()) {
            // 在后台执行 Git 提交
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    val gitManager = com.example.hexobloguploader.git.GitOperationsManager(this@EditPostActivity)
                    
                    // 检查仓库是否已初始化
                    if (!gitManager.isRepositoryInitialized()) {
                        Log.w("EditPostActivity", "Git 仓库未初始化，跳过自动提交")
                        return@launch
                    }
                    
                    // 检查是否是有效的 Hexo 仓库
                    if (!gitManager.isValidHexoRepository()) {
                        Log.w("EditPostActivity", "不是有效的 Hexo 仓库，跳过自动提交")
                        return@launch
                    }
                    
                    // 生成提交信息
                    val commitMessage = if (isNewPost) {
                        "新增文章: $postTitle"
                    } else {
                        "更新文章: $postTitle"
                    }
                    
                    // 检查是否启用自动拉取
                    val autoPull = gitSettingsManager.isAutoPullEnabled()
                    
                    // 执行提交和推送
                    val result = gitManager.commitAndPush(commitMessage, gitToken, autoPull)
                    
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        if (result.success) {
                            Toast.makeText(this@EditPostActivity, "文章已自动提交到 Git", Toast.LENGTH_SHORT).show()
                            Log.d("EditPostActivity", "Git 提交成功: ${result.message}")
                        } else {
                            // 提交失败，但不影响文章保存
                            Log.w("EditPostActivity", "Git 提交失败: ${result.message}")
                            
                            // 检查是否是冲突导致的失败
                            if (result.message.contains("冲突") || result.message.contains("conflict")) {
                                // 显示冲突提示
                                Toast.makeText(
                                    this@EditPostActivity,
                                    "文章保存成功，但 Git 提交遇到冲突，请手动解决",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@EditPostActivity,
                                    "文章保存成功，但 Git 提交失败: ${result.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        Log.e("EditPostActivity", "Git 提交异常: ${e.message}", e)
                        Toast.makeText(
                            this@EditPostActivity,
                            "文章保存成功，但 Git 提交异常: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } else {
            Log.w("EditPostActivity", "GitHub Token 为空，跳过自动提交")
        }
    }
    
    /**
     * 检查是否应该自动提交
     */
    private fun shouldAutoCommit(): Boolean {
        // 这里应该从设置中读取用户偏好
        // 暂时返回 true 进行测试
        return true
    }
    
    /**
     * 获取 Git Token
     */
    private fun getGitToken(): String {
        // 这里应该从安全存储中获取 Token
        // 暂时返回空字符串
        return ""
    }
    
    private fun deletePost() {
        if (currentPost != null && !isNewPost) {
            // 显示确认对话框
            android.app.AlertDialog.Builder(this)
                .setTitle("删除文章")
                .setMessage("确定要删除这篇文章吗？此操作不可撤销。")
                .setPositiveButton("删除") { _, _ ->
                    val success = storageManager.deletePost(currentPost!!)
                    
                    if (success) {
                        Toast.makeText(this, "文章删除成功", Toast.LENGTH_SHORT).show()
                        
                        // 返回结果
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this, "文章删除失败", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }
    
    /**
     * 插入图片
     */
    private fun insertImage() {
        // 显示图片插入对话框
        android.app.AlertDialog.Builder(this)
            .setTitle("插入图片")
            .setMessage("选择图片插入方式:")
            .setPositiveButton("从相册选择") { _, _ ->
                selectImageFromGallery()
            }
            .setNegativeButton("输入图片URL") { _, _ ->
                showImageUrlDialog()
            }
            .setNeutralButton("上传到图床") { _, _ ->
                showImageUploadDialog()
            }
            .setNeutralButton("取消", null)
            .show()
    }
    
    /**
     * 从相册选择图片
     */
    private fun selectImageFromGallery() {
        // 创建图片选择Intent
        val intent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(android.content.Intent.CATEGORY_OPENABLE)
            putExtra(android.content.Intent.EXTRA_ALLOW_MULTIPLE, false)
        }
        
        // 启动图片选择
        try {
            startActivityForResult(
                android.content.Intent.createChooser(intent, "选择图片"),
                REQUEST_IMAGE_PICK
            )
        } catch (e: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "未找到图片选择应用", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 显示图片URL输入对话框
     */
    private fun showImageUrlDialog() {
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(32, 16, 32, 16)
        
        val editTextUrl = android.widget.EditText(this)
        editTextUrl.hint = "输入图片URL"
        editTextUrl.setText("https://example.com/image.jpg")
        
        val editTextAlt = android.widget.EditText(this)
        editTextAlt.hint = "图片描述（可选）"
        editTextAlt.setText("图片描述")
        
        layout.addView(editTextUrl)
        layout.addView(editTextAlt)
        
        android.app.AlertDialog.Builder(this)
            .setTitle("插入图片")
            .setView(layout)
            .setPositiveButton("插入") { _, _ ->
                val url = editTextUrl.text.toString().trim()
                val alt = editTextAlt.text.toString().trim()
                if (url.isNotEmpty()) {
                    val imageMarkdown = if (alt.isNotEmpty()) {
                        "![$alt]($url)"
                    } else {
                        "![]($url)"
                    }
                    insertTextAtCursor(imageMarkdown)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    /**
     * 显示图片上传对话框
     */
    private fun showImageUploadDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("上传图片到图床")
            .setMessage("图床功能开发中...\n\n目前支持:\n1. 直接输入图片URL\n2. 从相册选择\n\n图床功能将在后续版本中添加。")
            .setPositiveButton("确定", null)
            .show()
    }
    
    /**
     * 插入链接
     */
    private fun insertLink() {
        val editTextUrl = android.widget.EditText(this)
        editTextUrl.hint = "输入链接URL"
        
        val editTextText = android.widget.EditText(this)
        editTextText.hint = "输入链接文本"
        
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(32, 16, 32, 16)
        layout.addView(editTextUrl)
        layout.addView(editTextText)
        
        android.app.AlertDialog.Builder(this)
            .setTitle("插入链接")
            .setView(layout)
            .setPositiveButton("插入") { _, _ ->
                val url = editTextUrl.text.toString().trim()
                val text = editTextText.text.toString().trim()
                if (url.isNotEmpty() && text.isNotEmpty()) {
                    val linkMarkdown = "[$text]($url)"
                    insertTextAtCursor(linkMarkdown)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    /**
     * 插入代码块
     */
    private fun insertCodeBlock() {
        val editText = android.widget.EditText(this)
        editText.hint = "输入编程语言（可选）"
        
        android.app.AlertDialog.Builder(this)
            .setTitle("插入代码块")
            .setMessage("输入编程语言（如：kotlin, java, python）：")
            .setView(editText)
            .setPositiveButton("插入") { _, _ ->
                val language = editText.text.toString().trim()
                val codeBlock = if (language.isNotEmpty()) {
                    "```$language\n// 在这里输入代码\n```"
                } else {
                    "```\n// 在这里输入代码\n```"
                }
                insertTextAtCursor(codeBlock)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    /**
     * 插入粗体文本
     */
    private fun insertBold() {
        val selectedText = getSelectedText()
        if (selectedText.isNotEmpty()) {
            insertTextAtCursor("**$selectedText**")
        } else {
            insertTextAtCursor("**粗体文本**")
        }
    }
    
    /**
     * 插入斜体文本
     */
    private fun insertItalic() {
        val selectedText = getSelectedText()
        if (selectedText.isNotEmpty()) {
            insertTextAtCursor("*$selectedText*")
        } else {
            insertTextAtCursor("*斜体文本*")
        }
    }
    
    /**
     * 获取选中的文本
     */
    private fun getSelectedText(): String {
        val editText = binding.editTextContent
        val start = editText.selectionStart
        val end = editText.selectionEnd
        
        return if (start < end) {
            editText.text.toString().substring(start, end)
        } else {
            ""
        }
    }
    
    /**
     * 在光标位置插入文本
     */
    private fun insertTextAtCursor(text: String) {
        val editText = binding.editTextContent
        val start = editText.selectionStart
        val end = editText.selectionEnd
        
        val content = editText.text.toString()
        val newContent = StringBuilder(content)
            .insert(start, text)
            .toString()
        
        editText.setText(newContent)
        
        // 将光标移动到插入文本的末尾
        val newCursorPosition = start + text.length
        editText.setSelection(newCursorPosition)
        
        // 更新预览
        updatePreview()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            REQUEST_IMAGE_PICK -> {
                if (resultCode == RESULT_OK && data != null) {
                    val imageUri = data.data
                    if (imageUri != null) {
                        processSelectedImage(imageUri)
                    }
                }
            }
        }
    }
    
    /**
     * 处理选中的图片
     */
    private fun processSelectedImage(imageUri: android.net.Uri) {
        // 显示进度对话框
        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("正在处理图片...")
            setCancelable(false)
            show()
        }
        
        // 在后台线程处理图片
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                // 获取原始文件名
                val originalFileName = com.example.hexobloguploader.utils.ImageProcessor.getFileNameFromUri(
                    this@EditPostActivity, 
                    imageUri
                )
                
                // 保存图片到博客目录
                val imagePath = com.example.hexobloguploader.utils.ImageProcessor.saveImageToBlog(
                    this@EditPostActivity,
                    imageUri,
                    originalFileName
                )
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    progressDialog.dismiss()
                    
                    if (imagePath != null) {
                        // 显示图片信息对话框
                        showImageInfoDialog(imagePath, originalFileName)
                    } else {
                        Toast.makeText(
                            this@EditPostActivity,
                            "图片保存失败，请重试",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@EditPostActivity,
                        "图片处理失败: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    /**
     * 显示图片信息对话框
     */
    private fun showImageInfoDialog(imagePath: String, originalFileName: String?) {
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(32, 16, 32, 16)
        
        val editTextAlt = android.widget.EditText(this)
        editTextAlt.hint = "图片描述（可选）"
        
        val textViewInfo = android.widget.TextView(this)
        textViewInfo.text = "图片已保存到: $imagePath"
        textViewInfo.setTextColor(android.graphics.Color.GRAY)
        textViewInfo.textSize = 12f
        
        layout.addView(editTextAlt)
        layout.addView(textViewInfo)
        
        android.app.AlertDialog.Builder(this)
            .setTitle("插入图片")
            .setMessage("图片已保存成功，请输入图片描述：")
            .setView(layout)
            .setPositiveButton("插入") { _, _ ->
                val altText = editTextAlt.text.toString().trim()
                val imageMarkdown = com.example.hexobloguploader.utils.ImageProcessor.generateMarkdownImageSyntax(
                    imagePath,
                    altText
                )
                insertTextAtCursor(imageMarkdown)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    override fun onBackPressed() {
        // 检查是否有未保存的更改
        val title = binding.editTextTitle.text.toString().trim()
        val content = binding.editTextContent.text.toString().trim()
        
        val hasChanges = if (isNewPost) {
            title.isNotEmpty() || content.isNotEmpty()
        } else {
            val originalTitle = currentPost?.title ?: ""
            val originalContent = currentPost?.content ?: ""
            title != originalTitle || content != originalContent
        }
        
        if (hasChanges) {
            android.app.AlertDialog.Builder(this)
                .setTitle("未保存的更改")
                .setMessage("您有未保存的更改，是否要保存？")
                .setPositiveButton("保存") { _, _ ->
                    savePost()
                }
                .setNegativeButton("不保存") { _, _ ->
                    super.onBackPressed()
                }
                .setNeutralButton("取消", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }
}