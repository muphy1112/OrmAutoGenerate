package me.muphy;

import me.muphy.config.RuphyMapperProperties;
import me.muphy.mapper.TableMapper;
import me.muphy.service.TableService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnClass(DataSource.class)
@EnableConfigurationProperties(RuphyMapperProperties.class)
@MapperScan("me.muphy.mapper")
public class RuphyMapperAutoConfiguration {

    @Autowired
    private TableMapper tableMapper;
    @Autowired
    private RuphyMapperProperties properties;

    @Bean
    public TableService tableService(){
        return new TableService(tableMapper, properties);
    }
}
