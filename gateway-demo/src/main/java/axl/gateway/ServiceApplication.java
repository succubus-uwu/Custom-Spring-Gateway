package axl.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableScheduling
public class ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }

    @RestController
    public static class DemoController {

        @GetMapping("/api/hello")
        public String hello(@RequestHeader(value = "X-User-Id", required = false) String userId) {
            return "Hello from demo-service! " + userId;
        }
    }
}
