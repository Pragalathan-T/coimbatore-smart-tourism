package com.yourorg.tourism;

import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TourismApplicationTests {

	static {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		System.setProperty("user.timezone", "UTC");
	}

	@Test
	void contextLoads() {
	}

}
