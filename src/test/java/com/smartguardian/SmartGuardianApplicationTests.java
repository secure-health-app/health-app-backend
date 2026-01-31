package com.smartguardian;

import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Disabled in CI: full context requires security + database wiring")
@SpringBootTest
class SmartGuardianApplicationTests {
}
