package com.azure.spring.autoconfigure.storage.blob;

import com.azure.spring.autoconfigure.AzureSDKMetadata;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

public class AzureStorageBlobMetadata implements AzureSDKMetadata {
    @Override
    public String getServiceName() {
        return "storage.blob";
    }

    @Override
    public Class<?> getServiceClientBuilderClass() {
        return BlobServiceClientBuilder.class;
    }

    @Override
    public Class<?> getServiceClientClass() {
        return BlobServiceClient.class;
    }

    @Override
    public Class<?> getServiceAsyncClientClass() {
        return BlobServiceAsyncClient.class;
    }
}
