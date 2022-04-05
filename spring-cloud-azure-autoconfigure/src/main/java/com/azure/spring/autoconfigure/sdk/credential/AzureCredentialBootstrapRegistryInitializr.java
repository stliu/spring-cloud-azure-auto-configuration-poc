package com.azure.spring.autoconfigure.sdk.credential;


import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.slf4j.Logger;
import org.springframework.boot.BootstrapContext;
import org.springframework.boot.BootstrapContextClosedEvent;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Provide default {@link com.azure.identity.DefaultAzureCredential} to {@link BootstrapContext}.
 * <p>
 * Also, then the BootstrapContext will be closed, the {@link com.azure.identity.DefaultAzureCredential} will be
 * populated into Application Context.
 */
class AzureCredentialBootstrapRegistryInitializr implements BootstrapRegistryInitializer,
    ApplicationListener<BootstrapContextClosedEvent> {
    private static final Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(AzureCredentialBootstrapRegistryInitializr.class);

    @Override
    public void initialize(BootstrapRegistry registry) {
        LOGGER.info("Initialize AzureCredentialBootstrapRegistryInitializr");
        registry.addCloseListener(this);
        registry.registerIfAbsent(TokenCredential.class, context -> new DefaultAzureCredentialBuilder().build());
    }

    @Override
    public void onApplicationEvent(BootstrapContextClosedEvent event) {
        LOGGER.info("BootstrapContextClosedEvent received, populate DefaultAzureCredential into ApplicationContext");

        BootstrapContext bootstrapContext = event.getBootstrapContext();

        GenericApplicationContext applicationContext = (GenericApplicationContext) event.getApplicationContext();

        TokenCredential tokenCredential = bootstrapContext.get(TokenCredential.class);
        applicationContext.registerBean(TokenCredential.class, () -> tokenCredential);
    }

    /**
     * copied from Spring Cloud Vault's org.springframework.cloud.vault.config.VaultConfigDataLoader#registerIfAbsent
     * (org.springframework.boot.ConfigurableBootstrapContext, java.lang.String, java.lang.Class<T>, java.util
     * .function.Function<org.springframework.boot.BootstrapContext,T>, java.util.function.Consumer<org
     * .springframework.context.ConfigurableApplicationContext>)
     *
     */
    //    static <T> void registerIfAbsent(ConfigurableBootstrapContext bootstrap, String beanName, Class<T>
    //    instanceType,
    //                                     Function<BootstrapContext, T> instanceSupplier,
    //                                     Consumer<ConfigurableApplicationContext> contextCustomizer) {
    //
    //        bootstrap.registerIfAbsent(instanceType, instanceSupplier::apply);
    //
    //        bootstrap.addCloseListener(event -> {
    //
    //            GenericApplicationContext gac = (GenericApplicationContext) event.getApplicationContext();
    //
    //            contextCustomizer.accept(gac);
    //            T instance = event.getBootstrapContext().get(instanceType);
    //
    //            gac.registerBean(beanName, instanceType, () -> instance);
    //        });
    //    }

}
