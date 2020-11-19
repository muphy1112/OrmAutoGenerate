package me.muphy.service;

import me.muphy.config.RuphyMapperProperties;
import me.muphy.entity.ColumnEntity;
import me.muphy.mapper.TableMapper;
import me.muphy.util.NamingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableService {

    private TableMapper tableMapper;
    private RuphyMapperProperties properties;
    private Map<String, String> typeMap;
    private final Logger logger = LoggerFactory.getLogger(TableService.class);

    public TableService(TableMapper tableMapper, RuphyMapperProperties properties) {
        this.tableMapper = tableMapper;
        this.properties = properties;
    }

    public boolean createAllFile(String schema) {
        List<String> tables = tableMapper.getTablesFromSchema(schema);
        for (String tableName : tables) {
            createTkMapper();
            createEntity(tableName, schema);
            createMapper(tableName);
            createMapperConfig(tableName, schema);
        }
        return true;
    }

    public boolean createTkMapper() {
        String filePath = NamingUtils.getCurrentPath(properties.getMapperPath(), "TkMapper.java");
        File file = new File(filePath);
        if (file.exists()) {
            logger.info("文件已经存在：" + filePath);
            return false;
        }
        file.getParentFile().mkdirs();
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("package " + getPackageName(properties.getMapperPath(), "mapper") + ";\n\n");
            fileWriter.write("import tk.mybatis.mapper.common.Mapper;\n");
            fileWriter.write("import org.springframework.stereotype.Component;\n");
            fileWriter.write("import tk.mybatis.mapper.common.MySqlMapper;\n\n");
            fileWriter.write("@Component");
            fileWriter.write("public interface TkMapper<T> extends Mapper<T>, MySqlMapper<T> {\n}");
            fileWriter.close();
            logger.info("创建文件成功：" + filePath);
        } catch (IOException e) {
            logger.info("创建文件失败：" + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean createMapperConfigs(String schema) {
        List<String> tables = tableMapper.getTablesFromSchema(schema);
        for (String tableName : tables) {
            createMapperConfig(tableName, schema);
        }
        return true;
    }

    public boolean createMapperConfig(String tableName, String schema) {
        List<ColumnEntity> entities = tableMapper.getTableDescribe(tableName, schema);
        if (entities == null || entities.size() <= 0) {
            logger.info("没有查询到表相关的列：" + tableName);
            return false;
        }
        String filePath = NamingUtils.getCurrentPath(properties.getMapperPath(), NamingUtils.getUpperCamelCase(tableName) + "Mapper.xml");
        File file = new File(filePath);
        if (file.exists()) {
            logger.info("文件已经存在：" + filePath);
            if (!properties.isReplaceFile()) {
                return false;
            }
        }
        file.getParentFile().mkdirs();
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(getMapperConfigString(tableName, schema));
            fileWriter.close();
            logger.info("创建文件成功：" + filePath);
        } catch (IOException e) {
            logger.info("创建文件失败：" + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean createEntities(String schema) {
        List<String> tables = tableMapper.getTablesFromSchema(schema);
        for (String tableName : tables) {
            createEntity(tableName, schema);
        }
        return true;
    }

    public boolean createEntity(String tableName, String schema) {
        String filePath = NamingUtils.getCurrentPath(properties.getEntityPath(), NamingUtils.getUpperCamelCase(tableName) + ".java");
        File file = new File(filePath);
        if (file.exists()) {
            logger.info("文件已经存在：" + filePath);
            if (!properties.isReplaceFile()) {
                return false;
            }
        }
        file.getParentFile().mkdirs();
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(getEntityString(tableName, schema));
            fileWriter.close();
            logger.info("创建文件成功：" + filePath);
        } catch (IOException e) {
            logger.info("创建文件失败：" + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean createMappers(String schema) {
        List<String> tables = tableMapper.getTablesFromSchema(schema);
        for (String tableName : tables) {
            createMapper(tableName);
        }
        return true;
    }

    public boolean createMapper(String tableName) {
        if (NamingUtils.isEmpty(tableName)) {
            return false;
        }
        String entityName = NamingUtils.getUpperCamelCase(tableName);
        String path = NamingUtils.getCurrentPath(properties.getMapperPath(), entityName) + "Mapper.java";
        File mapperFile = new File(path);
        if (mapperFile.exists()) {
            logger.info("文件已经存在：" + path);
            if (!properties.isReplaceFile()) {
                return false;
            }
        }
        mapperFile.getParentFile().mkdirs();
        StringBuilder sb = new StringBuilder();
        sb.append("package " + getPackageName(properties.getMapperPath(), "mapper") + ";\n\n");
        sb.append("import org.apache.ibatis.annotations.Mapper;\n");
        sb.append("import " + getPackageName(properties.getEntityPath(), "entity") + "." + entityName + ";\n\n");
        sb.append("@Mapper\n");
        sb.append("public interface " + entityName + "Mapper extends TkMapper<" + entityName + "> {\n}");
        try {
            FileWriter fileWriter = new FileWriter(mapperFile);
            fileWriter.write(sb.toString());
            fileWriter.close();
            logger.info("创建Mapper：" + path);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private String getPackageName(String path, String defaultPackageName) {
        String pkgName = path.replaceAll(".+/src/main/java/(.+)$", "$1");
        if (pkgName.equals(path)) {
            pkgName = defaultPackageName;
        } else {
            pkgName = pkgName.replaceAll("/$", "").replaceAll("/", ".");
        }
        return pkgName;
    }

    private String getMapperConfigString(String tableName, String schema) {
        String mapperPkgName = getPackageName(properties.getMapperPath(), "mapper");
        String entityPkgName = getPackageName(properties.getEntityPath(), "entity");
        String tableCamelCase = NamingUtils.getUpperCamelCase(tableName);
        List<ColumnEntity> entities = tableMapper.getTableDescribe(tableName, schema);
        if (entities == null || entities.size() <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n");
        sb.append("<mapper namespace=\"" + mapperPkgName + "." + tableCamelCase + "Mapper\">\n");
        sb.append("\t<resultMap id=\"BaseResultMap\" type=\"" + entityPkgName + "." + tableCamelCase + "\">\n");
        for (ColumnEntity column : entities) {
            String columnName = column.getColumnName();
            if ("id".equalsIgnoreCase(columnName)) {
                sb.append("\t\t<id column=\"" + columnName + "\" jdbcType=\"" + column.getDataType() + "\" property=\"id\" />\n");
            } else {
                sb.append("\t\t<result column=\"" + columnName + "\" jdbcType=\"" + column.getDataType()
                        + "\" property=\"" + NamingUtils.getLowerCamelCase(columnName) + "\" />\n");
            }
        }
        sb.append("\t</resultMap>\n");
        sb.append("\t<sql id=\"BaseColumns\">\n\t\t");
        for (int i = 0; i < entities.size(); i++) {
            sb.append(entities.get(i).getColumnName());
            if (i < entities.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("\n\t</sql>\n");
        sb.append("</mapper>");
        return sb.toString();
    }

    private String getEntityString(String tableName, String schema) {
        String pkgName = getPackageName(properties.getEntityPath(), "entity");
        List<ColumnEntity> entities = tableMapper.getTableDescribe(tableName, schema);
        if (entities == null || entities.size() <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        StringBuilder msb = new StringBuilder();
        sb.append("package " + pkgName + ";\n\n");
        sb.append("import java.util.Date;\n\n");
        sb.append("public class " + NamingUtils.getUpperCamelCase(tableName) + " {\n\n");
        for (int i = 0; i < entities.size(); i++) {
            String fieldUpperCamelCase = NamingUtils.getUpperCamelCase(entities.get(i).getColumnName());
            String fieldLowerCamelCase = NamingUtils.getLowerCamelCase(entities.get(i).getColumnName());
            String type = ConvertType(entities.get(i).getDataType());
            sb.append("\tprivate " + type + " " + fieldLowerCamelCase + ";\n");
            msb.append("\tpublic " + type + " get" + fieldUpperCamelCase + "() {\n");
            msb.append("\t\treturn " + fieldLowerCamelCase + ";\n");
            msb.append("\t}\n\n");
            msb.append("\tpublic void set" + fieldUpperCamelCase + "(" + type + " " + fieldLowerCamelCase + ") {\n");
            msb.append("\t\tthis." + fieldLowerCamelCase + " = " + fieldLowerCamelCase + ";\n");
            msb.append("\t}\n\n");
        }
        sb.append("\n");
        sb.append(msb.toString());
        sb.append("}\n");
        return sb.toString();
    }

    private String ConvertType(String dbType) {
        if (NamingUtils.isEmpty(dbType)) {
            return "String";
        }
        if (this.typeMap == null) {
            this.typeMap = new HashMap<String, String>();
            this.typeMap.put("varchar", "String");
            this.typeMap.put("longtext", "String");
            this.typeMap.put("text", "String");
            this.typeMap.put("char", "String");
            this.typeMap.put("longblob", "String");
            this.typeMap.put("mediumtext", "String");
            this.typeMap.put("blob", "String");
            this.typeMap.put("set", "String");
            this.typeMap.put("enum", "String");
            this.typeMap.put("bigint", "Integer");
            this.typeMap.put("int", "Integer");
            this.typeMap.put("bit", "Integer");
            this.typeMap.put("smallint", "Integer");
            this.typeMap.put("tinyint", "Integer");
            this.typeMap.put("decimal", "Integer");
            this.typeMap.put("double", "double");
            this.typeMap.put("time", "Date");
            this.typeMap.put("timestamp", "Date");
            this.typeMap.put("datetime", "Date");
        }
        String type = typeMap.get(dbType);
        if (NamingUtils.isEmpty(type)) {
            return "String";
        }
        return type;
    }

}
