# Issue tracker: Local Markdown

Private agent issues and specs live as Markdown files in `.scratch/`, which is excluded from Git.

## Conventions

- One effort per directory: `.scratch/<effort-slug>/`
- The specification is `.scratch/<effort-slug>/spec.md`
- Implementation tickets live at `.scratch/<effort-slug>/issues/<NN>-<slug>.md`
- Each ticket has a `Status:` line near the top
- Comments and history are appended under `## Comments`
- Dependencies use a `Blocked by: NN, NN` line

## Publishing and fetching

When a skill says “publish to the issue tracker,” create the appropriate file under `.scratch/<effort-slug>/`.

When a skill says “fetch the relevant ticket,” read the referenced local file.

## Wayfinding

- Map: `.scratch/<effort-slug>/map.md`
- Tickets: `.scratch/<effort-slug>/issues/<NN>-<slug>.md`
- Ticket types: `research`, `prototype`, `grilling`, or `task`
- Wayfinding statuses: `claimed` or `resolved`
- Resolve a ticket by appending its result under `## Answer`, setting `Status: resolved`, and adding a summary pointer to the map
