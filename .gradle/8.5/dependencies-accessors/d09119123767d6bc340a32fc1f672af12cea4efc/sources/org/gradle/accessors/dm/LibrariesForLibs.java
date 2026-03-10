package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.artifacts.dsl.CapabilityNotationParser;
import javax.inject.Inject;

/**
 * A catalog of dependencies accessible via the `libs` extension.
 */
@NonNullApi
public class LibrariesForLibs extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final AndroidxLibraryAccessors laccForAndroidxLibraryAccessors = new AndroidxLibraryAccessors(owner);
    private final FirebaseLibraryAccessors laccForFirebaseLibraryAccessors = new FirebaseLibraryAccessors(owner);
    private final HiltLibraryAccessors laccForHiltLibraryAccessors = new HiltLibraryAccessors(owner);
    private final JjwtLibraryAccessors laccForJjwtLibraryAccessors = new JjwtLibraryAccessors(owner);
    private final LombokLibraryAccessors laccForLombokLibraryAccessors = new LombokLibraryAccessors(owner);
    private final MysqlLibraryAccessors laccForMysqlLibraryAccessors = new MysqlLibraryAccessors(owner);
    private final OkhttpLibraryAccessors laccForOkhttpLibraryAccessors = new OkhttpLibraryAccessors(owner);
    private final RetrofitLibraryAccessors laccForRetrofitLibraryAccessors = new RetrofitLibraryAccessors(owner);
    private final SpringLibraryAccessors laccForSpringLibraryAccessors = new SpringLibraryAccessors(owner);
    private final SpringdocLibraryAccessors laccForSpringdocLibraryAccessors = new SpringdocLibraryAccessors(owner);
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibs(DefaultVersionCatalog config, ProviderFactory providers, ObjectFactory objects, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) {
        super(config, providers, objects, attributesFactory, capabilityNotationParser);
    }

        /**
         * Creates a dependency provider for gson (com.google.code.gson:gson)
     * with versionRef 'gson'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getGson() {
            return create("gson");
    }

        /**
         * Creates a dependency provider for rxandroid (io.reactivex.rxjava3:rxandroid)
     * with versionRef 'rxandroid'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getRxandroid() {
            return create("rxandroid");
    }

        /**
         * Creates a dependency provider for rxjava (io.reactivex.rxjava3:rxjava)
     * with versionRef 'rxjava'.
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getRxjava() {
            return create("rxjava");
    }

    /**
     * Returns the group of libraries at androidx
     */
    public AndroidxLibraryAccessors getAndroidx() {
        return laccForAndroidxLibraryAccessors;
    }

    /**
     * Returns the group of libraries at firebase
     */
    public FirebaseLibraryAccessors getFirebase() {
        return laccForFirebaseLibraryAccessors;
    }

    /**
     * Returns the group of libraries at hilt
     */
    public HiltLibraryAccessors getHilt() {
        return laccForHiltLibraryAccessors;
    }

    /**
     * Returns the group of libraries at jjwt
     */
    public JjwtLibraryAccessors getJjwt() {
        return laccForJjwtLibraryAccessors;
    }

    /**
     * Returns the group of libraries at lombok
     */
    public LombokLibraryAccessors getLombok() {
        return laccForLombokLibraryAccessors;
    }

    /**
     * Returns the group of libraries at mysql
     */
    public MysqlLibraryAccessors getMysql() {
        return laccForMysqlLibraryAccessors;
    }

    /**
     * Returns the group of libraries at okhttp
     */
    public OkhttpLibraryAccessors getOkhttp() {
        return laccForOkhttpLibraryAccessors;
    }

    /**
     * Returns the group of libraries at retrofit
     */
    public RetrofitLibraryAccessors getRetrofit() {
        return laccForRetrofitLibraryAccessors;
    }

    /**
     * Returns the group of libraries at spring
     */
    public SpringLibraryAccessors getSpring() {
        return laccForSpringLibraryAccessors;
    }

    /**
     * Returns the group of libraries at springdoc
     */
    public SpringdocLibraryAccessors getSpringdoc() {
        return laccForSpringdocLibraryAccessors;
    }

    /**
     * Returns the group of versions at versions
     */
    public VersionAccessors getVersions() {
        return vaccForVersionAccessors;
    }

    /**
     * Returns the group of bundles at bundles
     */
    public BundleAccessors getBundles() {
        return baccForBundleAccessors;
    }

    /**
     * Returns the group of plugins at plugins
     */
    public PluginAccessors getPlugins() {
        return paccForPluginAccessors;
    }

    public static class AndroidxLibraryAccessors extends SubDependencyFactory {
        private final AndroidxCoreLibraryAccessors laccForAndroidxCoreLibraryAccessors = new AndroidxCoreLibraryAccessors(owner);
        private final AndroidxLifecycleLibraryAccessors laccForAndroidxLifecycleLibraryAccessors = new AndroidxLifecycleLibraryAccessors(owner);
        private final AndroidxNavigationLibraryAccessors laccForAndroidxNavigationLibraryAccessors = new AndroidxNavigationLibraryAccessors(owner);
        private final AndroidxRoomLibraryAccessors laccForAndroidxRoomLibraryAccessors = new AndroidxRoomLibraryAccessors(owner);
        private final AndroidxSecurityLibraryAccessors laccForAndroidxSecurityLibraryAccessors = new AndroidxSecurityLibraryAccessors(owner);

        public AndroidxLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for appcompat (androidx.appcompat:appcompat)
         * with versionRef 'appcompat'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getAppcompat() {
                return create("androidx.appcompat");
        }

            /**
             * Creates a dependency provider for constraintlayout (androidx.constraintlayout:constraintlayout)
         * with versionRef 'constraintlayout'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getConstraintlayout() {
                return create("androidx.constraintlayout");
        }

            /**
             * Creates a dependency provider for material (com.google.android.material:material)
         * with versionRef 'material'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getMaterial() {
                return create("androidx.material");
        }

        /**
         * Returns the group of libraries at androidx.core
         */
        public AndroidxCoreLibraryAccessors getCore() {
            return laccForAndroidxCoreLibraryAccessors;
        }

        /**
         * Returns the group of libraries at androidx.lifecycle
         */
        public AndroidxLifecycleLibraryAccessors getLifecycle() {
            return laccForAndroidxLifecycleLibraryAccessors;
        }

        /**
         * Returns the group of libraries at androidx.navigation
         */
        public AndroidxNavigationLibraryAccessors getNavigation() {
            return laccForAndroidxNavigationLibraryAccessors;
        }

        /**
         * Returns the group of libraries at androidx.room
         */
        public AndroidxRoomLibraryAccessors getRoom() {
            return laccForAndroidxRoomLibraryAccessors;
        }

        /**
         * Returns the group of libraries at androidx.security
         */
        public AndroidxSecurityLibraryAccessors getSecurity() {
            return laccForAndroidxSecurityLibraryAccessors;
        }

    }

    public static class AndroidxCoreLibraryAccessors extends SubDependencyFactory {

        public AndroidxCoreLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for ktx (androidx.core:core-ktx)
         * with versionRef 'coreKtx'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getKtx() {
                return create("androidx.core.ktx");
        }

    }

    public static class AndroidxLifecycleLibraryAccessors extends SubDependencyFactory {

        public AndroidxLifecycleLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for common (androidx.lifecycle:lifecycle-common-java8)
         * with versionRef 'lifecycle'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getCommon() {
                return create("androidx.lifecycle.common");
        }

            /**
             * Creates a dependency provider for livedata (androidx.lifecycle:lifecycle-livedata)
         * with versionRef 'lifecycle'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getLivedata() {
                return create("androidx.lifecycle.livedata");
        }

            /**
             * Creates a dependency provider for runtime (androidx.lifecycle:lifecycle-runtime)
         * with versionRef 'lifecycle'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getRuntime() {
                return create("androidx.lifecycle.runtime");
        }

            /**
             * Creates a dependency provider for viewmodel (androidx.lifecycle:lifecycle-viewmodel)
         * with versionRef 'lifecycle'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getViewmodel() {
                return create("androidx.lifecycle.viewmodel");
        }

    }

    public static class AndroidxNavigationLibraryAccessors extends SubDependencyFactory {

        public AndroidxNavigationLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for fragment (androidx.navigation:navigation-fragment)
         * with versionRef 'navigation'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getFragment() {
                return create("androidx.navigation.fragment");
        }

            /**
             * Creates a dependency provider for ui (androidx.navigation:navigation-ui)
         * with versionRef 'navigation'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getUi() {
                return create("androidx.navigation.ui");
        }

    }

    public static class AndroidxRoomLibraryAccessors extends SubDependencyFactory {

        public AndroidxRoomLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for compiler (androidx.room:room-compiler)
         * with versionRef 'room'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getCompiler() {
                return create("androidx.room.compiler");
        }

            /**
             * Creates a dependency provider for runtime (androidx.room:room-runtime)
         * with versionRef 'room'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getRuntime() {
                return create("androidx.room.runtime");
        }

            /**
             * Creates a dependency provider for rxjava3 (androidx.room:room-rxjava3)
         * with versionRef 'room'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getRxjava3() {
                return create("androidx.room.rxjava3");
        }

    }

    public static class AndroidxSecurityLibraryAccessors extends SubDependencyFactory {

        public AndroidxSecurityLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for crypto (androidx.security:security-crypto)
         * with versionRef 'securityCrypto'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getCrypto() {
                return create("androidx.security.crypto");
        }

    }

    public static class FirebaseLibraryAccessors extends SubDependencyFactory {

        public FirebaseLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for admin (com.google.firebase:firebase-admin)
         * with version '9.2.0'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getAdmin() {
                return create("firebase.admin");
        }

            /**
             * Creates a dependency provider for auth (com.google.firebase:firebase-auth-ktx)
         * with no version specified
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getAuth() {
                return create("firebase.auth");
        }

            /**
             * Creates a dependency provider for bom (com.google.firebase:firebase-bom)
         * with versionRef 'firebaseBom'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getBom() {
                return create("firebase.bom");
        }

    }

    public static class HiltLibraryAccessors extends SubDependencyFactory {

        public HiltLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for android (com.google.dagger:hilt-android)
         * with versionRef 'hilt'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getAndroid() {
                return create("hilt.android");
        }

            /**
             * Creates a dependency provider for compiler (com.google.dagger:hilt-compiler)
         * with versionRef 'hilt'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getCompiler() {
                return create("hilt.compiler");
        }

    }

    public static class JjwtLibraryAccessors extends SubDependencyFactory {

        public JjwtLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for api (io.jsonwebtoken:jjwt-api)
         * with versionRef 'jjwt'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getApi() {
                return create("jjwt.api");
        }

            /**
             * Creates a dependency provider for impl (io.jsonwebtoken:jjwt-impl)
         * with versionRef 'jjwt'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getImpl() {
                return create("jjwt.impl");
        }

            /**
             * Creates a dependency provider for jackson (io.jsonwebtoken:jjwt-jackson)
         * with versionRef 'jjwt'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getJackson() {
                return create("jjwt.jackson");
        }

    }

    public static class LombokLibraryAccessors extends SubDependencyFactory implements DependencyNotationSupplier {
        private final LombokMapstructLibraryAccessors laccForLombokMapstructLibraryAccessors = new LombokMapstructLibraryAccessors(owner);

        public LombokLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for lombok (org.projectlombok:lombok)
         * with version '1.18.30'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> asProvider() {
                return create("lombok");
        }

        /**
         * Returns the group of libraries at lombok.mapstruct
         */
        public LombokMapstructLibraryAccessors getMapstruct() {
            return laccForLombokMapstructLibraryAccessors;
        }

    }

    public static class LombokMapstructLibraryAccessors extends SubDependencyFactory {

        public LombokMapstructLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for binding (org.mapstruct:lombok-mapstruct-binding)
         * with version '0.2.0'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getBinding() {
                return create("lombok.mapstruct.binding");
        }

    }

    public static class MysqlLibraryAccessors extends SubDependencyFactory {

        public MysqlLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for connector (com.mysql:mysql-connector-j)
         * with version '8.3.0'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getConnector() {
                return create("mysql.connector");
        }

    }

    public static class OkhttpLibraryAccessors extends SubDependencyFactory implements DependencyNotationSupplier {

        public OkhttpLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for okhttp (com.squareup.okhttp3:okhttp)
         * with versionRef 'okhttp'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> asProvider() {
                return create("okhttp");
        }

            /**
             * Creates a dependency provider for logging (com.squareup.okhttp3:logging-interceptor)
         * with versionRef 'okhttp'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getLogging() {
                return create("okhttp.logging");
        }

    }

    public static class RetrofitLibraryAccessors extends SubDependencyFactory implements DependencyNotationSupplier {
        private final RetrofitAdapterLibraryAccessors laccForRetrofitAdapterLibraryAccessors = new RetrofitAdapterLibraryAccessors(owner);
        private final RetrofitConverterLibraryAccessors laccForRetrofitConverterLibraryAccessors = new RetrofitConverterLibraryAccessors(owner);

        public RetrofitLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for retrofit (com.squareup.retrofit2:retrofit)
         * with versionRef 'retrofit'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> asProvider() {
                return create("retrofit");
        }

        /**
         * Returns the group of libraries at retrofit.adapter
         */
        public RetrofitAdapterLibraryAccessors getAdapter() {
            return laccForRetrofitAdapterLibraryAccessors;
        }

        /**
         * Returns the group of libraries at retrofit.converter
         */
        public RetrofitConverterLibraryAccessors getConverter() {
            return laccForRetrofitConverterLibraryAccessors;
        }

    }

    public static class RetrofitAdapterLibraryAccessors extends SubDependencyFactory {

        public RetrofitAdapterLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for rxjava3 (com.squareup.retrofit2:adapter-rxjava3)
         * with versionRef 'retrofit'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getRxjava3() {
                return create("retrofit.adapter.rxjava3");
        }

    }

    public static class RetrofitConverterLibraryAccessors extends SubDependencyFactory {

        public RetrofitConverterLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for gson (com.squareup.retrofit2:converter-gson)
         * with versionRef 'retrofit'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getGson() {
                return create("retrofit.converter.gson");
        }

    }

    public static class SpringLibraryAccessors extends SubDependencyFactory {
        private final SpringBootLibraryAccessors laccForSpringBootLibraryAccessors = new SpringBootLibraryAccessors(owner);

        public SpringLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at spring.boot
         */
        public SpringBootLibraryAccessors getBoot() {
            return laccForSpringBootLibraryAccessors;
        }

    }

    public static class SpringBootLibraryAccessors extends SubDependencyFactory {
        private final SpringBootStarterLibraryAccessors laccForSpringBootStarterLibraryAccessors = new SpringBootStarterLibraryAccessors(owner);

        public SpringBootLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at spring.boot.starter
         */
        public SpringBootStarterLibraryAccessors getStarter() {
            return laccForSpringBootStarterLibraryAccessors;
        }

    }

    public static class SpringBootStarterLibraryAccessors extends SubDependencyFactory {
        private final SpringBootStarterDataLibraryAccessors laccForSpringBootStarterDataLibraryAccessors = new SpringBootStarterDataLibraryAccessors(owner);

        public SpringBootStarterLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for security (org.springframework.boot:spring-boot-starter-security)
         * with no version specified
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getSecurity() {
                return create("spring.boot.starter.security");
        }

            /**
             * Creates a dependency provider for validation (org.springframework.boot:spring-boot-starter-validation)
         * with no version specified
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getValidation() {
                return create("spring.boot.starter.validation");
        }

            /**
             * Creates a dependency provider for web (org.springframework.boot:spring-boot-starter-web)
         * with no version specified
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getWeb() {
                return create("spring.boot.starter.web");
        }

        /**
         * Returns the group of libraries at spring.boot.starter.data
         */
        public SpringBootStarterDataLibraryAccessors getData() {
            return laccForSpringBootStarterDataLibraryAccessors;
        }

    }

    public static class SpringBootStarterDataLibraryAccessors extends SubDependencyFactory {

        public SpringBootStarterDataLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for jpa (org.springframework.boot:spring-boot-starter-data-jpa)
         * with no version specified
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getJpa() {
                return create("spring.boot.starter.data.jpa");
        }

            /**
             * Creates a dependency provider for redis (org.springframework.boot:spring-boot-starter-data-redis)
         * with no version specified
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getRedis() {
                return create("spring.boot.starter.data.redis");
        }

    }

    public static class SpringdocLibraryAccessors extends SubDependencyFactory {

        public SpringdocLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for openapi (org.springdoc:springdoc-openapi-starter-webmvc-ui)
         * with versionRef 'springDoc'.
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getOpenapi() {
                return create("springdoc.openapi");
        }

    }

    public static class VersionAccessors extends VersionFactory  {

        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: agp (8.2.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getAgp() { return getVersion("agp"); }

            /**
             * Returns the version associated to this alias: appcompat (1.6.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getAppcompat() { return getVersion("appcompat"); }

            /**
             * Returns the version associated to this alias: constraintlayout (2.1.4)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getConstraintlayout() { return getVersion("constraintlayout"); }

            /**
             * Returns the version associated to this alias: coreKtx (1.12.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getCoreKtx() { return getVersion("coreKtx"); }

            /**
             * Returns the version associated to this alias: firebaseAuth (22.3.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getFirebaseAuth() { return getVersion("firebaseAuth"); }

            /**
             * Returns the version associated to this alias: firebaseBom (32.7.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getFirebaseBom() { return getVersion("firebaseBom"); }

            /**
             * Returns the version associated to this alias: gradle (8.5)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getGradle() { return getVersion("gradle"); }

            /**
             * Returns the version associated to this alias: gson (2.10.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getGson() { return getVersion("gson"); }

            /**
             * Returns the version associated to this alias: hilt (2.50)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getHilt() { return getVersion("hilt"); }

            /**
             * Returns the version associated to this alias: java (17)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getJava() { return getVersion("java"); }

            /**
             * Returns the version associated to this alias: jjwt (0.12.5)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getJjwt() { return getVersion("jjwt"); }

            /**
             * Returns the version associated to this alias: kotlin (1.9.22)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getKotlin() { return getVersion("kotlin"); }

            /**
             * Returns the version associated to this alias: lifecycle (2.7.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getLifecycle() { return getVersion("lifecycle"); }

            /**
             * Returns the version associated to this alias: material (1.11.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getMaterial() { return getVersion("material"); }

            /**
             * Returns the version associated to this alias: navigation (2.7.7)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getNavigation() { return getVersion("navigation"); }

            /**
             * Returns the version associated to this alias: okhttp (4.12.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getOkhttp() { return getVersion("okhttp"); }

            /**
             * Returns the version associated to this alias: retrofit (2.9.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getRetrofit() { return getVersion("retrofit"); }

            /**
             * Returns the version associated to this alias: room (2.6.1)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getRoom() { return getVersion("room"); }

            /**
             * Returns the version associated to this alias: rxandroid (3.0.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getRxandroid() { return getVersion("rxandroid"); }

            /**
             * Returns the version associated to this alias: rxjava (3.1.8)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getRxjava() { return getVersion("rxjava"); }

            /**
             * Returns the version associated to this alias: securityCrypto (1.1.0-alpha06)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getSecurityCrypto() { return getVersion("securityCrypto"); }

            /**
             * Returns the version associated to this alias: springBoot (3.2.3)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getSpringBoot() { return getVersion("springBoot"); }

            /**
             * Returns the version associated to this alias: springDoc (2.3.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getSpringDoc() { return getVersion("springDoc"); }

    }

    public static class BundleAccessors extends BundleFactory {

        public BundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

    }

    public static class PluginAccessors extends PluginFactory {
        private final AndroidPluginAccessors paccForAndroidPluginAccessors = new AndroidPluginAccessors(providers, config);
        private final KotlinPluginAccessors paccForKotlinPluginAccessors = new KotlinPluginAccessors(providers, config);
        private final SpringPluginAccessors paccForSpringPluginAccessors = new SpringPluginAccessors(providers, config);

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Creates a plugin provider for hilt to the plugin id 'com.google.dagger.hilt.android'
             * with versionRef 'hilt'.
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getHilt() { return createPlugin("hilt"); }

        /**
         * Returns the group of plugins at plugins.android
         */
        public AndroidPluginAccessors getAndroid() {
            return paccForAndroidPluginAccessors;
        }

        /**
         * Returns the group of plugins at plugins.kotlin
         */
        public KotlinPluginAccessors getKotlin() {
            return paccForKotlinPluginAccessors;
        }

        /**
         * Returns the group of plugins at plugins.spring
         */
        public SpringPluginAccessors getSpring() {
            return paccForSpringPluginAccessors;
        }

    }

    public static class AndroidPluginAccessors extends PluginFactory {

        public AndroidPluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Creates a plugin provider for android.application to the plugin id 'com.android.application'
             * with versionRef 'agp'.
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getApplication() { return createPlugin("android.application"); }

            /**
             * Creates a plugin provider for android.library to the plugin id 'com.android.library'
             * with versionRef 'agp'.
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getLibrary() { return createPlugin("android.library"); }

    }

    public static class KotlinPluginAccessors extends PluginFactory {

        public KotlinPluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Creates a plugin provider for kotlin.android to the plugin id 'org.jetbrains.kotlin.android'
             * with versionRef 'kotlin'.
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getAndroid() { return createPlugin("kotlin.android"); }

            /**
             * Creates a plugin provider for kotlin.kapt to the plugin id 'org.jetbrains.kotlin.kapt'
             * with versionRef 'kotlin'.
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getKapt() { return createPlugin("kotlin.kapt"); }

    }

    public static class SpringPluginAccessors extends PluginFactory {
        private final SpringDependencyPluginAccessors paccForSpringDependencyPluginAccessors = new SpringDependencyPluginAccessors(providers, config);

        public SpringPluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Creates a plugin provider for spring.boot to the plugin id 'org.springframework.boot'
             * with versionRef 'springBoot'.
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getBoot() { return createPlugin("spring.boot"); }

        /**
         * Returns the group of plugins at plugins.spring.dependency
         */
        public SpringDependencyPluginAccessors getDependency() {
            return paccForSpringDependencyPluginAccessors;
        }

    }

    public static class SpringDependencyPluginAccessors extends PluginFactory {

        public SpringDependencyPluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Creates a plugin provider for spring.dependency.management to the plugin id 'io.spring.dependency-management'
             * with versionRef 'springBoot'.
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getManagement() { return createPlugin("spring.dependency.management"); }

    }

}
