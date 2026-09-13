package ru.yandex.practicum.transfer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.transfer.config.ContractTestWebClientConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(ContractTestWebClientConfig.class)
class TransferApplicationTests {

	@Test
	void contextLoads() {
	}

}
