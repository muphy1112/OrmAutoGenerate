package me.muphy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "muphy.mapper.autogenerate")
public class RuphyMapperProperties {
    private String entityPath;
    private String mapperPath;
    private boolean replaceFile = false;

    public String getEntityPath() {
        return entityPath;
    }

    public void setEntityPath(String entityPath) {
        this.entityPath = entityPath;
    }

    public String getMapperPath() {
        return mapperPath;
    }

    public void setMapperPath(String mapperPath) {
        this.mapperPath = mapperPath;
    }

    public boolean isReplaceFile() {
        return replaceFile;
    }

    public void setReplaceFile(boolean replaceFile) {
        this.replaceFile = replaceFile;
    }
}
