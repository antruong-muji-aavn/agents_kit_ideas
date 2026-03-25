---
name: backend/javaee/persistence-patterns
description: "JPA/Hibernate and MongoDB persistence patterns"
---

# Persistence Patterns

## JPA Entity

```java
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Organization sender;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Enumerated(EnumType.STRING)
    private MessageStatus status;
}
```

## DAO Pattern

```java
@ApplicationScoped
public class MessageDAO {

    @PersistenceContext
    private EntityManager em;

    public Message find(Long id) {
        return em.find(Message.class, id);
    }

    public List<Message> findAll(int page, int size) {
        return em.createQuery("SELECT m FROM Message m ORDER BY m.createdAt DESC", Message.class)
            .setFirstResult(page * size)
            .setMaxResults(size)
            .getResultList();
    }

    public void persist(Message message) {
        em.persist(message);
    }
}
```

## MongoDB (document store)

Used for: audit logs, analytics events, flexible-schema documents.

```java
@ApplicationScoped
public class AuditLogRepository {

    @Inject
    private MongoClient mongoClient;

    public void log(AuditEvent event) {
        MongoCollection<Document> collection = mongoClient
            .getDatabase("epost")
            .getCollection("audit_logs");

        Document doc = new Document()
            .append("action", event.getAction())
            .append("userId", event.getUserId())
            .append("timestamp", new Date());
        collection.insertOne(doc);
    }
}
```

## Dual Database Strategy

| PostgreSQL | MongoDB |
|-----------|---------|
| Transactional data | Event/audit logs |
| Relational queries | Document queries |
| ACID compliance | Flexible schema |
| JPA/Hibernate | Java driver direct |
