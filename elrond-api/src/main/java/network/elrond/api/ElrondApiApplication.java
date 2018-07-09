package network.elrond.api;

import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.data.BootstrapType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


@SpringBootApplication
@EnableSwagger2
public class ElrondApiApplication {
    private static final Logger logger = LogManager.getLogger(ElrondApiApplication.class);

    public static ConfigurableApplicationContext configurableApplicationContext = null;

    /**
     * Configure swagger-ui
     *
     * @return
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    public static void main(String[] args) {
        SimpleDateFormat sdfSource = new SimpleDateFormat(
                "yyyy-MM-dd HH.mm.ss");
        Util.changeLogsPath("logs/" + Util.getHostName() + " - " + sdfSource.format(new Date()));

        logger.info("Starting ElrondApiApplication...");

        configurableApplicationContext = null;

        boolean isEnoughArguments = (args != null) && (args.length == 5);

        if (isEnoughArguments) {
            logger.info("Will auto-start node with parameters: {}", Arrays.asList(args));
            Thread threadAutoStart = new Thread(() -> {
                while (true) {
                    ThreadUtil.sleep(1000);

                    if (configurableApplicationContext == null) {
                        continue;
                    }

                    if (configurableApplicationContext.isActive()) {
                        logger.info("Auto-starting node with parameters: {}", Arrays.asList(args));

                        try {
                            ElrondNodeController elrondNodeController = configurableApplicationContext.getBean(ElrondNodeController.class);

                            elrondNodeController.startNode(null, args[0], Integer.valueOf(args[1]), Integer.valueOf(args[2]), args[3], args[4],
                                    null, BootstrapType.START_FROM_SCRATCH, args[0], args[0]);
                        } catch (Exception ex) {
                            logger.catching(ex);
                            logger.error("Can not auto-start node!");
                        }
                    } else {
                        logger.error("Can not auto-start node!");
                    }

                    return;
                }
            });
            threadAutoStart.start();
        } else {
            logger.info("Will not auto-start! Expected 5 arguments: node_name, port, master_port, ip, private_key");
        }

        configurableApplicationContext = SpringApplication.run(ElrondApiApplication.class, args);
    }
}
