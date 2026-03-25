---
name: backend/javaee/testing-patterns
description: "Testing patterns: JUnit 4, Mockito, PowerMock, Arquillian"
---

# Backend Testing Patterns

## Unit Tests (JUnit 4 + Mockito)

```java
@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTest {

    @Mock
    private MessageDAO messageDAO;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private MessageService messageService;

    @Test
    public void shouldFindMessageById() {
        Message expected = new Message();
        expected.setId(1L);
        when(messageDAO.find(1L)).thenReturn(expected);

        MessageDTO result = messageService.findById(1L);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        verify(messageDAO).find(1L);
    }

    @Test
    public void shouldReturnNullForMissingMessage() {
        when(messageDAO.find(999L)).thenReturn(null);

        MessageDTO result = messageService.findById(999L);

        assertNull(result);
    }
}
```

## PowerMock (legacy static methods)

```java
@RunWith(PowerMockRunner.class)
@PrepareForTest(LegacyHelper.class)
public class LegacyServiceTest {

    @Test
    public void shouldHandleStaticCall() {
        PowerMockito.mockStatic(LegacyHelper.class);
        when(LegacyHelper.compute(any())).thenReturn("result");

        // test code
    }
}
```

Note: Prefer refactoring over PowerMock for new code.

## Integration Tests (Arquillian)

```java
@RunWith(Arquillian.class)
public class MessageResourceIT {

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
            .addPackage(MessageResource.class.getPackage())
            .addAsResource("test-persistence.xml", "META-INF/persistence.xml");
    }

    @ArquillianResource
    private URL baseURL;

    @Test
    public void shouldReturnMessages() {
        Response response = ClientBuilder.newClient()
            .target(baseURL + "api/v1/messages")
            .request(MediaType.APPLICATION_JSON)
            .get();

        assertEquals(200, response.getStatus());
    }
}
```

## Test Configuration

- `src/test/resources/test-persistence.xml` — H2 in-memory DB for tests
- `src/test/resources/arquillian.xml` — Arquillian container config
- Coverage: JaCoCo (0.8.4) with SonarQube reporting
