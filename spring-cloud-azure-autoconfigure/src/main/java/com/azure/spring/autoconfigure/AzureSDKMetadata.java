package com.azure.spring.autoconfigure;

public interface AzureSDKMetadata {
    String getServiceName();
    Class<?> getServiceClientBuilderClass();
    Class<?> getServiceClientClass();
    Class<?> getServiceAsyncClientClass();
}
