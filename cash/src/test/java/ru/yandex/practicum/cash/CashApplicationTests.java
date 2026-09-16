package ru.yandex.practicum.cash;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.cash.config.ContractTestWebClientConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(ContractTestWebClientConfig.class)
class CashApplicationTests {

	@Test
	void contextLoads() {
	}

}
