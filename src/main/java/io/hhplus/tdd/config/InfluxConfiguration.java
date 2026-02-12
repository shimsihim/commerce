package io.hhplus.tdd.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.influx.InfluxApiVersion;
import io.micrometer.influx.InfluxConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;

@Configuration
public class InfluxConfiguration {

    @Value("${management.metrics.export.influx.uri}")
    private String uri;

    @Value("${management.metrics.export.influx.org}")
    private String org;

    @Value("${management.metrics.export.influx.bucket}")
    private String bucket;

    @Value("${management.metrics.export.influx.token}")
    private String token;

    @Value("${management.metrics.export.influx.step}")
    private Duration step;

    @Value("${spring.application.name:unknown-app}")
    private String appName;

    @Value("${ENVIRONMENT:dev}")
    private String environment;

    /**
     * [1] InfluxDB 연결 설정 (환경변수 적용 + V2 강제)
     */
    @Bean
    public InfluxConfig influxConfig() {
        return new InfluxConfig() {
            @Override
            public String get(String key) {
                return null; // 오버라이드 메서드 우선
            }
            @Override
            public String uri() { return uri; }
            @Override
            public String org() { return org; }
            @Override
            public String bucket() { return bucket; }
            @Override
            public String token() { return token; }
            @Override
            public InfluxApiVersion apiVersion() { return InfluxApiVersion.V2; } // V2 강제
            @Override
            public Duration step() { return step; }
        };
    }

    /**
     * 모든 메트릭에 'application' 이름, 'environment', 'host' 정보를 자동으로 붙임.
     * 그라파나에서 서버 식별 및 환경 구분 가능.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
                "application", appName,
                "environment", environment,
                "host", getHostName()
        );
    }

    /**
     * 메트릭 필터링 설정
     * - percentile 설정은 application.yml에서 관리됨
     * - 필요시 특정 메트릭을 필터링할 수 있음
     */
    @Bean
    public MeterFilter meterFilter() {
        return MeterFilter.deny(id -> {
            // (선택) 너무 자잘한 JVM 메트릭이 싫으면 여기서 거를 수 있음
            // return id.getName().startsWith("jvm.buffer");
            return false;
        });
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown-host";
        }
    }
}