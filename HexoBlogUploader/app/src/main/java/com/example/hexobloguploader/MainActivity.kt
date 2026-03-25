package com.example.hexobloguploader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hexobloguploader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 这里将添加应用的主要逻辑
        setupUI()
    }
    
    private fun setupUI() {
        // 初始化界面组件
        binding.textView.text = "Hexo 博客上传器"
        binding.button.setOnClickListener {
            // 这里将添加博客上传逻辑
            binding.textView.text = "开始上传博客..."
        }
    }
}