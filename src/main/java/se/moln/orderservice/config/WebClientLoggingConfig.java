package se.moln.orderservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientLoggingConfig {
    private static final Logger LOG = LoggerFactory.getLogger(WebClientLoggingConfig.class);

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        ExchangeFilterFunction logReq = (req, next) -> {
            LOG.debug("WEBCLIENT -> {} {}", req.method(), req.url());
            return next.exchange(req);
        };
        ExchangeFilterFunction logRes = (req, next) ->
                next.exchange(req).flatMap(res -> {
                    LOG.debug("WEBCLIENT <- {} {}", res.statusCode().value(), req.url());
                    return Mono.just(res);
                });
        return builder.filter(logReq).filter(logRes).build();
    }
}
