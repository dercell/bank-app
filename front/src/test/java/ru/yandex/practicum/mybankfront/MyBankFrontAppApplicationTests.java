package ru.yandex.practicum.mybankfront;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mybankfront.config.ContractTestWebClientConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(ContractTestWebClientConfig.class)
class MyBankFrontAppApplicationTests {

	@Test
	void contextLoads() {
	}

}
