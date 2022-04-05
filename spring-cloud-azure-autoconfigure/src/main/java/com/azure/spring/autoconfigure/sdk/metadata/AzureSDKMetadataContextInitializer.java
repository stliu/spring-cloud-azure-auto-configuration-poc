package com.azure.spring.autoconfigure.sdk.metadata;

import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.spring.autoconfigure.AzureSDKMetadata;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
//import org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.List;

class AzureSDKMetadataContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>,
    Ordered {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AzureSDKMetadataContextInitializer.class);
    public static final String BEAN_NAME = "com.azure.spring.autoconfigure.sdk.metadata.azureSDKMetadataReaderFactory";

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        BeanFactoryPostProcessor postProcessor = new AzureSDKMetadataReaderFactoryPostProcessor(applicationContext);
        applicationContext.addBeanFactoryPostProcessor(postProcessor);
        ((GenericApplicationContext) applicationContext).registerBean(AzureSDKTraitsBeanPostProcessor.class, ()->new AzureSDKTraitsBeanPostProcessor(applicationContext));
    }

    static class AzureSDKTraitsBeanPostProcessor implements BeanPostProcessor {
        private final ConfigurableApplicationContext context;
        AzureSDKTraitsBeanPostProcessor(ConfigurableApplicationContext context) {
            this.context = context;
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

            if (bean instanceof TokenCredentialTrait) {

                LOGGER.info("Bean {} has TokenCredentialTrait, now setting token credential", beanName);
                TokenCredential credential = context.getBean(TokenCredential.class);

                ((TokenCredentialTrait) bean).credential(credential);
            }
            return bean;
        }

    }

    static class AzureSDKMetadataReaderFactoryPostProcessor implements BeanDefinitionRegistryPostProcessor,
        PriorityOrdered {
        private final ConfigurableApplicationContext context;

        AzureSDKMetadataReaderFactoryPostProcessor(ConfigurableApplicationContext context) {
            this.context = context;
        }

        @Override
        public int getOrder() {
            // Must happen after org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer
            // .CachingMetadataReaderFactoryPostProcessor
            return Ordered.HIGHEST_PRECEDENCE + 1;
        }

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            register(registry);
            //            configureConfigurationClassPostProcessor(registry);
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        }

        private void register(BeanDefinitionRegistry registry) {

            List<AzureSDKMetadata> sdkMetadataList = SpringFactoriesLoader.loadFactories(AzureSDKMetadata.class,
                context.getClassLoader());


            ConfigurableEnvironment environment = context.getEnvironment();

            for (AzureSDKMetadata sdkMetadata : sdkMetadataList) {
                LOGGER.info("registering Azure SDK for {}", sdkMetadata.getServiceName());

                /**
                 * TODO we need to figure out how to apply those condition check for example
                 *
                 * @ConditionalOnClass(BlobServiceClientBuilder.class)
                 * @ConditionalOnProperty(value = "spring.cloud.azure.storage.blob.enabled", havingValue = "true",
                 * matchIfMissing = true)
                 * @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.storage.blob", name = { "account-name",
                 * "endpoint", "connection-string" })
                 */
                BeanDefinitionBuilder builder =
                    BeanDefinitionBuilder.genericBeanDefinition(sdkMetadata.getServiceClientBuilderClass());

//                builder.addAutowiredProperty()
                registry.registerBeanDefinition(sdkMetadata.getServiceName(), builder.getBeanDefinition());
            }
            //            if (!registry.containsBeanDefinition(BEAN_NAME)) {
            //                BeanDefinition definition = BeanDefinitionBuilder
            //                    .rootBeanDefinition(SharedMetadataReaderFactoryContextInitializer
            //                    .SharedMetadataReaderFactoryBean.class,
            //                    SharedMetadataReaderFactoryContextInitializer.SharedMetadataReaderFactoryBean::new)
            //                    .getBeanDefinition();
            //                registry.registerBeanDefinition(BEAN_NAME, definition);
            //            }


        }


    }
}
