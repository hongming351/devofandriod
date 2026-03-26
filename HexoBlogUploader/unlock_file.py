#!/usr/bin/env python3
"""
脚本用于解锁被占用的文件
"""
import os
import sys
import ctypes
from ctypes import wintypes

# Windows API 常量
FILE_SHARE_READ = 0x00000001
FILE_SHARE_WRITE = 0x00000002
FILE_SHARE_DELETE = 0x00000004
OPEN_EXISTING = 3
GENERIC_READ = 0x80000000
GENERIC_WRITE = 0x40000000

# 加载 kernel32.dll
kernel32 = ctypes.WinDLL('kernel32', use_last_error=True)

# 定义函数原型
kernel32.CreateFileW.restype = wintypes.HANDLE
kernel32.CreateFileW.argtypes = (
    wintypes.LPCWSTR,      # lpFileName
    wintypes.DWORD,        # dwDesiredAccess
    wintypes.DWORD,        # dwShareMode
    wintypes.LPVOID,       # lpSecurityAttributes
    wintypes.DWORD,        # dwCreationDisposition
    wintypes.DWORD,        # dwFlagsAndAttributes
    wintypes.HANDLE        # hTemplateFile
)

kernel32.CloseHandle.argtypes = (wintypes.HANDLE,)

def unlock_file(file_path):
    """尝试解锁文件"""
    print(f"尝试解锁文件: {file_path}")
    
    # 首先尝试正常删除
    try:
        os.remove(file_path)
        print("文件删除成功!")
        return True
    except PermissionError as e:
        print(f"权限错误: {e}")
    except OSError as e:
        print(f"系统错误: {e}")
    
    # 尝试使用 Windows API 打开文件并关闭句柄
    try:
        # 以读权限打开文件
        hFile = kernel32.CreateFileW(
            file_path,
            GENERIC_READ,
            FILE_SHARE_READ | FILE_SHARE_WRITE | FILE_SHARE_DELETE,
            None,
            OPEN_EXISTING,
            0,
            None
        )
        
        if hFile != -1:  # INVALID_HANDLE_VALUE
            print("成功获取文件句柄，正在关闭...")
            kernel32.CloseHandle(hFile)
            
            # 再次尝试删除
            try:
                os.remove(file_path)
                print("文件删除成功!")
                return True
            except Exception as e:
                print(f"删除失败: {e}")
        else:
            error_code = ctypes.get_last_error()
            print(f"无法获取文件句柄，错误代码: {error_code}")
            
    except Exception as e:
        print(f"API调用失败: {e}")
    
    return False

def main():
    if len(sys.argv) < 2:
        print("用法: python unlock_file.py <文件路径>")
        sys.exit(1)
    
    file_path = sys.argv[1]
    
    if not os.path.exists(file_path):
        print(f"文件不存在: {file_path}")
        sys.exit(1)
    
    if unlock_file(file_path):
        print("操作成功!")
    else:
        print("操作失败，文件可能被系统进程锁定。")
        print("建议:")
        print("1. 重启计算机")
        print("2. 检查是否有防病毒软件正在扫描该文件")
        print("3. 关闭所有可能使用该文件的程序（如IDE、资源管理器）")

if __name__ == "__main__":
    main()