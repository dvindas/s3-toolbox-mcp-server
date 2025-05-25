package com.github.dvindas.mcpserver.s3toolbox.config;

import com.github.dvindas.mcpserver.s3toolbox.tool.S3Tool;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author dvindas
 */
@Configuration
public class MCPConfiguration {

    @Bean
    public List<ToolCallback> registerTools(S3Tool S3Tool) {
        return List.of(ToolCallbacks.from(S3Tool));
    }

}