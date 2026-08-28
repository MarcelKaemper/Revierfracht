package de.mkaemper.revierfracht;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PostgisIntegrationTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void postgisExtensionIsInstalled() {
		Integer count = jdbcTemplate.queryForObject(
				"select count(*) from pg_extension where extname = 'postgis'", Integer.class);

		assertThat(count).isEqualTo(1);
	}

}
