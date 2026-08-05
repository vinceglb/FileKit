# Domain documentation

FileKit uses a single-context domain model.

## Before exploring

Read these when they exist:

- `specs/CONTEXT.md` for FileKit’s glossary and domain model
- Relevant decisions under `specs/adr/`

If they do not exist, proceed silently. Create them lazily through the domain-modeling workflow when terminology or architectural decisions are resolved.

## Structure

```text
/
└── specs/
    ├── CONTEXT.md
    ├── adr/
    └── agents/
```

## Vocabulary

Use domain terms as defined in `specs/CONTEXT.md`. If a necessary concept is missing, reconsider the terminology or record the gap for domain modeling.

## ADR conflicts

Explicitly surface proposals that contradict an existing ADR instead of silently overriding it.
