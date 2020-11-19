# ruphy-tk-mapper-spring-boot-starter
自动生成基于 TkMapper、Mybaties、SpringBoot Starter 的ORM结构，使用很简单，不过我很纠结到底使用starter还是单独的工具包好，TkMapper默认提供很多的通用curd方法，字段与列名映射关系在mapper.xml中写

## spring boot 项目结构
### 命名规范
* mapper接口名和XML文件名用表名转为大驼峰命名方式 + Mapper
* entity类名用表名转为大驼峰命名方式
### mapper存储结构
```text
com.example.xxx.mapper
|--------------------.TableName1Mapper.java
|--------------------.TableName1Mapper.xml
|--------------------.TableName2Mapper.java
|--------------------.TableName2Mapper.xml
# 这个接口是固定的，上面所有接口都要继承此接口
|--------------------.TkMapper.xml
```
### entity存储结构
```text
com.example.xxx.entity
|--------------------.TableName1.java
|--------------------.TableName2.java
```

## 使用方式
### 安装
```text
git@github.com:muphy1112/ruphy-tk-mapper-spring-boot-starter.git
cd ruphy-tk-mapper-spring-boot-starter
maven install
```
### 添加pom依赖
```xml
<dependency>
    <groupId>me.muphy</groupId>
    <artifactId>ruphy-tk-mapper-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
### 增加外部化配置
```properties
# 目前只支持使用绝对路径，这样也方便为其他项目生成文件，如下配置会自动为生成的类添加包名：me.muphy.entity 和 me.muphy.mapper
muphy.mapper.autogenerate.entity-path=E:/workspace/recorder/src/main/java/me/muphy/entity
muphy.mapper.autogenerate.mapper-path=E:/workspace/recorder/src/main/java/me/muphy/mapper
# 此配置设置是否覆盖已经存在的文件
muphy.mapper.autogenerate.replace-file=false
# 添加datasource相关的配置
```
### 运行
```java
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);
        TableService service = context.getBean(TableService.class);
        service.createAllFile("hacker");
    }
}
```

## 生成后的文件效果，以表sys_user(CREATE TABLE `sys_user` ( `ID` bigint(20),  `ACCOUNT` varchar(45),  `PASSWORD` varchar(45));)为例：
### me.muphy.mapper.TkMapper.java
```java
package me.muphy.mapper;

import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

@Component
public interface TkMapper<T> extends Mapper<T>, MySqlMapper<T> {
}
```
### me.muphy.mapper.SysUserMapper.java
```java
package me.muphy.mapper;

import org.apache.ibatis.annotations.Mapper;
import me.muphy.entity.SysUser;
//import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends TkMapper<SysUser> {
    //可以直接增加一些接口， 此句与下面举例方法不属于生成的代码
    //@Select("select * from sys_user where account like '%${name}%'")
    //List<SysUser> getUsers(String name);
}
```
### me.muphy.mapper.SysUserMapper.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="me.muphy.mapper.SysUserMapper">
	<resultMap id="BaseResultMap" type="me.muphy.entity.SysUser">
		<id column="ID" jdbcType="bigint" property="id" />
		<result column="ACCOUNT" jdbcType="varchar" property="account" />
		<result column="PASSWORD" jdbcType="varchar" property="password" />
	</resultMap>
	<sql id="BaseColumns">
		ID,ACCOUNT,PASSWORD
	</sql>
</mapper>
```