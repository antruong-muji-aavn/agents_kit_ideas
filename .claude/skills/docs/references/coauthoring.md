# Doc Co-Authoring Workflow

Structured workflow for guiding users through collaborative document creation. Act as an active guide, walking users through three stages: Context Gathering, Refinement & Structure, and Reader Testing.

## When to Offer This Workflow

**Trigger conditions:**
- User mentions writing documentation: "write a doc", "draft a proposal", "create a spec", "write up"
- User mentions specific doc types: "PRD", "design doc", "decision doc", "RFC"
- User seems to be starting a substantial writing task

**Initial offer:**
Offer the user a structured workflow for co-authoring the document. Explain the three stages:

1. **Context Gathering**: User provides all relevant context while Claude asks clarifying questions
2. **Refinement & Structure**: Iteratively build each section through brainstorming and editing
3. **Reader Testing**: Test the doc with a fresh Claude (no context) to catch blind spots before others read it

Ask if they want to try this workflow or prefer to work freeform. If user declines, work freeform.

## Stage 1: Context Gathering

**Goal:** Close the gap between what the user knows and what Claude knows, enabling smart guidance later.

### Initial Questions

1. What type of document is this? (e.g., technical spec, decision doc, proposal)
2. Who's the primary audience?
3. What's the desired impact when someone reads this?
4. Is there a template or specific format to follow?
5. Any other constraints or context to know?

### Info Dumping

Once initial questions are answered, encourage the user to dump all the context they have:
- Background on the project/problem
- Related team discussions or shared documents
- Why alternative solutions aren't being used
- Organizational context (team dynamics, past incidents, politics)
- Timeline pressures or constraints
- Technical architecture or dependencies

**During context gathering:**
- If user mentions team channels or shared documents: read via integrations if available, else ask them to paste content
- Track what's being learned and what's still unclear

**Asking clarifying questions:**
When user signals they've done their initial dump, ask 5-10 numbered questions based on gaps in the context.

**Exit condition:**
Sufficient context when questions show understanding — when edge cases and trade-offs can be asked about without needing basics explained.

## Stage 2: Refinement & Structure

**Goal:** Build the document section by section through brainstorming, curation, and iterative refinement.

For each section:
1. Ask 5-10 clarifying questions about what to include
2. Brainstorm 5-20 options depending on section complexity
3. User indicates what to keep/remove/combine
4. Draft the section
5. Refine through surgical edits

**Section ordering:**
Suggest starting with whichever section has the most unknowns. For decision docs: core proposal. For specs: technical approach. Summary sections are best left for last.

**For each section:**

### Step 1: Clarifying Questions
Announce work on [SECTION NAME]. Ask 5-10 specific questions.

### Step 2: Brainstorming
Brainstorm 5-20 numbered options. Offer to brainstorm more if needed.

### Step 3: Curation
Ask which points to keep, remove, or combine.

### Step 4: Gap Check
Check if anything important is missing.

### Step 5: Drafting
Replace placeholder text with drafted content. Use `str_replace` for edits — never reprint the whole doc.

### Step 6: Iterative Refinement
Continue iterating until user is satisfied. After 3 consecutive iterations with no substantial changes, ask if anything can be removed.

### Near Completion
At 80%+ sections done, re-read the entire document and check for: flow, consistency, redundancy, contradictions, generic filler.

## Stage 3: Reader Testing

**Goal:** Test the document with a fresh Claude (no context bleed) to verify it works for readers.

**If sub-agents are available (Claude Code):**

1. Generate 5-10 questions readers would realistically ask
2. Test with a sub-agent using just the document content + question
3. Run additional checks for ambiguity, false assumptions, contradictions
4. Report findings and loop back to refinement for issues

**If no sub-agents available:**

1. Generate 5-10 realistic reader questions
2. Have user open a fresh Claude conversation and test the questions
3. Ask Reader Claude: "What is ambiguous?" / "What knowledge does this assume?" / "Any internal contradictions?"
4. Iterate based on results

**Exit condition:** Reader Claude consistently answers questions correctly and doesn't surface new gaps.

## Final Review

When Reader Testing passes:
1. Recommend user do a final read-through — they own this document
2. Suggest double-checking facts, links, technical details
3. Ask them to verify it achieves the desired impact

**Final tips:**
- Consider linking this conversation in an appendix
- Use appendices to provide depth without bloating the main doc
- Update the doc as feedback is received from real readers

## Tips for Effective Guidance

- Be direct and procedural
- If user wants to skip a stage: ask if they want to write freeform
- If user seems frustrated: suggest ways to move faster
- Use `create_file` for drafting full sections
- Use `str_replace` for all edits
- Never use artifacts for brainstorming lists — that's just conversation
