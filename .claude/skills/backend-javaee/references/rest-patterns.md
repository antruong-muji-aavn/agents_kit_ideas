---
name: backend/javaee/rest-patterns
description: "JAX-RS REST endpoint patterns for WildFly"
---

# JAX-RS REST Patterns

## Endpoint Structure

```java
@Path("/api/v1/messages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class MessageResource {

    @Inject
    private MessageService messageService;

    @GET
    public Response getMessages(@QueryParam("page") @DefaultValue("0") int page,
                                @QueryParam("size") @DefaultValue("20") int size) {
        List<MessageDTO> messages = messageService.findAll(page, size);
        return Response.ok(messages).build();
    }

    @GET
    @Path("/{id}")
    public Response getMessage(@PathParam("id") Long id) {
        MessageDTO message = messageService.findById(id);
        if (message == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(message).build();
    }

    @POST
    public Response createMessage(@Valid MessageCreateDTO dto) {
        MessageDTO created = messageService.create(dto);
        URI location = UriBuilder.fromResource(MessageResource.class)
            .path("{id}").build(created.getId());
        return Response.created(location).entity(created).build();
    }
}
```

## Error Handling

```java
@Provider
public class ConstraintViolationExceptionMapper
    implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException e) {
        Map<String, String> errors = e.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                v -> v.getPropertyPath().toString(),
                ConstraintViolation::getMessage
            ));
        return Response.status(422).entity(errors).build();
    }
}
```

## Conventions

- Always return `Response` objects (not raw types)
- Use `@Valid` for bean validation on DTOs
- Path versioning: `/api/v1/<resource>`
- Pagination via `@QueryParam` with defaults
- Produce JSON by default
