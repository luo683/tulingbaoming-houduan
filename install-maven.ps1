# 快速安装Maven脚本
# 请右键保存为 install-maven.ps1，然后以管理员身份运行

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "      Maven 自动安装脚本" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 下载Maven
Write-Host "[1/4] 正在下载Maven 3.9.9..." -ForegroundColor Green
$url = "https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip"
$zipFile = "$env:TEMP\apache-maven.zip"

try {
    Invoke-WebRequest -Uri $url -OutFile $zipFile -UseBasicParsing
    Write-Host "      下载完成！" -ForegroundColor Green
} catch {
    Write-Host "      下载失败，请检查网络连接" -ForegroundColor Red
    exit 1
}

# 2. 解压Maven
Write-Host "[2/4] 正在解压到 C:\..." -ForegroundColor Green
try {
    # 清理旧版本
    if (Test-Path "C:\apache-maven") {
        Remove-Item "C:\apache-maven" -Recurse -Force
    }
    if (Test-Path "C:\apache-maven-3.9.9") {
        Remove-Item "C:\apache-maven-3.9.9" -Recurse -Force
    }

    # 解压
    Expand-Archive -Path $zipFile -DestinationPath "C:\" -Force

    # 重命名
    Rename-Item "C:\apache-maven-3.9.9" "C:\apache-maven"

    Write-Host "      解压完成！" -ForegroundColor Green
} catch {
    Write-Host "      解压失败: $_" -ForegroundColor Red
    exit 1
}

# 3. 配置环境变量
Write-Host "[3/4] 正在配置环境变量..." -ForegroundColor Green
try {
    # 设置MAVEN_HOME
    [Environment]::SetEnvironmentVariable("MAVEN_HOME", "C:\apache-maven", "Machine")

    # 添加到Path
    $currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
    if ($currentPath -notlike "*apache-maven*") {
        $newPath = "$currentPath;C:\apache-maven\bin"
        [Environment]::SetEnvironmentVariable("Path", $newPath, "Machine")
    }

    Write-Host "      环境变量配置完成！" -ForegroundColor Green
} catch {
    Write-Host "      环境变量配置失败: $_" -ForegroundColor Red
    exit 1
}

# 4. 验证安装
Write-Host "[4/4] 验证安装..." -ForegroundColor Green
if (Test-Path "C:\apache-maven\bin\mvn.cmd") {
    Write-Host "      Maven文件安装成功！" -ForegroundColor Green
} else {
    Write-Host "      Maven文件未找到！" -ForegroundColor Red
    exit 1
}

# 清理临时文件
Remove-Item $zipFile -Force

Write-Host ""
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "       Maven 安装成功！" -ForegroundColor Green
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Yellow
Write-Host "1. 关闭此窗口" -ForegroundColor White
Write-Host "2. 重新打开一个新的PowerShell窗口" -ForegroundColor White
Write-Host "3. 运行: mvn -v" -ForegroundColor White
Write-Host "4. 启动后端: cd 'E:\图灵 报名功能\后端\tuling' && mvn spring-boot:run" -ForegroundColor White
Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
