package com.contact.manager.config;

import com.contact.manager.entities.Candidate;
import com.contact.manager.listeners.CustomCacheEventLogger;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.*;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.impl.config.persistence.CacheManagerPersistenceConfiguration;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Caching;
import java.io.File;
import java.time.Duration;
import java.util.List;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfig implements InitializingBean {

    @Value("${cache.persistence.directory}")
    private String persistenceDirectory;

    @Bean
    public CacheManager cacheManager1(
            CustomCacheEventLogger eventLoggerConfig
    ) {
        var cache = CacheManagerBuilder.newCacheManagerBuilder()
                .using(PooledExecutionServiceConfigurationBuilder.newPooledExecutionServiceConfigurationBuilder()
                        .defaultPool("dflt", 0, 10)
                        .pool("defaultDiskPool", 2, 3)
                        .build())
                .with(new CacheManagerPersistenceConfiguration(new File(persistenceDirectory)))
                .withDefaultDiskStoreThreadPool("defaultDiskPool")
                .withCache("candidates",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, Candidate.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .heap(100, EntryUnit.ENTRIES))
                                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(5)))
                )
                .withCache("candidatesSearch",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, List.class,
                                        ResourcePoolsBuilder.newResourcePoolsBuilder()
                                                .heap(20, EntryUnit.ENTRIES))
//                                                .disk(20L, MemoryUnit.MB))
                                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(10)))
                )
                .withCache("positionsSearch",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, List.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .heap(20L, EntryUnit.ENTRIES))
                                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(5)))
                )
                .withCache("templates",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, List.class,
                                        ResourcePoolsBuilder.newResourcePoolsBuilder()
                                                .heap(1, EntryUnit.ENTRIES))
                                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(10)))
                )
                .build();
        var cachingProvider = Caching.getCachingProvider();
        var ehcacheProvider = (EhcacheCachingProvider) cachingProvider;

        return new JCacheCacheManager(ehcacheProvider.getCacheManager(ehcacheProvider.getDefaultURI(), cache.getRuntimeConfiguration()));
    }


//    @Bean
//    public CacheManager cacheManager(
//            CustomCacheEventLogger eventLoggerConfig
//    ) {
//
//        Map<String, CacheConfiguration<?, ?>> caches = Map.of(
//                "candidates", getCandidatesCacheConfiguration(),
////                "candidatesSearch", getCandidatesSearchCacheConfiguration(),
//                "positions", getPositionsCacheConfiguration()
//        );
//
//        var cachingProvider = Caching.getCachingProvider();
//        var ehcacheProvider = (EhcacheCachingProvider) cachingProvider;
//
//        PooledExecutionServiceConfiguration diskStoreThreadPool = PooledExecutionServiceConfigurationBuilder.newPooledExecutionServiceConfigurationBuilder()
//                .pool("diskStoreThreadPool", 1, 3)
//                .defaultPool("default", 1, 10)
//                .build();
//        var configuration = new DefaultConfiguration(
//                caches,
//                ehcacheProvider.getDefaultClassLoader(),
//                new DefaultPersistenceConfiguration(new File(persistenceDirectory))
////                diskStoreThreadPool
//        );
//
//        return new JCacheCacheManager(
//                ehcacheProvider.getCacheManager(
//                        ehcacheProvider.getDefaultURI(), configuration));
//    }

    private static CacheConfiguration<Long, Candidate> getCandidatesCacheConfiguration() {
        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        Long.class, Candidate.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder()
                                .heap(100, EntryUnit.ENTRIES)
                )
//                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(5)))
                .build();
    }

    private static CacheConfiguration<String, List> getCandidatesSearchCacheConfiguration() {
        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        String.class, List.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder()
                                .disk(10, MemoryUnit.MB, true)
                )
                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(5)))
                .withDiskStoreThreadPool("diskStoreThreadPool", 1)

                .build();
    }

    private static CacheConfiguration<String, List> getPositionsCacheConfiguration() {
        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        String.class, List.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder()
                                .heap(11, EntryUnit.ENTRIES)
                )
                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(5)))
                .withDiskStoreThreadPool("diskStoreThreadPool", 1)

                .build();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        File persistenceDir = new File(persistenceDirectory);
        if (persistenceDir.exists()) {
//            Files.deleteTree(persistenceDir.toPath());
            return;
        }
        persistenceDir.mkdirs();
        log.info("Created cache persistence directory: {}", persistenceDir);
    }

//    private static CacheConfiguration<Object, Object> getCandidatesCacheConfiguration() {
//        //                .withService(eventLoggerConfig)
//        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
//                        Object.class, Object.class,
//                        ResourcePoolsBuilder.newResourcePoolsBuilder()
//                                .heap(100, EntryUnit.ENTRIES)
//                                .offheap(1, MemoryUnit.GB)
//                                .disk(3, MemoryUnit.GB, true)
//                )
//                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(5)))
////                .withService(eventLoggerConfig)
//
//                .build();
//    }
}