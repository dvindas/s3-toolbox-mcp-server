package com.github.dvindas.mcpserver.s3toolbox;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;

import java.util.Map;

/**
 * Test class for S3 Toolbox MCP Client.
 * Make sure to build the server jar first:
 * <pre>
 * mvn clean install -DskipTests
 * </pre>
 */
public class S3ToolboxClient {

    public static void main(String[] args) {

        var stdioParams = ServerParameters.builder("java")
                .args("-jar", "target/s3-toolbox-mcp-server-1.0.0.jar")
                .env(Map.of(
                        "AWS_ACCESS_KEY_ID", "test",
                        "AWS_SECRET_ACCESS_KEY", "test",
                        "AWS_REGION", "us-east-1"
                ))
                .build();

        var transport = new StdioClientTransport(stdioParams);
        var client = McpClient.sync(transport).build();

        client.initialize();

        // List and demonstrate tools
        ListToolsResult toolsList = client.listTools();
        System.out.println("Available Tools = " + toolsList);

        client.closeGracefully();
    }
} 