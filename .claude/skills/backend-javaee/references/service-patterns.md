---
name: backend/javaee/service-patterns
description: "CDI and EJB service layer patterns"
---

# Service Layer Patterns

## CDI Bean (preferred for new code)

```java
@ApplicationScoped
@Transactional
public class MessageService {

    @Inject
    private MessageDAO messageDAO;

    @Inject
    private NotificationService notificationService;

    public MessageDTO findById(Long id) {
        Message entity = messageDAO.find(id);
        return entity != null ? toDTO(entity) : null;
    }

    public MessageDTO create(MessageCreateDTO dto) {
        Message entity = fromDTO(dto);
        messageDAO.persist(entity);
        notificationService.notifyRecipient(entity);
        return toDTO(entity);
    }
}
```

## Stateless EJB (existing legacy code)

```java
@Stateless
public class LegacyMessageService {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private AuditService auditService;

    // Container-managed transactions
    public Message create(Message message) {
        em.persist(message);
        auditService.log("MESSAGE_CREATED", message.getId());
        return message;
    }
}
```

## When to Use What

| Pattern | Use When |
|---------|----------|
| `@ApplicationScoped` + CDI | New services, most cases |
| `@Stateless` EJB | Needs EJB features (timer, async, remote) |
| `@RequestScoped` | Per-request state needed |
