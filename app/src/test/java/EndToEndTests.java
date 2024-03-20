import com.revproxy.RevProxyApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RevProxyApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.main.web-application-type=reactive"}
)
public class EndToEndTests {

    @LocalServerPort
    private int port;

    @Test
    public void smokeTest(){
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<String> response = testRestTemplate.getForEntity( "http://127.0.0.1:" + port + "/", String.class);

        assertNotNull("Should receive a response", response);
        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    }
}
