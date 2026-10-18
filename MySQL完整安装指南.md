# MySQL 8.0 完整安装指南

## 📥 下载MySQL

### 官方下载地址
https://dev.mysql.com/downloads/mysql/

### 国内镜像（推荐，速度快）
华为云镜像：
https://repo.huaweicloud.com/mysql/Downloads/MySQL-8.0/mysql-8.0.33-winx64.zip

阿里云镜像：
https://mirrors.aliyun.com/mysql/MySQL-8.0/mysql-8.0.33-winx64.zip

**选择下载：mysql-8.0.33-winx64.zip（约350MB）**

---

## 🔧 安装步骤

### 步骤1：解压MySQL
1. 下载完成后，解压到 `C:\mysql-8.0.33`
2. 确保目录结构：`C:\mysql-8.0.33\bin\mysqld.exe`

### 步骤2：创建配置文件
在 `C:\mysql-8.0.33` 目录下创建文件 `my.ini`：

```ini
[mysqld]
# 设置3306端口
port=3306

# 设置mysql的安装目录
basedir=C:\\mysql-8.0.33

# 设置mysql数据库的数据存放目录
datadir=C:\\mysql-8.0.33\\data

# 允许最大连接数
max_connections=200

# 允许连接失败的次数
max_connect_errors=10

# 服务端使用的字符集
character-set-server=utf8mb4

# 默认存储引擎
default-storage-engine=INNODB

# 默认使用"mysql_native_password"插件认证
default_authentication_plugin=mysql_native_password

[mysql]
# 设置mysql客户端默认字符集
default-character-set=utf8mb4

[client]
# 设置mysql客户端连接服务端时默认使用的端口
port=3306
default-character-set=utf8mb4
```

### 步骤3：初始化MySQL
以管理员身份打开PowerShell，运行：

```powershell
cd C:\mysql-8.0.33\bin
.\mysqld --initialize --console
```

**重要**：记录初始密码！会显示类似：
```
[Note] A temporary password is generated for root@localhost: abc123XYZ!
```

### 步骤4：安装MySQL服务
```powershell
.\mysqld --install MySQL
```

### 步骤5：启动MySQL服务
```powershell
net start MySQL
```

### 步骤6：修改root密码
```powershell
.\mysql -u root -p
# 输入刚才记录的临时密码

# 在MySQL中执行
ALTER USER 'root'@'localhost' IDENTIFIED BY '你的新密码';
FLUSH PRIVILEGES;
exit;
```

### 步骤7：配置环境变量（可选）
- 新建系统变量：`MYSQL_HOME` = `C:\mysql-8.0.33`
- 在Path中添加：`%MYSQL_HOME%\bin`

---

## ✅ 验证安装

```powershell
mysql -u root -p
# 输入你设置的新密码

# 应该能成功登录
```

---

## 🗄️ 创建项目数据库

登录MySQL后，运行：

```sql
CREATE DATABASE tuling_society CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE tuling_society;

source E:/图灵 报名功能/后端/tuling/src/main/resources/db/migration/V2__add_event_registration_tables.sql;

-- 验证表是否创建
SHOW TABLES;

-- 应该看到 event_registrations 表
```

---

## 🔧 配置Spring Boot

编辑 `后端/tuling/src/main/resources/application.yml`：

```yaml
datasource:
  url: jdbc:mysql://localhost:3306/tuling_society?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
  username: root
  password: 你的MySQL密码  # 改这里
```

---

## 🚀 启动后端

```powershell
cd "E:\图灵 报名功能\后端\tuling"
mvn spring-boot:run
```

等待看到：
```
Started Application in XX seconds
```

---

## 🎨 启动前端

新开一个PowerShell窗口：

```powershell
cd "E:\图灵 报名功能\前端\tuling-web"
npm run dev
```

---

## 🌐 访问系统

浏览器打开：http://localhost:5173/registrations/

---

## ❌ 常见问题

### 问题1：mysqld 命令不存在
**解决**：确认在 `C:\mysql-8.0.33\bin` 目录下运行

### 问题2：服务启动失败
**解决**：
```powershell
# 删除服务
sc delete MySQL

# 重新安装
.\mysqld --install MySQL

# 启动
net start MySQL
```

### 问题3：忘记临时密码
**解决**：
1. 删除 `C:\mysql-8.0.33\data` 目录
2. 重新运行初始化命令
3. 记录新的临时密码

### 问题4：无法连接数据库
**解决**：
```powershell
# 检查MySQL服务是否运行
Get-Service -Name MySQL

# 如果未运行
net start MySQL
```

---

## 🎯 安装完成检查清单

- [ ] MySQL解压到 C:\mysql-8.0.33
- [ ] my.ini 配置文件已创建
- [ ] mysqld --initialize 执行成功
- [ ] 临时密码已记录
- [ ] MySQL服务已安装并启动
- [ ] root密码已修改
- [ ] tuling_society 数据库已创建
- [ ] 表结构已导入
- [ ] application.yml 密码已配置
- [ ] 后端启动成功
- [ ] 前端启动成功
- [ ] 浏览器可以访问

---

请按照以上步骤操作，遇到问题随时告诉我！
