package com.example.hexobloguploader.git

import android.content.Context
import android.util.Log
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import java.io.File

/**
 * SSH 认证管理器
 * 负责管理 SSH 密钥和认证
 */
class SshAuthManager(private val context: Context) {
    
    companion object {
        private const val TAG = "SshAuthManager"
        private const val SSH_KEY_DIR = "ssh_keys"
        private const val DEFAULT_KEY_NAME = "id_rsa"
        private const val DEFAULT_PUB_KEY_NAME = "id_rsa.pub"
    }
    
    private val sshKeyDir: File by lazy {
        File(context.filesDir, SSH_KEY_DIR).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    /**
     * 初始化 SSH 会话工厂
     */
    fun initSshSessionFactory() {
        try {
            // 使用 JGit 的默认 SSH 会话工厂
            // JGit 会自动处理 SSH 配置
            Log.d(TAG, "SSH 会话工厂初始化成功")
        } catch (e: Exception) {
            Log.e(TAG, "初始化 SSH 会话工厂失败: ${e.message}", e)
        }
    }
    
    /**
     * 保存 SSH 私钥
     * @param privateKey SSH 私钥内容
     * @param keyName 密钥文件名（可选）
     * @return 保存结果
     */
    fun savePrivateKey(privateKey: String, keyName: String = DEFAULT_KEY_NAME): Boolean {
        return try {
            val keyFile = File(sshKeyDir, keyName)
            keyFile.writeText(privateKey)
            
            // 设置文件权限（仅所有者可读写）
            keyFile.setReadable(false, false)
            keyFile.setReadable(true, true)
            keyFile.setWritable(false, false)
            keyFile.setWritable(true, true)
            
            Log.d(TAG, "SSH 私钥已保存: ${keyFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "保存 SSH 私钥失败: ${e.message}", e)
            false
        }
    }
    
    /**
     * 保存 SSH 公钥
     * @param publicKey SSH 公钥内容
     * @param keyName 公钥文件名（可选）
     * @return 保存结果
     */
    fun savePublicKey(publicKey: String, keyName: String = DEFAULT_PUB_KEY_NAME): Boolean {
        return try {
            val keyFile = File(sshKeyDir, keyName)
            keyFile.writeText(publicKey)
            Log.d(TAG, "SSH 公钥已保存: ${keyFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "保存 SSH 公钥失败: ${e.message}", e)
            false
        }
    }
    
    /**
     * 从文件导入 SSH 密钥
     * @param keyFile 密钥文件
     * @param keyName 保存的文件名（可选）
     * @return 导入结果
     */
    fun importPrivateKeyFromFile(keyFile: File, keyName: String = DEFAULT_KEY_NAME): Boolean {
        return try {
            if (!keyFile.exists() || !keyFile.isFile) {
                Log.e(TAG, "密钥文件不存在或不是文件: ${keyFile.absolutePath}")
                return false
            }
            
            val keyContent = keyFile.readText()
            return savePrivateKey(keyContent, keyName)
        } catch (e: Exception) {
            Log.e(TAG, "导入 SSH 私钥失败: ${e.message}", e)
            false
        }
    }
    
    /**
     * 获取私钥文件
     */
    fun getPrivateKeyFile(keyName: String = DEFAULT_KEY_NAME): File {
        return File(sshKeyDir, keyName)
    }
    
    /**
     * 获取公钥文件
     */
    fun getPublicKeyFile(keyName: String = DEFAULT_PUB_KEY_NAME): File {
        return File(sshKeyDir, keyName)
    }
    
    /**
     * 检查 SSH 密钥是否存在
     */
    fun hasSshKey(keyName: String = DEFAULT_KEY_NAME): Boolean {
        return getPrivateKeyFile(keyName).exists()
    }
    
    /**
     * 获取 SSH 密钥信息
     */
    fun getSshKeyInfo(keyName: String = DEFAULT_KEY_NAME): SshKeyInfo {
        val privateKeyFile = getPrivateKeyFile(keyName)
        val publicKeyFile = getPublicKeyFile("$keyName.pub")
        
        return SshKeyInfo(
            privateKeyExists = privateKeyFile.exists(),
            publicKeyExists = publicKeyFile.exists(),
            privateKeyPath = privateKeyFile.absolutePath,
            publicKeyPath = publicKeyFile.absolutePath,
            privateKeySize = if (privateKeyFile.exists()) privateKeyFile.length() else 0,
            publicKeySize = if (publicKeyFile.exists()) publicKeyFile.length() else 0
        )
    }
    
    /**
     * 删除 SSH 密钥
     */
    fun deleteSshKey(keyName: String = DEFAULT_KEY_NAME): Boolean {
        return try {
            val privateKeyFile = getPrivateKeyFile(keyName)
            val publicKeyFile = getPublicKeyFile("$keyName.pub")
            
            var success = true
            if (privateKeyFile.exists()) {
                success = success && privateKeyFile.delete()
            }
            if (publicKeyFile.exists()) {
                success = success && publicKeyFile.delete()
            }
            
            if (success) {
                Log.d(TAG, "SSH 密钥已删除: $keyName")
            } else {
                Log.w(TAG, "删除 SSH 密钥失败: $keyName")
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "删除 SSH 密钥异常: ${e.message}", e)
            false
        }
    }
    
    /**
     * 生成 SSH 密钥对
     * @param comment 密钥注释（可选）
     * @param keyName 密钥文件名（可选）
     * @return 生成结果
     */
    fun generateSshKeyPair(comment: String = "hexo-blog-uploader", keyName: String = DEFAULT_KEY_NAME): SshKeyGenResult {
        return try {
            val jsch = JSch()
            val keyPair = com.jcraft.jsch.KeyPair.genKeyPair(jsch, com.jcraft.jsch.KeyPair.RSA, 2048)
            
            // 保存私钥
            val privateKeyFile = getPrivateKeyFile(keyName)
            keyPair.writePrivateKey(privateKeyFile.absolutePath)
            
            // 保存公钥
            val publicKeyFile = getPublicKeyFile("$keyName.pub")
            keyPair.writePublicKey("$comment@hexo-blog-uploader", publicKeyFile.absolutePath)
            
            // 读取公钥内容
            val publicKeyContent = publicKeyFile.readText()
            
            keyPair.dispose()
            
            Log.d(TAG, "SSH 密钥对生成成功: $keyName")
            
            SshKeyGenResult(
                success = true,
                message = "SSH 密钥对生成成功",
                privateKeyPath = privateKeyFile.absolutePath,
                publicKeyPath = publicKeyFile.absolutePath,
                publicKeyContent = publicKeyContent
            )
        } catch (e: Exception) {
            Log.e(TAG, "生成 SSH 密钥对失败: ${e.message}", e)
            SshKeyGenResult(
                success = false,
                message = "生成 SSH 密钥对失败: ${e.message}"
            )
        }
    }
    
    /**
     * 获取所有 SSH 密钥
     */
    fun getAllSshKeys(): List<SshKeyInfo> {
        return sshKeyDir.listFiles { file -> 
            file.isFile && !file.name.endsWith(".pub")
        }?.map { file ->
            getSshKeyInfo(file.name)
        } ?: emptyList()
    }
    
    /**
     * 验证 SSH 密钥
     */
    fun validateSshKey(keyName: String = DEFAULT_KEY_NAME): SshKeyValidationResult {
        return try {
            val privateKeyFile = getPrivateKeyFile(keyName)
            if (!privateKeyFile.exists()) {
                return SshKeyValidationResult(
                    isValid = false,
                    message = "私钥文件不存在"
                )
            }
            
            val jsch = JSch()
            jsch.addIdentity(privateKeyFile.absolutePath)
            
            // 尝试创建一个会话来验证密钥
            val session = jsch.getSession("test", "github.com", 22)
            session.setConfig("StrictHostKeyChecking", "no")
            
            // 设置超时时间
            session.timeout = 5000
            
            try {
                session.connect()
                session.disconnect()
                
                SshKeyValidationResult(
                    isValid = true,
                    message = "SSH 密钥验证成功"
                )
            } catch (e: JSchException) {
                SshKeyValidationResult(
                    isValid = false,
                    message = "SSH 密钥验证失败: ${e.message}"
                )
            }
        } catch (e: Exception) {
            SshKeyValidationResult(
                isValid = false,
                message = "验证 SSH 密钥异常: ${e.message}"
            )
        }
    }
}

/**
 * SSH 密钥信息
 */
data class SshKeyInfo(
    val privateKeyExists: Boolean,
    val publicKeyExists: Boolean,
    val privateKeyPath: String,
    val publicKeyPath: String,
    val privateKeySize: Long,
    val publicKeySize: Long
) {
    /**
     * 获取格式化信息
     */
    fun getFormattedInfo(): String {
        return """
            SSH 密钥信息:
            私钥存在: ${if (privateKeyExists) "✅" else "❌"}
            公钥存在: ${if (publicKeyExists) "✅" else "❌"}
            私钥路径: $privateKeyPath
            公钥路径: $publicKeyPath
            私钥大小: ${privateKeySize} 字节
            公钥大小: ${publicKeySize} 字节
        """.trimIndent()
    }
}

/**
 * SSH 密钥生成结果
 */
data class SshKeyGenResult(
    val success: Boolean,
    val message: String,
    val privateKeyPath: String? = null,
    val publicKeyPath: String? = null,
    val publicKeyContent: String? = null
)

/**
 * SSH 密钥验证结果
 */
data class SshKeyValidationResult(
    val isValid: Boolean,
    val message: String
)