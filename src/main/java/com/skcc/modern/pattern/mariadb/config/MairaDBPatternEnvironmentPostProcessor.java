package com.skcc.modern.pattern.mariadb.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.MalformedURLException;

@Order(Ordered.LOWEST_PRECEDENCE)
public class MairaDBPatternEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MairaDBPatternEnvironmentPostProcessor.class);

    private void addLast(ConfigurableEnvironment environment, Resource resource) throws IOException {
        environment.getPropertySources().addLast(new PropertiesPropertySourceLoader().load("dbaccess", resource).get(0));
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
            // K8S로 배포한 설정을 먼저 확인하고 로컬에 있는 설정을 확인하는 방식으로 구성
            try {
                // K8S 환경에서 Mount 시켜줘야 함
                // - /pattern/application-circuitbreaker.yml 이 기존 정책으로 application 설정 파일 임
                // - 해당 설정이 우선순위를 하위로 낮추는 역할을 하기 때문에 위 경로 설정은 사실 사용자 입장에서는 무의미할 수 있음
                Resource k8sPath = new FileUrlResource("/pattern/dbaccess.yml");

                // 패키지된 기본 설정값 : resources/pattern.yml
                Resource localPath = new ClassPathResource("dbaccess.yml");

                if (k8sPath.exists()) {
                    logger.error("Load k8s Path file : " + k8sPath.getFilename());
                    addLast(environment, k8sPath);
                } else if (localPath.exists()) {
                    logger.error("Load Local Path file : " + localPath.getFilename());
                    addLast(environment, localPath);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

    }
}
