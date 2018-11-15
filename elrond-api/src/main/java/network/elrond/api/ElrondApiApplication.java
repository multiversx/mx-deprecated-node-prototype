package network.elrond.api;

import network.elrond.core.ResponseObject;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.data.model.BootstrapType;

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
import java.util.Map;


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

        ResponseObject responseObject = CommandLinesInterpretor.interpretCommandLines(args);

        if (responseObject == null){
            logger.fatal("Internal error! Can not start node!");
            System.exit(1);
            return;
        }

        if (!responseObject.isSuccess()){
            System.exit(1);
            return;
        }

        logger.info("Starting ElrondApiApplication...");

        configurableApplicationContext = null;

        boolean isEnoughArguments = responseObject.getPayload() != null;

        if (isEnoughArguments) {
            StringBuilder stringBuilderParameters = new StringBuilder();

            @SuppressWarnings("unchecked")
			Map<String, Object> data = (Map<String, Object>) responseObject.getPayload();

            boolean isFirst = true;
            for (String key : data.keySet()){
                if (!isFirst){
                    stringBuilderParameters.append(", ");
                }
                isFirst = false;
                stringBuilderParameters.append(key);
                stringBuilderParameters.append(": ");
                stringBuilderParameters.append(data.get(key).toString());
            }


            logger.info("Will auto-start node with parameters: [{}]", stringBuilderParameters.toString());
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

                            elrondNodeController.startNode(null,
                                    data.get("node_name").toString(),
                                    (int)data.get("port"),
                                    (int)data.get("master_peer_port"),
                                    data.get("peer_ip").toString(),
                                    data.get("node_private_key").toString(),
                                    null,
                                    (BootstrapType)data.get("startup_type"));
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
            logger.info("Will not auto-start! Type -h or --help to get the command line options!");
        }

        configurableApplicationContext = SpringApplication.run(ElrondApiApplication.class, args);
    }
}
