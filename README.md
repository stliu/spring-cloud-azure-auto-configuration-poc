# Goal

Explore a way to provide spring boot auto configuration support for Azure SDKs by defining SDK metadata, without creating lots of duplicated auto configuration classes, one for each Azure SDK.


# Problem

[Spring Cloud Azure](https://spring.io/projects/spring-cloud-azure) is a set of open source Spring Cloud projects that enable developers to build, deploy, and manage applications on the Azure cloud.

One of feature that Spring Cloud Azure provides is Spring Boot's auto-configuration support and Spring Boot Actuator Health Indicator.

But the current implementation has a lot of duplicated auto configuration classes, one for each Azure SDK.
All of these are following the same pattern, this is because of all of Azure SDKs have similiar patterns.

In short, all of Azure SDKs have:

* {Service}ClientBuilder
* {Service}Client
* {Service}AsyncClient

And in the Spring Cloud Azure code, we have:

* `com.azure.spring.cloud.autoconfigure.appconfiguration.AzureAppConfigurationAutoConfiguration`
* `com.azure.spring.cloud.autoconfigure.keyvault.secrets.AzureKeyVaultSecretAutoConfiguration`

The implementation of these two auto configuration classes share same pattern:

Conditions:

1. If the `{Service}ClientBuilder` is  present on classpath.
2. If this service is explicitly disabled
3. If the service endpoint property is provided

And the auto configuration classes do the same thing:

1. register a `{Service}ClientBuilder` bean
2. register a `{Service}Client` bean
3. register a `{Service}AsyncClient` bean

So, how can we clean up those duplications?

# Solution

I'm wondering if it is possible to resolve this duplication problem by define a Azure SDK Metadata interface.

```java

interface AzureSDKMetadata {
    String getServiceName();
    String getServiceEndpoint();
    String getServiceClientBuilderClassName();
    String getServiceClientClassName();
    String getServiceAsyncClientClassName();
}

```

Then at some point during spring context initialization, probably `BeanFactoryPostProcessor` (since auto configuration, by nature, it's a bean factory):

```java

for(AzureSDKMetadata sdkMetadata: azureSDKMetadataList){
    if(checkCondition(sdkMetadata)){
        registerServiceBuilderBean(sdkMetadata);
        registerServiceClientBean(sdkMetadata);
        registerServiceAsyncClientBean(sdkMetadata);
    }    
}

```

We can do the same thing for Spring Boot Actuator Health Indicator.

# How to do that?

Since the whole idea of this poc is replacing those static AutoConfiguration classes by programmatically defined metadata, so, the first thing we need to know is how those auto configuration classes are used and when.

We know auto configuration classes are defined in the `META-INF/spring.factories` file, like [this one](https://github.com/Azure/azure-sdk-for-java/blob/5bc550c9a5de4f8ee93a5b3141500ee34be3850d/sdk/spring/spring-cloud-azure-autoconfigure/src/main/resources/META-INF/spring.factories#L6)



