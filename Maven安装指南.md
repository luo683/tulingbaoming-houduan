# Maven 安装指南（Windows）

## 快速安装步骤

### 1. 下载Maven
访问：https://maven.apache.org/download.cgi
下载：`apache-maven-3.9.x-bin.zip`（约9MB）

### 2. 解压
将下载的zip文件解压到：`C:\apache-maven`

### 3. 配置环境变量

#### 方法1：通过GUI配置
1. 右键点击"此电脑" → 属性
2. 点击"高级系统设置"
3. 点击"环境变量"
4. 在"系统变量"中点击"新建"：
   - 变量名：`MAVEN_HOME`
   - 变量值：`C:\apache-maven`
5. 找到"Path"变量，点击"编辑"
6. 点击"新建"，添加：`%MAVEN_HOME%\bin`
7. 点击"确定"保存所有更改

#### 方法2：通过PowerShell配置（管理员权限）
```powershell
[Environment]::SetEnvironmentVariable("MAVEN_HOME", "C:\apache-maven", "Machine")
$path = [Environment]::GetEnvironmentVariable("Path", "Machine")
[Environment]::SetEnvironmentVariable("Path", "$path;%MAVEN_HOME%\bin", "Machine")
```

### 4. 验证安装
关闭并重新打开PowerShell，运行：
```powershell
mvn -v
```

应该看到类似输出：
```
Apache Maven 3.9.x
Maven home: C:\apache-maven
Java version: 1.8.0_xxx
```

### 5. 启动后端
```powershell
cd "E:\图灵 报名功能\后端\tuling"
mvn spring-boot:run
```

---

## 如果您没有Java

Maven需要Java，如果提示Java未安装：

### 下载安装JDK
1. 访问：https://www.oracle.com/java/technologies/downloads/
2. 下载 JDK 8 或更高版本
3. 安装后配置JAVA_HOME环境变量

---

安装完成后，请告诉我！
